/*
 * Copyright (C) 2018 - 2022 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.extensions.defaultrole.manager;

import io.entgra.device.mgt.extensions.defaultrole.manager.bean.DefaultRolesConfig;
import io.entgra.device.mgt.extensions.defaultrole.manager.bean.Role;
import io.entgra.device.mgt.extensions.defaultrole.manager.internal.RoleManagerDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

public class IoTSStartupHandler implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(IoTSStartupHandler.class);

    @Override
    public void completingServerStartup() {
    }

    @Override
    public void completedServerStartup() {
        DefaultRolesConfig defaultRolesConfig = RoleManagerDataHolder.getInstance()
                .getDefaultRolesConfigManager().getDefaultRolesConfig();
        UserStoreManager userStoreManager;
        try {
            userStoreManager = RoleManagerDataHolder.getInstance().getUserStoreManager();
        } catch (UserStoreException e) {
            log.error("Unable to retrieve user store manager");
            return;
        }
        for (Role role : defaultRolesConfig.getRoles()) {
            try {
                if (userStoreManager.isExistingRole(role.getName())) {
                    updatePermissions(role);
                } else {
                    try {
                        addRole(role);
                    } catch (UserStoreException e) {
                        log.error("Error occurred when adding new role: " + role.getName(), e);
                    }
                }
            } catch (UserStoreException e) {
                log.error("Error occurred when checking the existence of role: " + role.getName(), e);
            }
        }
    }

    private void updatePermissions(Role role) throws UserStoreException {
        AuthorizationManager authorizationManager = RoleManagerDataHolder.getInstance().getUserRealm()
                .getAuthorizationManager();
        if (log.isDebugEnabled()) {
            log.debug("Updating the role '" + role.getName() + "'");
        }
        if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
            authorizationManager.clearRoleAuthorization(role.getName());
            for (String permission : role.getPermissions()) {
                authorizationManager.authorizeRole(role.getName(), permission, CarbonConstants.UI_PERMISSION_ACTION);
            }
        }
    }

    private void addRole(Role role) throws UserStoreException {
        UserStoreManager userStoreManager = RoleManagerDataHolder.getInstance().getUserStoreManager();
        if (log.isDebugEnabled()) {
            log.debug("Persisting the role " + role.getName() + " in the underlying user store");
        }
        Permission[] permissions = null;
        if (role.getPermissions() != null) {
            permissions = new Permission[role.getPermissions().size()];
            String permission;
            for (int i = 0; i < permissions.length; i++) {
                permission = role.getPermissions().get(i);
                permissions[i] = new Permission(permission, CarbonConstants.UI_PERMISSION_ACTION);
            }
        }
        userStoreManager.addRole(role.getName(), new String[]{"admin"}, permissions);
    }

}
