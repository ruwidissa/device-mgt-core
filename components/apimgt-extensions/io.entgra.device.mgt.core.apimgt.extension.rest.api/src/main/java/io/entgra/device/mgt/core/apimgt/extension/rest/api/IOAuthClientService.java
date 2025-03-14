/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.apimgt.extension.rest.api;

import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.IDNApplicationKeys;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.OAuthClientResponse;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.OAuthClientException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import okhttp3.Request;

public interface IOAuthClientService {
    /**
     * Handle execution of a APIM REST services invocation request. Token and cache handling will be handled by the
     * service itself.
     *
     * @param request Instance of {@link Request} to execute
     * @return Instance of {@link OAuthClientResponse} when successful invocation happens
     * @throws OAuthClientException        Throws when error encountered while sending the request
     * @throws BadRequestException         Throws when received of an invalid request
     * @throws UnexpectedResponseException Throws when unexpected response received
     */
    OAuthClientResponse execute(Request request) throws OAuthClientException, BadRequestException,
            UnexpectedResponseException;

    /**
     * Create and retrieve identity server side service provider applications
     *
     * @param clientName IDN client name
     * @return {@link IDNApplicationKeys}
     * @throws OAuthClientException Throws when error encountered while IDN client creation
     */
    IDNApplicationKeys getIdnApplicationKeys(String clientName, String grantTypes) throws OAuthClientException;
}
