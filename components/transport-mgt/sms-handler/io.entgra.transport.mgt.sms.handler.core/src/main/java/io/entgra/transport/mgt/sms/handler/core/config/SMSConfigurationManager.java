/*
 * Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.transport.mgt.sms.handler.core.config;

import io.entgra.transport.mgt.sms.handler.common.SMSHandlerConstants;
import io.entgra.transport.mgt.sms.handler.common.config.SMSConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Responsible for the SMS configuration initialization.
 */
public class SMSConfigurationManager {

    private static final Log log = LogFactory.getLog(SMSConfigurationManager.class);
    private static SMSConfigurationManager smsConfigurationManager;
    private SMSConfiguration smsConfiguration;
    private static final String SMS_CONFIG_PATH = CarbonUtils.getCarbonConfigDirPath() + File.separator
            + SMSHandlerConstants.SMS_CONFIG_XML_NAME;

    /**
     * Retrieve an instance of {@link SMSConfigurationManager}
     * @return an instance of {@link SMSConfigurationManager}
     */
    public static SMSConfigurationManager getInstance() {
        if (smsConfigurationManager == null) {
            synchronized (SMSConfigurationManager.class) {
                if (smsConfigurationManager == null) {
                    smsConfigurationManager = new SMSConfigurationManager();
                }
            }
        }
        return smsConfigurationManager;
    }

    /**
     * Initialize the SMS Configuration through the provided configuration location
     * @param configLocation has the path of the SMS configuration file
     * @throws DeviceManagementException throws when there are any errors during the initialization of
     * SMS configuration
     */
    public synchronized void initConfig(String configLocation) throws DeviceManagementException {
        try {
            File smsConfig = new File(configLocation);
            Document doc = DeviceManagerUtil.convertToDocument(smsConfig);

            /* Un-marshaling SMS configuration */
            JAXBContext smsContext = JAXBContext.newInstance(SMSConfiguration.class);
            Unmarshaller unmarshaller = smsContext.createUnmarshaller();
            this.smsConfiguration = (SMSConfiguration) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            String msg = "Error occurred while initializing SMS config '" + configLocation + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    /**
     * Initialize the SMS Configuration through the sms-config.xml file in the SMS_CONFIG_PATH
     * @throws DeviceManagementException throws when there are any errors during the initialization of
     * SMS configuration
     */
    public void initConfig() throws DeviceManagementException {
        this.initConfig(SMS_CONFIG_PATH);
    }

    /**
     * Retrieves the initialized {@link SMSConfiguration}
     * @return the initialized {@link SMSConfiguration}
     */
    public SMSConfiguration getSMSConfig() {
        return smsConfiguration;
    }
}
