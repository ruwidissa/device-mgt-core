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

package io.entgra.device.mgt.core.device.mgt.core.dao.impl;

import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo.Status;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.DevicePropertyInfo;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceData;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceLocationHistorySnapshot;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceMonitoringData;
import io.entgra.device.mgt.core.device.mgt.common.geo.service.GeoQuery;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceType;
import io.entgra.device.mgt.core.device.mgt.common.geo.service.GeoCluster;
import io.entgra.device.mgt.core.device.mgt.common.geo.service.GeoCoordinate;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Random;
import java.util.stream.Collectors;

public abstract class AbstractDeviceDAOImpl implements DeviceDAO {

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(AbstractDeviceDAOImpl.class);

    private static final String PROPERTY_KEY_COLUMN_NAME = "PROPERTY_NAME";
    private static final String PROPERTY_VALUE_COLUMN_NAME = "PROPERTY_VALUE";
    private static final String PROPERTY_DEVICE_TYPE_NAME = "DEVICE_TYPE_NAME";
    private static final String PROPERTY_DEVICE_IDENTIFICATION = "DEVICE_IDENTIFICATION";
    private static final String PROPERTY_TENANT_ID = "TENANT_ID";

    @Override
    public int addDevice(int typeId, Device device, int tenantId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int deviceId = -1;
        try {
            Connection conn = this.getConnection();
            String sql = "INSERT INTO DM_DEVICE(DESCRIPTION, NAME, DEVICE_TYPE_ID, DEVICE_IDENTIFICATION, " +
                    "LAST_UPDATED_TIMESTAMP, TENANT_ID) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, device.getDescription());
            stmt.setString(2, device.getName());
            stmt.setInt(3, typeId);
            stmt.setString(4, device.getDeviceIdentifier());
            stmt.setTimestamp(5, new Timestamp(new Date().getTime()));
            stmt.setInt(6, tenantId);
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                deviceId = rs.getInt(1);
            }
            return deviceId;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while enrolling device '" + device.getName() +
                    "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public boolean updateDevice(Device device, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int rows;
        try {
            conn = this.getConnection();
            String sql = "UPDATE DM_DEVICE SET NAME = ?, DESCRIPTION = ?, LAST_UPDATED_TIMESTAMP = ? " +
                    "WHERE DEVICE_TYPE_ID = (SELECT ID FROM DM_DEVICE_TYPE " +
                    "WHERE NAME = ? AND (PROVIDER_TENANT_ID = ? OR SHARED_WITH_ALL_TENANTS = ?)) " +
                    "AND DEVICE_IDENTIFICATION = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, device.getName());
            stmt.setString(2, device.getDescription());
            stmt.setTimestamp(3, new Timestamp(new Date().getTime()));
            stmt.setString(4, device.getType());
            stmt.setInt(5, tenantId);
            stmt.setBoolean(6, true);
            stmt.setString(7, device.getDeviceIdentifier());
            stmt.setInt(8, tenantId);
            rows = stmt.executeUpdate();
            return (rows > 0);
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while enrolling device '" +
                    device.getName() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public boolean recordDeviceUpdate(DeviceIdentifier deviceIdentifier, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int rows;
        try {
            conn = this.getConnection();
            String sql = "UPDATE DM_DEVICE SET LAST_UPDATED_TIMESTAMP = ? " +
                    "WHERE DEVICE_IDENTIFICATION = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, new Timestamp(new Date().getTime()));
            stmt.setString(2, deviceIdentifier.getId());
            stmt.setInt(3, tenantId);
            rows = stmt.executeUpdate();
            return (rows > 0);
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while updating device update timestamp of " +
                    " device '" + deviceIdentifier + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public Device getDevice(DeviceData deviceData, int tenantId) throws DeviceManagementDAOException {
        Device device = null;
        try {
            Connection conn = this.getConnection();
            String sql = "SELECT "
                    + "d1.ID AS DEVICE_ID, "
                    + "d1.DESCRIPTION, "
                    + "d1.NAME AS DEVICE_NAME, "
                    + "d1.LAST_UPDATED_TIMESTAMP, "
                    + "e.DEVICE_TYPE, "
                    + "e.DEVICE_IDENTIFICATION, "
                    + "e.OWNER, "
                    + "e.OWNERSHIP, "
                    + "e.STATUS, "
                    + "e.IS_TRANSFERRED, "
                    + "e.DATE_OF_LAST_UPDATE, "
                    + "e.DATE_OF_ENROLMENT, "
                    + "e.ID AS ENROLMENT_ID "
                    + "FROM DM_ENROLMENT e, "
                    + "(SELECT "
                    + "d.ID, "
                    + "d.DESCRIPTION, "
                    + "d.NAME, "
                    + "d.LAST_UPDATED_TIMESTAMP "
                    + "FROM DM_DEVICE d WHERE "
                    + "d.DEVICE_IDENTIFICATION = ? AND "
                    + "d.TENANT_ID = ?";

            if (deviceData.getLastModifiedDate() != null) {
                sql += " AND d.LAST_UPDATED_TIMESTAMP > ?";
            }

            sql += ") d1 WHERE d1.ID = e.DEVICE_ID AND ";

            if (!StringUtils.isBlank(deviceData.getDeviceOwner())) {
                sql += "e.OWNER = ? AND ";
            }
            if (!StringUtils.isBlank(deviceData.getDeviceOwnership())) {
                sql += "e.OWNERSHIP = ? AND ";
            }

            sql += "TENANT_ID = ? ORDER BY e.DATE_OF_LAST_UPDATE DESC";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIndx = 1;
                stmt.setString(paramIndx++, deviceData.getDeviceIdentifier().getId());
                stmt.setInt(paramIndx++, tenantId);
                if (deviceData.getLastModifiedDate() != null) {
                    stmt.setTimestamp(paramIndx++, new Timestamp(deviceData.getLastModifiedDate().getTime()));
                }
                if (!StringUtils.isBlank(deviceData.getDeviceOwner())) {
                    stmt.setString(paramIndx++, deviceData.getDeviceOwner());
                }
                if (!StringUtils.isBlank(deviceData.getDeviceOwnership())) {
                    stmt.setString(paramIndx++, deviceData.getDeviceOwnership());
                }
                stmt.setInt(paramIndx, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        device = DeviceManagementDAOUtil.loadMatchingDevice(rs, false);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while getting device data for device identifier "
                    + deviceData.getDeviceIdentifier().getId() + " and device type "
                    + deviceData.getDeviceIdentifier().getType();
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return device;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceIdentifier, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Device device = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, " +
                    "d1.LAST_UPDATED_TIMESTAMP, e.DEVICE_TYPE, " +
                    "e.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, " +
                    "(SELECT d.ID, d.DESCRIPTION, d.NAME, d.LAST_UPDATED_TIMESTAMP " +
                    "FROM DM_DEVICE d WHERE " +
                    "d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ?) d1 WHERE d1.ID = e.DEVICE_ID " +
                    "AND e.DEVICE_TYPE = ? AND TENANT_ID = ? ORDER BY e.DATE_OF_LAST_UPDATE DESC, e.STATUS ASC";
            // Status added as an order-by clause to fix a bug : when an existing device is
            // re-enrolled, earlier enrollment is marked as removed and a new enrollment is added.
            // However, both enrollments share the same time stamp. When retrieving the device
            // due to same timestamp, enrollment information is incorrect, intermittently. Hence,
            // status also should be taken into consideration when ordering. This should not present a
            // problem for other status transitions, as there would be an intermediary removed
            // state in between.
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceIdentifier.getId());
            stmt.setInt(2, tenantId);
            stmt.setString(3, deviceIdentifier.getType());
            stmt.setInt(4, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                device = DeviceManagementDAOUtil.loadMatchingDevice(rs, false);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while listing devices for type " +
                    "'" + deviceIdentifier.getType() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return device;
    }

    @Override
    public Device getDevice(String deviceIdentifier, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Device device = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT " +
                    "d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.LAST_UPDATED_TIMESTAMP, " +
                    "e.DEVICE_TYPE, e.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, " +
                    "e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID " +
                    "FROM " +
                    "DM_ENROLMENT e," +
                    " (SELECT d.ID, d.DESCRIPTION, d.NAME, d.LAST_UPDATED_TIMESTAMP " +
                    "FROM DM_DEVICE d " +
                    "WHERE d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ?) d1 " +
                    "WHERE " +
                    "d1.ID = e.DEVICE_ID " +
                    "AND TENANT_ID = ? " +
                    "ORDER BY e.DATE_OF_LAST_UPDATE DESC, e.STATUS ASC";
            // Status adeed as an orderby clause to fix a bug : when an existing device is
            // re-enrolled, earlier enrollment is marked as removed and a new enrollment is added.
            // However, both enrollments share the same time stamp. When retrieving the device
            // due to same timestamp, enrollment information is incorrect, intermittently. Hence
            // status also should be taken into consideration when ordering. This should not present a
            // problem for other status transitions, as there would be an intermediary removed
            // state in between.
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceIdentifier);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                device = DeviceManagementDAOUtil.loadMatchingDevice(rs, false);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while listing device " +
                    "'" + deviceIdentifier + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return device;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceIdentifier, String owner, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Device device = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, " +
                    "d1.LAST_UPDATED_TIMESTAMP, e.DEVICE_TYPE, e.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, " +
                    "e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, " +
                    "(SELECT d.ID, d.DESCRIPTION, d.NAME, d.LAST_UPDATED_TIMESTAMP " +
                    "FROM DM_DEVICE d WHERE d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ?) d1 " +
                    "WHERE e.DEVICE_TYPE = ? AND d1.ID = e.DEVICE_ID " +
                    "AND TENANT_ID = ? AND e.OWNER = ? ORDER BY e.DATE_OF_LAST_UPDATE DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceIdentifier.getId());
            stmt.setInt(2, tenantId);
            stmt.setString(3, deviceIdentifier.getType());
            stmt.setInt(4, tenantId);
            stmt.setString(5, owner);
            rs = stmt.executeQuery();
            if (rs.next()) {
                device = DeviceManagementDAOUtil.loadMatchingDevice(rs, false);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while listing devices for type " +
                    "'" + deviceIdentifier.getType() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return device;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceIdentifier, Date since, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Device device = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, " +
                    "d1.LAST_UPDATED_TIMESTAMP, e.DEVICE_TYPE, e.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, " +
                    "e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, " +
                    "(SELECT d.ID, d.DESCRIPTION, d.NAME, d.LAST_UPDATED_TIMESTAMP " +
                    "FROM DM_DEVICE d WHERE d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ? " +
                    "AND d.LAST_UPDATED_TIMESTAMP > ?) d1 WHERE e.DEVICE_TYPE = ? " +
                    "AND d1.ID = e.DEVICE_ID AND TENANT_ID = ? ORDER BY e.DATE_OF_LAST_UPDATE DESC";
            stmt = conn.prepareStatement(sql);
            int paramIdx = 1;
            stmt.setString(paramIdx++, deviceIdentifier.getId());
            stmt.setInt(paramIdx++, tenantId);
            stmt.setTimestamp(paramIdx++, new Timestamp(since.getTime()));
            stmt.setString(paramIdx++, deviceIdentifier.getType());
            stmt.setInt(paramIdx, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                device = DeviceManagementDAOUtil.loadMatchingDevice(rs, false);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while listing device for type " +
                    "'" + deviceIdentifier.getType() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return device;
    }

    @Override
    public List<Device> getDeviceBasedOnDeviceProperties(Map<String, String> deviceProps, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Device> devices = new ArrayList<>();
        if (deviceProps.isEmpty()) {
            return devices;
        }
        try {
            List<List<String>> outputLists = new ArrayList<>();
            List<String> deviceList = null;
            conn = this.getConnection();
            for (Map.Entry<String, String> entry : deviceProps.entrySet()) {

                stmt = conn.prepareStatement("SELECT DEVICE_IDENTIFICATION FROM DM_DEVICE_PROPERTIES " +
                        "WHERE (PROPERTY_NAME , PROPERTY_VALUE) IN " +
                        "((? , ?))  AND TENANT_ID = ?");
                stmt.setString(1, entry.getKey());
                stmt.setString(2, entry.getValue());
                stmt.setInt(3, tenantId);
                resultSet = stmt.executeQuery();

                deviceList = new ArrayList<>();
                while (resultSet.next()) {
                    deviceList.add(resultSet.getString(PROPERTY_DEVICE_IDENTIFICATION));
                }
                outputLists.add(deviceList);
            }
            List<String> deviceIds = findIntersection(outputLists);
            for (String deviceId : deviceIds) {
                devices.add(getDeviceProps(deviceId, tenantId));
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching devices against criteria : '" + deviceProps;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return devices;
    }

    @Override
    public List<DevicePropertyInfo> getDeviceBasedOnDeviceProperties(Map<String, String> deviceProps)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DevicePropertyInfo> deviceProperties = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT " +
                    "p.DEVICE_IDENTIFICATION, " +
                    "p.DEVICE_TYPE_NAME, " +
                    "p.TENANT_ID FROM " +
                    "DM_DEVICE_PROPERTIES p ";

            String groupByClause = "GROUP BY " +
                    "p.DEVICE_IDENTIFICATION, " +
                    "p.DEVICE_TYPE_NAME, " +
                    "p.TENANT_ID";

            int iterationCount = 0;
            StringBuilder propertyQuery = new StringBuilder(" ");
            for (Map.Entry<String, String> ignored : deviceProps.entrySet()) {
                String tempTableId = "t".concat(Integer.toString(iterationCount++));
                propertyQuery.append("JOIN DM_DEVICE_PROPERTIES ")
                        .append(tempTableId).append(" ").append("ON p.DEVICE_IDENTIFICATION = ")
                        .append(tempTableId).append(".DEVICE_IDENTIFICATION ")
                        .append("AND ")
                        .append(tempTableId).append(".PROPERTY_NAME = ? ")
                        .append("AND ")
                        .append(tempTableId).append(".PROPERTY_VALUE = ? ");
            }
            sql = sql.concat(propertyQuery.toString()).concat(groupByClause);
            stmt = conn.prepareStatement(sql);
            int index = 1;
            for (Map.Entry<String, String> entry : deviceProps.entrySet()) {
                stmt.setString(index++, entry.getKey());
                stmt.setString(index++, entry.getValue());
            }
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                DevicePropertyInfo devicePropertyInfo = new DevicePropertyInfo();
                devicePropertyInfo
                        .setDeviceIdentifier(resultSet.getString(PROPERTY_DEVICE_IDENTIFICATION));
                devicePropertyInfo.setTenantId(resultSet.getString(PROPERTY_TENANT_ID));
                devicePropertyInfo.setDeviceTypeName(resultSet.getString(PROPERTY_DEVICE_TYPE_NAME));
                deviceProperties.add(devicePropertyInfo);
            }
            return deviceProperties;
        } catch (SQLException e) {
            String msg = "Error occurred while fetching devices against criteria : '" + deviceProps;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public Device getDeviceProps(String deviceId, int tenantId) throws DeviceManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        Device device = null;
        ResultSet resultSet = null;
        String deviceType = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(
                    "SELECT * FROM DM_DEVICE_PROPERTIES WHERE DEVICE_IDENTIFICATION = ? AND TENANT_ID = ?");
            stmt.setString(1, deviceId);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            List<Device.Property> properties = new ArrayList<>();
            while (resultSet.next()) {
                Device.Property property = new Device.Property();
                property.setName(resultSet.getString(PROPERTY_KEY_COLUMN_NAME));
                property.setValue(resultSet.getString(PROPERTY_VALUE_COLUMN_NAME));
                properties.add(property);
                //We are repeatedly assigning device type here. Yes. This was done intentionally, as there would be
                //No other efficient/simple/inexpensive way of retrieving device type of a particular device from the database
                //Note that device-identification will be unique across device types
                deviceType = resultSet.getString(PROPERTY_DEVICE_TYPE_NAME);
            }
            device = new Device();
            device.setDeviceIdentifier(deviceId);
            device.setType(deviceType);
            device.setProperties(properties);

        } catch (SQLException e) {
            String msg = "Error occurred while fetching properties for device : '" + deviceId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, resultSet);
        }

        return device;
    }

    private List<String> findIntersection(List<List<String>> collections) {
        boolean first = true;
        List<String> intersectedResult = new ArrayList<>();
        for (Collection<String> collection : collections) {
            if (first) {
                intersectedResult.addAll(collection);
                first = false;
            } else {
                intersectedResult.retainAll(collection);
            }
        }
        return intersectedResult;
    }

    @Override
    public Device getDevice(String deviceIdentifier, Date since, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Device device = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, " +
                    "d1.LAST_UPDATED_TIMESTAMP, e.DEVICE_TYPE, e.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, " +
                    "e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID " +
                    "FROM" +
                    " DM_ENROLMENT e, " +
                    "(SELECT d.ID, d.DESCRIPTION, d.NAME, d.LAST_UPDATED_TIMESTAMP " +
                    "FROM" +
                    " DM_DEVICE d " +
                    "WHERE " +
                    "d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ? AND" +
                    " d.LAST_UPDATED_TIMESTAMP > ?) d1 " +
                    "WHERE" +
                    " d1.ID = e.DEVICE_ID AND TENANT_ID = ? " +
                    "ORDER BY " +
                    "e.DATE_OF_LAST_UPDATE DESC";
            stmt = conn.prepareStatement(sql);
            int paramIdx = 1;
            stmt.setString(paramIdx++, deviceIdentifier);
            stmt.setInt(paramIdx++, tenantId);
            stmt.setTimestamp(paramIdx++, new Timestamp(since.getTime()));
            stmt.setInt(paramIdx, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                device = DeviceManagementDAOUtil.loadMatchingDevice(rs, false);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while listing device for id " +
                    "'" + deviceIdentifier + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return device;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceIdentifier, String owner, Date since, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Device device = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, " +
                    "d1.LAST_UPDATED_TIMESTAMP, e.DEVICE_TYPE, e.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, " +
                    "e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, " +
                    "(SELECT d.ID, d.DESCRIPTION, d.NAME, d.LAST_UPDATED_TIMESTAMP " +
                    "FROM DM_DEVICE d WHERE " +
                    "d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ? " +
                    "AND d.LAST_UPDATED_TIMESTAMP > ?) d1 WHERE d1.ID = e.DEVICE_ID AND TENANT_ID = ? AND e.OWNER = ? " +
                    "AND e.DEVICE_TYPE = ? ORDER BY e.DATE_OF_LAST_UPDATE DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceIdentifier.getId());
            stmt.setInt(2, tenantId);
            stmt.setTimestamp(3, new Timestamp(since.getTime()));
            stmt.setInt(4, tenantId);
            stmt.setString(5, owner);
            stmt.setString(6, deviceIdentifier.getType());
            rs = stmt.executeQuery();
            if (rs.next()) {
                device = DeviceManagementDAOUtil.loadMatchingDevice(rs, false);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while listing device for type " +
                    "'" + deviceIdentifier.getType() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return device;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceIdentifier, EnrolmentInfo.Status status, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Device device = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, " +
                    "d1.LAST_UPDATED_TIMESTAMP, e.DEVICE_TYPE, e.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, " +
                    "e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, " +
                    "(SELECT d.ID, d.DESCRIPTION, d.NAME, d.LAST_UPDATED_TIMESTAMP " +
                    "FROM DM_DEVICE d WHERE " +
                    "d.TENANT_ID = ?) d1 WHERE d1.ID = e.DEVICE_ID AND e.DEVICE_TYPE = ? AND e.DEVICE_IDENTIFICATION = ? " +
                    "AND TENANT_ID = ? AND e.STATUS = ? ORDER BY e.DATE_OF_LAST_UPDATE DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, deviceIdentifier.getType());
            stmt.setString(3, deviceIdentifier.getId());
            stmt.setInt(4, tenantId);
            stmt.setString(5, status.toString());
            rs = stmt.executeQuery();
            if (rs.next()) {
                device = DeviceManagementDAOUtil.loadDevice(rs);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while listing devices for type " +
                    "'" + deviceIdentifier.getType() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return device;
    }

    @Override
    public SingletonMap getDevice(DeviceIdentifier deviceIdentifier)
            throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, " +
                    "d1.DESCRIPTION, " +
                    "d1.NAME AS DEVICE_NAME, " +
                    "d1.LAST_UPDATED_TIMESTAMP, " +
                    "e.DEVICE_TYPE, " +
                    "e.TENANT_ID, " +
                    "e.DEVICE_IDENTIFICATION, " +
                    "e.OWNER, " +
                    "e.OWNERSHIP, " +
                    "e.STATUS, " +
                    "e.IS_TRANSFERRED, " +
                    "e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, " +
                    "e.ID AS ENROLMENT_ID " +
                    "FROM DM_ENROLMENT e, " +
                    "(SELECT d.ID, " +
                    "d.DESCRIPTION, " +
                    "d.NAME, " +
                    "d.DEVICE_IDENTIFICATION, " +
                    "d.LAST_UPDATED_TIMESTAMP " +
                    "FROM DM_DEVICE d " +
                    "WHERE d.DEVICE_IDENTIFICATION = ?) d1 " +
                    "WHERE d1.ID = e.DEVICE_ID AND e.DEVICE_TYPE = ? " +
                    "ORDER BY e.DATE_OF_LAST_UPDATE DESC";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, deviceIdentifier.getId());
                stmt.setString(2, deviceIdentifier.getType());

                try (ResultSet rs = stmt.executeQuery()) {
                    SingletonMap deviceMap = null;
                    if (rs.next()) {
                        deviceMap = new SingletonMap(
                                rs.getInt("TENANT_ID"),
                                DeviceManagementDAOUtil.loadDevice(rs)
                        );
                    }
                    return deviceMap;
                }
            }

        } catch (SQLException e) {
            String msg = "Error occurred while listing devices (with tenant id) for type " +
                    deviceIdentifier.getType();
            log.error(msg);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public Device getDevice(int deviceId, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Device device = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, " +
                    "d1.LAST_UPDATED_TIMESTAMP, e.DEVICE_TYPE, e.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, " +
                    "e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT, " +
                    "e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, " +
                    "(SELECT d.ID, d.DESCRIPTION, d.NAME, d.LAST_UPDATED_TIMESTAMP " +
                    "FROM DM_DEVICE d WHERE d.ID = ? AND d.TENANT_ID = ?) d1 WHERE d1.ID = e.DEVICE_ID " +
                    "AND TENANT_ID = ? ORDER BY e.DATE_OF_LAST_UPDATE DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                device = DeviceManagementDAOUtil.loadDevice(rs);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving device for id " +
                    "'" + deviceId + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return device;
    }

    @Override
    public List<Device> getDevices(int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices;
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.LAST_UPDATED_TIMESTAMP, " +
                    "e.DEVICE_TYPE, e.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, " +
                    "e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, " +
                    "(SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME, d.LAST_UPDATED_TIMESTAMP FROM DM_DEVICE d " +
                    "WHERE d.TENANT_ID = ?) d1 " +
                    "WHERE d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ? ORDER BY e.DATE_OF_LAST_UPDATE DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            devices = new ArrayList<>();
            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving information of all " +
                    "registered devices", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devices;
    }

    @Override
    public List<Device> getDevices(String type, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.LAST_UPDATED_TIMESTAMP, " +
                    "e.DEVICE_TYPE, e.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, " +
                    "e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, " +
                    "(SELECT d.ID, d.DESCRIPTION, d.NAME, d.LAST_UPDATED_TIMESTAMP FROM DM_DEVICE d " +
                    "WHERE d.TENANT_ID = ?) d1 WHERE e.DEVICE_TYPE = ? AND d1.ID = e.DEVICE_ID " +
                    "AND TENANT_ID = ? ORDER BY e.DATE_OF_LAST_UPDATE DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, type);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            devices = new ArrayList<>();
            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadActiveDevice(rs, false);
                devices.add(device);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while listing devices for type '" + type + "'";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devices;
    }


    @Override
    public List<Device> getAllocatedDevices(String type, int tenantId, int activeServerCount,
                                            int serverIndex) throws DeviceManagementDAOException {
        List<Device> devices;
        try {
            Connection conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID," +
                    "    d1.DESCRIPTION," +
                    "    d1.NAME AS DEVICE_NAME," +
                    "    d1.LAST_UPDATED_TIMESTAMP, " +
                    "    e.DEVICE_TYPE," +
                    "    e.DEVICE_IDENTIFICATION," +
                    "    e.OWNER," +
                    "    e.OWNERSHIP," +
                    "    e.STATUS," +
                    "    e.IS_TRANSFERRED," +
                    "    e.DATE_OF_LAST_UPDATE," +
                    "    e.DATE_OF_ENROLMENT," +
                    "    e.ID AS ENROLMENT_ID " +
                    "FROM DM_ENROLMENT e," +
                    "    (SELECT d.ID," +
                    "            d.DESCRIPTION," +
                    "            d.NAME," +
                    "            d.LAST_UPDATED_TIMESTAMP" +
                    "    FROM DM_DEVICE d" +
                    "    WHERE d.TENANT_ID = ?) d1 " +
                    "WHERE d1.ID = e.DEVICE_ID" +
                    "        AND e.DEVICE_TYPE = ?" +
                    "        AND e.TENANT_ID = ?" +
                    "        AND MOD(d1.ID, ?) = ? " +
                    "ORDER BY e.DATE_OF_LAST_UPDATE DESC";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.setString(2, type);
                stmt.setInt(3, tenantId);
                stmt.setInt(4, activeServerCount);
                stmt.setInt(5, serverIndex);
                devices = new ArrayList<>();

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Device device = DeviceManagementDAOUtil.loadActiveDevice(rs, false);
                        devices.add(device);
                    }
                } catch (Exception e) {
                    String msg = "Error encountered while populating allocated active devices for server with index : " + serverIndex +
                            " active-server-count " + activeServerCount + " device-type " + type + " tenant-id " + tenantId;
                    log.error(msg, e);
                    throw new DeviceManagementDAOException(msg, e);
                }
            }
        } catch (SQLException e) {
            String msg = "Error encountered while retrieving allocated devices for server with index : " + serverIndex +
                    " active-server-count " + activeServerCount + " device-type " + type + " tenant-id " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return devices;
    }

    @Override
    public List<Device> getDevicesOfUser(String username, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT e1.OWNER, e1.OWNERSHIP, e1.ENROLMENT_ID, e1.DEVICE_ID, e1.STATUS, e1.IS_TRANSFERRED, " +
                    "e1.DATE_OF_LAST_UPDATE, e1.DATE_OF_ENROLMENT, d.DESCRIPTION, d.NAME AS DEVICE_NAME, " +
                    "d.DEVICE_IDENTIFICATION, d.LAST_UPDATED_TIMESTAMP, e1.DEVICE_TYPE FROM DM_DEVICE d, " +
                    "(SELECT e.OWNER, e.OWNERSHIP, e.ID AS ENROLMENT_ID, e.DEVICE_TYPE, " +
                    "e.DEVICE_ID, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT " +
                    "FROM DM_ENROLMENT e WHERE e.TENANT_ID = ? AND LOWER(e.OWNER) = LOWER(?)) e1 " +
                    "WHERE d.ID = e1.DEVICE_ID ORDER BY e1.DATE_OF_LAST_UPDATE DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, username);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices belongs to '" +
                    username + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devices;
    }


    @Override
    public List<Device> getDevicesOfUser(String username, String type, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT e1.OWNER, e1.OWNERSHIP, e1.ENROLMENT_ID, e1.DEVICE_ID, e1.STATUS, e1.IS_TRANSFERRED, " +
                    "e1.DATE_OF_LAST_UPDATE, e1.DATE_OF_ENROLMENT, d.DESCRIPTION, d.NAME AS DEVICE_NAME, " +
                    "d.DEVICE_IDENTIFICATION, d.LAST_UPDATED_TIMESTAMP, e1.DEVICE_TYPE FROM DM_DEVICE d, " +
                    "(SELECT e.OWNER, e.DEVICE_TYPE, e.OWNERSHIP, e.ID AS ENROLMENT_ID, e.DEVICE_ID, e.STATUS, " +
                    "e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT FROM DM_ENROLMENT e WHERE " +
                    "e.TENANT_ID = ? AND LOWER(e.OWNER) = LOWER(?) ORDER BY e.DATE_OF_LAST_UPDATE DESC) e1 " +
                    "WHERE d.ID = e1.DEVICE_ID AND e1.DEVICE_TYPE = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, username);
            stmt.setString(3, type);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices belongs to '" +
                    username + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devices;
    }

    @Override
    public List<Device> getDevicesOfUser(String username, int tenantId, List<String> deviceStatuses)
            throws DeviceManagementDAOException {
        List<Device> devices = new ArrayList<>();
        try {
            if (deviceStatuses.isEmpty()) {
                return devices;
            }
            Connection conn = this.getConnection();
            StringJoiner joiner = new StringJoiner(",", "SELECT "
                    + "e1.OWNER, "
                    + "e1.OWNERSHIP, "
                    + "e1.ENROLMENT_ID, "
                    + "e1.DEVICE_ID, "
                    + "e1.STATUS, "
                    + "e1.IS_TRANSFERRED, "
                    + "e1.DATE_OF_LAST_UPDATE, "
                    + "e1.DATE_OF_ENROLMENT, "
                    + "d.DESCRIPTION, "
                    + "d.NAME AS DEVICE_NAME, "
                    + "d.DEVICE_IDENTIFICATION, "
                    + "d.LAST_UPDATED_TIMESTAMP, "
                    + "e1.DEVICE_TYPE "
                    + "FROM "
                    + "DM_DEVICE d, "
                    + "(SELECT "
                    + "e.OWNER, "
                    + "e.DEVICE_TYPE, "
                    + "e.OWNERSHIP, "
                    + "e.ID AS ENROLMENT_ID, "
                    + "e.DEVICE_ID, "
                    + "e.STATUS, "
                    + "e.IS_TRANSFERRED, "
                    + "e.DATE_OF_LAST_UPDATE, "
                    + "e.DATE_OF_ENROLMENT "
                    + "FROM "
                    + "DM_ENROLMENT e "
                    + "WHERE "
                    + "e.TENANT_ID = ? AND "
                    + "LOWER(e.OWNER) = LOWER(?) AND "
                    + "e.STATUS IN (",
                    ")) e1 WHERE d.ID = e1.DEVICE_ID ORDER BY e1.DATE_OF_LAST_UPDATE DESC");

            deviceStatuses.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                int index = 1;
                stmt.setInt(index++, tenantId);
                stmt.setString(index++, username);
                for (String status : deviceStatuses) {
                    stmt.setObject(index++, status);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Device device = DeviceManagementDAOUtil.loadDevice(rs);
                        devices.add(device);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching the list of devices belongs to '" + username + "'";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return devices;
    }

    @Override
    public int getCountOfDevicesInGroup(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        int deviceCount = 0;
        int groupId = request.getGroupId();
        String deviceType = request.getDeviceType();
        boolean isDeviceTypeProvided = false;
        String deviceName = request.getDeviceName();
        boolean isDeviceNameProvided = false;
        String owner = request.getOwner();
        boolean isOwnerProvided = false;
        String ownerPattern = request.getOwnerPattern();
        boolean isOwnerPatternProvided = false;
        String ownership = request.getOwnership();
        boolean isOwnershipProvided = false;
        List<String> statusList = request.getStatusList();
        boolean isStatusProvided = false;
        Date since = request.getSince();
        boolean isSinceProvided = false;

        try {
            Connection conn = getConnection();
            String sql = "SELECT COUNT(d1.DEVICE_ID) AS DEVICE_COUNT " +
                    "FROM DM_ENROLMENT e, " +
                    "(SELECT gd.DEVICE_ID, " +
                    "gd.DESCRIPTION, " +
                    "gd.NAME, " +
                    "gd.DEVICE_IDENTIFICATION " +
                    "FROM " +
                    "(SELECT d.ID AS DEVICE_ID, " +
                    "d.DESCRIPTION,  " +
                    "d.NAME, " +
                    "d.DEVICE_IDENTIFICATION " +
                    "FROM DM_DEVICE d, " +
                    "(SELECT dgm.DEVICE_ID " +
                    "FROM DM_DEVICE_GROUP_MAP dgm " +
                    "WHERE  dgm.GROUP_ID = ?) dgm1 " +
                    "WHERE d.ID = dgm1.DEVICE_ID " +
                    "AND d.TENANT_ID = ?";
            //Add the query for device-name
            if (deviceName != null && !deviceName.isEmpty()) {
                sql = sql + " AND d.NAME LIKE ?";
                isDeviceNameProvided = true;
            }
            sql = sql + ") gd WHERE 1=1";
            //Add query for last updated timestamp
            if (since != null) {
                sql = sql + " AND d.LAST_UPDATED_TIMESTAMP > ?";
                isSinceProvided = true;
            }
            sql = sql + " ) d1 WHERE  d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ? ";
            //Add the query for device-type
            if (deviceType != null && !deviceType.isEmpty()) {
                sql = sql + " AND e.DEVICE_TYPE = ?";
                isDeviceTypeProvided = true;
            }
            //Add the query for ownership
            if (ownership != null && !ownership.isEmpty()) {
                sql = sql + " AND e.OWNERSHIP = ?";
                isOwnershipProvided = true;
            }
            //Add the query for owner
            if (owner != null && !owner.isEmpty()) {
                sql = sql + " AND e.OWNER = ?";
                isOwnerProvided = true;
            } else if (ownerPattern != null && !ownerPattern.isEmpty()) {
                sql = sql + " AND e.OWNER LIKE ?";
                isOwnerPatternProvided = true;
            }
            if (statusList != null && !statusList.isEmpty()) {
                sql += buildStatusQuery(statusList);
                isStatusProvided = true;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIdx = 1;
                stmt.setInt(paramIdx++, groupId);
                stmt.setInt(paramIdx++, tenantId);
                if (isDeviceNameProvided) {
                    stmt.setString(paramIdx++, "%" + deviceName + "%");
                }
                if (isSinceProvided) {
                    stmt.setTimestamp(paramIdx++, new Timestamp(since.getTime()));
                }
                stmt.setInt(paramIdx++, tenantId);
                if (isDeviceTypeProvided) {
                    stmt.setString(paramIdx++, deviceType);
                }
                if (isOwnershipProvided) {
                    stmt.setString(paramIdx++, ownership);
                }
                if (isOwnerProvided) {
                    stmt.setString(paramIdx++, owner);
                } else if (isOwnerPatternProvided) {
                    stmt.setString(paramIdx++, ownerPattern + "%");
                }
                if (isStatusProvided) {
                    for (String status : statusList) {
                        stmt.setString(paramIdx++, status);
                    }
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        deviceCount = rs.getInt("DEVICE_COUNT");
                    }
                    return deviceCount;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving count of" +
                    " devices belonging to group : " + groupId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    protected Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }

    /**
     * Get device count of user.
     *
     * @return device count
     */
    @Override
    public int getDeviceCount(String username, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int deviceCount = 0;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(ID) AS DEVICE_COUNT FROM DM_ENROLMENT WHERE " +
                    "LOWER(OWNER) = LOWER(?) AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                deviceCount = rs.getInt("DEVICE_COUNT");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while getting the device count", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return deviceCount;
    }

    /**
     * Get device count of user.
     *
     * @return device count
     */
    @Override
    public int getDeviceCount(String type, String status, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int deviceCount = 0;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(ID) AS DEVICE_COUNT FROM DM_ENROLMENT WHERE " +
                    "DEVICE_TYPE = ? AND STATUS = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, type);
            stmt.setString(2, status);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                deviceCount = rs.getInt("DEVICE_COUNT");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while getting the device count", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return deviceCount;
    }


    public List<String> getDeviceIdentifiers(String type, String status, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<String> deviceIDs = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT DEVICE_IDENTIFICATION FROM DM_ENROLMENT WHERE " +
                    "DEVICE_TYPE = ? AND STATUS = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, type);
            stmt.setString(2, status);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                deviceIDs.add(rs.getString("DEVICE_IDENTIFICATION"));
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving tenants which have " +
                    "device registered.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return deviceIDs;
    }

    @Override
    public boolean setEnrolmentStatusInBulk(String deviceType, String status,
                                            int tenantId, List<String> devices) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            if (devices.isEmpty()) {
                return false;
            }
            conn = this.getConnection();
            StringBuilder sql = new StringBuilder("UPDATE DM_ENROLMENT SET STATUS = ? " +
                    "WHERE DEVICE_IDENTIFICATION IN (");
            for (int i = 0; i < devices.size(); i++) {
                sql.append("?,");
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") AND DEVICE_TYPE = ? AND TENANT_ID = ?");
            stmt = conn.prepareStatement(sql.toString());
            stmt.setString(1, status);
            int index = 1;
            for (String device : devices) {
                stmt.setString(++index, device);
            }
            stmt.setString(++index, deviceType);
            stmt.setInt(++index, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while updating enrollment status in bulk", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return true;
    }

    /**
     * Get device count of all devices.
     *
     * @return device count
     */
    @Override
    public int getDeviceCount(int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int deviceCount = 0;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(ID) AS DEVICE_COUNT FROM DM_ENROLMENT WHERE " +
                    "TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                deviceCount = rs.getInt("DEVICE_COUNT");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while getting the device count", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return deviceCount;
    }

    @Override
    public int getDeviceCount(PaginationRequest request, int tenantId) throws DeviceManagementDAOException {
        int deviceCount = 0;
        String deviceType = request.getDeviceType();
        boolean isDeviceTypeProvided = false;
        String deviceName = request.getDeviceName();
        boolean isDeviceNameProvided = false;
        String owner = request.getOwner();
        boolean isOwnerProvided = false;
        String ownerPattern = request.getOwnerPattern();
        boolean isOwnerPatternProvided = false;
        String ownership = request.getOwnership();
        boolean isOwnershipProvided = false;
        List<String> statusList = request.getStatusList();
        boolean isStatusProvided = false;
        Date since = request.getSince();
        boolean isSinceProvided = false;

        try {
            Connection conn = getConnection();
            String sql = "SELECT COUNT(d1.ID) AS DEVICE_COUNT " +
                    "FROM DM_ENROLMENT e, " +
                    "(SELECT " +
                    "d.ID, " +
                    "d.NAME, " +
                    "d.DEVICE_IDENTIFICATION " +
                    "FROM " +
                    "DM_DEVICE d " +
                    "WHERE 1=1 ";
            //Add query for last updated timestamp
            if (since != null) {
                sql = sql + " AND d.LAST_UPDATED_TIMESTAMP > ?";
                isSinceProvided = true;
            }
            if (deviceName != null && !deviceName.isEmpty()) {
                sql = sql + " AND d.NAME LIKE ?";
                isDeviceNameProvided = true;
            }
            sql = sql + ") d1 WHERE d1.ID = e.DEVICE_ID AND TENANT_ID = ?";
            if (deviceType != null && !deviceType.isEmpty()) {
                sql = sql + " AND e.DEVICE_TYPE = ?";
                isDeviceTypeProvided = true;
            }
            if (ownership != null && !ownership.isEmpty()) {
                sql = sql + " AND e.OWNERSHIP = ?";
                isOwnershipProvided = true;
            }
            //Add the query for owner
            if (owner != null && !owner.isEmpty()) {
                sql = sql + " AND e.OWNER LIKE ?";
                isOwnerProvided = true;
            } else if (ownerPattern != null && !ownerPattern.isEmpty()) {
                sql = sql + " AND e.OWNER LIKE ?";
                isOwnerPatternProvided = true;
            }
            if (statusList != null && !statusList.isEmpty()) {
                sql += buildStatusQuery(statusList);
                isStatusProvided = true;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIdx = 1;
                if (isSinceProvided) {
                    stmt.setTimestamp(paramIdx++, new Timestamp(since.getTime()));
                }
                if (isDeviceNameProvided) {
                    stmt.setString(paramIdx++, "%" + request.getDeviceName() + "%");
                }
                stmt.setInt(paramIdx++, tenantId);
                if (isDeviceTypeProvided) {
                    stmt.setString(paramIdx++, request.getDeviceType());
                }
                if (isOwnershipProvided) {
                    stmt.setString(paramIdx++, request.getOwnership());
                }
                if (isOwnerProvided) {
                    stmt.setString(paramIdx++, "%" + owner + "%");
                } else if (isOwnerPatternProvided) {
                    stmt.setString(paramIdx++, ownerPattern + "%");
                }
                if (isStatusProvided) {
                    for (String status : statusList) {
                        stmt.setString(paramIdx++, status);
                    }
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        deviceCount = rs.getInt("DEVICE_COUNT");
                    }
                    return deviceCount;
                }
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving information of all " +
                    "registered devices", e);
        }
    }

    @Override
    public int getDeviceCountByType(String type, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int deviceCount = 0;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(ID) AS DEVICE_COUNT FROM DM_ENROLMENT WHERE DEVICE_TYPE = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, type);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                deviceCount = rs.getInt("DEVICE_COUNT");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while getting the device count", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return deviceCount;
    }

    @Override
    public int getDeviceCountByUser(String username, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int deviceCount = 0;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(ID) AS DEVICE_COUNT FROM DM_ENROLMENT WHERE LOWER(OWNER) = LOWER(?)" +
                    " AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setInt(2, tenantId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                deviceCount = rs.getInt("DEVICE_COUNT");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices belongs to '" +
                    username + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return deviceCount;
    }

    @Override
    public int getDeviceCountByName(String deviceName, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int deviceCount = 0;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(d1.ID) AS DEVICE_COUNT FROM DM_ENROLMENT e, (SELECT d.ID FROM DM_DEVICE d " +
                    "WHERE d.NAME LIKE ? AND d.TENANT_ID = ?) d1 " +
                    "WHERE d1.ID = e.DEVICE_ID AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceName + "%");
            stmt.setInt(2, tenantId);
            stmt.setInt(3, tenantId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                deviceCount = rs.getInt("DEVICE_COUNT");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the device count that matches " +
                    "'" + deviceName + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return deviceCount;
    }

    @Override
    public int getDeviceCountByOwnership(String ownerShip, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int deviceCount = 0;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(ID) AS DEVICE_COUNT FROM DM_ENROLMENT WHERE " +
                    "TENANT_ID = ? AND OWNERSHIP = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, ownerShip);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                deviceCount = rs.getInt("DEVICE_COUNT");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices that matches to ownership " +
                    "'" + ownerShip + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return deviceCount;
    }

    @Override
    public int getDeviceCountByStatus(String status, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int deviceCount = 0;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(ID) AS DEVICE_COUNT FROM DM_ENROLMENT WHERE " +
                    "TENANT_ID = ? AND STATUS = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, status);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                deviceCount = rs.getInt("DEVICE_COUNT");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices that matches to status " +
                    "'" + status + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return deviceCount;
    }

    @Override
    public int getDeviceCountByStatus(String deviceType, String status, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int deviceCount = 0;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(ID) AS DEVICE_COUNT FROM DM_ENROLMENT WHERE " +
                    "TENANT_ID = ? AND STATUS = ? AND DEVICE_TYPE = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, status);
            stmt.setString(3, deviceType);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                deviceCount = rs.getInt("DEVICE_COUNT");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices that matches to status " +
                    "'" + status + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return deviceCount;
    }

    @Override
    public int addEnrollment(Device device, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int enrolmentId = -1;
        try {
            conn = this.getConnection();
            String sql = "INSERT INTO DM_ENROLMENT(DEVICE_ID, DEVICE_TYPE, DEVICE_IDENTIFICATION, OWNER, OWNERSHIP, " +
                    "STATUS, IS_TRANSFERRED, DATE_OF_ENROLMENT, DATE_OF_LAST_UPDATE, TENANT_ID) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, new String[]{"id"});
            stmt.setInt(1, device.getId());
            stmt.setString(2, device.getType());
            stmt.setString(3, device.getDeviceIdentifier());
            stmt.setString(4, device.getEnrolmentInfo().getOwner());
            stmt.setString(5, device.getEnrolmentInfo().getOwnership().toString());
            stmt.setString(6, device.getEnrolmentInfo().getStatus().toString());
            stmt.setBoolean(7, device.getEnrolmentInfo().isTransferred());
            stmt.setTimestamp(8, new Timestamp(new Date().getTime()));
            stmt.setTimestamp(9, new Timestamp(new Date().getTime()));
            stmt.setInt(10, tenantId);
            stmt.execute();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                enrolmentId = rs.getInt(1);
            }
            return enrolmentId;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while adding enrolment", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public boolean setEnrolmentStatus(DeviceIdentifier deviceId, String currentOwner, Status status,
                                      int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "UPDATE DM_ENROLMENT SET STATUS = ? WHERE DEVICE_IDENTIFICATION = ? " +
                    "AND DEVICE_TYPE = ? AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status.toString());
            stmt.setString(2, deviceId.getId());
            stmt.setString(3, deviceId.getType());
            stmt.setString(4, currentOwner);
            stmt.setInt(5, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while setting the status of device enrolment", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return true;
    }

    @Override
    public Status getEnrolmentStatus(DeviceIdentifier deviceId, String currentOwner,
                                     int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Status status = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT STATUS FROM DM_ENROLMENT WHERE DEVICE_IDENTIFICATION = ? " +
                    "AND DEVICE_TYPE = ? AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId.getId());
            stmt.setString(2, deviceId.getType());
            stmt.setString(3, currentOwner);
            stmt.setInt(4, tenantId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                status = Status.valueOf(rs.getString("STATUS"));
            }
            return status;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the status of device enrolment", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public EnrolmentInfo getEnrolment(DeviceIdentifier deviceId, PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        EnrolmentInfo enrolmentInfo = null;
        try {
            Connection conn = this.getConnection();
            String sql = "SELECT "
                    + "ID AS ENROLMENT_ID, "
                    + "DEVICE_ID, "
                    + "OWNER, "
                    + "OWNERSHIP, "
                    + "STATUS, "
                    + "IS_TRANSFERRED, "
                    + "DATE_OF_ENROLMENT, "
                    + "DATE_OF_LAST_UPDATE, "
                    + "TENANT_ID "
                    + "FROM DM_ENROLMENT "
                    + "WHERE "
                    + "DEVICE_IDENTIFICATION = ? AND DEVICE_TYPE = ? "
                    + "AND OWNER = ? ";

            if (!StringUtils.isBlank(request.getOwnership())) {
                sql += "AND OWNERSHIP = ? ";
            }
            sql += "AND TENANT_ID = ?";
            int paramIdx = 1;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(paramIdx++, deviceId.getId());
                stmt.setString(paramIdx++, deviceId.getType());
                stmt.setString(paramIdx++, request.getOwner());
                if (!StringUtils.isBlank(request.getOwnership())) {
                    stmt.setString(paramIdx++, request.getOwnership());
                }
                stmt.setInt(paramIdx, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        enrolmentInfo = DeviceManagementDAOUtil.loadMatchingEnrolment(rs);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving the enrolment " +
                    "information of user '" + request.getOwner() + "' upon device '" + deviceId + "'";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return enrolmentInfo;
    }

    @Override
    public EnrolmentInfo getActiveEnrolment(DeviceIdentifier deviceId, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        EnrolmentInfo enrolmentInfo = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT ID AS ENROLMENT_ID, DEVICE_ID, OWNER, OWNERSHIP, STATUS, IS_TRANSFERRED, " +
                    "DATE_OF_ENROLMENT, DATE_OF_LAST_UPDATE, TENANT_ID FROM DM_ENROLMENT " +
                    "WHERE DEVICE_IDENTIFICATION = ? AND DEVICE_TYPE = ? AND TENANT_ID = ? AND STATUS != 'REMOVED'";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId.getId());
            stmt.setString(2, deviceId.getType());
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                enrolmentInfo = DeviceManagementDAOUtil.loadEnrolment(rs);
            }
            return enrolmentInfo;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the enrolment " +
                    "information of device '" + deviceId.getId() + "' of type : "
                    + deviceId.getType(), e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    public List<Device> getDevicesByStatus(EnrolmentInfo.Status status, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME AS DEVICE_NAME, d.LAST_UPDATED_TIMESTAMP, " +
                    "e.DEVICE_TYPE, e.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, " +
                    "e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT, e.ENROLMENT_ID FROM " +
                    "(SELECT e.ID, e.DEVICE_ID, e.DEVICE_TYPE, e.DEVICE_IDENTIFICATION, e.OWNER, " +
                    "e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_ENROLMENT, e.DATE_OF_LAST_UPDATE, e.ID AS " +
                    "ENROLMENT_ID FROM DM_ENROLMENT e WHERE TENANT_ID = ? AND STATUS = ?) e, " +
                    "DM_DEVICE d WHERE d.ID = e.DEVICE_ID AND d.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, status.toString());
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices that matches to status " +
                    "'" + status + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devices;
    }

    @Override
    @Deprecated
    public List<DeviceType> getDeviceTypes()
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<DeviceType> deviceTypes;
        try {
            conn = this.getConnection();
            String sql = "SELECT t.ID, t.NAME FROM DM_DEVICE_TYPE t";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            deviceTypes = new ArrayList<>();
            while (rs.next()) {
                DeviceType deviceType = DeviceManagementDAOUtil.loadDeviceType(rs);
                deviceTypes.add(deviceType);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while listing device types.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return deviceTypes;
    }

    /**
     * Returns the collection of devices that has been updated after the time given in the timestamp passed in.
     *
     * @param timestamp Timestamp in long, after which the devices have been updated.
     * @param tenantId  Tenant id of the currently logged-in user.
     * @return A collection of devices that have been updated after the provided timestamp
     */
    public List<Device> getDevices(long timestamp, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, e.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, d1.LAST_UPDATED_TIMESTAMP, e.OWNER, e.OWNERSHIP, e.STATUS, " +
                    "e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT, " +
                    "e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID AS DEVICE_ID, " +
                    "d.DESCRIPTION, d.NAME, d.DEVICE_IDENTIFICATION, d.LAST_UPDATED_TIMESTAMP FROM DM_DEVICE d " +
                    "WHERE d.TENANT_ID = ? AND d.LAST_UPDATED_TIMESTAMP < CURRENT_TIMESTAMP) d1 " +
                    "WHERE d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving information of all " +
                    "registered devices", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devices;
    }


    public List<Integer> getDeviceEnrolledTenants() throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Integer> tenants = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT distinct(TENANT_ID) FROM DM_DEVICE";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                tenants.add(rs.getInt("TENANT_ID"));
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving tenants which have " +
                    "device registered.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return tenants;
    }

    public List<GeoCluster> findGeoClusters(GeoQuery geoQuery, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<GeoCluster> geoClusters = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT AVG(DEVICE_LOCATION.LATITUDE) AS LATITUDE, " +
                    "AVG(DEVICE_LOCATION.LONGITUDE) AS LONGITUDE, " +
                    "MIN(DEVICE_LOCATION.LATITUDE) AS MIN_LATITUDE,  " +
                    "MAX(DEVICE_LOCATION.LATITUDE) AS MAX_LATITUDE, " +
                    "MIN(DEVICE_LOCATION.LONGITUDE) AS MIN_LONGITUDE, " +
                    "MAX(DEVICE_LOCATION.LONGITUDE) AS MAX_LONGITUDE, " +
                    "SUBSTRING(DEVICE_LOCATION.GEO_HASH,1,?) AS GEOHASH_PREFIX,  " +
                    "COUNT(DEVICE_LOCATION.ID) AS COUNT, " +
                    "MIN(DEVICE.ID) AS DEVICE_ID, " +
                    "MIN(DEVICE.NAME) AS DEVICE_NAME, " +
                    "MIN(DEVICE.DESCRIPTION) AS DESCRIPTION, " +
                    "MAX(DEVICE.LAST_UPDATED_TIMESTAMP) AS LAST_UPDATED_TIMESTAMP, " +
                    "MIN(ENROLMENT.DEVICE_TYPE) AS DEVICE_TYPE, " +
                    "MIN(DEVICE.DEVICE_IDENTIFICATION) AS DEVICE_IDENTIFICATION, " +
                    "MIN(ENROLMENT.ID) AS ENROLMENT_ID, " +
                    "MIN(ENROLMENT.OWNER) AS OWNER, " +
                    "MIN(ENROLMENT.OWNERSHIP) AS OWNERSHIP, " +
                    "MIN(ENROLMENT.IS_TRANSFERRED) AS IS_TRANSFERRED, " +
                    "MIN(ENROLMENT.DATE_OF_ENROLMENT) AS DATE_OF_ENROLMENT, " +
                    "MIN(ENROLMENT.DATE_OF_LAST_UPDATE) AS DATE_OF_LAST_UPDATE, " +
                    "MIN(ENROLMENT.STATUS) AS STATUS " +
                    "FROM DM_DEVICE_LOCATION AS DEVICE_LOCATION, DM_DEVICE AS DEVICE, " +
                    "DM_ENROLMENT AS ENROLMENT " +
                    "WHERE DEVICE_LOCATION.LATITUDE BETWEEN ? AND ? " +
                    "AND DEVICE_LOCATION.LONGITUDE BETWEEN ? AND ? ";
            if (geoQuery.getDeviceTypes() != null && !geoQuery.getDeviceTypes().isEmpty()) {
                sql += "AND ENROLMENT.DEVICE_TYPE IN (";
                sql += String.join(", ",
                        Collections.nCopies(geoQuery.getDeviceTypes().size(), "?"));
                sql += ") ";
            }
            if (geoQuery.getDeviceIdentifiers() != null && !geoQuery.getDeviceIdentifiers().isEmpty()) {
                sql += "AND DEVICE.DEVICE_IDENTIFICATION IN (";
                sql += String.join(", ",
                        Collections.nCopies(geoQuery.getDeviceIdentifiers().size(), "?"));
                sql += ") ";
            }
            if (geoQuery.getOwners() != null && !geoQuery.getOwners().isEmpty()) {
                sql += "AND ENROLMENT.OWNER IN (";
                sql += String.join(", ",
                        Collections.nCopies(geoQuery.getOwners().size(), "?"));
                sql += ") ";
            }
            if (geoQuery.getOwnerships() != null && !geoQuery.getOwnerships().isEmpty()) {
                sql += "AND ENROLMENT.OWNERSHIP IN (";
                sql += String.join(", ",
                        Collections.nCopies(geoQuery.getOwnerships().size(), "?"));
                sql += ") ";
            }
            if (geoQuery.getStatuses() != null && !geoQuery.getStatuses().isEmpty()) {
                sql += "AND ENROLMENT.STATUS IN (";
                sql += String.join(", ",
                        Collections.nCopies(geoQuery.getStatuses().size(), "?"));
                sql += ") ";
            } else {
                sql += "AND ENROLMENT.STATUS != 'REMOVED' ";
            }
            if (geoQuery.getCreatedBefore() != 0 || geoQuery.getCreatedAfter() != 0) {
                sql += "AND ENROLMENT.DATE_OF_ENROLMENT BETWEEN ? AND ? ";
            }
            if (geoQuery.getUpdatedBefore() != 0 || geoQuery.getUpdatedAfter() != 0) {
                sql += "AND ENROLMENT.DATE_OF_LAST_UPDATE BETWEEN ? AND ? ";
            }
            sql += "AND DEVICE.ID = DEVICE_LOCATION.DEVICE_ID " +
                    "AND DEVICE.ID = ENROLMENT.DEVICE_ID " +
                    "AND DEVICE.TENANT_ID = ? AND DEVICE.TENANT_ID = ENROLMENT.TENANT_ID GROUP BY GEOHASH_PREFIX";
            stmt = conn.prepareStatement(sql);

            int index = 1;
            stmt.setInt(index++, geoQuery.getGeohashLength());
            stmt.setDouble(index++, geoQuery.getSouthWest().getLatitude());
            stmt.setDouble(index++, geoQuery.getNorthEast().getLatitude());
            stmt.setDouble(index++, geoQuery.getSouthWest().getLongitude());
            stmt.setDouble(index++, geoQuery.getNorthEast().getLongitude());
            if (geoQuery.getDeviceTypes() != null) {
                for (String s : geoQuery.getDeviceTypes()) {
                    stmt.setString(index++, s);
                }
            }
            if (geoQuery.getDeviceIdentifiers() != null) {
                for (String s : geoQuery.getDeviceIdentifiers()) {
                    stmt.setString(index++, s);
                }
            }
            if (geoQuery.getOwners() != null) {
                for (String s : geoQuery.getOwners()) {
                    stmt.setString(index++, s);
                }
            }
            if (geoQuery.getOwnerships() != null) {
                for (String s : geoQuery.getOwnerships()) {
                    stmt.setString(index++, s);
                }
            }
            if (geoQuery.getStatuses() != null) {
                for (Status s : geoQuery.getStatuses()) {
                    stmt.setString(index++, s.toString());
                }
            }

            if (geoQuery.getCreatedBefore() != 0 || geoQuery.getCreatedAfter() != 0) {
                stmt.setTimestamp(index++, new Timestamp(geoQuery.getCreatedAfter()));
                if (geoQuery.getCreatedBefore() == 0) {
                    stmt.setTimestamp(index++, new Timestamp(System.currentTimeMillis()));
                } else {
                    stmt.setTimestamp(index++, new Timestamp(geoQuery.getCreatedBefore()));
                }
            }
            if (geoQuery.getUpdatedBefore() != 0 || geoQuery.getUpdatedAfter() != 0) {
                stmt.setTimestamp(index++, new Timestamp(geoQuery.getUpdatedAfter()));
                if (geoQuery.getUpdatedBefore() == 0) {
                    stmt.setTimestamp(index++, new Timestamp(System.currentTimeMillis()));
                } else {
                    stmt.setTimestamp(index++, new Timestamp(geoQuery.getUpdatedBefore()));
                }
            }
            stmt.setInt(index, tenantId);

            rs = stmt.executeQuery();

            double latitude;
            double longitude;
            double minLatitude;
            double maxLatitude;
            double minLongitude;
            double maxLongitude;
            long count;
            String geohashPrefix;
            Device device;
            while (rs.next()) {
                latitude = rs.getDouble("LATITUDE");
                longitude = rs.getDouble("LONGITUDE");
                minLatitude = rs.getDouble("MIN_LATITUDE");
                maxLatitude = rs.getDouble("MAX_LATITUDE");
                minLongitude = rs.getDouble("MIN_LONGITUDE");
                maxLongitude = rs.getDouble("MAX_LONGITUDE");
                count = rs.getLong("COUNT");
                geohashPrefix = rs.getString("GEOHASH_PREFIX");
                if (count == 1) {
                    device = DeviceManagementDAOUtil.loadDevice(rs);
                } else {
                    device = null;
                }
                geoClusters.add(new GeoCluster(new GeoCoordinate(latitude, longitude),
                        new GeoCoordinate(minLatitude, minLongitude), new GeoCoordinate(maxLatitude, maxLongitude),
                        count, geohashPrefix, device));
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving information of " +
                    "Geo Clusters", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return geoClusters;
    }

    @Override
    public List<Device> getDevicesByIdentifiers(List<String> deviceIdentifiers, int tenantId)
            throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            int index = 1;
            int counter = 0;
            List<Device> devices = new ArrayList<>();

            if (deviceIdentifiers.isEmpty()) {
                return devices;
            }

            StringJoiner joiner = new StringJoiner(",",
                    "SELECT "
                            + "d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.LAST_UPDATED_TIMESTAMP, "
                            + "e.DEVICE_TYPE, e.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, "
                            + "e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID "
                            + "FROM "
                            + "DM_ENROLMENT e, "
                            + "(SELECT d.ID, d.DESCRIPTION, d.NAME, d.LAST_UPDATED_TIMESTAMP "
                            + "FROM DM_DEVICE d "
                            + "WHERE "
                            + "d.DEVICE_IDENTIFICATION IN (",
                    ") AND d.TENANT_ID = ?) d1 "
                            + "WHERE d1.ID = e.DEVICE_ID AND TENANT_ID = ? "
                            + "ORDER BY e.DATE_OF_LAST_UPDATE DESC, e.STATUS ASC");
            while (counter < deviceIdentifiers.size()) {
                joiner.add("?");
                counter++;
            }
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (String identifier : deviceIdentifiers) {
                    ps.setObject(index++, identifier);
                }
                ps.setInt(index++, tenantId);
                ps.setInt(index, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        devices.add(DeviceManagementDAOUtil.loadDevice(rs));
                    }
                }
            }
            return devices;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while obtaining the DB connection to get devices for"
                    + " given device identifiers.", e);
        }
    }

    @Override
    public List<Device> getDevicesByIdentifiersAndStatuses(List<String> deviceIdentifiers,
                                                           List<EnrolmentInfo.Status> statuses, int tenantId)
            throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            int index = 1;
            int counter = 0;
            List<Device> devices = new ArrayList<>();

            if (deviceIdentifiers.isEmpty() || statuses.isEmpty()) {
                return devices;
            }

            StringJoiner statusJoiner = new StringJoiner(",", "e.STATUS IN (", ") ");
            while (counter < statuses.size()) {
                statusJoiner.add("?");
                counter++;
            }

            StringJoiner joiner = new StringJoiner(",",
                    "SELECT "
                            + "d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, e.DEVICE_TYPE, "
                            + "d1.DEVICE_IDENTIFICATION, d1.LAST_UPDATED_TIMESTAMP, e.OWNER, e.OWNERSHIP, "
                            + "e.STATUS, e.IS_TRANSFERRED, "
                            + "e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID "
                            + "FROM "
                            + "DM_ENROLMENT e, "
                            + "(SELECT d.ID, d.DESCRIPTION, d.NAME, d.DEVICE_IDENTIFICATION, d.LAST_UPDATED_TIMESTAMP "
                            + "FROM DM_DEVICE d "
                            + "WHERE "
                            + " d.DEVICE_IDENTIFICATION IN (",
                    ") AND d.TENANT_ID = ?) d1 "
                            + "WHERE d1.ID = e.DEVICE_ID AND " + statusJoiner.toString()
                            + "AND TENANT_ID = ? ORDER BY e.DATE_OF_LAST_UPDATE DESC, e.STATUS ASC");

            counter = 0;
            while (counter < deviceIdentifiers.size()) {
                joiner.add("?");
                counter++;
            }
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (String identifier : deviceIdentifiers) {
                    ps.setString(index++, identifier);
                }
                ps.setInt(index++, tenantId);
                for (EnrolmentInfo.Status status : statuses) {
                    ps.setString(index++, status.toString());
                }
                ps.setInt(index, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        devices.add(DeviceManagementDAOUtil.loadDevice(rs));
                    }
                }
            }
            return devices;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while obtaining the DB connection to get devices for"
                    + " given device identifiers and statuses.", e);
        }
    }

    public List<DeviceLocationHistorySnapshot> getDeviceLocationInfo(DeviceIdentifier deviceIdentifier, long from,
                                                                     long to) throws DeviceManagementDAOException {
        List<DeviceLocationHistorySnapshot> deviceLocationHistories = new ArrayList<>();
        String sql = "SELECT "
                + "DEVICE_ID, "
                + "TENANT_ID, "
                + "DEVICE_ID_NAME, "
                + "DEVICE_TYPE_NAME, "
                + "LATITUDE, "
                + "LONGITUDE, "
                + "SPEED, "
                + "HEADING, "
                + "TIMESTAMP, "
                + "GEO_HASH, "
                + "DEVICE_OWNER, "
                + "DEVICE_ALTITUDE, "
                + "DISTANCE "
                + "FROM DM_DEVICE_HISTORY_LAST_SEVEN_DAYS "
                + "WHERE "
                + "DEVICE_ID_NAME = ? AND "
                + "DEVICE_TYPE_NAME = ? AND "
                + "TIMESTAMP BETWEEN ? AND ? "
                + "ORDER BY timestamp";
        try {
            Connection conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, deviceIdentifier.getId());
                stmt.setString(2, deviceIdentifier.getType());
                stmt.setLong(3, from);
                stmt.setLong(4, to);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        deviceLocationHistories.add(DeviceManagementDAOUtil.loadDeviceLocation(rs));
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while obtaining the DB connection to get device location information";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return deviceLocationHistories;
    }

    @Override
    public void deleteDevices(List<String> deviceIdentifiers, List<Integer> deviceIds, List<Integer> enrollmentIds, List<Device> validDevices)
            throws DeviceManagementDAOException {
        Connection conn;
        try {
            conn = this.getConnection();
            if (enrollmentIds.isEmpty()) {
                String msg = "Enrollments not found for the devices: " + deviceIdentifiers;
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            } else {
                removeDeviceDetail(conn, deviceIds);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully removed device detail data of devices: " + deviceIdentifiers);
                }
                removeDeviceLocation(conn, deviceIds);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully removed device location data of devices: " + deviceIdentifiers);
                }
                removeDeviceInfo(conn, deviceIds);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully removed device info data of devices: " + deviceIdentifiers);
                }
                removeDeviceNotification(conn, deviceIds);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully removed device notification data of devices: " + deviceIdentifiers);
                }
                removeDevicePolicyApplied(conn, deviceIds);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully removed device applied policy data of devices: " + deviceIdentifiers);
                }
                removeDevicePolicy(conn, deviceIds);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully removed device policy data of devices: " + deviceIdentifiers);
                }
                removeDeviceApplication(conn, deviceIds);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully removed device application data of devices: " + deviceIdentifiers);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Starting to remove " + enrollmentIds.size() + " enrollment data of devices with " +
                            "identifiers: " + deviceIdentifiers);
                }
                removeEnrollmentDeviceDetail(conn, enrollmentIds);
                removeEnrollmentDeviceLocation(conn, enrollmentIds);
                removeEnrollmentDeviceInfo(conn, enrollmentIds);
                removeDeviceLargeOperationResponse(conn, enrollmentIds);
                removeDeviceOperationResponse(conn, enrollmentIds);
                removeEnrollmentOperationMapping(conn, enrollmentIds);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully removed enrollment device details, enrollment device location," +
                            "enrollment device info, enrollment device application mapping, " +
                            "enrollment device operation large response, enrollment device " +
                            "operation response, enrollment operation mapping data of " +
                            "devices with identifiers:  " + deviceIdentifiers);
                }
                refactorEnrolment(conn, deviceIds);
                refactorDeviceStatus(conn, validDevices);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully removed device enrollment data of devices: " + deviceIdentifiers);
                }
                removeDeviceGroupMapping(conn, deviceIds);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully removed device group mapping data of devices: " + deviceIdentifiers);
                }
                refactorDevice(conn, deviceIds);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully permanently deleted the device of devices: " + deviceIdentifiers);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting the devices: " + deviceIdentifiers;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Device> getAppNotInstalledDevices(
            PaginationRequest request, int tenantId, String packageName, String version)
            throws DeviceManagementDAOException {
        List<Device> devices;
        String deviceType = request.getDeviceType();
        boolean isVersionProvided = false;

        String sql = "SELECT " +
                "d.ID AS DEVICE_ID, " +
                "d.DESCRIPTION, " +
                "d.NAME AS DEVICE_NAME, " +
                "e.DEVICE_TYPE, " +
                "d.DEVICE_IDENTIFICATION, " +
                "d.LAST_UPDATED_TIMESTAMP, " +
                "e.OWNER, " +
                "e.OWNERSHIP, " +
                "e.STATUS, " +
                "e.IS_TRANSFERRED, " +
                "e.DATE_OF_LAST_UPDATE, " +
                "e.DATE_OF_ENROLMENT, " +
                "e.ID AS ENROLMENT_ID " +
                "FROM DM_DEVICE AS d " +
                "INNER JOIN DM_ENROLMENT AS e ON d.ID = e.DEVICE_ID " +
                "WHERE " +
                "e.DEVICE_TYPE = ? AND e.TENANT_ID = ? AND d.ID " +
                "NOT IN " +
                "(SELECT a.DEVICE_ID FROM DM_APPLICATION AS a WHERE a.APP_IDENTIFIER = ?";

        if (!StringUtils.isBlank(version)) {
            sql = sql + " AND a.VERSION = ? ";
            isVersionProvided = true;
        }

        sql = sql + ") LIMIT ? OFFSET ?";

        try {
            Connection conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIdx = 1;
                stmt.setString(paramIdx++, deviceType);
                stmt.setInt(paramIdx++, tenantId);
                stmt.setString(paramIdx++, packageName);
                if (isVersionProvided) {
                    stmt.setString(paramIdx++, version);
                }
                stmt.setInt(paramIdx++, request.getRowCount());
                stmt.setInt(paramIdx, request.getStartIndex());

                try (ResultSet rs = stmt.executeQuery()) {
                    devices = new ArrayList<>();
                    while (rs.next()) {
                        Device device = DeviceManagementDAOUtil.loadDevice(rs);
                        devices.add(device);
                    }
                    return devices;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of all " +
                    "registered devices under tenant id " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public int getCountOfAppNotInstalledDevices(
            PaginationRequest request, int tenantId, String packageName, String version)
            throws DeviceManagementDAOException {
        String deviceType = request.getDeviceType();
        boolean isVersionProvided = false;

        String sql = "SELECT " +
                "COUNT(d.ID) AS DEVICE_COUNT " +
                "FROM DM_DEVICE AS d " +
                "INNER JOIN DM_ENROLMENT AS e ON d.ID = e.DEVICE_ID " +
                "WHERE " +
                "e.DEVICE_TYPE = ? AND e.TENANT_ID = ? AND d.ID " +
                "NOT IN " +
                "(SELECT a.DEVICE_ID FROM DM_APPLICATION AS a WHERE a.APP_IDENTIFIER = ?";

        if (!StringUtils.isBlank(version)) {
            sql = sql + " AND a.VERSION = ? ";
            isVersionProvided = true;
        }

        sql = sql + ")";

        try {
            Connection conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIdx = 1;
                stmt.setString(paramIdx++, deviceType);
                stmt.setInt(paramIdx++, tenantId);
                stmt.setString(paramIdx++, packageName);
                if (isVersionProvided) {
                    stmt.setString(paramIdx, version);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    int deviceCount = 0;
                    if (rs.next()) {
                        deviceCount = rs.getInt("DEVICE_COUNT");
                    }
                    return deviceCount;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of all " +
                    "registered devices under tenant id " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Device> getDevicesByEncryptionStatus(PaginationRequest request, int tenantId, boolean isEncrypted)
            throws DeviceManagementDAOException {
        try {
            Connection conn = getConnection();
            String sql =
                    "SELECT e1.owner," +
                            "e1.ownership," +
                            "e1.enrolment_id," +
                            "e1.device_id," +
                            "e1.status," +
                            "e1.date_of_last_update," +
                            "e1.date_of_enrolment," +
                            "d.description," +
                            "d.NAME AS DEVICE_NAME," +
                            "d.device_identification," +
                            "d.LAST_UPDATED_TIMESTAMP, " +
                            "e1.DEVICE_TYPE " +
                            "FROM dm_device d," +
                            "(SELECT e.owner," +
                            "e.ownership," +
                            "e.id AS ENROLMENT_ID," +
                            "e.device_id," +
                            "e.status, " +
                            "e.date_of_last_update, " +
                            "e.date_of_enrolment, " +
                            "e.DEVICE_TYPE " +
                            "FROM dm_enrolment e " +
                            "INNER JOIN " +
                            "(SELECT DEVICE_ID " +
                            "FROM DM_DEVICE_INFO " +
                            "WHERE " +
                            "KEY_FIELD = 'encryptionEnabled' " +
                            "AND VALUE_FIELD = ?) AS di " +
                            "ON di.DEVICE_ID = e.DEVICE_ID " +
                            "WHERE e.tenant_id = ?) e1 " +
                            "WHERE d.id = e1.device_id " +
                            "ORDER BY e1.date_of_last_update DESC " +
                            "LIMIT ? OFFSET ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, isEncrypted);
                ps.setInt(2, tenantId);
                ps.setInt(3, request.getRowCount());
                ps.setInt(4, request.getStartIndex());

                try (ResultSet rs = ps.executeQuery()) {
                    List<Device> devices = new ArrayList<>();
                    if (rs.next()) {
                        Device device = DeviceManagementDAOUtil.loadDevice(rs);
                        devices.add(device);
                    }
                    return devices;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while building or executing queries to retrieve information " +
                    "of devices filtered by encryption status: " + isEncrypted;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public int getCountOfDevicesByEncryptionStatus(int tenantId, boolean isEncrypted)
            throws DeviceManagementDAOException {
        try {
            Connection conn = getConnection();
            String sql =
                    "SELECT COUNT(DEVICE_ID) AS DEVICE_COUNT " +
                            "FROM DM_DEVICE_INFO " +
                            "WHERE KEY_FIELD = 'encryptionEnabled' " +
                            "AND VALUE_FIELD = ?";
            //TODO: Add tenant column to DM_DEVICE_INFO table

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, isEncrypted);

                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt("DEVICE_COUNT") : 0;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while building or executing queries to retrieve the count of devices " +
                    "in the provided encryption status: " + isEncrypted;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /***
     * This method removes records of a given list of devices from the DM_DEVICE_DETAIL table
     * @param conn Connection object
     * @param deviceIds list of device ids (primary keys)
     * @throws DeviceManagementDAOException if deletion fails
     */
    private void removeDeviceDetail(Connection conn, List<Integer> deviceIds) throws DeviceManagementDAOException {
        String sql = "DELETE FROM DM_DEVICE_DETAIL WHERE DEVICE_ID = ?";
        try {
            if (!executeBatchOperation(conn, sql, deviceIds)) {
                String msg = "Failed to remove device details of devices with deviceIds : " + deviceIds +
                        " while executing batch operation";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while removing device details of devices with deviceIds : " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /***
     * This method removes records of a given list of devices from the DM_DEVICE_LOCATION table
     * @param conn Connection object
     * @param deviceIds list of device ids (primary keys)
     * @throws DeviceManagementDAOException if deletion fails
     */
    private void removeDeviceLocation(Connection conn, List<Integer> deviceIds) throws DeviceManagementDAOException {
        String sql = "DELETE FROM DM_DEVICE_LOCATION WHERE DEVICE_ID = ?";
        try {
            if (!executeBatchOperation(conn, sql, deviceIds)) {
                String msg = "Failed to remove locations of devices with deviceIds : " + deviceIds
                        + " while executing batch operation";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while removing locations of devices with deviceIds : " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /***
     * This method removes records of a given list of devices from the DM_DEVICE_INFO table
     * @param conn Connection object
     * @param deviceIds list of device ids (primary keys)
     * @throws DeviceManagementDAOException if deletion fails
     */
    private void removeDeviceInfo(Connection conn, List<Integer> deviceIds) throws DeviceManagementDAOException {
        String sql = "DELETE FROM DM_DEVICE_INFO WHERE DEVICE_ID = ?";
        try {
            if (!executeBatchOperation(conn, sql, deviceIds)) {
                String msg = "Failed to remove device info of devices with deviceIds : " + deviceIds
                        + " while executing batch operation";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while removing device info of devices with deviceIds : " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /***
     * This method removes records of a given list of devices from the DM_NOTIFICATION table
     * @param conn Connection object
     * @param deviceIds list of device ids (primary keys)
     * @throws DeviceManagementDAOException if deletion fails
     */
    private void removeDeviceNotification(Connection conn, List<Integer> deviceIds) throws DeviceManagementDAOException {
        String sql = "DELETE FROM DM_NOTIFICATION WHERE DEVICE_ID = ?";
        try {
            if (!executeBatchOperation(conn, sql, deviceIds)) {
                String msg = "Failed to remove device notifications of devices with deviceIds : " + deviceIds +
                        " while executing batch operation";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while removing device notifications of devices with deviceIds : " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

    }


    /***
     * This method removes records of a given list of devices from the DM_DEVICE_POLICY_APPLIED table
     * @param conn Connection object
     * @param deviceIds list of device ids (primary keys)
     * @throws DeviceManagementDAOException if deletion fails
     */
    private void removeDevicePolicyApplied(Connection conn, List<Integer> deviceIds)
            throws DeviceManagementDAOException {
        String sql = "DELETE FROM DM_DEVICE_POLICY_APPLIED WHERE DEVICE_ID = ?";
        try {
            if (!executeBatchOperation(conn, sql, deviceIds)) {
                String msg = "Failed to remove policies applied on devices with deviceIds : " + deviceIds +
                        " while executing batch operation";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while removing policies applied on devices with deviceIds : " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /***
     * This method removes records of a given list of devices from the DM_DEVICE_POLICY table
     * @param conn Connection object
     * @param deviceIds list of device ids (primary keys)
     * @throws DeviceManagementDAOException if deletion fails
     */
    private void removeDevicePolicy(Connection conn, List<Integer> deviceIds) throws DeviceManagementDAOException {
        String sql = "DELETE FROM DM_DEVICE_POLICY WHERE DEVICE_ID = ?";
        try {
            if (!executeBatchOperation(conn, sql, deviceIds)) {
                String msg = "Failed to remove policies of devices with deviceIds : " + deviceIds +
                        " while executing batch operation";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while removing policies of devices with deviceIds : " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /***
     * This method removes records of a given list of devices from the DM_APPLICATION table
     * @param conn Connection object
     * @param deviceIds list of device ids (primary keys)
     * @throws DeviceManagementDAOException if deletion fails
     */
    private void removeDeviceApplication(Connection conn, List<Integer> deviceIds)
            throws DeviceManagementDAOException {
        String sql = "DELETE FROM DM_APPLICATION WHERE DEVICE_ID = ?";
        try {
            if (!executeBatchOperation(conn, sql, deviceIds)) {
                String msg = "Failed to remove applications of devices with deviceIds : " + deviceIds +
                        " while executing batch operation";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while removing applications of devices devices with deviceIds : " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /***
     * This method removes records of a given list of enrollments from the DM_DEVICE_DETAIL table
     * @param conn Connection object
     * @param enrollmentIds list of enrollment ids (primary keys)
     * @throws DeviceManagementDAOException if deletion fails
     */
    private void removeEnrollmentDeviceDetail(Connection conn, List<Integer> enrollmentIds)
            throws DeviceManagementDAOException {
        String sql = "DELETE FROM DM_DEVICE_DETAIL WHERE ENROLMENT_ID = ?";
        try {
            if (!executeBatchOperation(conn, sql, enrollmentIds)) {
                String msg = "Failed to remove enrollment details of devices with enrollmentIds : " + enrollmentIds
                        + " while executing batch operation";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while removing enrollment details of devices with enrollmentIds : "
                    + enrollmentIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /***
     * This method removes records of a given list of enrollments from the DM_DEVICE_LOCATION table
     * @param conn Connection object
     * @param enrollmentIds list of enrollment ids (primary keys)
     * @throws DeviceManagementDAOException if deletion fails
     */
    private void removeEnrollmentDeviceLocation(Connection conn, List<Integer> enrollmentIds)
            throws DeviceManagementDAOException {
        String sql = "DELETE FROM DM_DEVICE_LOCATION WHERE ENROLMENT_ID = ?";
        try {
            if (!executeBatchOperation(conn, sql, enrollmentIds)) {
                String msg = "Failed to remove enrollment locations of devices with enrollmentIds : " + enrollmentIds
                        + " while executing batch operation";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while removing enrollment locations of devices with enrollmentIds : "
                    + enrollmentIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /***
     * This method removes records of a given list of enrollments from the DM_DEVICE_INFO table
     * @param conn Connection object
     * @param enrollmentIds list of enrollment ids (primary keys)
     * @throws DeviceManagementDAOException if deletion fails
     */
    private void removeEnrollmentDeviceInfo(Connection conn, List<Integer> enrollmentIds)
            throws DeviceManagementDAOException {
        String sql = "DELETE FROM DM_DEVICE_INFO WHERE ENROLMENT_ID = ?";
        try {
            if (!executeBatchOperation(conn, sql, enrollmentIds)) {
                String msg = "Failed to remove enrollment info of devices with enrollmentIds : " + enrollmentIds
                        + " while executing batch operation";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while removing enrollment info of devices with enrollmentIds : "
                    + enrollmentIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }


    /***
     * This method removes records of a given list of enrollments from the DM_DEVICE_OPERATION_RESPONSE table
     * @param conn Connection object
     * @param enrollmentIds list of enrollment ids (primary keys)
     * @throws DeviceManagementDAOException if deletion fails
     */
    private void removeDeviceOperationResponse(Connection conn, List<Integer> enrollmentIds)
            throws DeviceManagementDAOException {
        try {
            String sql = "DELETE FROM DM_DEVICE_OPERATION_RESPONSE WHERE ENROLMENT_ID = ?";
            if (!executeBatchOperation(conn, sql, enrollmentIds)) {
                String msg = "Failed to remove device operation response of devices with enrollmentIds : "
                        + enrollmentIds + " while executing batch operation";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while removing device operation response of devices with enrollmentIds : "
                    + enrollmentIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    private void removeDeviceLargeOperationResponse(Connection conn, List<Integer> enrollmentIds)
            throws DeviceManagementDAOException {
        String sql = "DELETE FROM DM_DEVICE_OPERATION_RESPONSE_LARGE " +
                "WHERE EXISTS (SELECT ID FROM DM_DEVICE_OPERATION_RESPONSE " +
                "WHERE DM_DEVICE_OPERATION_RESPONSE.ID = DM_DEVICE_OPERATION_RESPONSE_LARGE.ID " +
                "AND DM_DEVICE_OPERATION_RESPONSE.ENROLMENT_ID = ?)";
        try {
            if (!executeBatchOperation(conn, sql, enrollmentIds)) {
                String msg = "Failed to remove device large operation response of devices with " +
                        "enrollmentIds : "
                        + enrollmentIds + " while executing batch operation";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while removing large device operation response of " +
                    "devices with enrollmentIds : " + enrollmentIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /***
     * This method removes records of a given list of enrollments from the DM_ENROLMENT_OP_MAPPING table
     * @param conn Connection object
     * @param enrollmentIds list of enrollment ids (primary keys)
     * @throws DeviceManagementDAOException if deletion fails
     */
    private void removeEnrollmentOperationMapping(Connection conn, List<Integer> enrollmentIds)
            throws DeviceManagementDAOException {
        String sql = "DELETE FROM DM_ENROLMENT_OP_MAPPING WHERE ENROLMENT_ID = ?";
        try {
            if (!executeBatchOperation(conn, sql, enrollmentIds)) {
                String msg = "Failed to remove enrollment operation mapping of devices with enrollmentIds : "
                        + enrollmentIds + " while executing batch operation";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while removing enrollment operation mapping of devices with enrollmentIds :"
                    + enrollmentIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /***
     * This method removes records of a given list of devices from the DM_DEVICE_GROUP_MAP table
     * @param conn Connection object
     * @param deviceIds list of device ids (primary keys)
     * @throws DeviceManagementDAOException if deletion fails
     */
    private void removeDeviceGroupMapping(Connection conn, List<Integer> deviceIds) throws DeviceManagementDAOException {
        String sql = "DELETE FROM DM_DEVICE_GROUP_MAP WHERE DEVICE_ID = ?";
        try {
            if (!executeBatchOperation(conn, sql, deviceIds)) {
                String msg = "Failed to remove device group mapping of devices with deviceIds : " + deviceIds
                        + " while executing batch operation";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while removing device group mapping of devices with deviceIds : "
                    + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /***
     * This method refactors some attributes of a given list of devices which are being deleted by the user
     * @param conn Connection object
     * @param deviceIds list of device ids (primary keys)
     * @throws DeviceManagementDAOException if refactoring fails
     */
    public void refactorDevice(Connection conn, List<Integer> deviceIds) throws DeviceManagementDAOException {
        String updateQuery = "UPDATE DM_DEVICE SET DEVICE_IDENTIFICATION = ?, NAME = ? WHERE ID = ?";

        try (PreparedStatement preparedStatement = conn.prepareStatement(updateQuery)) {
            for (int deviceId : deviceIds) {

                String randomIdentification = generateRandomString(10);
                String randomName = generateRandomString(20);

                preparedStatement.setString(1, randomIdentification);
                preparedStatement.setString(2, randomName);
                preparedStatement.setInt(3, deviceId);

                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            String msg = "SQL error occurred while refactoring device properties of deviceIds: " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }


    /***
     * This method refactors some attributes of a given list of devices in the DM_ENROLMENT table
     * @param conn Connection object
     * @param deviceIds list of device ids (primary keys)
     * @throws DeviceManagementDAOException if refactoring fails
     */
    public void refactorEnrolment(Connection conn, List<Integer> deviceIds) throws DeviceManagementDAOException {
        String updateQuery = "UPDATE DM_ENROLMENT SET OWNER = ?, STATUS = ? WHERE DEVICE_ID = ?";

        try (PreparedStatement preparedStatement = conn.prepareStatement(updateQuery)) {
            for (int deviceId : deviceIds) {

                String randomOwner = generateRandomString(4);

                preparedStatement.setString(1, randomOwner);
                preparedStatement.setString(2, String.valueOf(Status.DELETED));
                preparedStatement.setInt(3, deviceId);

                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            String msg = "SQL error occurred while refactoring device enrolment properties of deviceIds: " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomString = new StringBuilder();

        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            randomString.append(characters.charAt(index));
        }

        return randomString.toString();
    }

    /***
     * This method executes batch operations for a given list of primary keys
     * where the statement only has one param of type int, following the given pattern:
     *    DELETE FROM TABLE WHERE ID = ?
     *
     *    This method does not check if the number of rows affected by the executeBatch() method is 0
     *    because there can be tables with no records to delete.
     * @param sql SQL statement
     * @param conn Connection object
     * @param identifiers list of device ids (primary keys)
     * @throws SQLException if deletion fails.
     */
    private boolean executeBatchOperation(Connection conn, String sql, List<Integer> identifiers) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn.getMetaData().supportsBatchUpdates()) {
                for (int identifier : identifiers) {
                    ps.setInt(1, identifier);
                    ps.addBatch();
                }
                for (int i : ps.executeBatch()) {
                    if (i == Statement.SUCCESS_NO_INFO || i == Statement.EXECUTE_FAILED) {
                        return false;
                    }
                }
            } else {
                for (int identifier : identifiers) {
                    ps.setInt(1, identifier);
                    ps.executeUpdate();
                }
            }
        }
        return true;
    }

    private int getDeviceId(Connection conn, DeviceIdentifier deviceIdentifier, int tenantId)
            throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int deviceId = -1;
        try {
            String sql = "SELECT ID FROM DM_DEVICE WHERE DEVICE_IDENTIFICATION = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceIdentifier.getId());
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                deviceId = rs.getInt("ID");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving device id of the device", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return deviceId;
    }

    public boolean transferDevice(String deviceType, String deviceIdentifier, String owner, int destinationTenantId)
            throws DeviceManagementDAOException, SQLException {
        Connection conn = this.getConnection();
        int deviceId = getDeviceId(conn, new DeviceIdentifier(deviceIdentifier, deviceType), -1234);
        PreparedStatement stmt = null;
        try {
            String sql = "UPDATE DM_DEVICE SET TENANT_ID = ? WHERE ID = ? AND TENANT_ID = -1234";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, destinationTenantId);
            stmt.setInt(2, deviceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            conn.rollback();
            throw new DeviceManagementDAOException("Error occurred while removing device", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        try {
            String sql = "UPDATE DM_DEVICE_PROPERTIES SET TENANT_ID = ? " +
                    "WHERE DEVICE_TYPE_NAME = ? AND DEVICE_IDENTIFICATION = ? AND TENANT_ID = -1234";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, destinationTenantId);
            stmt.setString(2, deviceType);
            stmt.setString(3, deviceIdentifier);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        try {
            String sql = "UPDATE DM_ENROLMENT SET TENANT_ID = ?, OWNER = ? WHERE DEVICE_ID = ? AND TENANT_ID = -1234";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, destinationTenantId);
            stmt.setString(2, owner);
            stmt.setInt(3, deviceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return true;
    }

    protected String buildStatusQuery(List<String> statusList)
            throws DeviceManagementDAOException {
        if (statusList == null || statusList.isEmpty()) {
            String msg = "SQL query build for status list failed. Status list cannot be empty or null";
            log.error(msg);
            throw new DeviceManagementDAOException(msg);
        }
        StringJoiner joiner = new StringJoiner(",", " AND e.STATUS IN(", ")");
        statusList.stream().map(status -> "?").forEach(joiner::add);

        return joiner.toString();
    }

    public int getFunctioningDevicesInSystem() throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int deviceCount = 0;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(e.DEVICE_ID) AS DEVICE_COUNT FROM DM_ENROLMENT e WHERE STATUS = 'ACTIVE' " +
                    "OR STATUS = 'UNREACHABLE'";
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                deviceCount = rs.getInt("DEVICE_COUNT");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching count of functioning devices", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return deviceCount;
    }

    @Override
    public List<DeviceMonitoringData> getAllDevicesForMonitoring(
            int deviceTypeId, String deviceTypeName, int activeServerCount, int serverHashIndex)
            throws DeviceManagementDAOException {
        List<DeviceMonitoringData> devices = new ArrayList<>();

        String sql = "SELECT D.ID AS DEVICE_ID," +
                " D.NAME AS DEVICE_NAME, " +
                " D.DESCRIPTION AS DESCRIPTION," +
                " D.DEVICE_IDENTIFICATION, " +
                " D.LAST_UPDATED_TIMESTAMP, " +
                " D.TENANT_ID, " +
                " E.ENROLMENT_ID, " +
                " EN.OWNER, " +
                " EN.OWNERSHIP, " +
                " EN.IS_TRANSFERRED, " +
                " EN.DATE_OF_ENROLMENT, " +
                " EN.DATE_OF_LAST_UPDATE, " +
                " EN.STATUS " +
                "FROM DM_DEVICE D, DM_ENROLMENT EN," +
                " (SELECT DEVICE_ID, MAX(ID) AS ENROLMENT_ID" +
                " FROM DM_ENROLMENT" +
                " WHERE STATUS IN ('ACTIVE', 'UNREACHABLE') " +
                " GROUP BY DEVICE_ID) E" +
                " WHERE D.ID = E.DEVICE_ID AND E.ENROLMENT_ID = EN.ID AND D.DEVICE_TYPE_ID = ?";
        if (activeServerCount > 0) {
            sql += " AND MOD(D.ID, ?) = ?";
        }

        try {
            Connection conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, deviceTypeId);
                if (activeServerCount > 0) {
                    stmt.setInt(2, activeServerCount);
                    stmt.setInt(3, serverHashIndex);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        devices.add(DeviceManagementDAOUtil.loadDevice(rs, deviceTypeName));
                    }
                    return devices;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving devices";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public int getGroupedDevicesCount(PaginationRequest request, List<Integer> deviceIds, String groupName,
                                      int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            if (deviceIds.isEmpty()) {
                return 0;
            }
            int index = 1;
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT "
                            + "COUNT(DM_DEVICE_GROUP_MAP.DEVICE_ID) AS DEVICE_COUNT "
                            + "FROM DM_DEVICE_GROUP_MAP "
                            + "INNER JOIN DM_GROUP ON "
                            + "DM_DEVICE_GROUP_MAP.GROUP_ID = DM_GROUP.ID "
                            + "WHERE DM_DEVICE_GROUP_MAP.DEVICE_ID IN (",
                    ") AND DM_GROUP.TENANT_ID = ?");
            deviceIds.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            if (StringUtils.isNotBlank(groupName)) {
                query += " AND DM_GROUP.GROUP_NAME = ?";
            }

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (Integer deviceId : deviceIds) {
                    ps.setInt(index++, deviceId);
                }
                ps.setInt(index++, tenantId);
                if (StringUtils.isNotBlank(groupName)) {
                    ps.setString(index, groupName);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("DEVICE_COUNT");
                    }
                    return 0;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of all registered devices " +
                    "according to device ids and the limit area.";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<String> getOperators(int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<String> deviceOperators = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT DISTINCT(VALUE_FIELD) AS OPERATOR " +
                    "FROM DM_DEVICE_INFO i " +
                    "INNER JOIN DM_ENROLMENT e ON " +
                    "e.DEVICE_ID " +
                    "WHERE e.DEVICE_ID = i.DEVICE_ID " +
                    "AND KEY_FIELD = 'operator' " +
                    "AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String operator = rs.getString("OPERATOR");
                deviceOperators.add(operator);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while listing device operators.";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return deviceOperators;
    }

    @Override
    public List<String> getAgentVersions(int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<String> agentVersions = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT DISTINCT(VALUE_FIELD) AS AGENT_VERSION " +
                    "FROM DM_DEVICE_INFO i " +
                    "INNER JOIN DM_ENROLMENT e ON " +
                    "e.DEVICE_ID " +
                    "WHERE e.DEVICE_ID = i.DEVICE_ID " +
                    "AND KEY_FIELD = 'AGENT_VERSION' " +
                    "AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String agentVersion = rs.getString("AGENT_VERSION");
                agentVersions.add(agentVersion);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while listing agent versions.";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return agentVersions;
    }

    public List<Device> getDevicesEnrolledSince(Date since) throws DeviceManagementDAOException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<Device> devices = new ArrayList<>();
        String query = "SELECT e.ID AS ENROLMENT_ID, e.DEVICE_ID, e.OWNER, e.OWNERSHIP, e.DATE_OF_ENROLMENT, " +
                "e.DATE_OF_LAST_UPDATE, e.IS_TRANSFERRED, e.STATUS, d.DEVICE_NAME, d.DESCRIPTION, " +
                "d.LAST_UPDATED_TIMESTAMP, e.DEVICE_TYPE, " +
                "e.DEVICE_IDENTIFICATION " +
                "FROM DM_ENROLMENT e, (SELECT d1.ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME FROM DM_DEVICE d1 " +
                "WHERE d1.TENANT_ID = ?) d WHERE e.STATUS NOT IN ('DELETED', 'REMOVED') " +
                "AND e.DATE_OF_ENROLMENT > ? AND e.TENANT_ID = ?";
        try {
            Connection connection = DeviceManagementDAOFactory.getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, tenantId);
                preparedStatement.setTimestamp(2, new Timestamp(since.getTime()));
                preparedStatement.setInt(3, tenantId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Device device = DeviceManagementDAOUtil.loadDevice(resultSet);
                        device.setProperties(getDeviceProps(device.getDeviceIdentifier(), tenantId).getProperties());
                        devices.add(device);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving devices";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return devices;
    }

    public List<Device> getDevicesEnrolledPriorTo(Date priorTo) throws DeviceManagementDAOException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<Device> devices = new ArrayList<>();
        String query = "SELECT e.ID AS ENROLMENT_ID, e.DEVICE_ID, e.OWNER, e.OWNERSHIP, e.DATE_OF_ENROLMENT, " +
                "e.DATE_OF_LAST_UPDATE, e.IS_TRANSFERRED, e.STATUS, d.DEVICE_NAME, d.DESCRIPTION, " +
                "d.LAST_UPDATED_TIMESTAMP, e.DEVICE_TYPE, e.DEVICE_IDENTIFICATION " +
                "FROM DM_ENROLMENT e, (SELECT d1.ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME FROM DM_DEVICE d1 " +
                "WHERE d1.TENANT_ID = ?) d WHERE e.STATUS NOT IN ('DELETED', 'REMOVED') " +
                "AND e.DATE_OF_ENROLMENT < ? AND e.TENANT_ID = ?";
        try {
            Connection connection = DeviceManagementDAOFactory.getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, tenantId);
                preparedStatement.setTimestamp(2, new Timestamp(priorTo.getTime()));
                preparedStatement.setInt(3, tenantId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Device device = DeviceManagementDAOUtil.loadDevice(resultSet);
                        device.setProperties(getDeviceProps(device.getDeviceIdentifier(), tenantId).getProperties());
                        devices.add(device);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving devices";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return devices;
    }

    public abstract void refactorDeviceStatus (Connection conn, List<Device> validDevices)
            throws DeviceManagementDAOException;

    @Override
    public int getCountOfDevicesNotInGroup(PaginationRequest request, int tenantId) throws DeviceManagementDAOException {
        int deviceCount = 0;
        int groupId = request.getGroupId();
        String deviceType = request.getDeviceType();
        boolean isDeviceTypeProvided = false;
        String deviceName = request.getDeviceName();
        boolean isDeviceNameProvided = false;
        String owner = request.getOwner();
        boolean isOwnerProvided = false;
        String ownerPattern = request.getOwnerPattern();
        boolean isOwnerPatternProvided = false;
        String ownership = request.getOwnership();
        boolean isOwnershipProvided = false;
        List<String> statusList = request.getStatusList();
        boolean isStatusProvided = false;
        Date since = request.getSince();
        boolean isSinceProvided = false;

        try {
            Connection conn = getConnection();
            String sql = "SELECT COUNT(d1.DEVICE_ID) AS DEVICE_COUNT " +
                    "FROM DM_ENROLMENT e, " +
                    "(SELECT gd.ID AS DEVICE_ID, " +
                    "gd.DESCRIPTION, " +
                    "gd.NAME, " +
                    "gd.DEVICE_IDENTIFICATION " +
                    "FROM DM_DEVICE gd " +
                    "WHERE gd.ID NOT IN (SELECT dgm.DEVICE_ID " +
                    "FROM DM_DEVICE_GROUP_MAP dgm " +
                    "WHERE dgm.GROUP_ID = ?) " +
                    "AND gd.TENANT_ID = ?";

            if (deviceName != null && !deviceName.isEmpty()) {
                sql += " AND gd.NAME LIKE ?";
                isDeviceNameProvided = true;
            }
            sql += " AND 1=1";

            if (since != null) {
                sql += " AND gd.LAST_UPDATED_TIMESTAMP > ?";
                isSinceProvided = true;
            }
            sql += " ) d1 WHERE d1.DEVICE_ID = e.DEVICE_ID AND e.TENANT_ID = ?";

            if (deviceType != null && !deviceType.isEmpty()) {
                sql += " AND e.DEVICE_TYPE = ?";
                isDeviceTypeProvided = true;
            }

            if (ownership != null && !ownership.isEmpty()) {
                sql += " AND e.OWNERSHIP = ?";
                isOwnershipProvided = true;
            }

            if (owner != null && !owner.isEmpty()) {
                sql += " AND e.OWNER = ?";
                isOwnerProvided = true;
            } else if (ownerPattern != null && !ownerPattern.isEmpty()) {
                sql += " AND e.OWNER LIKE ?";
                isOwnerPatternProvided = true;
            }
            if (statusList != null && !statusList.isEmpty()) {
                sql += buildStatusQuery(statusList);
                isStatusProvided = true;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIdx = 1;
                stmt.setInt(paramIdx++, groupId);
                stmt.setInt(paramIdx++, tenantId);
                if (isDeviceNameProvided) {
                    stmt.setString(paramIdx++, "%" + deviceName + "%");
                }
                if (isSinceProvided) {
                    stmt.setTimestamp(paramIdx++, new Timestamp(since.getTime()));
                }
                stmt.setInt(paramIdx++, tenantId);
                if (isDeviceTypeProvided) {
                    stmt.setString(paramIdx++, deviceType);
                }
                if (isOwnershipProvided) {
                    stmt.setString(paramIdx++, ownership);
                }
                if (isOwnerProvided) {
                    stmt.setString(paramIdx++, owner);
                } else if (isOwnerPatternProvided) {
                    stmt.setString(paramIdx++, ownerPattern + "%");
                }
                if (isStatusProvided) {
                    for (String status : statusList) {
                        stmt.setString(paramIdx++, status);
                    }
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        deviceCount = rs.getInt("DEVICE_COUNT");
                    }
                    return deviceCount;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving count of devices not in group: " + groupId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> getDevicesNotInGivenIdList(PaginationRequest request, List<Integer> deviceIds, int tenantId)
            throws DeviceManagementDAOException {
        List<Integer> filteredDeviceIds = new ArrayList<>();
        try {
            Connection connection = getConnection();
            String sql = "SELECT ID AS DEVICE_ID FROM DM_DEVICE WHERE TENANT_ID = ?";

            if (deviceIds != null && !deviceIds.isEmpty()) {
                sql += " AND ID NOT IN ( " + deviceIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ")";
            }

            sql += " LIMIT ? OFFSET ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                int paraIdx = 1;
                preparedStatement.setInt(paraIdx++, tenantId);

                if (deviceIds != null && !deviceIds.isEmpty()) {
                    for (Integer deviceId : deviceIds) {
                        preparedStatement.setInt(paraIdx++, deviceId);
                    }
                }

                preparedStatement.setInt(paraIdx++, request.getRowCount());
                preparedStatement.setInt(paraIdx, request.getStartIndex());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        filteredDeviceIds.add(resultSet.getInt("DEVICE_ID"));
                    }
                }
                return filteredDeviceIds;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving device ids not in: " + filteredDeviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> getDevicesInGivenIdList(PaginationRequest request, List<Integer> deviceIds, int tenantId)
            throws DeviceManagementDAOException {
        List<Integer> filteredDeviceIds = new ArrayList<>();
        if (deviceIds == null || deviceIds.isEmpty()) return filteredDeviceIds;

        String deviceIdStringList = deviceIds.stream().map(id -> "?").collect(Collectors.joining(","));
        try {
            Connection connection = getConnection();
            String sql = "SELECT ID AS DEVICE_ID " +
                    "FROM DM_DEVICE " +
                    "WHERE ID IN (" + deviceIdStringList + ")" +
                    " AND TENANT_ID = ? " +
                    "LIMIT ? " +
                    "OFFSET ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                int paraIdx = 1;
                for (Integer deviceId : deviceIds) {
                    preparedStatement.setInt(paraIdx++, deviceId);
                }

                preparedStatement.setInt(paraIdx++, tenantId);
                preparedStatement.setInt(paraIdx++, request.getRowCount());
                preparedStatement.setInt(paraIdx, request.getStartIndex());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        filteredDeviceIds.add(resultSet.getInt("DEVICE_ID"));
                    }
                }
                return filteredDeviceIds;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving device ids in: " + filteredDeviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public int getDeviceCountNotInGivenIdList(List<Integer> deviceIds, int tenantId)
            throws DeviceManagementDAOException {
        int deviceCount = 0;
        try {
            Connection connection = getConnection();
            String sql = "SELECT COUNT(ID) AS COUNT " +
                    "FROM DM_DEVICE " +
                    "WHERE TENANT_ID = ?";

            if (deviceIds != null && !deviceIds.isEmpty()) {
                sql += " AND ID NOT IN ( " + deviceIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ")";
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                int paraIdx = 1;
                preparedStatement.setInt(paraIdx++, tenantId);

                if (deviceIds != null && !deviceIds.isEmpty()) {
                    for (Integer deviceId : deviceIds) {
                        preparedStatement.setInt(paraIdx++, deviceId);
                    }
                }

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        deviceCount = resultSet.getInt("COUNT");
                    }
                }
                return deviceCount;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving device count";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Device> getDevicesByDeviceIds(PaginationRequest paginationRequest, List<Integer> deviceIds, int tenantId)
            throws DeviceManagementDAOException {
        List<Device> devices = new ArrayList<>();
        if (deviceIds == null || deviceIds.isEmpty()) return devices;

        String deviceIdStringList = deviceIds.stream().map(id -> "?").collect(Collectors.joining(","));
        boolean isOwnerProvided = false;
        boolean isDeviceStatusProvided = false;
        boolean isDeviceNameProvided = false;
        try {
            Connection connection = getConnection();
            String sql = "SELECT e.DEVICE_ID, " +
                    "d.DEVICE_IDENTIFICATION, " +
                    "e.STATUS, " +
                    "e.OWNER, " +
                    "d.NAME AS DEVICE_NAME, " +
                    "e.DEVICE_TYPE, " +
                    "e.OWNERSHIP, " +
                    "e.DATE_OF_LAST_UPDATE " +
                    "FROM DM_DEVICE d " +
                    "INNER JOIN DM_ENROLMENT e " +
                    "ON d.ID = e.DEVICE_ID " +
                    "WHERE d.DEVICE_TYPE_ID = ? " +
                    "AND d.TENANT_ID = ? " +
                    "AND e.DEVICE_ID IN (" + deviceIdStringList+ ") " +
                    "AND e.STATUS NOT IN ('DELETED', 'REMOVED')";

            if (paginationRequest.getOwner() != null) {
                sql = sql + " AND e.OWNER LIKE ?";
                isOwnerProvided = true;
            }

            if (paginationRequest.getDeviceStatus() != null) {
                sql = sql + " AND e.STATUS = ?";
                isDeviceStatusProvided = true;
            }

            if (paginationRequest.getDeviceName() != null) {
                sql = sql + " AND d.NAME LIKE ?";
                isDeviceNameProvided = true;
            }

            sql = sql + " LIMIT ? OFFSET ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                int parameterIdx = 1;
                preparedStatement.setInt(parameterIdx++, paginationRequest.getDeviceTypeId());
                preparedStatement.setInt(parameterIdx++, tenantId);

                for (Integer deviceId : deviceIds) {
                    preparedStatement.setInt(parameterIdx++, deviceId);
                }

                if (isOwnerProvided)
                    preparedStatement.setString(parameterIdx++, "%" + paginationRequest.getOwner() + "%");
                if (isDeviceStatusProvided)
                    preparedStatement.setString(parameterIdx++, paginationRequest.getDeviceStatus());
                if (isDeviceNameProvided)
                    preparedStatement.setString(parameterIdx++, "%" + paginationRequest.getDeviceName() + "%");

                preparedStatement.setInt(parameterIdx++, paginationRequest.getRowCount());
                preparedStatement.setInt(parameterIdx, paginationRequest.getStartIndex());

                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    Device device;
                    while(resultSet.next()) {
                        device = new Device();
                        device.setId(resultSet.getInt("DEVICE_ID"));
                        device.setDeviceIdentifier(resultSet.getString("DEVICE_IDENTIFICATION"));
                        device.setName(resultSet.getString("DEVICE_NAME"));
                        device.setType(resultSet.getString("DEVICE_TYPE"));
                        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
                        enrolmentInfo.setStatus(EnrolmentInfo.Status.valueOf(resultSet.getString("STATUS")));
                        enrolmentInfo.setOwner(resultSet.getString("OWNER"));
                        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.valueOf(resultSet.getString("OWNERSHIP")));
                        enrolmentInfo.setDateOfLastUpdate(resultSet.getTimestamp("DATE_OF_LAST_UPDATE").getTime());
                        device.setEnrolmentInfo(enrolmentInfo);
                        devices.add(device);
                    }
                }
            }
            return devices;
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving devices for device ids in: " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    // todo: fix the join query
    @Override
    public int getDeviceCountByDeviceIds(PaginationRequest paginationRequest, List<Integer> deviceIds, int tenantId)
            throws DeviceManagementDAOException {
        int deviceCount = 0;
        if (deviceIds == null || deviceIds.isEmpty()) return deviceCount;

        String deviceIdStringList = deviceIds.stream().map(id -> "?").collect(Collectors.joining(","));
        boolean isOwnerProvided = false;
        boolean isDeviceStatusProvided = false;
        boolean isDeviceNameProvided = false;
        try {
            Connection connection = getConnection();
            String sql = "SELECT COUNT(DISTINCT e.DEVICE_ID) AS COUNT " +
                    "FROM DM_DEVICE d " +
                    "INNER JOIN DM_ENROLMENT e " +
                    "ON d.ID = e.DEVICE_ID " +
                    "WHERE e.TENANT_ID = ? " +
                    "AND e.DEVICE_ID IN (" + deviceIdStringList+ ") " +
                    "AND d.DEVICE_TYPE_ID = ? " +
                    "AND e.STATUS NOT IN ('DELETED', 'REMOVED')";

            if (paginationRequest.getOwner() != null) {
                sql = sql + " AND e.OWNER LIKE ?";
                isOwnerProvided = true;
            }

            if (paginationRequest.getDeviceStatus() != null) {
                sql = sql + " AND e.STATUS = ?";
                isDeviceStatusProvided = true;
            }

            if (paginationRequest.getDeviceName() != null) {
                sql = sql + " AND d.NAME LIKE ?";
                isDeviceNameProvided = true;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                int parameterIdx = 1;
                preparedStatement.setInt(parameterIdx++, tenantId);

                for (Integer deviceId : deviceIds) {
                    preparedStatement.setInt(parameterIdx++, deviceId);
                }

                preparedStatement.setInt(parameterIdx++, paginationRequest.getDeviceTypeId());
                if (isOwnerProvided)
                    preparedStatement.setString(parameterIdx++, "%" + paginationRequest.getOwner() + "%");
                if (isDeviceStatusProvided)
                    preparedStatement.setString(parameterIdx++, paginationRequest.getDeviceStatus());
                if (isDeviceNameProvided)
                    preparedStatement.setString(parameterIdx, "%" + paginationRequest.getDeviceName() + "%");

                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        deviceCount = resultSet.getInt("COUNT");
                    }
                }
            }
            return deviceCount;
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving device count for device ids in: " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

}
