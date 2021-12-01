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
package org.wso2.carbon.device.mgt.core.operation.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.ActivityPaginationRequest;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.ActivityHolder;
import org.wso2.carbon.device.mgt.common.operation.mgt.ActivityStatus;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationResponse;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.OperationResponseMeta;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMapping;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.util.OperationDAOUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class holds the generic implementation of OperationDAO which can be used to support ANSI db syntax.
 */
public class GenericOperationDAOImpl implements OperationDAO {

    private static final Log log = LogFactory.getLog(GenericOperationDAOImpl.class);

    public int addOperation(Operation operation) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_OPERATION(TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, OPERATION_CODE, " +
                    "INITIATED_BY, OPERATION_DETAILS) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, operation.getType().toString());
            stmt.setTimestamp(2, new Timestamp(new Date().getTime()));
            stmt.setTimestamp(3, null);
            stmt.setString(4, operation.getCode());
            stmt.setString(5, operation.getInitiatedBy());
            stmt.setObject(6, operation);
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            int id = -1;
            if (rs.next()) {
                id = rs.getInt(1);
            }
            return id;
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while adding operation metadata. " +
                    e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    public boolean updateOperationStatus(int enrolmentId, int operationId, Operation.Status status)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        boolean isUpdated = false;
        try {
            long time = System.currentTimeMillis() / 1000;
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement("UPDATE DM_ENROLMENT_OP_MAPPING SET STATUS=?, UPDATED_TIMESTAMP=? " +
                    "WHERE ENROLMENT_ID=? and OPERATION_ID=?");
            stmt.setString(1, status.toString());
            stmt.setLong(2, time);
            stmt.setInt(3, enrolmentId);
            stmt.setInt(4, operationId);
            int numOfRecordsUpdated = stmt.executeUpdate();
            if (numOfRecordsUpdated != 0) {
                isUpdated = true;
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while update device mapping operation status " +
                    "metadata. " + e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
        return isUpdated;
    }

    @Override
    public void updateEnrollmentOperationsStatus(int enrolmentId, String operationCode, Operation.Status existingStatus,
                                                 Operation.Status newStatus) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            String query = "SELECT EOM.ID FROM DM_ENROLMENT_OP_MAPPING EOM INNER JOIN DM_OPERATION DM "
                    + "ON DM.ID = EOM.OPERATION_ID  WHERE EOM.ENROLMENT_ID = ? AND DM.OPERATION_CODE = ? "
                    + "AND EOM.STATUS = ?";
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, enrolmentId);
            stmt.setString(2, operationCode);
            stmt.setString(3, existingStatus.toString());
            // This will return only one result always.
            rs = stmt.executeQuery();
            int id = 0;
            while (rs.next()) {
                id = rs.getInt("ID");
            }
            if (id != 0) {
                stmt = connection.prepareStatement(
                        "UPDATE DM_ENROLMENT_OP_MAPPING SET STATUS = ?, " + "UPDATED_TIMESTAMP = ?  WHERE ID = ?");
                stmt.setString(1, newStatus.toString());
                stmt.setLong(2, System.currentTimeMillis() / 1000);
                stmt.setInt(3, id);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new OperationManagementDAOException(
                    "Error occurred while update device mapping operation status " + "metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
    }

    @Override
    public Map<Integer, Integer> getExistingOperationIDs(Integer[] enrolmentIds, String operationCode)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<Integer, Integer> existingOperationIds = new HashMap<>();
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            StringBuilder query = new StringBuilder("SELECT OPERATION_ID, ENROLMENT_ID FROM DM_ENROLMENT_OP_MAPPING " +
                    "WHERE OPERATION_CODE = ? AND STATUS IN ('NOTNOW', 'PENDING') AND ENROLMENT_ID IN (");
            for (int i = 0; i < enrolmentIds.length; i++) {
                query.append(" ?,");
            }
            query.deleteCharAt(query.length() - 1);
            query.append(")");
            stmt = connection.prepareStatement(query.toString());
            stmt.setString(1, operationCode);

            for (int i = 0; i < enrolmentIds.length; i++) {
                stmt.setInt(i + 2, enrolmentIds[i]);
            }

            rs = stmt.executeQuery();
            int operationId;
            int enrollmentId;
            while (rs.next()) {
                enrollmentId = rs.getInt("ENROLMENT_ID");
                operationId = rs.getInt("OPERATION_ID");
                existingOperationIds.put(enrollmentId, operationId);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while update device mapping operation status " +
                    "metadata. " + e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return existingOperationIds;
    }

    @Override
    public OperationResponseMeta addOperationResponse(int enrolmentId,
                                                      org.wso2.carbon.device.mgt.common.operation.mgt.Operation operation,
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
                    new String[]{"ID"});
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

    @Override
    public void addOperationResponseLarge(OperationResponseMeta responseMeta,
                                          org.wso2.carbon.device.mgt.common.operation.mgt.Operation operation,
                                          String deviceId) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ByteArrayOutputStream bao = null;
        ObjectOutputStream oos = null;
        ResultSet rs = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement("INSERT INTO DM_DEVICE_OPERATION_RESPONSE_LARGE " +
                    "(ID, OPERATION_RESPONSE, OPERATION_ID, EN_OP_MAP_ID, RECEIVED_TIMESTAMP, DEVICE_IDENTIFICATION) " +
                    "VALUES(?, ?, ?, ?, ?, ?)");
            bao = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bao);
            oos.writeObject(operation.getOperationResponse());
            stmt.setInt(1, responseMeta.getId());
            stmt.setBytes(2, bao.toByteArray());
            stmt.setInt(3, operation.getId());
            stmt.setInt(4, responseMeta.getOperationMappingId());
            stmt.setTimestamp(5, responseMeta.getReceivedTimestamp());
            stmt.setString(6, deviceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while inserting operation response. " +
                    e.getMessage(), e);
        } catch (IOException e) {
            throw new OperationManagementDAOException("Error occurred while serializing operation response object. " +
                    e.getMessage(), e);
        } finally {
            if (bao != null) {
                try {
                    bao.close();
                } catch (IOException e) {
                    log.warn("Error occurred while closing ByteArrayOutputStream", e);
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    log.warn("Error occurred while closing ObjectOutputStream", e);
                }
            }
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public Map<String, Map<String, List<OperationResponse>>> getLargeOperationResponsesInBulk(List<Integer> operationResponseIds)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<String, Map<String, List<OperationResponse>>> operationResponseMapping = new HashMap<>();
        Map<String, List<OperationResponse>> operationDeviceMappings;
        List<OperationResponse> responseList;

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();

            String sql1 = "SELECT * FROM DM_DEVICE_OPERATION_RESPONSE_LARGE WHERE ID IN (";

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < operationResponseIds.size(); i++) {
                builder.append("?,");
            }
            sql1 += builder.deleteCharAt(builder.length() - 1) + ")";
            stmt = conn.prepareStatement(sql1);
            int i;
            for (i = 0; i < operationResponseIds.size(); i++) {
                stmt.setInt(i + 1, operationResponseIds.get(i));
            }
            rs = stmt.executeQuery();

            while (rs.next()) {
                String activityID = OperationDAOUtil.getActivityId(rs.getInt("OPERATION_ID"));
                String deviceID = rs.getString("DEVICE_IDENTIFICATION");

                if (operationResponseMapping.containsKey(activityID)) {
                    operationDeviceMappings = operationResponseMapping.get(activityID);
                    if (operationDeviceMappings.containsKey(deviceID)) {
                        responseList = operationDeviceMappings.get(deviceID);
                    } else {
                        responseList = new ArrayList<>();
                        operationDeviceMappings.put(deviceID, responseList);
                    }

                } else {
                    responseList = new ArrayList<>();
                    operationDeviceMappings = new HashMap<>();
                    operationDeviceMappings.put(deviceID, responseList);
                    operationResponseMapping.put(activityID, operationDeviceMappings);
                }
                responseList.add(OperationDAOUtil.getLargeOperationResponse(rs));
            }

        } catch (SQLException e) {
            throw new OperationManagementDAOException(
                    "Error occurred while getting the operation details from the database. " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new OperationManagementDAOException(
                    "Error occurred while converting the operation response to string.. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new OperationManagementDAOException(
                    "IO exception occurred while converting the operations responses. " + e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operationResponseMapping;
    }

    @Override
    public void populateLargeOperationResponses(List<Activity> activities,
                                                List<Integer> largeResponseIDs)
            throws OperationManagementDAOException {
        if (!largeResponseIDs.isEmpty()) {
            Map<String, Map<String, List<OperationResponse>>> largeOperationResponses = getLargeOperationResponsesInBulk(largeResponseIDs);
            if (!largeOperationResponses.isEmpty()) {
                for (Activity tempActivity : activities) {
                    if (largeOperationResponses.containsKey(tempActivity.getActivityId())) {
                        List<ActivityStatus> activityStatuses = tempActivity.getActivityStatus();
                        Map<String, List<OperationResponse>> deviceOpResponseMap = largeOperationResponses.get(tempActivity.getActivityId());
                        for (Map.Entry<String, List<OperationResponse>> deviceOpRes : deviceOpResponseMap.entrySet()) {
                            for (ActivityStatus status : activityStatuses) {
                                if (deviceOpRes.getKey().equalsIgnoreCase(status.getDeviceIdentifier().getId())) {
                                    if (status.getResponses() == null) {
                                        status.setResponses(new ArrayList<>());
                                    }
                                    status.getResponses().addAll(deviceOpRes.getValue());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Activity getActivity(int operationId) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Activity activity = null;
        List<ActivityStatus> activityStatusList = new ArrayList<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT " +
                    "    eom.ENROLMENT_ID," +
                    "    eom.CREATED_TIMESTAMP," +
                    "    eom.UPDATED_TIMESTAMP," +
                    "    eom.OPERATION_ID," +
                    "    eom.OPERATION_CODE," +
                    "    eom.INITIATED_BY," +
                    "    eom.TYPE AS OPERATION_TYPE," +
                    "    eom.STATUS," +
                    "    eom.DEVICE_ID," +
                    "    eom.DEVICE_IDENTIFICATION," +
                    "    eom.DEVICE_TYPE AS DEVICE_TYPE_NAME," +
                    "    eom.ID AS EOM_MAPPING_ID," +
                    "    opr.ID AS OP_RES_ID," +
                    "    opr.RECEIVED_TIMESTAMP," +
                    "    opr.OPERATION_RESPONSE," +
                    "    opr.IS_LARGE_RESPONSE " +
                    "FROM " +
                    "    DM_ENROLMENT_OP_MAPPING eom " +
                    "        LEFT JOIN " +
                    "    DM_DEVICE_OPERATION_RESPONSE opr ON opr.EN_OP_MAP_ID = eom.ID " +
                    "WHERE " +
                    "    eom.OPERATION_ID = ? AND eom.TENANT_ID = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, operationId);
            stmt.setInt(2, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            rs = stmt.executeQuery();

            int enrolmentId = 0;
            ActivityStatus activityStatus = null;
            int responseId = 0;
            List<Integer> largeResponseIDs = new ArrayList<>();
            while (rs.next()) {
                if (enrolmentId == 0) {
                    activity = new Activity();
                    activity.setActivityId(DeviceManagementConstants.OperationAttributes.ACTIVITY + operationId);
                    activity.setType(Activity.Type.valueOf(rs.getString("OPERATION_TYPE")));
                    activity.setCreatedTimeStamp(new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));
                    activity.setInitiatedBy(rs.getString("INITIATED_BY"));
                }
                if (enrolmentId != rs.getInt("ENROLMENT_ID")) {
                    activityStatus = new ActivityStatus();

                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                    deviceIdentifier.setType(rs.getString("DEVICE_TYPE_NAME"));
                    activityStatus.setDeviceIdentifier(deviceIdentifier);

                    activityStatus.setStatus(ActivityStatus.Status.valueOf(rs.getString("STATUS")));

                    List<OperationResponse> operationResponses = new ArrayList<>();
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.setUpdatedTimestamp(new java.util.Date(rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());
                        operationResponses.add(OperationDAOUtil.getOperationResponse(rs));
                    }
                    activityStatus.setResponses(operationResponses);

                    activityStatusList.add(activityStatus);

                    enrolmentId = rs.getInt("ENROLMENT_ID");
                    activity.setActivityStatus(activityStatusList);
                } else {
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        responseId = rs.getInt("OP_RES_ID");
                        if (rs.getBoolean("IS_LARGE_RESPONSE")) {
                            largeResponseIDs.add(responseId);
                        } else {
                            if (activityStatus == null) {
                                activityStatus = new ActivityStatus();
                            }
                            if (activityStatus.getResponses() == null) {
                                activityStatus.setResponses(new ArrayList<>());
                            }
                            activityStatus.getResponses().add(OperationDAOUtil.getOperationResponse(rs));
                        }
                    }
                }
            }
            if (!largeResponseIDs.isEmpty()) {
                Map<String, Map<String, List<OperationResponse>>> largeOperationResponses = getLargeOperationResponsesInBulk(largeResponseIDs);
                if (!largeOperationResponses.isEmpty()) {
                    List<ActivityStatus> activityStatuses = activity.getActivityStatus();
                    if (activityStatuses != null) {
                        Map<String, List<OperationResponse>> deviceOpResponseMap = largeOperationResponses.get(activity.getActivityId());
                        for (Map.Entry<String, List<OperationResponse>> deviceOpRes : deviceOpResponseMap.entrySet()) {
                            for (ActivityStatus status : activityStatuses) {
                                if (deviceOpRes.getKey().equalsIgnoreCase(status.getDeviceIdentifier().getId())) {
                                    status.getResponses().addAll(deviceOpRes.getValue());
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while getting the operation details from " +
                    "the database. " + e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return activity;
    }


    @Override
    public List<Activity> getActivityList(List<Integer> activityIds) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Activity activity;
        List<Activity> activities = new ArrayList<>();
        Object[] data = activityIds.toArray();

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql =
                    "SELECT eom.ENROLMENT_ID, eom.OPERATION_ID, eom.ID AS EOM_MAPPING_ID, "
                            + "dor.ID AS OP_RES_ID, de.DEVICE_ID, d.DEVICE_IDENTIFICATION, d.DEVICE_TYPE_ID, "
                            + "dt.NAME AS DEVICE_TYPE_NAME, eom.STATUS, eom.CREATED_TIMESTAMP, "
                            + "eom.UPDATED_TIMESTAMP, op.OPERATION_CODE, op.TYPE AS OPERATION_TYPE, "
                            + "dor.OPERATION_RESPONSE,  op.INITIATED_BY, dor.RECEIVED_TIMESTAMP, dor.IS_LARGE_RESPONSE FROM "
                            + "DM_ENROLMENT_OP_MAPPING eom INNER JOIN DM_OPERATION op "
                            + "ON op.ID=eom.OPERATION_ID INNER JOIN DM_ENROLMENT de "
                            + "ON de.ID=eom.ENROLMENT_ID INNER JOIN DM_DEVICE d ON d.ID=de.DEVICE_ID "
                            + "INNER JOIN DM_DEVICE_TYPE dt ON dt.ID=d.DEVICE_TYPE_ID "
                            + "LEFT JOIN DM_DEVICE_OPERATION_RESPONSE dor ON dor.ENROLMENT_ID=de.id "
                            + "AND dor.OPERATION_ID = eom.OPERATION_ID WHERE eom.OPERATION_ID "
                            + "IN (SELECT * FROM TABLE(x INT = ?)) AND de.TENANT_ID = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setObject(1, data);

            stmt.setInt(2, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            rs = stmt.executeQuery();

            int operationId = 0;
            int enrolmentId = 0;
            int responseId = 0;
            ActivityStatus activityStatus = new ActivityStatus();
            List<Integer> largeResponseIDs = new ArrayList<>();
            while (rs.next()) {
                activity = new Activity();

                if (operationId != rs.getInt("OPERATION_ID")) {
                    activities.add(activity);
                    List<ActivityStatus> statusList = new ArrayList<>();
                    activityStatus = new ActivityStatus();

                    operationId = rs.getInt("OPERATION_ID");
                    enrolmentId = rs.getInt("ENROLMENT_ID");

                    activity.setType(Activity.Type.valueOf(rs.getString("OPERATION_TYPE")));
                    activity.setCreatedTimeStamp(
                            new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));
                    activity.setInitiatedBy(rs.getString("INITIATED_BY"));

                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                    deviceIdentifier.setType(rs.getString("DEVICE_TYPE_NAME"));

                    activityStatus.setDeviceIdentifier(deviceIdentifier);

                    activityStatus.setStatus(ActivityStatus.Status.valueOf(rs.getString("STATUS")));

                    List<OperationResponse> operationResponses = new ArrayList<>();
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.setUpdatedTimestamp(
                                new java.util.Date(rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());

                    }
                    if (rs.getTimestamp("RECEIVED_TIMESTAMP") != null) {
                        if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                            responseId = rs.getInt("OP_RES_ID");
                            if (rs.getBoolean("IS_LARGE_RESPONSE")) {
                                largeResponseIDs.add(responseId);
                            } else {
                                operationResponses.add(OperationDAOUtil.getOperationResponse(rs));
                            }
                        }
                    }
                    activityStatus.setResponses(operationResponses);
                    statusList.add(activityStatus);
                    activity.setActivityStatus(statusList);
                    activity.setActivityId(OperationDAOUtil.getActivityId(rs.getInt("OPERATION_ID")));
                }

                if (operationId == rs.getInt("OPERATION_ID") && enrolmentId != rs.getInt("ENROLMENT_ID")) {
                    activityStatus = new ActivityStatus();

                    activity.setType(Activity.Type.valueOf(rs.getString("OPERATION_TYPE")));
                    activity.setCreatedTimeStamp(
                            new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));

                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                    deviceIdentifier.setType(rs.getString("DEVICE_TYPE_NAME"));
                    activityStatus.setDeviceIdentifier(deviceIdentifier);

                    activityStatus.setStatus(ActivityStatus.Status.valueOf(rs.getString("STATUS")));

                    List<OperationResponse> operationResponses = new ArrayList<>();
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.setUpdatedTimestamp(
                                new java.util.Date(rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());
                    }
                    if (rs.getTimestamp("RECEIVED_TIMESTAMP") != null) {
                        responseId = rs.getInt("OP_RES_ID");
                        if (rs.getBoolean("IS_LARGE_RESPONSE")) {
                            largeResponseIDs.add(responseId);
                        } else {
                            operationResponses.add(OperationDAOUtil.getOperationResponse(rs));
                        }
                    }
                    activityStatus.setResponses(operationResponses);
                    activity.getActivityStatus().add(activityStatus);

                    enrolmentId = rs.getInt("ENROLMENT_ID");
                }

                if (rs.getInt("OP_RES_ID") != 0 && responseId != rs.getInt("OP_RES_ID") && rs.getTimestamp(
                        "RECEIVED_TIMESTAMP") != null) {
                    responseId = rs.getInt("OP_RES_ID");
                    if (rs.getBoolean("IS_LARGE_RESPONSE")) {
                        largeResponseIDs.add(responseId);
                    } else {
                        activityStatus.getResponses().add(OperationDAOUtil.getOperationResponse(rs));
                    }
                }
            }
            if (!largeResponseIDs.isEmpty()) {
                populateLargeOperationResponses(activities, largeResponseIDs);
            }

        } catch (SQLException e) {
            throw new OperationManagementDAOException(
                    "Error occurred while getting the operation details from the database. " + e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return activities;
    }

    public Activity getActivityByDevice(int operationId, int deviceId) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Activity activity = null;
        List<ActivityStatus> activityStatusList = new ArrayList<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT "
                    + "eom.ENROLMENT_ID, "
                    + "eom.OPERATION_ID, eom.ID AS EOM_MAPPING_ID, "
                    + "dor.ID AS OP_RES_ID, "
                    + "de.DEVICE_ID, "
                    + "d.DEVICE_IDENTIFICATION, "
                    + "d.DEVICE_TYPE_ID, dt.NAME AS DEVICE_TYPE_NAME, "
                    + "eom.STATUS, eom.CREATED_TIMESTAMP, "
                    + "eom.UPDATED_TIMESTAMP, "
                    + "op.OPERATION_CODE, "
                    + "op.TYPE AS OPERATION_TYPE, "
                    + "dor.OPERATION_RESPONSE, "
                    + "dor.RECEIVED_TIMESTAMP, "
                    + "dor.IS_LARGE_RESPONSE, "
                    + "op.INITIATED_BY FROM DM_ENROLMENT_OP_MAPPING AS eom "
                    + "INNER JOIN DM_OPERATION AS op ON op.ID=eom.OPERATION_ID "
                    + "INNER JOIN DM_ENROLMENT AS de ON de.ID=eom.ENROLMENT_ID "
                    + "INNER JOIN DM_DEVICE AS d ON d.ID=de.DEVICE_ID "
                    + "INNER JOIN DM_DEVICE_TYPE AS dt ON dt.ID=d.DEVICE_TYPE_ID "
                    + "LEFT JOIN DM_DEVICE_OPERATION_RESPONSE AS dor ON dor.ENROLMENT_ID=de.id "
                    + "AND dor.OPERATION_ID = eom.OPERATION_ID "
                    + "WHERE eom.OPERATION_ID = ? AND de.device_id = ? AND de.TENANT_ID = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, operationId);
            stmt.setInt(2, deviceId);
            stmt.setInt(3, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            rs = stmt.executeQuery();

            int enrolmentId = 0;
            ActivityStatus activityStatus = null;
            List<Integer> largeResponseIDs = new ArrayList<>();

            while (rs.next()) {
                if (enrolmentId == 0) {
                    activity = new Activity();
                    activity.setActivityId(DeviceManagementConstants.OperationAttributes.ACTIVITY + operationId);
                    activity.setType(Activity.Type.valueOf(rs.getString("OPERATION_TYPE")));
                    activity.setCreatedTimeStamp(
                            new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));
                    activity.setInitiatedBy(rs.getString("INITIATED_BY"));
                }
                if (enrolmentId != rs.getInt("ENROLMENT_ID")) {
                    activityStatus = new ActivityStatus();
                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"),
                            rs.getString("DEVICE_TYPE_NAME"));
                    activityStatus.setDeviceIdentifier(deviceIdentifier);
                    activityStatus.setStatus(ActivityStatus.Status.valueOf(rs.getString("STATUS")));

                    List<OperationResponse> operationResponses = new ArrayList<>();
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.setUpdatedTimestamp(
                                new java.util.Date(rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());
                        if (rs.getBoolean("IS_LARGE_RESPONSE")) {
                            largeResponseIDs.add(rs.getInt("OP_RES_ID"));
                        } else {
                            operationResponses.add(OperationDAOUtil.getOperationResponse(rs));
                        }
                    }
                    activityStatus.setResponses(operationResponses);
                    activityStatusList.add(activityStatus);
                    enrolmentId = rs.getInt("ENROLMENT_ID");
                    activity.setActivityStatus(activityStatusList);
                } else {
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        if (rs.getBoolean("IS_LARGE_RESPONSE")) {
                            largeResponseIDs.add(rs.getInt("OP_RES_ID"));
                        } else {
                            if (activityStatus == null) {
                                activityStatus = new ActivityStatus();
                            }
                            activityStatus.getResponses().add(OperationDAOUtil.getOperationResponse(rs));
                        }
                    }
                }
            }

            if (!largeResponseIDs.isEmpty()) {
                Map<String, Map<String, List<OperationResponse>>> largeOperationResponses = getLargeOperationResponsesInBulk(largeResponseIDs);
                if (!largeOperationResponses.isEmpty()) {
                    List<ActivityStatus> activityStatuses = activity.getActivityStatus();
                    if (activityStatuses != null) {
                        Map<String, List<OperationResponse>> deviceOpResponseMap = largeOperationResponses.get(activity.getActivityId());
                        for (Map.Entry<String, List<OperationResponse>> deviceOpRes : deviceOpResponseMap.entrySet()) {
                            for (ActivityStatus status : activityStatuses) {
                                if (deviceOpRes.getKey().equalsIgnoreCase(status.getDeviceIdentifier().getId())) {
                                    status.getResponses().addAll(deviceOpRes.getValue());
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while getting the operation details from " +
                    "the database. " + e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return activity;
    }

    @Override
    public List<Activity> getFilteredActivities(String operationCode, int limit, int offset)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Activity> activities = new ArrayList<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String sql = "SELECT " +
                    "    opr.ENROLMENT_ID, " +
                    "    opr.CREATED_TIMESTAMP, " +
                    "    opr.UPDATED_TIMESTAMP, " +
                    "    opr.OPERATION_ID, " +
                    "    opr.OPERATION_CODE, " +
                    "    opr.OPERATION_TYPE, " +
                    "    opr.STATUS, " +
                    "    opr.DEVICE_ID, " +
                    "    opr.DEVICE_IDENTIFICATION, " +
                    "    opr.DEVICE_TYPE, " +
                    "    ops.RECEIVED_TIMESTAMP, " +
                    "    ops.ID AS OP_RES_ID, " +
                    "    ops.OPERATION_RESPONSE, " +
                    "    ops.IS_LARGE_RESPONSE, " +
                    "    opr.INITIATED_BY " +
                    " FROM " +
                    "    (SELECT " +
                    "            opm.ID MAPPING_ID, " +
                    "            opm.ENROLMENT_ID, " +
                    "            opm.CREATED_TIMESTAMP, " +
                    "            opm.UPDATED_TIMESTAMP, " +
                    "            opm.OPERATION_ID, " +
                    "            op.OPERATION_CODE, " +
                    "            op.INITIATED_BY, " +
                    "            op.TYPE AS OPERATION_TYPE, " +
                    "            opm.STATUS, " +
                    "            en.DEVICE_ID, " +
                    "            de.DEVICE_IDENTIFICATION, " +
                    "            dt.NAME AS DEVICE_TYPE, " +
                    "            de.TENANT_ID " +
                    "    FROM" +
                    "        DM_ENROLMENT_OP_MAPPING  opm " +
                    "        INNER JOIN DM_OPERATION  op ON opm.OPERATION_ID = op.ID " +
                    "        INNER JOIN DM_ENROLMENT  en ON opm.ENROLMENT_ID = en.ID " +
                    "        INNER JOIN DM_DEVICE  de ON en.DEVICE_ID = de.ID " +
                    "        INNER JOIN DM_DEVICE_TYPE  dt ON dt.ID = de.DEVICE_TYPE_ID " +
                    "    WHERE " +
                    "        op.OPERATION_CODE = ? " +
                    "            AND de.TENANT_ID = ? " +
                    "    ORDER BY opm.UPDATED_TIMESTAMP " +
                    "    LIMIT ? OFFSET ?) opr " +
                    " LEFT JOIN DM_DEVICE_OPERATION_RESPONSE ops ON opr.MAPPING_ID = ops.EN_OP_MAP_ID " +
                    " WHERE " +
                    "    opr.OPERATION_CODE = ? " +
                    "    AND opr.TENANT_ID = ? ";
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, operationCode);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, limit);
            stmt.setInt(4, offset);
            stmt.setString(5, operationCode);
            stmt.setInt(6, tenantId);

            rs = stmt.executeQuery();

            int operationId = 0;
            int enrolmentId = 0;
            int responseId = 0;
            Activity activity = null;
            ActivityStatus activityStatus = null;
            List<Integer> largeResponseIDs = new ArrayList<>();
            while (rs.next()) {

                if (operationId != rs.getInt("OPERATION_ID")) {
                    activity = new Activity();
                    activities.add(activity);
                    List<ActivityStatus> statusList = new ArrayList<>();
                    activityStatus = new ActivityStatus();

                    operationId = rs.getInt("OPERATION_ID");
                    enrolmentId = rs.getInt("ENROLMENT_ID");

                    activity.setType(Activity.Type.valueOf(rs.getString("OPERATION_TYPE")));
                    activity.setCreatedTimeStamp(new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));
                    activity.setInitiatedBy(rs.getString("INITIATED_BY"));

                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                    deviceIdentifier.setType(rs.getString("DEVICE_TYPE"));
                    activityStatus.setDeviceIdentifier(deviceIdentifier);

                    activityStatus.setStatus(ActivityStatus.Status.valueOf(rs.getString("STATUS")));

                    List<OperationResponse> operationResponses = new ArrayList<>();
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.setUpdatedTimestamp(new java.util.Date(
                                rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());

                    }
                    if (rs.getTimestamp("RECEIVED_TIMESTAMP") != null) {
                        responseId = rs.getInt("OP_RES_ID");
                        if (rs.getBoolean("IS_LARGE_RESPONSE")) {
                            largeResponseIDs.add(responseId);
                        } else {
                            operationResponses.add(OperationDAOUtil.getOperationResponse(rs));
                        }
                    }
                    activityStatus.setResponses(operationResponses);
                    statusList.add(activityStatus);
                    activity.setActivityStatus(statusList);
                    activity.setActivityId(OperationDAOUtil.getActivityId(rs.getInt("OPERATION_ID")));

                }

                if (operationId == rs.getInt("OPERATION_ID") && enrolmentId != rs.getInt("ENROLMENT_ID")) {
                    activityStatus = new ActivityStatus();

                    activity.setType(Activity.Type.valueOf(rs.getString("OPERATION_TYPE")));
                    activity.setCreatedTimeStamp(new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));

                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                    deviceIdentifier.setType(rs.getString("DEVICE_TYPE"));
                    activityStatus.setDeviceIdentifier(deviceIdentifier);

                    activityStatus.setStatus(ActivityStatus.Status.valueOf(rs.getString("STATUS")));

                    List<OperationResponse> operationResponses = new ArrayList<>();
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.setUpdatedTimestamp(new java.util.Date(
                                rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());
                    }
                    if (rs.getTimestamp("RECEIVED_TIMESTAMP") != null) {
                        responseId = rs.getInt("OP_RES_ID");
                        if (rs.getBoolean("IS_LARGE_RESPONSE")) {
                            largeResponseIDs.add(responseId);
                        } else {
                            operationResponses.add(OperationDAOUtil.getOperationResponse(rs));
                        }
                    }
                    activityStatus.setResponses(operationResponses);
                    activity.getActivityStatus().add(activityStatus);

                    enrolmentId = rs.getInt("ENROLMENT_ID");
                }

                if (rs.getInt("OP_RES_ID") != 0 && responseId != rs.getInt("OP_RES_ID")) {
                    if (rs.getTimestamp("RECEIVED_TIMESTAMP") != null) {
                        responseId = rs.getInt("OP_RES_ID");
                        if (rs.getBoolean("IS_LARGE_RESPONSE")) {
                            largeResponseIDs.add(responseId);
                        } else {
                            activityStatus.getResponses().add(OperationDAOUtil.getOperationResponse(rs));
                        }
                    }
                }
            }
            if (!largeResponseIDs.isEmpty()) {
                populateLargeOperationResponses(activities, largeResponseIDs);
            }

        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while getting the operation details from " +
                    "the database. " + e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return activities;

    }

    @Override
    public int getTotalCountOfFilteredActivities(String operationCode) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();

            String sql = "SELECT COUNT(*) AS COUNT FROM DM_ENROLMENT_OP_MAPPING m\n"
                    + "  INNER JOIN DM_ENROLMENT d ON m.ENROLMENT_ID = d.ID\n"
                    + "  INNER JOIN DM_OPERATION o ON m.OPERATION_ID = o.ID\n"
                    + "WHERE o.OPERATION_CODE = ? AND d.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, operationCode);
            stmt.setInt(2, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("COUNT");
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException(
                    "Error occurred while getting the activity count from the database. " + e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return 0;
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfterByUser(long timestamp, String user, int limit, int offset)
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
                    "    opr.ID AS OP_RES_ID," +
                    "    opr.RECEIVED_TIMESTAMP," +
                    "    opr.OPERATION_RESPONSE," +
                    "    opr.IS_LARGE_RESPONSE " +
                    "FROM " +
                    "    DM_ENROLMENT_OP_MAPPING eom " +
                    "LEFT JOIN " +
                    "    DM_DEVICE_OPERATION_RESPONSE opr ON opr.EN_OP_MAP_ID = eom.ID " +
                    "INNER JOIN " +
                    "    (SELECT DISTINCT OPERATION_ID FROM DM_ENROLMENT_OP_MAPPING ORDER BY OPERATION_ID ASC limit ? , ? ) eom_ordered " +
                    "       ON eom_ordered.OPERATION_ID = eom.OPERATION_ID " +
                    "WHERE " +
                    "    eom.UPDATED_TIMESTAMP > ? " +
                    "        AND eom.TENANT_ID = ? " +
                    "        AND eom.INITIATED_BY = ? " +
                    "ORDER BY eom.OPERATION_ID, eom.UPDATED_TIMESTAMP";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, offset);
                stmt.setInt(2, limit);
                stmt.setLong(3, timestamp);
                stmt.setInt(4, tenantId);
                stmt.setString(5, user);

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
            String msg = "Error occurred while getting the operation details from the database. ";
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        }
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
    public int getActivityCountUpdatedAfter(long timestamp) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT COUNT(DISTINCT(OPERATION_ID)) AS COUNT FROM DM_ENROLMENT_OP_MAPPING WHERE " +
                    "UPDATED_TIMESTAMP > ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, timestamp);
            stmt.setInt(2, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("COUNT");
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException(
                    "Error occurred while getting the activity count from the database. " + e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return 0;
    }

    @Override
    public int getActivityCountUpdatedAfterByUser(long timestamp, String user) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT COUNT(DISTINCT(OPERATION_ID)) AS COUNT " +
                    "FROM DM_ENROLMENT_OP_MAPPING AS m " +
                    "        INNER JOIN " +
                    "    DM_OPERATION dp ON dp.ID = m.OPERATION_ID " +
                    "WHERE m.UPDATED_TIMESTAMP > ?" +
                    "        AND dp.INITIATED_BY = ?" +
                    "        AND m.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, timestamp);
            stmt.setString(2, user);
            stmt.setInt(3, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("COUNT");
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while getting the activity count from " +
                    "the database. " + e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return 0;
    }

    @Override
    public Operation getOperation(int id) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, OPERATION_CODE, INITIATED_BY FROM " +
                    "DM_OPERATION WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
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
                operation.setInitiatedBy(rs.getString("INITIATED_BY"));
            }

        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL Error occurred while retrieving the operation object " +
                    "available for the id '" + id, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operation;
    }

    @Override
    public Operation getOperationByDeviceAndId(int enrolmentId, int operationId) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, o.TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, om.STATUS, " +
                    "o.OPERATION_CODE, o.INITIATED_BY, om.ID AS OM_MAPPING_ID, " +
                    "om.UPDATED_TIMESTAMP FROM (SELECT ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP," +
                    "OPERATION_CODE, INITIATED_BY FROM DM_OPERATION  WHERE id = ?) o INNER JOIN (SELECT * FROM " +
                    "DM_ENROLMENT_OP_MAPPING dm where dm.OPERATION_ID = ? AND dm.ENROLMENT_ID = ?) om " +
                    "ON o.ID = om.OPERATION_ID ";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, operationId);
            stmt.setInt(2, operationId);
            stmt.setInt(3, enrolmentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                operation = new Operation();
                operation.setId(rs.getInt("ID"));
                operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                operation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                if (rs.getLong("UPDATED_TIMESTAMP") == 0) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(
                            new java.sql.Timestamp((rs.getLong("UPDATED_TIMESTAMP") * 1000)).toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
                operation.setInitiatedBy(rs.getString("INITIATED_BY"));
                OperationDAOUtil.setActivityId(operation, rs.getInt("ID"));
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operation " +
                    "available for the device'" + enrolmentId + "' with id '" + operationId, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operation;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(
            int enrolmentId, Operation.Status status) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;
        List<Operation> operations = new ArrayList<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.OPERATION_CODE, " +
                    "o.INITIATED_BY, om.ID AS OM_MAPPING_ID, om.UPDATED_TIMESTAMP FROM DM_OPERATION o " +
                    "INNER JOIN (SELECT * FROM DM_ENROLMENT_OP_MAPPING dm " +
                    "WHERE dm.ENROLMENT_ID = ? AND dm.STATUS = ?) om ON o.ID = om.OPERATION_ID ORDER BY o.CREATED_TIMESTAMP DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            stmt.setString(2, status.toString());
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
                operation.setInitiatedBy(rs.getString("INITIATED_BY"));
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
    public List<? extends Operation> getOperationsByDeviceAndStatus(int enrolmentId, PaginationRequest request,
                                                                    Operation.Status status)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;
        List<Operation> operations = new ArrayList<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.OPERATION_CODE, " +
                    "o.INITIATED_BY, om.ID AS OM_MAPPING_ID, om.UPDATED_TIMESTAMP FROM DM_OPERATION o " +
                    "INNER JOIN (SELECT * FROM DM_ENROLMENT_OP_MAPPING dm " +
                    "WHERE dm.ENROLMENT_ID = ? AND dm.STATUS = ?) om ON o.ID = om.OPERATION_ID ORDER BY " +
                    "o.CREATED_TIMESTAMP DESC LIMIT ?,?";
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
                operation.setInitiatedBy(rs.getString("INITIATED_BY"));
                operation.setStatus(status);
                OperationDAOUtil.setActivityId(operation, rs.getInt("OM_MAPPING_ID"));
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
    public List<? extends Operation> getOperationsForDevice(int enrolmentId)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;
        List<Operation> operations = new ArrayList<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, o.TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, " +
                    "o.OPERATION_CODE, o.INITIATED_BY, om.STATUS, om.ID AS OM_MAPPING_ID, om.UPDATED_TIMESTAMP " +
                    "FROM DM_OPERATION o INNER JOIN (SELECT * FROM DM_ENROLMENT_OP_MAPPING dm " +
                    "WHERE dm.ENROLMENT_ID = ?) om ON o.ID = om.OPERATION_ID " +
                    "ORDER BY o.CREATED_TIMESTAMP DESC, o.ID DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
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
                operation.setInitiatedBy(rs.getString("INITIATED_BY"));
                operation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                operations.add(operation);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operation " +
                    "available for the device'" + enrolmentId + "' with status '", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operations;
    }

    @Override
    public List<? extends Operation> getOperationsForDevice(int enrolmentId, PaginationRequest request)
            throws OperationManagementDAOException {
        Operation operation;
        List<Operation> operations = new ArrayList<>();
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
        StringBuilder sql = new StringBuilder("SELECT " +
                "o.ID, " +
                "TYPE, " +
                "o.CREATED_TIMESTAMP, " +
                "o.RECEIVED_TIMESTAMP, " +
                "o.OPERATION_CODE, " +
                "o.INITIATED_BY, " +
                "om.STATUS, " +
                "om.ID AS OM_MAPPING_ID, " +
                "om.UPDATED_TIMESTAMP " +
                "FROM " +
                "DM_OPERATION o " +
                "INNER JOIN " +
                "(SELECT dm.OPERATION_ID, " +
                "dm.ID, " +
                "dm.STATUS, " +
                "dm.UPDATED_TIMESTAMP " +
                "FROM " +
                "DM_ENROLMENT_OP_MAPPING dm " +
                "WHERE " +
                "dm.ENROLMENT_ID = ?");

        if (updatedFrom != null && updatedFrom != 0 && updatedTo != null && updatedTo != 0) {
            sql.append(" AND dm.UPDATED_TIMESTAMP BETWEEN ? AND ?");
            isUpdatedDayProvided = true;
        }
        sql.append(") om ON o.ID = om.OPERATION_ID ");
        if (createdFrom != null && !createdFrom.isEmpty() && createdTo != null && !createdTo.isEmpty()) {
            sql.append(" WHERE o.CREATED_TIMESTAMP BETWEEN ? AND ?");
            isCreatedDayProvided = true;
        }
        if ((isCreatedDayProvided) && (status != null && !status.isEmpty())) {
            int size = status.size();
            sql.append(" AND (om.STATUS = ? ");
            for (int i = 0; i < size - 1; i++) {
                sql.append(" OR om.STATUS = ?");
            }
            sql.append(")");
            isStatusProvided = true;
        } else if ((!isCreatedDayProvided) && (status != null && !status.isEmpty())) {
            int size = status.size();
            sql.append(" WHERE (om.STATUS = ? ");
            for (int i = 0; i < size - 1; i++) {
                sql.append(" OR om.STATUS = ?");
            }
            sql.append(")");
            isStatusProvided = true;
        }
        if ((isCreatedDayProvided || isStatusProvided) && (operationCode != null && !operationCode.isEmpty())) {
            int size = operationCode.size();
            sql.append(" AND (o.OPERATION_CODE = ? ");
            for (int i = 0; i < size - 1; i++) {
                sql.append(" OR o.OPERATION_CODE = ?");
            }
            sql.append(")");
            isOperationCodeProvided = true;
        } else if ((!isCreatedDayProvided && !isStatusProvided) && (operationCode != null && !operationCode.isEmpty())) {
            int size = operationCode.size();
            sql.append(" WHERE (o.OPERATION_CODE = ? ");
            for (int i = 0; i < size - 1; i++) {
                sql.append(" OR o.OPERATION_CODE = ?");
            }
            sql.append(")");
            isOperationCodeProvided = true;
        }
        sql.append(" ORDER BY o.CREATED_TIMESTAMP DESC LIMIT ?,?");
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;
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
                    for (String s : status) {
                        stmt.setString(paramIndex++, s);
                    }
                }
                if (isOperationCodeProvided) {
                    for (String s : operationCode) {
                        stmt.setString(paramIndex++, s);
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
                        if (rs.getLong("UPDATED_TIMESTAMP") == 0) {
                            operation.setReceivedTimeStamp("");
                        } else {
                            operation.setReceivedTimeStamp(
                                    new java.sql.Timestamp((rs.getLong("UPDATED_TIMESTAMP") * 1000)).toString());
                        }
                        operation.setCode(rs.getString("OPERATION_CODE"));
                        operation.setInitiatedBy(rs.getString("INITIATED_BY"));
                        operation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                        OperationDAOUtil.setActivityId(operation, rs.getInt("ID"));
                        operations.add(operation);
                    }
                }
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operation " +
                    "available for the device'" + enrolmentId + "' with status '", e);
        }
        return operations;
    }

    @Override
    public int getOperationCountForDevice(int enrolmentId, PaginationRequest request)
            throws OperationManagementDAOException {
        String createdTo = null;
        String createdFrom = null;
        DateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        if (request.getOperationLogFilters().getCreatedDayFrom() != null) {
            createdFrom = simple.format(request.getOperationLogFilters().getCreatedDayFrom());
        }
        if (request.getOperationLogFilters().getCreatedDayTo() != null) {
            createdTo = simple.format(request.getOperationLogFilters().getCreatedDayTo());
        }

        Long updatedFrom = request.getOperationLogFilters().getUpdatedDayFrom();
        Long updatedTo = request.getOperationLogFilters().getUpdatedDayTo();
        List<String> operationCodes = request.getOperationLogFilters().getOperationCode();
        List<String> status = request.getOperationLogFilters().getStatus();
        boolean isCreatedDayProvided = false;
        boolean isUpdatedDayProvided = false;
        boolean isOperationCodeProvided = false;
        boolean isStatusProvided = false;

        String sql = "SELECT "
                + "COUNT(o.ID) AS OPERATION_COUNT "
                + "FROM "
                + "DM_OPERATION o "
                + "INNER JOIN "
                + "(SELECT dm.OPERATION_ID, "
                + "dm.ID, "
                + "dm.STATUS, "
                + "dm.UPDATED_TIMESTAMP "
                + "FROM "
                + "DM_ENROLMENT_OP_MAPPING dm "
                + "WHERE "
                + "dm.ENROLMENT_ID = ?";

        if (updatedFrom != null && updatedFrom != 0 && updatedTo != null && updatedTo != 0) {
            sql += " AND dm.UPDATED_TIMESTAMP BETWEEN ? AND ?";
            isUpdatedDayProvided = true;
        }
        sql += ") om ON o.ID = om.OPERATION_ID ";
        if (createdFrom != null && !createdFrom.isEmpty() && createdTo != null && !createdTo.isEmpty()) {
            sql += " WHERE o.CREATED_TIMESTAMP BETWEEN ? AND ?";
            isCreatedDayProvided = true;
        }
        if (status != null && !status.isEmpty()) {
            if (isCreatedDayProvided) {
                sql += " AND (om.STATUS = ? ";
            } else {
                sql += " WHERE (om.STATUS = ? ";
            }
            sql = IntStream.range(0, status.size() - 1).mapToObj(i -> " OR om.STATUS = ?")
                    .collect(Collectors.joining("", sql, ""));
            sql += ")";
            isStatusProvided = true;
        }
        if (operationCodes != null && !operationCodes.isEmpty()) {
            if (isCreatedDayProvided || isStatusProvided) {
                sql += " AND (o.OPERATION_CODE = ? ";
            } else {
                sql += " WHERE (o.OPERATION_CODE = ? ";
            }
            sql = IntStream.range(0, operationCodes.size() - 1).mapToObj(i -> " OR o.OPERATION_CODE = ?")
                    .collect(Collectors.joining("", sql, ""));
            sql += ")";
            isOperationCodeProvided = true;
        }
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIndex = 1;
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
                    for (String s : status) {
                        stmt.setString(paramIndex++, s);
                    }
                }
                if (isOperationCodeProvided) {
                    for (String s : operationCodes) {
                        stmt.setString(paramIndex++, s);
                    }
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("OPERATION_COUNT");
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while retrieving the operation count of the device" + enrolmentId
                    + " for search query";
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        }
        return 0;
    }

    @Override
    public Operation getNextOperation(int enrolmentId, Operation.Status status) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement("SELECT o.ID, o.TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, " +
                    "o.OPERATION_CODE, o.INITIATED_BY, om.ID AS OM_MAPPING_ID, om.UPDATED_TIMESTAMP FROM DM_OPERATION o " +
                    "INNER JOIN (SELECT * FROM DM_ENROLMENT_OP_MAPPING dm " +
                    "WHERE dm.ENROLMENT_ID = ? AND dm.STATUS = ?) om ON o.ID = om.OPERATION_ID " +
                    "ORDER BY om.UPDATED_TIMESTAMP ASC, om.ID ASC LIMIT 1");
            stmt.setInt(1, enrolmentId);
            stmt.setString(2, status.toString());
            rs = stmt.executeQuery();

            Operation operation = null;
            if (rs.next()) {
                operation = new Operation();
                operation.setType(OperationDAOUtil.getType(rs.getString("TYPE")));
                operation.setId(rs.getInt("ID"));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());

                if (rs.getLong("UPDATED_TIMESTAMP") == 0) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(
                            new java.sql.Timestamp((rs.getLong("UPDATED_TIMESTAMP") * 1000)).toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
                operation.setInitiatedBy(rs.getString("INITIATED_BY"));
                operation.setStatus(Operation.Status.PENDING);
                OperationDAOUtil.setActivityId(operation, rs.getInt("ID"));
            }
            return operation;
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while getting operation metadata. " +
                    e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    public List<? extends Operation> getOperationsByDeviceStatusAndType(
            int enrolmentId, Operation.Status status, Operation.Type type) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;
        List<Operation> operations = new ArrayList<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, OPERATION_CODE, o.INITIATED_BY," +
                    " om.ID AS OM_MAPPING_ID, om.UPDATED_TIMESTAMP FROM " +
                    "(SELECT o.ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, OPERATION_CODE, INITIATED_BY " +
                    "FROM DM_OPERATION o WHERE o.TYPE = ?) o INNER JOIN (SELECT * FROM DM_ENROLMENT_OP_MAPPING dm " +
                    "WHERE dm.ENROLMENT_ID = ? AND dm.STATUS = ?) om ON o.ID = om.OPERATION_ID ORDER BY o.CREATED_TIMESTAMP ASC";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, type.toString());
            stmt.setInt(2, enrolmentId);
            stmt.setString(3, status.toString());
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
                operation.setInitiatedBy(rs.getString("INITIATED_BY"));
                OperationDAOUtil.setActivityId(operation, rs.getInt("ID"));
                operations.add(operation);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operation available " +
                    "for the device'" + enrolmentId + "' with status '" + status.toString(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operations;
    }

    @Override
    public Map<Integer, List<OperationMapping>> getOperationMappingsByStatus(Operation.Status opStatus, Operation.PushNotificationStatus pushNotificationStatus,
                                                                             int limit) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn;
        OperationMapping operationMapping;
        Map<Integer, List<OperationMapping>> operationMappingsTenantMap = new HashMap<>();
        try {
            conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT op.ENROLMENT_ID, op.OPERATION_ID, d.DEVICE_IDENTIFICATION, dt.NAME as DEVICE_TYPE, " +
                    "d.TENANT_ID FROM DM_DEVICE d, DM_ENROLMENT_OP_MAPPING op, DM_DEVICE_TYPE dt  WHERE op.STATUS = ?" +
                    " AND op.PUSH_NOTIFICATION_STATUS = ? AND d.DEVICE_TYPE_ID = dt.ID AND d.ID=op.ENROLMENT_ID ORDER" +
                    " BY op.OPERATION_ID LIMIT ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, opStatus.toString());
            stmt.setString(2, pushNotificationStatus.toString());
            stmt.setInt(3, limit);
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
            throw new OperationManagementDAOException("SQL error while getting operation mappings from database. " +
                    e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operationMappingsTenantMap;
    }

    @Override
    public List<Activity> getActivities(ActivityPaginationRequest activityPaginationRequest)
            throws OperationManagementDAOException {
        try {
            boolean isTimeDurationFilteringProvided = false;
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
                    "    opr.ID AS OP_RES_ID," +
                    "    opr.RECEIVED_TIMESTAMP," +
                    "    opr.OPERATION_RESPONSE," +
                    "    opr.IS_LARGE_RESPONSE " +
                    "FROM " +
                    "    DM_ENROLMENT_OP_MAPPING eom " +
                    "LEFT JOIN " +
                    "    DM_DEVICE_OPERATION_RESPONSE opr ON opr.EN_OP_MAP_ID = eom.ID " +
                    "INNER JOIN " +
                    "    (SELECT DISTINCT OPERATION_ID FROM DM_ENROLMENT_OP_MAPPING WHERE TENANT_ID = ? ";

            if (activityPaginationRequest.getDeviceType() != null) {
                sql += "AND DEVICE_TYPE = ? ";
            }
            if (activityPaginationRequest.getDeviceId() != null) {
                sql += "AND DEVICE_IDENTIFICATION = ? ";
            }
            if (activityPaginationRequest.getOperationCode() != null) {
                sql += "AND OPERATION_CODE = ? ";
            }
            if (activityPaginationRequest.getInitiatedBy() != null) {
                sql += "AND INITIATED_BY = ? ";
            }
            if (activityPaginationRequest.getSince() != 0) {
                sql += "AND UPDATED_TIMESTAMP > ? ";
            }
            if (activityPaginationRequest.getStartTimestamp() > 0 && activityPaginationRequest.getEndTimestamp() > 0) {
                isTimeDurationFilteringProvided = true;
                sql += "AND CREATED_TIMESTAMP BETWEEN  ? AND ? ";
            }
            if (activityPaginationRequest.getType() != null) {
                sql += "AND TYPE = ? ";
            }
            if (activityPaginationRequest.getStatus() != null) {
                sql += "AND STATUS = ? ";
            }

            sql += "ORDER BY OPERATION_ID ASC limit ? , ? ) eom_ordered " +
                    "ON eom_ordered.OPERATION_ID = eom.OPERATION_ID WHERE eom.TENANT_ID = ? ";

            if (activityPaginationRequest.getDeviceType() != null) {
                sql += "AND eom.DEVICE_TYPE = ? ";
            }
            if (activityPaginationRequest.getDeviceId() != null) {
                sql += "AND eom.DEVICE_IDENTIFICATION = ? ";
            }
            if (activityPaginationRequest.getOperationCode() != null) {
                sql += "AND eom.OPERATION_CODE = ? ";
            }
            if (activityPaginationRequest.getInitiatedBy() != null) {
                sql += "AND eom.INITIATED_BY = ? ";
            }
            if (activityPaginationRequest.getSince() != 0) {
                sql += "AND eom.UPDATED_TIMESTAMP > ? ";
            }
            if (isTimeDurationFilteringProvided) {
                sql += "AND eom.CREATED_TIMESTAMP BETWEEN  ? AND ? ";
            }
            if (activityPaginationRequest.getType() != null) {
                sql += "AND eom.TYPE = ? ";
            }
            if (activityPaginationRequest.getStatus() != null) {
                sql += "AND eom.STATUS = ? ";
            }

            sql += "ORDER BY eom.OPERATION_ID, eom.UPDATED_TIMESTAMP";

            int index = 1;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(index++, tenantId);
                if (activityPaginationRequest.getDeviceType() != null) {
                    stmt.setString(index++, activityPaginationRequest.getDeviceType());
                }
                if (activityPaginationRequest.getDeviceId() != null) {
                    stmt.setString(index++, activityPaginationRequest.getDeviceId());
                }
                if (activityPaginationRequest.getOperationCode() != null) {
                    stmt.setString(index++, activityPaginationRequest.getOperationCode());
                }
                if (activityPaginationRequest.getInitiatedBy() != null) {
                    stmt.setString(index++, activityPaginationRequest.getInitiatedBy());
                }
                if (activityPaginationRequest.getSince() != 0) {
                    stmt.setLong(index++, activityPaginationRequest.getSince());
                }
                if (isTimeDurationFilteringProvided) {
                    stmt.setLong(index++, activityPaginationRequest.getStartTimestamp());
                    stmt.setLong(index++, activityPaginationRequest.getEndTimestamp());
                }
                if (activityPaginationRequest.getType() != null) {
                    stmt.setString(index++, activityPaginationRequest.getType().name());
                }
                if (activityPaginationRequest.getStatus() != null) {
                    stmt.setString(index++, activityPaginationRequest.getStatus().name());
                }

                stmt.setInt(index++, activityPaginationRequest.getOffset());
                stmt.setInt(index++, activityPaginationRequest.getLimit());
                stmt.setInt(index++, tenantId);

                if (activityPaginationRequest.getDeviceType() != null) {
                    stmt.setString(index++, activityPaginationRequest.getDeviceType());
                }
                if (activityPaginationRequest.getDeviceId() != null) {
                    stmt.setString(index++, activityPaginationRequest.getDeviceId());
                }
                if (activityPaginationRequest.getOperationCode() != null) {
                    stmt.setString(index++, activityPaginationRequest.getOperationCode());
                }
                if (activityPaginationRequest.getInitiatedBy() != null) {
                    stmt.setString(index++, activityPaginationRequest.getInitiatedBy());
                }
                if (activityPaginationRequest.getSince() != 0) {
                    stmt.setLong(index++, activityPaginationRequest.getSince());
                }
                if (isTimeDurationFilteringProvided) {
                    stmt.setLong(index++, activityPaginationRequest.getStartTimestamp());
                    stmt.setLong(index++, activityPaginationRequest.getEndTimestamp());
                }
                if (activityPaginationRequest.getType() != null) {
                    stmt.setString(index++, activityPaginationRequest.getType().name());
                }
                if (activityPaginationRequest.getStatus() != null) {
                    stmt.setString(index, activityPaginationRequest.getStatus().name());
                }

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
    public int getActivitiesCount(ActivityPaginationRequest activityPaginationRequest)
            throws OperationManagementDAOException {
        try {
            boolean isTimeDurationFilteringProvided = false;
            Connection conn = OperationManagementDAOFactory.getConnection();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String sql = "SELECT count(DISTINCT OPERATION_ID) AS ACTIVITY_COUNT FROM DM_ENROLMENT_OP_MAPPING " +
                    "WHERE TENANT_ID = ? ";

            if (activityPaginationRequest.getDeviceType() != null) {
                sql += "AND DEVICE_TYPE = ? ";
            }
            if (activityPaginationRequest.getDeviceId() != null) {
                sql += "AND DEVICE_IDENTIFICATION = ? ";
            }
            if (activityPaginationRequest.getOperationCode() != null) {
                sql += "AND OPERATION_CODE = ? ";
            }
            if (activityPaginationRequest.getInitiatedBy() != null) {
                sql += "AND INITIATED_BY = ? ";
            }
            if (activityPaginationRequest.getSince() != 0) {
                sql += "AND UPDATED_TIMESTAMP > ? ";
            }
            if (activityPaginationRequest.getType() != null) {
                sql += "AND TYPE = ? ";
            }
            if (activityPaginationRequest.getStatus() != null) {
                sql += "AND STATUS = ? ";
            }
            if (activityPaginationRequest.getStartTimestamp() > 0 && activityPaginationRequest.getEndTimestamp() > 0) {
                isTimeDurationFilteringProvided = true;
                sql += "AND CREATED_TIMESTAMP BETWEEN ? AND ? ";
            }

            int index = 1;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(index++, tenantId);
                if (activityPaginationRequest.getDeviceType() != null) {
                    stmt.setString(index++, activityPaginationRequest.getDeviceType());
                }
                if (activityPaginationRequest.getDeviceId() != null) {
                    stmt.setString(index++, activityPaginationRequest.getDeviceId());
                }
                if (activityPaginationRequest.getOperationCode() != null) {
                    stmt.setString(index++, activityPaginationRequest.getOperationCode());
                }
                if (activityPaginationRequest.getInitiatedBy() != null) {
                    stmt.setString(index++, activityPaginationRequest.getInitiatedBy());
                }
                if (activityPaginationRequest.getSince() != 0) {
                    stmt.setLong(index++, activityPaginationRequest.getSince());
                }
                if (activityPaginationRequest.getType() != null) {
                    stmt.setString(index++, activityPaginationRequest.getType().name());
                }
                if (activityPaginationRequest.getStatus() != null) {
                    stmt.setString(index++, activityPaginationRequest.getStatus().name());
                }
                if (isTimeDurationFilteringProvided) {
                    stmt.setLong(index++, activityPaginationRequest.getStartTimestamp());
                    stmt.setLong(index, activityPaginationRequest.getEndTimestamp());
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("ACTIVITY_COUNT");
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while getting the operation details from the database.";
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        }
        return 0;
    }

}
