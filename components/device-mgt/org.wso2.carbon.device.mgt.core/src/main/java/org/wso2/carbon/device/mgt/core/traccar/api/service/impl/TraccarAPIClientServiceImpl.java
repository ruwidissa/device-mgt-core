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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.TrackerAlreadyExistException;
import org.wso2.carbon.device.mgt.core.traccar.api.service.DeviceAPIClientService;
import org.wso2.carbon.device.mgt.core.traccar.api.service.addons.TrackerClient;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarDevice;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarGroups;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarPosition;
import org.wso2.carbon.device.mgt.core.traccar.common.config.TraccarConfigurationException;

import java.util.Date;

public class TraccarAPIClientServiceImpl implements DeviceAPIClientService {

    private static final Log log = LogFactory.getLog(TraccarAPIClientServiceImpl.class);

    public void addDevice(Device device, int tenantId) {
        TrackerClient client = new TrackerClient();
        String lastUpdatedTime = String.valueOf((new Date().getTime()));
        TraccarDevice traccarDevice = new TraccarDevice(device.getId(), device.getName(), device.getDeviceIdentifier(),
                "online", "false", lastUpdatedTime, "", "", "", "",
                "", "");
        try {
            client.addDevice(traccarDevice, tenantId);
        } catch (TraccarConfigurationException e) {
            String msg = "Error occurred while mapping with deviceId";
            log.error(msg, e);
        } catch (TrackerAlreadyExistException e) {
            String msg = "The device already exist";
            log.error(msg, e);
        }
    }

    public void updateDevice(Device device, int tenantId) {
        TrackerClient client = new TrackerClient();
        String lastUpdatedTime = String.valueOf((new Date().getTime()));
        TraccarDevice traccarDeviceInfo = new TraccarDevice(device.getId(), device.getName(), device.getDeviceIdentifier(),
                "online", "false", lastUpdatedTime, "", "", "", "",
                "", "");
        try {
            client.updateDevice(traccarDeviceInfo, tenantId);
        } catch (TraccarConfigurationException e) {
            String msg = "Error occurred while mapping with deviceId";
            log.error(msg, e);
        } catch (TrackerAlreadyExistException e) {
            String msg = "The device already exist";
            log.error(msg, e);
        }
    }

    public void updateLocation(Device device, DeviceLocation deviceLocation, int tenantId) {
        TrackerClient client = new TrackerClient();
        TraccarPosition traccarPosition = new TraccarPosition(device.getDeviceIdentifier(),
                deviceLocation.getUpdatedTime().getTime(),
                deviceLocation.getLatitude(), deviceLocation.getLongitude(),
                deviceLocation.getBearing(), deviceLocation.getSpeed());

        String lastUpdatedTime = String.valueOf((new Date().getTime()));
        TraccarDevice traccarDevice = new TraccarDevice(device.getId(), device.getName(), device.getDeviceIdentifier(),
                "online", "false", lastUpdatedTime, "", "", "", "",
                "", "");
        try {
            client.updateLocation(traccarDevice, traccarPosition, tenantId);
        } catch (TraccarConfigurationException e) {
            String msg = "Error occurred while mapping with deviceId";
            log.error(msg, e);
        }catch (TrackerAlreadyExistException e) {
            String msg = "The device already exist";
            log.error(msg, e);
        }
    }

    public void disEndrollDevice(int deviceId, int tenantId) {
        TrackerClient client = new TrackerClient();
        try {
            client.disEndrollDevice(deviceId, tenantId);
        } catch (TraccarConfigurationException e) {
            String msg = "Error occurred while mapping with deviceId";
            log.error(msg, e);
        }
    }

    public void addGroup(DeviceGroup group, int groupId, int tenantId) {
        TrackerClient client = new TrackerClient();
        TraccarGroups traccarGroups = new TraccarGroups(group.getName());
        try {
            client.addGroup(traccarGroups, groupId, tenantId);
        } catch (TraccarConfigurationException e) {
            String msg = "Error occurred while mapping with groupId";
            log.error(msg, e);
        } catch (TrackerAlreadyExistException e) {
            String msg = "The group already exist";
            log.error(msg, e);
        }
    }

    public void updateGroup(DeviceGroup group, int groupId, int tenantId) {
        TrackerClient client = new TrackerClient();
        TraccarGroups traccarGroups = new TraccarGroups(group.getName());
        try {
            client.updateGroup(traccarGroups, groupId, tenantId);
        } catch (TraccarConfigurationException e) {
            String msg = "Error occurred while mapping with groupId";
            log.error(msg, e);
        } catch (TrackerAlreadyExistException e) {
            String msg = "The group already exist";
            log.error(msg, e);
        }
    }

    public void deleteGroup(int groupId, int tenantId) {
        TrackerClient client = new TrackerClient();
        try {
            client.deleteGroup(groupId, tenantId);
        } catch (TraccarConfigurationException e) {
            String msg = "Error occurred while mapping with groupId";
            log.error(msg, e);
        }
    }

}
