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
package io.entgra.device.mgt.core.device.mgt.extensions.userstore.role.mapper.internal;

import io.entgra.device.mgt.core.device.mgt.extensions.userstore.role.mapper.UserStoreRoleMappingConfigManager;
import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.service.HeartBeatManagementService;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class UserStoreRoleMappingDataHolder {

    private ConfigurationContextService configurationContextService;
    private RealmService realmService;
    private UserStoreRoleMappingConfigManager userStoreRoleMappingConfigManager;
    private HeartBeatManagementService heartBeatService;
    private static final UserStoreRoleMappingDataHolder thisInstance = new UserStoreRoleMappingDataHolder();

    private UserStoreRoleMappingDataHolder() {}

    public static UserStoreRoleMappingDataHolder getInstance() {
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

    public UserStoreRoleMappingConfigManager getUserStoreRoleMappingConfigManager() {
        return userStoreRoleMappingConfigManager;
    }

    public void setUserStoreRoleMappingConfigManager(UserStoreRoleMappingConfigManager userStoreRoleMappingConfigManager) {
        this.userStoreRoleMappingConfigManager = userStoreRoleMappingConfigManager;
    }

    public HeartBeatManagementService getHeartBeatService() {
        return heartBeatService;
    }

    public void setHeartBeatService(HeartBeatManagementService heartBeatService) {
        this.heartBeatService = heartBeatService;
    }
}
