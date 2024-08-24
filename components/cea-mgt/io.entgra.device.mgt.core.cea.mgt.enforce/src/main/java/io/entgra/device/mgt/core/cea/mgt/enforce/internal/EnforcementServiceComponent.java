/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.enforce.internal;

import io.entgra.device.mgt.core.cea.mgt.common.service.EnforcementServiceManager;
import io.entgra.device.mgt.core.cea.mgt.enforce.Impl.EnforcementServiceManagerImpl;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.user.core.service.RealmService;

@Component(
        name = "io.entgra.device.mgt.core.cea.mgt.enforcementServiceManager",
        immediate = true)
public class EnforcementServiceComponent {
    private static final Log log = LogFactory.getLog(EnforcementServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            EnforcementServiceManager enforcementServiceManager = new EnforcementServiceManagerImpl();
            componentContext.getBundleContext().registerService(EnforcementServiceManager.class.getName(),
                    enforcementServiceManager, null);
            if (log.isDebugEnabled()) {
                log.debug("Enforcement service manager initialized");
            }
        } catch (Throwable t) {
            String msg = "Error occurred while activating " + EnforcementServiceComponent.class.getName();
            log.error(msg, t);
        }
    }

    @Reference(
            name = "org.wso2.carbon.device.manager",
            service = io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            bind = "setDeviceManagementServiceProviderService",
            unbind = "unsetDeviceManagementServiceProviderService")
    protected void setDeviceManagementServiceProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        EnforcementServiceComponentDataHolder.getInstance().setDeviceManagementProviderService(deviceManagementProviderService);
        if (log.isDebugEnabled()) {
            log.debug("Device management provider service is set successfully");
        }
    }

    protected void unsetDeviceManagementServiceProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        EnforcementServiceComponentDataHolder.getInstance().setDeviceManagementProviderService(null);
        if (log.isDebugEnabled()) {
            log.debug("Device management provider service is unset successfully");
        }
    }

    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            bind = "setRealmService",
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        EnforcementServiceComponentDataHolder.getInstance().setRealmService(realmService);
        if (log.isDebugEnabled()) {
            log.debug("Realm service is set successfully");
        }
    }

    protected void unsetRealmService(RealmService realmService) {
        EnforcementServiceComponentDataHolder.getInstance().setRealmService(null);
        if (log.isDebugEnabled()) {
            log.debug("Realm service is unset successfully");
        }
    }
}
