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

package io.entgra.device.mgt.core.device.mgt.url.printer.internal;

import io.entgra.device.mgt.core.device.mgt.url.printer.URLPrinterStartupHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

@Component(
        name = "io.entgra.device.mgt.core.device.mgt.url.printer.internal.UrlPrinterServiceComponent",
        immediate = true)
public class UrlPrinterServiceComponent {

    private static final Log log = LogFactory.getLog(UrlPrinterServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Activating Url printer Service Component");
        }
        try {
            BundleContext bundleContext = ctx.getBundleContext();
            bundleContext.registerService(ServerStartupObserver.class.getName(), new URLPrinterStartupHandler(), null);
            if (log.isDebugEnabled()) {
                log.debug("Url printer Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Url printer Service Component", e);
        }
    }
    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Url printer Service Component");
        }
    }

    @Reference(
            name = "configuration.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting ConfigurationContextService");
        }

        UrlPrinterDataHolder.getInstance().setConfigurationContextService(configurationContextService);

    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("Un-setting ConfigurationContextService");
        }
        UrlPrinterDataHolder.getInstance().setConfigurationContextService(null);
    }
}
