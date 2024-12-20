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

package io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.impl.operation;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Activity;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.ActivityHolder;
import io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.OperationMapping;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.impl.GenericOperationDAOImpl;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.util.OperationDAOUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class holds the implementation of OperationDAO which can be used to support SQLServer db syntax.
 */
public class SQLServerOperationDAOImpl extends GenericOperationDAOImpl {

    private static final Log log = LogFactory.getLog(SQLServerOperationDAOImpl.class);

    @Override
    public List<? extends Operation> getOperationsForDevice(int enrolmentId, PaginationRequest request)
            throws OperationManagementDAOException {
        Operation operation;
        List<Operation> operations = new ArrayList<Operation>();
        Long createdTo = null;
        Long createdFrom = null;
        boolean isCreatedDayProvided = false;
        boolean isUpdatedDayProvided = false;  //updated day = received day
        boolean isOperationCodeProvided = false;
        boolean isStatusProvided = false;
        if (request.getOperationLogFilters().getCreatedDayFrom() != null) {
            createdFrom = request.getOperationLogFilters().getCreatedDayFrom();
        }
        if (request.getOperationLogFilters().getCreatedDayTo() != null) {
            createdTo = request.getOperationLogFilters().getCreatedDayTo();
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
        if (createdFrom != null && createdFrom != 0 && createdTo != null && createdTo != 0) {
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
            // sql = sql + " AND o.OPERATION_CODE = ? ";
            int size = operationCode.size();
            sql = sql + " AND (o.OPERATION_CODE = ? ";
            for (int i = 0; i < size - 1; i++) {
                sql = sql + " OR o.OPERATION_CODE = ?";
            }
            sql = sql + ")";
            isOperationCodeProvided = true;
        } else if ((!isCreatedDayProvided && !isStatusProvided) && (operationCode != null && !operationCode.isEmpty())) {
            //sql = sql + " WHERE o.OPERATION_CODE = ? ";
            int size = operationCode.size();
            sql = sql + " WHERE (o.OPERATION_CODE = ? ";
            for (int i = 0; i < size - 1; i++) {
                sql = sql + " OR o.OPERATION_CODE = ?";
            }
            sql = sql + ")";
            isOperationCodeProvided = true;
        }
        sql = sql + " ORDER BY o.CREATED_TIMESTAMP DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
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
                    stmt.setLong(paramIndex++, createdFrom);
                    stmt.setLong(paramIndex++, createdTo);
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
                        operation.setCreatedTimeStamp(new Timestamp(rs.getLong("CREATED_TIMESTAMP") * 1000L).toInstant().toString());
                        if (rs.getLong("UPDATED_TIMESTAMP") == 0) {
                            operation.setReceivedTimeStamp("");
                        } else {
                            operation.setReceivedTimeStamp(
                                    new java.sql.Timestamp((rs.getLong("UPDATED_TIMESTAMP") * 1000)).toInstant().toString());
                        }
                        operation.setCode(rs.getString("OPERATION_CODE"));
                        operation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                        OperationDAOUtil.setActivityId(operation, rs.getInt("ID"));
                        operations.add(operation);
                    }
                }
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operations " +
                    "available for the device '" + enrolmentId + "'", e);
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
            String sql = "SELECT o.ID, TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.OPERATION_CODE, " +
                         "om.ID AS OM_MAPPING_ID, om.UPDATED_TIMESTAMP FROM DM_OPERATION o " +
                         "INNER JOIN (SELECT dm.OPERATION_ID, dm.ID, dm.STATUS, dm.UPDATED_TIMESTAMP FROM DM_ENROLMENT_OP_MAPPING dm " +
                         "WHERE dm.ENROLMENT_ID = ? AND dm.STATUS = ?) om ON o.ID = om.OPERATION_ID ORDER BY " +
                         "o.CREATED_TIMESTAMP DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            stmt.setString(2, status.toString());
            stmt.setInt(3, request.getStartIndex());
            stmt.setInt(4, request.getRowCount());
            rs = stmt.executeQuery();

            while (rs.next()) {
                operation = new Operation();
                operation.setId(rs.getInt("ID"));
                operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                if (rs.getLong("UPDATED_TIMESTAMP") == 0) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(
                            new java.sql.Timestamp((rs.getLong("UPDATED_TIMESTAMP") * 1000)).toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
                operation.setStatus(status);
                OperationDAOUtil.setActivityId(operation, rs.getInt("ID"));
                operations.add(operation);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operations " +
                                                      "available for the device'" + enrolmentId + "' with status '" + status.toString(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operations;
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfter(long timestamp, int limit, int offset)
            throws OperationManagementDAOException {
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String sql = "SELECT " +
                         "    eom.ENROLMENT_ID," +
                         "    eom.CREATED_TIMESTAMP," +
                         "    eom.UPDATED_TIMESTAMP," +
                         "    eom.OPERATION_ID," +
                         "    eom.OPERATION_CODE," +
                         "    eom.INITIATED_BY," +
                         "    eom.TYPE," +
                         "    eom.STATUS," +
                         "    eom.DEVICE_ID," +
                         "    eom.DEVICE_IDENTIFICATION," +
                         "    eom.DEVICE_TYPE," +
                         "    ops.ID AS OP_RES_ID," +
                         "    ops.RECEIVED_TIMESTAMP," +
                         "    ops.OPERATION_RESPONSE," +
                         "    ops.IS_LARGE_RESPONSE " +
                         "FROM " +
                         "    DM_ENROLMENT_OP_MAPPING AS eom " +
                         "INNER JOIN " +
                         "  (SELECT DISTINCT OPERATION_ID FROM DM_ENROLMENT_OP_MAPPING ORDER BY OPERATION_ID ASC limit ? , ? ) AS eom_ordered " +
                         "         ON eom_ordered.OPERATION_ID = eom.OPERATION_ID " +
                         "LEFT JOIN " +
                         "    DM_DEVICE_OPERATION_RESPONSE AS ops ON ops.EN_OP_MAP_ID = eom.ID " +
                         "WHERE " +
                         "    eom.UPDATED_TIMESTAMP > ? " +
                         "        AND eom.TENANT_ID = ? " +
                         "ORDER BY eom.OPERATION_ID, eom.UPDATED_TIMESTAMP";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, offset);
                stmt.setInt(2, limit);
                stmt.setLong(3, timestamp);
                stmt.setInt(4, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    ActivityHolder activityHolder = OperationDAOUtil.getActivityHolder(rs);
                    List<Integer> largeResponseIDs = activityHolder.getLargeResponseIDs();
                    List<Activity> activities = activityHolder.getActivityList();
                    if (!largeResponseIDs.isEmpty()) {
                        populateLargeOperationResponses(activities, largeResponseIDs);
                    }
                    return activities;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while getting the operation details from the database.";
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        }
    }

    @Override
    public Map<Integer, List<OperationMapping>> getOperationMappingsByStatus(Operation.Status opStatus, Operation.PushNotificationStatus pushNotificationStatus,
                                                                             int limit) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        OperationMapping operationMapping;
        Map<Integer, List<OperationMapping>> operationMappingsTenantMap = new HashMap<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT op.ENROLMENT_ID, op.OPERATION_ID, d.DEVICE_IDENTIFICATION, dt.NAME as DEVICE_TYPE, d" +
                    ".TENANT_ID FROM DM_DEVICE d, DM_ENROLMENT_OP_MAPPING op, DM_DEVICE_TYPE dt  WHERE op.STATUS = ? " +
                    "AND op.PUSH_NOTIFICATION_STATUS = ? AND d.DEVICE_TYPE_ID = dt.ID " +
                    "AND d.ID=op.ENROLMENT_ID ORDER BY op.OPERATION_ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, opStatus.toString());
            stmt.setString(2, pushNotificationStatus.toString());
            stmt.setInt(3, 0);
            stmt.setInt(4, limit);
            rs = stmt.executeQuery();
            while (rs.next()) {
                int tenantID = rs.getInt("TENANT_ID");
                List<OperationMapping> operationMappings = operationMappingsTenantMap.get(tenantID);
                if (operationMappings == null) {
                    operationMappings = new LinkedList<>();
                    operationMappingsTenantMap.put(tenantID, operationMappings);
                }
                operationMapping = new OperationMapping();
                operationMapping.setOperationId(rs.getInt("OPERATION_ID"));
                DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                deviceIdentifier.setType(rs.getString("DEVICE_TYPE"));
                operationMapping.setDeviceIdentifier(deviceIdentifier);
                operationMapping.setEnrollmentId(rs.getInt("ENROLMENT_ID"));
                operationMapping.setTenantId(tenantID);
                operationMappings.add(operationMapping);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error while getting operation mappings from database. ", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operationMappingsTenantMap;
    }

    public Operation getNextOperation(int enrolmentId, Operation.Status status)
            throws OperationManagementDAOException {
        String sql =
                "SELECT " +
                    "o.ID, " +
                    "TYPE, " +
                    "o.CREATED_TIMESTAMP, " +
                    "o.RECEIVED_TIMESTAMP, " +
                    "OPERATION_CODE, " +
                    "om.ID AS OM_MAPPING_ID, " +
                    "om.UPDATED_TIMESTAMP " +
                "FROM " +
                    "DM_OPERATION o " +
                    "INNER JOIN (" +
                        "SELECT * " +
                        "FROM " +
                            "DM_ENROLMENT_OP_MAPPING dm " +
                        "WHERE " +
                            "dm.ENROLMENT_ID = ? AND " +
                            "dm.STATUS = ?) om " +
                    "ON o.ID = om.OPERATION_ID " +
                "ORDER BY " +
                    "om.UPDATED_TIMESTAMP ASC, " +
                    "om.ID ASC " +
                "OFFSET 0 " +
                "ROWS FETCH NEXT 1 ROWS ONLY";

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, enrolmentId);
                stmt.setString(2, status.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    Operation operation = null;
                    if (rs.next()) {
                        operation = new Operation();
                        operation.setType(OperationDAOUtil.getType(rs.getString("TYPE")));
                        operation.setId(rs.getInt("ID"));
                        operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                        if (rs.getLong("UPDATED_TIMESTAMP") == 0) {
                            operation.setReceivedTimeStamp("");
                        } else {
                            operation.setReceivedTimeStamp(new java.sql.Timestamp((
                                    rs.getLong("UPDATED_TIMESTAMP") * 1000)).toString());
                        }
                        operation.setCode(rs.getString("OPERATION_CODE"));
                        operation.setStatus(Operation.Status.PENDING);
                        OperationDAOUtil.setActivityId(operation, rs.getInt("ID"));
                    }
                    return operation;
                }
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while retrieving next operation for enrollment ID: " + enrolmentId +
                    " having the status " + status.toString() + ".";
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        }
    }
}
