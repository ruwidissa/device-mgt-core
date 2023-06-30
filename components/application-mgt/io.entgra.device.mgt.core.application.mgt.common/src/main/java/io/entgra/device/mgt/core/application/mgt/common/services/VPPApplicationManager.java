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

package io.entgra.device.mgt.core.application.mgt.common.services;

import io.entgra.device.mgt.core.application.mgt.common.dto.ProxyResponse;
import io.entgra.device.mgt.core.application.mgt.common.dto.VppAssetDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.VppUserDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;

import java.io.IOException;

public interface VPPApplicationManager {

    VppUserDTO addUser(VppUserDTO userDTO) throws ApplicationManagementException;

    VppUserDTO getUserByDMUsername(String emmUsername) throws ApplicationManagementException;

    void updateUser(VppUserDTO userDTO) throws ApplicationManagementException;

    void syncUsers(String clientId) throws ApplicationManagementException;

    void syncAssets(int nextPageIndex) throws ApplicationManagementException;

    VppAssetDTO getAssetByAppId(int appId) throws ApplicationManagementException;

    ProxyResponse callVPPBackend(String url, String payload, String accessToken, String method) throws IOException;
}
