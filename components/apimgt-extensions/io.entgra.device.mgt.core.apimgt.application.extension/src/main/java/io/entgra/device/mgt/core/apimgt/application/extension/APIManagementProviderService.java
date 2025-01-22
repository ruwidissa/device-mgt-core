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
package io.entgra.device.mgt.core.apimgt.application.extension;


import io.entgra.device.mgt.core.apimgt.application.extension.bean.ApiApplicationProfile;
import io.entgra.device.mgt.core.apimgt.application.extension.bean.Token;
import io.entgra.device.mgt.core.apimgt.application.extension.bean.TokenCreationProfile;
import io.entgra.device.mgt.core.apimgt.application.extension.bean.ApiApplicationKey;
import io.entgra.device.mgt.core.apimgt.application.extension.exception.APIManagerException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;

/**
 * This comprise on operation that is been done with api manager from CDMF. This service needs to be implemented in
 * APIM.
 */
public interface APIManagementProviderService {

    /**
     * Check whether the tier is loaded for the tenant.
     *
     * @return
     */
    boolean isTierLoaded();

    /**
     * Create and retrieve API application token base on {@link TokenCreationProfile}
     * @param tokenCreationProfile {@link TokenCreationProfile}
     * @return Retrieve {@link Token} result on a successful execution
     * @throws APIManagerException Throws when error occurred while retrieving the token
     */
    Token getToken(TokenCreationProfile tokenCreationProfile) throws APIManagerException;

    /**
     * Register API application base on {@link ApiApplicationProfile}
     * @param apiApplicationProfile {@link ApiApplicationProfile}
     * @return {@link ApiApplicationKey} result on a successful execution
     * @throws APIManagerException Throws when error encountered while registering the application profile
     * @throws BadRequestException Throws when the application profile contains invalid attributes
     * @throws UnexpectedResponseException Throws when unexpected response received from the REST API client
     */
    ApiApplicationKey registerApiApplication(ApiApplicationProfile apiApplicationProfile)
            throws APIManagerException, BadRequestException, UnexpectedResponseException;

    /**
     * Generate custom JWT token via extended JWT client
     * @param tokenCreationProfile {@link TokenCreationProfile}
     * @return Retrieve {@link Token} result on a successful execution
     * @throws APIManagerException Throws when error occurred while retrieving the token
     */
    Token getCustomToken(TokenCreationProfile tokenCreationProfile) throws APIManagerException;
}
