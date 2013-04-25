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

import com.betfair.utils.mockserver.playback.RecordedObject
import com.betfair.utils.mockserver.playback.RecordedRequest
import com.betfair.utils.mockserver.playback.RecordedResponse
import org.springframework.web.bind.annotation.RequestMethod

/**
 * Created with IntelliJ IDEA.
 * User: wambeekm
 * Date: 03/12/12
 * Time: 14:13
 * To change this template use File | Settings | File Templates.
 */
class StructuredLoader extends Loader{

    public StructuredLoader(){

    }

    @Override
    List<RecordedRequest> load(String uuid) {
        File dir = new File(BASE_DIR + File.separator + uuid)
        if(!dir.exists()){
            throw new FileNotFoundException("Cannot find file:\n\tid= $uuid\n\tlocation= $dir")
        }
        if(!dir.isDirectory()){
            throw new java.io.FileNotFoundException("$dir is expected to be a directory but wasn't!")
        }

        List<RecordedRequest> interactions = new ArrayList<RecordedRequest>()
        //find all expectation files and sort them properly (ie. not like 1, 10, 2, 3, ...)
        def sortedFiles = dir.listFiles().findAll{ it.name ==~ /\d+/}.sort{ it.name.toInteger()}
        sortedFiles.each { file->
            println "Loading File: $file.name"
            interactions.add(parse(file))
        }
        return interactions
    }


    /**
     * Parses the session file and re-creates a RecordedRequest object
     *
     * @param file The file to be parsed
     * @return The RecordedRequest object
     */
    private RecordedRequest parse(File file){
        def reader = file.newReader("UTF-8")

        RecordedRequest request = new RecordedRequest()
        RecordedResponse response = new RecordedResponse()
        try{
            def line = readFileHeaders(reader)
            def url = line.replace("REQUEST: ", "")
            request.setUrl(url)

            readRequest(reader, request)
            readResponse(reader, response)

            request.setResponse(response)
        }
        catch (EOFException e){
            throw new Exception("Bad File: $file.name\n$e.message")
        }
        finally{
            reader.close()
        }

        return request
    }

    /**
     * Reads the headers of the file - Creation date, last used, etc.
     *
     * @param reader The reader looking at the file
     * @return A string containing the header data
     */
    private String readFileHeaders(BufferedReader reader){
        def currentLine = reader.readLine()
        while(currentLine != null){
            if(currentLine.matches(/REQUEST: .*/)){
                return currentLine
            }
            currentLine = reader.readLine()
        }
        throw new EOFException("Reached end of file without finding request")
    }

    /**
     * Reads the request out of the file
     *
     * @param reader The reader reading the file
     * @param request The RecordedRequest object to be set
     */
    private void readRequest(BufferedReader reader, RecordedRequest request){
        String line = reader.readLine()
        StringBuilder rawHttp = new StringBuilder()

        if(line != null){
            rawHttp.append(line + "\n")
            def arr = line.split(" ")
            request.setMethod(arr[0])
            if(arr[1].contains("?")){
                def uri = arr[1].split("\\?", 2)
                request.setPath(uri[0])
                request.setQueryString(uri[1])
            }
            else{
                request.setPath(arr[1])
            }
            request.setProtocol(arr[2])
        }
        line = reader.readLine()

        def headers = [:]
        while(line){
            rawHttp.append(line + "\n")
            def pair = line.split(": ", 2)
            headers[pair[0]] = pair[1]
            line = reader.readLine()
        }
        request.setHeaders(headers)
        rawHttp.append(line + "\n")

        if(request.method.equals(RequestMethod.POST)){
            line = reader.readLine()
            if(line.matches(/RESPONSE:/)){
                request.setRawHttp(rawHttp.toString())
                return
            }
            readBody(reader, line, request, rawHttp)
        }
        else{
            reader.readLine()
        }
        request.setRawHttp(rawHttp.toString())

    }

    /**
     * Reads the response out of file and creates a RecordedResponse object
     *
     * @param reader The reader reading the file
     * @param response The RecordedResponse object
     */
    private void readResponse(BufferedReader reader, RecordedResponse response){
        String line = reader.readLine()
        if(line.matches(/RESPONSE:/)){
            line = reader.readLine()
        }
        StringBuilder rawHttp = new StringBuilder()
        rawHttp.append(line + "\n")

        def statusLine = line.split(" ")
        response.setProtocol(statusLine[0])
        response.setStatusCode(statusLine[1])
        response.setStatusReason(statusLine[2])

        line = reader.readLine()
        def headers = [:]
        while(line){
            rawHttp.append(line + "\n")
            def pair = line.split(": ", 2)
            headers[pair[0]] = pair[1]
            line = reader.readLine()
        }
        response.setHeaders(headers)

        line = reader.readLine()
        readBody(reader, line, response, rawHttp)
        response.setRawHttp(rawHttp.toString())
    }

    /**
     * Reads the request or response body's from file
     *
     * @param reader The reader looking at the file
     * @param line The line the reader just read
     * @param object The RecordedObject to have it's body set
     * @param rawHttp The raw HTTP String to be built up
     */
    private void readBody(BufferedReader reader, String line, RecordedObject object, StringBuilder rawHttp){
        rawHttp.append(line + "\n")
        StringBuilder body = new StringBuilder()
        while(line){
            body.append(line + "\n")
            rawHttp.append(line + "\n")
            line = reader.readLine()
        }
        rawHttp.append("\n")
        object.setBody(body.toString())
    }

    @Override
    String findUUID(String recordingName) {
        if(!recordingName){
            throw new IOException("No file name was supplied")
        }
        def uuid = recordingName.toLowerCase() ==~ /[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}/
        if(uuid){
            return recordingName
        }
        def path = BASE_DIR + File.separator + recordingName + File.separator
        File metaFile = new File(path + "." + recordingName)
        if(!metaFile.exists()){
            throw new FileNotFoundException("Cannot find file:\n\tname= $recordingName\n\tlocation= ${path}")
        }
        uuid = ""
        metaFile.withReader("UTF-8") { reader ->
            String line = reader.readLine()
            while(line && line.contains("=")){
                def arr = line.split("=", 2)
                if(arr[1].toLowerCase() ==~ /[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}/){
                    uuid = arr[1]
                    break
                }
                line = reader.readLine()
            }
        }
        if(uuid){
            return uuid
        }
        else{
            throw new IOException("UUID not found in meta data file $metaFile.absolutePath")
        }
    }
}
