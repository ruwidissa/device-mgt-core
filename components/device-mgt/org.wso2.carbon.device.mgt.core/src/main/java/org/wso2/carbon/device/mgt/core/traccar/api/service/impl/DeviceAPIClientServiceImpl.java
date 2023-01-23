/*
 * Copyright (C) 2022 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
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
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.TrackerDeviceInfo;
import org.wso2.carbon.device.mgt.common.TrackerPermissionInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.exceptions.TrackerAlreadyExistException;
import org.wso2.carbon.device.mgt.core.dao.TrackerManagementDAOException;
import org.wso2.carbon.device.mgt.core.traccar.api.service.DeviceAPIClientService;
import org.wso2.carbon.device.mgt.core.traccar.api.service.TraccarClientFactory;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarDevice;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarGroups;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarPosition;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarUser;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DeviceAPIClientServiceImpl implements DeviceAPIClientService {

    private static final Log log = LogFactory.getLog(DeviceAPIClientServiceImpl.class);
    TraccarClientFactory client = TraccarClientFactory.getInstance();

    @Override
    public void addDevice(Device device, int tenantId) throws ExecutionException, InterruptedException {
        String lastUpdatedTime = String.valueOf((new Date().getTime()));
        TraccarDevice traccarDevice = new TraccarDevice(device.getId(), device.getName(), device.getDeviceIdentifier(),
                "online", "false", lastUpdatedTime, "", "", "", "",
                "", "");
        try {
            client.addDevice(traccarDevice, tenantId);
        } catch (TrackerManagementDAOException e) {
            String msg = "Error occurred while mapping with deviceId";
            log.error(msg, e);
        } catch (TrackerAlreadyExistException e) {
            String msg = "The device already exist";
            log.error(msg, e);
        }
    }

    @Override
    public void modifyDevice(Device device, int tenantId) throws ExecutionException, InterruptedException {
        TraccarDevice traccarDevice = new TraccarDevice(device.getId(), device.getDeviceIdentifier(), device.getName());
        try {
            client.modifyDevice(traccarDevice, tenantId);
        } catch (TrackerManagementDAOException e) {
            String msg = "Error occurred while mapping with deviceId";
            log.error(msg, e);
        }
    }

    @Override
    public void updateLocation(Device device, DeviceLocation deviceLocation, int tenantId) throws ExecutionException, InterruptedException {
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
        } catch (TrackerManagementDAOException e) {
            String msg = "Error occurred while mapping with deviceId";
            log.error(msg, e);
        }catch (TrackerAlreadyExistException e) {
            String msg = "The device already exist";
            log.error(msg, e);
        }
    }

    @Override
    public void disEnrollDevice(int deviceId, int tenantId) throws ExecutionException, InterruptedException{
        try {
            client.disEnrollDevice(deviceId, tenantId);
        } catch (TrackerManagementDAOException e) {
            String msg = "Error occurred while dis-enrolling the device";
            log.error(msg, e);
        }
    }

    @Override
    public void addGroup(DeviceGroup group, int groupId, int tenantId) throws
            TrackerManagementDAOException, TrackerAlreadyExistException, ExecutionException, InterruptedException {
        TraccarGroups traccarGroups = new TraccarGroups(group.getName());
        client.addGroup(traccarGroups, groupId, tenantId);
    }

    @Override
    public void updateGroup(DeviceGroup group, int groupId, int tenantId) throws
            TrackerManagementDAOException, TrackerAlreadyExistException, ExecutionException, InterruptedException {
        TraccarGroups traccarGroups = new TraccarGroups(group.getName());
        client.updateGroup(traccarGroups, groupId, tenantId);
    }

    @Override
    public void deleteGroup(int groupId, int tenantId) throws
            TrackerManagementDAOException, ExecutionException, InterruptedException {
        client.deleteGroup(groupId, tenantId);
    }

    @Override
    public String returnUser(String username) {
        TraccarClientFactory client = TraccarClientFactory.getInstance();
        try {
            return client.returnUser(username);
        } catch (TrackerManagementDAOException e) {
            JSONObject obj = new JSONObject();
            String msg = "Error occurred while creating a user: "+ e;
            obj.put("error", msg);
            return obj.toString();
        }
    }

    @Override
    public TrackerDeviceInfo getTrackerDevice(int deviceId, int tenantId) throws
            TrackerManagementDAOException {
        TraccarClientFactory client = TraccarClientFactory.getInstance();
        return client.getTrackerDevice(deviceId, tenantId);
    }

    @Override
    public boolean getUserIdofPermissionByDeviceIdNUserId(int deviceId, int userId) throws
            TrackerManagementDAOException {
        TraccarClientFactory client = TraccarClientFactory.getInstance();
        return client.getUserIdofPermissionByDeviceIdNUserId(deviceId, userId);
    }

    @Override
    public void addTrackerUserDevicePermission(int userId, int deviceId) throws
            TrackerManagementDAOException, ExecutionException, InterruptedException {
        TraccarClientFactory client = TraccarClientFactory.getInstance();
        client.setPermission(userId, deviceId);
    }

    @Override
    public List<TrackerPermissionInfo> getUserIdofPermissionByUserIdNIdList(int userId, List<Integer> NotInDeviceIdList) throws
            TrackerManagementDAOException {
        TraccarClientFactory client = TraccarClientFactory.getInstance();
        return client.getUserIdofPermissionByUserIdNIdList(userId, NotInDeviceIdList);
    }

    @Override
    public void removeTrackerUserDevicePermission(int userId, int deviceId, int removeType) throws
            TrackerManagementDAOException, ExecutionException, InterruptedException {
        TraccarClientFactory client = TraccarClientFactory.getInstance();
        client.removePermission(userId, deviceId, removeType);
    }

    public static String fetchUserInfo(String userName) throws ExecutionException, InterruptedException {
        TraccarClientFactory client = TraccarClientFactory.getInstance();
        return client.fetchUserInfo(userName);
    }

    public static String createUser(TraccarUser traccarUser) throws ExecutionException, InterruptedException {
        TraccarClientFactory client = TraccarClientFactory.getInstance();
        return client.createUser(traccarUser);
    }

    public static String updateUser(TraccarUser traccarUser, int userId) throws
            ExecutionException, InterruptedException {
        TraccarClientFactory client = TraccarClientFactory.getInstance();
        return client.updateUser(traccarUser, userId);
    }

    public static String generateRandomString(int len) {
        TraccarClientFactory client = TraccarClientFactory.getInstance();
        return client.generateRandomString(len);
    }

}
