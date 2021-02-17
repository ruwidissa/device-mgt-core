/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.core.operation.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationEnrolmentMapping;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMapping;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationMappingDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperationMappingDAOImpl implements OperationMappingDAO {

    private static final Log log = LogFactory.getLog(OperationMappingDAOImpl.class);

    @Override
    public void addOperationMapping(Operation operation, Integer deviceId, boolean isScheduled, Device device, Integer tenantId) throws
            OperationManagementDAOException {
        PreparedStatement stmt = null;
        try {
            long time = System.currentTimeMillis() / 1000;
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_ENROLMENT_OP_MAPPING(ENROLMENT_ID, OPERATION_ID, STATUS, " +
                    "PUSH_NOTIFICATION_STATUS, CREATED_TIMESTAMP, UPDATED_TIMESTAMP, OPERATION_CODE, INITIATED_BY, " +
                         "TYPE, DEVICE_TYPE, DEVICE_ID, DEVICE_IDENTIFICATION, TENANT_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, operation.getId());
            stmt.setString(3, Operation.Status.PENDING.toString());
            if (isScheduled) {
                stmt.setString(4, Operation.PushNotificationStatus.SCHEDULED.toString());
            } else {
                stmt.setString(4, Operation.PushNotificationStatus.COMPLETED.toString());
            }
            stmt.setLong(5, time);
            stmt.setLong(6, time);
            stmt.setString(7, operation.getCode());
            stmt.setString(8, operation.getInitiatedBy());
            stmt.setString(9, operation.getType().toString());
            stmt.setString(10, device.getType());
            stmt.setInt(11, device.getId());
            stmt.setString(12, device.getDeviceIdentifier());
            stmt.setInt(13, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while persisting device operation mappings. " +
                    e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void addOperationMapping(Operation operation, List<Device> devices, boolean isScheduled, Integer tenantId) throws
            OperationManagementDAOException {
        PreparedStatement stmt = null;
        try {
            long time = System.currentTimeMillis() / 1000;
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_ENROLMENT_OP_MAPPING(ENROLMENT_ID, OPERATION_ID, STATUS, " +
                         "PUSH_NOTIFICATION_STATUS, CREATED_TIMESTAMP, UPDATED_TIMESTAMP, OPERATION_CODE, INITIATED_BY, " +
                         "TYPE, DEVICE_TYPE, DEVICE_ID, DEVICE_IDENTIFICATION, TENANT_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            for (Device device : devices) {
                stmt.setInt(1, device.getEnrolmentInfo().getId());
                stmt.setInt(2, operation.getId());
                stmt.setString(3, Operation.Status.PENDING.toString());
                if (isScheduled) {
                    stmt.setString(4, Operation.PushNotificationStatus.SCHEDULED.toString());
                } else {
                    stmt.setString(4, Operation.PushNotificationStatus.COMPLETED.toString());
                }
                stmt.setLong(5, time);
                stmt.setLong(6, time);
                stmt.setString(7, operation.getCode());
                stmt.setString(8, operation.getInitiatedBy());
                stmt.setString(9, operation.getType().toString());
                stmt.setString(10, device.getType());
                stmt.setInt(11, device.getId());
                stmt.setString(12, device.getDeviceIdentifier());
                stmt.setInt(13, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while persisting device operation mappings. " +
                    e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void removeOperationMapping(int operationId,
                                       Integer deviceId) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_ENROLMENT_OP_MAPPING WHERE ENROLMENT_ID = ? AND OPERATION_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, operationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while persisting device operation mappings. " +
                    e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void updateOperationMapping(int operationId, Integer deviceId,
                                       Operation.PushNotificationStatus pushNotificationStatus)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "UPDATE DM_ENROLMENT_OP_MAPPING SET PUSH_NOTIFICATION_STATUS = ? WHERE ENROLMENT_ID = ? and " +
                    "OPERATION_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, pushNotificationStatus.toString());
            stmt.setInt(2, deviceId);
            stmt.setInt(3, operationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while updating device operation mappings. " +
                    e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void updateOperationMapping(List<OperationMapping> operationMappingList) throws
            OperationManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "UPDATE DM_ENROLMENT_OP_MAPPING SET PUSH_NOTIFICATION_STATUS = ? WHERE ENROLMENT_ID = ? and " +
                    "OPERATION_ID = ?";
            stmt = conn.prepareStatement(sql);
            if (conn.getMetaData().supportsBatchUpdates()) {
                for (OperationMapping operationMapping : operationMappingList) {
                    stmt.setString(1, operationMapping.getPushNotificationStatus().toString());
                    stmt.setInt(2, operationMapping.getEnrollmentId());
                    stmt.setInt(3, operationMapping.getOperationId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } else {
                for (OperationMapping operationMapping : operationMappingList) {
                    stmt.setString(1, operationMapping.getPushNotificationStatus().toString());
                    stmt.setInt(2, operationMapping.getEnrollmentId());
                    stmt.setInt(3, operationMapping.getOperationId());
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while updating device operation mappings as " +
                    "batch . " + e.getMessage(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    private OperationEnrolmentMapping getEnrolmentOpMapping(ResultSet rs) throws SQLException {
        OperationEnrolmentMapping enrolmentOperationMapping = new OperationEnrolmentMapping();
        enrolmentOperationMapping.setEnrolmentId(rs.getInt("ENROLMENT_ID"));
        enrolmentOperationMapping.setDeviceId(rs.getString("DEVICE_IDENTIFIER"));
        enrolmentOperationMapping.setTenantId(rs.getInt("TENANT_ID"));
        enrolmentOperationMapping.setCreatedTime(rs.getLong("CREATED_TIMESTAMP"));
        enrolmentOperationMapping.setDeviceStatus(rs.getString("ENROLMENT_STATUS"));
        return enrolmentOperationMapping;
    }
}
