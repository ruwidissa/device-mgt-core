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
package org.wso2.carbon.device.mgt.core.cache;

import java.util.Objects;

public class APIResourcePermissionCacheKey {

    private String context;
    private volatile int hashCode;

    public APIResourcePermissionCacheKey(String context) {
        this.context = context;
    }


    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!APIResourcePermissionCacheKey.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final APIResourcePermissionCacheKey other = (APIResourcePermissionCacheKey) obj;
        String thisId = this.context;
        String otherId = other.context;
        if (!thisId.equals(otherId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hash(context);
        }
        return hashCode;
    }
}
