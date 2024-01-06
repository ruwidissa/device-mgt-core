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


package io.entgra.device.mgt.core.device.mgt.core.search;

import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.DeviceStatusManagementService;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.DeviceStatusManagementServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.entgra.device.mgt.core.device.mgt.core.common.BaseDeviceManagementTest;
import io.entgra.device.mgt.core.device.mgt.core.device.details.mgt.DeviceInformationManager;
import io.entgra.device.mgt.core.device.mgt.core.device.details.mgt.impl.DeviceInformationManagerImpl;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderServiceImpl;

public class DeviceDetails extends BaseDeviceManagementTest {

    private static final Log log = LogFactory.getLog(DeviceDetails.class);

    @BeforeClass
    @Override
    public void init() throws Exception {

            DeviceManagementProviderService deviceManagementProviderService = new DeviceManagementProviderServiceImpl();
            DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceManagementProviderService);

            DeviceStatusManagementService deviceStatusManagementService = new DeviceStatusManagementServiceImpl();
            DeviceManagementDataHolder.getInstance().setDeviceStatusManagementService(deviceStatusManagementService);
    }

    @Test
    public void addDeviceDetails() throws Exception {

        log.debug("Adding the device details to database.....!");
        DeviceInformationManager deviceInformationManager = new DeviceInformationManagerImpl();
//        deviceInformationManager.addDeviceInfo(Utils.getDeviceIdentifier(), Utils.getDeviceInfo());
//        deviceInformationManager.addDeviceLocation(Utils.getSampleDeviceLocation());
        log.debug("Device details added to database.....!");
    }

}

