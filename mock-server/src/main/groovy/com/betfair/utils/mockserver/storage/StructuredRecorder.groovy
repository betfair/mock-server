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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created with IntelliJ IDEA.
 * User: wambeekm
 * Date: 30/11/12
 * Time: 12:03
 * To change this template use File | Settings | File Templates.
 */
class StructuredRecorder extends Recorder {
    @Value('${recordingLocation}') private final FILE_LOCATION
    private static final SEP = File.separator
    @Autowired URLHelper urlHelper
    @Value('${overwrite}') private final boolean OVERWRITE

    private static final Logger LOG = LoggerFactory.getLogger(StructuredRecorder.class);

    public StructuredRecorder(){
        super()
    }

    @Override
    def createSessionFile(String uuid){
        createSessionFile(uuid, uuid)
    }

    @Override
    def createSessionFile(String uuid, String folderName){
        if(!uuid){
            throw new Exception("No UUID given")
        }
        File sessionMetaFile = new File(FILE_LOCATION + "${SEP}${folderName}${SEP}.${folderName}" )
        if(sessionMetaFile.parentFile.exists()){
            if(OVERWRITE){
                LOG.info("Directory with name $folderName already exists. Clearing out folder.")
                def parentDir = sessionMetaFile.parentFile
                parentDir.eachFile {
                    if(!it.delete()){
                        LOG.error("Unable to delete file: ${it.absolutePath}")
                    }
                }
            }
            else{
                throw new Exception("The name '$folderName' is already in use and OVERWRITE is disabled!")
            }
        }
        if(!sessionMetaFile.parentFile.exists() && !sessionMetaFile.parentFile.mkdirs()){
            throw new Exception("Unable to make parent directories for UUID: $uuid")
        }

        if(!sessionMetaFile.createNewFile()){
            throw new Exception("Unable to create metadata file for UUID: $uuid")
        }

        def timestamp = new Date().toTimestamp().toString()

        def bw = sessionMetaFile.newWriter("UTF-8")
        try{
            bw.writeLine("uuid=$uuid")
            bw.writeLine("creationDate=$timestamp")
            bw.writeLine("lastEdited=$timestamp")
            bw.writeLine("lastUsed=")
            bw.writeLine("recorded=AUTO")
            bw.writeLine("description=")
            bw.writeLine("Created by the Mock Server on $timestamp for uuid $uuid")
            bw.flush()
        }catch(IOException e){
            LOG.error "Unable to create metadata for UUID: $uuid"
            throw e
        }
        finally {
            bw.close()
        }
    }

    @Override
    protected void write(Map<String, String> arr){
        String dirName = arr["fileName"]

        File sessionFolder = new File(FILE_LOCATION + "${SEP}${dirName}" )

        if(!sessionFolder.exists()){
            throw new Exception("The uuid could not be found: $dirName")
        }

        def fileCount = 0
        sessionFolder.eachFile { file->
            if(!file.directory){
                fileCount++
            }
        }

        String name = sessionFolder.absolutePath + "${SEP}${fileCount}"
        File reqFile = new File(name)
        reqFile.createNewFile()

        String timestamp = new Date().toTimestamp().toString()
        def bw = reqFile.newWriter("UTF-8")
        try{
            bw.writeLine("creationDate=$timestamp")
            bw.writeLine("lastEdited=$timestamp")
            bw.writeLine("lastUsed=")
            bw.writeLine("recorded=AUTO")
            bw.writeLine("")
            bw.writeLine("REQUEST: ${arr["requestUrl"]}")
            bw.writeLine(arr["request"])
            bw.writeLine("RESPONSE:")
            bw.writeLine(arr["response"])
            bw.flush()
        }catch(Exception e){
            println "unable to create file for : $dirName"
            throw e
        }
        finally{
            bw.close()
        }
    }

    @Override
    def void saveSessionServiceCalls(String dirName, HttpServletRequest request, String body, HttpResponseDecorator response){
        String uuid = request.getHeader("X-UUID") ? request.getHeader("X-UUID") : request.getHeader("X-REQUESTUUID")
        if(!uuid){
            throw new Exception("Cannot determine UUID!")
        }
        File sessionDirectory = new File(FILE_LOCATION + "${SEP}${dirName}")
        if(!sessionDirectory.exists()){
            //error message?
            throw new FileNotFoundException("Session Directory Does not exist!")
        }

        def saveStrings = ["fileName":dirName]
        def reqUrl =request.requestURL.toString()
        reqUrl = urlHelper.urlFixer(reqUrl)
//        reqUrl+= request.queryString ? request.queryString : ""
        saveStrings["requestUrl"] = reqUrl
        String query = (request.queryString)? "?$request.queryString": ""
        StringBuilder reqString = new StringBuilder()
        reqString.append("${request.method} ")
        reqString.append("${request.requestURI}$query ")
        reqString.append("${request.protocol}\n")

        for(name in request.headerNames){
            if(isUnwantedHeader(name)){
                LOG.debug("Stripping out header: $name")
            }
            else{
                reqString.append("$name: ${request.getHeader(name)}\n")
            }
        }
        reqString.append("\n")
        if(body){
            reqString.append("$body\n")
        }
        saveStrings["request"] = reqString.toString().replace("\r", "")

        def rspString = new StringBuilder()
        rspString.append(response.statusLine)
        rspString.append("\n")
        for(header in response.allHeaders){
            if(!isUnwantedHeader(header.name)){
                def name = header.name
                def value = header.value
                rspString.append("$name: $value\n")
            }
        }
        rspString.append("\n")
        def rspBody = response.data
        if(rspBody){
            rspString.append(rspBody.toString())
        }
        rspString.append("\n")

        saveStrings["response"] = rspString.toString().replace("\r", "")
        save(saveStrings)
    }

    /**
     * Add a request and response to a synchronized queue for writing to file
     *
     * @param saveStrings The formatted Strings to be written to file
     */
    private def save(Map<String, String> saveStrings){
        while(true){
            synchronized(writeQueue){
                writeQueue.add( saveStrings )
                writeQueue.notifyAll()
                return
            }
            sleep 10
        }

    }

}
