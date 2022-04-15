/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.analytics.mgt.grafana.proxy.core.internal;

import io.entgra.analytics.mgt.grafana.proxy.core.service.GrafanaAPIService;
import io.entgra.analytics.mgt.grafana.proxy.core.service.GrafanaQueryService;

public class GrafanaMgtDataHolder {

    private GrafanaAPIService grafanaAPIService;
    private GrafanaQueryService grafanaQueryService;

    private GrafanaMgtDataHolder() {

    }

    public GrafanaAPIService getGrafanaAPIService() {
        return grafanaAPIService;
    }

    public void setGrafanaAPIService(GrafanaAPIService grafanaAPIService) {
        this.grafanaAPIService = grafanaAPIService;
    }

    public GrafanaQueryService getGrafanaQueryService() {
        return grafanaQueryService;
    }

    public void setGrafanaQueryService(GrafanaQueryService grafanaQueryService) {
        this.grafanaQueryService = grafanaQueryService;
    }

    public static class InstanceHolder {
        public static GrafanaMgtDataHolder instance = new GrafanaMgtDataHolder();
    }

    public static GrafanaMgtDataHolder getInstance() {
        return InstanceHolder.instance;
    }


}
