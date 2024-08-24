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


import io.entgra.device.mgt.core.apimgt.application.extension.dto.ApiApplicationKey;
import io.entgra.device.mgt.core.apimgt.application.extension.exception.APIManagerException;
import io.entgra.device.mgt.core.identity.jwt.client.extension.dto.AccessTokenInfo;

import java.util.ArrayList;

/**
 * This comprise on operation that is been done with api manager from CDMF. This service needs to be implemented in APIM.
 */
public interface APIManagementProviderService {

    /**
     * Check whether the tier is loaded for the tenant.
     * @return
     */
    boolean isTierLoaded();

    ApiApplicationKey generateAndRetrieveApplicationKeys(String applicationName, String[] tags,
                                                         String keyType, String username,
                                                         boolean isAllowedAllDomains,
                                                         String validityTime,
                                                         String password, String accessToken,
                                                         ArrayList<String> supportedGrantTypes,
                                                         String callbackUrl,
                                                         boolean isMappingRequired) throws APIManagerException;

    /**
     * To get access token for given scopes and for the given validity period
     * @param scopes Scopes
     * @param tags Tags
     * @param applicationName Application Name
     * @param tokenType Token Type
     * @param validityPeriod Validity Period
     * @param username Name of the user to create the token. If null, set as carbon context user
     * @return {@link String} Access Token
     * @throws APIManagerException if error occurred while getting the access token for given scopes,
     * validity period etc.
     */
    AccessTokenInfo getAccessToken(String scopes, String[] tags, String applicationName, String
            tokenType, String validityPeriod, String username)
            throws APIManagerException;

}
