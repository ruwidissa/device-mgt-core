/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.analytics.mgt.grafana.proxy.core.service;

import com.google.gson.JsonObject;
import io.entgra.analytics.mgt.grafana.proxy.common.exception.GrafanaManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.DBConnectionException;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;

public interface GrafanaQueryService {

    void buildSafeQuery(JsonObject queryRequestBody, String dashboardUID, String panelId,
                               URI requestUri) throws IOException, SQLException, GrafanaManagementException,
            DBConnectionException, io.entgra.application.mgt.common.exception.DBConnectionException;

}
