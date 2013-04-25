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

import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.integration.junit4.JMock
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletResponse


@RunWith(JMock.class)
class CyclicalOutcomeTest {

    Mockery ctx = new Mockery()

    @Test
    void respondsEachCyclicallyInTurn() {
        Outcome out1 = ctx.mock(Outcome.class, "o1")
        Outcome out2 = ctx.mock(Outcome.class, "o2")
        Outcome out3 = ctx.mock(Outcome.class, "o3")
        HttpServletResponse resp = ctx.mock(HttpServletResponse.class)

        ctx.checking(new Expectations() {{
            oneOf(out2).respondTo(resp)
            oneOf(out3).respondTo(resp)
            oneOf(out2).respondTo(resp)
            oneOf(out3).respondTo(resp)
            exactly(3).of(out1).respondTo(resp)
        }})

        CyclicalOutcome tested = new CyclicalOutcome(out1, out2, out3)

        tested.respondTo(resp) // 1 x1
        tested.respondTo(resp) // 2 x1
        tested.respondTo(resp) // 3 x1
        tested.respondTo(resp) // 1 x2
        tested.respondTo(resp) // 2 x2
        tested.respondTo(resp) // 3 x2
        tested.respondTo(resp) // 1 x3
    }
}
