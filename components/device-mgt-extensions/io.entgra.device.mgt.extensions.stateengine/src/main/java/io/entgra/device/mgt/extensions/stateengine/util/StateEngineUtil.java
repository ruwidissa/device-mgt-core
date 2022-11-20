/*
 * Copyright (C) 2018 - 2021 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.extensions.stateengine.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

public class StateEngineUtil {

    public static final String ADMIN_PERMISSION_REGISTRY_PATH = "/permission/admin";
    private static final String CDM_ADMIN_PERMISSION = "/device-mgt/devices/any-device/permitted-actions-under-owning-device";
    private static final Log log = LogFactory.getLog(StateEngineUtil.class);
    private static RealmService realmService;

    public static boolean isAdminUser() throws UserStoreException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String userName = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        UserRealm userRealm = getRealmService().getTenantUserRealm(tenantId);
        if (userRealm != null && userRealm.getAuthorizationManager() != null) {
            return userRealm.getAuthorizationManager()
                    .isUserAuthorized(removeTenantDomain(userName),
                            getAbsolutePermissionPath(CDM_ADMIN_PERMISSION),
                            CarbonConstants.UI_PERMISSION_ACTION);
        }
        return false;
    }

    public static String getAbsolutePermissionPath(String permissionPath) {
        return ADMIN_PERMISSION_REGISTRY_PATH + permissionPath;
    }

    private static String removeTenantDomain(String username) {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (username.endsWith(tenantDomain)) {
            return username.substring(0, username.lastIndexOf("@"));
        }
        return username;
    }

    private static RealmService getRealmService() {
        if (realmService == null) {
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
            if (realmService == null) {
                String msg = "Realm service has not initialized.";
                log.error(msg);
                throw new IllegalStateException(msg);
            }
        }
        return realmService;
    }

}
