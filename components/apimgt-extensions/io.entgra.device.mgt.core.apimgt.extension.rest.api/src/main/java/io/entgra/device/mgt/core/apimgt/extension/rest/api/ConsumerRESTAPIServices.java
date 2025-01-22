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
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Application;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.ApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.KeyManager;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Subscription;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.ApiApplicationInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;

import java.util.List;
import java.util.Map;

public interface ConsumerRESTAPIServices {

    Application[] getAllApplications(String appName)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Application getDetailsOfAnApplication(String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Application createApplication(Application application)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Boolean deleteApplication(String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Subscription[] getAllSubscriptions(String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    APIInfo[] getAllApis(Map<String, String> queryParams, Map<String, String> headerParams)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Subscription createSubscription(Subscription subscriptions)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    public ApplicationKey[] getAllKeys(String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Subscription[] createSubscriptions(List<Subscription> subscriptions)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    ApplicationKey generateApplicationKeys(String applicationId, String keyManager,
                                           String validityTime, String keyType, String grantTypesToBeSupported, String callbackUrl)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    ApplicationKey mapApplicationKeys(String consumerKey, String consumerSecret, Application application,
                                      String keyManager, String keyType)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    ApplicationKey getKeyDetails(String applicationId, String keyMapId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    ApplicationKey updateGrantType(String applicationId, String keyMapId, List<String> supportedGrantTypes, String callbackUrl)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    KeyManager[] getAllKeyManagers()
            throws APIServicesException, BadRequestException, UnexpectedResponseException;
}
