/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.api.impl.util;

import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.service.GrafanaQueryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class GrafanaMgtAPIUtils {

    private static final Log log = LogFactory.getLog(GrafanaMgtAPIUtils.class);
    private static volatile GrafanaQueryService grafanaQueryService;

    /**
     * Accessing GrafanaQueryService from OSGI service context
     * @return GrafanaQueryService instance
     */
    public static GrafanaQueryService getGrafanaQueryService() {
        if (grafanaQueryService == null) {
            synchronized (GrafanaMgtAPIUtils.class) {
                if (grafanaQueryService == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    grafanaQueryService =
                            (GrafanaQueryService) ctx.getOSGiService(GrafanaQueryService.class, null);
                    if (grafanaQueryService == null) {
                        String msg = "Grafana Query service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
        return grafanaQueryService;
    }
}
