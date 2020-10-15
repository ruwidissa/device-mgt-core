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
import org.wso2.carbon.device.mgt.common.geo.service.GeofenceData;

import java.util.List;

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
}
