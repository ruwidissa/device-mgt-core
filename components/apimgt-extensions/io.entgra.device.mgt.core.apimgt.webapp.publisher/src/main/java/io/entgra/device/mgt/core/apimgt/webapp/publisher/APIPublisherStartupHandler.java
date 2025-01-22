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
import io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.config.Tenants;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.config.WebappPublisherConfig;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.dto.ApiScope;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.exception.APIManagerPublisherException;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataKeyAlreadyExistsException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.common.permission.mgt.PermissionManagementException;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceManagementConfig;
import io.entgra.device.mgt.core.device.mgt.core.config.permission.DefaultPermission;
import io.entgra.device.mgt.core.device.mgt.core.config.permission.DefaultPermissions;
import io.entgra.device.mgt.core.device.mgt.core.permission.mgt.PermissionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerStartupObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public class APIPublisherStartupHandler implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(APIPublisherStartupHandler.class);
    private static final int CONNECTION_RETRY_FACTOR = 2;
    private static final int MAX_RETRY_COUNT = 5;
    private static final Gson gson = new Gson();
    private static final Stack<APIConfig> failedAPIsStack = new Stack<>();
    private static int retryTime = 2000;
    private static Stack<APIConfig> currentAPIsStack;
    private final List<String> publishedAPIs = new ArrayList<>();
    private APIPublisherService publisher;

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        APIPublisherDataHolder.getInstance().setServerStarted(true);
        currentAPIsStack = APIPublisherDataHolder.getInstance().getUnpublishedApis();
        Thread t = new Thread(() -> {
            if (log.isDebugEnabled()) {
                log.debug("Server has just started, hence started publishing unpublished APIs");
                log.debug("Total number of unpublished APIs: "
                        + APIPublisherDataHolder.getInstance().getUnpublishedApis().size());
            }
            publisher = APIPublisherDataHolder.getInstance().getApiPublisherService();
            int retryCount = 0;
            while (retryCount < MAX_RETRY_COUNT && (!failedAPIsStack.isEmpty() || !currentAPIsStack.isEmpty())) {
                if (retryCount > 0) {
                    try {
                        retryTime = retryTime * CONNECTION_RETRY_FACTOR;
                        Thread.sleep(retryTime);
                    } catch (InterruptedException te) {
                        //do nothing.
                    }
                }
                Stack<APIConfig> failedApis;
                if (!currentAPIsStack.isEmpty()) {
                    publishAPIs(currentAPIsStack, failedAPIsStack);
                    failedApis = failedAPIsStack;
                } else {
                    publishAPIs(failedAPIsStack, currentAPIsStack);
                    failedApis = currentAPIsStack;
                }
                retryCount++;
                if (retryCount == MAX_RETRY_COUNT && !failedApis.isEmpty()) {
                    StringBuilder error = new StringBuilder();
                    error.append("Error occurred while publishing API ['");
                    while (!failedApis.isEmpty()) {
                        APIConfig api = failedApis.pop();
                        error.append(api.getName() + ",");
                    }
                    error.append("']");
                    log.info(error.toString());
                }
            }

            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
            DefaultPermissions defaultPermissions = deviceManagementConfig.getDefaultPermissions();
            try {
                publisher.updateScopeRoleMapping();
                publisher.addDefaultScopesIfNotExist(defaultPermissions.getDefaultPermissions());
            } catch (APIManagerPublisherException e) {
                log.error("failed to update scope role mapping.", e);
            }

            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                updateScopeMetadataEntryAndRegistryWithDefaultScopes(defaultPermissions.getDefaultPermissions());
                updateApiPublishingEnabledTenants(tenantDomain);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }

            log.info("Successfully published : [" + publishedAPIs + "]. " +
                    "and failed : [" + failedAPIsStack + "] " +
                    "Total successful count : [" + publishedAPIs.size() + "]. " +
                    "Failed count : [" + failedAPIsStack.size() + "]");

            // execute after api publishing
            for (PostApiPublishingObsever observer : APIPublisherDataHolder.getInstance().getPostApiPublishingObseverList()) {
                if (log.isDebugEnabled()) {
                    log.debug("Executing " + observer.getClass().getName());
                }
                observer.execute();
            }
            log.info("Finish executing PostApiPublishingObsevers");
        });
        t.start();
        log.info("Starting API publishing procedure");
    }

/**
     * Publish apis provided by the API stack, if failed while publishing, then failed API will be added to
     * the failed API stack
     *
     * @param apis        Stack of APIs to publish
     * @param failedStack Stack to record failed APIs
     */
    private void publishAPIs(Stack<APIConfig> apis, Stack<APIConfig> failedStack) {
        while (!apis.isEmpty()) {
            APIConfig api = apis.pop();
            try {
                publisher.publishAPI(api);
                for (ApiScope scope : api.getScopes()) {
                    APIPublisherDataHolder.getInstance().getPermScopeMapping().putIfAbsent(scope.getPermissions(), scope.getKey());
                }
                publishedAPIs.add(api.getName());
                log.info("Successfully published API [" + api.getName() + "]");
            } catch (APIManagerPublisherException e) {
                log.error("failed to publish api.", e);
                failedStack.push(api);
            }
        }
    }

    /**
     * Update permission scope mapping entry with default scopes if perm-scope-mapping entry exists, otherwise this function
     * will create that entry and update the value with default permissions.
     */
    public static void updateScopeMetadataEntryAndRegistryWithDefaultScopes(List<DefaultPermission> defaultPermissions) {
        Map<String, String> permScopeMap = APIPublisherDataHolder.getInstance().getPermScopeMapping();
        Metadata permScopeMapping;

        MetadataManagementService metadataManagementService = APIPublisherDataHolder.getInstance().getMetadataManagementService();

        try {
            permScopeMapping = metadataManagementService.retrieveMetadata(Constants.PERM_SCOPE_MAPPING_META_KEY);
            boolean entryAlreadyExists = permScopeMapping != null;
            if (permScopeMap == null || permScopeMap.isEmpty()) {
                permScopeMap = entryAlreadyExists ? gson.fromJson(permScopeMapping.getMetaValue(), HashMap.class) :
                        new HashMap<>();
            }

            for (DefaultPermission defaultPermission : defaultPermissions) {
                permScopeMap.putIfAbsent(defaultPermission.getName(), defaultPermission.getScopeMapping().getKey());
                PermissionUtils.putPermission(defaultPermission.getName());
            }

            permScopeMapping = new Metadata();
            permScopeMapping.setMetaKey(Constants.PERM_SCOPE_MAPPING_META_KEY);
            permScopeMapping.setMetaValue(gson.toJson(permScopeMap));

            if (entryAlreadyExists) {
                metadataManagementService.updateMetadata(permScopeMapping);
            } else {
                metadataManagementService.createMetadata(permScopeMapping);
            }

            APIPublisherDataHolder.getInstance().setPermScopeMapping(permScopeMap);
            log.info(Constants.PERM_SCOPE_MAPPING_META_KEY + "entry updated successfully");
        } catch (MetadataKeyAlreadyExistsException e) {
            log.error("Metadata entry already exists for " + Constants.PERM_SCOPE_MAPPING_META_KEY, e);
        } catch (MetadataManagementException e) {
            log.error("Error encountered while updating permission scope mapping metadata with default scopes", e);
        } catch (PermissionManagementException e) {
            log.error("Error when adding default permission to the registry", e);
        }
    }

    private void updateApiPublishingEnabledTenants(String superTenantDomain) {
        MetadataManagementService metadataManagementService = APIPublisherDataHolder.getInstance().getMetadataManagementService();
        WebappPublisherConfig webappPublisherConfig = WebappPublisherConfig.getInstance();

        Metadata tenantsEntry = new Metadata();
        List<String> tenants = new ArrayList<>();

        tenants.add(superTenantDomain);
        tenants.addAll(webappPublisherConfig.getTenants().getTenant());

        tenantsEntry.setMetaKey(Constants.API_PUBLISHING_ENABLED_TENANT_LIST_KEY);
        tenantsEntry.setMetaValue(gson.toJson(tenants));

        try {
            if (metadataManagementService.retrieveMetadata(Constants.API_PUBLISHING_ENABLED_TENANT_LIST_KEY) == null) {
                metadataManagementService.createMetadata(tenantsEntry);
                return;
            }

            metadataManagementService.updateMetadata(tenantsEntry);
        } catch (MetadataKeyAlreadyExistsException e) {
            String msg = "Metadata entry already exists for " + Constants.API_PUBLISHING_ENABLED_TENANT_LIST_KEY;
            log.error(msg, e);
            throw new IllegalStateException(msg, e);
        } catch (MetadataManagementException e) {
            String msg = "Error encountered while updating api publish enabled tenants metadata entry";
            log.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }
}
