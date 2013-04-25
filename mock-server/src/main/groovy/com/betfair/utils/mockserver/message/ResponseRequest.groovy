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



package com.betfair.utils.mockserver.message

import org.apache.commons.io.IOUtils
import org.springframework.http.MediaType
import com.betfair.utils.mockserver.domain.Response
import com.betfair.utils.mockserver.domain.Delay
import org.codehaus.jackson.annotate.JsonRawValue


class ResponseRequest {
    String code, contentType, bodyFile
    @JsonRawValue String body
    List<ResponseHeader> headers
    DelayMatch delay // I'd like to use a Delay object directly but never works and not worth wasting time on now


    Response toResponse() {
        def expectation = new Response()

        expectation.returnCode = readCode()
        expectation.returnContentType = readContentType()
        expectation.responseBody = readReply()
        expectation.delay = readDelay()
        headers.each{header -> header.values.each{value -> expectation.headers.add(header.name, value)}}

        return expectation
    }

    private int readCode() {
        if (code == null) (body == null && bodyFile == null) ? 404 : 200
        else Integer.parseInt(code)
    }

    def readContentType() {
        if (contentType == null) { null }
        else if (contentType.contains("xml")) {
            MediaType.parseMediaType("application/xml; charset=utf-8")
        }
        else if (contentType.contains("json")) {
            MediaType.parseMediaType("application/json; charset=utf-8")
        }
        else {
            MediaType.parseMediaType(contentType)
        }
    }

    private def readReply() {
        if (body != null) {
            body
        } else if (bodyFile != null) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(bodyFile)
            if (stream != null)
                IOUtils.toString(stream)
            else {
                throw new FileNotFoundException("Cannot load the file for response body : " + body.getResponseBodyFile())
            }
        }
    }

    private def readDelay() {
        // DelayMatch -> Delay
        if(this.delay == null) {
            return new Delay(0L, 0L)
        }

        return new Delay(this.delay.timeMillis, this.delay.jitter)
    }
}
