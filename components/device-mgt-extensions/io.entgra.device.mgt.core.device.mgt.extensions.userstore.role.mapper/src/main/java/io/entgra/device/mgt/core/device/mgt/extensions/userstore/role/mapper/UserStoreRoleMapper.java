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
package io.entgra.device.mgt.core.device.mgt.extensions.userstore.role.mapper;

import io.entgra.device.mgt.core.device.mgt.extensions.userstore.role.mapper.bean.RoleMapping;
import io.entgra.device.mgt.core.device.mgt.extensions.userstore.role.mapper.bean.UserStoreRoleMappingConfig;
import io.entgra.device.mgt.core.device.mgt.extensions.userstore.role.mapper.internal.UserStoreRoleMappingDataHolder;
import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserStoreRoleMapper implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(UserStoreRoleMapper.class);

    private UserStoreRoleMappingConfig config = null;
    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {

        config = UserStoreRoleMappingDataHolder.getInstance()
                .getUserStoreRoleMappingConfigManager().getUserStoreRoleMappingConfig();

        try {
            if ((config.isEnabled() &&
                    UserStoreRoleMappingDataHolder.getInstance().getHeartBeatService().isTaskPartitioningEnabled() &&
                    UserStoreRoleMappingDataHolder.getInstance().getHeartBeatService().isQualifiedToExecuteTask())
                || (config.isEnabled() &&
                    !UserStoreRoleMappingDataHolder.getInstance().getHeartBeatService().isTaskPartitioningEnabled())) {
                Runnable periodicTask = new Runnable() {
                    public void run() {
                        updateRoleMapping();
                        log.info("UserStoreRoleMapper executed....");
                    }
                };

                ScheduledExecutorService executor =
                        Executors.newSingleThreadScheduledExecutor();

                executor.scheduleAtFixedRate(periodicTask, config.getInitialDelayInSeconds(), config.getPeriodInSeconds(), TimeUnit.SECONDS);
            }
        } catch (HeartBeatManagementException e) {
            log.error("Error while accessing heart beat service " + e.getMessage());
        }
    }

    private void updateRoleMapping() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                    MultitenantConstants.SUPER_TENANT_ID);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

            List<RoleMapping> roleMappings = config.getMappings();

            if (!roleMappings.isEmpty()) {
                UserStoreManager userStoreManager =
                        UserStoreRoleMappingDataHolder.getInstance().getRealmService()
                                .getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).getUserStoreManager();

                for (RoleMapping mapping : roleMappings) {
                    if (userStoreManager.isExistingRole(mapping.getSecondaryRole())) {
                        String[] users = userStoreManager.getUserListOfRole(mapping.getSecondaryRole());
                        if (users != null && users.length > 0) {
                            List<String> primaryRoles = mapping.getInternalRoles();
                            for (String role : primaryRoles) {
                                if (userStoreManager.isExistingRole(role)) {
                                    String[] existingUsers = userStoreManager.getUserListOfRole(role);
                                    List<String> existingUserList = new ArrayList<>(Arrays.asList(existingUsers));
                                    List<String> newUserList = new ArrayList<>();
                                    for (String user : users) {
                                        if (existingUserList.contains(user)) {
                                            // if contains, remove from existing list
                                            existingUserList.remove(user);
                                        } else {
                                            // new user
                                            newUserList.add(user);
                                        }
                                    }

                                    List<String> deleteUserList = new ArrayList<>();
                                    if (!existingUserList.isEmpty()) {
                                        String domain = mapping.getSecondaryRole().substring(0, mapping.getSecondaryRole().indexOf("/"));
                                        for (String user : existingUserList) {
                                            if (user.startsWith(domain.toUpperCase())) {
                                                deleteUserList.add(user);
                                            }
                                        }
                                    }

                                    // update user list of given role
                                    if (!newUserList.isEmpty() || !deleteUserList.isEmpty()) {
                                        userStoreManager.updateUserListOfRole(role, deleteUserList.toArray(new String[0]), newUserList.toArray(new String[0]));
                                        log.info("update user role mapping executed.....");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (UserStoreException e) {
            log.error("Error while getting user store..." + e.getMessage());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
