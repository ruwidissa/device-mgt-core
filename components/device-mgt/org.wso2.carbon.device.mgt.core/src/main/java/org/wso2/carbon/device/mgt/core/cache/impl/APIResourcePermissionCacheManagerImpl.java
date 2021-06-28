/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.cache.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.permission.mgt.Permission;
import org.wso2.carbon.device.mgt.core.cache.APIResourcePermissionCacheKey;
import org.wso2.carbon.device.mgt.core.cache.APIResourcePermissionCacheManager;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import javax.cache.Cache;
import java.util.List;

public class APIResourcePermissionCacheManagerImpl implements APIResourcePermissionCacheManager {


    private static final Log log = LogFactory.getLog(APIResourcePermissionCacheManagerImpl.class);

    private static APIResourcePermissionCacheManagerImpl apiResourceCacgeManager;

    private APIResourcePermissionCacheManagerImpl() {
    }

    public static APIResourcePermissionCacheManagerImpl getInstance() {
        if (apiResourceCacgeManager == null) {
            synchronized (APIResourcePermissionCacheManagerImpl.class) {
                if (apiResourceCacgeManager == null) {
                    apiResourceCacgeManager = new APIResourcePermissionCacheManagerImpl();
                }
            }
        }
        return apiResourceCacgeManager;
    }


    @Override
    public void addAPIResourcePermissionToCache(APIResourcePermissionCacheKey cacheKey, List<Permission> permissions) {
        Cache<APIResourcePermissionCacheKey, List<Permission>> lCache = DeviceManagerUtil.getAPIResourcePermissionCache();
        if (lCache != null) {
            if (lCache.containsKey(cacheKey)) {
                this.updateAPIResourcePermissionInCache(cacheKey, permissions);
            } else {
                lCache.put(cacheKey, permissions);
            }
        }
    }

    @Override
    public void updateAPIResourcePermissionInCache(APIResourcePermissionCacheKey cacheKey, List<Permission> permissions) {

        Cache<APIResourcePermissionCacheKey, List<Permission>> lCache = DeviceManagerUtil.getAPIResourcePermissionCache();
        if (lCache != null) {
            if (lCache.containsKey(cacheKey)) {
                lCache.replace(cacheKey, permissions);
            }
        }

    }

    @Override
    public List<Permission> getAPIResourceRermissionFromCache(APIResourcePermissionCacheKey cacheKey) {
        Cache<APIResourcePermissionCacheKey, List<Permission>> lCache = DeviceManagerUtil.getAPIResourcePermissionCache();
        if (lCache != null) {
            return lCache.get(cacheKey);
        }
        return null;
    }
}
