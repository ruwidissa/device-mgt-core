/*
 * Copyright (C) 2018 - 2022 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.traccar.api.service.impl;

import org.wso2.carbon.device.mgt.common.event.config.EventMetaData;
import org.wso2.carbon.device.mgt.core.event.config.EventOperationExecutor;
import org.wso2.carbon.device.mgt.core.geo.task.GeoFenceEventOperationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.traccar.api.service.DeviceAPIClientService;
import org.wso2.carbon.device.mgt.core.traccar.api.service.addons.TrackerClient;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarDevice;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarGroups;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarPosition;
import org.wso2.carbon.device.mgt.core.traccar.common.config.TraccarConfigurationException;

import java.io.IOException;
import java.util.List;

public class DeviceAPIClientServiceImpl implements DeviceAPIClientService {


    public void addDevice(TraccarDevice deviceInfo) throws TraccarConfigurationException {
        TrackerClient client = new TrackerClient();
        client.addDevice(deviceInfo);
    }

    public void updateLocation(TraccarPosition deviceInfo) throws TraccarConfigurationException {
        TrackerClient client = new TrackerClient();
        client.updateLocation(deviceInfo);
    }

    public void disDevice(TraccarDevice deviceInfo) throws TraccarConfigurationException {
        TrackerClient client = new TrackerClient();
        client.disDevice(deviceInfo);
    }

    /*public String addGroup(TraccarGroups groupInfo) throws IOException {
        TrackerClient client = new TrackerClient();
        return (client.addGroup(groupInfo));
    }*/

}
