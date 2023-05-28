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

package io.entgra.device.mgt.core.operation.template;

import io.entgra.device.mgt.core.operation.template.util.ConnectionManagerUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestUtils {
    private static final Log log = LogFactory.getLog(TestUtils.class);
    public static String subtypeId = "3";
    public static String deviceType = "METER";
    public static String operationCode = "BILLING_REGISTERS_RETRIEVE";

    public static String getOperationDefinition(String subtypeId, String operationCode) {
        String operationDefinition = "{ \"subTypeId\": "+subtypeId+",    \"deviceType\": \"METER\",         " +
                "  \"maxAttempts\": 1,        \"registerTransaction\": {               " +
                " \"globalRegName\": \"CSRQ_RL\",                \"register\": \"0.0.96.3.10.255\", " +
                "               \"attributeIndex\": 2,                \"classId\": 70,             " +
                "   \"status\": \"PENDING\",                \"receivedTimeStamp\": 0,             " +
                "   \"isWriteOut\": false,                \"isProfileRegister\": false,             " +
                "   \"remoteMethod\": {                    \"index\": 2,                    \"data\": 0.0,  " +
                "                  \"type\": \"INT8\"                \t\t},\t\t\"registers\": [\"0.0.0_0\"," +
                " \"0.9.0_0\", \"1.8.0_0\", \"1.8.1_0\", \"1.8.2_0\",                            \"1.8.3_0\"," +
                " \"2.8.0_0\", \"2.8.1_0\", \"2.8.2_0\", \"2.8.3_0\", \"1.8.0*01_0\", \"1.8.1*01_0\"," +
                "                            \"1.8.2*01_0\", \"1.8.3*01_0\", \"2.8.0*01_0\", \"2.8.1*01_0\", " +
                "\"2.8.2*01_0\", \"2.8.3*01_0\",                            \"9.6.0*01_0\", \"9.6.0_0\", " +
                "\"10.6.0*01_0\", \"10.6.0_0\",                            \"0.4.2_0\", \"0.4.3_0\", \"0.4.5_0\"," +
                " \"0.4.6_0\",                            \"14.7.0_0\", \"31.7.0_0\", \"32.7.0_0\",     " +
                "                       \"51.7.0_0\", \"52.7.0_0\", \"71.7.0_0\", \"72.7.0_0\"],       " +
                " \"attemptCount\": 1,        \"transportMode\": \"NET_ONLY\",       " +
                " \"hasPermanentError\": false,        \"tryToReachViaSMS\": false,      " +
                "  \"waitingTime\": 0,    \"code\": \"BILLING_REGISTERS_RETRIEVE\",   " +
                " \"properties\": {        \"RequireDateAdjust\": \"30000\",       " +
                " \"RequireAuthentication\": \"32\",        \"RequireSerialValidation\": \"1.0.0.0.0.255\"    }," +
                "    \"type\": \"PROFILE\",    \"id\": 0,    \"control\": \"NO_REPEAT\",    " +
                "\"isEnabled\": true}";

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
