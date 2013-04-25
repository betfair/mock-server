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

package com.betfair.site.common.core.web.mock.request;

import com.betfair.site.common.core.utils.FreemarkerProcessor;
import com.betfair.site.common.core.web.mock.request.annotations.RequestMock;
import com.betfair.site.common.core.web.mock.request.annotations.RequestMockDataProvider;
import com.betfair.site.common.core.web.mock.request.annotations.RequestMocks;
import com.betfair.utils.mockingclient.MockServerClient;
import com.betfair.utils.mockingclient.domain.*;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import freemarker.template.TemplateException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Arrays.asList;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.util.StringUtils.hasText;

/**
 * Date: 12/7/12 - Time: 12:45 AM
 *
 * @author Bruno Lopes
 */
public abstract class AbstractRequestMockLoader {

    private static final Logger LOG = Logger.getLogger("AbstractRequestMockLoader");

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TEMPLATES_PATH = "/mocks";
    private static final String TEMPLATES_MODULES_PREFIX = "/modules";

    private Map<RequestMockContext, RequestMockContext> contexts = new ConcurrentHashMap<RequestMockContext, RequestMockContext>();

    public void loadRequestMocks(RequestMockContext context)
            throws IOException, InvocationTargetException, NoSuchMethodException, TemplateException,
            IllegalAccessException {
        context = getContext(context);
        if (context.getTestInstance() instanceof RequestMockSupport) {
            List<RequestMock> mocks = getRequestMocks(context);
            if (!mocks.isEmpty()) {
                MockSession session = setupMockServer(context);
                for (RequestMock mock : mocks) {
                    processRequestMock(context, mock, session);
                }
            }
        }
    }

    public void unloadRequestMocks(RequestMockContext context) {
        context = removeContext(context);
        if (context != null && context.getTestInstance() instanceof RequestMockSupport
                && context.getMockSession() != null) {
            getMockServerClient(context).deleteSession(context.getMockSession());
        }
    }

    private List<RequestMock> getRequestMocks(RequestMockContext context) {
        List<RequestMock> list = new ArrayList<RequestMock>();
        addRequestMock(list, findAnnotation(context.getTestClass(), RequestMock.class));
        addRequestMocks(list, findAnnotation(context.getTestClass(), RequestMocks.class));
        addRequestMock(list, findAnnotation(context.getTestMethod(), RequestMock.class));
        addRequestMocks(list, findAnnotation(context.getTestMethod(), RequestMocks.class));
        return list;
    }

    private void addRequestMocks(List<RequestMock> list, RequestMocks mocks) {
        if (mocks != null) {
            list.addAll(asList(mocks.value()));
        }
    }

    private void addRequestMock(List<RequestMock> list, RequestMock mock) {
        if (mock != null) {
            list.add(mock);
        }
    }

    private void processRequestMock(RequestMockContext context, RequestMock requestMock, MockSession mockSession)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException, TemplateException {

        Map<String, Object> data = retrieveMockData(context, requestMock);

        String expectations = getFreemarkerProcessor(context)
                .processTemplate(getTemplateName(requestMock), TEMPLATES_PATH, data);

        if (hasText(expectations)) {
            boolean expectationAdded = false;
            try {
                ExpectationTO expectationsTo = MAPPER.readValue(expectations, ExpectationTO.class);
                getMockServerClient(context).addExpectation(mockSession, expectationsTo);
                LOG.log(Level.INFO, "Added expectation with the following data:\n" + expectations);
                expectationAdded = true;
            } catch (JsonMappingException e) {
                LOG.log(Level.INFO, "Couldn't parse expectation text. Attempting to parse as Expectation array!");
            }

            // if we couldn't parse
            if (!expectationAdded) {
                ExpectationTO[] expectationsTos = MAPPER.readValue(expectations, ExpectationTO[].class);

                for (ExpectationTO expectationsTo : expectationsTos) {
                    getMockServerClient(context).addExpectation(mockSession, expectationsTo);
                }
                LOG.log(Level.INFO, "Added expectation with the following data:\n" + expectations);
            }
        }
    }

    protected Map<String, Object> retrieveMockData(RequestMockContext context, RequestMock requestMock)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map<String, Object> data = new HashMap<String, Object>();
        String dataProvider = requestMock.dataProvider();
        Class<?> dataProviderClass = requestMock.dataProviderClass();
        if (hasText(dataProvider)) {
            if (dataProviderClass.equals(Object.class)) {
                dataProviderClass = context.getTestClass();
            }

            Method dataProviderMethod = null;
            for (Method method : dataProviderClass.getMethods()) {
                if (method.isAnnotationPresent(RequestMockDataProvider.class)) {
                    String currentProviderName =
                            getDataProviderName(method.getAnnotation(RequestMockDataProvider.class), method.getName());
                    if (dataProvider.equals(currentProviderName)) {
                        dataProviderMethod = method;
                        break;
                    }
                }
            }
            if (dataProviderMethod != null) {

                Object rawData;
                String[] dataProviderArgs = requestMock.dataProviderArgs();

                if (dataProviderArgs.length > 0) {
                    rawData = dataProviderMethod.invoke(context.getTestInstance(), new Object[]{dataProviderArgs});
                } else {
                    rawData = dataProviderMethod.invoke(context.getTestInstance());
                }

                if (rawData instanceof Map) {
                    for (Map.Entry entry : ((Map<Object, Object>) rawData).entrySet()) {
                        data.put(entry.getKey().toString(), entry.getValue());
                    }
                } else {
                    throw new RuntimeException("Mock Loader for test '" + context.toString() +
                            "' was unable to use RequestMockDataProvider '" + dataProvider +
                            "' because it does not return a Map object.");
                }
            }
        }
        return data;
    }

    /**
     * Creates and returns a new session with the specified request uuid
     *
     * @param requestUUID
     * @return
     */
    private SessionTO createSessionTO(UUID requestUUID) {
        final String X_UUID = "X-UUID";
        final String X_REQUEST_UUID = "X-requestuuid";

        SessionTO session = new SessionTO();
        RequestMatchTO requestMatch = new RequestMatchTO();
        session.setCommonWhen(requestMatch);

        List<Object> any = Lists.newArrayList();
        requestMatch.setAny(any);
        any.add(addHeaderMatchTO(X_UUID, requestUUID.toString()));
        any.add(addHeaderMatchTO(X_REQUEST_UUID, requestUUID.toString()));

        return session;
    }

    private Map<String, Object> addHeaderMatchTO(String headerName, String headerValue) {
        Map<String, Object> headerMatch = Maps.newHashMap();

        NamedMatchTO match = new NamedMatchTO();
        ValueMatchTO matchName = new ValueMatchTO();
        matchName.setEqI(headerName);
        match.setName(matchName);

        ValueMatchTO matchValue = new ValueMatchTO();
        matchValue.setEq(headerValue);
        match.setValue(matchValue);

        headerMatch.put("header", match);
        return headerMatch;
    }

    private MockSession setupMockServer(RequestMockContext context) {
        RequestMockSupport testInstance = (RequestMockSupport) context.getTestInstance();
        UUID requestUUID = UUID.randomUUID();
        testInstance.setRequestId(requestUUID.toString());
        MockSession session = getMockServerClient(context).createSession(createSessionTO(requestUUID));
        context.setMockSession(session);
        return session;
    }

    abstract protected FreemarkerProcessor getFreemarkerProcessor(RequestMockContext context);

    abstract protected MockServerClient getMockServerClient(RequestMockContext context);

    private String getDataProviderName(RequestMockDataProvider requestMockDataProvider, String methodName) {
        String value = getValue(requestMockDataProvider.value(), requestMockDataProvider.name());
        if (value != null) {
            return value;
        }
        return methodName;
    }

    private String getTemplateName(RequestMock requestMock) {
        return TEMPLATES_MODULES_PREFIX + getValue(requestMock.value(), requestMock.template(),
                "Template is a required parameter on the RequestMock annotation.");
    }

    private String getValue(String value, String defaultValue) {
        return getValue(value, defaultValue, null);
    }

    private String getValue(String value, String defaultValue, String errorMessage) {
        if (hasText(value)) {
            return value;
        }
        if (hasText(defaultValue)) {
            return defaultValue;
        }
        if (errorMessage != null) {
            throw new RuntimeException(errorMessage);
        }
        return null;
    }

    protected class RequestMockContext {
        protected Object testInstance;
        protected Method testMethod;
        protected MockSession session;

        public RequestMockContext(Object testInstance, Method testMethod) {
            this.testInstance = testInstance;
            this.testMethod = testMethod;
        }

        public Class<?> getTestClass() {
            return testInstance.getClass();
        }

        public Object getTestInstance() {
            return testInstance;
        }

        public Method getTestMethod() {
            return testMethod;
        }

        protected void setMockSession(MockSession session) {
            this.session = session;
        }

        protected MockSession getMockSession() {
            return this.session;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final RequestMockContext that = (RequestMockContext) o;
            return Objects.equal(this.testInstance, that.testInstance) && Objects.equal(this.testMethod, that.testMethod);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(testInstance, testMethod);
        }
    }

    protected RequestMockContext removeContext(RequestMockContext context) {
        return contexts.remove(context);
    }

    protected RequestMockContext getContext(RequestMockContext providedContext) {
        RequestMockContext context = contexts.get(providedContext);
        if (context == null) {
            context = providedContext;
            contexts.put(providedContext, context);
        }
        return context;
    }

}

