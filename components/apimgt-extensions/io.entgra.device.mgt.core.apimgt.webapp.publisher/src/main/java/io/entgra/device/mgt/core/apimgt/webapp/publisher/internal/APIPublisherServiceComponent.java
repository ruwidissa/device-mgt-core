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
package io.entgra.device.mgt.core.apimgt.webapp.publisher.internal;

import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIPublisherService;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIPublisherServiceImpl;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIPublisherStartupHandler;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.config.WebappPublisherConfig;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;

@Component(
        name = "io.entgra.device.mgt.core.apimgt.webapp.publisher.internal.APIPublisherServiceComponent",
        immediate = true)
public class APIPublisherServiceComponent {

    private static Log log = LogFactory.getLog(APIPublisherServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing webapp publisher bundle");
            }

            if (log.isDebugEnabled()) {
                log.debug("Loading webapp publisher configurations");
            }
            /* Initializing webapp publisher configuration */
            WebappPublisherConfig.init();

            /* Registering declarative service instances exposed by DeviceManagementServiceComponent */
            this.registerServices(componentContext);
            APIPublisherDataHolder.getInstance().setPermScopeMapping(new HashMap<>());

            if (log.isDebugEnabled()) {
                log.debug("Webapp publisher bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing webapp publisher bundle", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    private void registerServices(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Registering OSGi service DeviceManagementProviderServiceImpl");
        }
        /* Registering Device Management Service */
        BundleContext bundleContext = componentContext.getBundleContext();

        APIPublisherService publisher = new APIPublisherServiceImpl();
        APIPublisherDataHolder.getInstance().setApiPublisherService(publisher);
        bundleContext.registerService(APIPublisherService.class, publisher, null);
        bundleContext.registerService(ServerStartupObserver.class, new APIPublisherStartupHandler(), null);
    }

    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Realm Service");
        }
        APIPublisherDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Realm Service");
        }
        APIPublisherDataHolder.getInstance().setRealmService(null);
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry service initialized");
        }
        APIPublisherDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        APIPublisherDataHolder.getInstance().setRegistryService(null);
    }

    @Reference(
            name = "io.entgra.meta.mgt",
            service = io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMetaDataMgtService")
    protected void setMetaDataMgtService(MetadataManagementService metadataManagementService) {
        if (metadataManagementService != null && log.isDebugEnabled()) {
            log.debug("Meta data mgt mgt service initialized");
        }
        APIPublisherDataHolder.getInstance().setMetadataManagementService(metadataManagementService);
    }

    protected void unsetMetaDataMgtService(MetadataManagementService metadataManagementService) {
        APIPublisherDataHolder.getInstance().setMetadataManagementService(null);
    }

}
