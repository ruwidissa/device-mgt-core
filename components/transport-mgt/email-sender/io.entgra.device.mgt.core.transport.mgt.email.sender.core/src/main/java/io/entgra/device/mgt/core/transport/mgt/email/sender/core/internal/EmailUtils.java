/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.transport.mgt.email.sender.core.internal;

import io.entgra.device.mgt.core.transport.mgt.email.sender.core.EmailSenderConfigurationFailedException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

class EmailUtils {

    private static final String EMAIL_TEMPLATE_DIR_RELATIVE_REGISTRY_PATH = "/email-templates";
    private static Log log = LogFactory.getLog(EmailSenderServiceComponent.class);

    static void setupEmailTemplates() throws EmailSenderConfigurationFailedException {
        File templateDir =
                new File(CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                         "resources" + File.separator + "email-templates");
        if (!templateDir.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("The directory that is expected to use as the container for all email templates is not " +
                          "available. Therefore, no template is uploaded to the registry");
            }
        }
        if (templateDir.canRead()) {
            try {
                int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
                String tenantTemplateDirectory = CarbonUtils.getCarbonTenantsDirPath() + File.separator + tenantId + File.separator;
                File destinationDirectory = new File(tenantTemplateDirectory);
                FileUtils.copyDirectoryToDirectory(templateDir, destinationDirectory);
            } catch (FileNotFoundException e) {
                throw new EmailSenderConfigurationFailedException("Error occurred while writing template file " +
                                                                  "contents as an input stream of a resource", e);
            } catch (IOException e) {
                throw new EmailSenderConfigurationFailedException("Error occurred while serializing file " +
                                                                  "contents to a string", e);
            }
        }
    }

}
