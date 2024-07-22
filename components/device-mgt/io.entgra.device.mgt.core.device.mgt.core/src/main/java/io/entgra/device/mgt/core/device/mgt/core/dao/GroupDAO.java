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

package io.entgra.device.mgt.core.device.mgt.core.dao;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.GroupPaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroup;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroupRoleWrapper;
import io.entgra.device.mgt.core.device.mgt.core.dto.GroupDetailsDTO;

import java.util.List;
import java.util.Map;

/**
 * This interface represents the key operations associated with persisting group related information.
 */
public interface GroupDAO {
    /**
     * Add new Device Group.
     *
     * @param deviceGroup to be added.
     * @param tenantId    of the group.
     * @return sql execution result.
     * @throws GroupManagementDAOException
     */
    int addGroup(DeviceGroup deviceGroup, int tenantId) throws GroupManagementDAOException;

    /**
     * Add properties for device group.
     * Note that groupId parameter is considered seperately due to the groupId parameter passed with
     * device group Payload is ignored in the add/update logic instead the internal groupId reference is used.
     *
     * @param groups to be added.
     * @param tenantId    of the group.
     * @return sql execution result.
     * @throws GroupManagementDAOException
     */
    int addGroupWithRoles(DeviceGroupRoleWrapper groups, int tenantId) throws GroupManagementDAOException;

    /**
     * Add properties for device group.
     * Note that groupId parameter is considered seperately due to the groupId parameter passed with
     * device group Payload is ignored in the add/update logic instead the internal groupId reference is used.
     *
     * @param deviceGroup to be added.
     * @param tenantId    of the group.
     * @return sql execution result.
     * @throws GroupManagementDAOException
     */
    boolean addGroupProperties(DeviceGroup deviceGroup, int groupId, int tenantId) throws GroupManagementDAOException;

    /**
     * Update properties for device group.
     * Note that groupId parameter is considered seperately due to the groupId parameter passed with
     * device group Payload is ignored in the add/update logic instead the internal groupId reference is used.
     *
     * @param groups to be updated.
     * @param tenantId    of the group.
     * @return sql execution result.
     * @throws GroupManagementDAOException
     */
    boolean addGroupPropertiesWithRoles(DeviceGroupRoleWrapper groups, int groupId, int tenantId) throws GroupManagementDAOException;

    /**
     * Update properties for device group.
     * Note that groupId parameter is considered seperately due to the groupId parameter passed with
     * device group Payload is ignored in the add/update logic instead the internal groupId reference is used.
     *
     * @param deviceGroup to be updated.
     * @param tenantId    of the group.
     * @return sql execution result.
     * @throws GroupManagementDAOException
     */
    boolean updateGroupProperties(DeviceGroup deviceGroup, int groupId, int tenantId) throws GroupManagementDAOException;

    /**
     * Remove properties for device group.
     *
     * @param groupId to be deleted.
     * @param tenantId    of the group.
     * @throws GroupManagementDAOException
     */
    void deleteAllGroupProperties(int groupId, int tenantId) throws GroupManagementDAOException;

    /**
     * Remove properties of device groups.
     *
     * @param groupIds to be deleted.
     * @param tenantId of the group.
     * @throws GroupManagementDAOException on error during deletion of group properties of groups
     */
    void deleteAllGroupsProperties(List<Integer> groupIds, int tenantId) throws GroupManagementDAOException;

    /**
     * Retrives all properties stored against a group.
     *
     * @param groupId to be deleted.
     * @param tenantId    of the group.
     * @return sql execution result.
     * @throws GroupManagementDAOException
     */
    Map<String,String> getAllGroupProperties(int groupId, int tenantId) throws GroupManagementDAOException;

    /**
     * Update an existing Device Group.
     *
     * @param deviceGroup group to update.
     * @param groupId of Device Group.
     * @param tenantId of the group.
     * @throws GroupManagementDAOException
     */
    void updateGroup(DeviceGroup deviceGroup, int groupId, int tenantId)
            throws GroupManagementDAOException;

    /**
     * Update existing Device Groups.
     *
     * @param deviceGroups groups to update.
     * @param tenantId of the group.
     * @throws GroupManagementDAOException on error during updating of groups
     */
    void updateGroups(List<DeviceGroup> deviceGroups, int tenantId) throws GroupManagementDAOException;

    /**
     * Delete an existing Device Group.
     *
     * @param groupId of Device Group.
     * @param tenantId  of the group.
     * @throws GroupManagementDAOException
     */
    void deleteGroup(int groupId, int tenantId) throws GroupManagementDAOException;

    /**
     * Delete mappings of Device Groups.
     *
     * @param groupIds of Device Groups.
     * @param tenantId  of the group.
     * @throws GroupManagementDAOException on error during deletion of mappings of groups
     */
    void deleteGroupsMapping(List<Integer> groupIds, int tenantId) throws GroupManagementDAOException;

    /**
     * Delete mappings of Device Groups.
     *
     * @param role of Device Groups.
     * @param tenantId  of the role.
     * @throws GroupManagementDAOException on error during deletion of mappings of groups
     */
    void deleteGroupsMapping(String role, int tenantId) throws GroupManagementDAOException;

    /**
     * Delete existing Device Groups.
     *
     * @param groupIds of Device Groups.
     * @param tenantId  of the group.
     * @throws GroupManagementDAOException on error during deletion of groups
     */
    void deleteGroups(List<Integer> groupIds, int tenantId) throws GroupManagementDAOException;

    /**
     * Get device group by id.
     *
     * @param groupId of Device Group.
     * @param tenantId  of the group.
     * @return Device Group in tenant with specified name.
     * @throws GroupManagementDAOException
     */
    DeviceGroup getGroup(int groupId, int tenantId) throws GroupManagementDAOException;

    /**
     * Get children groups by parent path.
     *
     * @param parentPath of parent group.
     * @param tenantId  of the group.
     * @return {@link List<DeviceGroup>} list of children device groups
     * @throws GroupManagementDAOException on error during retrieval of children groups
     */
    List<DeviceGroup> getChildrenGroups(String parentPath, int tenantId) throws GroupManagementDAOException;

    /**
     * Get root groups.
     *
     * @param tenantId  of the group.
     * @return {@link List<DeviceGroup>} list of root device groups
     * @throws GroupManagementDAOException on error during retrieval of root groups
     */
    List<DeviceGroup> getRootGroups(int tenantId) throws GroupManagementDAOException;

    /**
     * Get the groups of device with device id provided
     * @param deviceId
     * @return groups which has the device.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getGroups(int deviceId, int tenantId) throws GroupManagementDAOException;

    /**
     * Get paginated list of Device Groups in tenant.
     *
     * @param paginationRequest to filter results.
     * @param tenantId of user's tenant.
     * @return List of all Device Groups in tenant.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getGroups(GroupPaginationRequest paginationRequest, int tenantId) throws GroupManagementDAOException;

    /**
     * Get paginated list of Device Groups in tenant with specified device group ids.
     *
     * @param paginationRequest to filter results.
     * @param deviceGroupIds    of groups required.
     * @param tenantId          of user's tenant.
     * @param isWithParentPath      of user's ParentPath.
     * @return List of all Device Groups in tenant.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getGroups(GroupPaginationRequest paginationRequest, List<Integer> deviceGroupIds,
                                int tenantId, boolean isWithParentPath) throws GroupManagementDAOException;

    /**
     * Get paginated list of Device Groups in tenant with specified device group ids.
     *
     * @param paginationRequest to filter results.
     * @param deviceGroupIds    of groups required.
     * @param tenantId          of user's tenant.
     * @return List of all Device Groups in tenant.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getGroups(GroupPaginationRequest paginationRequest, List<Integer> deviceGroupIds,
                                int tenantId) throws GroupManagementDAOException;

    /**
     * Get the list of Device Groups in tenant.
     *
     * @param tenantId of user's tenant.
     * @return List of all Device Groups in tenant.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getGroups(List<Integer> deviceGroupIds, int tenantId) throws GroupManagementDAOException;

    /**
     * Get the list of Device Groups in tenant.
     *
     * @param tenantId of user's tenant.
     * @return List of all Device Groups in tenant.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getGroups(int tenantId) throws GroupManagementDAOException;

    /**
     * Get count of Device Groups in tenant.
     *
     * @param tenantId of user's tenant.
     * @return List of all Device Groups in tenant.
     * @throws GroupManagementDAOException
     */
    int getGroupCount(int tenantId, String status) throws GroupManagementDAOException;

    /**
     * Get paginated count of Device Groups in tenant.
     *
     * @param paginationRequest to filter results.
     * @param tenantId of user's tenant.
     * @return List of all Device Groups in tenant.
     * @throws GroupManagementDAOException
     */
    int getGroupCount(GroupPaginationRequest paginationRequest, int tenantId) throws GroupManagementDAOException;

    /**
     * Check group already existed with given name.
     *
     * @param groupName of the Device Group.
     * @param tenantId of user's tenant.
     * @return existence of group with name
     * @throws GroupManagementDAOException
     */
    DeviceGroup getGroup(String groupName, int tenantId) throws GroupManagementDAOException;

    /**
     * Add device to a given Device Group.
     *
     * @param groupId of Device Group.
     * @param deviceId of the device.
     * @param tenantId of user's tenant.
     * @throws GroupManagementDAOException
     */
    void addDevice(int groupId, int deviceId, int tenantId) throws GroupManagementDAOException;

    /**
     * Remove device from the Device Group.
     *
     * @param groupId of Device Group.
     * @param deviceId of the device.
     * @param tenantId of user's tenant.
     * @throws GroupManagementDAOException
     */
    void removeDevice(int groupId, int deviceId, int tenantId) throws GroupManagementDAOException;

    /**
     * Check device is belonging to a Device Group.
     *
     * @param groupId of Device Group.
     * @param deviceId of the device.
     * @param tenantId of user's tenant.
     * @throws GroupManagementDAOException
     */
    boolean isDeviceMappedToGroup(int groupId, int deviceId, int tenantId)
            throws GroupManagementDAOException;

    /**
     * Get count of devices in a Device Group.
     *
     * @param groupId of Device Group.
     * @param tenantId of user's tenant.
     * @return device count.
     * @throws GroupManagementDAOException
     */
    int getDeviceCount(int groupId, int tenantId) throws GroupManagementDAOException;

    /**
     * Get paginated result of devices of a given tenant and device group.
     *
     * @param groupId of Device Group.
     * @param startIndex for pagination.
     * @param rowCount for pagination.
     * @param tenantId of user's tenant.
     * @return list of device in group
     * @throws GroupManagementDAOException
     */
    List<Device> getDevices(int groupId, int startIndex, int rowCount, int tenantId)
            throws GroupManagementDAOException;

    /**
     * Get All the devices that are in one of the given device status and belongs to given group
     *
     * @param groupName Group name
     * @param deviceStatuses Device Statuses
     * @param tenantId Tenant Id
     * @return List of devices
     * @throws GroupManagementDAOException if error occurred while retreving list of devices that are in one of the
     *                                     given device status and belongs to the given group
     */
    List<Device> getAllDevicesOfGroup(String groupName, List<String> deviceStatuses, int tenantId)
            throws GroupManagementDAOException;

    List<Device> getAllDevicesOfGroup(String groupName, int tenantId) throws GroupManagementDAOException;

    /**
     * Get all user roles for device group.
     *
     * @param groupId  of the group
     * @param tenantId of user's tenant.
     * @return list of roles
     * @throws GroupManagementDAOException
     */
    List<String> getRoles(int groupId, int tenantId) throws GroupManagementDAOException;

    /**
     * Add user role for device group.
     *
     * @param groupId  of the group.
     * @param role     to be added.
     * @param tenantId of user's tenant.
     * @throws GroupManagementDAOException
     */
    void addRole(int groupId, String role, int tenantId) throws GroupManagementDAOException;

    /**
     * Add user role for device group.
     *
     * @param groupId  of the group.
     * @param role     to be removed.
     * @param tenantId of user's tenant.
     * @throws GroupManagementDAOException
     */
    void removeRole(int groupId, String role, int tenantId) throws GroupManagementDAOException;

    /**
     * Get all device groups which shared with a user role.
     *
     * @param roles    of the group.
     * @param tenantId of user's tenant.
     * @return list of device groups.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getGroups(String[] roles, int tenantId) throws GroupManagementDAOException;

    /**
     * Get all device group ids which shared with a user role.
     *
     * @param roles    of the group.
     * @param tenantId of user's tenant.
     * @return list of device group ids.
     * @throws GroupManagementDAOException
     */
    List<Integer> getGroupIds(String[] roles, int tenantId) throws GroupManagementDAOException;

    /**
     * Get count of all device groups which shared with a user role.
     *
     * @param roles    of the group.
     * @param tenantId of user's tenant.
     * @param parentPath of the group.
     * @return count of device groups.
     * @throws GroupManagementDAOException
     */
    int getGroupsCount(String[] roles, int tenantId, String parentPath) throws GroupManagementDAOException;

    /**
     * Get all device groups which owned by user.
     *
     * @param username of the owner.
     * @param tenantId of user's tenant.
     * @return list of device groups.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getOwnGroups(String username, int tenantId) throws GroupManagementDAOException;

    /**
     * Get all device group ids which owned by user.
     *
     * @param username of the owner.
     * @param tenantId of user's tenant.
     * @return list of device group ids.
     * @throws GroupManagementDAOException
     */
    List<Integer> getOwnGroupIds(String username, int tenantId) throws GroupManagementDAOException;

    /**
     * Get count of device groups which owned by user.
     *
     * @param username of the owner.
     * @param tenantId of user's tenant.
     * @param parentPath of the group.
     * @return count of device groups.
     * @throws GroupManagementDAOException
     */
    int getOwnGroupsCount(String username, int tenantId, String parentPath) throws GroupManagementDAOException;

    /**
     * Get device Ids of devices which are assigned to groups.
     *
     * @param paginationRequest Request object with offset and limit.
     * @param groupNames default group names that should be omitted when checking the device
     *                   whether they have been assigned to groups
     * @throws GroupManagementDAOException Might occur while retrieving information of group
     * unassigned devices
     * @return details of devices that are unassigned to groups.
     */

    List<Device> getGroupUnassignedDevices(PaginationRequest paginationRequest,
                                           List<String> groupNames)
            throws GroupManagementDAOException;

    /**
     * Get group details and list of device IDs related to the group.
     *
     * @param groupName Group name
     * @param allowingDeviceStatuses the statuses of devices
     * @param deviceTypeId the device type id
     * @param tenantId Tenant ID
     * @param deviceOwner owner of the device
     * @param deviceName name of the device
     * @param deviceStatus status of the device
     * @param offset the offset for the data set
     * @param limit the limit for the data set
     * @return {@link GroupDetailsDTO} which containing group details and a list of device IDs
     * @throws GroupManagementDAOException if an error occurs while retrieving the group details and devices
     */
    GroupDetailsDTO getGroupDetailsWithDevices(String groupName, List<String> allowingDeviceStatuses, int deviceTypeId,
                                               int tenantId, String deviceOwner, String deviceName, String deviceStatus, int offset, int limit)
            throws GroupManagementDAOException;

    int getDeviceCount(String groupName, int tenantId) throws GroupManagementDAOException;
}