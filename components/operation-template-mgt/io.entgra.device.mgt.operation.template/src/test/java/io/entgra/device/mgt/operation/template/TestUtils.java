/*
 * Copyright (C) 2018 - 2023 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.operation.template;

import io.entgra.device.mgt.operation.template.util.ConnectionManagerUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestUtils {
    private static final Log log = LogFactory.getLog(TestUtils.class);
    public static Integer subtypeId = 3;
    public static String deviceType = "METER";
    public static String operationCode = "BILLING_REGISTERS_RETRIEVE";

    public static String getOperationDefinition(int subtypeId, String operationCode) {
        String operationDefinition = "{\"" + subtypeId + "\":2,\"deviceType\":\"METER\",\"maxAttempts\":1,"
                + "\"registerTransaction\":{},\"registers\":[\"0.0.0_0\",\"0.9.0_0\",\"1.8.0_0\","
                + "\"1.8.1_0\",\"1.8.2_0\",\"1.8.3_0\",\"2.8.0_0\",\"2.8.1_0\",\"2.8.2_0\",\"2.8.3_0\","
                + "\"1.8.0*01_0\",\"1.8.1*01_0\",\"1.8.2*01_0\",\"1.8.3*01_0\",\"2.8.0*01_0\","
                + "\"2.8.1*01_0\",\"2.8.2*01_0\",\"2.8.3*01_0\",\"9.6.0*01_0\",\"9.6.0_0\","
                + "\"10.6.0*01_0\",\"10.6.0_0\",\"0.4.2_0\",\"0.4.3_0\",\"0.4.5_0\",\"0.4.6_0\","
                + "\"14.7.0_0\",\"31.7.0_0\",\"32.7.0_0\",\"51.7.0_0\",\"52.7.0_0\",\"71.7.0_0\","
                + "\"72.7.0_0\"],\"attemptCount\":1,\"transportMode\":\"NET_ONLY\","
                + "\"hasPermanentError\":false,\"tryToReachViaSMS\":false,\"waitingTime\":0,"
                + "\"code\":\"" + operationCode + "\",\"properties\":{\"RequireDateAdjust\":\"30000\","
                + "\"RequireAuthentication\":\"32\",\"RequireSerialValidation\":\"1.0.0.0.0.255\"},"
                + "\"type\":\"PROFILE\",\"id\":0,\"control\":\"NO_REPEAT\",\"isEnabled\":true}";

        return operationDefinition;
    }

    public static void cleanupResources(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing result set", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing prepared statement", e);
            }
        }
        if (conn != null) {
            ConnectionManagerUtils.closeDBConnection();
        }
    }

}
