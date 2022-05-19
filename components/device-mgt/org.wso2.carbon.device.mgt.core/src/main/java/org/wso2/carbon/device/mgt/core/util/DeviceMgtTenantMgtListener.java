/* Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagementException;
import org.wso2.carbon.device.mgt.common.roles.config.Role;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.permission.mgt.PermissionUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceMgtTenantMgtListener implements TenantMgtListener {
    private static final Log log = LogFactory.getLog(DeviceMgtTenantMgtListener.class);
    private static final int EXEC_ORDER = 10;
    private static final String PERMISSION_ACTION = "ui.execute";

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) {
        DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        if (config.getDefaultRoles().isEnabled()) {
            Map<String, List<Permission>> roleMap = getValidRoleMap(config);
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .setTenantDomain(tenantInfoBean.getTenantDomain(), true);
                UserStoreManager userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService()
                        .getTenantUserRealm(tenantInfoBean.getTenantId()).getUserStoreManager();

                roleMap.forEach((key, value) -> {
                    try {
                        userStoreManager.addRole(key, null, value.toArray(new Permission[0]));
                    } catch (UserStoreException e) {
                        log.error("Error occurred while adding default roles into user store.", e);
                    }
                });
            } catch (UserStoreException e) {
                log.error("Error occurred while getting user store manager.", e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        try {
            DeviceManagementDataHolder.getInstance().getWhiteLabelManagementService().
                    addDefaultWhiteLabelThemeIfNotExist(tenantInfoBean.getTenantId());
        } catch (MetadataManagementException e) {
            log.error("Error occurred while adding default white label theme to created tenant.", e);
        }
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) {
        // doing nothing
    }

    @Override
    public void onTenantDelete(int i) {
        // doing nothing
    }

    @Override
    public void onTenantRename(int i, String s, String s1) {
        // doing nothing
    }

    @Override
    public void onTenantInitialActivation(int i) {
        // doing nothing
    }

    @Override
    public void onTenantActivation(int i) {
        // doing nothing
    }

    @Override
    public void onTenantDeactivation(int i) {
        // doing nothing
    }

    @Override
    public void onSubscriptionPlanChange(int i, String s, String s1) {
        // doing nothing
    }

    @Override
    public int getListenerOrder() {
        return EXEC_ORDER;
    }

    @Override
    public void onPreDelete(int i) {
        // doing nothing
    }

    /**
     * Use the default roles defined in the cdm-config and evaluate the defined permissions. If permissions does not
     * exist then exclude them and return role map which contains defined roles in the cdm-config and existing
     * permission list as a roleMap
     * @param config cdm-config
     * @return {@link Map} key is role name and value is list of permissions which needs to be assigned to the role
     * defined in the key.
     */
    private Map<String, List<Permission>> getValidRoleMap(DeviceManagementConfig config) {
        Map<String, List<Permission>> roleMap = new HashMap<>();
        try {
            for (Role role : config.getDefaultRoles().getRoles()) {
                List<Permission> permissionList = new ArrayList<>();
                for (String permissionPath : role.getPermissions()) {
                    if (PermissionUtils.checkResourceExists(permissionPath)) {
                        Permission permission = new Permission(permissionPath, PERMISSION_ACTION);

                        permissionList.add(permission);
                    } else {
                        log.warn("Permission  " + permissionPath + " does not exist. Hence it will not add to role "
                                + role.getName());
                    }
                }
                roleMap.put(role.getName(), permissionList);
            }
        } catch (PermissionManagementException | RegistryException e) {
            log.error("Error occurred while checking permission existence.", e);
        }
        return roleMap;
    }
}
