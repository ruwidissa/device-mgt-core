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
package io.entgra.device.mgt.core.tenant.mgt.core.internal;

import io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.PublisherRESTAPIServices;
import io.entgra.device.mgt.core.application.mgt.common.services.ApplicationManager;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.DeviceStatusManagementService;
import io.entgra.device.mgt.core.tenant.mgt.core.TenantManager;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.WhiteLabelManagementService;
import org.wso2.carbon.user.core.service.RealmService;

public class TenantMgtDataHolder {
    private static final TenantMgtDataHolder instance = new TenantMgtDataHolder();
    private TenantManager tenantManager;

    private ApplicationManager applicationManager;

    private WhiteLabelManagementService whiteLabelManagementService;

    private RealmService realmService;

    private DeviceStatusManagementService deviceStatusManagementService;

    private APIApplicationServices apiApplicationServices;

    private PublisherRESTAPIServices publisherRESTAPIServices;

    public RealmService getRealmService() {
        if (realmService == null) {
            throw new IllegalStateException("RealmService is not initialized.");
        }
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }

    public void setApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    public WhiteLabelManagementService getWhiteLabelManagementService() {
        return whiteLabelManagementService;
    }

    public void setWhiteLabelManagementService(WhiteLabelManagementService whiteLabelManagementService) {
        this.whiteLabelManagementService = whiteLabelManagementService;
    }

    public TenantManager getTenantManager() {
        return tenantManager;
    }

    public void setTenantManager(TenantManager tenantManager) {
        this.tenantManager = tenantManager;
    }

    public static TenantMgtDataHolder getInstance() {
        return instance;
    }

    public DeviceStatusManagementService getDeviceStatusManagementService() {
        return deviceStatusManagementService;
    }

    public void setDeviceStatusManagementService(DeviceStatusManagementService deviceStatusManagementService) {
        this.deviceStatusManagementService = deviceStatusManagementService;
    }

    /**
     * Retrieves the API Manager Publisher REST API Service instance from OSGI service context.
     * @return {@link PublisherRESTAPIServices} API Manager Publisher REST API Service
     */
    public PublisherRESTAPIServices getPublisherRESTAPIServices() {
        if (publisherRESTAPIServices == null) {
            throw new IllegalStateException("API Manager Publisher REST API Service was not initialized.");
        }
        return publisherRESTAPIServices;
    }

    public void setPublisherRESTAPIServices(PublisherRESTAPIServices publisherRESTAPIServices) {
        this.publisherRESTAPIServices = publisherRESTAPIServices;
    }
}
