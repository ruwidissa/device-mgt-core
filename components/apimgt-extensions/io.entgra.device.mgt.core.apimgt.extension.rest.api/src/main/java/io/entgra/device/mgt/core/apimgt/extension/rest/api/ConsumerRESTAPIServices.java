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

package io.entgra.device.mgt.core.apimgt.extension.rest.api;

import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.*;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.ApiApplicationInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;

import java.util.List;
import java.util.Map;

public interface ConsumerRESTAPIServices {

    Application[] getAllApplications(ApiApplicationInfo apiApplicationInfo, String appName)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Application getDetailsOfAnApplication(ApiApplicationInfo apiApplicationInfo, String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Application createApplication(ApiApplicationInfo apiApplicationInfo, Application application)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Boolean deleteApplication(ApiApplicationInfo apiApplicationInfo, String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Subscription[] getAllSubscriptions(ApiApplicationInfo apiApplicationInfo, String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    APIInfo[] getAllApis(ApiApplicationInfo apiApplicationInfo, Map<String, String> queryParams, Map<String, String> headerParams)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Subscription createSubscription(ApiApplicationInfo apiApplicationInfo, Subscription subscriptions)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Subscription[] createSubscriptions(ApiApplicationInfo apiApplicationInfo, List<Subscription> subscriptions)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    ApplicationKey generateApplicationKeys(ApiApplicationInfo apiApplicationInfo, String applicationId, String keyManager,
                                           String validityTime, String keyType)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    ApplicationKey mapApplicationKeys(ApiApplicationInfo apiApplicationInfo, Application application, String keyManager, String keyType)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    ApplicationKey getKeyDetails(ApiApplicationInfo apiApplicationInfo, String applicationId, String keyMapId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    ApplicationKey updateGrantType(ApiApplicationInfo apiApplicationInfo, String applicationId, String keyMapId, String keyManager,
                                   List<String> supportedGrantTypes, String callbackUrl)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    KeyManager[] getAllKeyManagers(ApiApplicationInfo apiApplicationInfo)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;
}
