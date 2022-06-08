/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.apimgt.webapp.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.webapp.publisher.dto.ApiScope;
import org.wso2.carbon.apimgt.webapp.publisher.dto.ApiUriTemplate;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.webapp.publisher.config.WebappPublisherConfig;
import org.wso2.carbon.apimgt.webapp.publisher.exception.APIManagerPublisherException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantSearchResult;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class represents the concrete implementation of the APIPublisherService that corresponds to providing all
 * API publishing related operations.
 */
public class APIPublisherServiceImpl implements APIPublisherService {
    private static final String UNLIMITED_TIER = "Unlimited";
    private static final String API_PUBLISH_ENVIRONMENT = "Default";
    private static final String CREATED_STATUS = "CREATED";
    private static final String PUBLISH_ACTION = "Publish";
    public static final APIManagerFactory API_MANAGER_FACTORY = APIManagerFactory.getInstance();
    private static final Log log = LogFactory.getLog(APIPublisherServiceImpl.class);

    @Override
    public void publishAPI(APIConfig apiConfig) throws APIManagerPublisherException {
        WebappPublisherConfig config = WebappPublisherConfig.getInstance();
        List<String> tenants = new ArrayList<>(Collections.singletonList(APIConstants.SUPER_TENANT_DOMAIN));
        tenants.addAll(config.getTenants().getTenant());
        RealmService realmService = (RealmService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(RealmService.class, null);
        try {
            boolean tenantFound = false;
            boolean tenantsLoaded = false;
            TenantSearchResult tenantSearchResult = null;
            for (String tenantDomain : tenants) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                if (!tenantsLoaded) {
                    tenantSearchResult = realmService.getTenantManager()
                            .listTenants(Integer.MAX_VALUE, 0, "asc", "UM_ID", null);
                    tenantsLoaded = true;
                }
                if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    tenantFound = true;
                    realmService.getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID)
                            .getRealmConfiguration().getAdminUserName();
                } else {
                    List<Tenant> allTenants = tenantSearchResult.getTenantList();
                    for (Tenant tenant : allTenants) {
                        if (tenant.getDomain().equals(tenantDomain)) {
                            tenantFound = true;
                            tenant.getAdminName();
                            break;
                        } else {
                            tenantFound = false;
                        }
                    }
                }

                if (tenantFound) {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(apiConfig.getOwner());
                    int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

                    try {
                        apiConfig.setOwner(APIUtil.getTenantAdminUserName(tenantDomain));
                        apiConfig.setTenantDomain(tenantDomain);
                        APIProvider apiProvider = API_MANAGER_FACTORY.getAPIProvider(apiConfig.getOwner());
                        APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(apiConfig.getOwner()),
                                apiConfig.getName(), apiConfig.getVersion());

                        if (!apiProvider.isAPIAvailable(apiIdentifier)) {

                            // add new scopes as shared scopes
                            Set<String> allSharedScopeKeys = apiProvider.getAllSharedScopeKeys(tenantDomain);
                            for (ApiScope apiScope : apiConfig.getScopes()) {
                                if (!allSharedScopeKeys.contains(apiScope.getKey())) {
                                    Scope scope = new Scope();
                                    scope.setName(apiScope.getName());
                                    scope.setDescription(apiScope.getDescription());
                                    scope.setKey(apiScope.getKey());
                                    scope.setRoles(apiScope.getRoles());
                                    apiProvider.addSharedScope(scope, tenantDomain);
                                }
                            }
                            API api = getAPI(apiConfig, true);
                            api.setId(apiIdentifier);
                            API createdAPI = apiProvider.addAPI(api);
                            if (CREATED_STATUS.equals(createdAPI.getStatus())) {
                                apiProvider.changeLifeCycleStatus(tenantDomain, createdAPI.getUuid(), PUBLISH_ACTION, null);
                                APIRevision apiRevision = new APIRevision();
                                apiRevision.setApiUUID(createdAPI.getUuid());
                                apiRevision.setDescription("Initial Revision");
                                String apiRevisionId = apiProvider.addAPIRevision(apiRevision, tenantDomain);

                                APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
                                apiRevisionDeployment.setDeployment(API_PUBLISH_ENVIRONMENT);
                                apiRevisionDeployment.setVhost(System.getProperty("iot.gateway.host"));
                                apiRevisionDeployment.setDisplayOnDevportal(true);

                                List<APIRevisionDeployment> apiRevisionDeploymentList = new ArrayList<>();
                                apiRevisionDeploymentList.add(apiRevisionDeployment);
                                apiProvider.deployAPIRevision(createdAPI.getUuid(), apiRevisionId, apiRevisionDeploymentList);
                            }
                        } else {
                            if (WebappPublisherConfig.getInstance().isEnabledUpdateApi()) {

                                // With 4.x to 5.x upgrade
                                // - there cannot be same local scope assigned in 2 different APIs
                                // - local scopes will be deprecated in the future, so need to move all scopes as shared scopes

                                // if an api scope is not available as shared scope, but already assigned as local scope -> that means, the scopes available for this API has not moved as shared scopes
                                // in order to do that :
                                // 1. update the same API removing scopes from URI templates
                                // 2. add scopes as shared scopes
                                // 3. update the API again adding scopes for the URI Templates

                                // if an api scope is not available as shared scope, and not assigned as local scope -> that means, there are new scopes
                                // 1. add new scopes as shared scopes
                                // 2. update the API adding scopes for the URI Templates

                                Set<String> allSharedScopeKeys = apiProvider.getAllSharedScopeKeys(tenantDomain);
                                Set<ApiScope> scopesToMoveAsSharedScopes = new HashSet<>();
                                for (ApiScope apiScope : apiConfig.getScopes()) {
                                    // if the scope is not available as shared scope and it is assigned to an API as a local scope
                                    // need remove the local scope and add as a shared scope
                                    if (!allSharedScopeKeys.contains(apiScope.getKey())) {
                                        if (apiProvider.isScopeKeyAssignedLocally(apiIdentifier, apiScope.getKey(), tenantId)) {
                                            // collect scope to move as shared scopes
                                            scopesToMoveAsSharedScopes.add(apiScope);
                                        } else {
                                            // if new scope add as shared scope
                                            Scope scope = new Scope();
                                            scope.setName(apiScope.getName());
                                            scope.setDescription(apiScope.getDescription());
                                            scope.setKey(apiScope.getKey());
                                            scope.setRoles(apiScope.getRoles());
                                            apiProvider.addSharedScope(scope, tenantDomain);
                                        }
                                    }
                                }

                                // Get existing API
                                API existingAPI = apiProvider.getAPI(apiIdentifier);

                                if (scopesToMoveAsSharedScopes.size() > 0) {
                                    // update API to remove local scopes
                                    API api = getAPI(apiConfig, false);
                                    api.setStatus(existingAPI.getStatus());
                                    apiProvider.updateAPI(api);

                                    for (ApiScope apiScope : scopesToMoveAsSharedScopes) {
                                        Scope scope = new Scope();
                                        scope.setName(apiScope.getName());
                                        scope.setDescription(apiScope.getDescription());
                                        scope.setKey(apiScope.getKey());
                                        scope.setRoles(apiScope.getRoles());
                                        apiProvider.addSharedScope(scope, tenantDomain);
                                    }
                                }

                                existingAPI = apiProvider.getAPI(apiIdentifier);
                                API api = getAPI(apiConfig, true);
                                api.setStatus(existingAPI.getStatus());
                                apiProvider.updateAPI(api);

                                // Assumption: Assume the latest revision is the published one
                                String latestRevisionUUID = apiProvider.getLatestRevisionUUID(existingAPI.getUuid());
                                List<APIRevisionDeployment> latestRevisionDeploymentList =
                                        apiProvider.getAPIRevisionDeploymentList(latestRevisionUUID);

                                List<APIRevision> apiRevisionList = apiProvider.getAPIRevisions(existingAPI.getUuid());
                                if (apiRevisionList.size() >= 5) {
                                    String earliestRevisionUUID = apiProvider.getEarliestRevisionUUID(existingAPI.getUuid());
                                    List<APIRevisionDeployment> earliestRevisionDeploymentList =
                                            apiProvider.getAPIRevisionDeploymentList(earliestRevisionUUID);
                                    apiProvider.undeployAPIRevisionDeployment(existingAPI.getUuid(), earliestRevisionUUID, earliestRevisionDeploymentList);
                                    apiProvider.deleteAPIRevision(existingAPI.getUuid(), earliestRevisionUUID, tenantDomain);
                                }

                                // create new revision
                                APIRevision apiRevision = new APIRevision();
                                apiRevision.setApiUUID(existingAPI.getUuid());
                                apiRevision.setDescription("Updated Revision");
                                String apiRevisionId = apiProvider.addAPIRevision(apiRevision, tenantDomain);

                                apiProvider.deployAPIRevision(existingAPI.getUuid(), apiRevisionId, latestRevisionDeploymentList);

                                if (CREATED_STATUS.equals(existingAPI.getStatus())) {
                                    apiProvider.changeLifeCycleStatus(tenantDomain, existingAPI.getUuid(), PUBLISH_ACTION, null);
                                }
                            }
                        }
                    } catch (FaultGatewaysException | APIManagementException e) {
                        String msg = "Error occurred while publishing api";
                        log.error(msg, e);
                        throw new APIManagerPublisherException(e);
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
            }
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving admin user from tenant user realm";
            log.error(msg, e);
            throw new APIManagerPublisherException(e);
        }
    }

    private API getAPI(APIConfig config, boolean includeScopes) {

        APIIdentifier apiIdentifier = new APIIdentifier(config.getOwner(), config.getName(), config.getVersion());
        API api = new API(apiIdentifier);
        api.setDescription("");
        String context = config.getContext();
        context = context.startsWith("/") ? context : ("/" + context);
        api.setContext(context + "/" + config.getVersion());
        api.setStatus(CREATED_STATUS);
        api.setWsdlUrl(null);
        api.setResponseCache("Disabled");
        api.setContextTemplate(context + "/{version}");
        api.setSwaggerDefinition(APIPublisherUtil.getSwaggerDefinition(config));
        api.setType("HTTP");

        Set<URITemplate> uriTemplates = new HashSet<>();
        Iterator<ApiUriTemplate> iterator;
        for (iterator = config.getUriTemplates().iterator(); iterator.hasNext(); ) {
            ApiUriTemplate apiUriTemplate = iterator.next();
            URITemplate uriTemplate = new URITemplate();
            uriTemplate.setAuthType(apiUriTemplate.getAuthType());
            uriTemplate.setHTTPVerb(apiUriTemplate.getHttpVerb());
            uriTemplate.setResourceURI(apiUriTemplate.getResourceURI());
            uriTemplate.setUriTemplate(apiUriTemplate.getUriTemplate());
            if (includeScopes) {
                Scope scope = new Scope();
                if (apiUriTemplate.getScope() != null) {
                    scope.setName(apiUriTemplate.getScope().getName());
                    scope.setDescription(apiUriTemplate.getScope().getDescription());
                    scope.setKey(apiUriTemplate.getScope().getKey());
                    scope.setRoles(apiUriTemplate.getScope().getRoles());
                    uriTemplate.setScopes(scope);
                }
            }
            uriTemplates.add(uriTemplate);
        }
        api.setUriTemplates(uriTemplates);

        api.setApiOwner(config.getOwner());


        api.setDefaultVersion(config.isDefault());
        api.setTransports("https,http");

        Set<String> tags = new HashSet<>();
        tags.addAll(Arrays.asList(config.getTags()));
        api.setTags(tags);

        Set<Tier> availableTiers = new HashSet<>();
        availableTiers.add(new Tier(UNLIMITED_TIER));
        api.setAvailableTiers(availableTiers);

        Set<String> environments = new HashSet<>();
        environments.add(API_PUBLISH_ENVIRONMENT);
        api.setEnvironments(environments);

        if (config.isSharedWithAllTenants()) {
            api.setSubscriptionAvailability(APIConstants.SUBSCRIPTION_TO_ALL_TENANTS);
            api.setVisibility(APIConstants.API_GLOBAL_VISIBILITY);
        } else {
            api.setSubscriptionAvailability(APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT);
            api.setVisibility(APIConstants.API_PRIVATE_VISIBILITY);
        }
        String endpointConfig = "{ \"endpoint_type\": \"http\", \"sandbox_endpoints\": { \"url\": \" " +
                config.getEndpoint() + "\" }, \"production_endpoints\": { \"url\": \" " + config.getEndpoint() + "\" } }";

        api.setEndpointConfig(endpointConfig);
        List<String> accessControlAllowOrigins = new ArrayList<>();
        accessControlAllowOrigins.add("*");

        List<String> accessControlAllowHeaders = new ArrayList<>();
        accessControlAllowHeaders.add("authorization");
        accessControlAllowHeaders.add("Access-Control-Allow-Origin");
        accessControlAllowHeaders.add("Content-Type");
        accessControlAllowHeaders.add("SOAPAction");
        accessControlAllowHeaders.add("apikey");
        accessControlAllowHeaders.add("Internal-Key");
        List<String> accessControlAllowMethods = new ArrayList<>();
        accessControlAllowMethods.add("GET");
        accessControlAllowMethods.add("PUT");
        accessControlAllowMethods.add("DELETE");
        accessControlAllowMethods.add("POST");
        accessControlAllowMethods.add("PATCH");
        accessControlAllowMethods.add("OPTIONS");
        CORSConfiguration corsConfiguration = new CORSConfiguration(false, accessControlAllowOrigins, false,
                accessControlAllowHeaders, accessControlAllowMethods);
        api.setCorsConfiguration(corsConfiguration);

        api.setAuthorizationHeader("Authorization");
        List<String> keyManagers = new ArrayList<>();
        keyManagers.add("all");
        api.setKeyManagers(keyManagers);
        api.setEnableStore(true);

        return api;
    }
}
