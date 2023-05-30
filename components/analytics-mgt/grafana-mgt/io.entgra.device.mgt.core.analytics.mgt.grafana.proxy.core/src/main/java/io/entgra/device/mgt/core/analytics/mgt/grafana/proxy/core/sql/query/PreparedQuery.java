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
package io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.sql.query;

import java.util.List;

public class PreparedQuery {

    public static final String PREPARED_SQL_PARAM_PLACEHOLDER = "?";

    private String preparedSQL;
    private List<String> parameters;

    public PreparedQuery(String preparedSQL, List<String> parameters) {
        this.preparedSQL = preparedSQL;
        this.parameters = parameters;
    }

    public String getPreparedSQL() {
        return preparedSQL;
    }

    public void setPreparedSQL(String preparedSQL) {
        this.preparedSQL = preparedSQL;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }
}
