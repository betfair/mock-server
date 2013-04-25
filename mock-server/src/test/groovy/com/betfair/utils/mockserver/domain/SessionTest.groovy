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

import com.betfair.utils.mockserver.message.ExpectationRequest
import com.betfair.utils.mockserver.message.OutcomeRequest
import com.betfair.utils.mockserver.message.RequestMatch
import com.betfair.utils.mockserver.message.ValueMatch
import org.hamcrest.Matcher
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.integration.junit4.JMock
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest

import static java.util.Arrays.asList
import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.CoreMatchers.containsString
import static org.hamcrest.CoreMatchers.endsWith
import static org.hamcrest.MatcherAssert.assertThat
import com.betfair.utils.mockserver.util.ClientRequest
import com.betfair.utils.mockserver.util.Maybe

import org.hamcrest.TypeSafeMatcher
import org.hamcrest.Description
import com.betfair.utils.mockserver.message.ResponseRequest
import org.springframework.http.MediaType

/**
 * User: graya2
 * Date: 27/09/12
 */
@RunWith(JMock.class)
class SessionTest {

    public static final String urlThatMatchesAll = "/bar/foo"
    String urlThatMatchesSessionOnly = "/bat/foo"
    String urlThatMatchesExpectationOnly = "/bar/foot"

    Mockery ctx = new Mockery()

    Matcher<HttpServletRequest> commonMatch = RequestMatch.createUrlMatch(endsWith("foo"))
    Session tested = new Session(1, commonMatch)
    ExpectationRequest whenUrlHasBar_Bash = new ExpectationRequest() {{
        when = new RequestMatch() {{
            url = new ValueMatch() {{ contains = "bar" }}
        }}
        then = new OutcomeRequest() {{
            respond = new ResponseRequest() {{ body="boo"; contentType=MediaType.APPLICATION_XML }}
        }}
    }}

    @Test
    public void shouldStartOutEmpty() throws Exception {
        assertThat(tested.listExpectations(), equalTo("[]"))
    }

    @Test
    public void shouldreportExpectations() throws Exception {
        tested.setUpExpectation(whenUrlHasBar_Bash)
        assertThat(tested.listExpectations(), containsString("bar"))
    }

    @Test
    public void shouldNumberExpectations() throws Exception {
        assertThat(tested.setUpExpectation(whenUrlHasBar_Bash), equalTo(1L))
    }


    @Test
    public void shouldInvokeMatchedExpectations() throws Exception {
        tested.setUpExpectation(whenUrlHasBar_Bash)

        assertThat(tested.respondTo(requestWithUrl(urlThatMatchesAll), null), isKnown)
    }

    @Test
    public void shouldAvoidUnMatchedExpectations() throws Exception {
        tested.setUpExpectation(whenUrlHasBar_Bash)
        assertThat(tested.respondTo(requestWithUrl(urlThatMatchesSessionOnly), null), isUnknown)
    }


    @Test
    public void shouldAvoidUnMatchedSessions() throws Exception {
        tested.setUpExpectation(whenUrlHasBar_Bash)
        assertThat(tested.respondTo(requestWithUrl(urlThatMatchesExpectationOnly), null), isUnknown)
    }

    private ClientRequest requestWithUrl(String url) {
        HttpServletRequest request = ctx.mock(HttpServletRequest.class)
        ctx.checking(new Expectations() {{
            allowing(request).getRequestURI(); will(returnValue(url))
        }})
        new ClientRequest(request, null)
    }

    Matcher<Maybe> isKnown = maybe(true)
    Matcher<Maybe> isUnknown = maybe(false)

    def Matcher<Maybe> maybe(Boolean known) {
        new TypeSafeMatcher<Maybe>(Maybe.class) {
            protected boolean matchesSafely(Maybe item) { return known == item.isKnown() }
            void describeTo(Description description) { description.appendText(known ? "isKnown" : "isUnknown") }
        }
    }
    
}
