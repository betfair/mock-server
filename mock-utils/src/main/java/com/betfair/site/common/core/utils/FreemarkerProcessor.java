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

package com.betfair.site.common.core.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * User: martinsj
 * Date: 12/3/12
 */
@Component(value = FreemarkerProcessor.COMPONENT_NAME)
public final class FreemarkerProcessor {

    public static final String COMPONENT_NAME = "freemarkerProcessor";

    public String processTemplate(String templateName, String pathPrefix) throws IOException, TemplateException {
        return processTemplate(templateName, pathPrefix, null);
    }

    public String processTemplate(String templateName, String pathPrefix, Map<String, Object> values)
            throws IOException, TemplateException {

        Template template = loadTemplate(templateName, pathPrefix);
        return processTemplate(template, values);
    }

    private String processTemplate(Template template, Map<String, Object> values)
            throws IOException, TemplateException {

        StringWriter parsedModelWriter = new StringWriter();
        template.process(values, parsedModelWriter);
        return parsedModelWriter.toString();
    }

    private Template loadTemplate(String templateName, String pathPrefix) throws IOException {
        Configuration cfg = new Configuration();
        cfg.setClassForTemplateLoading(this.getClass(), pathPrefix);
        return cfg.getTemplate(templateName);
    }
}
