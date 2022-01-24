/*
 * Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.analytics.mgt.grafana.proxy.core.service.cache;

import java.util.Objects;

public class QueryTemplateCacheKey {

    private final String dashboardUID;
    private final String panelId;
    private final String refId;
    private volatile int hashCode;

    public QueryTemplateCacheKey(String dashboardUID, String panelId, String refId) {
        this.dashboardUID = dashboardUID;
        this.panelId = panelId;
        this.refId = refId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof QueryTemplateCacheKey) {
            final QueryTemplateCacheKey other = (QueryTemplateCacheKey) obj;
            String thisId = this.dashboardUID + this.panelId + this.refId;
            String otherId = other.dashboardUID + other.panelId + other.refId;
            return thisId.equals(otherId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hash(dashboardUID, panelId, refId);
        }
        return hashCode;
    }
}
