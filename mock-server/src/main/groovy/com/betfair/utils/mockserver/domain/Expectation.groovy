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




package com.betfair.utils.mockserver.domain;


import com.betfair.utils.mockserver.util.ClientRequest
import org.hamcrest.Matcher
import org.hamcrest.StringDescription
import org.springframework.http.ResponseEntity

import java.util.concurrent.atomic.AtomicInteger
import javax.servlet.http.HttpServletResponse

public class Expectation {

    final Long id
    private final AtomicInteger invocationCount = new AtomicInteger(0)
    private final Matcher<ClientRequest> constraints
    private final Outcome outcome


    public Expectation(Long id,
                       Matcher<ClientRequest> constraints,
                       Outcome outcome) {
        this.id = id
        this.constraints = constraints
        this.outcome = outcome
    }

    public boolean match(ClientRequest request) {
        return constraints.matches(request)
    }

    public ResponseEntity<String> respondTo(HttpServletResponse response) throws IOException {
        invocationCount.incrementAndGet()
        outcome.respondTo(response)
    }

    public String toString() {
        def description = new StringDescription()
        constraints.describeTo(description)
        return description.toString() + " invoked " + invocationCount.get() + " times."
    }


}