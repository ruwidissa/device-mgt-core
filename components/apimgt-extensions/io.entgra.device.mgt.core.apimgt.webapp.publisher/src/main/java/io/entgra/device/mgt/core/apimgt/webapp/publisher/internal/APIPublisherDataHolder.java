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
package io.entgra.device.mgt.core.apimgt.webapp.publisher.internal;

import io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.PublisherRESTAPIServices;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIConfig;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIPublisherService;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.PostApiPublishingObsever;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


public class APIPublisherDataHolder {

    private APIPublisherService apiPublisherService;
    private ConfigurationContextService configurationContextService;
    private RealmService realmService;
    private TenantManager tenantManager;
    private RegistryService registryService;
    private boolean isServerStarted;
    private Stack<APIConfig> unpublishedApis = new Stack<>();
    private Map<String, String> permScopeMapping = new HashMap<>();
    private APIApplicationServices apiApplicationServices;
    private PublisherRESTAPIServices publisherRESTAPIServices;
    private MetadataManagementService metadataManagementService;

    private static APIPublisherDataHolder thisInstance = new APIPublisherDataHolder();

    private List<PostApiPublishingObsever> postApiPublishingObseverList = new ArrayList<>();
    private APIPublisherDataHolder() {
    }

    public static APIPublisherDataHolder getInstance() {
        return thisInstance;
    }

    public APIPublisherService getApiPublisherService() {
        if (apiPublisherService == null) {
            throw new IllegalStateException("APIPublisher service is not initialized properly");
        }
        return apiPublisherService;
    }

    public void setApiPublisherService(APIPublisherService apiPublisherService) {
        this.apiPublisherService = apiPublisherService;
    }

    public void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        if (configurationContextService == null) {
            throw new IllegalStateException("ConfigurationContext service is not initialized properly");
        }
        return configurationContextService;
    }

    public RealmService getRealmService() {
        if (realmService == null) {
            throw new IllegalStateException("Realm service is not initialized properly");
        }
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
        setTenantManager(realmService != null ?
                realmService.getTenantManager() : null);
    }

    public UserStoreManager getUserStoreManager() throws UserStoreException {
        if (realmService == null) {
            String msg = "Realm service has not initialized.";
            throw new IllegalStateException(msg);
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        return realmService.getTenantUserRealm(tenantId).getUserStoreManager();
    }

    public UserRealm getUserRealm() throws UserStoreException {
        UserRealm realm;
        if (realmService == null) {
            throw new IllegalStateException("Realm service not initialized");
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        realm = realmService.getTenantUserRealm(tenantId);
        return realm;
    }

    private void setTenantManager(TenantManager tenantManager) {
        this.tenantManager = tenantManager;
    }

    public TenantManager getTenantManager() {
        if (tenantManager == null) {
            throw new IllegalStateException("Tenant manager is not initialized properly");
        }
        return tenantManager;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public boolean isServerStarted() {
        return isServerStarted;
    }

    public void setServerStarted(boolean serverStarted) {
        isServerStarted = serverStarted;
    }

    public Stack<APIConfig> getUnpublishedApis() {
        return unpublishedApis;
    }

    public void setUnpublishedApis(Stack<APIConfig> unpublishedApis) {
        this.unpublishedApis = unpublishedApis;
    }

    public Map<String, String> getPermScopeMapping() {
        return permScopeMapping;
    }

    public void setPermScopeMapping(Map<String, String> permScopeMapping) {
        this.permScopeMapping = permScopeMapping;
    }

    public APIApplicationServices getApiApplicationServices() {
        return apiApplicationServices;
    }

    public void setApiApplicationServices(APIApplicationServices apiApplicationServices) {
        this.apiApplicationServices = apiApplicationServices;
    }

    public PublisherRESTAPIServices getPublisherRESTAPIServices() {
        return publisherRESTAPIServices;
    }

    public void setPublisherRESTAPIServices(PublisherRESTAPIServices publisherRESTAPIServices) {
        this.publisherRESTAPIServices = publisherRESTAPIServices;
    }

    public MetadataManagementService getMetadataManagementService() {
        return metadataManagementService;
    }

    public void setMetadataManagementService(MetadataManagementService metadataManagementService) {
        this.metadataManagementService = metadataManagementService;
    }

    public List<PostApiPublishingObsever> getPostApiPublishingObseverList() {
        return postApiPublishingObseverList;
    }

    public void addPostApiPublishingObseverList(PostApiPublishingObsever postApiPublishingObseverList) {
        this.postApiPublishingObseverList.add(postApiPublishingObseverList);
    }

    public void removePostApiPublishingObseverList(PostApiPublishingObsever postApiPublishingObsever) {
        this.postApiPublishingObseverList.remove(postApiPublishingObsever);
    }
}
