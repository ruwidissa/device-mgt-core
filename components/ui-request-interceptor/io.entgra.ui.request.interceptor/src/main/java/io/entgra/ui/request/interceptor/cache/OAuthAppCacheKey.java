/*
 * Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.ui.request.interceptor.cache;

import java.util.Objects;

/**
 * The key object used for Login Cache
 */
public class OAuthAppCacheKey {

    private String appName;
    private String appOwner;
    private volatile int hashCode;

    public OAuthAppCacheKey(String appName, String appOwner) {
        this.appName = appName;
        this.appOwner = appOwner;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppOwner() {
        return appOwner;
    }

    public void setAppOwner(String appOwner) {
        this.appOwner = appOwner;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof OAuthAppCacheKey) {
            final OAuthAppCacheKey other = (OAuthAppCacheKey) obj;
            String thisId = this.appName + "-" + this.appOwner;
            String otherId = other.appName + "-" + other.appOwner;
            return thisId.equals(otherId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hash(appName, appOwner);
        }
        return hashCode;
    }
}
