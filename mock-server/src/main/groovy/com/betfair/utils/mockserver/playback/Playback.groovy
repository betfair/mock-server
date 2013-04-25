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

import com.betfair.utils.mockserver.storage.LoadedExpectations
import com.betfair.utils.mockserver.storage.Loader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

/**
 * Created with IntelliJ IDEA.
 * User: wambeekm
 * Date: 20/11/12
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */
class Playback {
    @Value('${csvSeparator}') private String DEFAULT_SEPARATOR
    @Value('${fileFormat}') String fileFormat
    @Autowired Loader loader

    public Playback(){

    }

    /**
     * Reads the uuid and the expectations from disk and converts them into mock server dsl expectations.
     *
     * @param name The name of the Recording
     * @return the LoadedExpectations object containing mockserver dsl
     */
    public LoadedExpectations run(String name){
        def interactions = loader.load(name)
        def expectations = new LoadedExpectations(findUUID(name), toExpectations(interactions))
        return expectations
    }

    /**
     * Converts all Recorded objects into a list of string expectations for the http POST body
     *
     * @param interactions The list of RecordedObject interactions
     * @return The list of expectations
     */
    private List<String> toExpectations(List<RecordedRequest> interactions){
        List<String> expectations = new ArrayList<String>()
        for(interaction in interactions){
            expectations.add(
                    constructExpectation(interaction))
        }

        return expectations
    }

    /**
     * constructs a string Expectation from a RecordedObject
     *
     * @param request The RecordedRequest object to turn into a string expectation
     * @return The String Expectation
     */
    private String constructExpectation(RecordedRequest request){
        def path = request.path
        //if starting character begins with '/' remove it
        if(path.charAt(0) == "/"){
            path = path[1..(path.size()-1)]
        }
        def query = request.queryMap
        def body = request.safeBody
        def responseHeaders = request.response.headers
        String expectation = """{
    "when":{
        "all":[
            {"url":{
                "contains":"$path"
            }}^query^^body^
        ]
    },
    "then":{
        "respond":{
            "code":"${request.response.statusCode}",
            "contentType":"application/json",
            ^responseHeaders^
            "body":"${request.response.safeBody}",
            "delay":{
                "timeMillis":500,
                "jitter":50
            }
        }
    }
}"""
        String end =  expectation.replace("^query^", buildExpectationParams(query))
        println "EXPECTATION WITH QUEREYS: " + end
        return end
                .replace("^body^", buildExpectationBody(body))
                .replace("^responseHeaders^", buildResponseHeaders(responseHeaders))
    }

    /**
     * Builds the expectation's url paramaters from the RecordedRequest object if there are any
     *
     * @param queryMap
     * @return The query parameters ready to be inserted into the expectation string
     */
    def String buildExpectationParams(Map<String, String> queryMap){
        String queryParams = ""
        if (queryMap){
            queryParams = """,
            {"all":["""
            def params = """
                {"param":{
                    "name":{
                        "eq":"^key^"
                    },
                    "value":{
                        "eq":"^val^"
                    }
                }}"""
            Iterator i = queryMap.keySet().iterator()

            while(i.hasNext()){
                String key = i.next()
                String val = queryMap[key]
                queryParams += params.replace("^key^", key).replace("^val^", val)
                queryParams += (i.hasNext() ? ",\n" : "\n")
            }

            queryParams += """
            ]}"""
        }
        return queryParams
    }

    /**
     * get the uuid used during the recording
     *
     * @param name The name of the recording
     * @return the uuid
     */
    def String findUUID(String name){
        return loader.findUUID(name)
    }

    /**
     * Build the expectation's match body, if a body exists
     *
     * @param jsonSafeBody The json safe body String (escaped ")
     * @return The body string to be inserted into the expectation. Empty string if no body present
     */
    def String buildExpectationBody(String jsonSafeBody){
        String expectationBody = ""
        if(jsonSafeBody){
            expectationBody = """,
            {"body": {
                "contains":"$jsonSafeBody"
            }}
            """
        }
        return expectationBody
    }

    /**
     * Builds the response headers for the expectation, if there are any
     *
     * @param headers A map containing the headers
     * @return A string containing the response headers section of the expectation
     */
    def String buildResponseHeaders(Map<String, String> headers){
        def returnString = ""
        if(headers){
            returnString = """
            "headers":[
                """

            headers.remove("content-type")
            headers.remove("Content-Type")
//            headers.remove("connection")
//            headers.remove("Connection")
//            headers.remove("cneonction")
//            headers.remove("Cneonction")

            headers.eachWithIndex { name, value, index ->

                returnString += """{"name" : "$name", "values" : ["$value"]}"""
                if(index < (headers.size() - 1)) {
//                    println "$index < ${headers.size() -1}"
                    returnString += ",\n\t"
                }

            }
            returnString += """
            ],"""
        }

        return returnString
    }
}
