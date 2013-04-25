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



package com.betfair.utils.mockserver.util

import org.springframework.beans.factory.annotation.Value

/**
 * Created with IntelliJ IDEA.
 * User: wambeekm
 * Date: 03/12/12
 * Time: 10:58
 * To change this template use File | Settings | File Templates.
 */
class URLHelper {
    @Value('${mockServicesDomain}') private String mockServicesDomain
    Properties mappingsFile
    def mappings

    public URLHelper(){

    }

    /**
     * Replaces any occurrences of ${mockServicesDomain} placeholders in the service-mappings.properties file.
     * Only runs if mappings is not null or empty.
     */
    def setup(){
        if(mappings){ //mappings is already set
            return
        }
        def keys = mappingsFile.keySet().toArray()
        println "MOCK_SERVICES_DOMAIN: $mockServicesDomain"
        for (int i=0; i< keys.size(); i++){
            def thing = mappingsFile.get(keys[i])
            thing = thing.toString()
            thing = thing.replace('${mockServicesDomain}',mockServicesDomain)
            //println "REPLACED: ${thing}"
            mappingsFile.put(keys[i], thing)
        }
        //println "KEYSET: $keys"
        //println mappingsFile
        mappings = new ConfigSlurper().parse(mappingsFile)
    }

    /**
     * Converts the betfair.gb request back to its original url
     * @deprecated URLs should be used with config file instead, allowing services to be changed without re-compiling.
     * @param oldUrl The old url to be fixed
     * @return The new url if it matched the cases. null if not found.
     */
    @Deprecated
    public String urlFixerOld(String oldUrl){
        switch(oldUrl){
            case "":
                   return null
             //Insert pattern matching here
            default:
                throw new UnknownHostException("Unable to find host for $oldUrl")
        }
    }

    /**
     * Converts the betfair.gb request back to its original url
     * @param oldUrl The old url to be fixed
     * @return The new url if it matched the cases. null if not found.
     */
    public String urlFixer(String oldUrl){
        setup()
        def mappingsProps = new Properties()
//        println "MAPPINGS: " + mappings

        //finds the url by matching the old with the
        def newURL = mappings.pattern.find{ name, pattern ->
//            println "\tNAME: " + name + " REGEX: "+ pattern
//            println "\tREFLECTION: " + pattern.class.name
            oldUrl =~ pattern
        }

        if(newURL){
            newURL = mappings.url."${newURL.key}"
//            println "Old Url: $oldUrl"
//            println "matched to $newURL"
        }
        else{
            throw new UnknownHostException("Unable to match host for $oldUrl")
        }

        return newURL
    }
}
