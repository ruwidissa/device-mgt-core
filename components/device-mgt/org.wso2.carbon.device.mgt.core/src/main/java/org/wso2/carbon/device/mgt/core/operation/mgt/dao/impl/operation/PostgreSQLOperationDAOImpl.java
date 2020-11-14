/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.operation.mgt.dao.impl.operation;

import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.OperationResponseMeta;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.impl.GenericOperationDAOImpl;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.util.OperationDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * This class holds the implementation of OperationDAO which can be used to support PostgreSQL db syntax.
 */
public class PostgreSQLOperationDAOImpl extends GenericOperationDAOImpl {

    @Override
    public List<? extends Operation> getOperationsForDevice(int enrolmentId, PaginationRequest request)
            throws OperationManagementDAOException {
        Operation operation;
        List<Operation> operations = new ArrayList<Operation>();
        String createdTo = null;
        String createdFrom = null;
        DateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        boolean isCreatedDayProvided = false;
        boolean isUpdatedDayProvided = false;  //updated day = received day
        boolean isOperationCodeProvided = false;
        boolean isStatusProvided = false;
        if (request.getOperationLogFilters().getCreatedDayFrom() != null) {
            createdFrom = simple.format(request.getOperationLogFilters().getCreatedDayFrom());
        }
        if (request.getOperationLogFilters().getCreatedDayTo() != null) {
            createdTo = simple.format(request.getOperationLogFilters().getCreatedDayTo());
        }
        Long updatedFrom = request.getOperationLogFilters().getUpdatedDayFrom();
        Long updatedTo = request.getOperationLogFilters().getUpdatedDayTo();
        List<String> operationCode = request.getOperationLogFilters().getOperationCode();
        List<String> status = request.getOperationLogFilters().getStatus();
        String sql = "SELECT " +
                        "o.ID, " +
                        "TYPE, " +
                        "o.CREATED_TIMESTAMP, " +
                        "o.RECEIVED_TIMESTAMP, " +
                        "o.OPERATION_CODE, " +
                        "om.STATUS, " +
                        "om.ID AS OM_MAPPING_ID, " +
                        "om.UPDATED_TIMESTAMP " +
                    "FROM " +
                        "DM_OPERATION o " +
                    "INNER JOIN " +
                        "(SELECT " +
                            "dm.OPERATION_ID, " +
                            "dm.ID, " +
                            "dm.STATUS, " +
                            "dm.UPDATED_TIMESTAMP " +
                        "FROM " +
                            "DM_ENROLMENT_OP_MAPPING dm " +
                        "WHERE " +
                            "dm.ENROLMENT_ID = ?";

        if (updatedFrom != null && updatedFrom != 0 && updatedTo != null && updatedTo != 0) {
            sql = sql + " AND dm.UPDATED_TIMESTAMP BETWEEN ? AND ?";
            isUpdatedDayProvided = true;
        }
        sql = sql + ") om ON o.ID = om.OPERATION_ID ";
        if (createdFrom != null && !createdFrom.isEmpty() && createdTo != null && !createdTo.isEmpty()) {
            sql = sql + " WHERE o.CREATED_TIMESTAMP BETWEEN ? AND ?";
            isCreatedDayProvided = true;
        }
        if ((isCreatedDayProvided) && (status != null && !status.isEmpty())) {
            int size = status.size();
            sql = sql + " AND (om.STATUS = ? ";
            for (int i = 0; i < size - 1; i++) {
                sql = sql + " OR om.STATUS = ?";
            }
            sql = sql + ")";
            isStatusProvided = true;
        } else if ((!isCreatedDayProvided) && (status != null && !status.isEmpty())) {
            int size = status.size();
            sql = sql + " WHERE (om.STATUS = ? ";
            for (int i = 0; i < size - 1; i++) {
                sql = sql + " OR om.STATUS = ?";
            }
            sql = sql + ")";
            isStatusProvided = true;
        }
        if ((isCreatedDayProvided || isStatusProvided) && (operationCode != null && !operationCode.isEmpty())) {
            int size = operationCode.size();
            sql = sql + " AND (o.OPERATION_CODE = ? ";
            for (int i = 0; i < size - 1; i++) {
                sql = sql + " OR o.OPERATION_CODE = ?";
            }
            sql = sql + ")";
            isOperationCodeProvided = true;
        } else if ((!isCreatedDayProvided && !isStatusProvided) && (operationCode != null && !operationCode.isEmpty())) {
            int size = operationCode.size();
            sql = sql + " WHERE (o.OPERATION_CODE = ? ";
            for (int i = 0; i < size - 1; i++) {
                sql = sql + " OR o.OPERATION_CODE = ?";
            }
            sql = sql + ")";
            isOperationCodeProvided = true;
        }
        sql = sql + " ORDER BY o.CREATED_TIMESTAMP DESC LIMIT ? OFFSET ?";
        int paramIndex = 1;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(paramIndex++, enrolmentId);
                if (isUpdatedDayProvided) {
                    stmt.setLong(paramIndex++, updatedFrom);
                    stmt.setLong(paramIndex++, updatedTo);
                }
                if (isCreatedDayProvided) {
                    stmt.setString(paramIndex++, createdFrom);
                    stmt.setString(paramIndex++, createdTo);
                }
                if (isStatusProvided) {
                    int size = status.size();
                    for (int i = 0; i < size; i++) {
                        stmt.setString(paramIndex++, status.get(i));
                    }
                }
                if (isOperationCodeProvided) {
                    int size = operationCode.size();
                    for (int i = 0; i < size; i++) {
                        stmt.setString(paramIndex++, operationCode.get(i));
                    }
                }
                stmt.setInt(paramIndex++, request.getStartIndex());
                stmt.setInt(paramIndex, request.getRowCount());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        operation = new Operation();
                        operation.setId(rs.getInt("ID"));
                        operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                        operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                        if (rs.getTimestamp("RECEIVED_TIMESTAMP") == null) {
                            operation.setReceivedTimeStamp("");
                        } else {
                            operation.setReceivedTimeStamp(rs.getTimestamp("RECEIVED_TIMESTAMP").toString());
                        }
                        operation.setCode(rs.getString("OPERATION_CODE"));
                        operation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                        OperationDAOUtil.setActivityId(operation, rs.getInt("ID"));
                        operations.add(operation);
                    }
                }
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operation " +
                    "available for the device'" + enrolmentId, e);
        }
        return operations;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(int enrolmentId, PaginationRequest request,
                                                                    Operation.Status status)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;
        List<Operation> operations = new ArrayList<Operation>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, o.TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.OPERATION_CODE " +
                         "FROM DM_OPERATION o " +
                         "INNER JOIN (SELECT * FROM DM_ENROLMENT_OP_MAPPING dm " +
                         "WHERE dm.ENROLMENT_ID = ? AND dm.STATUS = ?) om ON o.ID = om.OPERATION_ID ORDER BY " +
                         "o.CREATED_TIMESTAMP DESC LIMIT ? OFFSET ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            stmt.setString(2, status.toString());
            stmt.setInt(3, request.getRowCount());
            stmt.setInt(4, request.getStartIndex());
            rs = stmt.executeQuery();

            while (rs.next()) {
                operation = new Operation();
                operation.setId(rs.getInt("ID"));
                operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                if (rs.getTimestamp("RECEIVED_TIMESTAMP") == null) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(rs.getTimestamp("RECEIVED_TIMESTAMP").toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
                operation.setStatus(status);
                OperationDAOUtil.setActivityId(operation, rs.getInt("ID"));
                operations.add(operation);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operation " +
                                                      "available for the device'" + enrolmentId + "' with status '" + status.toString(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operations;
    }

    @Override
    public OperationResponseMeta addOperationResponse(int enrolmentId, org.wso2.carbon.device.mgt.common.operation.mgt.Operation operation,
            String deviceId) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isLargeResponse = false;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();

            stmt = connection.prepareStatement("SELECT ID FROM DM_ENROLMENT_OP_MAPPING WHERE ENROLMENT_ID = ? " +
                    "AND OPERATION_ID = ?");
            stmt.setInt(1, enrolmentId);
            stmt.setInt(2, operation.getId());

            rs = stmt.executeQuery();
            int enPrimaryId = 0;
            if (rs.next()) {
                enPrimaryId = rs.getInt("ID");
            }
            stmt = connection.prepareStatement("INSERT INTO DM_DEVICE_OPERATION_RESPONSE(OPERATION_ID, ENROLMENT_ID, " +
                            "EN_OP_MAP_ID, OPERATION_RESPONSE, IS_LARGE_RESPONSE, RECEIVED_TIMESTAMP) VALUES(?, ?, ?, ?, ?, ?)",
                    new String[]{"id"});
            stmt.setInt(1, operation.getId());
            stmt.setInt(2, enrolmentId);
            stmt.setInt(3, enPrimaryId);

            if (operation.getOperationResponse() != null && operation.getOperationResponse().length() >= 1000) {
                isLargeResponse = true;
                stmt.setBytes(4, null);
            } else {
                stmt.setString(4, operation.getOperationResponse());
            }
            stmt.setBoolean(5, isLargeResponse);

            Timestamp receivedTimestamp = new Timestamp(new Date().getTime());
            stmt.setTimestamp(6, receivedTimestamp);
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            int opResID = -1;
            if (rs.next()) {
                opResID = rs.getInt(1);
            }

            OperationResponseMeta responseMeta = new OperationResponseMeta();
            responseMeta.setId(opResID);
            responseMeta.setEnrolmentId(enrolmentId);
            responseMeta.setOperationMappingId(enPrimaryId);
            responseMeta.setReceivedTimestamp(receivedTimestamp);
            responseMeta.setLargeResponse(isLargeResponse);
            return responseMeta;
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while inserting operation response. " +
                    e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }
}
