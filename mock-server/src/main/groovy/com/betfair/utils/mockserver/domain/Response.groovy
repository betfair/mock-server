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




package com.betfair.utils.mockserver.domain

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import javax.servlet.http.HttpServletResponse

public class Response implements Outcome {
    int returnCode = 404
    String responseBody = "";
    MediaType returnContentType = null;
    HttpHeaders headers = new HttpHeaders()
    Delay delay = null

    public ResponseEntity respondTo(HttpServletResponse response) throws IOException {
        if(delay != null)
        {
            def delayTimeMillis = delay.nextDelay()
            Thread.sleep(delayTimeMillis)
            // Sleeping under windows maybe 10-15ms variation (inaccuracy) due to the OS scheduling clock ticks
            // With Linux each clock tick is ~1ms so better accuracy
            // every x clock-ticks the processes quantum may end, the quantum won't end early unless the process
            // priority changes or it waits on something
            // Linux 10-200 clock ticks for a quantum usually so 10-200ms
            // Windows each clock tick is ~10-15ms
        }
        if (returnContentType != null)
            headers.setContentType(returnContentType)
        return new ResponseEntity<String>(responseBody, headers, HttpStatus.valueOf(returnCode));
    }

}