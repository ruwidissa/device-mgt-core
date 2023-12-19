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
package io.entgra.device.mgt.core.device.mgt.core.internal;

import io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServicesImpl;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.PublisherRESTAPIServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.PublisherRESTAPIServicesImpl;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Scope;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants.User;
import org.wso2.carbon.stratos.common.exception.TenantManagementClientException;
import org.wso2.carbon.tenant.mgt.exception.TenantManagementException;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.security.SecureRandom;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Load configuration files to tenant's registry.
 */
public class TenantCreateObserver extends AbstractAxis2ConfigurationContextObserver {
    private static final Log log = LogFactory.getLog(TenantCreateObserver.class);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();



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

//            String password = this.generateInitialUserPassword();

//            createUserIfNotExists("test_reserved_user", password, userStoreManager);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        createApplication(tenantDomain);
                    } catch (TenantManagementException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.start();


//            executor.submit(() -> {
//                try {
//                    createApplication();
//                } catch (TenantManagementException e) {
//                    throw new RuntimeException(e);
//                }
//            });


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


    private void createApplication(String tenantDomain) throws TenantManagementException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

        PublisherRESTAPIServices publisherRESTAPIServices = new PublisherRESTAPIServicesImpl();
        APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
        APIApplicationKey apiApplicationKey = null;
        AccessTokenInfo accessTokenInfo = null;
        try {
            apiApplicationServices.createAndRetrieveApplicationCredentialsAndGenerateToken();
//            log.error("apiApplicationKey: " + apiApplicationKey.getClientId());
//            log.error("apiApplicationKey: " + apiApplicationKey.getClientSecret());
//            accessTokenInfo = apiApplicationServices.generateAccessTokenFromRegisteredApplication(
//                    apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
        } catch (APIServicesException e) {
            String errorMsg = "Error occurred while generating the API application";
            log.error(errorMsg, e);
            throw new TenantManagementException(errorMsg, e);
        }
//        Scope[] scopes = publisherRESTAPIServices.getScopes(apiApplicationKey, accessTokenInfo);
    }
    private void createUserIfNotExists(String username, String password, UserStoreManager userStoreManager) {

        try {
            if (!userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername(username))) {
                String[] roles = {"admin"};
                userStoreManager.addUser(MultitenantUtils.getTenantAwareUsername(username), password, roles, null, "");

                userStoreManager.updateCredential(MultitenantUtils.getTenantAwareUsername(username), "reservedpwd", password);
            }
        } catch (UserStoreException e) {
            String msg = "Error when trying to fetch tenant details";
            log.error(msg);
        }
    }

    private String generateInitialUserPassword() {
        int passwordLength = 6;
        //defining the pool of characters to be used for initial password generation
        String lowerCaseCharset = "abcdefghijklmnopqrstuvwxyz";
        String upperCaseCharset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numericCharset = "0123456789";
        SecureRandom randomGenerator = new SecureRandom();
        String totalCharset = lowerCaseCharset + upperCaseCharset + numericCharset;
        int totalCharsetLength = totalCharset.length();
        StringBuilder initialUserPassword = new StringBuilder();
        for (int i = 0; i < passwordLength; i++) {
            initialUserPassword.append(
                    totalCharset.charAt(randomGenerator.nextInt(totalCharsetLength)));
        }
        if (log.isDebugEnabled()) {
            log.debug("Initial user password is created for new user: " + initialUserPassword);
        }
        return initialUserPassword.toString();
    }

}
