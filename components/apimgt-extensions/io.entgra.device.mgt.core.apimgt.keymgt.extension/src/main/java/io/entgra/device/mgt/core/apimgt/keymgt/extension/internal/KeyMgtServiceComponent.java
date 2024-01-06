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

package io.entgra.device.mgt.core.apimgt.keymgt.extension.internal;

import io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.ConsumerRESTAPIServices;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.service.KeyMgtService;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.service.KeyMgtServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

@Component(
        name = "io.entgra.device.mgt.core.apimgt.keymgt.extension.internal.KeyMgtServiceComponent",
        immediate = true)
public class KeyMgtServiceComponent {

    private static final Log log = LogFactory.getLog(KeyMgtServiceComponent.class);

    @SuppressWarnings("unused")
    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing key management bundle");
            }

            BundleContext bundleContext = componentContext.getBundleContext();

            KeyMgtService keyMgtService = new KeyMgtServiceImpl();
            bundleContext.registerService(KeyMgtService.class.getName(), keyMgtService, null);
            KeyMgtDataHolder.getInstance().setKeyMgtService(keyMgtService);

            if (log.isDebugEnabled()) {
                log.debug("Key management bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing key management bundle", e);
        }
    }

    @SuppressWarnings("unused")
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Key Management Service Component");
        }
    }

    /**
     * Sets APIM Consumer REST API service.
     *
     * @param consumerRESTAPIServices An instance of ConsumerRESTAPIServices
     */
    @Reference(
            name = "APIM.consumer.service",
            service = io.entgra.device.mgt.core.apimgt.extension.rest.api.ConsumerRESTAPIServices.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConsumerRESTAPIServices")
    protected void setConsumerRESTAPIServices(ConsumerRESTAPIServices consumerRESTAPIServices) {
        if (log.isDebugEnabled()) {
            log.debug("Setting APIM Consumer REST API Service");
        }
        KeyMgtDataHolder.getInstance().setConsumerRESTAPIServices(consumerRESTAPIServices);
    }

    /**
     * Unset APIM Consumer REST API service
     *
     * @param consumerRESTAPIServices An instance of ConsumerRESTAPIServices
     */
    protected void unsetConsumerRESTAPIServices(ConsumerRESTAPIServices consumerRESTAPIServices) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting APIM Consumer REST API Service");
        }
        KeyMgtDataHolder.getInstance().setConsumerRESTAPIServices(null);
    }

    /**
     * Sets DCR REST API service.
     *
     * @param apiApplicationServices An instance of APIApplicationServices
     */
    @Reference(
            name = "APIM.application.service",
            service = io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServices.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIApplicationServices")
    protected void setAPIApplicationServices(APIApplicationServices apiApplicationServices) {
        if (log.isDebugEnabled()) {
            log.debug("Setting DCR REST API Service");
        }
        KeyMgtDataHolder.getInstance().setApiApplicationServices(apiApplicationServices);
    }

    /**
     * Unset DCR REST API service
     *
     * @param apiApplicationServices An instance of APIApplicationServices
     */
    protected void unsetAPIApplicationServices(APIApplicationServices apiApplicationServices) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting DCR REST API Service");
        }
        KeyMgtDataHolder.getInstance().setApiApplicationServices(null);
    }
}
