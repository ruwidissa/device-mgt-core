/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.keymgt.extension;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerDto;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.handlers.DefaultKeyValidationHandler;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.permission.mgt.Permission;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagementException;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagerService;
import org.wso2.carbon.device.mgt.core.permission.mgt.PermissionManagerServiceImpl;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;

public class KeyValidationHandler extends DefaultKeyValidationHandler {

    /* This key validation handler is written extending API Manager's
    * AbstractKeyValidationHandler, which implements KeyValidationHandler
    * where all the methods have been implemented. Since the logic is
    * taken from KeyValidationHandler, the latest logical changes
    * should be monitored and updated here accordingly */

    private static final Log log = LogFactory.getLog(KeyValidationHandler.class);

    public KeyValidationHandler() {
        log.info(this.getClass().getName() + " Initialised");
    }

    @Override
    public boolean validateScopes(TokenValidationContext validationContext) throws APIKeyMgtException {

        if (validationContext.isCacheHit()) {
            return true;
        }
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = validationContext.getValidationInfoDTO();

        if (apiKeyValidationInfoDTO == null) {
            throw new APIKeyMgtException("Key Validation information not set");
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String httpVerb = validationContext.getHttpVerb();
        String[] scopes;
        Set<String> scopesSet = apiKeyValidationInfoDTO.getScopes();
        StringBuilder scopeList = new StringBuilder();

        if (scopesSet != null && !scopesSet.isEmpty()) {
            scopes = scopesSet.toArray(new String[scopesSet.size()]);
            if (log.isDebugEnabled() && scopes != null) {
                for (String scope : scopes) {
                    scopeList.append(scope);
                    scopeList.append(",");
                }
                scopeList.deleteCharAt(scopeList.length() - 1);
                log.debug("Scopes allowed for token : " + validationContext.getAccessToken() + " : "
                        + scopeList.toString());
            }
        }

        String resourceList = validationContext.getMatchingResource();
        List<String> resourceArray;
        if ((APIConstants.GRAPHQL_QUERY.equalsIgnoreCase(validationContext.getHttpVerb()))
                || (APIConstants.GRAPHQL_MUTATION.equalsIgnoreCase(validationContext.getHttpVerb()))
                || (APIConstants.GRAPHQL_SUBSCRIPTION.equalsIgnoreCase(validationContext.getHttpVerb()))) {
            resourceArray = new ArrayList<>(Arrays.asList(resourceList.split(",")));
        } else {
            resourceArray = new ArrayList<>(Arrays.asList(resourceList));
        }

        String actualVersion = validationContext.getVersion();
        //Check if the api version has been prefixed with _default_
        if (actualVersion != null && actualVersion.startsWith(APIConstants.DEFAULT_VERSION_PREFIX)) {
            //Remove the prefix from the version.
            actualVersion = actualVersion.split(APIConstants.DEFAULT_VERSION_PREFIX)[1];
        }
        SubscriptionDataStore tenantSubscriptionStore =
                SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        API api = tenantSubscriptionStore.getApiByContextAndVersion(validationContext.getContext(),
                actualVersion);
        boolean scopesValidated = false;
        if (api != null) {

            for (String resource : resourceArray) {
                List<URLMapping> resources = api.getResources();
                URLMapping urlMapping = null;
                for (URLMapping mapping : resources) {
                    if (Objects.equals(mapping.getHttpMethod(), httpVerb) || "WS".equalsIgnoreCase(api.getApiType())) {
                        if (isResourcePathMatching(resource, mapping)) {
                            urlMapping = mapping;
                            break;
                        }
                    }
                }
                if (urlMapping != null) {
                    if (urlMapping.getScopes().size() == 0) {
                        scopesValidated = true;
                        continue;
                    }
                    List<String> mappingScopes = urlMapping.getScopes();
                    boolean validate = false;
                    for (String scope : mappingScopes) {
                        if (scopesSet.contains(scope)) {
                            scopesValidated = true;
                            validate = true;
                            break;
                        }
                        try {
                            validate = scopesValidated = authorizePermissions(validationContext);
                            break;
                        } catch (UserStoreException e) {
                            String msg = "Error occurred while validating user permissions";
                            log.error(msg, e);
                            throw new APIKeyMgtException(msg);
                        }
                    }
                    if (!validate && urlMapping.getScopes().size() > 0) {
                        break;
                    }
                }
            }
        }
        if (!scopesValidated) {
            apiKeyValidationInfoDTO.setAuthorized(false);
            apiKeyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.INVALID_SCOPE);
        }
        return scopesValidated;
    }

    /**
     * Authorizes the permissions of a user for a given context
     *
     * @param validationContext token validation context object
     * @return returns whether a user is authorized
     * @throws UserStoreException throws  if an error occurs while getting the tenant user realm
     * */
    private boolean authorizePermissions(TokenValidationContext validationContext) throws UserStoreException {
        PrivilegedCarbonContext context = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String username;

        RealmService realmService = (RealmService) context.getOSGiService(RealmService.class, null);
        UserRealm userRealm = realmService.getTenantUserRealm(PrivilegedCarbonContext
                .getThreadLocalCarbonContext().getTenantId());;
        AccessTokenInfo accessTokenInfo;
        try {
            accessTokenInfo = getAccessTokenInfo(validationContext);
        } catch (APIManagementException e) {
            log.error("Error occurred while getting access token info");
            return false;
        }
        username = accessTokenInfo.getEndUserName();
        String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(username);

        List<Permission> matchingPermissions;
        StringBuilder ctx = new StringBuilder();
        try {
            PermissionManagerService permissionManagerService = PermissionManagerServiceImpl.getInstance();
            String[] ctxArr = validationContext.getContext().split("/");
            for (String c : ctxArr) {
                if (c.matches("[v|V]\\d{1,3}\\.\\d{1,3}"))
                ctx.append(c);
            }
            ctx = new StringBuilder(ctxArr[0] + "/" + ctxArr[1] + "/" + ctxArr[2] + "/" + ctxArr[3]);
            matchingPermissions = permissionManagerService.getPermission(ctx.toString());
        } catch (PermissionManagementException e) {
            log.error("Error occurred while fetching permissions for context " + ctx, e);
            return false;
        }

        String requestUri = validationContext.getContext();
        String requestMethod = validationContext.getHttpVerb();
        String contextPath = ctx.toString();

        if (matchingPermissions == null) {
            if (log.isDebugEnabled()) {
                log.debug("Permission to request '" + requestUri + "' is not defined in the configuration");
            }
            return false;
        }

        String requiredPermission = null;
        List<MatchingResource> matchingResources = new ArrayList<>();
        for (Permission permission : matchingPermissions) {
            if (requestMethod.equals(permission.getMethod()) && requestUri.matches(permission.getUrlPattern())) {
                if (requestUri.equals(permission.getUrl())) { // is there a exact match
                    requiredPermission = permission.getPath();
                    break;
                } else { // all templated urls add to a list
                    matchingResources.add(new MatchingResource(permission.getUrlPattern().replace(contextPath, ""), permission.getPath()));
                }
            }
        }

        if (requiredPermission == null) {
            if (matchingResources.size() == 1) { // only 1 templated url found
                requiredPermission = matchingResources.get(0).getPermission();
            }

            if (matchingResources.size() > 1) { // more than 1 templated urls found
                String urlWithoutContext = requestUri.replace(contextPath, "");
                StringTokenizer st = new StringTokenizer(urlWithoutContext, "/");
                int tokenPosition = 1;
                while (st.hasMoreTokens()) {
                    List<MatchingResource> tempList = new ArrayList<>();
                    String currentToken = st.nextToken();
                    for (MatchingResource matchingResource : matchingResources) {
                        StringTokenizer stmr = new StringTokenizer(matchingResource.getUrlPattern(), "/");
                        int internalTokenPosition = 1;
                        while (stmr.hasMoreTokens()) {
                            String internalToken = stmr.nextToken();
                            if ((tokenPosition == internalTokenPosition)  && currentToken.equals(internalToken)) {
                                tempList.add(matchingResource);
                            }
                            internalTokenPosition++;
                            if (tokenPosition < internalTokenPosition) {
                                break;
                            }
                        }
                    }
                    if (tempList.size() == 1) {
                        requiredPermission = tempList.get(0).getPermission();
                        break;
                    }
                    tokenPosition++;
                }
            }
        }

        if (requiredPermission == null) {
            if (log.isDebugEnabled()) {
                log.debug("Matching permission not found for " + requestUri);
            }
            return false;
        }

        boolean isUserAuthorized;
        try {
            isUserAuthorized = userRealm.getAuthorizationManager().isUserAuthorized(
                    tenantAwareUsername,
                    requiredPermission,
                    "ui.execute" // check against null values
            );
            return isUserAuthorized;
        } catch (Exception e) {
            log.error("Error occurred while retrieving user store. " + e.getMessage());
            return false;
        }
    }

    private AccessTokenInfo getAccessTokenInfo(TokenValidationContext validationContext)
            throws APIManagementException {

        Object cachedAccessTokenInfo =
                CacheProvider.createIntrospectionCache().get(validationContext.getAccessToken());
        if (cachedAccessTokenInfo != null) {
            return (AccessTokenInfo) cachedAccessTokenInfo;
        }
        String electedKeyManager = null;
        // Obtaining details about the token.
        if (StringUtils.isNotEmpty(validationContext.getTenantDomain())) {
            Map<String, KeyManagerDto>
                    tenantKeyManagers = KeyManagerHolder.getTenantKeyManagers(validationContext.getTenantDomain());
            KeyManager keyManagerInstance = null;
            if (tenantKeyManagers.values().size() == 1) {
                Map.Entry<String, KeyManagerDto> entry = tenantKeyManagers.entrySet().iterator().next();
                if (entry != null) {
                    KeyManagerDto keyManagerDto = entry.getValue();
                    if (keyManagerDto != null && (validationContext.getKeyManagers()
                            .contains(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS) ||
                            validationContext.getKeyManagers().contains(keyManagerDto.getName()))) {
                        keyManagerInstance = keyManagerDto.getKeyManager();
                        electedKeyManager = entry.getKey();
                    }
                }
            } else if (tenantKeyManagers.values().size() > 1) {
                if (validationContext.getKeyManagers()
                        .contains(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS)) {
                    for (Map.Entry<String, KeyManagerDto> keyManagerDtoEntry : tenantKeyManagers.entrySet()) {
                        if (keyManagerDtoEntry.getValue().getKeyManager() != null &&
                                keyManagerDtoEntry.getValue().getKeyManager()
                                        .canHandleToken(validationContext.getAccessToken())) {
                            keyManagerInstance = keyManagerDtoEntry.getValue().getKeyManager();
                            electedKeyManager = keyManagerDtoEntry.getKey();
                            break;
                        }
                    }
                } else {
                    for (String selectedKeyManager : validationContext.getKeyManagers()) {
                        KeyManagerDto keyManagerDto = tenantKeyManagers.get(selectedKeyManager);
                        if (keyManagerDto != null && keyManagerDto.getKeyManager() != null &&
                                keyManagerDto.getKeyManager().canHandleToken(validationContext.getAccessToken())) {
                            keyManagerInstance = keyManagerDto.getKeyManager();
                            electedKeyManager = selectedKeyManager;
                            break;
                        }
                    }
                }
            }

            if (keyManagerInstance != null) {
                AccessTokenInfo tokenInfo = keyManagerInstance.getTokenMetaData(validationContext.getAccessToken());
                tokenInfo.setKeyManager(electedKeyManager);
                CacheProvider.getGatewayIntrospectCache().put(validationContext.getAccessToken(), tokenInfo);
                return tokenInfo;
            } else {
                log.debug("KeyManager not available to authorize token.");
            }
        }
        return null;
    }

    private boolean isResourcePathMatching(String resourceString, URLMapping urlMapping) {

        String resource = resourceString.trim();
        String urlPattern = urlMapping.getUrlPattern().trim();

        if (resource.equalsIgnoreCase(urlPattern)) {
            return true;
        }

        // If the urlPattern is only one character longer than the resource and the urlPattern ends with a '/'
        if (resource.length() + 1 == urlPattern.length() && urlPattern.endsWith("/")) {
            // Check if resource is equal to urlPattern if the trailing '/' of the urlPattern is ignored
            String urlPatternWithoutSlash = urlPattern.substring(0, urlPattern.length() - 1);
            return resource.equalsIgnoreCase(urlPatternWithoutSlash);
        }

        return false;
    }
}
