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

import org.hamcrest.Matcher
import org.hamcrest.StringDescription
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.CopyOnWriteArrayList
import org.springframework.http.ResponseEntity
import javax.servlet.http.HttpServletResponse
import com.betfair.utils.mockserver.message.ExpectationRequest
import com.betfair.utils.mockserver.util.ClientRequest;
import com.betfair.utils.mockserver.util.Maybe

/**
 * User: graya2
 * Date: 24/09/12
 */
class Session {
    final Long id
    final Matcher<ClientRequest> commonWhen

    private final AtomicLong expectationIds = new AtomicLong(0L)
    // CopyOnWriteArrayList instead of synchronising on a LinkedList and assumes mutations are rare
    // Persistent Immutable Vector would be another option
    private List<Expectation> expectations = new CopyOnWriteArrayList<Expectation>()

    Session(Long id, Matcher<ClientRequest> commonWhen) {
        this.id = id
        this.commonWhen = commonWhen
    }

    // TODO break dependency on message format
    def setUpExpectation(ExpectationRequest message) {
        def expectation = message.toExpectation(expectationIds.incrementAndGet())
        addExpectation(expectation)
        expectation.id
    }

    private void addExpectation(Expectation expectation) {
        expectations.add(expectation);
    }

    @Override
    String toString() {
        def description = new StringDescription()
        commonWhen.describeTo(description)
        return "session " + id + " - " + description
    }

    String listExpectations() {
        expectations.toString()
    }

    public Maybe<ResponseEntity<String>> respondTo(ClientRequest request, HttpServletResponse response) {
        if (commonWhen.matches(request)) {
            // Reverse iteration so the most recent added expectations match first
            // We could use a linked list but it's less performant under high concurrency
            // due to synchronisation which read only operations don't require with the CopyOnWriteArrayList
            for(ListIterator listIter = expectations.listIterator(expectations.size()); listIter.hasPrevious(); ) {
                Expectation expectation = listIter.previous()
                if (expectation.match(request)) {
                    return Maybe.definitely(expectation.respondTo(response))
                }
            }
        }
        Maybe.unknown();
    }

}
