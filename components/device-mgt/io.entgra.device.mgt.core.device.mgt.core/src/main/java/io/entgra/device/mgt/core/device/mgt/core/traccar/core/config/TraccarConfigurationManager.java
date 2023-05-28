/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.core.traccar.core.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.core.traccar.common.TraccarHandlerConstants;
import io.entgra.device.mgt.core.device.mgt.core.traccar.common.config.TraccarConfiguration;
import io.entgra.device.mgt.core.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;


public class TraccarConfigurationManager {
    private static final Log log = LogFactory.getLog(TraccarConfigurationManager.class);
    private static TraccarConfigurationManager traccarConfigurationManager;
    private TraccarConfiguration traccarConfiguration;
    private static final String CarbonUtilsFile = CarbonUtils.getCarbonConfigDirPath() + File.separator;
    private static final String TRACCAR_CONFIG_PATH = CarbonUtilsFile + TraccarHandlerConstants.TraccarConfig.TRACCAR_CONFIG_XML_NAME;

    /**
     * Retrieve an instance of {@link TraccarConfigurationManager}
     * @return an instance of {@link TraccarConfigurationManager}
     */
    public static TraccarConfigurationManager getInstance() {
        if (traccarConfigurationManager == null) {
            synchronized (TraccarConfigurationManager.class) {
                if (traccarConfigurationManager == null) {
                    traccarConfigurationManager = new TraccarConfigurationManager();
                }
            }
        }
        return traccarConfigurationManager;
    }

    /**
     * Initialize the Traccar Configuration through the provided configuration location
     * @param configLocation has the path of the Traccar configuration file
     * @throws DeviceManagementException throws when there are any errors during the initialization of
     * Traccar configuration
     */
    public synchronized void initConfig(String configLocation) throws DeviceManagementException {
        try {
            File traccarConfig = new File(configLocation);
            Document doc = DeviceManagerUtil.convertToDocument(traccarConfig);

            //Un-marshaling Traccar configuration
            JAXBContext traccarContext = JAXBContext.newInstance(TraccarConfiguration.class);
            Unmarshaller unmarshaller = traccarContext.createUnmarshaller();
            this.traccarConfiguration = (TraccarConfiguration) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            String msg = "Error occurred while initializing Traccar config '" + configLocation + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    /**
     * Initialize the Traccar Configuration through the traccar-config.xml file in the TRACCAR_CONFIG_PATH
     * @throws DeviceManagementException throws when there are any errors during the initialization of
     * Traccar configuration
     */
    public void initConfig() throws DeviceManagementException {
        this.initConfig(TRACCAR_CONFIG_PATH);
    }

    /**
     * Retrieves the initialized {@link TraccarConfiguration}
     * @return the initialized {@link TraccarConfiguration}
     */
    public TraccarConfiguration getTraccarConfig() {
        try{
            initConfig();
        }catch (Exception e){
            log.error("TraccarConfiguration:", e);
        }
        return traccarConfiguration;
    }
}
