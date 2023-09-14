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
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.TokenInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;

import java.util.List;
import java.util.Map;

public interface ConsumerRESTAPIServices {
    Application[] getAllApplications(TokenInfo tokenInfo, String appName)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Application getDetailsOfAnApplication(TokenInfo tokenInfo, String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Application createApplication(TokenInfo tokenInfo, Application application)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Boolean deleteApplication(TokenInfo tokenInfo, String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Subscription[] getAllSubscriptions(TokenInfo tokenInfo, String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    APIInfo[] getAllApis(TokenInfo tokenInfo, Map<String, String> queryParams, Map<String, String> headerParams)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Subscription createSubscription(TokenInfo tokenInfo, Subscription subscriptions)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Subscription[] createSubscriptions(TokenInfo tokenInfo, List<Subscription> subscriptions)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    ApplicationKey generateApplicationKeys(TokenInfo tokenInfo, String applicationId, String keyManager, String validityTime, String keyType)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    ApplicationKey mapApplicationKeys(TokenInfo tokenInfo, Application application, String keyManager, String keyType)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    ApplicationKey getKeyDetails(TokenInfo tokenInfo, String applicationId, String keyMapId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    ApplicationKey updateGrantType(TokenInfo tokenInfo, String applicationId, String keyMapId, String keyManager,
                                   List<String> supportedGrantTypes, String callbackUrl)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    KeyManager[] getAllKeyManagers(TokenInfo tokenInfo)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;
}
