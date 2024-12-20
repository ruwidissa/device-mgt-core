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

import com.google.gson.Gson;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.PublisherRESTAPIServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Scope;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.util.APIPublisherUtils;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataKeyAlreadyExistsException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants.User;
import org.wso2.carbon.tenant.mgt.exception.TenantManagementException;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Load configuration files to tenant's registry.
 */
public class TenantCreateObserver extends AbstractAxis2ConfigurationContextObserver {
    private static final Log log = LogFactory.getLog(TenantCreateObserver.class);
    private String msg = null;

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
            if (!userStoreManager.isExistingRole(DeviceManagementConstants.User.DEFAULT_UI_EXECUTER)) {
                userStoreManager.addRole(DeviceManagementConstants.User.DEFAULT_UI_EXECUTER,
                        null,
                        null);
            }
            userStoreManager.updateRoleListOfUser(tenantAdminName, null,
                    new String[] {DeviceManagementConstants.User.DEFAULT_DEVICE_ADMIN,
                            DeviceManagementConstants.User.DEFAULT_DEVICE_USER});

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        publishScopesToTenant(tenantDomain);
                    } catch (TenantManagementException e) {
                        log.error("Error occurred while generating API application for  the tenant: " + tenantDomain + ".");
                    }
                }
            });
            thread.start();

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

    /**
     * This method will create OAuth application under the given tenant domain and generate an access token against the
     * client credentials. Once this access token is generated it will then be used to retrieve all the scopes that are already
     * published to that tenant space. The scopes of the super tenant will also be retrieved in order to compare which scopes were added
     * or removed. (A temporary admin user will be created in the sub tenant space to publish the scopes and will be deleted once
     * the scope publishing task is done)
     * @param tenantDomain tenant domain that the scopes will be published to.
     * @throws TenantManagementException if there are any errors when publishing scopes to a tenant
     */
    private void publishScopesToTenant(String tenantDomain) throws TenantManagementException {
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            APIApplicationServices apiApplicationServices = DeviceManagementDataHolder.getInstance().getApiApplicationServices();
            APIApplicationKey apiApplicationKey;
            AccessTokenInfo accessTokenInfo;

            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

                APIPublisherUtils.createScopePublishUserIfNotExists(tenantDomain);
                apiApplicationKey = apiApplicationServices.createAndRetrieveApplicationCredentials(
                        "ClientForScopePublish",
                        "client_credentials password refresh_token");
                accessTokenInfo = apiApplicationServices.generateAccessTokenFromRegisteredApplication(
                        apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
            } catch (APIServicesException e) {
                msg = "Error occurred while generating the API application for tenant: '" + tenantDomain + "'.";
                log.error(msg, e);
                throw new TenantManagementException(msg, e);
            }

            try {
                PublisherRESTAPIServices publisherRESTAPIServices = DeviceManagementDataHolder.getInstance().getPublisherRESTAPIServices();
                Scope[] superTenantScopes = getAllScopesFromSuperTenant(apiApplicationServices, publisherRESTAPIServices);

                if (superTenantScopes != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Number of super tenant scopes already published - " + superTenantScopes.length);
                    }

                    Scope[] subTenantScopes = publisherRESTAPIServices.getScopes(apiApplicationKey, accessTokenInfo);

                    if (subTenantScopes.length > 0) {
                        // If there is already existing scopes on the sub tenant space then do a comparison with the
                        // super tenant scopes to add those new scopes to sub tenant space or to delete them from
                        // sub tenant space if it is not existing on the super tenant scope list.

                        if (log.isDebugEnabled()) {
                            log.debug("Number of sub tenant scopes already published - " + subTenantScopes.length);
                        }

                        List<Scope> missingScopes = new ArrayList<>();
                        List<Scope> deletedScopes = new ArrayList<>();

                        for (Scope superTenantScope : superTenantScopes) {
                            boolean isMatchingScope = false;
                            for (Scope subTenantScope : subTenantScopes) {
                                if (superTenantScope.getName().equals(subTenantScope.getName())) {
                                    isMatchingScope = true;
                                    break;
                                }
                            }
                            if (!isMatchingScope) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Missing scope found in sub tenant space - " +
                                            superTenantScope.getName());
                                }
                                missingScopes.add(superTenantScope);
                            }
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("Total number of missing scopes found in sub tenant space - " +
                                    missingScopes.size());
                        }

                        if (missingScopes.size() > 0) {
                            if (log.isDebugEnabled()) {
                                log.debug("Starting to add new/updated shared scopes to the tenant: '" + tenantDomain + "'.");
                            }
                            publishSharedScopes(missingScopes, publisherRESTAPIServices, apiApplicationKey,
                                    accessTokenInfo);
                        }

                        for (Scope subTenantScope : subTenantScopes) {
                            boolean isMatchingScope = false;
                            for (Scope superTenantScope : superTenantScopes) {
                                if (superTenantScope.getName().equals(subTenantScope.getName())) {
                                    isMatchingScope = true;
                                    break;
                                }
                            }
                            if (!isMatchingScope) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Deleted scope found in sub tenant space - " +
                                            subTenantScope.getName());
                                }
                                deletedScopes.add(subTenantScope);
                            }
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("Total number of deleted scopes found in sub tenant space - " +
                                    deletedScopes.size());
                        }

                        if (deletedScopes.size() > 0) {
                            if (log.isDebugEnabled()) {
                                log.debug("Starting to delete shared scopes from the tenant: '" + tenantDomain + "'.");
                            }
                            for (Scope deletedScope : deletedScopes) {
                                if (publisherRESTAPIServices.isSharedScopeNameExists(apiApplicationKey, accessTokenInfo,
                                        deletedScope.getName())) {
                                    Scope scope = createScopeObject(deletedScope);
                                    publisherRESTAPIServices.deleteSharedScope(apiApplicationKey, accessTokenInfo, scope);
                                }
                            }
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Starting to publish shared scopes to newly created tenant: '" + tenantDomain + "'.");
                        }

                        publishSharedScopes(Arrays.asList(superTenantScopes), publisherRESTAPIServices,
                                apiApplicationKey, accessTokenInfo);
                    }
                } else {
                    msg = "Unable to publish scopes to sub tenants due to super tenant scopes list being empty.";
                    log.error(msg);
                    throw new TenantManagementException(msg);
                }
            } catch (BadRequestException e) {
                msg = "Invalid request sent when publishing scopes to '" + tenantDomain + "' tenant space.";
                log.error(msg, e);
                throw new TenantManagementException(msg, e);
            } catch (UnexpectedResponseException e) {
                msg = "Unexpected response received when publishing scopes to '" + tenantDomain + "' tenant space.";
                log.error(msg, e);
                throw new TenantManagementException(msg, e);
            } catch (APIServicesException e) {
                msg = "Error occurred while publishing scopes to '" + tenantDomain + "' tenant space.";
                log.error(msg, e);
                throw new TenantManagementException(msg, e);
            } finally {
                APIPublisherUtils.removeScopePublishUserIfExists(tenantDomain);
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * This method will retrieve the value of the permission scope mapping meta key stored in each tenant's metadata
     * @param tenantDomain the tenant domain that the permission scope mapping meta value retrieved from
     * @return {@link Map} containing the permission key and the scope value
     * @throws TenantManagementException if there is an error while retrieving permission scope metadata
     */
    private Map<String, String> getPermScopeMapping(String tenantDomain) throws TenantManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving permission scope mapping from metadata from the tenant: '" + tenantDomain + "'.");
        }
        Map<String, String> permScopeMapping = null;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            MetadataManagementService metadataManagementService = DeviceManagementDataHolder.getInstance().getMetadataManagementService();
            Metadata metadata = metadataManagementService.retrieveMetadata(Constants.PERM_SCOPE_MAPPING_META_KEY);
            if (metadata != null) {
                permScopeMapping = new Gson().fromJson(metadata.getMetaValue().toString(), HashMap.class);
            }
        } catch (MetadataManagementException e) {
            msg = "Error occurred while retrieving permission scope mapping from metadata for tenant: '" + tenantDomain + "'.";
            log.error(msg, e);
            throw new TenantManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return permScopeMapping;
    }

    /**
     * This method will create a new metadata entry or update the existing metadata entry in the sub tenant metadata repository which is
     * taken from the super tenant metadata
     * @param superTenantPermScopeMapping {@link Map} containing the permission key and the scope value of the super tenant
     * @param metadataManagementService {@link MetadataManagementService} instance
     * @throws MetadataManagementException if there is an error while creating or updating the metadata entry
     * @throws MetadataKeyAlreadyExistsException if the metadata key already exists while trying to create a new metadata entry
     */
    private void updatePermScopeMetaData(Map<String, String> superTenantPermScopeMapping,
                                         MetadataManagementService metadataManagementService) throws MetadataManagementException,
            MetadataKeyAlreadyExistsException {

        Metadata newMetaData = new Metadata();
        newMetaData.setMetaKey(Constants.PERM_SCOPE_MAPPING_META_KEY);
        newMetaData.setMetaValue(new Gson().toJson(superTenantPermScopeMapping));
        if (metadataManagementService.retrieveMetadata(Constants.PERM_SCOPE_MAPPING_META_KEY) == null) {
            metadataManagementService.createMetadata(newMetaData);
        } else {
            metadataManagementService.updateMetadata(newMetaData);
        }
    }

    /**
     * Get all the scopes from the super tenant space
     * @param apiApplicationServices {@link APIApplicationServices} is used to create an OAuth application and retrieve client ID and secret
     * @param publisherRESTAPIServices {@link PublisherRESTAPIServices} is used to get all scopes under a given tenant using client credentials
     * @return array of {@link Scope}
     * @throws BadRequestException if an invalid request is sent to the API Manager Publisher REST API Service
     * @throws UnexpectedResponseException if an unexpected response is received from the API Manager Publisher REST API Service
     * @throws TenantManagementException if an error occurred while processing the request sent to API Manager Publisher REST API Service
     */
    private Scope[] getAllScopesFromSuperTenant(APIApplicationServices apiApplicationServices,
                                                PublisherRESTAPIServices publisherRESTAPIServices) throws BadRequestException,
            UnexpectedResponseException, TenantManagementException {

        try {
            // Get all scopes of super tenant to compare later with the sub tenant scopes. This is done
            // in order to see if any new scopes were added or deleted
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            APIApplicationKey superTenantApiApplicationKey = apiApplicationServices.createAndRetrieveApplicationCredentials(
                    "ClientForScopePublish",
                    "client_credentials password refresh_token");
            AccessTokenInfo superTenantAccessToken = apiApplicationServices.generateAccessTokenFromRegisteredApplication(
                    superTenantApiApplicationKey.getClientId(), superTenantApiApplicationKey.getClientSecret());
            return publisherRESTAPIServices.getScopes(superTenantApiApplicationKey, superTenantAccessToken);
        } catch (APIServicesException e) {
            msg = "Error occurred while retrieving access token from super tenant";
            log.error(msg, e);
            throw new TenantManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Add shared scopes to the tenant space.
     * @param scopeList {@link List} of {@link Scope}
     * @param publisherRESTAPIServices {@link PublisherRESTAPIServices} is used to add shared scopes to a given tenant using client credentials
     * @param apiApplicationKey {@link APIApplicationKey} contains client credentials of the OAuth application
     * @param accessTokenInfo {@link AccessTokenInfo} contains token information generated from the client credentials
     * @throws BadRequestException if an invalid request is sent to the API Manager Publisher REST API Service
     * @throws UnexpectedResponseException if an unexpected response is received from the API Manager Publisher REST API Service
     * @throws APIServicesException if an error occurred while processing the request sent to API Manager Publisher REST API Service
     */
    private void publishSharedScopes (List<Scope> scopeList, PublisherRESTAPIServices publisherRESTAPIServices,
                                        APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo)
            throws BadRequestException, UnexpectedResponseException, APIServicesException {

        for (Scope tenantScope : scopeList) {
            if (!publisherRESTAPIServices.isSharedScopeNameExists(apiApplicationKey, accessTokenInfo,
                    tenantScope.getName())) {
                Scope scope = createScopeObject(tenantScope);
                publisherRESTAPIServices.addNewSharedScope(apiApplicationKey, accessTokenInfo, scope);
            }
        }
    }

    /**
     * Creates a new scope object from the passed scope which includes the id, display name, description, name and bindings.
     * @param tenantScope existing {@link Scope} from a tenant
     * @return {@link Scope}
     */
    private Scope createScopeObject (Scope tenantScope) {
        Scope scope = new Scope();
        scope.setId(tenantScope.getId());
        scope.setDisplayName(tenantScope.getDisplayName());
        scope.setDescription(tenantScope.getDescription());
        scope.setName(tenantScope.getName());
        List<String> existingBindings = tenantScope.getBindings();
        List<String> bindings = new ArrayList<>();
        if (existingBindings != null &&
                existingBindings.contains(DeviceManagementConstants.User.DEFAULT_UI_EXECUTER)) {
            bindings.add(DeviceManagementConstants.User.DEFAULT_UI_EXECUTER);
        } else {
            bindings.add(Constants.ADMIN_ROLE_KEY);
        }
        scope.setBindings(bindings);
        return scope;
    }
}