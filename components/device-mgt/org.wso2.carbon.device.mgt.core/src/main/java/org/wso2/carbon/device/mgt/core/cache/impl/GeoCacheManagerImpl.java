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

package org.wso2.carbon.device.mgt.core.cache.impl;

import org.wso2.carbon.device.mgt.common.geo.service.GeofenceData;
import org.wso2.carbon.device.mgt.core.cache.GeoCacheKey;
import org.wso2.carbon.device.mgt.core.cache.GeoCacheManager;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import javax.cache.Cache;

public class GeoCacheManagerImpl implements GeoCacheManager {

    private static GeoCacheManager geoCacheManager;
    private GeoCacheManagerImpl() {}

    public static GeoCacheManager getInstance() {
        if (geoCacheManager == null) {
            synchronized (GeoCacheManagerImpl.class) {
                if (geoCacheManager == null) {
                    geoCacheManager = new GeoCacheManagerImpl();
                }
            }
        }
        return geoCacheManager;
    }

    @Override
    public void addFenceToCache(GeofenceData geofenceData, int fenceId, int tenantId) {
        Cache<GeoCacheKey, GeofenceData> lCache = DeviceManagerUtil.getGeoCache();
        if (lCache != null) {
            GeoCacheKey cacheKey = getCacheKey(fenceId, tenantId);
            if (lCache.containsKey(cacheKey)) {
                this.updateGeoFenceInCache(geofenceData, fenceId, tenantId);
            } else {
                lCache.put(cacheKey, geofenceData);
            }
        }
    }

    @Override
    public void removeFenceFromCache(int fenceId, int tenantId) {
        Cache<GeoCacheKey, GeofenceData> lCache = DeviceManagerUtil.getGeoCache();
        if (lCache != null) {
            GeoCacheKey cacheKey = getCacheKey(fenceId, tenantId);
            if (lCache.containsKey(cacheKey)) {
                lCache.remove(cacheKey);
            }
        }
    }

    @Override
    public void updateGeoFenceInCache(GeofenceData geofenceData, int fenceId, int tenantId) {
        Cache<GeoCacheKey, GeofenceData> lCache = DeviceManagerUtil.getGeoCache();
        if (lCache != null) {
            GeoCacheKey cacheKey = getCacheKey(fenceId, tenantId);
            if (lCache.containsKey(cacheKey)) {
                lCache.replace(cacheKey, geofenceData);
            }
        }
    }

    @Override
    public GeofenceData getGeoFenceFromCache(int fenceId, int tenantId) {
        GeofenceData geofenceData = null;
        Cache<GeoCacheKey, GeofenceData> lCache = DeviceManagerUtil.getGeoCache();
        if (lCache != null) {
            geofenceData = lCache.get(getCacheKey(fenceId, tenantId));
        }
        return geofenceData;
    }

    private GeoCacheKey getCacheKey(int fenceId, int tenantId) {
        GeoCacheKey geoCacheKey = new GeoCacheKey();
        geoCacheKey.setFenceId(fenceId);
        geoCacheKey.setTenantId(tenantId);
        return geoCacheKey;
    }
}
