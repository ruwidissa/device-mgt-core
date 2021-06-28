/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class UserRoleCreateObserver implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(UserRoleCreateObserver.class);
    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        String tenantAdminName = "admin";

        try {
            UserStoreManager userStoreManager =
                    DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(
                            MultitenantConstants.SUPER_TENANT_ID).getUserStoreManager();
            userStoreManager.addRole(
                    DeviceManagementConstants.User.DEFAULT_DEVICE_ADMIN,
                    new String[]{tenantAdminName},
                    DeviceManagementConstants.User.PERMISSIONS_FOR_DEVICE_ADMIN);
            userStoreManager.addRole(
                    DeviceManagementConstants.User.DEFAULT_DEVICE_USER,
                    new String[]{tenantAdminName},
                    DeviceManagementConstants.User.PERMISSIONS_FOR_DEVICE_USER);

            if (log.isDebugEnabled()) {
                log.debug("Device management roles: " + DeviceManagementConstants.User.DEFAULT_DEVICE_USER + ", " +
                                DeviceManagementConstants.User.DEFAULT_DEVICE_ADMIN + " created for the tenant:" + tenantDomain + "."
                );
                log.debug("Tenant administrator: " + tenantAdminName + "@" + tenantDomain +
                                " is assigned to the role:" + DeviceManagementConstants.User.DEFAULT_DEVICE_ADMIN + "."
                );
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while creating roles for the tenant: " + tenantDomain + ".");
        }
    }
}
