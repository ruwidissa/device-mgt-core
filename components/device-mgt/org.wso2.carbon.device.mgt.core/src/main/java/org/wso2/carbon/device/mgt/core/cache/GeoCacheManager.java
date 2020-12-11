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

package org.wso2.carbon.device.mgt.core.cache;

import org.wso2.carbon.device.mgt.common.geo.service.GeofenceData;

public interface GeoCacheManager {
    /**
     * Add geo fences to the cache
     * @param geofenceData adding fence object
     * @param fenceId id of the fence
     * @param tenantId id of the tenant
     */
    void addFenceToCache(GeofenceData geofenceData, int fenceId, int tenantId);

    /**
     * Update geo fences already in cache
     * @param geofenceData updating geo fence object
     * @param fenceId id of the fence
     * @param tenantId id of the tenant
     */
    void updateGeoFenceInCache(GeofenceData geofenceData, int fenceId, int tenantId);

    /**
     * Remove geo fence from cache
     * @param fenceId id of the fence
     * @param tenantId id of the tenant
     */
    void removeFenceFromCache(int fenceId, int tenantId);

    /**
     * Get geo fence data from the cache
     * @param fenceId id of the retrieving fence object
     * @param tenantId tenant id of the fence created
     * @return GeofenceData object if the cache have the specific object or null if there is no entry
     */
    GeofenceData getGeoFenceFromCache(int fenceId, int tenantId);
}
