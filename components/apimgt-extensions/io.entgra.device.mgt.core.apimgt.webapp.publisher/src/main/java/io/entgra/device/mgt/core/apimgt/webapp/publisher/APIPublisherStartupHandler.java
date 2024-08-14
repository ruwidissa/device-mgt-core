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
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataKeyAlreadyExistsException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceManagementConfig;
import io.entgra.device.mgt.core.device.mgt.core.config.permission.DefaultPermission;
import io.entgra.device.mgt.core.device.mgt.core.config.permission.DefaultPermissions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.exception.APIManagerPublisherException;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import org.wso2.carbon.core.ServerStartupObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class APIPublisherStartupHandler implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(APIPublisherStartupHandler.class);
    private static int retryTime = 2000;
    private static final int CONNECTION_RETRY_FACTOR = 2;
    private static final int MAX_RETRY_COUNT = 5;
    private static Stack<APIConfig> failedAPIsStack = new Stack<>();
    private static Stack<APIConfig> currentAPIsStack;
    private static final Gson gson = new Gson();

    private APIPublisherService publisher;

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {
        APIPublisherDataHolder.getInstance().setServerStarted(true);
        currentAPIsStack = APIPublisherDataHolder.getInstance().getUnpublishedApis();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (log.isDebugEnabled()) {
                    log.debug("Server has just started, hence started publishing unpublished APIs");
                    log.debug("Total number of unpublished APIs: "
                            + APIPublisherDataHolder.getInstance().getUnpublishedApis().size());
                }
                publisher = APIPublisherDataHolder.getInstance().getApiPublisherService();
                int retryCount = 0;
                while (retryCount < MAX_RETRY_COUNT && (!failedAPIsStack.isEmpty() || !currentAPIsStack.isEmpty())) {
                    try {
                        retryTime = retryTime * CONNECTION_RETRY_FACTOR;
                        Thread.sleep(retryTime);
                    } catch (InterruptedException te) {
                        //do nothing.
                    }
                    Stack<APIConfig> failedApis;
                    if (!APIPublisherDataHolder.getInstance().getUnpublishedApis().isEmpty()) {
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
                        log.error(error.toString());
                    }
                }

                try {
                    publisher.updateScopeRoleMapping();
                    publisher.addDefaultScopesIfNotExist();
                } catch (APIManagerPublisherException e) {
                    log.error("failed to update scope role mapping.", e);
                }

                updateScopeMetadataEntryWithDefaultScopes();

                // execute after api publishing
                for (PostApiPublishingObsever observer : APIPublisherDataHolder.getInstance().getPostApiPublishingObseverList()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Executing " + observer.getClass().getName());
                    }
                    observer.execute();
                }
                log.info("Finish executing PostApiPublishingObsevers");
            }
        });
        t.start();
    }

    private void publishAPIs(Stack<APIConfig> apis, Stack<APIConfig> failedStack) {
        while (!apis.isEmpty()) {
            APIConfig api = apis.pop();
            try {
                publisher.publishAPI(api);
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
    private void updateScopeMetadataEntryWithDefaultScopes() {
        MetadataManagementService metadataManagementService = APIPublisherDataHolder.getInstance().getMetadataManagementService();
        try {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
            DefaultPermissions defaultPermissions = deviceManagementConfig.getDefaultPermissions();
            Metadata permScopeMapping = metadataManagementService.retrieveMetadata(Constants.PERM_SCOPE_MAPPING_META_KEY);
            Map<String, String> permScopeMap = (permScopeMapping != null) ? gson.fromJson(permScopeMapping.getMetaValue(), HashMap.class) :
                    new HashMap<>();
            for (DefaultPermission defaultPermission : defaultPermissions.getDefaultPermissions()) {
                permScopeMap.putIfAbsent(defaultPermission.getName(),
                        defaultPermission.getScopeMapping().getKey());
            }

            APIPublisherDataHolder.getInstance().setPermScopeMapping(permScopeMap);
            if (permScopeMapping != null) {
                permScopeMapping.setMetaValue(gson.toJson(permScopeMap));
                metadataManagementService.updateMetadata(permScopeMapping);
                return;
            }

            permScopeMapping = new Metadata();
            permScopeMapping.setMetaKey(Constants.PERM_SCOPE_MAPPING_META_KEY);
            permScopeMapping.setMetaValue(gson.toJson(permScopeMap));
            metadataManagementService.createMetadata(permScopeMapping);
        } catch (MetadataManagementException e) {
            log.error("Error encountered while updating permission scope mapping metadata with default scopes");
        } catch (MetadataKeyAlreadyExistsException e) {
            log.error("Metadata entry already exists for " + Constants.PERM_SCOPE_MAPPING_META_KEY);
        }
    }

}
