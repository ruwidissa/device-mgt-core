/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package io.entgra.device.mgt.core.transport.mgt.email.sender.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.Map;

public class VelocityBasedEmailContentProvider implements EmailContentProvider {

    private VelocityEngine engine;
    private static final Log log = LogFactory.getLog(VelocityBasedEmailContentProvider.class);

    private static final String EMAIL_TEMPLATE_PATH =   "repository" + File.separator +
            "resources" + File.separator + "email-templates";
    public VelocityBasedEmailContentProvider() {
        engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, CarbonUtils.getCarbonHome());
        engine.init();
    }

    @Override
    public EmailData getContent(String name, Map<String,
            TypedValue<Class<?>, Object>> params) throws ContentProcessingInterruptedException {
        VelocityContext ctx = new VelocityContext();
        for (Map.Entry<String, TypedValue<Class<?>, Object>> param : params.entrySet()) {
            ctx.put(param.getKey(), param.getValue().getValue());
        }
        Template template = engine.getTemplate(EMAIL_TEMPLATE_PATH + File.separator + name + ".vm");

        StringWriter content = new StringWriter();
        template.merge(ctx, content);

        InputStream is = null;
        try {
            JAXBContext jaxbCtx = JAXBContext.newInstance(EmailData.class);
            Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();

            is = new ByteArrayInputStream(content.toString().getBytes());
            return (EmailData) unmarshaller.unmarshal(is);
        } catch (JAXBException e) {
            throw new ContentProcessingInterruptedException("Error occurred while parsing email data", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.warn("Error occurred while closing input stream used to convert email configuration " +
                            "to an internal object model", e);
                }
            }
        }
    }

}
