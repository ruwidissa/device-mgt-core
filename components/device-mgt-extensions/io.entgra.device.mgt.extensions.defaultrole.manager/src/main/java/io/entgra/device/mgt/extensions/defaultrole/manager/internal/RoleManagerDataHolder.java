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
package io.entgra.device.mgt.extensions.defaultrole.manager.internal;

import io.entgra.device.mgt.extensions.defaultrole.manager.DefaultRolesConfigManager;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class RoleManagerDataHolder {

    private ConfigurationContextService configurationContextService;
    private RealmService realmService;
    private DefaultRolesConfigManager defaultRolesConfigManager;

    private static final RoleManagerDataHolder thisInstance = new RoleManagerDataHolder();

    private RoleManagerDataHolder() {}

    public static RoleManagerDataHolder getInstance() {
        return thisInstance;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public UserStoreManager getUserStoreManager() throws UserStoreException {
        if (realmService == null) {
            String msg = "Realm service has not initialized.";
            throw new IllegalStateException(msg);
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        return realmService.getTenantUserRealm(tenantId).getUserStoreManager();
    }

    public UserRealm getUserRealm() throws UserStoreException {
        UserRealm realm;
        if (realmService == null) {
            throw new IllegalStateException("Realm service not initialized");
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        realm = realmService.getTenantUserRealm(tenantId);
        return realm;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public DefaultRolesConfigManager getDefaultRolesConfigManager() {
        return defaultRolesConfigManager;
    }

    public void setDefaultRolesConfigManager(DefaultRolesConfigManager defaultRolesConfigManager) {
        this.defaultRolesConfigManager = defaultRolesConfigManager;
    }

}
