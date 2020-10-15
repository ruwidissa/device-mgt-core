/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.common.geo.service;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;

import java.util.List;

/**
 * This represents the Geo service functionality which should be implemented by
 * required GeoServiceManagers.
 */
public interface GeoLocationProviderService {

    List<GeoFence> getWithinAlerts(DeviceIdentifier identifier, String owner) throws GeoLocationBasedServiceException;

    List<GeoFence> getWithinAlerts() throws GeoLocationBasedServiceException;

    List<GeoFence> getExitAlerts(DeviceIdentifier identifier, String owner) throws GeoLocationBasedServiceException;

    List<GeoFence> getExitAlerts() throws GeoLocationBasedServiceException;

    boolean createGeoAlert(Alert alert, DeviceIdentifier identifier, String alertType, String owner)
            throws GeoLocationBasedServiceException, AlertAlreadyExistException;

    boolean createGeoAlert(Alert alert, String alertType)
            throws GeoLocationBasedServiceException,AlertAlreadyExistException;

    boolean updateGeoAlert(Alert alert, DeviceIdentifier identifier, String alertType, String owner)
            throws GeoLocationBasedServiceException, AlertAlreadyExistException;

    boolean updateGeoAlert(Alert alert, String alertType)
            throws GeoLocationBasedServiceException,AlertAlreadyExistException;

    boolean removeGeoAlert(String alertType, DeviceIdentifier identifier, String queryName, String owner)
            throws GeoLocationBasedServiceException;

    boolean removeGeoAlert(String alertType, String queryName)
            throws GeoLocationBasedServiceException;

    String getSpeedAlerts(DeviceIdentifier identifier, String owner) throws GeoLocationBasedServiceException;

    String getSpeedAlerts() throws GeoLocationBasedServiceException;

    String getProximityAlerts(DeviceIdentifier identifier, String owner) throws GeoLocationBasedServiceException;

    String getProximityAlerts() throws GeoLocationBasedServiceException;

    List<GeoFence> getStationaryAlerts(DeviceIdentifier identifier, String owner) throws GeoLocationBasedServiceException;

    List<GeoFence> getStationaryAlerts() throws GeoLocationBasedServiceException;

    List<GeoFence> getTrafficAlerts(DeviceIdentifier identifier, String owner) throws GeoLocationBasedServiceException;

    List<GeoFence> getTrafficAlerts() throws GeoLocationBasedServiceException;

    /**
     * Create new GeoFence
     * @param geofenceData fence data
     * @return true if the fence creation success
     * @throws GeoLocationBasedServiceException error occurs while creating a geofence
     */
    boolean createGeofence(GeofenceData geofenceData) throws GeoLocationBasedServiceException;

    /**
     * Get geofence by ID
     * @param fenceId id of the fence which should be retrieved
     * @return Extracted geofence data
     * @throws GeoLocationBasedServiceException error occurs while retrieving a geofence
     */
    GeofenceData getGeoFences(int fenceId) throws GeoLocationBasedServiceException;

    /**
     * Get paginated geofence list
     * @param request Pagination Request
     * @return List of Geofences retrieved
     * @throws GeoLocationBasedServiceException error occurs while retrieving geofences
     */
    List<GeofenceData> getGeoFences(PaginationRequest request) throws GeoLocationBasedServiceException;

    /**
     * Search geo fences using the fence name
     * @param name searching name of the fence
     * @return list of fences found for the specific name
     * @throws GeoLocationBasedServiceException  for errors occur while querying geo fences
     */
    List<GeofenceData> getGeoFences(String name) throws GeoLocationBasedServiceException;

    /**
     * Get all geo fences of the tenant
     * @return list of the all geo fences of the tenant
     * @throws GeoLocationBasedServiceException for errors occur while querying geo fences
     */
    List<GeofenceData> getGeoFences() throws GeoLocationBasedServiceException;

    /**
     * Delete Geofence with ID
     * @param fenceId Id of the fence which should be deleted
     * @return true if deletion success. false if not record found for the used Id
     * @throws GeoLocationBasedServiceException  for errors occur while deleting geo fences
     */
    boolean deleteGeofenceData(int fenceId) throws GeoLocationBasedServiceException;

    /**
     * Update a Geofence. Will not be updated tenantId and owner
     * @param geofenceData Bean with updated geofence data
     * @param fenceId Id of the fence which should be updated
     * @return true if update success. false if not a record found for the used Id
     * @throws GeoLocationBasedServiceException  for errors occur while updating geo fences
     */
    boolean updateGeofence(GeofenceData geofenceData, int fenceId) throws GeoLocationBasedServiceException;
}
