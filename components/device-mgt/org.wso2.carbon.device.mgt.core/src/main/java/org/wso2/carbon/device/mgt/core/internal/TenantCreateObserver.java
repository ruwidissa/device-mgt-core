/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants.User;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Load configuration files to tenant's registry.
 */
public class TenantCreateObserver extends AbstractAxis2ConfigurationContextObserver {
    private static final Log log = LogFactory.getLog(TenantCreateObserver.class);

    /**
     * Create configuration context.
     *
     * @param configurationContext {@link ConfigurationContext} object
     */
    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            //Add the devicemgt-user and devicemgt-admin roles if not exists.
            UserRealm userRealm = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
            UserStoreManager userStoreManager =
                    DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                            .getUserStoreManager();
            AuthorizationManager authorizationManager = DeviceManagementDataHolder.getInstance().getRealmService()
                    .getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).getAuthorizationManager();

            String tenantAdminName = userRealm.getRealmConfiguration().getAdminUserName();

            if (!userStoreManager.isExistingRole(DeviceManagementConstants.User.DEFAULT_DEVICE_ADMIN)) {
                userStoreManager.addRole(
                        DeviceManagementConstants.User.DEFAULT_DEVICE_ADMIN,
                        null,
                        DeviceManagementConstants.User.PERMISSIONS_FOR_DEVICE_ADMIN);
            } else {
                for (Permission permission : DeviceManagementConstants.User.PERMISSIONS_FOR_DEVICE_ADMIN) {
                    authorizationManager.authorizeRole(DeviceManagementConstants.User.DEFAULT_DEVICE_ADMIN,
                            permission.getResourceId(), permission.getAction());
                }
            }
            if (!userStoreManager.isExistingRole(DeviceManagementConstants.User.DEFAULT_DEVICE_USER)) {
                userStoreManager.addRole(
                        DeviceManagementConstants.User.DEFAULT_DEVICE_USER,
                        null,
                        DeviceManagementConstants.User.PERMISSIONS_FOR_DEVICE_USER);
            } else {
                for (Permission permission : DeviceManagementConstants.User.PERMISSIONS_FOR_DEVICE_USER) {
                    authorizationManager.authorizeRole(DeviceManagementConstants.User.DEFAULT_DEVICE_USER,
                            permission.getResourceId(), permission.getAction());
                }
            }
            userStoreManager.updateRoleListOfUser(tenantAdminName, null,
                    new String[] {DeviceManagementConstants.User.DEFAULT_DEVICE_ADMIN,
                            DeviceManagementConstants.User.DEFAULT_DEVICE_USER});
            if (log.isDebugEnabled()) {
                log.debug("Device management roles: " + User.DEFAULT_DEVICE_USER + ", " + User.DEFAULT_DEVICE_ADMIN +
                                  " created for the tenant:" + tenantDomain + "."
                );
                log.debug("Tenant administrator: " + tenantAdminName + "@" + tenantDomain +
                                  " is assigned to the role:" + User.DEFAULT_DEVICE_ADMIN + "."
                );
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while creating roles for the tenant: " + tenantDomain + ".");
        }
    }
}