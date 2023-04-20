/*
 * Copyright (c) 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.devicemgt.apimgt.extension.rest.api.internal;

import io.entgra.devicemgt.apimgt.extension.rest.api.APIApplicationServices;
import io.entgra.devicemgt.apimgt.extension.rest.api.APIApplicationServicesImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

/**
 * @scr.component name="io.entgra.devicemgt.apimgt.extension.rest.api.internal.PublisherRESTAPIServiceComponent"
 * immediate="true"
 */
public class PublisherRESTAPIServiceComponent {

    private static Log log = LogFactory.getLog(PublisherRESTAPIServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing publisher API extension bundle");
        }
        try {
            BundleContext bundleContext = componentContext.getBundleContext();

            APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
            bundleContext.registerService(APIApplicationServices.class.getName(), apiApplicationServices, null);
            PublisherRESTAPIDataHolder.getInstance().setApiApplicationServices(apiApplicationServices);

            if (log.isDebugEnabled()) {
                log.debug("API Application bundle has been successfully initialized");
            }
        } catch (Exception e) {
            log.error("Error occurred while initializing API Application bundle", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }
}
