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

import org.jmock.Expectations
import org.jmock.Mockery
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

import static java.util.Collections.enumeration
import static org.hamcrest.CoreMatchers.not
import static org.hamcrest.MatcherAssert.assertThat

@Ignore
@RunWith(JUnit4.class)
class RequestMatchTest {

    RequestMatch tested = new RequestMatch()

    private final Mockery ctx = new Mockery()
    private final HttpServletRequest passes = ctx.mock(HttpServletRequest.class, "passing")
    private final HttpServletRequest fails = ctx.mock(HttpServletRequest.class, "failing")


    @Test
    public void shouldMatchMethod() {
        tested.method = "post"

        ctx.checking(new Expectations() {{
            allowing(passes).getMethod()
            will(returnValue("POST"))

            allowing(fails).getMethod()
            will(returnValue("get"))
        }})

        runAssertion()
    }

    @Test
    public void shouldMatchUri() {
        tested.url = new ValueMatch() {{ eq = "poo" }}

        ctx.checking(new Expectations() {{
            allowing(passes).getRequestURI()
            will(returnValue("poo"))

            allowing(fails).getRequestURI()
            will(returnValue("shmoo"))
        }})

        runAssertion()
    }

    @Test
    public void shouldMatchHeaders() {
        tested.header = new NamedMatch() {{
            name = new ValueMatch() {{ eq = "poo" }}
            value = new ValueMatch() {{ eq = "poo" }}
        }}

        ctx.checking(new Expectations() {{
            allowing(passes).getHeaderNames()
            will(returnValue(enumeration(["ping", "poo", "pow"])))
            allowing(passes).getHeader("poo")
            will(returnValue("poo"))

            allowing(fails).getHeaderNames()
            will(returnValue(enumeration(["ping", "pow"])))
        }})

        runAssertion()
    }

    @Test
    public void shouldMatchParams() {
        tested.param = new NamedMatch() {{
            name = new ValueMatch() {{ eq = "poo" }}
            value = new ValueMatch() {{ eq = "poo" }}
        }}

        ctx.checking(new Expectations() {{
            allowing(passes).getParameterNames()
            will(returnValue(enumeration(["ping", "poo", "pow"])))
            allowing(passes).getParameter("poo")
            will(returnValue("poo"))

            allowing(fails).getParameterNames()
            will(returnValue(enumeration(["ping", "pow"])))
        }})

        runAssertion()
    }

    @Test
    public void shouldMatchCookie() {
        tested.cookie = new NamedMatch() {{
            name = new ValueMatch() {{ eq = "poo" }}
            value = new ValueMatch() {{ eq = "poo" }}
        }}

        ctx.checking(new Expectations() {{
            def wrongCookie = new Cookie("bing", "ting")
            def rightCookie = new Cookie("poo", "poo")

            allowing(passes).getCookies()
            will(returnValue([wrongCookie, rightCookie] as Cookie[]))
            allowing(fails).getCookies()
            will(returnValue([wrongCookie] as Cookie[]))

        }})

        runAssertion()
    }

//    @Test
//    public void shouldMatchBody() {
//        tested.body = new ValueMatch() {{ eq = "poo" }}
//
//        ctx.checking(new Expectations() {{
//            allowing(passes).getInputStream()
// will(returnValue(new ServletInputStream() {
//                int read() {
//                    0
//                }
//            }))
//            allowing(fails).getCookies()
// will(returnValue(""))
//
//        }})
//
//        runAssertion()
//    }


    @Test
    public void shouldMatchAll() {
        tested.all = [
                new RequestMatch() {{ url = new ValueMatch() {{ beginsWith = "/foo" }} }},
                new RequestMatch() {{ url = new ValueMatch() {{ endsWith = "bar" }} }},
            ]

        ctx.checking(new Expectations() {{
            allowing(passes).getRequestURI()
            will(returnValue("/foo/bang/bar"))
            allowing(fails).getRequestURI()
            will(returnValue("/foo/bang/bat"))
        }})

        runAssertion()
    }



    private void runAssertion() {
        def rule = tested.asMatcher()
        assertThat(passes, rule)
        assertThat(fails, not(rule))
    }

}
