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

package com.betfair.utils.mockingclient.dsl
import com.betfair.utils.mockingclient.domain.*
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.map.annotate.JsonSerialize

import javax.xml.bind.JAXBContext
/**
 * Builders used for building requests to send to the mock server.
 *
 */

class RequestBuilder {

    /**
     * Start building a session request.
     *
     * @return the session builder.
     */
    def static session() {
        return new SessionBuilder()
    }

    /**
     * Start building an expectation.
     *
     * @return the expectation builder
     */
    def static expectation() {
        return new ExpectationBuilder()
    }
}

class ExpectationBuilder {
    def when
    def outcome


    def RequestMatchBuilder<ExpectationBuilder> when() {
        when = new RequestMatchBuilder<ExpectationBuilder>(this)

        when
    }

    def OutcomeBuilder then() {
        outcome = new OutcomeBuilder(this)

        outcome
    }

    def build() {
        def expectation = new ExpectationTO()
        expectation.when = when.build()
        expectation.then = outcome.build()

        expectation
    }
}

class OutcomeBuilder {
    def parent
    def response

    OutcomeBuilder(parent) {
        this.parent = parent
    }

    def ResponseBuilder respond() {
        response = new ResponseBuilder(parent);

        response
    }

    def OutcomeTO build() {
        def outcome = new OutcomeTO()
        outcome.respond = response.build()

        outcome
    }
}

/**
 * Builds a mock server response.
 */
class ResponseBuilder {

    def static mapper = new ObjectMapper()
    static {
        mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL)
    }

    def parent

    def contentType
    def delay
    def statusCode
    def headers = []
    def cookies = []
    def body
    def type = "plain"

    ResponseBuilder(parent) {
        this.parent = parent
    }

    /**
     * Set the content type of the response. This should be a standard MIME type.
     *
     * @param contentType the content type, as a string
     */
    def ResponseBuilder contentType(contentType) {
        this.contentType = contentType

        this
    }

    /**
     * Set a time period to wait before serving the response. A jitter value can also be specified, which will apply
     * some gaussian noise to the actual period waited.
     *
     * @param time the time to wait, milliseconds
     * @param jitter the mean amount of jitter to add to the response time, milliseconds
     */
    def ResponseBuilder after(time, jitter) {
        delay = new DelayTO(time, jitter)

        this
    }

    /**
     * Set the HTTP status code for the response.
     *
     * @param statusCode the status code
     */
    def ResponseBuilder statusCode(statusCode) {
        this.statusCode = statusCode

        this
    }

    /**
     * Set the response body. Assumed to be a string, use {@see #asXml} or {@see #asJson} to serialise the object in
     * either format.
     *
     * @param body the response body
     */
    def ResponseBuilder withBody(body) {
        this.body = body

        this
    }

    /**
     * Indicate that the body should be serialised to JSON.
     */
    def ResponseBuilder asJson() {
        type = "JSON"

        this
    }

    /**
     * Indicate that the body should be serialised to XML.
     */
    def ResponseBuilder asXml() {
        type = "XML"

        this
    }

    /**
     * Add the header to the response, specified as a named value.
     */
    def NamedValueBuilder<ResponseBuilder> addingHeader() {
        def builder = new NamedValueBuilder<ResponseBuilder>(this)
        headers.add(builder)

        builder
    }

    /**
     * Add the cookie to the response, specified as a named value.
     */
    def NamedValueBuilder<ResponseBuilder> addingCookie() {
        def builder = new NamedValueBuilder<ResponseBuilder>(this)
        cookies.add(builder)

        builder
    }

    /**
     * Indicate creation of this reponse is complete and return control to the parent builder.
     */
    def complete() {
        return parent
    }

    /**
     * Build the response object.
     *
     * @return the compeleted {@see ResponseTO)
     */
    def ResponseTO build() {
        def response = new ResponseTO()

        switch (type) {
            case "XML":
                def output = new ByteArrayOutputStream();
                def context = JAXBContext.newInstance(body.class)
                context.createMarshaller().marshal(body, output)
                response.body = output.toString()
                break;
            case "JSON":
                response.body = mapper.writeValueAsString(body)
                break;
            default:
                response.body = body;
        }
        response.contentType = contentType
        response.delay = delay
        response.code = statusCode

        response
    }
}

/**
 * Builds a request matcher.
 *
 * @param < T > type of the parent builder
 */
class RequestMatchBuilder<T> {
    def values = [:]
    def T parent

    RequestMatchBuilder(T parent) {
        this.parent = parent
    }

    /**
     * Indicate that this request matcher should match on URL. The details of the URL matching can be configured with
     * the returned builder.
     */
    def ValueBuilder<RequestMatchBuilder<T>> url() {
        def builder = new ValueBuilder<RequestMatchBuilder<T>>(this)
        values["url"] = builder
        builder
    }

    /**
     * Indicate that this request matcher should match on a header. The details of the match can be configured with the
     * returned builder.
     */
    def NamedValueBuilder<RequestMatchBuilder<T>> header() {
        def builder = new NamedValueBuilder(this)
        values["header"] = builder
        builder
    }

    /**
     * Indicates that this matcher has been completely configured, and returns control to the parent builder.
     */
    def T matches() {
        parent
    }

    def build() {
        def result = new RequestMatchTO()
        values.each({key, value ->
            result[key as String] = value.build()
        } )
        result
    }
}

class SessionBuilder {
    def request = new RequestMatchBuilder<SessionBuilder>(this);

    /**
     * Define what conditions must be met to determine that a request is part of this session.
     */
    def RequestMatchBuilder<SessionBuilder> when() {
        request
    }

    def build() {
        def result = new SessionTO()
        result.commonWhen = request.build()

        result
    }
}

class NamedValueBuilder<T> {
    def T parent
    def name
    def value

    NamedValueBuilder(parent) {
        this.parent = parent
    }

    /**
     * Set the name of the field to match.
     */
    def ValueBuilder<NamedValueBuilder<T>> name() {
        name = new ValueBuilder<NamedValueBuilder<T>>(this)
    }

    /**
     * Set the value of the field to match.
     */
    def ValueBuilder<T> value() {
        value = new ValueBuilder<T>(parent)
    }

    def build() {
        def match = new NamedMatchTO()
        match.name = name.build()
        match.value = value.build()
        match
    }
}

class ValueBuilder<T> {

    def String type
    def value
    def parent

    ValueBuilder(T parent) {
        this.parent = parent
    }

    def T endingWith(suffix){
        type = "endsWith"
        value = suffix
        parent
    }

    def T equalTo(other) {
        type = "eq"
        value = other
        parent
    }

    def T equalInsensitiveTo(other) {
        type = "eqI"
        value = other
        parent
    }

    def T beginsWith(other) {
        type = "beginsWith"
        value = other
        parent
    }

    def T contains(other) {
        type = "contains"
        value = other
        parent
    }

    def T regexMatching(other) {
        type = "regex"
        value = other
        parent
    }

    def T isIn(other) {
        type = "isIn"
        value = other
        parent
    }




    def build() {
        def match = new ValueMatchTO()
        match[type] = value
        match
    }
}
