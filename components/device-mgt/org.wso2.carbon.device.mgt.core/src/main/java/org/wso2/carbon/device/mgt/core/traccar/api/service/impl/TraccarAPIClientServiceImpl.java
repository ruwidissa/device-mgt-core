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

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.core.traccar.api.service.DeviceAPIClientService;
import org.wso2.carbon.device.mgt.core.traccar.api.service.addons.TrackerClient;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarDevice;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarGroups;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarPosition;
import org.wso2.carbon.device.mgt.core.traccar.common.config.TraccarConfigurationException;

import java.util.Date;

public class TraccarAPIClientServiceImpl implements DeviceAPIClientService {

    public void addDevice(Device device, int tenantId) throws TraccarConfigurationException {
        TrackerClient client = new TrackerClient();
        String lastUpdatedTime = String.valueOf((new Date().getTime()));
        TraccarDevice traccarDeviceInfo = new TraccarDevice(device.getId(), device.getName(), device.getDeviceIdentifier(),
                "online", "false", lastUpdatedTime, "", "", "", "",
                "", "");
        client.addDevice(traccarDeviceInfo, tenantId);
    }

    public void updateDevice(Device device, int tenantId) throws TraccarConfigurationException {
        TrackerClient client = new TrackerClient();
        String lastUpdatedTime = String.valueOf((new Date().getTime()));
        TraccarDevice traccarDeviceInfo = new TraccarDevice(device.getId(), device.getName(), device.getDeviceIdentifier(),
                "online", "false", lastUpdatedTime, "", "", "", "",
                "", "");
        client.updateDevice(traccarDeviceInfo, tenantId);
    }

    public void updateLocation(Device device, DeviceLocation deviceLocation) throws TraccarConfigurationException {
        TrackerClient client = new TrackerClient();
        TraccarPosition traccarPosition = new TraccarPosition(device.getDeviceIdentifier(),
                deviceLocation.getUpdatedTime().getTime(),
                deviceLocation.getLatitude(), deviceLocation.getLongitude(),
                deviceLocation.getBearing(), deviceLocation.getSpeed());
        client.updateLocation(traccarPosition);
    }

    public void disEndrollDevice(int deviceId, int tenantId) throws TraccarConfigurationException {
        TrackerClient client = new TrackerClient();
        client.disEndrollDevice(deviceId, tenantId);
    }

    public void addGroup(DeviceGroup group, int groupId, int tenantId) throws TraccarConfigurationException {
        TrackerClient client = new TrackerClient();
        TraccarGroups traccarGroups = new TraccarGroups(group.getName());
        client.addGroup(traccarGroups, groupId, tenantId);
    }

    public void updateGroup(DeviceGroup group, int traccarGroupId, int groupId, int tenantId) throws TraccarConfigurationException {
        TrackerClient client = new TrackerClient();
        TraccarGroups traccarGroups = new TraccarGroups(group.getName());
        client.updateGroup(traccarGroups, traccarGroupId, groupId, tenantId);
    }

    public void deleteGroup(int traccarGroupId, int tenantId) throws TraccarConfigurationException {
        TrackerClient client = new TrackerClient();
        client.deleteGroup(traccarGroupId, tenantId);
    }

}
