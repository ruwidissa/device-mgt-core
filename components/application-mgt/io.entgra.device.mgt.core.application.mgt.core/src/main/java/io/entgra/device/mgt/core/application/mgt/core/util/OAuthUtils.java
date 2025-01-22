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

package io.entgra.device.mgt.core.application.mgt.core.util;

import io.entgra.device.mgt.core.apimgt.application.extension.APIManagementProviderService;
import io.entgra.device.mgt.core.apimgt.application.extension.bean.ApiApplicationProfile;
import io.entgra.device.mgt.core.apimgt.application.extension.bean.ApiApplicationKey;
import io.entgra.device.mgt.core.apimgt.application.extension.exception.APIManagerException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import io.entgra.device.mgt.core.application.mgt.common.dto.ApiRegistrationProfile;
import io.entgra.device.mgt.core.identity.jwt.client.extension.JWTClient;
import io.entgra.device.mgt.core.identity.jwt.client.extension.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.identity.jwt.client.extension.exception.JWTClientException;
import io.entgra.device.mgt.core.identity.jwt.client.extension.service.JWTClientManagerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class OAuthUtils {

    private static final Log log = LogFactory.getLog(OAuthUtils.class);

    public static ApiApplicationKey getClientCredentials(String tenantDomain)
            throws UserStoreException, APIManagerException {
        ApiRegistrationProfile registrationProfile = new ApiRegistrationProfile();
        registrationProfile.setApplicationName(Constants.ApplicationInstall.APPLICATION_NAME);
        registrationProfile.setTags(new String[]{Constants.ApplicationInstall.DEVICE_TYPE_ANDROID});
        registrationProfile.setAllowedToAllDomains(false);
        registrationProfile.setMappingAnExistingOAuthApp(false);
        return getCredentials(registrationProfile, tenantDomain);
    }

    public static ApiApplicationKey getCredentials(ApiRegistrationProfile registrationProfile, String tenantDomain)
            throws UserStoreException, APIManagerException {
        ApiApplicationKey apiApplicationKeyInfo;
        if (tenantDomain == null || tenantDomain.isEmpty()) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                    .getRealmConfiguration().getAdminUserName();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            APIManagementProviderService apiManagementProviderService = (APIManagementProviderService) ctx.
                    getOSGiService(APIManagementProviderService.class, null);

            ApiApplicationProfile apiApplicationProfile = new ApiApplicationProfile();
            apiApplicationProfile.setApplicationName(registrationProfile.getApplicationName());
            apiApplicationProfile.setTags(registrationProfile.getTags());
            apiApplicationProfile.setGrantTypes("refresh_token client_credentials password");
            apiApplicationKeyInfo = apiManagementProviderService.
                    registerApiApplication(apiApplicationProfile);
        } catch (BadRequestException | UnexpectedResponseException  e) {
            String msg = "Error encountered while registering api application";
            log.error(msg);
            throw new APIManagerException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return apiApplicationKeyInfo;
    }

    public static AccessTokenInfo getOAuthCredentials(ApiApplicationKey apiApplicationKey, String username)
            throws APIManagerException {
        try {
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            JWTClientManagerService jwtClientManagerService = (JWTClientManagerService) ctx.
                    getOSGiService(JWTClientManagerService.class, null);
            JWTClient jwtClient = jwtClientManagerService.getJWTClient();
            return jwtClient.getAccessToken(apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret(),
                    username, Constants.ApplicationInstall.SUBSCRIPTION_SCOPE);
        } catch (JWTClientException e) {
            String errorMsg = "Error while generating an OAuth token for user " + username;
            log.error(errorMsg, e);
            throw new APIManagerException(errorMsg, e);
        }
    }

}
