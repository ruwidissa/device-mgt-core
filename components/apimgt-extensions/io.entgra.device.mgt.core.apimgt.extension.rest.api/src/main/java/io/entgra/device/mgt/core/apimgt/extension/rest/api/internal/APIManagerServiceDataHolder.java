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

package io.entgra.device.mgt.core.apimgt.extension.rest.api.internal;

import io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.IOAuthClientService;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.PublisherRESTAPIServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.ConsumerRESTAPIServices;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

public class APIManagerServiceDataHolder {
    private static final Log log = LogFactory.getLog(APIManagerServiceDataHolder.class);
    private APIApplicationServices apiApplicationServices;
    private APIManagerConfigurationService apiManagerConfigurationService;
    private PublisherRESTAPIServices publisherRESTAPIServices;
    private RealmService realmService;
    private TenantManager tenantManager;
    private IOAuthClientService ioAuthClientService;

    private static APIManagerServiceDataHolder thisInstance = new APIManagerServiceDataHolder();

    private ConsumerRESTAPIServices consumerRESTAPIServices;

    private APIManagerServiceDataHolder() {
    }

    public static APIManagerServiceDataHolder getInstance() {
        return thisInstance;
    }

    public APIApplicationServices getApiApplicationServices() {
        return apiApplicationServices;
    }

    public void setApiApplicationServices(APIApplicationServices apiApplicationServices) {
        this.apiApplicationServices = apiApplicationServices;
    }

    public void setAPIManagerConfiguration(APIManagerConfigurationService apiManagerConfigurationService) {
        this.apiManagerConfigurationService = apiManagerConfigurationService;
    }

    public APIManagerConfigurationService getAPIManagerConfigurationService() {
        if (apiManagerConfigurationService == null) {
            throw new IllegalStateException("API Manager Configuration service is not initialized properly");
        }
        return apiManagerConfigurationService;
    }


    public PublisherRESTAPIServices getPublisherRESTAPIServices() {
        return publisherRESTAPIServices;
    }

    public void setPublisherRESTAPIServices(PublisherRESTAPIServices publisherRESTAPIServices) {
        this.publisherRESTAPIServices = publisherRESTAPIServices;
    }

    public RealmService getRealmService() {
        if (realmService == null) {
            throw new IllegalStateException("Realm service is not initialized properly");
        }
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
        this.setTenantManager(realmService);
    }

    public TenantManager getTenantManager() {
        return tenantManager;
    }

    private void setTenantManager(RealmService realmService) {
        if (realmService == null) {
            throw new IllegalStateException("Realm service is not initialized properly");
        }
        this.tenantManager = realmService.getTenantManager();
    }

    public ConsumerRESTAPIServices getConsumerRESTAPIServices() {
        return consumerRESTAPIServices;
    }

    public void setConsumerRESTAPIServices(ConsumerRESTAPIServices consumerRESTAPIServices) {
        this.consumerRESTAPIServices = consumerRESTAPIServices;
    }

    public IOAuthClientService getIoAuthClientService() {
        if (ioAuthClientService == null) {
            String msg = "OAuth client service is not initialized properly";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return ioAuthClientService;
    }

    public void setIoAuthClientService(IOAuthClientService ioAuthClientService) {
        this.ioAuthClientService = ioAuthClientService;
    }
}
