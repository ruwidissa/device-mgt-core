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

package io.entgra.device.mgt.core.subtype.mgt.dao.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.entgra.device.mgt.core.subtype.mgt.dto.DeviceSubType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DAOUtil {

    public static DeviceSubType loadDeviceSubType(ResultSet rs) throws SQLException {
        DeviceSubType deviceSubType = new DeviceSubType() {
            @Override
            public <T> DeviceSubType convertToDeviceSubType() {
                return null;
            }

            @Override
            public String parseSubTypeToJson() throws JsonProcessingException {
                return null;
            }
        };
        deviceSubType.setTenantId(rs.getInt("TENANT_ID"));
        deviceSubType.setSubTypeId(rs.getString("SUB_TYPE_ID"));
        deviceSubType.setSubTypeName(rs.getString("SUB_TYPE_NAME"));
        deviceSubType.setDeviceType(rs.getString("DEVICE_TYPE"));
        deviceSubType.setTypeDefinition(rs.getString("TYPE_DEFINITION"));
        return deviceSubType;
    }

    public static List<DeviceSubType> loadDeviceSubTypes(ResultSet rs) throws SQLException {
        try {
            Map<String, DeviceSubType> deviceSubTypes = new LinkedHashMap<>();
            DeviceSubType deviceSubType = new DeviceSubType() {
                @Override
                public <T> DeviceSubType convertToDeviceSubType() {
                    return null;
                }

                @Override
                public String parseSubTypeToJson() throws JsonProcessingException {
                    return null;
                }
            };
            while (rs.next()) {
                String currentSubTypeId = rs.getString("SUB_TYPE_ID");
                String deviceType = rs.getString("DEVICE_TYPE");
                String operationCode = rs.getString("OPERATION_CODE");
                String key = deviceType + "|" + currentSubTypeId;
                if (!deviceSubTypes.containsKey(key)) {
                    deviceSubType = loadDeviceSubType(rs);
                }
                if (operationCode != null) {
                    deviceSubType.addSupportedOperation(operationCode);
                }
                deviceSubTypes.put(key, deviceSubType);
            }
            return new ArrayList<>(deviceSubTypes.values());
        } catch (Exception e) {
            return null;
        }
    }
}
