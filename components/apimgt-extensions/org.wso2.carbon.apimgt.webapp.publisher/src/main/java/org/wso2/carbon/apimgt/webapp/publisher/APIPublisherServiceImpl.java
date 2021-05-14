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
import org.wso2.carbon.apimgt.webapp.publisher.dto.ApiUriTemplate;
import org.wso2.carbon.apimgt.webapp.publisher.exception.APIManagerPublisherException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
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

    @Override
    public void publishAPI(APIConfig apiConfig) throws APIManagerPublisherException {
        String tenantDomain = MultitenantUtils.getTenantDomain(apiConfig.getOwner());
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(apiConfig.getOwner());
        try {
            APIProvider apiProvider = API_MANAGER_FACTORY.getAPIProvider(apiConfig.getOwner());
            API api = getAPI(apiConfig);

            if (!apiProvider.isAPIAvailable(api.getId())) {
                API createdAPI = apiProvider.addAPI(api);
                if (CREATED_STATUS.equals(createdAPI.getStatus())) {
                    apiProvider.changeLifeCycleStatus(tenantDomain, createdAPI.getUuid(), PUBLISH_ACTION, null);
                    APIRevision apiRevision = new APIRevision();
                    apiRevision.setApiUUID(createdAPI.getUuid());
                    apiRevision.setDescription("Initial Revision");
                    String apiRevisionId = apiProvider.addAPIRevision(apiRevision, tenantDomain);
                    APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
                    apiRevisionDeployment.setDeployment(API_PUBLISH_ENVIRONMENT);
                    apiRevisionDeployment.setVhost("localhost");
                    apiRevisionDeployment.setDisplayOnDevportal(true);

                    List<APIRevisionDeployment> apiRevisionDeploymentList = new ArrayList<>();
                    apiRevisionDeploymentList.add(apiRevisionDeployment);
                    apiProvider.deployAPIRevision(createdAPI.getUuid(), apiRevisionId, apiRevisionDeploymentList);

                }
            } else {
                if (WebappPublisherConfig.getInstance().isEnabledUpdateApi()) {
                    API existingAPI = apiProvider.getAPI(api.getId());
                    api.setStatus(existingAPI.getStatus());
                    apiProvider.updateAPI(api);
                    if (api.getId().getName().equals(existingAPI.getId().getName()) &&
                            api.getId().getVersion().equals(existingAPI.getId().getVersion())) {
                        if (CREATED_STATUS.equals(existingAPI.getStatus())) {
                            apiProvider.changeLifeCycleStatus(tenantDomain, existingAPI.getUuid(), PUBLISH_ACTION, null);
                        }
                    }
                }
            }


        } catch (FaultGatewaysException | APIManagementException e) {
            throw new APIManagerPublisherException(e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private API getAPI(APIConfig config) {

        APIIdentifier apiIdentifier = new APIIdentifier(config.getOwner(), config.getName(), config.getVersion());
        API api = new API(apiIdentifier);
        api.setDescription("");
        String context = config.getContext();
        context = context.startsWith("/") ? context : ("/" + context);
        api.setContext(context + "/" + config.getVersion());
        api.setStatus(CREATED_STATUS);
        api.setWsdlUrl(null);
        api.setResponseCache("Disabled");
        api.setContextTemplate(context + "/{version}" );
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
            Scope scope = new Scope();
            if (apiUriTemplate.getScope() != null) {
                scope.setName(apiUriTemplate.getScope().getName());
                scope.setDescription(apiUriTemplate.getScope().getDescription());
                scope.setKey(apiUriTemplate.getScope().getKey());
                scope.setRoles(apiUriTemplate.getScope().getRoles());
                uriTemplate.setScope(scope);
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
                config.getEndpoint() + "\" }, \"production_endpoints\": { \"url\": \" "+ config.getEndpoint()+"\" } }";

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
