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

import io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.PublisherRESTAPIServices;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIPublisherService;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIPublisherServiceImpl;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIPublisherStartupHandler;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.config.WebappPublisherConfig;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;

/**
 * @scr.component name="io.entgra.device.mgt.core.apimgt.webapp.publisher" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="APIM.application.service"
 * interface="io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServices"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setAPIApplicationServices"
 * unbind="unsetAPIApplicationServices"
 * @scr.reference name="APIM.publisher.service"
 * interface="io.entgra.device.mgt.core.apimgt.extension.rest.api.PublisherRESTAPIServices"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setPublisherRESTAPIServices"
 * unbind="unsetPublisherRESTAPIServices"
 * @scr.reference name="io.entgra.meta.mgt"
 * interface="io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService"
 * cardinality="0..1"
 * policy="dynamic"
 * bind="setMetaDataMgtService"
 * unbind="unsetMetaDataMgtService"
 */
public class APIPublisherServiceComponent {

    private static Log log = LogFactory.getLog(APIPublisherServiceComponent.class);

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

    protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry service initialized");
        }
        APIPublisherDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        APIPublisherDataHolder.getInstance().setRegistryService(null);
    }

    protected void setAPIApplicationServices(APIApplicationServices apiApplicationServices) {
        if (log.isDebugEnabled()) {
            log.debug("Setting DCR REST API Service");
        }
        APIPublisherDataHolder.getInstance().setApiApplicationServices(apiApplicationServices);
    }

    protected void unsetAPIApplicationServices(APIApplicationServices apiApplicationServices) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting DCR REST API Service");
        }
        APIPublisherDataHolder.getInstance().setApiApplicationServices(null);
    }

    protected void setPublisherRESTAPIServices(PublisherRESTAPIServices publisherRESTAPIServices) {
        if (log.isDebugEnabled()) {
            log.debug("Setting APIM Publisher REST API Service");
        }
        APIPublisherDataHolder.getInstance().setPublisherRESTAPIServices(publisherRESTAPIServices);
    }

    protected void unsetPublisherRESTAPIServices(PublisherRESTAPIServices publisherRESTAPIServices) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting APIM Publisher REST API Service");
        }
        APIPublisherDataHolder.getInstance().setPublisherRESTAPIServices(null);
    }

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
