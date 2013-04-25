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



package com.betfair.utils.mockserver.storage

import com.betfair.utils.mockserver.util.URLHelper
import groovyx.net.http.HttpResponseDecorator
import org.springframework.beans.factory.annotation.Value

import javax.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created with IntelliJ IDEA.
 * User: wambeekm
 * Date: 15/11/12
 * Time: 14:54
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
class CSVRecorder extends Recorder{
    @Value('${csvSeparator}') public static final String DEFAULT_SEPARATOR

    @Autowired URLHelper urlHelper

    public CSVRecorder(){
        super()
    }


    /**
     * Creates the session file and parent directories if they don't exist
     *
     * @param uuid the session id
     * @return
     */
    @Override
    def createSessionFile(String uuid){
        if(!uuid){
            return
        }
        File sessionFile = null
        if(System.getProperty("os.name").contains("Windows")){
            sessionFile = new File("C:\\etc\\mock-server\\sessions\\sessionCall_${uuid}.csv")
        }
        else{
            sessionFile = new File("/etc/mock-server/sessions/sessionCall_${uuid}.csv")
        }

        if(!sessionFile.exists()){
            sessionFile.parentFile.mkdirs();
            sessionFile.createNewFile()
            sessionFile.append("sep=$DEFAULT_SEPARATOR\n") //set delimiter for csv file
        }

    }

    /**
     * Not used in this class. Calls createSessionFile(uuid)
     *
     * @param uuid The uuid
     * @param notUsed not used variable
     */
    @Override
    def createSessionFile(String uuid, String notUsed){
        createSessionFile(uuid)
    }

    /**
     * add a request and response to a synchronized queue for writing to a csv file
     *
     * @param uuid The overriden request uuid
     * @param request The Request from SSW
     * @param response The response from the services
     */
    private def save(String uuid, String request, String response){
        def arr = ["uuid":uuid, "request":request, "response":response]
        synchronized(writeQueue){
            writeQueue.add( arr )
            writeQueue.notifyAll()
        }

    }

    /**
     * write to the file
     *
     * @param arr
     */
    @Override
    protected void write(Map<String, String> arr){
        String uuid = arr["uuid"]
        File sessionFile = null
        if(System.getProperty("os.name").contains("Windows")){
            sessionFile = new File("C:\\etc\\mock-server\\sessions\\sessionCall_${uuid}.csv")
        }
        else{
            sessionFile = new File("/etc/mock-server/sessions/sessionCall_${uuid}.csv")
        }

        if(!sessionFile.exists()){
            //error message?
            return
        }
        for(s in arr){
            println s
        }
        println "end of request\n"
        sessionFile.append("${arr["request"]}\n${arr["response"]}\n")
    }

    /**
     * Save service calls made during a session to a csv file.
     *
     * @param notUsed notUsed for this method of recording
     * @param request The request made by SSW
     * @param body The body of a POST request (null if GET request)
     * @param response The response from the real service
     */
    @Override
    def void saveSessionServiceCalls(String notUsed, HttpServletRequest request, String body, HttpResponseDecorator response){
        String uuid = request.getHeader("X-UUID") ? request.getHeader("X-UUID") : request.getHeader("X-REQUESTUUID")
        File sessionFile = null
        if(!uuid){
            return
        }
        else{
            if(System.getProperty("os.name").contains("Windows")){
                sessionFile = new File("C:\\etc\\mock-server\\sessions\\sessionCall_${uuid}.csv")
            }
            else{
                sessionFile = new File("/etc/mock-server/sessions/sessionCall_${uuid}.csv")
            }

            if(!sessionFile.exists()){
                //error message?
                return
            }
        }
        String method = request.method
        String url = urlHelper.urlFixer(request.requestURL.toString())
        String path = request.requestURI
        def headers = [:]
        for(name in request.headerNames){
            headers[name] = request.getHeader(name)
        }
        headers.remove("accept-encoding")
        headers.remove("content-length")
        String query = request.queryString
        url += (query) ? "?"+query : ""

        String reqString = "REQ${DEFAULT_SEPARATOR}method:$method${DEFAULT_SEPARATOR}url:$url${DEFAULT_SEPARATOR}path:$path${DEFAULT_SEPARATOR}headers:$headers${DEFAULT_SEPARATOR}query:" + (query ? "?"+query : "") + "${DEFAULT_SEPARATOR}"
        reqString += "body:" + (body ? body : "")
        println("Saving service call from $url to file: sessionCall_${uuid}.csv")
        String rspString = "RSP${DEFAULT_SEPARATOR}json:${response.data.toString()}"
        save(uuid, reqString, rspString)
    }
}
