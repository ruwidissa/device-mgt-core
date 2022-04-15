/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.report.mgt.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidConfigurationException;
import org.wso2.carbon.device.mgt.common.exceptions.ReportManagementException;
import org.wso2.carbon.device.mgt.core.report.mgt.Constants;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * ConfigurationManager is responsible for the managing Application Management related configurations.
 */
public class ReportMgtConfigurationManager {

    private static final Log log = LogFactory.getLog(ReportMgtConfigurationManager.class);

    private ReportMgtConfiguration configuration;

    private static String configPath;

    private static volatile ReportMgtConfigurationManager configurationManager;

    private ReportMgtConfigurationManager() {

    }

    public static ReportMgtConfigurationManager getInstance() {
        if (configurationManager == null) {
            synchronized (ReportMgtConfigurationManager.class) {
                if (configurationManager == null) {
                    configurationManager = new ReportMgtConfigurationManager();
                    try {
                        configurationManager.initConfig();
                    } catch (ReportManagementException e) {
                        log.error(e);
                    }
                }
            }
        }
        return configurationManager;
    }

    public static synchronized void setConfigLocation(String configPath) throws InvalidConfigurationException {
        if (ReportMgtConfigurationManager.configPath == null) {
            ReportMgtConfigurationManager.configPath = configPath;
        } else {
            throw new InvalidConfigurationException("Configuration path " + configPath + " is already defined");
        }
    }

    private void initConfig() throws ReportManagementException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ReportMgtConfiguration.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            if (configPath == null) {
                configPath = Constants.DEFAULT_CONFIG_FILE_LOCATION;
            }
            //TODO: Add validation for the configurations
            this.configuration = (ReportMgtConfiguration) unmarshaller.unmarshal(new File(configPath));
        } catch (Exception e) {
            log.error(e);
            throw new InvalidConfigurationException("Error occurred while initializing application config: "
                    + configPath, e);
        }
    }

    public ReportMgtConfiguration getConfiguration() {
        return configuration;
    }

}
