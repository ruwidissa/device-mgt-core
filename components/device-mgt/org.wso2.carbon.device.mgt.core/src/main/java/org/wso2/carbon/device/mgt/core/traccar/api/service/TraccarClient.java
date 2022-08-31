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

package org.wso2.carbon.device.mgt.core.traccar.api.service;

import org.wso2.carbon.device.mgt.common.TrackerDeviceInfo;
import org.wso2.carbon.device.mgt.common.TrackerPermissionInfo;
import org.wso2.carbon.device.mgt.common.exceptions.TrackerAlreadyExistException;
import org.wso2.carbon.device.mgt.core.dao.TrackerManagementDAOException;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarDevice;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarGroups;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarPosition;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarUser;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface TraccarClient {

    void addDevice(TraccarDevice deviceInfo, int tenantId) throws
            TrackerManagementDAOException, TrackerAlreadyExistException, ExecutionException, InterruptedException;

    void updateLocation(TraccarDevice device, TraccarPosition deviceInfo, int tenantId) throws
            TrackerManagementDAOException, TrackerAlreadyExistException, ExecutionException, InterruptedException;

    void disEnrollDevice(int traccarDeviceId, int tenantId) throws
            TrackerManagementDAOException, ExecutionException, InterruptedException;

    void addGroup(TraccarGroups groupInfo, int groupId, int tenantId) throws
            TrackerManagementDAOException, TrackerAlreadyExistException, ExecutionException, InterruptedException;

    void updateGroup(TraccarGroups groupInfo, int groupId, int tenantId) throws
            TrackerManagementDAOException, TrackerAlreadyExistException, ExecutionException, InterruptedException;

    void deleteGroup(int traccarGroupId, int tenantId) throws
            TrackerManagementDAOException, ExecutionException, InterruptedException;

    void setPermission(int userId, int deviceId) throws
            TrackerManagementDAOException, ExecutionException, InterruptedException;

    void removePermission(int userId, int deviceId, int removeType) throws
            TrackerManagementDAOException, ExecutionException, InterruptedException;

    List<TrackerPermissionInfo> getUserIdofPermissionByUserIdNIdList(int userId, List<Integer> NotInDeviceIdList)
            throws TrackerManagementDAOException;

    boolean getUserIdofPermissionByDeviceIdNUserId(int deviceId, int userId) throws
            TrackerManagementDAOException, SQLException;

    TrackerDeviceInfo getTrackerDevice(int deviceId, int tenantId) throws
            TrackerManagementDAOException, SQLException;

    String fetchAllUsers() throws TrackerManagementDAOException, ExecutionException, InterruptedException;

    String fetchUserInfo(String userName) throws TrackerManagementDAOException, ExecutionException, InterruptedException;

    String createUser(TraccarUser traccarUser) throws ExecutionException, InterruptedException;

    String updateUser(TraccarUser traccarUser, int userId) throws ExecutionException, InterruptedException;

    String returnUser(String userName) throws TrackerManagementDAOException;
}
