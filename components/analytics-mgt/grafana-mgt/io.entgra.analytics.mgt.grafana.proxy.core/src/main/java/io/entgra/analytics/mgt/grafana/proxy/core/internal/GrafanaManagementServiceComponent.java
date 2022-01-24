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
import io.entgra.analytics.mgt.grafana.proxy.core.service.impl.GrafanaAPIServiceImpl;
import io.entgra.analytics.mgt.grafana.proxy.core.service.impl.GrafanaQueryServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * @scr.component name="io.entgra.analytics.mgt.grafana.proxy.grafanamanagementservicecomponent" immediate="true"
 */
public class GrafanaManagementServiceComponent {

    private static Log log = LogFactory.getLog(GrafanaManagementServiceComponent.class);

    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing grafana proxy management core bundle");
            }

            BundleContext bundleContext = componentContext.getBundleContext();

            GrafanaAPIService grafanaAPIService = new GrafanaAPIServiceImpl();
            GrafanaQueryService grafanaQueryService = new GrafanaQueryServiceImpl(grafanaAPIService);
            bundleContext.registerService(GrafanaAPIService.class.getName(), grafanaAPIService, null);
            GrafanaMgtDataHolder.getInstance().setGrafanaAPIService(grafanaAPIService);
            bundleContext.registerService(GrafanaQueryService.class.getName(), grafanaQueryService, null);
            GrafanaMgtDataHolder.getInstance().setGrafanaQueryService(grafanaQueryService);

            if (log.isDebugEnabled()) {
                log.debug("Grafana management core bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing grafana management core bundle", e);
        }
    }

    @SuppressWarnings("unused")
    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Grafana Management Service Component");
        }
    }
}
