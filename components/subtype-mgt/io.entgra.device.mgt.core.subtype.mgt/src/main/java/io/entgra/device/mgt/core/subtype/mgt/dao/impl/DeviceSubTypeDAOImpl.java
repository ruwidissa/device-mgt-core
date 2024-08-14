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

package io.entgra.device.mgt.core.subtype.mgt.dao.impl;

import io.entgra.device.mgt.core.subtype.mgt.dao.DeviceSubTypeDAO;
import io.entgra.device.mgt.core.subtype.mgt.dao.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.subtype.mgt.dao.util.DAOUtil;
import io.entgra.device.mgt.core.subtype.mgt.dto.DeviceSubType;
import io.entgra.device.mgt.core.subtype.mgt.exception.DBConnectionException;
import io.entgra.device.mgt.core.subtype.mgt.exception.SubTypeMgtDAOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


public class DeviceSubTypeDAOImpl implements DeviceSubTypeDAO {
    private static final Log log = LogFactory.getLog(DeviceSubTypeDAOImpl.class);

    @Override
    public boolean addDeviceSubType(DeviceSubType deviceSubType) throws SubTypeMgtDAOException {
        try {
            String sql = "INSERT INTO DM_DEVICE_SUB_TYPE (SUB_TYPE_ID, TENANT_ID, DEVICE_TYPE, SUB_TYPE_NAME, " +
                    "TYPE_DEFINITION) VALUES (?, ?, ?, ?, ?)";

            Connection conn = ConnectionManagerUtil.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, deviceSubType.getSubTypeId());
                stmt.setInt(2, deviceSubType.getTenantId());
                stmt.setString(3, deviceSubType.getDeviceType().toString());
                stmt.setString(4, deviceSubType.getSubTypeName());
                stmt.setString(5, deviceSubType.getTypeDefinition());
                return stmt.executeUpdate() > 0;
            }

        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to insert device sub type for " +
                    deviceSubType.getDeviceType() + " subtype & subtype Id: " + deviceSubType.getSubTypeId();
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to insert device sub type for " +
                    deviceSubType.getDeviceType() + " subtype & subtype Id: " + deviceSubType.getSubTypeId();
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        }
    }

    @Override
    public boolean updateDeviceSubType(String subTypeId, int tenantId, String deviceType,
                                       String subTypeName, String typeDefinition)
            throws SubTypeMgtDAOException {
        try {
            String sql = "UPDATE DM_DEVICE_SUB_TYPE SET TYPE_DEFINITION = ? , SUB_TYPE_NAME = ? WHERE SUB_TYPE_ID = ? "
                    + "AND TENANT_ID = ? AND DEVICE_TYPE = ?";

            Connection conn = ConnectionManagerUtil.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, typeDefinition);
                stmt.setString(2, subTypeName);
                stmt.setString(3, subTypeId);
                stmt.setInt(4, tenantId);
                stmt.setString(5, deviceType);
                return stmt.executeUpdate() > 0;
            }

        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to update device sub type for " +
                    deviceType + " subtype & subtype Id: " + subTypeId;
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to update device sub type for " +
                    deviceType + " subtype & subtype Id: " + subTypeId;
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        }
    }

    @Override
    public DeviceSubType getDeviceSubType(String subTypeId, int tenantId, String deviceType)
            throws SubTypeMgtDAOException {
        try {
            String sql = "SELECT s.*, o.OPERATION_CODE FROM DM_DEVICE_SUB_TYPE s " +
                    "LEFT JOIN SUB_OPERATION_TEMPLATE o ON s.SUB_TYPE_ID = o.SUB_TYPE_ID " +
                    "AND s.DEVICE_TYPE = o.DEVICE_TYPE " +
                    "WHERE s.SUB_TYPE_ID = ? AND s.TENANT_ID = ? AND s.DEVICE_TYPE = ?";

            Connection conn = ConnectionManagerUtil.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, subTypeId);
                stmt.setInt(2, tenantId);
                stmt.setString(3, deviceType);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<DeviceSubType> deviceSubTypes = DAOUtil.loadDeviceSubTypes(rs);
                    return (deviceSubTypes != null && !deviceSubTypes.isEmpty()) ? deviceSubTypes.get(0) : null;
                }
            }

        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to retrieve device subtype for " + deviceType
                    + " subtype & subtype Id: " + subTypeId;
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to retrieve device subtype for " + deviceType + " " +
                    "subtype & subtype Id: " + subTypeId;
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        }
    }

    @Override
    public List<DeviceSubType> getAllDeviceSubTypes(int tenantId, String deviceType)
            throws SubTypeMgtDAOException {
        try {
            String sql = "SELECT s.*, o.OPERATION_CODE FROM DM_DEVICE_SUB_TYPE s " +
                    "LEFT JOIN SUB_OPERATION_TEMPLATE o on s.SUB_TYPE_ID = o.SUB_TYPE_ID AND s.DEVICE_TYPE  = o.DEVICE_TYPE " +
                    "WHERE s.TENANT_ID = ? AND s.DEVICE_TYPE = ? ORDER BY " +
                    "s.SUB_TYPE_ID";

            Connection conn = ConnectionManagerUtil.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.setString(2, deviceType);
                try (ResultSet rs = stmt.executeQuery()) {
                    return DAOUtil.loadDeviceSubTypes(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to retrieve all device sub types for " +
                    deviceType + " subtypes";
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to retrieve all device sub types for " + deviceType + " "
                    + "subtypes";
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        }
    }

    @Override
    public int getDeviceSubTypeCount(String deviceType) throws SubTypeMgtDAOException {
        try {
            String sql = "SELECT COUNT(*) as SUB_TYPE_COUNT FROM DM_DEVICE_SUB_TYPE WHERE DEVICE_TYPE = ? ";

            Connection conn = ConnectionManagerUtil.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, deviceType);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("SUB_TYPE_COUNT");
                    }
                    return 0;
                }
            }

        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to retrieve device sub types count for " +
                    deviceType + " subtypes";
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to retrieve device sub types count for " + deviceType +
                    " subtypes";
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        }
    }

    @Override
    public boolean checkDeviceSubTypeExist(String subTypeId, int tenantId, String deviceType)
            throws SubTypeMgtDAOException {
        try {
            String sql = "SELECT * FROM DM_DEVICE_SUB_TYPE WHERE SUB_TYPE_ID = ? AND TENANT_ID = ? AND DEVICE_TYPE " +
                    "= ? ";

            Connection conn = ConnectionManagerUtil.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, subTypeId);
                stmt.setInt(2, tenantId);
                stmt.setString(3, deviceType);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }

        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to check device subtype exist for " + deviceType
                    + " subtype & subtype id: " + subTypeId;
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to check device subtype exist for " + deviceType + " " +
                    "subtype & subtype id: " + subTypeId;
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        }
    }

    @Override
    public DeviceSubType getDeviceSubTypeByProvider(String subTypeName, int tenantId,
                                                    String deviceType)
            throws SubTypeMgtDAOException {
        try {
            String sql = "SELECT * FROM DM_DEVICE_SUB_TYPE WHERE SUB_TYPE_NAME = ? AND TENANT_ID = ? AND DEVICE_TYPE " +
                    "= ? ";

            Connection conn = ConnectionManagerUtil.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, subTypeName);
                stmt.setInt(2, tenantId);
                stmt.setString(3, deviceType);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return DAOUtil.loadDeviceSubType(rs);
                    }
                    return null;
                }
            }

        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining DB connection to retrieve device subtype for " + deviceType
                    + " subtype & subtype name: " + subTypeName;
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to retrieve device subtype for " + deviceType + " " +
                    "subtype & subtype name: " + subTypeName;
            log.error(msg);
            throw new SubTypeMgtDAOException(msg, e);
        }
    }

}
