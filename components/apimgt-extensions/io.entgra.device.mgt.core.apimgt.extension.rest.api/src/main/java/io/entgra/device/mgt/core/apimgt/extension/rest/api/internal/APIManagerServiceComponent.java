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

package io.entgra.device.mgt.core.apimgt.extension.rest.api.internal;

import io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServicesImpl;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.ConsumerRESTAPIServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.ConsumerRESTAPIServicesImpl;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.IOAuthClientService;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.OAuthClient;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.PublisherRESTAPIServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.PublisherRESTAPIServicesImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;

@Component(
        name = "io.entgra.device.mgt.core.apimgt.extension.rest.api.internal.APIManagerServiceComponent",
        immediate = true)
public class APIManagerServiceComponent {

    private static Log log = LogFactory.getLog(APIManagerServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing publisher API extension bundle");
        }
        try {
            BundleContext bundleContext = componentContext.getBundleContext();

            IOAuthClientService ioAuthClientService = OAuthClient.getInstance();
            bundleContext.registerService(IOAuthClientService.class, ioAuthClientService, null);
            APIManagerServiceDataHolder.getInstance().setIoAuthClientService(ioAuthClientService);

            APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
            bundleContext.registerService(APIApplicationServices.class.getName(), apiApplicationServices, null);
            APIManagerServiceDataHolder.getInstance().setApiApplicationServices(apiApplicationServices);

            PublisherRESTAPIServices publisherRESTAPIServices = new PublisherRESTAPIServicesImpl();
            bundleContext.registerService(PublisherRESTAPIServices.class.getName(), publisherRESTAPIServices, null);
            APIManagerServiceDataHolder.getInstance().setPublisherRESTAPIServices(publisherRESTAPIServices);

            ConsumerRESTAPIServices consumerRESTAPIServices = new ConsumerRESTAPIServicesImpl();
            bundleContext.registerService(ConsumerRESTAPIServices.class.getName(), consumerRESTAPIServices, null);
            APIManagerServiceDataHolder.getInstance().setConsumerRESTAPIServices(consumerRESTAPIServices);

            if (log.isDebugEnabled()) {
                log.debug("API Application bundle has been successfully initialized");
            }
        } catch (Exception e) {
            log.error("Error occurred while initializing API Application bundle", e);
        }
    }
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    @Reference(
            name = "apim.configuration.service",
            service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService apiManagerConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting API Manager Configuration Service");
        }
        APIManagerServiceDataHolder.getInstance().setAPIManagerConfiguration(apiManagerConfigurationService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService apiManagerConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting API Manager Configuration Service");
        }
        APIManagerServiceDataHolder.getInstance().setAPIManagerConfiguration(null);
    }
}
