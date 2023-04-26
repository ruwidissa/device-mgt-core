/*
 * Copyright (c) 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.apimgt.extension.rest.api;

import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.model.Scope;

public interface PublisherRESTAPIServices {

    JSONObject getScopes(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean isSharedScopeNameExists(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String key)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean updateSharedScope(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, Scope scope)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;
}
