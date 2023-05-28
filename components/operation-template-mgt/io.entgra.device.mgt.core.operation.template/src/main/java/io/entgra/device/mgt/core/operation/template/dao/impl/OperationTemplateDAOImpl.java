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

package io.entgra.device.mgt.core.operation.template.dao.impl;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.operation.template.dto.OperationTemplate;
import io.entgra.device.mgt.core.operation.template.dao.OperationTemplateDAO;
import io.entgra.device.mgt.core.operation.template.exception.DBConnectionException;
import io.entgra.device.mgt.core.operation.template.exception.OperationTemplateManagementDAOException;
import io.entgra.device.mgt.core.operation.template.util.ConnectionManagerUtils;
import io.entgra.device.mgt.core.operation.template.util.DAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation for generic DB engines.
 */
public class OperationTemplateDAOImpl implements OperationTemplateDAO {

    private static final Log log = LogFactory.getLog(OperationTemplateDAOImpl.class);

    /**
     * @param operationTemplate
     * @throws OperationTemplateManagementDAOException
     */
    @Override
    public void addOperationTemplate(OperationTemplate operationTemplate)
            throws OperationTemplateManagementDAOException {

        try {
            String sql =
                    "INSERT INTO SUB_OPERATION_TEMPLATE (" + "OPERATION_DEFINITION, " + "OPERATION_CODE, "
                            + "SUB_TYPE_ID, " + "DEVICE_TYPE, " + "CREATE_TIMESTAMP, " + "UPDATE_TIMESTAMP) "
                            + "VALUES (?, ?, ?, ?, ?, ?)";
            Connection conn = ConnectionManagerUtils.getDBConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                int index = 1;
                stmt.setObject(index++, new Gson().toJson(operationTemplate.getOperationDefinition()));
                stmt.setString(index++, operationTemplate.getCode());
                stmt.setString(index++, operationTemplate.getSubTypeId());
                stmt.setString(index++, operationTemplate.getDeviceType());
                stmt.setTimestamp(index++, new Timestamp(System.currentTimeMillis()));
                stmt.setTimestamp(index++, null);
                stmt.executeUpdate();
            } catch (SQLException e) {
                String msg = "Error occurred while processing insert operation template.";
                log.error(msg);
                throw new OperationTemplateManagementDAOException(msg, e);
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to insert operation template.";
            log.error(msg);
            throw new OperationTemplateManagementDAOException(msg, e);
        }
    }

    /**
     * @param operationTemplate
     * @return
     * @throws OperationTemplateManagementDAOException
     */
    @Override
    public OperationTemplate updateOperationTemplate(OperationTemplate operationTemplate)
            throws OperationTemplateManagementDAOException {

        try {
            String sql =
                    "UPDATE SUB_OPERATION_TEMPLATE SET OPERATION_DEFINITION = ?, UPDATE_TIMESTAMP = ? WHERE SUB_TYPE_ID = ? AND DEVICE_TYPE = ? "
                            + "AND OPERATION_CODE = ?";

            Connection conn = ConnectionManagerUtils.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, operationTemplate.getOperationDefinition());
                stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                stmt.setString(3, operationTemplate.getSubTypeId());
                stmt.setString(4, operationTemplate.getDeviceType());
                stmt.setString(5, operationTemplate.getCode());
                stmt.executeUpdate();

                return getOperationTemplate(operationTemplate.getSubTypeId(), operationTemplate.getDeviceType(), operationTemplate.getCode());
            } catch (SQLException e) {
                String msg = "Error occurred while processing update operation template.";
                log.error(msg);
                throw new OperationTemplateManagementDAOException(msg, e);
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to update operation template.";
            throw new OperationTemplateManagementDAOException(msg, e);
        }
    }

    /**
     * @param subTypeId
     * @param deviceType
     * @param operationCode
     * @return
     * @throws OperationTemplateManagementDAOException
     */
    @Override
    public OperationTemplate getOperationTemplate(String subTypeId, String deviceType, String operationCode)
            throws OperationTemplateManagementDAOException {
        try {
            String sql = "SELECT * FROM SUB_OPERATION_TEMPLATE WHERE SUB_TYPE_ID = ? AND DEVICE_TYPE = ? AND OPERATION_CODE = ?";
            Connection conn = ConnectionManagerUtils.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, subTypeId);
                stmt.setString(2, deviceType);
                stmt.setString(3, operationCode);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return DAOUtil.loadOperationTemplate(rs);
                    }
                    return null;
                }
            } catch (SQLException e) {
                String msg = "Error occurred while loading operation template.";
                log.error(e.getMessage());
                throw new OperationTemplateManagementDAOException(msg, e);
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to loading operation template.";
            log.error(msg);
            throw new OperationTemplateManagementDAOException(msg, e);
        }
    }

    /**
     * @param subTypeId
     * @param deviceType
     * @param operationCode
     * @throws OperationTemplateManagementDAOException
     */
    @Override
    public void deleteOperationTemplate(String subTypeId, String deviceType, String operationCode)
            throws OperationTemplateManagementDAOException {
        String sql = "DELETE FROM SUB_OPERATION_TEMPLATE WHERE SUB_TYPE_ID = ? AND DEVICE_TYPE = ? AND OPERATION_CODE = ?";
        try {
            Connection conn = ConnectionManagerUtils.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, subTypeId);
                stmt.setString(2, deviceType);
                stmt.setString(3, operationCode);
                stmt.executeUpdate();
            } catch (SQLException e) {
                String msg = "Error occurred while deleting operation template for sub type id : " + subTypeId
                        + " and operation code : " + operationCode;
                log.error(msg, e);
                throw new OperationTemplateManagementDAOException(msg, e);
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to delete operation template.";
            log.error(msg);
            throw new OperationTemplateManagementDAOException(msg, e);
        }
    }
}
