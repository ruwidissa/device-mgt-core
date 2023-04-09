package io.entgra.tenant.mgt.core.impl;

import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.InvalidConfigurationException;
import io.entgra.application.mgt.core.config.ConfigurationManager;
import io.entgra.application.mgt.common.services.ApplicationManager;
import io.entgra.tenant.mgt.core.TenantManager;
import io.entgra.tenant.mgt.common.exception.TenantMgtException;
import io.entgra.tenant.mgt.core.internal.TenantMgtDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.permission.mgt.PermissionUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagementException;
import org.wso2.carbon.device.mgt.common.roles.config.Role;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TenantManagerImpl implements TenantManager {
    private static final Log log = LogFactory.getLog(TenantManagerImpl.class);
    private static final String PERMISSION_ACTION = "ui.execute";

    @Override
    public void addDefaultRoles(TenantInfoBean tenantInfoBean) throws TenantMgtException {
        initTenantFlow(tenantInfoBean);
        DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        if (config.getDefaultRoles().isEnabled()) {
            Map<String, List<Permission>> roleMap = getValidRoleMap(config);
            try {
                UserStoreManager userStoreManager = TenantMgtDataHolder.getInstance().getRealmService()
                        .getTenantUserRealm(tenantInfoBean.getTenantId()).getUserStoreManager();

                roleMap.forEach((key, value) -> {
                    try {
                        userStoreManager.addRole(key, null, value.toArray(new Permission[0]));
                    } catch (UserStoreException e) {
                        log.error("Error occurred while adding default roles into user store", e);
                    }
                });
            } catch (UserStoreException e) {
                String msg = "Error occurred while getting user store manager";
                log.error(msg, e);
                throw new TenantMgtException(msg, e);
            }
        }
        try {
            TenantMgtDataHolder.getInstance().getWhiteLabelManagementService().
                    addDefaultWhiteLabelThemeIfNotExist(tenantInfoBean.getTenantId());
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while adding default white label theme to created tenant - id "+tenantInfoBean.getTenantId();
            log.error(msg, e);
            throw new TenantMgtException(msg, e);
        } finally {
            endTenantFlow();
        }
    }

    @Override
    public void addDefaultAppCategories(TenantInfoBean tenantInfoBean) throws TenantMgtException {
        initTenantFlow(tenantInfoBean);
        try {
            ApplicationManager applicationManager = TenantMgtDataHolder.getInstance().getApplicationManager();
            applicationManager
                    .addApplicationCategories(ConfigurationManager.getInstance().getConfiguration().getAppCategories());
        } catch (InvalidConfigurationException e) {
            String msg = "Error occurred while getting application manager";
            throw new TenantMgtException(msg, e);
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting default application categories";
            log.error(msg, e);
            throw new TenantMgtException(msg, e);
        } finally {
            endTenantFlow();
        }

    }

    private void initTenantFlow(TenantInfoBean tenantInfoBean) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(tenantInfoBean.getTenantId());
        privilegedCarbonContext.setTenantDomain(tenantInfoBean.getTenantDomain());
    }

    private void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
    }

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
