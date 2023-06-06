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

import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.APIInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.APIKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Application;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Subscription;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.KeyManager;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;

import java.util.List;
import java.util.Map;

public interface ConsumerRESTAPIServices {
    Application[] getAllApplications(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String appName)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Application getDetailsOfAnApplication(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                          String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Application createApplication(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                  Application application)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Subscription[] getAllSubscriptions(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                       String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    APIInfo[] getAllApis(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                         Map<String, String> queryParam)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Subscription createSubscription(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                    Subscription subscriptions)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Subscription[] createSubscriptions(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                       List<Subscription> subscriptions)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    APIKey generateApplicationKeys(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                   String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    KeyManager[] getAllKeyManagers(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;
}
