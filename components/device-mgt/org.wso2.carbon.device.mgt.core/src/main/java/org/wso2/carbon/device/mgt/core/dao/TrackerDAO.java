/*
 *   Copyright (c) 2022, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *   Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.device.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.TrackerDeviceInfo;
import org.wso2.carbon.device.mgt.common.TrackerGroupInfo;
import org.wso2.carbon.device.mgt.common.TrackerPermissionInfo;

import java.util.List;

public interface TrackerDAO {

    /**
     * Add new Device.
     * @param traccarDeviceId to be added.
     * @param deviceId of the device.
     * @param tenantId of the group.
     * @return boolean value.
     * @throws TrackerManagementDAOException
     */
    void addTrackerDevice(int traccarDeviceId, int deviceId, int tenantId) throws TrackerManagementDAOException;

    /**
     * get trackerDevice info.
     * @param deviceId of the device.
     * @param tenantId of the group.
     * @return Tracker Device Info.
     * @throws TrackerManagementDAOException
     */
    TrackerDeviceInfo getTrackerDevice(int deviceId, int tenantId) throws TrackerManagementDAOException;

    /**
     * update trackerDevice status and traccarDeviceId.
     * @param traccarDeviceId of the Device.
     * @param deviceId of the device.
     * @param tenantId of the group.
     * @param status of the device.
     * @throws TrackerManagementDAOException
     */
    void updateTrackerDeviceIdANDStatus(int traccarDeviceId, int deviceId, int tenantId, int status) throws TrackerManagementDAOException;

    /**
     * Remove a Device.
     * @param deviceId of the device.
     * @param tenantId of the group.
     * @return sql execution result.
     * @throws TrackerManagementDAOException
     */
    void removeTrackerDevice(int deviceId, int tenantId) throws TrackerManagementDAOException;

    /**
     * Add new Group.
     * @param traccarGroupId to be added.
     * @param groupId of the group.
     * @param tenantId of the group.
     * @throws TrackerManagementDAOException
     */
    void addTrackerGroup(int traccarGroupId, int groupId, int tenantId) throws TrackerManagementDAOException;

    /**
     * Update status and traccarGroupId of a Group.
     * @param traccarGroupId to be added.
     * @param groupId of the group.
     * @param tenantId of the group.
     * @param status of the group.
     * @return boolean value.
     * @throws TrackerManagementDAOException
     */
    Boolean updateTrackerGroupIdANDStatus(int traccarGroupId, int groupId, int tenantId, int status) throws TrackerManagementDAOException;

    /**
     * Remove a Group.
     * @param id of groups.
     * @return boolean value.
     * @throws TrackerManagementDAOException
     */
    int removeTrackerGroup(int id) throws TrackerManagementDAOException;

    /**
     * get trackerGroup info.
     * @param groupId of the device.
     * @param tenantId of the group.
     * @return Tracker Device Info.
     * @throws TrackerManagementDAOException
     */
    TrackerGroupInfo getTrackerGroup(int groupId, int tenantId) throws TrackerManagementDAOException;

    /**
     * give permission to a user to view traccar device.
     * @param traccarUserId mapping table.
     * @param deviceId mapping table.
     * @return boolean value.
     * @throws TrackerManagementDAOException
     */
    Boolean addTrackerUserDevicePermission(int traccarUserId, int deviceId) throws TrackerManagementDAOException;

    /**
     * Remove a permission on viewing a device.
     * @param deviceId of permissions.
     * @return boolean value.
     * @throws TrackerManagementDAOException
     */
    Boolean removeTrackerUserDevicePermission(int deviceId, int userId, int removeType) throws TrackerManagementDAOException;

    /**
     * get list of userDevicePermissions by.
     * @param deviceId of permissions.
     * @return Tracker Permission Info.
     * @throws TrackerManagementDAOException
     */
    List<TrackerPermissionInfo> getUserIdofPermissionByDeviceId(int deviceId) throws TrackerManagementDAOException;
    /**
     * get list of userDevicePermissions by.
     * @param userId of permissions.
     * @param NotInDeviceIdList list of permissions.
     * @return Tracker Permission Info.
     * @throws TrackerManagementDAOException
     */
    List<TrackerPermissionInfo> getUserIdofPermissionByUserIdNIdList(int userId, List<Integer> NotInDeviceIdList) throws TrackerManagementDAOException;

    /**
     * get list of userDevicePermissions by.
     * @param deviceId of permissions.
     * @param userId of permissions.
     * @return Tracker Permission Info.
     * @throws TrackerManagementDAOException
     */
    Boolean getUserIdofPermissionByDeviceIdNUserId(int deviceId, int userId) throws TrackerManagementDAOException;

}
