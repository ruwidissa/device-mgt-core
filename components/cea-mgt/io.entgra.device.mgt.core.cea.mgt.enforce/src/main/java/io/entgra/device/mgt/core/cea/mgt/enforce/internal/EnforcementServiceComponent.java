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
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="io.entgra.device.mgt.core.cea.mgt.enforcementServiceManager" immediate="true"
 * @scr.reference name="org.wso2.carbon.device.manager"
 * interface="io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDeviceManagementServiceProviderService"
 * unbind="unsetDeviceManagementServiceProviderService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 */

public class EnforcementServiceComponent {
    private static final Log log = LogFactory.getLog(EnforcementServiceComponent.class);

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
