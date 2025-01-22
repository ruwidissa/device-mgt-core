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

import com.google.gson.Gson;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.PublisherRESTAPIServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.APIInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.APIRevision;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.APIRevisionDeployment;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.CORSConfiguration;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Documentation;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Mediation;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.MediationPolicy;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Operations;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Scope;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.util.APIPublisherUtils;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.config.WebappPublisherConfig;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.dto.ApiScope;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.dto.ApiUriTemplate;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.exception.APIManagerPublisherException;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceManagementConfig;
import io.entgra.device.mgt.core.device.mgt.core.config.permission.DefaultPermission;
import io.entgra.device.mgt.core.device.mgt.core.config.permission.DefaultPermissions;
import io.entgra.device.mgt.core.device.mgt.core.config.permission.ScopeMapping;
import io.entgra.device.mgt.core.device.mgt.core.permission.mgt.PermissionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantSearchResult;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import io.entgra.device.mgt.core.device.mgt.common.permission.mgt.PermissionManagementException;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the concrete implementation of the APIPublisherService that corresponds to providing all
 * API publishing related operations.
 */
public class APIPublisherServiceImpl implements APIPublisherService {
    public static final APIManagerFactory API_MANAGER_FACTORY = APIManagerFactory.getInstance();
    private static final Gson gson = new Gson();
    private static final String UNLIMITED_TIER = "Unlimited";
    private static final String WS_UNLIMITED_TIER = "AsyncUnlimited";
    private static final String API_PUBLISH_ENVIRONMENT = "Default";
    private static final String CREATED_STATUS = "CREATED";
    private static final String PUBLISH_ACTION = "Publish";
    public static final String SUBSCRIPTION_TO_ALL_TENANTS = "ALL_TENANTS";
    public static final String SUBSCRIPTION_TO_CURRENT_TENANT = "CURRENT_TENANT";
    public static final String API_GLOBAL_VISIBILITY = "PUBLIC";
    public static final String API_PRIVATE_VISIBILITY = "PRIVATE";
    private static final String ADMIN_ROLE_KEY = "admin";

    private static final Log log = LogFactory.getLog(APIPublisherServiceImpl.class);

    @Override
    public void publishAPI(APIConfig apiConfig) throws APIManagerPublisherException {
        WebappPublisherConfig config = WebappPublisherConfig.getInstance();
        List<String> tenants = new ArrayList<>(Collections.singletonList(APIConstants.SUPER_TENANT_DOMAIN));
        tenants.addAll(config.getTenants().getTenant());
        RealmService realmService = (RealmService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(RealmService.class, null);
        PublisherRESTAPIServices publisherRESTAPIServices = APIPublisherDataHolder.getInstance().getPublisherRESTAPIServices();

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
                    log.info("Initiating API publishing sequence for API [ " + apiConfig.getName() + "] in domain [ " +
                            tenantDomain + " ]");
                    try {
                        APIPublisherUtils.createScopePublishUserIfNotExists(tenantDomain);
                    } catch (APIServicesException e) {
                        String errorMsg = "Error occurred while generating the API application";
                        log.error(errorMsg, e);
                        throw new APIManagerPublisherException(e);
                    }

                    try {
                        apiConfig.setOwner(APIUtil.getTenantAdminUserName(tenantDomain));
                        apiConfig.setTenantDomain(tenantDomain);
                        APIIdentifier apiIdentifier = new APIIdentifier(APIUtil.replaceEmailDomain(apiConfig.getOwner()),
                                apiConfig.getName(), apiConfig.getVersion());

                        APIInfo[] apiList = publisherRESTAPIServices.getApis();
                        boolean apiFound = false;
                        for (int i = 0; i < apiList.length; i++) {
                            APIInfo apiObj = apiList[i];
                            if (apiObj.getName().equals(apiIdentifier.getApiName().replace(Constants.SPACE,
                                    Constants.EMPTY_STRING))) {
                                apiFound = true;
                                apiIdentifier.setUuid(apiObj.getId());
                                break;
                            }
                        }
                        String apiUuid = apiIdentifier.getUUID();
                        if (!apiFound) {
                            // add new scopes as shared scopes
                            addNewSharedScope(apiConfig.getScopes(), publisherRESTAPIServices);
                            APIInfo api = getAPI(apiConfig, true);
                            APIInfo createdAPI = publisherRESTAPIServices.addAPI(api);
                            apiUuid = createdAPI.getId();
                            if (apiConfig.getEndpointType() != null && "WS".equals(apiConfig.getEndpointType())) {
                                publisherRESTAPIServices.saveAsyncApiDefinition(apiUuid, apiConfig.getAsyncApiDefinition());
                            }
                            if (CREATED_STATUS.equals(createdAPI.getLifeCycleStatus())) {
                                // if endpoint type "dynamic" and then add in sequence
                                if ("dynamic".equals(apiConfig.getEndpointType())) {
                                    Mediation mediation = new Mediation();
                                    mediation.setName(apiConfig.getInSequenceName());
                                    mediation.setConfig(apiConfig.getInSequenceConfig());
                                    mediation.setType("in");
                                    mediation.setGlobal(false);
                                    publisherRESTAPIServices.addApiSpecificMediationPolicy(apiUuid, mediation);
                                }
                                publisherRESTAPIServices.changeLifeCycleStatus(apiUuid, PUBLISH_ACTION);

                                APIRevision apiRevision = new APIRevision();
                                apiRevision.setApiUUID(apiUuid);
                                apiRevision.setDescription("Initial Revision");
                                String apiRevisionId = publisherRESTAPIServices.addAPIRevision(apiRevision).getId();

                                APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
                                apiRevisionDeployment.setName(API_PUBLISH_ENVIRONMENT);
                                apiRevisionDeployment.setVhost(System.getProperty("iot.gateway.host"));
                                apiRevisionDeployment.setDisplayOnDevportal(true);

                                List<APIRevisionDeployment> apiRevisionDeploymentList = new ArrayList<>();
                                apiRevisionDeploymentList.add(apiRevisionDeployment);
                                publisherRESTAPIServices.deployAPIRevision(apiUuid, apiRevisionId, apiRevisionDeploymentList);
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

                                // It is guaranteed that there is no local scope if we update from 5.0.0 to the most
                                // recent version. Therefore, if the scope is not already available as a shared scope,
                                // new scopes must be added as shared scopes. Additionally, it is necessary to
                                // upgrade to 5.0.0 first before updating from 5.0.0 to the most recent version if we
                                // are updating from a version that is older than 5.0.0.

                                addNewSharedScope(apiConfig.getScopes(), publisherRESTAPIServices);

                                // Get existing API
                                APIInfo existingAPI = publisherRESTAPIServices.getApi(apiUuid);
                                APIInfo api = getAPI(apiConfig, true);
                                api.setLifeCycleStatus(existingAPI.getLifeCycleStatus());
                                api.setId(apiUuid);
                                publisherRESTAPIServices.updateApi(api);

                                if (apiConfig.getEndpointType() != null && "WS".equals(apiConfig.getEndpointType())) {
                                    publisherRESTAPIServices.saveAsyncApiDefinition(apiUuid, apiConfig.getAsyncApiDefinition());
                                }

                                // if endpoint type "dynamic" and then add /update in sequence
                                if ("dynamic".equals(apiConfig.getEndpointType())) {
                                    Mediation mediation = new Mediation();
                                    mediation.setName(apiConfig.getInSequenceName());
                                    mediation.setConfig(apiConfig.getInSequenceConfig());
                                    mediation.setType("in");
                                    mediation.setGlobal(false);

                                    MediationPolicy[] mediationList = publisherRESTAPIServices
                                            .getAllApiSpecificMediationPolicies(apiUuid);

                                    boolean isMediationPolicyFound = false;
                                    for (int i = 0; i < mediationList.length; i++) {
                                        MediationPolicy mediationPolicy = mediationList[i];
                                        if (apiConfig.getInSequenceName().equals(mediationPolicy.getName())) {
                                            mediation.setUuid(mediationPolicy.getId());
                                            publisherRESTAPIServices.deleteApiSpecificMediationPolicy(apiUuid, mediation);
                                            publisherRESTAPIServices.addApiSpecificMediationPolicy(apiUuid, mediation);
                                            isMediationPolicyFound = true;
                                            break;
                                        }
                                    }
                                    if (!isMediationPolicyFound) {
                                        publisherRESTAPIServices.addApiSpecificMediationPolicy(apiUuid, mediation);
                                    }
                                }

                                int apiRevisionCount = publisherRESTAPIServices.getAPIRevisions(apiUuid, null).length;
                                if (apiRevisionCount >= 5) {
                                    // This will retrieve the deployed revision
                                    APIRevision[] revisionDeploymentList = publisherRESTAPIServices.getAPIRevisions(apiUuid, true);
                                    if (revisionDeploymentList.length > 0) {
                                        APIRevision latestRevisionDeployment = revisionDeploymentList[0];
                                        publisherRESTAPIServices.undeployAPIRevisionDeployment(latestRevisionDeployment, apiUuid);
                                    }
                                    // This will retrieve the undeployed revision list
                                    APIRevision[] undeployedRevisionList = publisherRESTAPIServices.getAPIRevisions(apiUuid, false);
                                    if (undeployedRevisionList.length > 0) {
                                        APIRevision earliestUndeployRevision = undeployedRevisionList[0];
                                        publisherRESTAPIServices.deleteAPIRevision(earliestUndeployRevision, apiUuid);
                                    }
                                }

                                // create new revision
                                APIRevision apiRevision = new APIRevision();
                                apiRevision.setApiUUID(apiUuid);
                                apiRevision.setDescription("Updated Revision");
                                String apiRevisionId = publisherRESTAPIServices.addAPIRevision(apiRevision).getId();

                                APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
                                apiRevisionDeployment.setName(API_PUBLISH_ENVIRONMENT);
                                apiRevisionDeployment.setVhost(System.getProperty("iot.gateway.host"));
                                apiRevisionDeployment.setDisplayOnDevportal(true);

                                List<APIRevisionDeployment> apiRevisionDeploymentList = new ArrayList<>();
                                apiRevisionDeploymentList.add(apiRevisionDeployment);

                                publisherRESTAPIServices.deployAPIRevision(apiUuid, apiRevisionId, apiRevisionDeploymentList);

                                if (CREATED_STATUS.equals(existingAPI.getLifeCycleStatus())) {
                                    publisherRESTAPIServices.changeLifeCycleStatus(apiUuid, PUBLISH_ACTION);
                                }
                            }
                        }
                        if (apiUuid != null && apiConfig.getApiDocumentationSourceFile() != null) {
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

                            Documentation apiDocumentation = new Documentation(Documentation.DocumentationType.HOWTO, apiConfig.getApiDocumentationName());
                            apiDocumentation.setVisibility(Documentation.DocumentVisibility.API_LEVEL);
                            apiDocumentation.setSourceType(Documentation.DocumentSourceType.MARKDOWN);
                            apiDocumentation.setCreatedDate(new Date());
                            apiDocumentation.setLastUpdatedTime(new Date());
                            apiDocumentation.setSummary(apiConfig.getApiDocumentationSummary());
                            apiDocumentation.setOtherTypeName(null);

                            Documentation[] documentList = publisherRESTAPIServices.getDocumentations(apiUuid);

                            if (documentList.length > 0) {
                                for (int i = 0; i < documentList.length; i++) {
                                    Documentation existingDoc = documentList[i];
                                    if (existingDoc.getName().equals(apiConfig.getApiDocumentationName())
                                            && existingDoc.getType().equals(Documentation.DocumentationType.HOWTO.name())) {
                                        publisherRESTAPIServices.deleteDocumentations(apiUuid, existingDoc.getDocumentId());
                                    }
                                }
                            } else {
                                log.info("There is no any existing api documentation.");
                            }

                            Documentation createdDoc = publisherRESTAPIServices.addDocumentation(apiUuid, apiDocumentation);

                            publisherRESTAPIServices.addDocumentationContent(apiUuid, createdDoc.getDocumentId(), docContent);

                        }
                    } catch (APIManagementException | IOException | APIServicesException |
                             BadRequestException | UnexpectedResponseException e) {
                        String msg = "Error occurred while publishing api";
                        log.error(msg, e);
                        throw new APIManagerPublisherException(e);
                    } finally {
                        APIPublisherUtils.removeScopePublishUserIfExists(tenantDomain);
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

    /**
     * Add new Shared Scopes
     *
     * @param apiScopes set of API scopes
     * @param publisherRESTAPIServices {@link PublisherRESTAPIServices}
     * @throws BadRequestException if invalid payload receives to add new shared scopes.
     * @throws UnexpectedResponseException if the response is not either 200 or 400.
     * @throws APIServicesException if error occurred while processing the response.
     */
    private void addNewSharedScope(Set<ApiScope> apiScopes, PublisherRESTAPIServices publisherRESTAPIServices) throws BadRequestException, UnexpectedResponseException, APIServicesException {
        for (ApiScope apiScope : apiScopes) {
            if (!publisherRESTAPIServices.isSharedScopeNameExists(apiScope.getKey())) {
                Scope scope = new Scope();
                scope.setName(apiScope.getKey());
                scope.setDescription(apiScope.getDescription());
                scope.setDisplayName(apiScope.getName());
                List<String> bindings = new ArrayList<>(apiScope.getRoles());
                bindings.add(ADMIN_ROLE_KEY);
                scope.setBindings(bindings);
                publisherRESTAPIServices.addNewSharedScope(scope);
            }
        }
    }

    @Override
    public void addDefaultScopesIfNotExist(List<DefaultPermission> defaultPermissions) throws APIManagerPublisherException {
        WebappPublisherConfig config = WebappPublisherConfig.getInstance();
        List<String> tenants = new ArrayList<>(Collections.singletonList(APIConstants.SUPER_TENANT_DOMAIN));
        tenants.addAll(config.getTenants().getTenant());

        APIApplicationServices apiApplicationServices = APIPublisherDataHolder.getInstance().getApiApplicationServices();
        PublisherRESTAPIServices publisherRESTAPIServices = APIPublisherDataHolder.getInstance().getPublisherRESTAPIServices();

        for (String tenantDomain : tenants) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                Scope scope = new Scope();
                for (DefaultPermission defaultPermission : defaultPermissions) {
                    if (!publisherRESTAPIServices.isSharedScopeNameExists(defaultPermission.getScopeMapping().getKey())) {
                        ScopeMapping scopeMapping = defaultPermission.getScopeMapping();
                        List<String> bindings = new ArrayList<>(
                                Arrays.asList(scopeMapping.getDefaultRoles().split(",")));
                        scope.setName(scopeMapping.getKey());
                        scope.setDescription(scopeMapping.getName());
                        scope.setDisplayName(scopeMapping.getName());
                        scope.setBindings(bindings);
                        publisherRESTAPIServices.addNewSharedScope(scope);
                    }
                }
            } catch (BadRequestException e) {
                String errorMsg = "Bad request while adding default scopes for tenant: " + tenantDomain;
                log.error(errorMsg, e);
                throw new APIManagerPublisherException(errorMsg, e);
            } catch (UnexpectedResponseException e) {
                String errorMsg = "Unexpected response while adding default scopes for tenant: " + tenantDomain;
                log.error(errorMsg, e);
                throw new APIManagerPublisherException(errorMsg, e);
            } catch (APIServicesException e) {
                String errorMsg = "API services exception while adding default scopes for tenant: " + tenantDomain;
                log.error(errorMsg, e);
                throw new APIManagerPublisherException(errorMsg, e);
            } finally {
                APIPublisherUtils.removeScopePublishUserIfExists(tenantDomain);
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }


    @Override
    public void updateScopeRoleMapping()
            throws APIManagerPublisherException {
        // todo: This logic has written assuming all the scopes are now work as shared scopes
        WebappPublisherConfig config = WebappPublisherConfig.getInstance();
        List<String> tenants = new ArrayList<>(Collections.singletonList(APIConstants.SUPER_TENANT_DOMAIN));
        tenants.addAll(config.getTenants().getTenant());

        APIApplicationServices apiApplicationServices = APIPublisherDataHolder.getInstance().getApiApplicationServices();
        PublisherRESTAPIServices publisherRESTAPIServices = APIPublisherDataHolder.getInstance().getPublisherRESTAPIServices();

        APIApplicationKey apiApplicationKey;
        AccessTokenInfo accessTokenInfo;
        UserStoreManager userStoreManager;
        String fileName = null;

        for (String tenantDomain : tenants) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

                try {
                    fileName =
                            CarbonUtils.getCarbonConfigDirPath() + File.separator + "etc"
                                    + File.separator + tenantDomain + ".csv";
                    try {
                        userStoreManager = APIPublisherDataHolder.getInstance().getUserStoreManager();
                    } catch (UserStoreException e) {
                        log.error("Unable to retrieve user store manager for tenant: " + tenantDomain);
                        return;
                    }
                    if (Files.exists(Paths.get(fileName))) {
                        BufferedReader br = new BufferedReader(new FileReader(fileName));
                        int lineNumber = 0;
                        Map<Integer, String> roles = new HashMap<>();
                        Map<String, List<String>> rolePermissions = new HashMap<>();
                        String line;
                        String splitBy = ",";
                        while ((line = br.readLine()) != null) {  //returns a Boolean value
                            lineNumber++;
                            String[] scopeMapping = line.split(splitBy);    // use comma as separator
                            String role;
                            if (lineNumber == 1) { // skip titles
                                for (int i = 4; i < scopeMapping.length; i++) {
                                    role = scopeMapping[i];
                                    roles.put(i, role); // add roles to the map
                                    if (!"admin".equals(role)) {
                                        try {
                                            if (!userStoreManager.isExistingRole(role)) {
                                                try {
                                                    addRole(role);
                                                } catch (UserStoreException e) {
                                                    log.error("Error occurred when adding new role: " + role, e);
                                                }
                                            }
                                        } catch (UserStoreException e) {
                                            log.error("Error occurred when checking the existence of role: " + role, e);
                                        }
                                        rolePermissions.put(role, new ArrayList<>());
                                    }
                                }
                                continue;
                            }

                            Scope scope = new Scope();
                            scope.setDisplayName(
                                    scopeMapping[0] != null ? StringUtils.trim(scopeMapping[0]) : StringUtils.EMPTY);
                            scope.setDescription(
                                    scopeMapping[1] != null ? StringUtils.trim(scopeMapping[1]) : StringUtils.EMPTY);
                            scope.setName(
                                    scopeMapping[2] != null ? StringUtils.trim(scopeMapping[2]) : StringUtils.EMPTY);
                            //                        scope.setPermissions(
                            //                                scopeMapping[3] != null ? StringUtils.trim(scopeMapping[3]) : StringUtils.EMPTY);
                            String permission = scopeMapping[3] != null ? StringUtils.trim(scopeMapping[3]) : StringUtils.EMPTY;

                            List<String> rolesList = new ArrayList<>();
                            for (int i = 4; i < scopeMapping.length; i++) {
                                if (scopeMapping[i] != null && StringUtils.trim(scopeMapping[i]).equals("Yes")) {
                                    rolesList.add(roles.get(i));
                                    if (rolePermissions.containsKey(roles.get(i)) && StringUtils.isNotEmpty(permission)) {
                                        rolePermissions.get(roles.get(i)).add(permission);
                                    }
                                }
                            }
                            //Set scope details which related to the scope key
                            Scope[] scopes = publisherRESTAPIServices.getScopes();
                            for (int i = 0; i < scopes.length; i++) {
                                Scope relatedScope = scopes[i];
                                if (relatedScope.getName().equals(scopeMapping[2].toString())) {
                                    scope.setId(relatedScope.getId());
                                    scope.setUsageCount(relatedScope.getUsageCount());
                                    //Including already existing roles
                                    rolesList.addAll(relatedScope.getBindings());
                                }
                            }
                            scope.setBindings(rolesList);

                            if (publisherRESTAPIServices.isSharedScopeNameExists(scope.getName())) {
                                publisherRESTAPIServices.updateSharedScope(scope);
                                // todo: permission changed in update path, is not handled yet.
                            } else {
                                // This scope doesn't have an api attached.
                                log.warn(scope.getName() + " not available as shared, add as new scope");
                                publisherRESTAPIServices.addNewSharedScope(scope);
                                // add permission if not exist
                                try {
                                    PermissionUtils.putPermission(permission);
                                } catch (PermissionManagementException e) {
                                    log.error("Error when adding permission ", e);
                                }
                            }
                        }
                        for (String role : rolePermissions.keySet()) {
                            try {
                                updatePermissions(role, rolePermissions.get(role));
                            } catch (UserStoreException e) {
                                log.error("Error occurred when adding permissions to role: " + role, e);
                            }
                        }
                    }
                } catch (IOException | DirectoryIteratorException e) {
                    String errorMsg = "Failed to read scopes from file: '" + fileName + "'.";
                    log.error(errorMsg, e);
                    throw new APIManagerPublisherException(e);
                }
            } catch (APIServicesException e) {
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
            } finally {
                APIPublisherUtils.removeScopePublishUserIfExists(tenantDomain);
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    @Override
    public void updateScopeRoleMapping(String roleName, String[] permissions, String[] removedPermissions) throws APIManagerPublisherException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        APIApplicationServices apiApplicationServices = APIPublisherDataHolder.getInstance().getApiApplicationServices();
        PublisherRESTAPIServices publisherRESTAPIServices = APIPublisherDataHolder.getInstance().getPublisherRESTAPIServices();

        try {

            Scope[] scopeList = publisherRESTAPIServices.getScopes();

            Map<String, String> permScopeMap = APIPublisherDataHolder.getInstance().getPermScopeMapping();
            if (permissions.length != 0) {
                updateScopes(roleName, publisherRESTAPIServices, scopeList, permissions, permScopeMap, false);
            }
            if (removedPermissions.length != 0) {
                updateScopes(roleName, publisherRESTAPIServices, scopeList, removedPermissions, permScopeMap, true);
            }

            try {
                updatePermissions(roleName, Arrays.asList(permissions));
            } catch (UserStoreException e) {
                String errorMsg = "Error occurred when adding permissions to role: " + roleName;
                log.error(errorMsg, e);
                throw new APIManagerPublisherException(errorMsg, e);
            }
        } catch (APIServicesException e) {
            String errorMsg = "Error while processing Publisher REST API response";
            log.error(errorMsg, e);
            throw new APIManagerPublisherException(errorMsg, e);
        } catch (BadRequestException e) {
            String errorMsg = "Error while calling Publisher REST APIs";
            log.error(errorMsg, e);
            throw new APIManagerPublisherException(errorMsg, e);
        } catch (UnexpectedResponseException e) {
            String errorMsg = "Unexpected response from the server";
            log.error(errorMsg, e);
            throw new APIManagerPublisherException(errorMsg, e);
        } finally {
            APIPublisherUtils.removeScopePublishUserIfExists(tenantDomain);
        }
    }

    /**
     * Update Scopes
     *
     * @param roleName Role Name
     * @param publisherRESTAPIServices {@link PublisherRESTAPIServices}
     * @param scopeList scope list returning from APIM
     * @param permissions List of permissions
     * @param permScopeMap Permission Scope map
     * @param removingPermissions if list of permissions has to be removed from the role send true, otherwise sends false.
     * @throws APIManagerPublisherException If the method receives invalid permission to update.
     */
    private void updateScopes (String roleName, PublisherRESTAPIServices publisherRESTAPIServices,
                      Scope[] scopeList, String[] permissions, Map<String, String> permScopeMap, boolean removingPermissions )
            throws APIManagerPublisherException {
        for (String permission : permissions) {
            String scopeValue = permScopeMap.get(permission);
            if (scopeValue == null) {
                String msg = "Found invalid permission: " + permission + ". Hence aborting the scope role " +
                        "mapping process";
                log.error(msg);
                throw new APIManagerPublisherException(msg);
            }

            for (int i = 0; i < scopeList.length; i++) {
                Scope scopeObj = scopeList[i];
                if (scopeObj.getName().equals(scopeValue)) {
                    Scope scope = new Scope();
                    scope.setName(scopeObj.getName());
                    scope.setDisplayName(scopeObj.getDisplayName());
                    scope.setDescription(scopeObj.getDescription());
                    scope.setId(scopeObj.getId());

                    // Including already existing roles
                    List<String> existingRoleList = new ArrayList<>();
                    existingRoleList.addAll(scopeObj.getBindings());

                    if (!existingRoleList.contains(roleName)) {
                        existingRoleList.add(roleName);
                    }

                    if (removingPermissions) {
                        existingRoleList.remove(roleName);
                    } else {
                        if (!existingRoleList.contains(roleName)) {
                            existingRoleList.add(roleName);
                        }
                    }
                    scope.setBindings(existingRoleList);

                    try {
                        if (publisherRESTAPIServices.isSharedScopeNameExists(scope.getName())) {
                            publisherRESTAPIServices.updateSharedScope(scope);
                        } else {
                            // todo: come to this level means, that scope is removed from API, but haven't removed from the scope-role-permission-mappings list
                            log.warn(scope.getName() + " not available as shared scope");
                        }
                    } catch (APIServicesException | BadRequestException | UnexpectedResponseException  e) {
                        log.error("Error occurred while updating role scope mapping via APIM REST endpoint.", e);
                    }
                    break;
                }
            }
        }
    }

    private void updatePermissions(String role, List<String> permissions) throws UserStoreException {
        if (role == null || permissions == null) return;
        AuthorizationManager authorizationManager = APIPublisherDataHolder.getInstance().getUserRealm()
                .getAuthorizationManager();
        if (log.isDebugEnabled()) {
            log.debug("Updating the role '" + role + "'");
        }
        authorizationManager.clearRoleAuthorization(role);
        for (String permission : permissions) {
            authorizationManager.authorizeRole(role, permission, CarbonConstants.UI_PERMISSION_ACTION);
            authorizationManager.refreshAllowedRolesForResource(permission);
        }
    }

    private void addRole(String role) throws UserStoreException {
        UserStoreManager userStoreManager = APIPublisherDataHolder.getInstance().getUserStoreManager();
        if (log.isDebugEnabled()) {
            log.debug("Persisting the role " + role + " in the underlying user store");
        }
        userStoreManager.addRole(role, new String[]{"admin"}, new Permission[0]);
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

        List<String> tags = new ArrayList<>();
        tags.addAll(Arrays.asList(config.getTags()));
        apiInfo.setTags(tags);

        List<String> availableTiers = new ArrayList<>();
        if (config.getEndpointType() != null && "WS".equals(config.getEndpointType())) {
            availableTiers.add(WS_UNLIMITED_TIER);
        } else {
            availableTiers.add(UNLIMITED_TIER);
        }
        apiInfo.setPolicies(availableTiers);
        apiInfo.setApiThrottlingPolicy(UNLIMITED_TIER);

        //set operations and scopes
        List<Operations> operations = new ArrayList();
        List<JSONObject> scopeSet = new ArrayList();
        Iterator<ApiUriTemplate> iterator;

        for (iterator = config.getUriTemplates().iterator(); iterator.hasNext(); ) {
            ApiUriTemplate apiUriTemplate = iterator.next();
            Operations operation = new Operations();
            operation.setTarget(apiUriTemplate.getUriTemplate());
            operation.setVerb(apiUriTemplate.getHttpVerb());
            operation.setAuthType(apiUriTemplate.getAuthType());
            operation.setThrottlingPolicy(UNLIMITED_TIER);
            operation.setUriMapping(apiUriTemplate.getUriMapping());
            if (includeScopes) {
                if (apiUriTemplate.getScope() != null) {
                    Scope scope = new Scope();
                    scope.setName(apiUriTemplate.getScope().getKey());
                    scope.setDisplayName(apiUriTemplate.getScope().getName());
                    scope.setDescription(apiUriTemplate.getScope().getDescription());
                    List<String> bindings = new ArrayList<>(apiUriTemplate.getScope().getRoles());
                    bindings.add(ADMIN_ROLE_KEY);
                    scope.setBindings(bindings);

                    JSONObject scopeObject = new JSONObject();
                    scopeObject.put("scope", new JSONObject(gson.toJson(scope)));
                    scopeObject.put("shared", true);

                    scopeSet.add(scopeObject);

                    List<String> scopes = new ArrayList<>();
                    scopes.addAll(Arrays.asList(apiUriTemplate.getScope().getKey()));
                    operation.setScopes(scopes);
                }
            }
            operations.add(operation);
        }
        apiInfo.setScopes(scopeSet);
        apiInfo.setOperations(operations);

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

        List<String> transports = new ArrayList<>();
        transports.addAll(Arrays.asList(config.getTransports().split(",")));
        apiInfo.setTransport(transports);

        apiInfo.setType("HTTP");

        if (config.getEndpointType() != null && "dynamic".equals(config.getEndpointType())) {
            endpointConfig = "{\n" +
                    "        \"endpoint_type\": \"default\",\n" +
                    "        \"sandbox_endpoints\": {\n" +
                    "            \"url\": \" default \"\n" +
                    "        },\n" +
                    "        \"production_endpoints\": {\n" +
                    "            \"url\": \" default \"\n" +
                    "        }\n" +
                    "    }";
            endPointConfig = new JSONObject(endpointConfig);
            apiInfo.setEndpointImplementationType("ENDPOINT");
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

            transports.add("wss");
            transports.add("ws");
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
        apiInfo.setWebsubSubscriptionConfiguration(null);
        apiInfo.setAdvertiseInfo(null);

        return apiInfo;
    }

    /**
     * This method will construct the permission scope mapping hash map. This will call in each API publish call.
     * @param scopes API Scopes
     */
    private void constructPemScopeMap(Set<ApiScope> scopes) {
        APIPublisherDataHolder apiPublisherDataHolder = APIPublisherDataHolder.getInstance();
        Map<String, String> permScopeMap = apiPublisherDataHolder.getPermScopeMapping();
        for (ApiScope scope : scopes) {
            permScopeMap.put(scope.getPermissions(), scope.getKey());
        }
        apiPublisherDataHolder.setPermScopeMapping(permScopeMap);
    }
}
