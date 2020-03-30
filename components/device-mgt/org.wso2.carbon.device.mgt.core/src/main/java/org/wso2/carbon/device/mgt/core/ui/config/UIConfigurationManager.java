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

package org.wso2.carbon.device.mgt.core.ui.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Class responsible for the UI configuration initialization.
 */
public class UIConfigurationManager {

    private static final Log log = LogFactory.getLog(UIConfigurationManager.class);
    private UIConfiguration currentUIConfiguration;
    private static UIConfigurationManager uiConfigurationManager;
    private static final String UI_CONFIG_PATH = CarbonUtils.getCarbonConfigDirPath() + File.separator
            + DeviceManagementConstants.DataSourceProperties.UI_CONFIG_XML_NAME;

    public static UIConfigurationManager getInstance() {
        if (uiConfigurationManager == null) {
            synchronized (UIConfigurationManager.class) {
                if (uiConfigurationManager == null) {
                    uiConfigurationManager = new UIConfigurationManager();
                }
            }
        }
        return uiConfigurationManager;
    }

    public synchronized void initConfig(String configLocation) throws DeviceManagementException {
        try {
            File uiConfig = new File(configLocation);
            Document doc = DeviceManagerUtil.convertToDocument(uiConfig);

            /* Un-marshaling UI configuration */
            JAXBContext cdmContext = JAXBContext.newInstance(UIConfiguration.class);
            Unmarshaller unmarshaller = cdmContext.createUnmarshaller();
            this.currentUIConfiguration = (UIConfiguration) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            String msg = "Error occurred while initializing UI config";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    public void initConfig() throws DeviceManagementException {
        this.initConfig(UI_CONFIG_PATH);
    }

    public UIConfiguration getUIConfig() {
        return currentUIConfiguration;
    }
}
