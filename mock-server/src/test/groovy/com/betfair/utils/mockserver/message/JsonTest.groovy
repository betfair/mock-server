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

import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.CoreMatchers.equalTo
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpHeaders

import static org.hamcrest.CoreMatchers.notNullValue


/**
 * User: graya2
 * Date: 06/09/12
 */
class JsonTest {

    MappingJacksonHttpMessageConverter converter = new MappingJacksonHttpMessageConverter()

    @Test
    void shouldParseJustAUriEq() {
        ExpectationRequest parse = parse("""{"when" : {"url" : {"eq":"goget/Stuff"}}}""")
        assertThat(parse.when.url.eq, equalTo("goget/Stuff"))
    }

    @Test
    void shouldParseJustAUriEqI() {
        ExpectationRequest parse = parse("""{"when" : {"url" : {"eqI":"goget/Stuff"}}}""")
        assertThat(parse.when.url.eqI, equalTo("goget/Stuff"))
    }

    @Test
    void shouldParseAMethodType() {
        ExpectationRequest parse = parse("""{"when" : {"method" : "post"}}""")
        assertThat(parse.when.method, equalTo("post"))
    }

    @Test
    void shouldParseJustAUriRegex() {
        ExpectationRequest parse = parse("""{"when" : {"url" : {"regex":"goget/Stuff"}}}""")
        assertThat(parse.when.url.regex, equalTo("goget/Stuff"))
    }

    @Test
    void shouldParseJustUriAsOneOfTwoValues() {
        ExpectationRequest parse = parse("""{"when" : {"url" : {"any":[{"eq":"goget/Stuff"},{"eq":"goget/OtherStuff"}]}}}""")
        def any = parse.when.url.any
        assertThat(any.size, equalTo(2))
    }
    @Test
    void shouldParseUriAndHeaderAndCookie() {
        ExpectationRequest parse = parse(
                """{"when" : {
                        "url" : {"eq":"goget/Stuff"},
                        "header" : {"name": {"eqI":"Accept"}, "value":{"eq":"application/json"}},
                        "cookie" : {"name": {"eq":"ssoid"}, "value":{"eq":"1234"}}
                }}"""
        )
        assertThat(parse.when.url.eq, equalTo("goget/Stuff"))
        assertThat(parse.when.header.name.eqI, equalTo("Accept"))
        assertThat(parse.when.header.value.eq, equalTo("application/json"))
        assertThat(parse.when.cookie.name.eq, equalTo("ssoid"))
        assertThat(parse.when.cookie.value.eq, equalTo("1234"))
    }

    @Test
    void shouldParseAllInTheRequest() {
        ExpectationRequest parse = parse(
                """{"when" : {
                        "all" : [
                            {"url" : {"eq":"goget/Stuff"}},
                            {"header" : {"name": {"eqI":"Accept"}, "value":{"regex":"application/json"}}},
                            {"header" : {"name": {"eqI":"X-UUID"}, "value":{"eq":"4321"}}},
                            {"cookie" : {"name": {"eq":"ssoid"}, "value":{"eq":"1234"}}}
                        ]
                }}"""
        )
        def all = parse.when.all
        assertThat(all.size(), equalTo(4))

        assertThat(all[0].url.eq, equalTo("goget/Stuff"))
        assertThat(all[1].header.name.eqI, equalTo("Accept"))
        assertThat(all[1].header.value.regex, equalTo("application/json"))
        assertThat(all[2].header.name.eqI, equalTo("X-UUID"))
        assertThat(all[2].header.value.eq, equalTo("4321"))
        assertThat(all[3].cookie.name.eq, equalTo("ssoid"))
        assertThat(all[3].cookie.value.eq, equalTo("1234"))
    }

//    @Test
//    void shouldParseBody() {
//        ExpectationRequest parse = parse("""{"when" : {"body" : {"regex":"goget/Stuff"}}}""")
//        assertThat(parse.when.body.regex, equalTo("goget/Stuff"))
//    }
//
//    @Test
//    void shouldParseBodyFields() {
//        ExpectationRequest parse = parse("""{"when" : {"body" : {"field":{"name":"/this/that", "regex":"Stuff*"}}}}""")
//        assertThat(parse.when.body.field.name, equalTo("/this/that"))
//        assertThat(parse.when.body.field.regex, equalTo("Stuff*"))
//    }
//    @Test
//    void shouldParseNestedBodyFields() {
//        ExpectationRequest parse = parse("""{"when" : {"body" : {"field":{"name":"/this/that", "field":{"name":"other", "regex":"Stuff*"}}}}}""")
//        assertThat(parse.when.body.field.name, equalTo("/this/that"))
//        assertThat(parse.when.body.field.field.name, equalTo("other"))
//        assertThat(parse.when.body.field.field.regex, equalTo("Stuff*"))
//    }
//
//    @Test
//    void shouldParseCombinedNestedBodyFields() {
//        ExpectationRequest parse = parse("""{"when" : {"body" : {"field":{"name":"/this/that", "any":[{"field":{"name":"other", "regex":"Stuff*"}},{"eq":"moo"}]}}}}""")
//        assertThat(parse.when.body.field.name, equalTo("/this/that"))
//        def any = parse.when.body.field.any
//        assertThat(any.size(), equalTo(2))
//        assertThat(any[0].field.name, equalTo("other"))
//        assertThat(any[0].field.regex, equalTo("Stuff*"))
//        assertThat(any[1].eq, equalTo("moo"))
//    }

    @Test
    void shouldParseDefaultDelayAsZero() {
        ExpectationRequest parse = parse(
                """{"when" : {
                        "url" : {"eq":"goget/Stuff"}
                    },
                    "then" : {"respond":{
                        "code":"200",
                        "contentType":"text/plain",
                        "body":"ello",
                        "headers":[
                           {"name":"X-UUID", "values":["321654897"]}
                        ]
                    }}
                }"""
        )
        def delay = parse.then.respond.toResponse().delay
        assertThat(delay.timeMillis, equalTo(0L))
        assertThat(delay.jitter, equalTo(0L))
    }

    @Test
    void shouldParseSimpleResponse() {
        ExpectationRequest parse = parse(
                """{"when" : {
                        "url" : {"eq":"goget/Stuff"}
                    },
                    "then" : {"respond":{
                        "code":"200",
                        "contentType":"text/plain",
                        "body":"ello",
                        "delay":{"timeMillis":500,"jitter":100},
                        "headers":[
                           {"name":"X-UUID", "values":["321654897"]}
                        ]
                    }}
                }"""
        )
        def then = parse.then
        assertThat(then, notNullValue())
        assertThat(then.respond, notNullValue())

        assertThat(then.respond.code, equalTo("200"))
        assertThat(then.respond.contentType, equalTo("text/plain"))
        assertThat(then.respond.body, equalTo("ello"))
        def headers = then.respond.headers
        assertThat(headers.size(), equalTo(1))
        assertThat(headers[0].name, equalTo("X-UUID"))
        assertThat(headers[0].values.size(), equalTo(1))
        assertThat(headers[0].values[0], equalTo("321654897"))
        def delayMatch = then.respond.delay
        assertThat(delayMatch.timeMillis, equalTo(500L))
        assertThat(delayMatch.jitter, equalTo(100L))
    }

    @Test
    void ShouldParseWeightedResponse() {
        ExpectationRequest parse = parse(
                """{"when" : {
                        "url" : {"eq":"goget/Stuff"}
                    },
                    "then" : {"weighted": [ {"weight":2, "outcome": {"respond":{
                                                                        "code":"200",
                                                                        "contentType":"text/plain",
                                                                        "body":"ello",
                                                                        "delay":{"timeMillis":500,"jitter":100},
                                                                        "headers":[
                                                                           {"name":"X-UUID", "values":["321654897"]}
                                                                        ]
                                                                    }}},
                                            {"weight":8, "outcome": {"respond":{
                                                                       "code":"202",
                                                                       "contentType":"text/html",
                                                                       "body":"redirected",
                                                                       "delay":{"timeMillis":10,"jitter":4},
                                                                       "headers":[
                                                                          {"name":"X-IP", "values":["123.456.789.110"]}
                                                                       ]
                                                                   }}}]}
                }"""
        )

        def then = parse.then
        assertThat(then, notNullValue())
        assertThat(then.weighted, notNullValue())

        assertThat(then.weighted[0].weight, equalTo(new Integer(2)))
        assertThat(then.weighted[1].weight, equalTo(new Integer(8)))

        assertThat(then.weighted[0].outcome, notNullValue())
        assertThat(then.weighted[1].outcome, notNullValue())
        assertThat(then.weighted[0].outcome.respond, notNullValue())
        assertThat(then.weighted[1].outcome.respond, notNullValue())

        assertThat(then.weighted[0].outcome.respond.code, equalTo("200"))
        assertThat(then.weighted[1].outcome.respond.code, equalTo("202"))
        assertThat(then.weighted[0].outcome.respond.contentType, equalTo("text/plain"))
        assertThat(then.weighted[1].outcome.respond.contentType, equalTo("text/html"))
        assertThat(then.weighted[0].outcome.respond.body, equalTo("ello"))
        assertThat(then.weighted[1].outcome.respond.body, equalTo("redirected"))

        def headers0 = then.weighted[0].outcome.respond.headers[0]
        def headers1 = then.weighted[1].outcome.respond.headers[0]
        assertThat(headers0.name, equalTo("X-UUID"))
        assertThat(headers1.name, equalTo("X-IP"))
        assertThat(headers0.values[0], equalTo("321654897"))
        assertThat(headers1.values[0], equalTo("123.456.789.110"))

        def delayMatch0 = then.weighted[0].outcome.respond.delay
        def delayMatch1 = then.weighted[1].outcome.respond.delay
        assertThat(delayMatch0.timeMillis, equalTo(500L))
        assertThat(delayMatch0.jitter, equalTo(100L))
        assertThat(delayMatch1.timeMillis, equalTo(10L))
        assertThat(delayMatch1.jitter, equalTo(4L))
    }

    ExpectationRequest parse(String json) {
        return converter.read(ExpectationRequest.class, asHttpMessage(json))
    }

    private HttpInputMessage asHttpMessage(final String json) {
        new HttpInputMessage() {
            InputStream getBody() { return new ByteArrayInputStream(json.getBytes()); }
            HttpHeaders getHeaders() { throw new RuntimeException("getHeaders() called - WTF") }
        }
    }

}
