/*
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.event.config.EventConfig;
import org.wso2.carbon.device.mgt.common.geo.service.GeofenceData;
import org.wso2.carbon.device.mgt.core.dto.event.config.GeoFenceGroupMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Use to manage geofence data in DB
 */
public interface GeofenceDAO {

    /**
     * Create new record of GeoFence
     * @param geofenceData GeoFence record data
     * @return created row count
     * @throws DeviceManagementDAOException error occurs while saving the data
     */
    GeofenceData saveGeofence(GeofenceData geofenceData) throws DeviceManagementDAOException;

    /**
     * Retrieve a geofence record for specified Id
     * @param fenceId Id of the fence which should be queried
     * @return Retrieved geofence data with tenant and owner info
     * @throws DeviceManagementDAOException error occurs while reading the data
     */
    GeofenceData getGeofence(int fenceId) throws DeviceManagementDAOException;

    /**
     * Retrieve a paginated list of geofence data for a specific tenant
     * @param request pagination request with offset and limit
     * @param tenantId Id of the tenant which fences owned
     * @return List of geofences retrieved
     * @throws DeviceManagementDAOException error occurs while reading the data
     */
    List<GeofenceData> getGeoFencesOfTenant(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * Search geofence by fence name of a specific tenant
     * @param fenceName searching name
     * @param tenantId searching tenant
     * @return list of found fences
     * @throws DeviceManagementDAOException
     */
    List<GeofenceData> getGeoFencesOfTenant(String fenceName, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * Get all fences of the specific tenant
     * @param tenantId tenant id of the fences
     * @return list of the fences owned by the tenant
     * @throws DeviceManagementDAOException
     */
    List<GeofenceData> getGeoFencesOfTenant(int tenantId)
            throws DeviceManagementDAOException;

    /**
     * Delete a geofence using the geofence Id
     * @param fenceId Id of the fence which should be deleted
     * @return Affected row count
     * @throws DeviceManagementDAOException error occurs while deleting the data
     */
    int deleteGeofenceById(int fenceId) throws DeviceManagementDAOException;

    /**
     * Update a geofence record using fence id
     * @param geofenceData updated geofence data
     * @param fenceId id of the fence which should be updated
     * @return affected row count
     * @throws DeviceManagementDAOException error occurs while updating the data
     */
    int updateGeofence(GeofenceData geofenceData, int fenceId) throws DeviceManagementDAOException;

    /**
     * Create geofence-group mapping records for the fence associated groups
     * @param geofenceData geofence data to be mapped with device group
     * @param groupIds group ids of the geofence
     * @return true for the successful record creation
     * @throws DeviceManagementDAOException error occurred while saving event records
     */
    boolean createGeofenceGroupMapping(GeofenceData geofenceData, List<Integer> groupIds) throws DeviceManagementDAOException;

    /**
     * Get associated group ids of a geofence mapped with
     * @param fenceId id of the fence
     * @return list of group ids mapped with the specified fence
     * @throws DeviceManagementDAOException error occurred while reading group id records
     */
    List<Integer> getGroupIdsOfGeoFence(int fenceId) throws DeviceManagementDAOException;

    /**
     * Delete geofence-group mapping records
     * @param groupIdsToDelete group ids to be removed from the mapping table
     * @throws DeviceManagementDAOException error occurred while deleting group id mapping records
     */
    void deleteGeofenceGroupMapping(List<Integer> groupIdsToDelete, int fenceId) throws DeviceManagementDAOException;

    /**
     * Create geofence-event mapping records
     * @param fenceId geofence id of the mapping records to be placed
     * @param eventIds generated event ids for the geofence event configuration
     * @throws DeviceManagementDAOException error occurred while creating geofence event mapping records
     */
    void createGeofenceEventMapping(int fenceId, List<Integer> eventIds) throws DeviceManagementDAOException;

    /**
     * Remove geofence-event mapping records
     * @param removedEventIdList event ids should be removed from the records
     * @throws DeviceManagementDAOException error occurred deleting geofence event mapping
     */
    void deleteGeofenceEventMapping(List<Integer> removedEventIdList) throws DeviceManagementDAOException;

    /**
     * Get events of the geofence using fence ids
     * @param geofenceIds ids of geo fences to be queried
     * @return Event config list mapped with fence id
     * @throws DeviceManagementDAOException error occurred while retrieving geo fence event map
     */
    Map<Integer, List<EventConfig>> getEventsOfGeoFences(List<Integer> geofenceIds) throws DeviceManagementDAOException;

    /**
     * Get events of a particular geofence
     * @param geofenceId id of the fence to be queried
     * @return EventConfig list of the particular geofence
     * @throws DeviceManagementDAOException thrown errors while getting events of geofence
     */
    List<EventConfig> getEventsOfGeoFence(int geofenceId) throws DeviceManagementDAOException;

    /**
     * Get group Ids mapped with fence ids
     * @param fenceIds fence id list to be queried
     * @return GroupIds mapped with geofence id
     * @throws DeviceManagementDAOException thrown errors while retrieving group Ids of geo fence
     */
    Set<GeoFenceGroupMap> getGroupIdsOfGeoFences(List<Integer> fenceIds) throws DeviceManagementDAOException;

    /**
     * Get geo fences of the specific group and tenant
     * @param groupId id of the group
     * @param tenantId tenant id of the geo fences
     * @return List of geofence data mapped with specific group and tenant
     * @throws DeviceManagementDAOException
     */
    List<GeofenceData> getGeoFences(int groupId, int tenantId) throws DeviceManagementDAOException;

    /**
     * Get geofence using fence id and attached group Ids
     * @param fenceId id of the fence
     * @param requireGroupData true if mapped group data needed
     * @return Geofence data
     * @throws DeviceManagementDAOException
     */
    GeofenceData getGeofence(int fenceId, boolean requireGroupData) throws DeviceManagementDAOException;
}
