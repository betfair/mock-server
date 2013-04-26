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



package com.betfair.utils.mockserver.playback

import org.springframework.web.bind.annotation.RequestMethod

/**
 * Created with IntelliJ IDEA.
 * User: wambeekm
 * Date: 20/11/12
 * Time: 14:34
 * To change this template use File | Settings | File Templates.
 */
class RecordedRequest extends RecordedObject{
    RequestMethod method
    String url
    String path
    String queryString
    Map<String, String> queryMap
    RecordedResponse response

    @Override
    public void load(String[] data){
        if(data[0].toUpperCase() == "REQ"){
            String method = data[1].replaceFirst("method:", "")
            String url = data[2].replaceFirst("url:", "").split("\\?")[0]
            String path = data[3].replace("path:", "")
            String headers = data[4].replaceFirst("headers:", "")
            String queryString = URLDecoder.decode(data[5].replaceFirst("query:", ""))
            String body = data[6].replaceFirst("body:", "")

            this.@method = RequestMethod.valueOf(method)
            this.@url = url
            this.@path = path
            this.@headers = parseHeaders(headers)
            this.@queryString = queryString
            this.@queryMap = queryStringToMap(queryString)
            this.@body = body
        }
    }

/**
     * parses the header string to a key=value map
     * @param headerString
     * @return Map of headers
     */
    private Map parseHeaders(String headerString){
        def headers = [:]
        String[] arr = headerString.replace("[", "").replace("]", "").split(",")

        for(item in arr){
            def pair = item.split(":", 2)
            headers[pair[0]] = pair[1]
        }

        return headers
    }

    /**
     * set the RecordedResponse object
     * @param data the array of response data
     */
    public void setResponse(String[] data){
        response = new RecordedResponse()
        response.load(data)
    }

    /**
     * set the response object
     * @param response The RecordedResponse object
     */
    public void setResponse(RecordedResponse response){
        this.response = response
    }

    /**
     * turn the query string into a key=value map
     * @param queryString
     * @return The Query Map
     */
    private Map queryStringToMap(String queryString){
        def map = [:]
        if (queryString){
            queryString = queryString.replace("?", "")
            def splitString = queryString.split("&")
            for(pair in splitString){
                def keyVal = pair.split("=")
                map[keyVal[0]] = keyVal[1]
            }
        }
        return map
    }

    /**
     * Sets the http method (GET, POST, etc...)
     * @param method A string method type
     */
    public void setMethod(String method){
        this.method = RequestMethod.valueOf(method)
    }

    /**
     * Override the setQueryString method to also set the query map
     *
     * @param queryString The query string to set
     */
    public void setQueryString(String queryString){
        this.@queryString = URLDecoder.decode(queryString)
        this.@queryMap = queryStringToMap(this.@queryString)
    }




}
