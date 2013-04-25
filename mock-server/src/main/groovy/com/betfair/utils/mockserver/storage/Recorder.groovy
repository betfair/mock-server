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

import javax.servlet.http.HttpServletRequest
import groovyx.net.http.HttpResponseDecorator

/**
 * Created with IntelliJ IDEA.
 * User: wambeekm
 * Date: 30/11/12
 * Time: 12:19
 * To change this template use File | Settings | File Templates.
 */
abstract class Recorder implements Runnable{
    protected final List<Map<String, String>> writeQueue

    public Recorder(){
        writeQueue = Collections.synchronizedList(new LinkedList<Map<String, String>>())
    }

    /**
     * Create the session file or file structure to save requests and responses to
     *
     * @param uuid The UUID or name of the file to be created
     */
    abstract def createSessionFile(String uuid)

    /**
     * Create the session file or file structure to save requests and responses to, with a specified name
     *
     * @param uuid The UUID used during the session
     * @param name The name of the file to be created
     */
    abstract def createSessionFile(String uuid, String name)

    /**
     * Writes the string representations of the requests and responses to file
     *
     * @param arr a map containing uuid, request and response in string format
     */
    abstract protected void write(Map<String, String> arr)

    /**
     * Queues up requests and responses to be written to file in a thread safe way
     *
     * @param filename The name of the file to save to
     * @param request The HTTP Request from the SSW instance
     * @param body The HTTP request body if there is one
     * @param response The HTTP Response from the services
     */
    abstract def void saveSessionServiceCalls(String filename, HttpServletRequest request, String body, HttpResponseDecorator response)

    @Override
    public final void run() {
        while(true){
            synchronized(writeQueue){
                if(!writeQueue.empty){
                    write(writeQueue.pop())
                }
                else{
                    try{
                        writeQueue.wait()
                    }catch (InterruptedException e){}
                }
            }
            sleep 10 //sleep so as not to be a greedy thread
        }
    }

    /**
     * Starts off the thread that looks for items waiting to be written to
     */
    public final void start(){
        Thread t = new Thread(this)
        t.start()
    }

    public boolean isUnwantedHeader(String name){
        switch (name.toLowerCase()){
            case "content-encoding":
                return true
            case "ntCoent-Length":
                return true
            case "content-length":
                return true
//            case "connection":
//                return true
//            case "cneonction":
//                return true
            default:
                return false
        }
    }
}
