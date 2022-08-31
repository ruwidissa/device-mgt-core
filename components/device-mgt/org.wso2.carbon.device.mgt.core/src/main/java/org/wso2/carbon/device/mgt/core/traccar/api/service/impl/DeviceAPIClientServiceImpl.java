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
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.TrackerDeviceInfo;
import org.wso2.carbon.device.mgt.common.TrackerPermissionInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.exceptions.TrackerAlreadyExistException;
import org.wso2.carbon.device.mgt.core.dao.TrackerManagementDAOException;
import org.wso2.carbon.device.mgt.core.traccar.api.service.DeviceAPIClientService;
import org.wso2.carbon.device.mgt.core.traccar.api.service.addons.TraccarClientImpl;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarDevice;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarGroups;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarPosition;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarUser;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DeviceAPIClientServiceImpl implements DeviceAPIClientService {

    private static final Log log = LogFactory.getLog(DeviceAPIClientServiceImpl.class);

    public void addDevice(Device device, int tenantId) throws ExecutionException, InterruptedException {
        TraccarClientImpl client = new TraccarClientImpl();
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

    public void updateLocation(Device device, DeviceLocation deviceLocation, int tenantId) throws ExecutionException, InterruptedException {
        TraccarClientImpl client = new TraccarClientImpl();
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

    public void disEnrollDevice(int deviceId, int tenantId) throws ExecutionException, InterruptedException{
        TraccarClientImpl client = new TraccarClientImpl();
        try {
            client.disEnrollDevice(deviceId, tenantId);
        } catch (TrackerManagementDAOException e) {
            String msg = "Error occurred while dis-enrolling the device";
            log.error(msg, e);
        }
    }

    public void addGroup(DeviceGroup group, int groupId, int tenantId) throws
            TrackerManagementDAOException, TrackerAlreadyExistException, ExecutionException, InterruptedException {
        TraccarClientImpl client = new TraccarClientImpl();
        TraccarGroups traccarGroups = new TraccarGroups(group.getName());
        client.addGroup(traccarGroups, groupId, tenantId);
    }

    public void updateGroup(DeviceGroup group, int groupId, int tenantId) throws
            TrackerManagementDAOException, TrackerAlreadyExistException, ExecutionException, InterruptedException {
        TraccarClientImpl client = new TraccarClientImpl();
        TraccarGroups traccarGroups = new TraccarGroups(group.getName());
        client.updateGroup(traccarGroups, groupId, tenantId);
    }

    public void deleteGroup(int groupId, int tenantId) throws
            TrackerManagementDAOException, ExecutionException, InterruptedException {
        TraccarClientImpl client = new TraccarClientImpl();
        client.deleteGroup(groupId, tenantId);
    }

    public static String fetchUserInfo(String userName) throws ExecutionException, InterruptedException {
        TraccarClientImpl client = new TraccarClientImpl();
        return client.fetchUserInfo(userName);
    }

    public static TrackerDeviceInfo getTrackerDevice(int deviceId, int tenantId) throws
            TrackerManagementDAOException {
        TraccarClientImpl client = new TraccarClientImpl();
        return client.getTrackerDevice(deviceId, tenantId);
    }

    public static boolean getUserIdofPermissionByDeviceIdNUserId(int deviceId, int userId) throws
            TrackerManagementDAOException {
        TraccarClientImpl client = new TraccarClientImpl();
        return client.getUserIdofPermissionByDeviceIdNUserId(deviceId, userId);
    }

    public static String createUser(TraccarUser traccarUser) throws ExecutionException, InterruptedException {
        TraccarClientImpl client = new TraccarClientImpl();
        return client.createUser(traccarUser);
    }

    public static String updateUser(TraccarUser traccarUser, int userId) throws
            ExecutionException, InterruptedException {
        TraccarClientImpl client = new TraccarClientImpl();
        return client.updateUser(traccarUser, userId);
    }

    public static String returnUser(String userName) {
        TraccarClientImpl client = new TraccarClientImpl();
        try {
            return client.returnUser(userName);
        } catch (TrackerManagementDAOException e) {
            JSONObject obj = new JSONObject();
            String msg = "Error occurred while creating a user: "+ e;
            obj.put("error", msg);
            return obj.toString();
        }
    }

    public static void addTrackerUserDevicePermission(int userId, int deviceId) throws
            TrackerManagementDAOException, ExecutionException, InterruptedException {
        TraccarClientImpl client = new TraccarClientImpl();
        client.setPermission(userId, deviceId);
    }

    public static void removeTrackerUserDevicePermission(int userId, int deviceId, int removeType) throws
            TrackerManagementDAOException, ExecutionException, InterruptedException {
        TraccarClientImpl client = new TraccarClientImpl();
        client.removePermission(userId, deviceId, removeType);
    }

    public static List<TrackerPermissionInfo> getUserIdofPermissionByUserIdNIdList(int userId, List<Integer> NotInDeviceIdList) throws
            TrackerManagementDAOException {
        TraccarClientImpl client = new TraccarClientImpl();
        return client.getUserIdofPermissionByUserIdNIdList(userId, NotInDeviceIdList);
    }

    public static String generateRandomString(int len) {
        TraccarClientImpl client = new TraccarClientImpl();
        return client.generateRandomString(len);
    }

}
