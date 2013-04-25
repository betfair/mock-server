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



package com.betfair.utils.mockserver

import com.betfair.utils.mockserver.message.ExpectationRequest

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.ConcurrentHashMap
import org.springframework.http.ResponseEntity
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import com.betfair.utils.mockserver.domain.Session
import com.betfair.utils.mockserver.message.SessionRequest
import com.betfair.utils.mockserver.util.ClientRequest;
import com.betfair.utils.mockserver.util.Maybe


class Sessions {

    private final AtomicLong sessionIds = new AtomicLong(0L)
    private Map<Long, Session> sessions = new ConcurrentHashMap<Long, Session>()

    def setUpSession(SessionRequest message) {
        Long sessionId = sessionIds.incrementAndGet()
        sessions.put(sessionId, message.toSession(sessionId))
        sessionId
    }

    def setUpExpectation(Long sessionId, ExpectationRequest message) {
        sessions.get(sessionId)?.setUpExpectation(message)
    }

    String listSessions(String sessionUrlPrefix) {
        sessions.values().collect {sessionUrlPrefix + it.id}.toString()
    }
    Maybe<String> readSession(long sessionId) {
        Maybe.safeGet(sessions, sessionId).collect {it.toString()}
    }

    Maybe<String> listExpectations(long sessionId) {
        Maybe.safeGet(sessions, sessionId).collect {it.listExpectations()}
    }

    Maybe<String> removeSession(long sessionId) {
        Maybe.safeRemove(sessions, sessionId).collect {it.toString() + " " + it.listExpectations()}
    }

    Maybe<ResponseEntity<String>> respondTo(ClientRequest request, HttpServletResponse response) {
        for (Session session : new ArrayList(sessions.values())) {
            Maybe<ResponseEntity<String>> result = session.respondTo(request, response)
            if (result.isKnown()) return result
        }
        Maybe.unknown();
    }

}
