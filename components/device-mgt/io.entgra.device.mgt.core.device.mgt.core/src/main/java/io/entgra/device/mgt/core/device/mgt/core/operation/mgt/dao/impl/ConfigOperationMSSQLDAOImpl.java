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

package io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.ConfigOperation;
import io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

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
import java.util.Date;
import java.util.List;

public class ConfigOperationMSSQLDAOImpl extends GenericOperationDAOImpl {

    private static final Log log = LogFactory.getLog(ConfigOperationMSSQLDAOImpl.class);

    @Override
    public int addOperation(Operation operation) throws OperationManagementDAOException {
        try {
            operation.setCreatedTimeStamp(new Timestamp(new Date().getTime()).toString());
            Connection connection = OperationManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_OPERATION(TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, OPERATION_CODE, " +
                    "INITIATED_BY, OPERATION_DETAILS, TENANT_ID) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"})) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(operation);
                byte[] operationBytes = baos.toByteArray();
                stmt.setString(1, operation.getType().toString());
                stmt.setLong(2, DeviceManagementDAOUtil.getCurrentUTCTime());
                stmt.setLong(3, 0);
                stmt.setString(4, operation.getCode());
                stmt.setString(5, operation.getInitiatedBy());
                stmt.setBytes(6, operationBytes);
                stmt.setInt(7, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    int id = -1;
                    if (rs.next()) {
                        id = rs.getInt(1);
                    }
                    return id;
                }
            } catch (IOException e) {
                String msg = "Error when  converting operation id " + operation + " to input stream";
                log.error(msg, e);
                throw new OperationManagementDAOException(msg, e);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding command operation" + operation.getId();
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        }
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
        ConfigOperation configOperation = null;
        ByteArrayInputStream bais;
        ObjectInputStream ois;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT ID, ENABLED, OPERATION_DETAILS FROM DM_OPERATION WHERE ID = ? AND TYPE='CONFIG'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, operationId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        byte[] operationDetails = rs.getBytes("OPERATION_DETAILS");
                        bais = new ByteArrayInputStream(operationDetails);
                        ois = new ObjectInputStream(bais);
                        configOperation = (ConfigOperation) ois.readObject();
                        configOperation.setId(rs.getInt("ID"));
                        configOperation.setEnabled(rs.getBoolean("ENABLED"));
                    }
                }
            }
        } catch (IOException e) {
            String msg = "IO Error occurred while de serialize the policy operation " +
                    "object";
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        } catch (ClassNotFoundException e) {
            String msg = "Class not found error occurred while de serialize the policy " +
                    "operation object";
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while retrieving the policy operation " +
                    "object available for the id '"
                    + operationId;
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        }
        return configOperation;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(int enrolmentId, Operation.Status status)
            throws OperationManagementDAOException {
        ConfigOperation configOperation;
        List<Operation> operations = new ArrayList<>();
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT co.ID, co.OPERATION_DETAILS FROM DM_OPERATION co " +
                    "INNER JOIN (SELECT * FROM DM_ENROLMENT_OP_MAPPING WHERE ENROLMENT_ID = ? " +
                    "AND STATUS = ?) dm ON dm.OPERATION_ID = co.ID WHERE co.TYPE = 'CONFIG'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, enrolmentId);
                stmt.setString(2, status.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        byte[] operationDetails = rs.getBytes("OPERATION_DETAILS");
                        bais = new ByteArrayInputStream(operationDetails);
                        ois = new ObjectInputStream(bais);
                        configOperation = (ConfigOperation) ois.readObject();
                        configOperation.setStatus(status);
                        configOperation.setId(rs.getInt("ID"));
                        operations.add(configOperation);
                    }
                }
            }
        } catch (IOException e) {
            String msg = "IO Error occurred while de serialize the configuration " +
                    "operation object";
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        } catch (ClassNotFoundException e) {
            String msg = "Class not found error occurred while de serialize the " +
                    "configuration operation object";
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL error occurred while retrieving the operation available " +
                    "for the device'" + enrolmentId + "' with status '" + status.toString();
            log.error(msg, e);
            throw new OperationManagementDAOException(msg, e);
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
        }
        return operations;
    }

}
