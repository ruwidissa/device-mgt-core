/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.cache;

import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;

import java.sql.Timestamp;
import java.util.List;

public interface BillingCacheManager {
    /**
     * Adds a given billing object to the billing-cache.
     * @param startDate - startDate of the billing period.
     * @param endDate - endDate of the billing period.
     * @param paginationResult - PaginationResult object to be added.
     * @param tenantDomain - Owning tenant of the billing.
     *
     */
    void addBillingToCache(PaginationResult paginationResult, String tenantDomain, Timestamp startDate, Timestamp endDate) throws DeviceManagementException;

    /**
     * Removes a billing object from billing-cache.
     * @param startDate - startDate of the billing period.
     * @param endDate - endDate of the billing period.
     * @param tenantDomain - Owning tenant of the billing.
     *
     */
    void removeBillingFromCache(String tenantDomain, Timestamp startDate, Timestamp endDate) throws DeviceManagementException;

    /**
     * Removes a list of devices from billing-cache.
     * @param billingList - List of Cache-Keys of the billing objects to be removed.
     *
     */
    void removeBillingsFromCache(List<BillingCacheKey> billingList) throws DeviceManagementException;

    /**
     * Updates a given billing object in the billing-cache.
     * @param startDate - startDate of the billing period.
     * @param endDate - endDate of the billing period.
     * @param paginationResult - PaginationResult object to be updated.
     * @param tenantDomain - Owning tenant of the billing.
     *
     */
    void updateBillingInCache(PaginationResult paginationResult, String tenantDomain, Timestamp startDate, Timestamp endDate) throws DeviceManagementException;

    /**
     * Fetches a billing object from billing-cache.
     * @param startDate - startDate of the billing period.
     * @param endDate - endDate of the billing period.
     * @param tenantDomain - Owning tenant of the billing.
     * @return Device object
     *
     */
    PaginationResult getBillingFromCache(String tenantDomain, Timestamp startDate, Timestamp endDate);
}
