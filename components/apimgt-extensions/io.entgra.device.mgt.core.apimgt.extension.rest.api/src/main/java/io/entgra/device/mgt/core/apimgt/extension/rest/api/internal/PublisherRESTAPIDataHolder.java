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
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;

public class PublisherRESTAPIDataHolder {

    private APIApplicationServices apiApplicationServices;
    private APIManagerConfigurationService apiManagerConfigurationService;

    private static PublisherRESTAPIDataHolder thisInstance = new PublisherRESTAPIDataHolder();

    private PublisherRESTAPIDataHolder() {
    }

    static PublisherRESTAPIDataHolder getInstance() {
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

}
