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
package io.entgra.device.mgt.core.tenant.mgt.core.internal;

import io.entgra.device.mgt.core.application.mgt.common.services.ApplicationManager;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.WhiteLabelManagementService;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.WhiteLabelManagementServiceImpl;
import io.entgra.device.mgt.core.tenant.mgt.common.spi.TenantManagerService;
import io.entgra.device.mgt.core.tenant.mgt.core.TenantManager;
import io.entgra.device.mgt.core.tenant.mgt.core.impl.TenantManagerImpl;
import io.entgra.device.mgt.core.tenant.mgt.core.impl.TenantManagerServiceImpl;
import io.entgra.device.mgt.core.tenant.mgt.core.listener.DeviceMgtTenantListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.service.RealmService;

@Component(
        name = "io.entgra.device.mgt.core.tenant.mgt.core.internal.TenantMgtServiceComponent",
        immediate = true)
@SuppressWarnings("unused")
public class TenantMgtServiceComponent {

    private static final Log log = LogFactory.getLog(TenantManagerService.class);

    @SuppressWarnings("unused")
    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            TenantManagerService tenantManagerService = new TenantManagerServiceImpl();
            componentContext.getBundleContext().
                    registerService(TenantManagerServiceImpl.class.getName(), tenantManagerService, null);
            TenantManager tenantManager = new TenantManagerImpl();
            TenantMgtDataHolder.getInstance().setTenantManager(tenantManager);
            WhiteLabelManagementService whiteLabelManagementService = new WhiteLabelManagementServiceImpl();
            componentContext.getBundleContext().registerService(WhiteLabelManagementServiceImpl.class.getName(),
                    whiteLabelManagementService, null);
            TenantMgtDataHolder.getInstance().setWhiteLabelManagementService(whiteLabelManagementService);
            DeviceMgtTenantListener deviceMgtTenantListener = new DeviceMgtTenantListener();
            if(log.isDebugEnabled()) {
                log.info("Tenant management listener is registering");
            }
            componentContext.getBundleContext().
                    registerService(TenantMgtListener.class.getName(), deviceMgtTenantListener, null);
            if(log.isDebugEnabled()) {
                log.info("Tenant management service activated");
            }
        } catch (Throwable t) {
            String msg = "Error occurred while activating tenant management service";
            log.error(msg, t);
        }
    }

    @SuppressWarnings("unused")
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        // nothing to do
    }

    @Reference(
            name = "application.mgr",
            service = io.entgra.device.mgt.core.application.mgt.common.services.ApplicationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationManager")
    protected void setApplicationManager(ApplicationManager applicationManager) {
        if(log.isDebugEnabled()) {
            log.info("Application manager service is binding");
        }
        TenantMgtDataHolder.getInstance().setApplicationManager(applicationManager);
    }

    protected void unsetApplicationManager(ApplicationManager applicationManager) {
        if(log.isDebugEnabled()) {
            log.info("Application manager service is unbinding");
        }
        TenantMgtDataHolder.getInstance().setApplicationManager(null);
    }

    @Reference(
            name = "realm.service",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        if(log.isDebugEnabled()) {
            log.info("Realm Service service is binding");
        }
        TenantMgtDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        if(log.isDebugEnabled()) {
            log.info("Realm Service service is unbinding");
        }
        TenantMgtDataHolder.getInstance().setRealmService(null);
    }
}
