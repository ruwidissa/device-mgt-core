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
package io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.service;

import com.google.gson.JsonObject;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.common.exception.GrafanaManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DBConnectionException;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;

public interface GrafanaQueryService {

    void buildSafeQuery(JsonObject queryRequestBody, String dashboardUID, String panelId,
                               URI requestUri) throws IOException, SQLException, GrafanaManagementException,
            DBConnectionException, io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException;

}
