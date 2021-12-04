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
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.ConfigOperation;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConfigOperationDAOImpl extends GenericOperationDAOImpl {

    private static final Log log = LogFactory.getLog(ConfigOperationDAOImpl.class);

    @Override
    public int addOperation(Operation operation) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            operation.setCreatedTimeStamp(new Timestamp(new Date().getTime()).toString());
            Connection connection = OperationManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_OPERATION(TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, OPERATION_CODE, " +
                         "INITIATED_BY, OPERATION_DETAILS) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, operation.getType().toString());
            stmt.setLong(2, DeviceManagementDAOUtil.getCurrentUTCTime());
            stmt.setLong(3, 0);
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
            throw new OperationManagementDAOException("Error occurred while adding command operation", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public Operation getOperation(int operationId) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ConfigOperation configOperation = null;

        ByteArrayInputStream bais;
        ObjectInputStream ois;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT ID, ENABLED, OPERATION_DETAILS FROM DM_OPERATION WHERE ID = ? AND TYPE='CONFIG'";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, operationId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                byte[] operationDetails = rs.getBytes("OPERATION_DETAILS");
                bais = new ByteArrayInputStream(operationDetails);
                ois = new ObjectInputStream(bais);
                configOperation = (ConfigOperation) ois.readObject();
                configOperation.setId(rs.getInt("ID"));
                configOperation.setEnabled(rs.getBoolean("ENABLED"));
            }
        } catch (IOException e) {
            throw new OperationManagementDAOException("IO Error occurred while de serialize the policy operation " +
                    "object", e);
        } catch (ClassNotFoundException e) {
            throw new OperationManagementDAOException("Class not found error occurred while de serialize the policy " +
                    "operation object", e);
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL Error occurred while retrieving the policy operation " +
                    "object available for the id '"
                    + operationId, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return configOperation;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(int enrolmentId,
            Operation.Status status) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ConfigOperation configOperation;
        List<Operation> operations = new ArrayList<>();

        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT co.ID, co.OPERATION_DETAILS FROM DM_OPERATION co " +
                    "INNER JOIN (SELECT * FROM DM_ENROLMENT_OP_MAPPING WHERE ENROLMENT_ID = ? " +
                    "AND STATUS = ?) dm ON dm.OPERATION_ID = co.ID WHERE co.TYPE = 'CONFIG'";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            stmt.setString(2, status.toString());
            rs = stmt.executeQuery();

            while (rs.next()) {
                byte[] operationDetails = rs.getBytes("OPERATION_DETAILS");
                bais = new ByteArrayInputStream(operationDetails);
                ois = new ObjectInputStream(bais);
                configOperation = (ConfigOperation) ois.readObject();
                configOperation.setStatus(status);
                configOperation.setId(rs.getInt("ID"));
                operations.add(configOperation);
            }
        } catch (IOException e) {
            throw new OperationManagementDAOException("IO Error occurred while de serialize the configuration " +
                    "operation object", e);
        } catch (ClassNotFoundException e) {
            throw new OperationManagementDAOException("Class not found error occurred while de serialize the " +
                    "configuration operation object", e);
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operation available " +
                    "for the device'" + enrolmentId + "' with status '" + status.toString(), e);
        } finally {
            if (bais != null) {
                try {
                    bais.close();
                } catch (IOException e) {
                    log.warn("Error occurred while closing ByteArrayOutputStream", e);
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    log.warn("Error occurred while closing ObjectOutputStream", e);
                }
            }
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operations;
    }

}
