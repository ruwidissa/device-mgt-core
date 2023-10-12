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

package io.entgra.device.mgt.core.cea.mgt.enforce.service.gateway;

import io.entgra.device.mgt.core.cea.mgt.common.bean.ActiveSyncServer;
import io.entgra.device.mgt.core.cea.mgt.enforce.exception.GatewayServiceException;

public interface GatewayService {
    /**
     * Retrieve access token to invoke active sync server endpoints
     *
     * @param activeSyncServer {@link ActiveSyncServer}
     * @return Obtained access token
     * @throws GatewayServiceException Throws when error occurred while obtaining an access token
     */
    String acquireAccessToken(ActiveSyncServer activeSyncServer) throws GatewayServiceException;

    /**
     * Validate the access token
     *
     * @param activeSyncServer {@link ActiveSyncServer}
     * @return True when the token is valid, otherwise false
     * @throws GatewayServiceException Throws when error occurred while validating the token
     */
    boolean validate(ActiveSyncServer activeSyncServer) throws GatewayServiceException;
}
