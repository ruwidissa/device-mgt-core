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
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProfileOperationDAOImpl extends GenericOperationDAOImpl {

    private static final Log log = LogFactory.getLog(ProfileOperationDAOImpl.class);

    public int addOperation(Operation operation) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ByteArrayOutputStream bao = null;
        ObjectOutputStream oos = null;
        try {
            operation.setCreatedTimeStamp(new Timestamp(DeviceManagementDAOUtil.getCurrentUTCTime()).toString());
            operation.setEnabled(true);
            Connection connection = OperationManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_OPERATION(TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, OPERATION_CODE, " +
                         "INITIATED_BY, OPERATION_DETAILS, ENABLED) VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, operation.getType().toString());
            stmt.setLong(2, DeviceManagementDAOUtil.getCurrentUTCTime());
            stmt.setLong(3, 0);
            stmt.setString(4, operation.getCode());
            stmt.setString(5, operation.getInitiatedBy());

            bao = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bao);
            oos.writeObject(operation.getPayLoad());

            stmt.setBytes(6, bao.toByteArray());
            stmt.setBoolean(7, operation.isEnabled());
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            int id = -1;
            if (rs.next()) {
                id = rs.getInt(1);
            }
            return id;
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while adding profile operation", e);
        } catch (IOException e) {
            throw new OperationManagementDAOException("Error occurred while serializing profile operation object", e);
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
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
    }

    public Operation getOperation(int id) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ProfileOperation profileOperation = null;

        ByteArrayInputStream bais;
        ObjectInputStream ois;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT po.ID, po.ENABLED, po.OPERATION_DETAILS, po.CREATED_TIMESTAMP, po.OPERATION_CODE " +
                    "FROM DM_OPERATION po WHERE po.ID=? AND po.TYPE='PROFILE'";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                byte[] operationDetails = rs.getBytes("OPERATION_DETAILS");
                int oppId = rs.getInt("ID");
                bais = new ByteArrayInputStream(operationDetails);
                ois = new ObjectInputStream(bais);
                Object obj = ois.readObject();
                if(obj instanceof String){
                    profileOperation = new ProfileOperation();
                    profileOperation.setCode(rs.getString("OPERATION_CODE"));
                    profileOperation.setId(oppId);
                    profileOperation.setCreatedTimeStamp(new Timestamp(rs.getLong("CREATED_TIMESTAMP") * 1000L).toString());
                    profileOperation.setId(oppId);
                    profileOperation.setPayLoad(obj);
                } else {
                    profileOperation = (ProfileOperation) obj;
                    profileOperation.setCode(rs.getString("OPERATION_CODE"));
                    profileOperation.setId(rs.getInt("ID"));
                    profileOperation.setCreatedTimeStamp(rs.getString("CREATED_TIMESTAMP"));
                }
            }
        } catch (IOException e) {
            throw new OperationManagementDAOException("IO Error occurred while de serialize the profile " +
                    "operation object", e);
        } catch (ClassNotFoundException e) {
            throw new OperationManagementDAOException("Class not found error occurred while de serialize the " +
                    "profile operation object", e);
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL Error occurred while retrieving the profile " +
                    "operation object " + "available for the id '" + id, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return profileOperation;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(int enrolmentId,
                                                                    Operation.Status status) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ProfileOperation profileOperation;

        List<Operation> operationList = new ArrayList<Operation>();

        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT po1.ID, po1.ENABLED, po1.STATUS, po1.TYPE, po1.CREATED_TIMESTAMP, po1.RECEIVED_TIMESTAMP, " +
                    "po1.OPERATION_CODE, po1.OPERATION_DETAILS " +
                    "FROM (SELECT po.ID, po.ENABLED, po.OPERATION_DETAILS, po.TYPE, po.OPERATION_CODE, po.CREATED_TIMESTAMP, po.RECEIVED_TIMESTAMP, dm.STATUS " +
                    "FROM DM_OPERATION po INNER JOIN (SELECT ENROLMENT_ID, OPERATION_ID, STATUS FROM DM_ENROLMENT_OP_MAPPING " +
                    "WHERE ENROLMENT_ID = ? AND STATUS = ?) dm ON dm.OPERATION_ID = po.ID WHERE po.TYPE='PROFILE') po1";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            stmt.setString(2, status.toString());

            rs = stmt.executeQuery();

            while (rs.next()) {
                byte[] operationDetails = rs.getBytes("OPERATION_DETAILS");
                bais = new ByteArrayInputStream(operationDetails);
                ois = new ObjectInputStream(bais);
                Object obj = ois.readObject();
                if(obj instanceof String){
                    profileOperation = new ProfileOperation();
                    profileOperation.setCode(rs.getString("OPERATION_CODE"));
                    profileOperation.setId(rs.getInt("ID"));
                    profileOperation.setCreatedTimeStamp(rs.getString("CREATED_TIMESTAMP"));
                    profileOperation.setPayLoad(obj);
                    profileOperation.setStatus(status);
                    operationList.add(profileOperation);
                } else {
                    profileOperation = (ProfileOperation) obj;
                    profileOperation.setStatus(status);
                    profileOperation.setCode(rs.getString("OPERATION_CODE"));
                    profileOperation.setId(rs.getInt("ID"));
                    profileOperation.setCreatedTimeStamp(rs.getString("CREATED_TIMESTAMP"));
                    operationList.add(profileOperation);
                }
            }

        } catch (IOException e) {
            throw new OperationManagementDAOException("IO Error occurred while de serialize the profile " +
                    "operation object", e);
        } catch (ClassNotFoundException e) {
            throw new OperationManagementDAOException("Class not found error occurred while de serialize the " +
                    "profile operation object", e);
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operation " +
                    "available for the device'" + enrolmentId + "' with status '" + status.toString(), e);
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
        return operationList;
    }

}
