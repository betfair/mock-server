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

import org.hamcrest.FeatureMatcher
import org.hamcrest.Matcher

import javax.servlet.http.HttpServletRequest

import com.betfair.utils.mockserver.util.ClientRequest;
import com.betfair.utils.mockserver.util.MockMatchers

import org.hamcrest.CoreMatchers
import com.betfair.utils.mockserver.domain.ConstraintScope

class RequestMatch
extends AggregateMatch<ClientRequest, RequestMatch>
implements MatcherBuilder<ClientRequest> {
    String method
    ValueMatch url
    NamedMatch header, param, cookie
//    BodyMatch body
    ValueMatch body


    public static Matcher<ClientRequest> createMethodMatch (String method) {
        return new FeatureMatcher<ClientRequest, String>(CoreMatchers.equalTo(method.toUpperCase()), "url", "url") {
            @Override
            protected String featureValueOf(ClientRequest actual) {
                return actual.header.getMethod().toUpperCase();
            }
        };
    }
    public static Matcher<ClientRequest> createUrlMatch (Matcher<String> urlMatch) {
        return new FeatureMatcher<ClientRequest, String>(urlMatch, "url", "url") {
            @Override
            protected String featureValueOf(ClientRequest actual) {
                return actual.header.getRequestURI();
            }
        };
    }
    public static Matcher<ClientRequest> createBodyMatch (Matcher<String> bodyMatch) {
        return new FeatureMatcher<ClientRequest, String>(bodyMatch, "body", "body") {
            @Override
            protected String featureValueOf(ClientRequest actual) {
                return actual.body
            }
        };
    }

    public Matcher<ClientRequest> asMatcher() {
        LinkedList<Matcher<ClientRequest>> all = new LinkedList<Matcher<ClientRequest>>();

        if (method!= null) {
            all.add(createMethodMatch(method))
        }
        if (url != null) {
            all.add(createUrlMatch(url.asMatcher()))
        }
        if (header != null) {
            all.add(header.asMatcher(ConstraintScope.HEADER))
        }
        if (param != null) {
            all.add(param.asMatcher(ConstraintScope.PARAMETER))
        }
        if (cookie != null) {
            all.add(cookie.asMatcher(ConstraintScope.COOKIE))
        }
        if (body!= null) {
            all.add(createBodyMatch(body.asMatcher()))
        }

        all.addAll(combined())

        return MockMatchers.condenseGroup(all)
    }
}
