/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package io.entgra.device.mgt.core.identity.jwt.client.extension.internal;

import io.entgra.device.mgt.core.identity.jwt.client.extension.exception.JWTClientConfigurationException;
import io.entgra.device.mgt.core.identity.jwt.client.extension.service.JWTClientManagerService;
import io.entgra.device.mgt.core.identity.jwt.client.extension.service.JWTClientManagerServiceImpl;
import io.entgra.device.mgt.core.identity.jwt.client.extension.util.JWTClientUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.IOException;

@Component(
        name = "io.entgra.device.mgt.core.identity.jwt.client.extension.internal.JWTClientExtensionServiceComponent",
        immediate = true)
public class JWTClientExtensionServiceComponent {

    private static Log log = LogFactory.getLog(JWTClientExtensionServiceComponent.class);

    //    private ServiceRegistration serviceRegistration = null;
    @Activate
    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing jwt extension bundle");
        }
        try {
            JWTClientManagerService jwtClientManagerService = new JWTClientManagerServiceImpl();
            JWTClientUtil.initialize(jwtClientManagerService);
            BundleContext bundleContext = componentContext.getBundleContext();
            bundleContext.registerService(JWTClientManagerService.class.getName(), jwtClientManagerService, null);
        } catch (RegistryException e) {
            log.error("Failed loading the jwt config from registry.", e);
        } catch (IOException e) {
            log.error("Failed loading the jwt config from the file system.", e);
        } catch (JWTClientConfigurationException e) {
            log.error("Failed to set default jwt configurations.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.info("deactivating jwt extension bundle");
        }
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
        JWTClientExtensionDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        JWTClientExtensionDataHolder.getInstance().setRegistryService(null);
    }

    @Reference(
            name = "tenant.registry.loader",
            service = org.wso2.carbon.registry.core.service.TenantRegistryLoader.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTenantRegistryLoader")
    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        JWTClientExtensionDataHolder.getInstance().setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        JWTClientExtensionDataHolder.getInstance().setTenantRegistryLoader(null);
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
        JWTClientExtensionDataHolder.getInstance().setIndexLoaderService(indexLoader);
    }

    protected void unsetIndexLoader(TenantIndexingLoader indexLoader) {
        JWTClientExtensionDataHolder.getInstance().setIndexLoaderService(null);
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
        JWTClientExtensionDataHolder.getInstance().setRealmService(realmService);
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
        JWTClientExtensionDataHolder.getInstance().setRealmService(null);
    }
}
