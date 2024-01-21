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

package io.entgra.device.mgt.core.operation.template.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.entgra.device.mgt.core.operation.template.dto.OperationTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO Util class.
 */
public class DAOUtil {

    private DAOUtil() {
        throw new IllegalStateException("DAOUtil class");
    }

    /**
     *
     * @param rs
     * @return
     * @throws SQLException
     * @throws JsonSyntaxException
     */
    public static OperationTemplate loadOperationTemplate(ResultSet rs)
        throws SQLException, JsonSyntaxException {
        OperationTemplate operationTemplate = new OperationTemplate();
        operationTemplate.setSubTypeId(rs.getString("SUB_TYPE_ID"));
        operationTemplate.setCode(rs.getString("OPERATION_CODE"));
        operationTemplate.setDeviceType(rs.getString("DEVICE_TYPE"));
        operationTemplate.setOperationTemplateId(rs.getInt("SUB_OPERATION_TEMPLATE_ID"));
        operationTemplate.setOperationDefinition(rs.getString("OPERATION_DEFINITION"));

        return operationTemplate;
    }
}
