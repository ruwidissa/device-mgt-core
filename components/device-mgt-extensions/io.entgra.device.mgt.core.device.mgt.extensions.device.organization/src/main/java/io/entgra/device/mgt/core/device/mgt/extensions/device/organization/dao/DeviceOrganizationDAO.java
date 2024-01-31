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
package io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao;

import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.DeviceNodeResult;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.DeviceOrganization;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.exception.DeviceOrganizationMgtDAOException;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.AdditionResult;

import java.util.List;

/**
 * This is responsible for DeviceOrganization related DAO operations.
 */
public interface DeviceOrganizationDAO {

    /**
     * Retrieves child devices per particular device ID
     *
     * @param deviceId      The device ID for which child devices are retrieved.
     * @param maxDepth      The maximum depth to traverse when fetching child devices.
     * @param includeDevice Flag to indicate whether to include the parent device in the result.
     * @param tenantID The ID of the tenant.
     * @return A list of child device nodes.
     * @throws DeviceOrganizationMgtDAOException If an error occurs while retrieving child devices.
     */
    DeviceNodeResult getChildrenOfDeviceNode(int deviceId, int maxDepth, boolean includeDevice, int tenantID) throws DeviceOrganizationMgtDAOException;

    /**
     * Retrieves parent devices for a given device node.
     *
     * @param deviceId      The device ID for which parent devices are retrieved.
     * @param maxDepth      The maximum depth to traverse when fetching parent devices.
     * @param includeDevice Flag to indicate whether to include the current device node in the result.
     * @param tenantID The ID of the tenant.
     * @return A list of parent device nodes.
     * @throws DeviceOrganizationMgtDAOException If an error occurs while retrieving parent devices.
     */
    DeviceNodeResult getParentsOfDeviceNode(int deviceId, int maxDepth, boolean includeDevice, int tenantID) throws DeviceOrganizationMgtDAOException;

    /**
     * Retrieves all device organization records.
     *
     * @return A list of device organization records.
     * @throws DeviceOrganizationMgtDAOException If an error occurs while retrieving device organization records.
     */
    List<DeviceOrganization> getAllDeviceOrganizations() throws DeviceOrganizationMgtDAOException;

    /**
     * Retrieves device Organization Roots
     *
     * @return A list of root device organization records.
     * @throws DeviceOrganizationMgtDAOException
     */
    public List<DeviceOrganization> getDeviceOrganizationRoots(PaginationRequest request, int tenantID) throws DeviceOrganizationMgtDAOException;

    /**
     * Retrieves device Organization Leafs
     *
     * @return A list of leaf device organization records.
     * @throws DeviceOrganizationMgtDAOException
     */
    public List<DeviceOrganization> getDeviceOrganizationLeafs(PaginationRequest request, int tenantID) throws DeviceOrganizationMgtDAOException;

    /**
     * Adds a new record to the device organization table.
     *
     * @param deviceOrganization The device organization to be added.
     * @return True if the device organization is successfully added, false otherwise.
     * @throws DeviceOrganizationMgtDAOException If an error occurs while adding the device organization record.
     */
    AdditionResult addDeviceOrganization(DeviceOrganization deviceOrganization) throws DeviceOrganizationMgtDAOException;

    /**
     * Checks whether a record already exists with the same deviceId and parentDeviceId.
     *
     * @param deviceId       The ID of the device.
     * @param parentDeviceId The ID of the parent device.
     * @param tenantID The ID of the tenant.
     * @return True if a record with the specified deviceId and parentDeviceId exists, false otherwise.
     * @throws DeviceOrganizationMgtDAOException If an error occurs while checking the existence of the record.
     */
    boolean isDeviceOrganizationExist(int deviceId, Integer parentDeviceId, int tenantID) throws DeviceOrganizationMgtDAOException;

//    /**
//     * Check whether there is a cyclic relationship upon creation of new organization
//     * @param deviceID       The ID of the target device.
//     * @param parentDeviceID       The ID of the source device.
//     * @param tenantID The ID of the tenant.
//     * @return True if a cyclic relationship get created.
//     * @throws DeviceOrganizationMgtDAOException
//     */
//    boolean isCyclicRelationshipExist(int deviceID, Integer parentDeviceID, int tenantID)
//            throws DeviceOrganizationMgtDAOException;

    /**
     * Get a device organization by the CHILD_PARENT_COMP_KEY unique key.
     *
     * @param deviceId       The ID of the child device.
     * @param parentDeviceId The ID of the parent device.
     * @param tenantID The ID of the tenant.
     * @return The DeviceOrganization object if found, null otherwise.
     * @throws DeviceOrganizationMgtDAOException if an error occurs while accessing the database.
     */
    DeviceOrganization getDeviceOrganizationByUniqueKey(int deviceId, Integer parentDeviceId, int tenantID)
            throws DeviceOrganizationMgtDAOException;

    /**
     * Updates a record in the device organization table with the provided information.
     *
     * @param deviceOrganization The DeviceOrganization object containing the updated information.
     * @return True if the record was successfully updated, false otherwise.
     * @throws DeviceOrganizationMgtDAOException If an error occurs while updating the record.
     */
    boolean updateDeviceOrganization(DeviceOrganization deviceOrganization)
            throws DeviceOrganizationMgtDAOException;

    /**
     * Retrieves a device organization record from the database based on the provided organization ID.
     *
     * @param organizationId The unique identifier of the device organization record to retrieve.
     * @param tenantID The ID of the tenant.
     * @return The DeviceOrganization object representing the retrieved organization, or null if not found.
     * @throws DeviceOrganizationMgtDAOException If an error occurs while retrieving the organization record.
     */
    DeviceOrganization getDeviceOrganizationByID(int organizationId, int tenantID) throws DeviceOrganizationMgtDAOException;

    /**
     * Deletes a device organization record from the database based on the provided device ID and parent Device ID.
     * @param deviceId
     * @param parentDeviceId
     * @param tenantID
     * @return true if the organization record was successfully deleted, false otherwise.
     * @throws DeviceOrganizationMgtDAOException
     */
    boolean deleteDeviceOrganizationByUniqueKey(int deviceId, Integer parentDeviceId, int tenantID) throws DeviceOrganizationMgtDAOException;

    /**
     * Deletes a device organization record from the database based on the provided organization ID.
     *
     * @param organizationId The unique identifier of the device organization record to delete.
     * @param tenantID The ID of the tenant.
     * @return true if the organization record was successfully deleted, false otherwise.
     * @throws DeviceOrganizationMgtDAOException If an error occurs while deleting the organization record.
     */
    boolean deleteDeviceOrganizationByID(int organizationId, int tenantID) throws DeviceOrganizationMgtDAOException;

    /**
     * Deletes records associated with a particular device ID from the device organization table.
     * This method deletes records where the provided device ID matches either the deviceID column or
     * parentDeviceID column in the device organization table.
     *
     * @param deviceId The unique identifier of the device for which associated records should be deleted.
     * @param tenantID The ID of the tenant.
     * @return true if associated records were successfully deleted, false otherwise.
     * @throws DeviceOrganizationMgtDAOException If an error occurs while deleting the associated records.
     */
    boolean deleteDeviceAssociations(int deviceId, int tenantID) throws DeviceOrganizationMgtDAOException;

    /**
     * Checks whether a record with the specified device ID exists either in the deviceID column or
     * parentDeviceID column in the device organization table.
     *
     * @param deviceId The unique identifier of the device to check for existence.
     * @param tenantID The ID of the tenant.
     * @return true if a record with the given device ID exists, false otherwise.
     * @throws DeviceOrganizationMgtDAOException If an error occurs while querying the database.
     */
    boolean isDeviceIdExist(int deviceId, int tenantID) throws DeviceOrganizationMgtDAOException;

    /**
     * Checks if a child device with the given `deviceId` exists in the database.
     *
     * @param deviceId The ID of the child device to check.
     * @param tenantID The ID of the tenant.
     * @return `true` if the child device exists, `false` otherwise.
     * @throws DeviceOrganizationMgtDAOException If an error occurs while checking the existence.
     */
    boolean isChildDeviceIdExist(int deviceId, int tenantID) throws DeviceOrganizationMgtDAOException;

    /**
     * This method is only used for testing
     * @param tenantID
     * @throws DeviceOrganizationMgtDAOException
     */
    void addAllDevices(int tenantID) throws DeviceOrganizationMgtDAOException;

    /**
     * This method is only used for testing
     * @throws DeviceOrganizationMgtDAOException
     */
    void addOrganizations(int tenantID, int start, int end) throws DeviceOrganizationMgtDAOException;
}
