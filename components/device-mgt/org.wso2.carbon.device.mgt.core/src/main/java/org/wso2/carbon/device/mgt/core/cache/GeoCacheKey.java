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

import java.util.Objects;

public class GeoCacheKey {
    private int fenceId;
    private int tenantId;
    private volatile int hashCode;

    public GeoCacheKey(int fenceId, int tenantId) {
        this.fenceId = fenceId;
        this.tenantId = tenantId;
    }

    public GeoCacheKey() {
    }

    public int getFenceId() {
        return fenceId;
    }

    public void setFenceId(int fenceId) {
        this.fenceId = fenceId;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!GeoCacheKey.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final GeoCacheKey other = (GeoCacheKey) obj;
        String thisId = this.fenceId + "-" + "_" + this.tenantId;
        String otherId = other.fenceId + "-" + "_" + other.tenantId;
        return thisId.equals(otherId);
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hash(fenceId, tenantId);
        }
        return hashCode;
    }
}
