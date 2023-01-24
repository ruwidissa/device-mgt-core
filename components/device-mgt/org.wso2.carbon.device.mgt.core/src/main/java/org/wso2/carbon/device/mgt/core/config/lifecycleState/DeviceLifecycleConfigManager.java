/*
 *   Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.config.lifecycleState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Class responsible for the LifecycleStates configuration initialization.
 */
public class DeviceLifecycleConfigManager {

    private static final Log log = LogFactory.getLog(DeviceLifecycleConfigManager.class);
    private DeviceLifecycleConfig deviceLifecycleConfig;
    private static volatile DeviceLifecycleConfigManager deviceLifecycleConfigManager;
    private static final String DEVICE_LIFECYCLE_PATH = CarbonUtils.getCarbonConfigDirPath() + File.separator
            + DeviceManagementConstants.DataSourceProperties.DEVICE_LIFECYCLE_STATES_XML_NAME;

    private DeviceLifecycleConfigManager() {
    }

    public static DeviceLifecycleConfigManager getInstance() {
        if (deviceLifecycleConfigManager == null) {
            synchronized (DeviceLifecycleConfigManager.class) {
                if (deviceLifecycleConfigManager == null) {
                    deviceLifecycleConfigManager = new DeviceLifecycleConfigManager();
                    try {
                        deviceLifecycleConfigManager.initConfig();
                    } catch (Exception e) {
                        log.error(e);
                    }
                } else {
                    try {
                        deviceLifecycleConfigManager.initConfig();
                    } catch (Exception e) {
                        log.error(e);
                    }
                }
            }
        }
        return deviceLifecycleConfigManager;
    }

    public synchronized void initConfig() {
        try {
            File deviceLifecycleConfig = new File(DEVICE_LIFECYCLE_PATH);
            JAXBContext jaxbContext = JAXBContext.newInstance(DeviceLifecycleConfig.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            this.deviceLifecycleConfig = (DeviceLifecycleConfig) unmarshaller.unmarshal(deviceLifecycleConfig);

        } catch (JAXBException e) {
            String msg = "Error occured while initializing deviceLifecycle config";
            log.error(msg, e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            String msg = "Error(Exception) occured while initializing deviceLifecycle config";
            log.error(msg, e);
            throw new RuntimeException(e);
        }
    }

    public DeviceLifecycleConfig getDeviceLifecycleConfig() {
        return deviceLifecycleConfig;
    }
}
