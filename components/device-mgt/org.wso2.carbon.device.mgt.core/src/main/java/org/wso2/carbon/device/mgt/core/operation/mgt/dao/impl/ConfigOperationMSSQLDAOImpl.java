/*
 * Copyright (c) 2019, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.operation.mgt.dao.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.ConfigOperation;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ConfigOperationMSSQLDAOImpl extends GenericOperationDAOImpl {

    private static final Log log = LogFactory.getLog(ConfigOperationMSSQLDAOImpl.class);

    @Override
    public int addOperation(Operation operation) throws OperationManagementDAOException {
        int operationId;
        PreparedStatement stmt = null;
        try {
            operationId = super.addOperation(operation);
            operation.setCreatedTimeStamp(new Timestamp(new java.util.Date().getTime()).toString());
            Connection conn = OperationManagementDAOFactory.getConnection();
            stmt = conn.prepareStatement("INSERT INTO DM_CONFIG_OPERATION(OPERATION_ID, OPERATION_CONFIG) VALUES(?, ?)");
            stmt.setInt(1, operationId);
            stmt.setBinaryStream(2, toByteArrayInputStream(operation));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while adding command operation", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
        return operationId;
    }

    private ByteArrayInputStream toByteArrayInputStream(Operation operation) throws OperationManagementDAOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutput = new ObjectOutputStream(bos);
            objectOutput.writeObject(operation);
            objectOutput.flush();
        } catch (IOException e) {
            String msg = "Error when  converting operation id " + operation + " to input stream";
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        }
        return new ByteArrayInputStream(bos.toByteArray());
    }

    private Object fromByteArrayInputStream(InputStream binaryInput) throws OperationManagementDAOException {
        Object object;
        ObjectInputStream in = null;
        try {
            if (binaryInput == null || binaryInput.available() == 0) {
                return null;
            }
            in = new ObjectInputStream(binaryInput);
            object = in.readObject();
        } catch (ClassNotFoundException e) {
            String msg = "Error when  converting store config to operation due to missing class";
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        } catch (IOException e) {
            String msg = "Error when  converting store config to operation";
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
        return object;
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
            String sql = "SELECT OPERATION_ID, ENABLED, OPERATION_CONFIG FROM DM_CONFIG_OPERATION WHERE OPERATION_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, operationId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                InputStream operationDetails = rs.getBinaryStream("OPERATION_CONFIG");
                configOperation = (ConfigOperation) fromByteArrayInputStream(operationDetails);
                configOperation.setId(rs.getInt("OPERATION_ID"));
                configOperation.setEnabled(rs.getBoolean("ENABLED"));
            }
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
            String sql = "SELECT co.OPERATION_ID, co.OPERATION_CONFIG FROM DM_CONFIG_OPERATION co " +
                    "INNER JOIN (SELECT * FROM DM_ENROLMENT_OP_MAPPING WHERE ENROLMENT_ID = ? " +
                    "AND STATUS = ?) dm ON dm.OPERATION_ID = co.OPERATION_ID";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            stmt.setString(2, status.toString());
            rs = stmt.executeQuery();

            while (rs.next()) {
                InputStream operationDetails = rs.getBinaryStream("OPERATION_CONFIG");
                configOperation = (ConfigOperation) fromByteArrayInputStream(operationDetails);
                configOperation.setStatus(status);
                configOperation.setId(rs.getInt("OPERATION_ID"));
                operations.add(configOperation);
            }
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
