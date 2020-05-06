/*
 * Copyright (c) 2016a, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.ActivityStatus;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationResponse;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.impl.GenericOperationDAOImpl;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.util.OperationDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the implementation of OperationDAO which can be used to support MySQl db syntax.
 */
public class MySQLOperationDAOImpl extends GenericOperationDAOImpl {

    private static final Log log = LogFactory.getLog(MySQLOperationDAOImpl.class);

    @Override
    public List<Activity> getActivityList(List<Integer> activityIds) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Activity activity;
        List<Activity> activities = new ArrayList<>();

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();

            String sql1 = "SELECT " +
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
                    "        LEFT JOIN " +
                    "    DM_DEVICE_OPERATION_RESPONSE opr ON opr.EN_OP_MAP_ID = eom.ID " +
                    "WHERE " +
                    "    eom.OPERATION_ID IN (";

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < activityIds.size(); i++) {
                builder.append("?,");
            }
            sql1 += builder.deleteCharAt(builder.length() - 1).toString() + ") AND eom.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql1);
            int i;
            for (i = 0; i < activityIds.size(); i++) {
                stmt.setInt(i + 1, activityIds.get(i));
            }
            stmt.setInt(i + 1, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

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

                    activity.setType(Activity.Type.valueOf(rs.getString("TYPE")));
                    activity.setCreatedTimeStamp(
                            new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));
                    activity.setInitiatedBy(rs.getString("INITIATED_BY"));

                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                    deviceIdentifier.setType(rs.getString("DEVICE_TYPE"));

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
                    statusList.add(activityStatus);
                    activity.setActivityStatus(statusList);
                    activity.setActivityId(OperationDAOUtil.getActivityId(rs.getInt("OPERATION_ID")));
                }

                if (operationId == rs.getInt("OPERATION_ID") && enrolmentId != rs.getInt("ENROLMENT_ID")) {
                    activityStatus = new ActivityStatus();

                    activity.setType(Activity.Type.valueOf(rs.getString("TYPE")));
                    activity.setCreatedTimeStamp(
                            new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));
                    activity.setInitiatedBy(rs.getString("INITIATED_BY"));

                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                    deviceIdentifier.setType(rs.getString("DEVICE_TYPE"));
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
            if(!largeResponseIDs.isEmpty()) {
                populateLargeOperationResponses(activities, largeResponseIDs);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException(
                    "Error occurred while getting the operation details from " + "the database.", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return activities;
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfter(long timestamp, int limit,
                                                    int offset)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Activity> activities = new ArrayList<>();
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
                    "    DM_ENROLMENT_OP_MAPPING eom FORCE INDEX (IDX_ENROLMENT_OP_MAPPING) " +
                    "        LEFT JOIN " +
                    "    DM_DEVICE_OPERATION_RESPONSE opr ON opr.EN_OP_MAP_ID = eom.ID " +
                    "WHERE " +
                    "    eom.UPDATED_TIMESTAMP > ? " +
                    "        AND eom.TENANT_ID = ? " +
                    "ORDER BY eom.UPDATED_TIMESTAMP " +
                    "LIMIT ? OFFSET ?";

            stmt = conn.prepareStatement(sql);

            stmt.setLong(1, timestamp);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, limit);
            stmt.setInt(4, offset);

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

                    activity.setType(Activity.Type.valueOf(rs.getString("TYPE")));
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

                    activity.setType(Activity.Type.valueOf(rs.getString("TYPE")));
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
            if(!largeResponseIDs.isEmpty()) {
                populateLargeOperationResponses(activities, largeResponseIDs);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while getting the operation details from " +
                    "the database.", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return activities;
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfterByUser(long timestamp, String user, int limit, int offset)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Activity> activities = new ArrayList<>();
        List<Integer> largeResponseIDs = new ArrayList<>();
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
                    "    DM_ENROLMENT_OP_MAPPING eom FORCE INDEX (IDX_ENROLMENT_OP_MAPPING) " +
                    "        LEFT JOIN " +
                    "    DM_DEVICE_OPERATION_RESPONSE opr ON opr.EN_OP_MAP_ID = eom.ID " +
                    "WHERE " +
                    "    eom.UPDATED_TIMESTAMP > ? " +
                    "        AND eom.TENANT_ID = ? " +
                    "        AND eom.INITIATED_BY = ? " +
                    "ORDER BY eom.UPDATED_TIMESTAMP " +
                    "LIMIT ? OFFSET ?";

            stmt = conn.prepareStatement(sql);

            stmt.setLong(1, timestamp);
            stmt.setInt(2, tenantId);
            stmt.setString(3, user);
            stmt.setInt(4, limit);
            stmt.setInt(5, offset);

//            stmt.setLong(1, timestamp);
//            stmt.setString(2, user);
//            stmt.setInt(3, tenantId);
//            stmt.setInt(4, limit);
//            stmt.setInt(5, offset);
//            stmt.setLong(6, timestamp);
//            stmt.setString(7, user);
//            stmt.setInt(8, tenantId);

            rs = stmt.executeQuery();

            int operationId = 0;
            int enrolmentId = 0;
            int responseId = 0;
            Activity activity = null;
            ActivityStatus activityStatus = null;
            while (rs.next()) {

                if (operationId != rs.getInt("OPERATION_ID")) {
                    activity = new Activity();
                    activities.add(activity);
                    List<ActivityStatus> statusList = new ArrayList<>();
                    activityStatus = new ActivityStatus();

                    operationId = rs.getInt("OPERATION_ID");
                    enrolmentId = rs.getInt("ENROLMENT_ID");

                    activity.setType(Activity.Type.valueOf(rs.getString("TYPE")));
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
                    if (rs.getTimestamp("RECEIVED_TIMESTAMP") != (null)) {
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

                    activity.setType(Activity.Type.valueOf(rs.getString("TYPE")));
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
                    if (rs.getTimestamp("RECEIVED_TIMESTAMP") != (null)) {
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
                    if (rs.getTimestamp("RECEIVED_TIMESTAMP") != (null)) {
                        responseId = rs.getInt("OP_RES_ID");
                        if (rs.getBoolean("IS_LARGE_RESPONSE")) {
                            largeResponseIDs.add(responseId);
                        } else {
                            activityStatus.getResponses().add(OperationDAOUtil.getOperationResponse(rs));
                        }
                    }
                }
            }
            if(!largeResponseIDs.isEmpty()) {
                populateLargeOperationResponses(activities, largeResponseIDs);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while getting the operation details from " +
                    "the database.", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return activities;
    }

}