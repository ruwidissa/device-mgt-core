/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.cache.BillingCacheKey;
import org.wso2.carbon.device.mgt.core.cache.BillingCacheManager;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import javax.cache.Cache;
import java.sql.Timestamp;
import java.util.List;

/**
 * Implementation of BillingCacheManager.
 */
public class BillingCacheManagerImpl implements BillingCacheManager {

    private static final Log log = LogFactory.getLog(BillingCacheManagerImpl.class);

    private static BillingCacheManagerImpl billingCacheManager;

    private BillingCacheManagerImpl() {
    }

    public static BillingCacheManagerImpl getInstance() {
        if (billingCacheManager == null) {
            synchronized (BillingCacheManagerImpl.class) {
                if (billingCacheManager == null) {
                    billingCacheManager = new BillingCacheManagerImpl();
                }
            }
        }
        return billingCacheManager;
    }

    @Override
    public void addBillingToCache(PaginationResult paginationResult, String tenantDomain, Timestamp startDate, Timestamp endDate) throws DeviceManagementException {
        Cache<BillingCacheKey, PaginationResult> lCache = DeviceManagerUtil.getBillingCache();
        if (lCache != null) {
            BillingCacheKey cacheKey = getCacheKey(tenantDomain, startDate, endDate);
            if (lCache.containsKey(cacheKey)) {
                this.updateBillingInCache(paginationResult, tenantDomain, startDate, endDate);
            } else {
                lCache.put(cacheKey, paginationResult);
            }
        }
    }

    @Override
    public void removeBillingFromCache(String tenantDomain, Timestamp startDate, Timestamp endDate) throws DeviceManagementException {
        Cache<BillingCacheKey, PaginationResult> lCache = DeviceManagerUtil.getBillingCache();
        if (lCache != null) {
            BillingCacheKey cacheKey = getCacheKey(tenantDomain, startDate, endDate);
            if (lCache.containsKey(cacheKey)) {
                lCache.remove(cacheKey);
            }
        } else {
            String msg = "Failed to remove selected billing from cache";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
    }

    @Override
    public void removeBillingsFromCache(List<BillingCacheKey> billingList) throws DeviceManagementException {
        Cache<BillingCacheKey, PaginationResult> lCache = DeviceManagerUtil.getBillingCache();
        if (lCache != null) {
            for (BillingCacheKey cacheKey : billingList) {
                if (lCache.containsKey(cacheKey)) {
                    lCache.remove(cacheKey);
                }
            }
        } else {
            String msg = "Failed to remove billing from cache";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
    }

    @Override
    public void updateBillingInCache(PaginationResult paginationResult, String tenantDomain, Timestamp startDate, Timestamp endDate) throws DeviceManagementException {
        Cache<BillingCacheKey, PaginationResult> lCache = DeviceManagerUtil.getBillingCache();
        if (lCache != null) {
            BillingCacheKey cacheKey = getCacheKey(tenantDomain, startDate, endDate);
            if (lCache.containsKey(cacheKey)) {
                lCache.replace(cacheKey, paginationResult);
            }
        } else {
            String msg = "Failed to update billing cache";
            log.error(msg);
            throw new DeviceManagementException(msg);
        }
    }

    // TODO remove null check from here and do cache enable check in the methods calling this
    @Override
    public PaginationResult getBillingFromCache(String tenantDomain, Timestamp startDate, Timestamp endDate) {
        Cache<BillingCacheKey, PaginationResult> lCache = DeviceManagerUtil.getBillingCache();
        if (lCache != null) {
            return lCache.get(getCacheKey(tenantDomain, startDate, endDate));
        }
        return null;
    }

    /**
     * This method generates the billing CacheKey and returns it.
     */
    private BillingCacheKey getCacheKey(String tenantDomain, Timestamp startDate, Timestamp endDate) {
        BillingCacheKey billingCacheKey = new BillingCacheKey();
        billingCacheKey.setTenantDomain(tenantDomain);
        billingCacheKey.setStartDate(startDate);
        billingCacheKey.setEndDate(endDate);
        return billingCacheKey;
    }
}