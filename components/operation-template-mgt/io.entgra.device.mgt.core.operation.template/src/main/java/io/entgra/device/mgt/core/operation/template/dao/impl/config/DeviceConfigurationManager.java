/*
 * Copyright (c) 2023, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
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

package io.entgra.device.mgt.core.operation.template.dao.impl.config;

import io.entgra.device.mgt.core.operation.template.dao.impl.util.OperationTemplateManagementUtil;
import org.w3c.dom.Document;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Class responsible for the mobile device manager configuration initialization.
 */
public class DeviceConfigurationManager {

    public static final String DEVICE_CONFIG_XML_NAME = "cdm-config.xml";
    private DeviceManagementConfig deviceManagementConfig;
    private static DeviceConfigurationManager deviceConfigurationManager;
    private final String operationTemplateMgtConfigXMLPath = CarbonUtils.getCarbonConfigDirPath() + File.separator +
            DEVICE_CONFIG_XML_NAME;

    /**
     *
     * @return
     */
    public static DeviceConfigurationManager getInstance() {
        if (deviceConfigurationManager == null) {
            synchronized (DeviceConfigurationManager.class) {
                if (deviceConfigurationManager == null) {
                    deviceConfigurationManager = new DeviceConfigurationManager();
                }
            }
        }
        return deviceConfigurationManager;
    }

    /**
     *
     * @throws DeviceManagementException
     */
    public synchronized void initConfig() throws DeviceManagementException {
        try {
            File deviceMgtConfig = new File(operationTemplateMgtConfigXMLPath);
            Document doc = OperationTemplateManagementUtil.convertToDocument(deviceMgtConfig);
            JAXBContext mobileDeviceMgmtContext =
                    JAXBContext.newInstance(DeviceManagementConfig.class);
            Unmarshaller unmarshaller = mobileDeviceMgmtContext.createUnmarshaller();
            this.deviceManagementConfig =
                    (DeviceManagementConfig) unmarshaller.unmarshal(doc);
        } catch (Exception e) {
            throw new DeviceManagementException(
                    "Error occurred while initializing Mobile Device Management config", e);
        }
    }

    /**
     *
     * @return
     */
    public DeviceManagementConfig getDeviceManagementConfig() {
        return deviceManagementConfig;
    }

}
