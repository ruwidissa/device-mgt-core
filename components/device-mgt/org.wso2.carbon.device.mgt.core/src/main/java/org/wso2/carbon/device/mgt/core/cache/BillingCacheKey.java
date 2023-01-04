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

import java.sql.Timestamp;
import java.util.Objects;

public class BillingCacheKey {

    private String tenantDomain;
    private Timestamp startDate;
    private Timestamp endDate;
    private volatile int hashCode;

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!BillingCacheKey.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final BillingCacheKey other = (BillingCacheKey) obj;
        String thisId = this.tenantDomain + "_" + this.startDate + "_" + this.endDate;
        String otherId = other.tenantDomain + "_" + other.startDate + "_" + this.endDate;
        if (!thisId.equals(otherId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hash(tenantDomain, startDate, endDate);
        }
        return hashCode;
    }
}
