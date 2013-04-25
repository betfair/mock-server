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

import com.betfair.utils.mockserver.storage.Recorder
import com.betfair.utils.mockserver.util.URLHelper
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.HttpResponseException
import org.apache.http.impl.cookie.BasicClientCookie
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

import javax.servlet.http.HttpServletRequest
import groovyx.net.http.RESTClient

/**
 * Created with IntelliJ IDEA.
 * User: wambeekm
 * Date: 13/11/12
 * Time: 15:32
 * To change this template use File | Settings | File Templates.
 */
class Forwarder {
    @Value('${mockServicesDomain}') String DOMAIN
    @Value('${fileFormat}') String FILE_FORMAT
    @Value('${recordFailedResponses}') boolean RECORD_FAILED_RESPONSES
    @Autowired URLHelper urlHelper
    @Autowired Recorder recorder

    private static final Logger LOG = LoggerFactory.getLogger(Forwarder.class);

    private static final int SESSION_COOKIE = -1
    private static final int START_OF_TIME = 10
    private static final long MILLIS_PER_SECOND = 1000L


    public Forwarder(){

    }

    /**
     * Forwards a request to the real service
     *
     * @param request The HttpServletRequest from the controller
     * @param body The POST body if there is one
     * @return
     */
    def HttpResponseDecorator forward(HttpServletRequest request, String filename, String body = null){
        def names = request.headerNames
        def headers = ["accept":"application/json"]
        for(name in names){
            headers[name.toLowerCase()] = request.getHeader(name).replace("[", "").replace("]", "")
        }
        def cookies = request.cookies
        headers.remove("accept-encoding")
        headers.remove("content-length")
        headers.remove("transfer-encoding")
//        headers.remove("cneonction")
//        headers.remove("connection")
//        headers.remove("content-encoding")
        headers.remove("cookie")
        def url = urlHelper.urlFixer(request.requestURL.toString())

        def path = request.requestURI

        def query = request.queryString
//        def query = splitQueryString(request.queryString)
//        if(request.queryString){
//            query = URLDecoder.decode(request.queryString)
//        }

        def cookieJar = ""
        cookies.each { badCookie ->
            def goodCookie = buildCookie(badCookie)
            cookieJar += "$goodCookie.name=$goodCookie.value"
            if(badCookie != cookies.last()){
                cookieJar += "; "
            }
        }
        headers["cookie"] = cookieJar

        println "Forwarding Request: '$request.requestURL' to '$url$path?$query'"
        //don't know... but it works....
        if(query){
            url += "?" + query
        }

        RESTClient restClient = new RESTClient(url)
        HttpResponseDecorator response = null

        try{
            response = (HttpResponseDecorator)(restClient."${request.method.toLowerCase()}"(headers:headers, path:path, queryString:query, body:body, contentType:"application/json;charset=utf-8"))
            println "Response from $url is $response.statusLine"
            //TODO make this way of saveSessionServiceCalls() work
//            Thread.start {
//                println "NORMAL! ${Thread.currentThread().name}\n\tFileNAME: $filename\n\tRequest:$request\n\tBody: $body\n\tResponse:$response"
//                recorder.saveSessionServiceCalls(filename, request, body, response)
//            }
            recorder.saveSessionServiceCalls(filename, request, body, response)
        }
        catch(HttpResponseException e){
            println """*************************\nERROR: $e.message
Response from $url is $e.response.data
Path: $path?$query
Old url is $request.requestURL
*******************************************"""
            response = e.response
            //if recording failed responses from services is true
            if(RECORD_FAILED_RESPONSES){
//                Thread.start {
//                    println "FAILED ${Thread.currentThread().name}\n\tFileNAME: $filename\n\tRequest:$request\n\tBody: $body\n\tResponse:$response"
//                    recorder.saveSessionServiceCalls(filename, request, body, response)
//                }
                recorder.saveSessionServiceCalls(filename, request, body, response)
            }
        }

        return response
    }

    /**
     * Splits a query string into a key:value map
     *
     * @param queryString The query string from the request url
     * @return The key:value map
     */
    def splitQueryString(String queryString){
        println "query:\t" + queryString
        if(!queryString){
            return null
        }
        def queryArray = queryString.split("&")
        def queryMap = [:]
        for(item in queryArray){
            def pair = item.split("=")
            queryMap[pair[0]] = URLDecoder.decode(pair[1])
        }
    }

    /**
     * Create the session file
     *
     * @param uuid
     */
    def createSessionFile(String uuid){
        recorder.createSessionFile(uuid)
    }

    /**
     * Create the session file with the given name
     *
     * @param uuid
     * @param directoryName
     */
    def createSessionFile(String uuid, String directoryName){
        recorder.createSessionFile(uuid, directoryName)
    }

    /**
     * Converts a javax cookie to an apache cookie
     *
     * @param badCookie the javax cookie
     * @return The Apache cookie
     */
    private BasicClientCookie buildCookie(javax.servlet.http.Cookie badCookie){
        def goodCookie = new BasicClientCookie(badCookie.name, badCookie.value)
        goodCookie.with {
            if(badCookie.comment){
                comment = badCookie.comment
            }
            if(badCookie.domain){
                domain = badCookie.domain
            }
            if(badCookie.path){
                path = badCookie.path
            }
            if(badCookie.secure){
                secure = badCookie.secure
            }
            if(badCookie.version){
                version = badCookie.version
            }
            if(badCookie.maxAge && badCookie.maxAge != SESSION_COOKIE){
                expiryDate = convertMaxAgeToExpiry(badCookie.maxAge)
            }
        }
        return goodCookie
    }

    /**
     * Coverts the javax "max age" cookie field into the apache's "expiry" field
     *
     * @param maxAge The javax maxAge
     * @return The equivalent expiry value
     */
    public static Date convertMaxAgeToExpiry(int maxAge) {
        if (maxAge == SESSION_COOKIE) {
            return null;
        }

        if (maxAge <= 0) {
            // Cookie is to be removed
            return new Date(START_OF_TIME);
        }
        return new Date(System.currentTimeMillis() + maxAge * MILLIS_PER_SECOND);
    }

    def copyObj(orig) {
        def bos = new ByteArrayOutputStream()
        def oos = new ObjectOutputStream(bos)
        oos.writeObject(orig); oos.flush()
        def bin = new ByteArrayInputStream(bos.toByteArray())
        def ois = new ObjectInputStream(bin)
        return ois.readObject()
    }

}
