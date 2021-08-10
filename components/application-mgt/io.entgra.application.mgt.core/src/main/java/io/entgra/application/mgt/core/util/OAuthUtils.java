/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.application.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.application.mgt.common.dto.ApiRegistrationProfile;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;
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
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(PrivilegedCarbonContext.
                    getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration().getAdminUserName());
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            APIManagementProviderService apiManagementProviderService = (APIManagementProviderService) ctx.
                    getOSGiService(APIManagementProviderService.class, null);
            apiApplicationKeyInfo = apiManagementProviderService.
                    generateAndRetrieveApplicationKeys(registrationProfile.getApplicationName(),
                            registrationProfile.getTags(), Constants.ApplicationInstall.DEFAULT_TOKEN_TYPE,
                            null, registrationProfile.isAllowedToAllDomains(),
                            Constants.ApplicationInstall.DEFAULT_VALIDITY_PERIOD);
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
            return jwtClient.getAccessToken(apiApplicationKey.getConsumerKey(), apiApplicationKey.getConsumerSecret(),
                    username, Constants.ApplicationInstall.SUBSCRIPTION_SCOPE);
        } catch (JWTClientException e) {
            String errorMsg = "Error while generating an OAuth token for user " + username;
            log.error(errorMsg, e);
            throw new APIManagerException(errorMsg, e);
        }
    }

}
