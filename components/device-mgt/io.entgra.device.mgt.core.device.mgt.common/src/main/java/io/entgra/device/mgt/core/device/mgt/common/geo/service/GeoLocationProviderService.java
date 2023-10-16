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

package io.entgra.device.mgt.core.device.mgt.common.geo.service;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.PaginationResult;
import io.entgra.device.mgt.core.device.mgt.common.event.config.EventConfig;
import io.entgra.device.mgt.core.device.mgt.common.event.config.EventConfigurationException;

import java.util.ArrayList;
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
     * @throws EventConfigurationException  for errors occur while creating event configuration for the geofence
     */
    boolean createGeofence(GeofenceData geofenceData) throws GeoLocationBasedServiceException, EventConfigurationException;

    /**
     * Get geofence by ID
     * @param fenceId id of the fence which should be retrieved
     * @return {@link GeofenceData} Extracted geofence data
     * @throws GeoLocationBasedServiceException error occurs while retrieving a geofence
     */
    GeofenceData getGeoFences(int fenceId) throws GeoLocationBasedServiceException;

    /**
     * Get paginated geofence list
     * @param request Pagination Request
     * @return {@link GeofenceData} List of Geofences retrieved
     * @throws GeoLocationBasedServiceException error occurs while retrieving geofences
     */
    List<GeofenceData> getGeoFences(PaginationRequest request) throws GeoLocationBasedServiceException;

    /**
     * Search geo fences using the fence name
     * @param name searching name of the fence
     * @return {@link GeofenceData} list of fences found for the specific name
     * @throws GeoLocationBasedServiceException  for errors occur while querying geo fences
     */
    List<GeofenceData> getGeoFences(String name) throws GeoLocationBasedServiceException;

    /**
     * Get all geo fences of the tenant
     * @return {@link GeofenceData} list of the all geo fences of the tenant
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
     * @throws EventConfigurationException  for errors occur while updating event records of the geofence
     */
    boolean updateGeofence(GeofenceData geofenceData, int fenceId)
            throws GeoLocationBasedServiceException, EventConfigurationException;

    /**
     * Update geofence event configuration
     * @param geofenceData updated GeoFenceData object
     * @param removedEventIdList removed event ids
     * @param groupIds newly added group ids to be mapped with event records
     * @param fenceId updating fence id
     * @return true for successful update of geofence event data
     * @throws GeoLocationBasedServiceException any errors occurred while updating event records of the fence
     */
    boolean updateGeoEventConfigurations(GeofenceData geofenceData, List<Integer> removedEventIdList,
                                         List<Integer> groupIds, int fenceId) throws GeoLocationBasedServiceException;

    /**
     * Attach event data into geofence objects
     * @param geoFences list of GeofenceData to attach corresponding event data
     * @return {@link GeofenceData} events attached geofence object list
     * @throws GeoLocationBasedServiceException any errors occurred while attaching event records to geofences
     */
    List<GeofenceData> attachEventObjects(List<GeofenceData> geoFences) throws GeoLocationBasedServiceException;

    /**
     * Get Geofence records of groups. Attaching with corresponding event data of fences
     * @param groupId Id of the group geo which fences attached
     * @param tenantId Id of the tenant which geo fences attached
     * @param requireEventData use true for attach event records with the geo fence data
     * @return {@link GeofenceData} Queried geo fence data using group Id
     * @throws GeoLocationBasedServiceException any errors occurred while getting geofences of group
     */
    List<GeofenceData> getGeoFencesOfGroup(int groupId, int tenantId, boolean requireEventData) throws GeoLocationBasedServiceException;

    /**
     * Get event records mapped with specific geo fence
     * @param geoFenceId Id of the Geofence to retrieve mapped events
     * @return {@link EventConfig} Event records of the geofence
     * @throws GeoLocationBasedServiceException any errors occurred while reading event records to geofence
     */
    List<EventConfig> getEventsOfGeoFence(int geoFenceId) throws GeoLocationBasedServiceException;

    /**
     * Get geo fence count by tenant id
     * @return returns the geofence count of tenant.
     * @throws GeoLocationBasedServiceException any errors occurred while reading event records to geofence
     */
    int getGeoFenceCount() throws GeoLocationBasedServiceException;
}
