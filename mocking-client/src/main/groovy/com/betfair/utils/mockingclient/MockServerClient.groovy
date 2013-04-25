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

package com.betfair.utils.mockingclient

import com.betfair.utils.mockingclient.domain.Expectation
import com.betfair.utils.mockingclient.domain.MockSession
import org.apache.http.HttpHeaders
import org.apache.http.HttpHost
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.protocol.HTTP
import org.apache.http.util.EntityUtils
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.map.annotate.JsonSerialize

import java.util.logging.Logger

/**
 * Provides a friendly interface to the MockServer to set up sessions and expectations.
 */
class MockServerClient {
    private static final def Logger LOG = Logger.getLogger(MockServerClient.class.canonicalName);
    private static final def SESSION_URL = "/sessions"
    private static final def EXPECTATIONS_URL = "/expectations?sessionId="
    def private mapper = new ObjectMapper();
    def private httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager())
    def private HttpHost httpHost

    /**
     * Construct a MockServerClient pointing at the specified host and port.
     *
     * @param host the host that the mock server is running on
     * @param port the port that the mock server is running on
     */
    MockServerClient(host, port) {
        httpHost = new HttpHost(host, port as int)
        mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL)
    }

    /**
     * Create a mocking session described by sessionMatches. This should be a {@see SessionTO} built using the
     * {@see RequestBuilder#session} builder.
     *
     * @param sessionMatches describes the session
     * @return a handle to the mocking session
     *
     * @throws IllegalStateException if the server rejects the request
     */
    def MockSession createSession(sessionMatches) {
        def httpMessage = new HttpPost(SESSION_URL)
        String result = sendMessage(httpMessage, sessionMatches)
        def session = new MockSession()
        session.sessionId = (result =~ /\d+/)[0] as long

        LOG.info("Session created with id ${session.sessionId}")
        session
    }

    private String sendMessage(HttpPost httpMessage, messageBody) {
        httpMessage.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        httpMessage.entity = new StringEntity(serialiseMessage(messageBody), HTTP.UTF_8)

        def response = httpClient.execute(httpHost, httpMessage)

        if (response.statusLine.statusCode != HttpStatus.SC_CREATED) {
            EntityUtils.consume(response.entity)
            throw new IllegalStateException("Error response from mock server: " + response.statusLine.statusCode)
        }

        def result = EntityUtils.toString(response.entity)
        result
    }

    /**
     * Delete a previously created session.
     *
     * @param session the session handle
     */
    def deleteSession(session) {
        def httpMessage = new HttpDelete(SESSION_URL + "?sessionId=${session.sessionId}")

        def response = httpClient.execute(httpHost, httpMessage)

        if (response.statusLine.statusCode != HttpStatus.SC_OK) {
            throw new IllegalStateException("Error response from mock server: " + response.statusLine.statusCode)
        }

        EntityUtils.consume(response.entity)
        LOG.info("Session deleted with id ${session.sessionId}")
    }

    /**
     * Adds an expectation to a session. This should be an {@see ExpectationTO} build using
     * {@see RequestBuilder#expectation}
     *
     * @param session the session handle, returned from {@see #createSession}.
     * @param expectationTO
     * @return a handle to the expectation
     * @throws IllegalStateException if the server rejects the request.
     */
    def addExpectation(session, expectationTO) {
        def httpMessage = new HttpPost(EXPECTATIONS_URL + session.sessionId)
        String result = sendMessage(httpMessage, expectationTO)
        def expectation = new Expectation()
        expectation.expectationId = (result =~ /\d+/)[0] as int

        LOG.info("Expectation created with id ${expectation.expectationId}")
        expectation
    }


    def private serialiseMessage(message) {
        mapper.writeValueAsString(message);
    }
}
