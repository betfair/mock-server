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

package com.betfair.site.common.core.web.mock.request.selenium;

import com.betfair.site.common.core.utils.FreemarkerProcessor;
import com.betfair.site.common.core.web.mock.request.AbstractRequestMockLoader;
import com.betfair.site.common.core.web.mock.request.RequestMockSupport;
import com.betfair.site.web.testing.utils.configuration.ConfigHelper;
import com.betfair.utils.mockingclient.MockServerClient;

import java.lang.reflect.Method;

/**
 * Date: 12/7/12 - Time: 12:38 AM
 *
 * @author Bruno Lopes
 */
public class RequestMockLoaderAcceptanceTestHelper extends AbstractRequestMockLoader {


    public static final String MOCK_SERVER_HOST = "mock.server.host";
    public static final String MOCK_SERVER_PORT = "mock.server.port";
    public static final String DFT_MOCK_SERVER_HOST = "localhost";
    public static final String DFT_MOCK_SERVER_PORT = "9192";

    private FreemarkerProcessor freemarkerProcessor = new FreemarkerProcessor();

    private MockServerClient mockServerClient;

    private ConfigHelper config = new ConfigHelper();

    public RequestMockLoaderAcceptanceTestHelper() {
        String mockServerHost = getConfigProperty(MOCK_SERVER_HOST, DFT_MOCK_SERVER_HOST);
        String mockServerPort = getConfigProperty(MOCK_SERVER_PORT, DFT_MOCK_SERVER_PORT);
        mockServerClient = new MockServerClient(mockServerHost, mockServerPort);
    }

    private String getConfigProperty(String key, String defaultValue) {
        String value = config.getProp(key);
        return value != null ? value : defaultValue;
    }

    public void loadRequestMocks(RequestMockSupport instance, Method method) {
        try {
            loadRequestMocks(createContext(instance, method));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void unloadRequestMocks(RequestMockSupport instance, Method method) {
        unloadRequestMocks(createContext(instance, method));
    }

    private RequestMockContext createContext(RequestMockSupport instance, Method method) {
        return new RequestMockContext(instance, method);
    }

    @Override
    protected FreemarkerProcessor getFreemarkerProcessor(RequestMockContext context) {
        return freemarkerProcessor;
    }

    @Override
    protected MockServerClient getMockServerClient(RequestMockContext context) {
        return mockServerClient;
    }

}
