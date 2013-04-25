/*
 * Copyright (c) 2012 The Sporting Exchange Limited
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1.	Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2.	Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3.	Neither the names of The Sporting Exchange Limited, Betfair Limited nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */



package com.betfair.utils.mockserver

//import org.apache.http.HttpHostimport


import com.betfair.utils.mockserver.message.ExpectationRequest
import com.betfair.utils.mockserver.message.SessionRequest
import com.betfair.utils.mockserver.playback.Playback
import com.betfair.utils.mockserver.storage.LoadedExpectations
import com.betfair.utils.mockserver.util.ClientRequest
import com.betfair.utils.mockserver.util.Maybe
import groovy.json.JsonBuilder
import groovyx.net.http.HttpResponseDecorator
import org.apache.http.HttpHost
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.springframework.web.bind.annotation.*

import static org.springframework.http.HttpStatus.*

@Controller
class HttpRequestController {
    public static final String SESSION_URL_PREFIX = "/sessions?sessionId="
    public static final String EXPECTATION_URL_PREFIX = "/expectations?expectationId="
    
    Sessions sessions
    Map<String, String> recordingSessions = [:]
    @Autowired Forwarder forwarder
    @Autowired Playback playback
    @Value('${mockServerHost}') private final String MOCK_SERVER_HOST
    @Value('${mockServerPort}') private final int  MOCK_SERVER_PORT

    HttpRequestController(Sessions sessions) {
        this.sessions = sessions
    }

    HttpRequestController() {
        this(new Sessions())
    }

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestController.class);

    @RequestMapping(value = "/sessions", method = RequestMethod.POST)
    @ResponseStatus(CREATED)
    public @ResponseBody
    String createSession(@RequestBody SessionRequest session, HttpServletRequest request) {
        //create a file with the x-uuid used as the session id (to be used for record mode)
        if(request.getHeader("RECORD") && request.getHeader("RECORD").toUpperCase() != "FALSE"){
            def uuid = request.getHeader("X-UUID")

            def recordingName = request.getHeader("RECORD")
            if(recordingName.toUpperCase() == "TRUE"){
                recordingName = uuid
            }
            forwarder.createSessionFile(uuid, recordingName)
            def oldVal = recordingSessions.put(uuid, recordingName)
            if(!oldVal){
                LOG.debug("Starting Recording for UUID: $uuid")
            }
            else{
                if(recordingName == oldVal){
                    LOG.debug("The UUID $uuid is already Recording.")
                    recordingSessions.put(uuid, oldVal)
                    throw new IOException("The UUID '${(uuid != recordingName) ? (recordingName + '- ') : ''}$uuid' is already Recording.")
                }
                else{
                    LOG.debug("The UUID $uuid could not be added to the Recording List:\n$recordingSessions")
                    recordingSessions.put(uuid, oldVal)
                    throw new IOException("Unable to start recording for $uuid!\nRecording UUIDs: $recordingSessions")
                }
            }
        }
        else if(request.getHeader("PLAYBACK") && request.getHeader("PLAYBACK") != "FALSE"){
            def recordingName = request.getHeader("PLAYBACK")
            def sessionId = sessions.setUpSession(session)
            LOG.debug("Starting Playback for Session $sessionId - '$recordingName'")
            def uuid = createPlaybackExpectation(recordingName, sessionId)
            return new JsonBuilder([session:sessionId, uuid:uuid])

        }
        else if(request.getHeader("PASSTHROUGH") && request.getHeader("PASSTHROUGH").toUpperCase() == "TRUE"){
            //TODO Add pass-through capabilities (Forward requests to services without recording and without using UUIDs)
        }
        def sessionId = sessions.setUpSession(session)
        LOG.debug("session created ${sessionId}")
        return SESSION_URL_PREFIX + sessionId
    }

    @RequestMapping(value = "/playback", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.FOUND)
    public @ResponseBody
    String findUUID(HttpServletRequest request) {
        def name = request.getParameter("name")
        if(name){
            def returnString =  """{
        "name":"$name",
        "uuid":"${playback.findUUID(name)}"
}"""
            return returnString
        }
        else{
            throw new Exception("You must supply the parameter 'name' - eg. /playback?name=test")
        }

    }

    @RequestMapping(value = "/sessions", method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity<String> readSession(@RequestParam(value="sessionId", required=false) Long sessionId) {
        LOG.debug("session read ${sessionId}")
        if (sessionId == null) respondWithContent(OK, sessions.listSessions(SESSION_URL_PREFIX), "GET /sessions")
        else respondWithContent(sessions.readSession(sessionId), "GET /sessions")
    }

    @RequestMapping(value = "/sessions", method = RequestMethod.DELETE)
    @ResponseStatus(OK)
    public @ResponseBody
    ResponseEntity<String> removeSession(@RequestParam(value="sessionId", required=true) Long sessionId, HttpServletRequest request) {
        def uuid = request.getHeader("X-UUID")
        if(uuid){
            if(recordingSessions.remove(uuid)){
                LOG.debug("End of Recording: $uuid")
            }
            else{
                LOG.debug("$uuid was not recording.")
            }
        }

        LOG.debug("session removed ${sessionId}")
        respondWithContent(sessions.removeSession(sessionId), uuid)
    }



    @RequestMapping(value = "/expectations", method = RequestMethod.POST)
    @ResponseStatus(CREATED)
    public @ResponseBody
    String createExpectation(@RequestParam(value="sessionId", required=true) Long sessionId,
                             @RequestBody ExpectationRequest expectation) {
        LOG.debug("on session ${sessionId} expectation for ${expectation.when} created")
        EXPECTATION_URL_PREFIX + sessions.setUpExpectation(sessionId, expectation)
    }

    @RequestMapping(value = "/expectations", method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity<String> readExpectations(@RequestParam(value="sessionId", required=true) Long sessionId) {
        LOG.debug("expectations read ${sessionId}")
        respondWithContent(sessions.listExpectations(sessionId), "/expectations")
    }



    @RequestMapping(value = "/**", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    ResponseEntity<String> handleRequestUrlsGet(HttpServletRequest request, HttpServletResponse response) {
        //get the x-uuid or x-requestuuid from the service call (will be unified to always be x-uuid in future)
        def uuid = request.getHeader("X-UUID") ? request.getHeader("X-UUID") : request.getHeader("X-REQUESTUUID")
        def filename = recordingSessions.get(uuid)
        if(filename){
            //save the service call to the session file
            def rsp = forwarder.forward(request, filename)
            LOG.trace("forward handled ${request.requestURI}")
            if(rsp){
                def statusCode = HttpStatus.valueOf(rsp.statusLine.statusCode)
                respondWithContent(statusCode, rsp, uuid)
            }
            else{
                LOG.debug "Response: " + rsp.toString()
                respondWithContent(I_AM_A_TEAPOT, "An Error occured in the mock server while forwarding the response", uuid)
            }
        }
        else{
            LOG.trace("invocation handled ${request.requestURI}")
            def returning = sessions.respondTo(new ClientRequest(request), response)
                    .otherwise{respondWithContent(NOT_FOUND, "Please set up expectations for the url " + request.getRequestURI(), uuid)}
//            LOG.debug "RESPONSE OBJECT: " + returning.toString() + "<UUID:$uuid>"
            return returning
        }
    }

    @RequestMapping(value = "/**", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    ResponseEntity<String> handleRequestUrlsPost(HttpServletRequest request, HttpServletResponse response, @RequestBody String body) {
        //get the x-uuid or x-requestuuid from the service call (will be unified to always be x-uuid in future)
        def uuid = request.getHeader("X-UUID") ? request.getHeader("X-UUID") : request.getHeader("X-REQUESTUUID")
        def filename = recordingSessions.get(uuid)
        if(filename){
            //save the service call to the session file
            def rsp = forwarder.forward(request, filename, body)
            LOG.trace("forward handled ${request.requestURI}")
            if(rsp){
                def statusCode = HttpStatus.valueOf(rsp.statusLine.statusCode)
                respondWithContent(statusCode, rsp, uuid)
            }
            else{
                LOG.debug "Response: " + rsp.toString()
                respondWithContent(I_AM_A_TEAPOT, "An Error occured in the mock server while forwarding the response", uuid)
            }
        }
        else{
            //request.metaClass.getInputStream = { super.getInputStream() }
            LOG.debug("invocation handled ${request.requestURI}")
            def returning = sessions.respondTo(new ClientRequest(request, body), response)
                    .otherwise{respondWithContent(NOT_FOUND, "Please set up expectations for the url " + request.getRequestURI(), uuid)}
//            LOG.debug "RESPONSE OBJECT: " + returning.toString() + "<UUID:$uuid>"
            return returning
        }
    }

    /**
     *
     * @param status
     * @param s
     * @return
     */
    ResponseEntity<String> respondWithContent(HttpStatus status, String s, String uuid) {
        def rsp = new ResponseEntity<String>(s, new HttpHeaders(), status)
//        LOG.debug("RESPONSE OBJECT: ${rsp.toString()}" + "<UUID:$uuid>")
        return rsp
    }

    /**
     *
     * @param status
     * @param response
     * @return
     */
    ResponseEntity<String> respondWithContent(HttpStatus status, HttpResponseDecorator response, String uuid){
        def httpHeaders = new HttpHeaders()
        response.headers.each {header ->
            def name = header.name.toUpperCase()
            //ignore headers as these are either set by http clients or are no longer needed
            if(name != "CONTENT-LENGTH" && name != "NTCOENT-LENGTH" && name != "CONTENT-ENCODING"){
                httpHeaders.add(header.name, header.value)
            }
            else{
            }
        }
        def data = response.data
        if(data){
            data=data.toString()
        }
        else{
            data=""
        }
        def rsp = new ResponseEntity<String>(data, httpHeaders, status)
//        LOG.debug("RESPONSE OBJECT: ${rsp.toString()}" + "<UUID:$uuid>")
        return rsp
    }

    /**
     *
     * @param s
     * @return
     */
    ResponseEntity<String> respondWithContent(Maybe<String> s, String uuid) {
        def rsp = new ResponseEntity<String>(
                s.otherwise("not found"),
                new HttpHeaders(),
                s.isKnown() ? OK : NOT_FOUND)
//        LOG.debug("RESPONSE OBJECT: ${rsp.toString()}" + "<UUID:$uuid>")
        return rsp
    }



    /**
     * Creates an expectation during playback using the uuid to find a saved session call from file
     *
     * @param name The name of the expectations file
     * @param sessionId The session id that was created to load the expectations into
     * @return The UUID
     */
    def String createPlaybackExpectation(String name, long sessionId){
        //create list of string expectations
        LoadedExpectations loadedExpectations = playback.run(name)
        //send calls internally to be picked up by the createExpectation method
        def httpClient = new DefaultHttpClient()
        for(expectation in loadedExpectations.expectations){
            //TODO: work with all currency symbols, or make expectations accept the characters
            def currencyEscapedExp = expectation.replace("£","\\u00a3")//.replace("/", "\\/")
            LOG.trace("Sending expectation to self with session ID $sessionId")
            def httpMessage = new HttpPost("/expectations?sessionId=$sessionId")
            httpMessage.addHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8")
            def entity = new StringEntity(currencyEscapedExp,"UTF-8")
            entity.setContentType("application/json;charset=utf-8")
            httpMessage.entity = entity
            def response = httpClient.execute(new HttpHost(MOCK_SERVER_HOST, MOCK_SERVER_PORT), httpMessage)
            if (response.statusLine.statusCode != org.apache.http.HttpStatus.SC_CREATED) {
                EntityUtils.consume(response.entity)
                throw new Exception("Unable to create expectation : $currencyEscapedExp")
            }
            //consume body to allow httpClient to be re-used
            EntityUtils.consume(response.entity)
        }
        return loadedExpectations.uuid
    }

}

