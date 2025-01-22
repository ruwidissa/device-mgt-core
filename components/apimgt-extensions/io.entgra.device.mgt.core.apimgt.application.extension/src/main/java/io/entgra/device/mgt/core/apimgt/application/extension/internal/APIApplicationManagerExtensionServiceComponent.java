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
package io.entgra.device.mgt.core.apimgt.application.extension.internal;

import io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.ConsumerRESTAPIServices;
import io.entgra.device.mgt.core.apimgt.application.extension.APIManagementProviderService;
import io.entgra.device.mgt.core.apimgt.application.extension.APIManagementProviderServiceImpl;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.IOAuthClientService;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.core.service.RealmService;

@Component(
        name = "io.entgra.device.mgt.core.apimgt.application.extension.internal.APIApplicationManagerExtensionServiceComponent",
        immediate = true)
public class APIApplicationManagerExtensionServiceComponent {

    private static final Log log = LogFactory.getLog(APIApplicationManagerExtensionServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing device extension bundle");
            }
            APIManagementProviderService apiManagementProviderService = new APIManagementProviderServiceImpl();
            APIApplicationManagerExtensionDataHolder.getInstance().setAPIManagementProviderService(apiManagementProviderService);
            BundleContext bundleContext = componentContext.getBundleContext();
            bundleContext.registerService(APIManagementProviderService.class.getName(), apiManagementProviderService, null);
        }  catch (Throwable e) {
            log.error("Error occurred while initializing API application management extension bundle", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    @Reference(
            name = "tenant.registry.loader",
            service = org.wso2.carbon.registry.core.service.TenantRegistryLoader.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTenantRegistryLoader")
    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        APIApplicationManagerExtensionDataHolder.getInstance().setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        APIApplicationManagerExtensionDataHolder.getInstance().setTenantRegistryLoader(null);
    }

    @Reference(
            name = "tenant.index.loader",
            service = org.wso2.carbon.registry.indexing.service.TenantIndexingLoader.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIndexLoader")
    protected void setIndexLoader(TenantIndexingLoader indexLoader) {
        if (indexLoader != null && log.isDebugEnabled()) {
            log.debug("IndexLoader service initialized");
        }
        APIApplicationManagerExtensionDataHolder.getInstance().setIndexLoaderService(indexLoader);
    }

    protected void unsetIndexLoader(TenantIndexingLoader indexLoader) {
        APIApplicationManagerExtensionDataHolder.getInstance().setIndexLoaderService(null);
    }

    /**
     * Sets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    @Reference(
            name = "realm.service",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Realm Service");
        }
        APIApplicationManagerExtensionDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Unsets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Realm Service");
        }
        APIApplicationManagerExtensionDataHolder.getInstance().setRealmService(null);
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
        APIApplicationManagerExtensionDataHolder.getInstance().setConsumerRESTAPIServices(consumerRESTAPIServices);
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
        APIApplicationManagerExtensionDataHolder.getInstance().setConsumerRESTAPIServices(null);
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
        APIApplicationManagerExtensionDataHolder.getInstance().setApiApplicationServices(apiApplicationServices);
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
        APIApplicationManagerExtensionDataHolder.getInstance().setApiApplicationServices(null);
    }

    /**
     * Sets Meta Data Mgt service.
     *
     * @param metadataManagementService An instance of MetadataManagementService
     */
    @Reference(
            name = "meta.data.mgt.service",
            service = io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAMetaMgtServices")
    protected void setMetaMgtServices(MetadataManagementService metadataManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Meta data mgt Service");
        }
        APIApplicationManagerExtensionDataHolder.getInstance().setMetadataManagementService(metadataManagementService);
    }

    /**
     * Unset Meta Data Mgt service
     *
     * @param metadataManagementService An instance of MetadataManagementService
     */
    protected void unsetAMetaMgtServices(MetadataManagementService metadataManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Meta Data mgt Service");
        }
        APIApplicationManagerExtensionDataHolder.getInstance().setMetadataManagementService(null);
    }

    /**
     * Sets IOAuthclientService.
     *
     * @param ioAuthClientService An instance of {@link IOAuthClientService}
     */
    @Reference(
            name = "APIM.application.oauth.client.service",
            service = io.entgra.device.mgt.core.apimgt.extension.rest.api.IOAuthClientService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIApplicationServices")
    protected void setIOAuthClientService(IOAuthClientService ioAuthClientService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting IOAuthclientService.");
        }
        APIApplicationManagerExtensionDataHolder.getInstance().setIoAuthClientService(ioAuthClientService);
    }

    /**
     * Unset IOAuthclientService.
     *
     * @param ioAuthClientService An instance of {@link IOAuthClientService}
     */
    protected void unsetAPIApplicationServices(IOAuthClientService ioAuthClientService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting DCR REST API Service");
        }
        APIApplicationManagerExtensionDataHolder.getInstance().setIoAuthClientService(null);
    }
}
