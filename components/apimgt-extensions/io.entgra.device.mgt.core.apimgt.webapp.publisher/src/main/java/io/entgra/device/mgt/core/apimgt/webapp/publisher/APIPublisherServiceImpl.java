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
package io.entgra.device.mgt.core.apimgt.webapp.publisher;

import io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServicesImpl;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.PublisherRESTAPIServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.PublisherRESTAPIServicesImpl;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.APIInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.config.WebappPublisherConfig;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.dto.ApiScope;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.dto.ApiUriTemplate;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.exception.APIManagerPublisherException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantSearchResult;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;

/**
 * This class represents the concrete implementation of the APIPublisherService that corresponds to providing all
 * API publishing related operations.
 */
public class APIPublisherServiceImpl implements APIPublisherService {
    public static final APIManagerFactory API_MANAGER_FACTORY = APIManagerFactory.getInstance();
    private static final String UNLIMITED_TIER = "Unlimited";
    private static final String WS_UNLIMITED_TIER = "AsyncUnlimited";
    private static final String API_PUBLISH_ENVIRONMENT = "Default";
    private static final String CREATED_STATUS = "CREATED";
    private static final String PUBLISH_ACTION = "Publish";
    public static final String SUBSCRIPTION_TO_ALL_TENANTS = "ALL_TENANTS";
    public static final String SUBSCRIPTION_TO_CURRENT_TENANT = "CURRENT_TENANT";
    public static final String API_GLOBAL_VISIBILITY = "PUBLIC";
    public static final String API_PRIVATE_VISIBILITY = "PRIVATE";

    private static final Log log = LogFactory.getLog(APIPublisherServiceImpl.class);

    @Override
    public void publishAPI(APIConfig apiConfig) throws APIManagerPublisherException {
        WebappPublisherConfig config = WebappPublisherConfig.getInstance();
        List<String> tenants = new ArrayList<>(Collections.singletonList(APIConstants.SUPER_TENANT_DOMAIN));
        tenants.addAll(config.getTenants().getTenant());
        RealmService realmService = (RealmService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(RealmService.class, null);

        APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
        APIApplicationKey apiApplicationKey;
        AccessTokenInfo accessTokenInfo;
        try {
            apiApplicationKey = apiApplicationServices.createAndRetrieveApplicationCredentials();
            accessTokenInfo = apiApplicationServices.generateAccessTokenFromRegisteredApplication(
                    apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
        } catch (APIServicesException e) {
            String errorMsg = "Error occurred while generating the API application";
            log.error(errorMsg, e);
            throw new APIManagerPublisherException(e);
        }

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

                        PublisherRESTAPIServices publisherRESTAPIServices = new PublisherRESTAPIServicesImpl();
                        JSONArray apiList = (JSONArray) publisherRESTAPIServices.getApis(apiApplicationKey, accessTokenInfo).get("list");
                        boolean apiFound = false;
                        for (int i = 0; i < apiList.length(); i++) {
                            JSONObject apiObj = apiList.getJSONObject(i);
                            if (apiObj.getString("name").equals(apiIdentifier.getApiName().replace(Constants.SPACE,
                                    Constants.EMPTY_STRING))){
                                apiFound = true;
                                apiIdentifier.setUuid(apiObj.getString("id"));
                                break;
                            }
                        }
                        if (!apiFound) {
                            // add new scopes as shared scopes
                            for (ApiScope apiScope : apiConfig.getScopes()) {
                                if (!publisherRESTAPIServices.isSharedScopeNameExists(apiApplicationKey, accessTokenInfo,
                                        apiScope.getKey())) {
                                    Scope scope = new Scope();
                                    scope.setName(apiScope.getName());
                                    scope.setDescription(apiScope.getDescription());
                                    scope.setKey(apiScope.getKey());
                                    scope.setRoles(apiScope.getRoles());
                                    publisherRESTAPIServices.addNewSharedScope(apiApplicationKey, accessTokenInfo, scope);
                                }
                            }
                            APIInfo api = getAPI(apiConfig, true);
                            JSONObject createdAPI = publisherRESTAPIServices.addAPI(apiApplicationKey, accessTokenInfo, api);
                            if (apiConfig.getEndpointType() != null && "WS".equals(apiConfig.getEndpointType())) {
                                publisherRESTAPIServices.saveAsyncApiDefinition(apiApplicationKey, accessTokenInfo,
                                        createdAPI.getString("id"), apiConfig.getAsyncApiDefinition());
                            }
                            if (CREATED_STATUS.equals(createdAPI.getString("lifeCycleStatus"))) {
                                // if endpoint type "dynamic" and then add in sequence
                                if ("dynamic".equals(apiConfig.getEndpointType())) {
                                    Mediation mediation = new Mediation();
                                    mediation.setName(apiConfig.getInSequenceName());
                                    mediation.setConfig(apiConfig.getInSequenceConfig());
                                    mediation.setType("in");
                                    mediation.setGlobal(false);
                                    publisherRESTAPIServices.addApiSpecificMediationPolicy(apiApplicationKey,
                                            accessTokenInfo, createdAPI.getString("id"), mediation);
                                }
                                publisherRESTAPIServices.changeLifeCycleStatus(apiApplicationKey, accessTokenInfo,
                                        createdAPI.getString("id"), PUBLISH_ACTION);

                                APIRevision apiRevision = new APIRevision();
                                apiRevision.setApiUUID(createdAPI.getString("id"));
                                apiRevision.setDescription("Initial Revision");
                                String apiRevisionId = publisherRESTAPIServices.addAPIRevision(apiApplicationKey,
                                        accessTokenInfo, apiRevision).getString("id");

                                APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
                                apiRevisionDeployment.setDeployment(API_PUBLISH_ENVIRONMENT);
                                apiRevisionDeployment.setVhost(System.getProperty("iot.gateway.host"));
                                apiRevisionDeployment.setDisplayOnDevportal(true);

                                List<APIRevisionDeployment> apiRevisionDeploymentList = new ArrayList<>();
                                apiRevisionDeploymentList.add(apiRevisionDeployment);
                                publisherRESTAPIServices.deployAPIRevision(apiApplicationKey, accessTokenInfo,
                                        createdAPI.getString("id"), apiRevisionId, apiRevisionDeploymentList);
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

                                Set<ApiScope> scopesToMoveAsSharedScopes = new HashSet<>();
                                for (ApiScope apiScope : apiConfig.getScopes()) {
                                    // if the scope is not available as shared scope, and it is assigned to an API as a local scope
                                    // need remove the local scope and add as a shared scope
                                    if (!publisherRESTAPIServices.isSharedScopeNameExists(apiApplicationKey, accessTokenInfo,
                                            apiScope.getKey())) {
                                        //resolve- todo:apim- resolve
//                                         if (apiProvider.isScopeKeyAssignedLocally(apiIdentifier, apiScope.getKey(), tenantId)) {
                                        if (true) {
                                            // collect scope to move as shared scopes
                                            scopesToMoveAsSharedScopes.add(apiScope);
                                        } else {
                                            // if new scope add as shared scope
                                            Scope scope = new Scope();
                                            scope.setName(apiScope.getName());
                                            scope.setDescription(apiScope.getDescription());
                                            scope.setKey(apiScope.getKey());
                                            scope.setRoles(apiScope.getRoles());
                                            publisherRESTAPIServices.addNewSharedScope(apiApplicationKey, accessTokenInfo, scope);

                                        }
                                    }
                                }

                                // Get existing API
                                JSONObject existingAPI = publisherRESTAPIServices.getApi(apiApplicationKey, accessTokenInfo,
                                        apiIdentifier);
                                if (scopesToMoveAsSharedScopes.size() > 0) {
                                    // update API to remove local scopes
                                    APIInfo api = getAPI(apiConfig, false);
                                    api.setLifeCycleStatus(existingAPI.getString("lifeCycleStatus"));
                                    publisherRESTAPIServices.updateApi(apiApplicationKey, accessTokenInfo, api);

                                    for (ApiScope apiScope : scopesToMoveAsSharedScopes) {
                                        Scope scope = new Scope();
                                        scope.setName(apiScope.getName());
                                        scope.setDescription(apiScope.getDescription());
                                        scope.setKey(apiScope.getKey());
                                        scope.setRoles(apiScope.getRoles());
                                        publisherRESTAPIServices.addNewSharedScope(apiApplicationKey, accessTokenInfo, scope);
                                    }
                                }

                                existingAPI = publisherRESTAPIServices.getApi(apiApplicationKey, accessTokenInfo, apiIdentifier);
                                APIInfo api = getAPI(apiConfig, true);
                                api.setLastUpdatedTime(existingAPI.getString("lifeCycleStatus"));
                                api.setId(existingAPI.getString("id"));
                                publisherRESTAPIServices.updateApi(apiApplicationKey, accessTokenInfo, api);

                                if (apiConfig.getEndpointType() != null && "WS".equals(apiConfig.getEndpointType())) {
                                    publisherRESTAPIServices.saveAsyncApiDefinition(apiApplicationKey, accessTokenInfo,
                                            existingAPI.getString("id"), apiConfig.getAsyncApiDefinition());
                                }

                                // if endpoint type "dynamic" and then add /update in sequence
                                if ("dynamic".equals(apiConfig.getEndpointType())) {
                                    Mediation mediation = new Mediation();
                                    mediation.setName(apiConfig.getInSequenceName());
                                    mediation.setConfig(apiConfig.getInSequenceConfig());
                                    mediation.setType("in");
                                    mediation.setGlobal(false);

                                    List<Mediation> mediationList = (List) publisherRESTAPIServices
                                            .getAllApiSpecificMediationPolicies(apiApplicationKey, accessTokenInfo,
                                                    apiIdentifier).get("list");

                                    boolean isMediationPolicyFound = false;
                                    for (Mediation m : mediationList) {
                                        if (apiConfig.getInSequenceName().equals(m.getName())) {
                                            m.setConfig(apiConfig.getInSequenceConfig());
                                            publisherRESTAPIServices.
                                                    updateApiSpecificMediationPolicyContent(apiApplicationKey,
                                                            accessTokenInfo, existingAPI.getString("id"), m);
                                            isMediationPolicyFound = true;
                                            break;
                                        }
                                    }
                                    if (!isMediationPolicyFound) {
                                        publisherRESTAPIServices.addApiSpecificMediationPolicy(apiApplicationKey,
                                                accessTokenInfo, existingAPI.getString("id"), mediation);
                                    }
                                }

                                // This will retrieve the deployed revision
                                JSONArray revisionDeploymentList = (JSONArray) publisherRESTAPIServices.getAPIRevisions(apiApplicationKey,
                                        accessTokenInfo, existingAPI.getString("id"), true).get("list");
                                // This will retrieve the un deployed revision list
                                JSONArray undeployedRevisionList = (JSONArray) publisherRESTAPIServices.getAPIRevisions(apiApplicationKey,
                                        accessTokenInfo, existingAPI.getString("id"), false).get("list");
                                int apiRevisionCount = (int) publisherRESTAPIServices.getAPIRevisions(apiApplicationKey,
                                        accessTokenInfo, existingAPI.getString("id"), null).get("count");

                                if (apiRevisionCount >= 5) {
                                    JSONObject latestRevisionDeployment = revisionDeploymentList.getJSONObject(0);
                                    JSONObject earliestUndeployRevision = undeployedRevisionList.getJSONObject(0);
                                    publisherRESTAPIServices.undeployAPIRevisionDeployment(apiApplicationKey,
                                            accessTokenInfo, latestRevisionDeployment, existingAPI.getString("id"));
                                    publisherRESTAPIServices.deleteAPIRevision(apiApplicationKey, accessTokenInfo,
                                            earliestUndeployRevision, existingAPI.getString("id"));
                                }

                                // create new revision
                                APIRevision apiRevision = new APIRevision();
                                apiRevision.setApiUUID(existingAPI.getString("id"));
                                apiRevision.setDescription("Updated Revision");
                                String apiRevisionId = publisherRESTAPIServices.addAPIRevision(apiApplicationKey,
                                        accessTokenInfo, apiRevision).getString("id");

                                APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
                                apiRevisionDeployment.setDeployment(API_PUBLISH_ENVIRONMENT);
                                apiRevisionDeployment.setVhost(System.getProperty("iot.gateway.host"));
                                apiRevisionDeployment.setDisplayOnDevportal(true);

                                List<APIRevisionDeployment> apiRevisionDeploymentList = new ArrayList<>();
                                apiRevisionDeploymentList.add(apiRevisionDeployment);

                                publisherRESTAPIServices.deployAPIRevision(apiApplicationKey, accessTokenInfo,
                                        existingAPI.getString("id"), apiRevisionId, apiRevisionDeploymentList);

                                if (CREATED_STATUS.equals(existingAPI.getString("lifeCycleStatus"))) {
                                    publisherRESTAPIServices.changeLifeCycleStatus(apiApplicationKey,accessTokenInfo,
                                            existingAPI.getString("id"), PUBLISH_ACTION);
                                }
                            }
                        }
                        if (apiConfig.getApiDocumentationSourceFile() != null) {
                            APIInfo api = getAPI(apiConfig, true);

                            String fileName =
                                    CarbonUtils.getCarbonHome() + File.separator + "repository" +
                                            File.separator + "resources" + File.separator + "api-docs" + File.separator +
                                            apiConfig.getApiDocumentationSourceFile();

                            BufferedReader br = new BufferedReader(new FileReader(fileName));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line = null;
                            String ls = System.lineSeparator();
                            while ((line = br.readLine()) != null) {
                                stringBuilder.append(line);
                                stringBuilder.append(ls);
                            }
                            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                            br.close();
                            String docContent = stringBuilder.toString();

                            Documentation apiDocumentation = new Documentation(DocumentationType.HOWTO, apiConfig.getApiDocumentationName());
                            apiDocumentation.setVisibility(Documentation.DocumentVisibility.API_LEVEL);
                            apiDocumentation.setSourceType(Documentation.DocumentSourceType.MARKDOWN);
                            apiDocumentation.setCreatedDate(new Date());
                            apiDocumentation.setLastUpdated(new Date());
                            apiDocumentation.setSummary(apiConfig.getApiDocumentationSummary());
                            apiDocumentation.setOtherTypeName(null);

                            JSONArray documentList = (JSONArray) publisherRESTAPIServices.getDocumentations(apiApplicationKey,
                                    accessTokenInfo, api.getId()).get("list");

                            if (documentList.length() > 0) {
                                for (int i = 0; i < documentList.length(); i++) {
                                    JSONObject existingDoc = documentList.getJSONObject(i);
                                    if (existingDoc.getString("name").equals(apiConfig.getApiDocumentationName())
                                            && existingDoc.getString("type").equals(DocumentationType.HOWTO)) {
                                        publisherRESTAPIServices.deleteDocumentations(apiApplicationKey, accessTokenInfo,
                                                api.getId(), existingDoc.getString("documentId"));
                                    }
                                }
                            } else {
                                log.info("There is no any existing api documentation.");
                            }
                            Documentation createdDoc = publisherRESTAPIServices.addDocumentation(apiApplicationKey, accessTokenInfo,
                                    api.getId(), apiDocumentation);

                            publisherRESTAPIServices.addDocumentationContent(apiApplicationKey, accessTokenInfo, api,
                                    createdDoc.getId(), docContent);
                        }
                    } catch (APIManagementException | IOException | APIServicesException |
                             BadRequestException | UnexpectedResponseException e) {
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

    @Override
    public void updateScopeRoleMapping()
            throws APIManagerPublisherException {
        // todo: This logic has written assuming all the scopes are now work as shared scopes
        WebappPublisherConfig config = WebappPublisherConfig.getInstance();
        List<String> tenants = new ArrayList<>(Collections.singletonList(APIConstants.SUPER_TENANT_DOMAIN));
        tenants.addAll(config.getTenants().getTenant());

        APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
        APIApplicationKey apiApplicationKey;
        AccessTokenInfo accessTokenInfo;
        try {
            apiApplicationKey = apiApplicationServices.createAndRetrieveApplicationCredentials();
            accessTokenInfo = apiApplicationServices.generateAccessTokenFromRegisteredApplication(
                    apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
        } catch (APIServicesException e) {
            String errorMsg = "Error occurred while generating the API application";
            log.error(errorMsg, e);
            throw new APIManagerPublisherException(e);
        }

        try {
            for (String tenantDomain : tenants) {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                PublisherRESTAPIServices publisherRESTAPIServices = new PublisherRESTAPIServicesImpl();
                JSONObject scopeObject = publisherRESTAPIServices.getScopes(apiApplicationKey, accessTokenInfo);

                try {
                    String fileName =
                            CarbonUtils.getCarbonConfigDirPath() + File.separator + "etc"
                                    + File.separator + tenantDomain + ".csv";
                    if (Files.exists(Paths.get(fileName))) {
                        BufferedReader br = new BufferedReader(new FileReader(fileName));
                        int lineNumber = 0;
                        Map<Integer, String> roles = new HashMap<>();
                        String line = "";
                        String splitBy = ",";
                        while ((line = br.readLine()) != null)   //returns a Boolean value
                        {
                            lineNumber++;
                            String[] scopeMapping = line.split(splitBy);    // use comma as separator
                            if (lineNumber == 1) { // skip titles
                                for (int i = 0; i < scopeMapping.length; i++) {
                                    if (i > 3) {
                                        roles.put(i, scopeMapping[i]); // add roles to the map
                                    }
                                }
                                continue;
                            }

                            Scope scope = new Scope();
                            scope.setName(
                                    scopeMapping[0] != null ? StringUtils.trim(scopeMapping[0]) : StringUtils.EMPTY);
                            scope.setDescription(
                                    scopeMapping[1] != null ? StringUtils.trim(scopeMapping[1]) : StringUtils.EMPTY);
                            scope.setKey(
                                    scopeMapping[2] != null ? StringUtils.trim(scopeMapping[2]) : StringUtils.EMPTY);
                            //                        scope.setPermissions(
                            //                                scopeMapping[3] != null ? StringUtils.trim(scopeMapping[3]) : StringUtils.EMPTY);

                            String roleString = "";
                            for (int i = 4; i < scopeMapping.length; i++) {
                                if (scopeMapping[i] != null && StringUtils.trim(scopeMapping[i]).equals("Yes")) {
                                    roleString = roleString + "," + roles.get(i);
                                }
                            }
                            if (roleString.length() > 1) {
                                roleString = roleString.substring(1); // remove first , (comma)
                            }
                            scope.setRoles(roleString);

                            //Set scope id which related to the scope key
                            JSONArray scopeList = (JSONArray) scopeObject.get("list");
                            for (int i = 0; i < scopeList.length(); i++) {
                                JSONObject scopeObj = scopeList.getJSONObject(i);
                                if (scopeObj.getString("name").equals(scopeMapping[2] != null ?
                                        StringUtils.trim(scopeMapping[2]) : StringUtils.EMPTY)) {
                                    scope.setId(scopeObj.getString("id"));
                                }
                            }

                            if (publisherRESTAPIServices.isSharedScopeNameExists(apiApplicationKey, accessTokenInfo, scope.getKey())) {
                                publisherRESTAPIServices.updateSharedScope(apiApplicationKey, accessTokenInfo, scope);
                            } else {
                                // todo: come to this level means, that scope is removed from API, but haven't removed from the scope-role-permission-mappings list
                                if (log.isDebugEnabled()) {
                                    log.debug(scope.getKey() + " not available as shared scope");
                                }
                            }
                        }
                    }
                } catch (IOException | DirectoryIteratorException ex) {
                    log.error("failed to read scopes from file.", ex);
                } catch (APIServicesException | BadRequestException e) {
                    String errorMsg = "Error while calling APIs";
                    log.error(errorMsg, e);
                    throw new APIManagerPublisherException(e);
                }

            }
        }
        catch (APIServicesException e) {
            String errorMsg = "Error while processing Publisher REST API response";
            log.error(errorMsg, e);
            throw new APIManagerPublisherException(e);
        } catch (BadRequestException e) {
            String errorMsg = "Error while calling Publisher REST APIs";
            log.error(errorMsg, e);
            throw new APIManagerPublisherException(e);
        } catch (UnexpectedResponseException e) {
            String errorMsg = "Unexpected response from the server";
            log.error(errorMsg, e);
            throw new APIManagerPublisherException(e);
        }finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private APIInfo getAPI(APIConfig config, boolean includeScopes) {

        APIInfo apiInfo = new APIInfo();
        apiInfo.setName(config.getName().replace(Constants.SPACE, Constants.EMPTY_STRING));
        apiInfo.setDescription("");
        apiInfo.setContext(config.getContext());
        apiInfo.setVersion(config.getVersion());
        apiInfo.setProvider(config.getOwner());
        apiInfo.setLifeCycleStatus(CREATED_STATUS);
        apiInfo.setWsdlInfo(null);
        apiInfo.setWsdlUrl(null);
        apiInfo.setResponseCachingEnabled(false);
        apiInfo.setCacheTimeout(0);
        apiInfo.setHasThumbnail(false);
        apiInfo.setDefaultVersion(config.isDefault());
        apiInfo.setRevision(false);
        apiInfo.setRevisionedApiId(null);
        apiInfo.setEnableSchemaValidation(false);

        Set<String> tags = new HashSet<>();
        tags.addAll(Arrays.asList(config.getTags()));
        apiInfo.setTags(tags);

        Set<String> availableTiers = new HashSet<>();
        if (config.getEndpointType() != null && "WS".equals(config.getEndpointType())) {
            availableTiers.add(WS_UNLIMITED_TIER);
        } else {
            availableTiers.add(UNLIMITED_TIER);
        }
        apiInfo.setPolicies(availableTiers);

        if (config.getEndpointType() == null) {
            List<JSONObject> operations = new ArrayList();
            List<JSONObject> scopeSet = new ArrayList();
            Iterator<ApiUriTemplate> iterator;
            for (iterator = config.getUriTemplates().iterator(); iterator.hasNext(); ) {
                ApiUriTemplate apiUriTemplate = iterator.next();
                JSONObject operation = new JSONObject();
                operation.put("target", apiUriTemplate.getUriTemplate());
                operation.put("verb", apiUriTemplate.getHttpVerb());
                operation.put("authType", apiUriTemplate.getAuthType());
                operation.put("throttlingPolicy", UNLIMITED_TIER);
                if (includeScopes) {
                    if (apiUriTemplate.getScope() != null) {
                        String scopeString = "{\n" +
                                "            \"scope\": {\n" +
                                "                \"id\": null,\n" +
                                "                \"name\": \"" + apiUriTemplate.getScope().getKey() + "\",\n" +
                                "                \"displayName\": \"" + apiUriTemplate.getScope().getName() + "\",\n" +
                                "                \"description\": \"" + apiUriTemplate.getScope().getDescription() + "\",\n" +
                                "                \"bindings\": [\n" +
                                "                    \"" + apiUriTemplate.getScope().getRoles() + "\"\n" +
                                "                ],\n" +
                                "                \"usageCount\": null\n" +
                                "            },\n" +
                                "            \"shared\": true\n" +
                                "        }";
                        JSONObject scope = new JSONObject(scopeString);
                        scopeSet.add(scope);

                        Set<String> scopes = new HashSet<>();
                        scopes.add(apiUriTemplate.getScope().getKey());
                        operation.put("scopes", scopes);
                    }
                }
                operations.add(operation);
            }
            apiInfo.setScopes(scopeSet);
            apiInfo.setOperations(operations);
        }

        if (config.isSharedWithAllTenants()) {
            apiInfo.setSubscriptionAvailability(SUBSCRIPTION_TO_ALL_TENANTS);
            apiInfo.setVisibility(API_GLOBAL_VISIBILITY);
        } else {
            apiInfo.setSubscriptionAvailability(SUBSCRIPTION_TO_CURRENT_TENANT);
            apiInfo.setVisibility(API_PRIVATE_VISIBILITY);
        }

        String endpointConfig;
        endpointConfig = "{\n" +
                "        \"endpoint_type\": \"http\",\n" +
                "        \"sandbox_endpoints\": {\n" +
                "            \"url\": \"" + config.getEndpoint() + "\"\n" +
                "        },\n" +
                "        \"production_endpoints\": {\n" +
                "            \"url\": \"" + config.getEndpoint() + "\"\n" +
                "        }\n" +
                "    }";
        JSONObject endPointConfig = new JSONObject(endpointConfig);

        Set<String> transports = new HashSet<>();
        transports.addAll(Arrays.asList(config.getTransports()));
        apiInfo.setTransport(transports);

        apiInfo.setType("HTTP");

        if (config.getEndpointType() != null && "dynamic".equals(config.getEndpointType())) {
            endpointConfig = "{\n" +
                    "        \"endpoint_type\": \"http\",\n" +
                    "        \"sandbox_endpoints\": {\n" +
                    "            \"url\": \" default \"\n" +
                    "        },\n" +
                    "        \"production_endpoints\": {\n" +
                    "            \"url\": \" default \"\n" +
                    "        }\n" +
                    "    }";
            endPointConfig = new JSONObject(endpointConfig);
            //TODO: Will be used in dynamic endpoints
//            apiInfo.setInSequence(config.getInSequenceName());
        }

        // if ws endpoint
        if (config.getEndpointType() != null && "WS".equals(config.getEndpointType())) {
           endpointConfig = "{\n" +
                    "        \"endpoint_type\": \"ws\",\n" +
                    "        \"sandbox_endpoints\": {\n" +
                    "            \"url\": \"" + config.getEndpoint() + "\"\n" +
                    "        },\n" +
                    "        \"production_endpoints\": {\n" +
                    "            \"url\": \"" + config.getEndpoint() + "\"\n" +
                    "        }\n" +
                    "    }";
            endPointConfig = new JSONObject(endpointConfig);

            transports.addAll(Arrays.asList("wss,ws"));
            apiInfo.setTransport(transports);
            apiInfo.setType("WS");
        }
        apiInfo.setEndpointConfig(endPointConfig);

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
        apiInfo.setCorsConfiguration(corsConfiguration);

        apiInfo.setAuthorizationHeader("Authorization");
        List<String> keyManagers = new ArrayList<>();
        keyManagers.add("all");
        apiInfo.setKeyManagers(keyManagers);
        apiInfo.setEnableSchemaValidation(false);
        apiInfo.setMonetization(null);
        apiInfo.setServiceInfo(null);

        return apiInfo;

        //TODO: Will be used in WS or dynamic endpoints
//        if (config.getEndpointType() != null && "WS".equals(config.getEndpointType())) {
//            api.setAsyncApiDefinition(config.getAsyncApiDefinition());
//            AsyncApiParser asyncApiParser = new AsyncApiParser();
//            try {
//                api.setUriTemplates(asyncApiParser.getURITemplates(config.getAsyncApiDefinition(), true));
//            } catch (APIManagementException e) {
//
//            }
//            api.setWsUriMapping(asyncApiParser.buildWSUriMapping(config.getAsyncApiDefinition()));
//        } else {
//            api.setSwaggerDefinition(APIPublisherUtil.getSwaggerDefinition(config));
//
//            Set<URITemplate> uriTemplates = new HashSet<>();
//            Iterator<ApiUriTemplate> iterator;
//            for (iterator = config.getUriTemplates().iterator(); iterator.hasNext(); ) {
//                ApiUriTemplate apiUriTemplate = iterator.next();
//                URITemplate uriTemplate = new URITemplate();
//                uriTemplate.setAuthType(apiUriTemplate.getAuthType());
//                uriTemplate.setHTTPVerb(apiUriTemplate.getHttpVerb());
//                uriTemplate.setResourceURI(apiUriTemplate.getResourceURI());
//                uriTemplate.setUriTemplate(apiUriTemplate.getUriTemplate());
//                if (includeScopes) {
//                    Scope scope = new Scope();
//                    if (apiUriTemplate.getScope() != null) {
//                        scope.setName(apiUriTemplate.getScope().getName());
//                        scope.setDescription(apiUriTemplate.getScope().getDescription());
//                        scope.setKey(apiUriTemplate.getScope().getKey());
//                        scope.setRoles(apiUriTemplate.getScope().getRoles());
//                        uriTemplate.setScopes(scope);
//                    }
//                }
//                uriTemplates.add(uriTemplate);
//            }
//            api.setUriTemplates(uriTemplates);
//        }
    }
}
