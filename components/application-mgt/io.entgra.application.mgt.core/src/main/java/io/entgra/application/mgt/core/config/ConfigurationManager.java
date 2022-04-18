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
package io.entgra.application.mgt.core.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.InvalidConfigurationException;
import io.entgra.application.mgt.core.util.Constants;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

/**
 * ConfigurationManager is responsible for the managing Application Management related configurations.
 */
public class ConfigurationManager {

    private static final Log log = LogFactory.getLog(ConfigurationManager.class);

    private Configuration configuration;

    private IdentityServiceProviderConfiguration identityServiceProviderConfiguration;

    private static String configPath;

    private static String identityServerConfigPath;

    private static volatile ConfigurationManager configurationManager;

    private ConfigurationManager() {

    }

    public static ConfigurationManager getInstance() {
        if (configurationManager == null) {
            synchronized (ConfigurationManager.class) {
                if (configurationManager == null) {
                    configurationManager = new ConfigurationManager();
                    try {
                        configurationManager.initConfig();
                    } catch (ApplicationManagementException e) {
                        log.error(e);
                    }
                } else {
                    try {
                        configurationManager.initConfig();
                    } catch (ApplicationManagementException e) {
                        log.error(e);
                    }
                }
            }
        }
        return configurationManager;
    }

    public static synchronized void setIdentityServerConfigPathConfigLocation(String configPath) throws InvalidConfigurationException {
        if (identityServerConfigPath == null) {
            identityServerConfigPath = configPath;
        } else {
            throw new InvalidConfigurationException("Configuration path " + configPath + " is already defined");
        }
    }

    public static synchronized void setConfigLocation(String configPath) throws InvalidConfigurationException {
        if (ConfigurationManager.configPath == null) {
            ConfigurationManager.configPath = configPath;
        } else {
            throw new InvalidConfigurationException("Configuration path " + configPath + " is already defined");
        }
    }

    private void initConfig() throws ApplicationManagementException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
            JAXBContext jaxbISConfigContext = JAXBContext.newInstance(IdentityServiceProviderConfiguration.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Unmarshaller identityServerConfigUnmarshaller = jaxbISConfigContext.createUnmarshaller();
            if (configPath == null) {
                configPath = Constants.DEFAULT_CONFIG_FILE_LOCATION;
            }
            if (identityServerConfigPath == null) {
                identityServerConfigPath = Constants.DEFAULT_IDENTITY_SERVERS_CONFIG_FILE_LOCATION;
            }
            //TODO: Add validation for the configurations
            this.configuration = (Configuration) unmarshaller.unmarshal(new File(configPath));
            this.identityServiceProviderConfiguration = (IdentityServiceProviderConfiguration) identityServerConfigUnmarshaller.unmarshal(new File(identityServerConfigPath));
        } catch (Exception e) {
            log.error(e);
            throw new InvalidConfigurationException("Error occurred while initializing application managements configs: ", e);
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public IdentityServiceProviderConfiguration getIdentityServerConfiguration() {
        return identityServiceProviderConfiguration;
    }

    public Extension getExtension(Extension.Name extName) throws InvalidConfigurationException {
        for (Extension extension : configuration.getExtensions()) {
            if (extension.getName().contentEquals(extName.toString())) {
                return extension;
            }
        }
        throw new InvalidConfigurationException("Expecting an extension with name - " + extName + " , but not found!");
    }
}
