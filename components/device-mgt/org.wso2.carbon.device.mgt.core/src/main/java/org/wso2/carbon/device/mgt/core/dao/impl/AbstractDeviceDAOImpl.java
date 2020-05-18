/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
/*
 *  Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *  Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.core.dao.impl;

import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo.Status;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.configuration.mgt.DevicePropertyInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceData;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocationHistorySnapshot;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.geo.GeoCluster;
import org.wso2.carbon.device.mgt.core.geo.geoHash.GeoCoordinate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

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
                    "WHERE DEVICE_TYPE_ID = (SELECT ID FROM DM_DEVICE_TYPE WHERE NAME = ? AND (PROVIDER_TENANT_ID = ? OR SHARED_WITH_ALL_TENANTS = ?)) " +
                    "AND DEVICE_IDENTIFICATION = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql, new String[]{"id"});
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
    public Device getDevice(DeviceData deviceData, int tenantId) throws DeviceManagementDAOException {
        Device device = null;
        try {
            Connection conn = this.getConnection();
            String sql = "SELECT "
                    + "d1.ID AS DEVICE_ID, "
                    + "d1.DESCRIPTION, "
                    + "d1.NAME AS DEVICE_NAME, "
                    + "d1.DEVICE_TYPE, "
                    + "d1.DEVICE_IDENTIFICATION, "
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
                    + "t.NAME AS DEVICE_TYPE, "
                    + "d.DEVICE_IDENTIFICATION "
                    + "FROM DM_DEVICE d, DM_DEVICE_TYPE t";

            if (deviceData.getLastModifiedDate() != null) {
                sql += ", DM_DEVICE_DETAIL dt";
            }

            sql += " WHERE "
                    + "t.NAME = ? AND "
                    + "t.ID = d.DEVICE_TYPE_ID AND "
                    + "d.DEVICE_IDENTIFICATION = ? AND "
                    + "d.TENANT_ID = ?";

            if (deviceData.getLastModifiedDate() != null) {
                sql += " AND dt.DEVICE_ID = d.ID AND dt.UPDATE_TIMESTAMP > ?";
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
                stmt.setString(paramIndx++, deviceData.getDeviceIdentifier().getType());
                stmt.setString(paramIndx++, deviceData.getDeviceIdentifier().getId());
                stmt.setInt(paramIndx++, tenantId);
                if (deviceData.getLastModifiedDate() != null) {
                    stmt.setLong(paramIndx++, deviceData.getLastModifiedDate().getTime());
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
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID, d.DESCRIPTION, d.NAME, " +
                    "t.NAME AS DEVICE_TYPE, d.DEVICE_IDENTIFICATION FROM DM_DEVICE d, DM_DEVICE_TYPE t WHERE " +
                    "t.NAME = ? AND t.ID = d.DEVICE_TYPE_ID AND d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ?) d1 WHERE d1.ID = e.DEVICE_ID " +
                    "AND TENANT_ID = ? ORDER BY e.DATE_OF_LAST_UPDATE DESC, e.STATUS ASC";
            // Status adeed as an orderby clause to fix a bug : when an existing device is
            // re-enrolled, earlier enrollment is marked as removed and a new enrollment is added.
            // However, both enrollments share the same time stamp. When retrieving the device
            // due to same timestamp, enrollment information is incorrect, intermittently. Hence
            // status also should be taken into consideration when ordering. This should not present a
            // problem for other status transitions, as there would be an intermediary removed
            // state in between.
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceIdentifier.getType());
            stmt.setString(2, deviceIdentifier.getId());
            stmt.setInt(3, tenantId);
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
                    "d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID " +
                    "FROM " +
                    "DM_ENROLMENT e," +
                    " (SELECT d.ID, d.DESCRIPTION, d.NAME, t.NAME AS DEVICE_TYPE, d.DEVICE_IDENTIFICATION " +
                    "FROM " +
                    "DM_DEVICE d, DM_DEVICE_TYPE t " +
                    "WHERE " +
                    "t.ID = d.DEVICE_TYPE_ID AND d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ?) d1 " +
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
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID, d.DESCRIPTION, d.NAME, " +
                    "t.NAME AS DEVICE_TYPE, d.DEVICE_IDENTIFICATION FROM DM_DEVICE d, DM_DEVICE_TYPE t WHERE " +
                    "t.NAME = ? AND t.ID = d.DEVICE_TYPE_ID AND d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ?) d1 WHERE d1.ID = e.DEVICE_ID " +
                    "AND TENANT_ID = ? AND e.OWNER = ? ORDER BY e.DATE_OF_LAST_UPDATE DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceIdentifier.getType());
            stmt.setString(2, deviceIdentifier.getId());
            stmt.setInt(3, tenantId);
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
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID, d.DESCRIPTION, d.NAME, " +
                    "t.NAME AS DEVICE_TYPE, d.DEVICE_IDENTIFICATION FROM DM_DEVICE d, DM_DEVICE_TYPE t, DM_DEVICE_DETAIL dt " +
                    "WHERE t.NAME = ? AND  t.ID = d.DEVICE_TYPE_ID AND d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ? AND dt.DEVICE_ID = d.ID " +
                    "AND dt.UPDATE_TIMESTAMP > ?) d1 WHERE d1.ID = e.DEVICE_ID AND TENANT_ID = ? ORDER BY e.DATE_OF_LAST_UPDATE DESC";
            stmt = conn.prepareStatement(sql);
            int paramIdx = 1;
            stmt.setString(paramIdx++, deviceIdentifier.getType());
            stmt.setString(paramIdx++, deviceIdentifier.getId());
            stmt.setInt(paramIdx++, tenantId);
            stmt.setLong(paramIdx++, since.getTime());
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
            List<List<DevicePropertyInfo>> outputLists = new ArrayList<>();
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
            for (Map.Entry<String, String> stringStringEntry : deviceProps.entrySet()) {
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
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID " +
                    "FROM" +
                    " DM_ENROLMENT e, " +
                    "(SELECT d.ID, d.DESCRIPTION, d.NAME, " +
                    "t.NAME AS DEVICE_TYPE, d.DEVICE_IDENTIFICATION " +
                    "FROM" +
                    " DM_DEVICE d, DM_DEVICE_TYPE t," +
                    " DM_DEVICE_DETAIL dt " +
                    "WHERE " +
                    "t.ID = d.DEVICE_TYPE_ID AND d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ? AND" +
                    " dt.DEVICE_ID = d.ID AND dt.UPDATE_TIMESTAMP > ?) d1 " +
                    "WHERE" +
                    " d1.ID = e.DEVICE_ID AND TENANT_ID = ? " +
                    "ORDER BY " +
                    "e.DATE_OF_LAST_UPDATE DESC";
            stmt = conn.prepareStatement(sql);
            int paramIdx = 1;
            stmt.setString(paramIdx++, deviceIdentifier);
            stmt.setInt(paramIdx++, tenantId);
            stmt.setLong(paramIdx++, since.getTime());
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
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID, d.DESCRIPTION, d.NAME, " +
                    "t.NAME AS DEVICE_TYPE, d.DEVICE_IDENTIFICATION FROM DM_DEVICE d, DM_DEVICE_TYPE t, DM_DEVICE_DETAIL dt " +
                    "WHERE t.NAME = ? AND t.ID = d.DEVICE_TYPE_ID AND d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ? AND dt.DEVICE_ID = d.ID " +
                    "AND dt.UPDATE_TIMESTAMP > ?) d1 WHERE d1.ID = e.DEVICE_ID AND TENANT_ID = ? AND e.OWNER = ? ORDER BY e.DATE_OF_LAST_UPDATE DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceIdentifier.getType());
            stmt.setString(2, deviceIdentifier.getId());
            stmt.setInt(3, tenantId);
            stmt.setLong(4, since.getTime());
            stmt.setInt(5, tenantId);
            stmt.setString(6, owner);
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
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID, d.DESCRIPTION, d.NAME, " +
                    "t.NAME AS DEVICE_TYPE, d.DEVICE_IDENTIFICATION FROM DM_DEVICE d, DM_DEVICE_TYPE t WHERE " +
                    "t.NAME = ? AND t.ID = d.DEVICE_TYPE_ID AND d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ?) d1 WHERE d1.ID = e.DEVICE_ID " +
                    "AND TENANT_ID = ? AND e.STATUS = ? ORDER BY e.DATE_OF_LAST_UPDATE DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceIdentifier.getType());
            stmt.setString(2, deviceIdentifier.getId());
            stmt.setInt(3, tenantId);
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
                    "d1.DEVICE_TYPE, " +
                    "e.TENANT_ID, " +
                    "d1.DEVICE_IDENTIFICATION, " +
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
                    "t.NAME AS DEVICE_TYPE, " +
                    "d.DEVICE_IDENTIFICATION " +
                    "FROM DM_DEVICE d, " +
                    "DM_DEVICE_TYPE t " +
                    "WHERE t.NAME = ? " +
                    "AND t.ID = d.DEVICE_TYPE_ID " +
                    "AND d.DEVICE_IDENTIFICATION = ?) d1 " +
                    "WHERE d1.ID = e.DEVICE_ID " +
                    "ORDER BY e.DATE_OF_LAST_UPDATE DESC";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, deviceIdentifier.getType());
                stmt.setString(2, deviceIdentifier.getId());

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
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID, d.DESCRIPTION, d.NAME, " +
                    "t.NAME AS DEVICE_TYPE, d.DEVICE_IDENTIFICATION FROM DM_DEVICE d, DM_DEVICE_TYPE t WHERE " +
                    "d.ID = ? AND t.ID = d.DEVICE_TYPE_ID AND d.TENANT_ID = ?) d1 WHERE d1.ID = e.DEVICE_ID " +
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
            String sql = "SELECT d1.DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID AS DEVICE_ID, " +
                    "d.DESCRIPTION, d.NAME, d.DEVICE_IDENTIFICATION, t.NAME AS DEVICE_TYPE FROM DM_DEVICE d, " +
                    "DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ?) d1 " +
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
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID, d.DESCRIPTION, " +
                    "d.NAME, d.DEVICE_IDENTIFICATION, t.NAME AS DEVICE_TYPE FROM DM_DEVICE d, " +
                    "DM_DEVICE_TYPE t WHERE DEVICE_TYPE_ID = t.ID AND t.NAME = ? " +
                    "AND t.ID = d.DEVICE_TYPE_ID AND d.TENANT_ID = ?) d1 WHERE d1.ID = e.DEVICE_ID AND TENANT_ID = ?" +
                    " ORDER BY e.DATE_OF_LAST_UPDATE DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, type);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            devices = new ArrayList<>();
            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadActiveDevice(rs, false);
                if (device != null) {
                    devices.add(device);
                }
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while listing devices for type '" + type + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
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
            String sql = "SELECT e1.OWNER, e1.OWNERSHIP, e1.ENROLMENT_ID, e1.DEVICE_ID, e1.STATUS, e1.IS_TRANSFERRED, e1.DATE_OF_LAST_UPDATE," +
                    " e1.DATE_OF_ENROLMENT, d.DESCRIPTION, d.NAME AS DEVICE_NAME, d.DEVICE_IDENTIFICATION, t.NAME " +
                    "AS DEVICE_TYPE FROM DM_DEVICE d, (SELECT e.OWNER, e.OWNERSHIP, e.ID AS ENROLMENT_ID, " +
                    "e.DEVICE_ID, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT FROM DM_ENROLMENT e WHERE " +
                    "e.TENANT_ID = ? AND LOWER(e.OWNER) = LOWER(?)) e1, DM_DEVICE_TYPE t WHERE d.ID = e1.DEVICE_ID " +
                    "AND t.ID = d.DEVICE_TYPE_ID ORDER BY e1.DATE_OF_LAST_UPDATE DESC";
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
            String sql = "SELECT e1.OWNER, e1.OWNERSHIP, e1.ENROLMENT_ID, e1.DEVICE_ID, e1.STATUS, e1.IS_TRANSFERRED, e1.DATE_OF_LAST_UPDATE," +
                    " e1.DATE_OF_ENROLMENT, d.DESCRIPTION, d.NAME AS DEVICE_NAME, d.DEVICE_IDENTIFICATION, t.NAME " +
                    "AS DEVICE_TYPE FROM DM_DEVICE d, (SELECT e.OWNER, e.OWNERSHIP, e.ID AS ENROLMENT_ID, " +
                    "e.DEVICE_ID, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT FROM DM_ENROLMENT e WHERE " +
                    "e.TENANT_ID = ? AND LOWER(e.OWNER) = LOWER(?) ORDER BY e.DATE_OF_LAST_UPDATE DESC) e1, DM_DEVICE_TYPE t WHERE d.ID = e1.DEVICE_ID " +
                    "AND t.ID = d.DEVICE_TYPE_ID AND t.NAME= ?";
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
                    "gd.DEVICE_IDENTIFICATION, " +
                    "t.NAME AS DEVICE_TYPE " +
                    "FROM " +
                    "(SELECT d.ID AS DEVICE_ID, " +
                    "d.DESCRIPTION,  " +
                    "d.NAME, " +
                    "d.DEVICE_IDENTIFICATION, " +
                    "d.DEVICE_TYPE_ID " +
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
            sql = sql + ") gd, DM_DEVICE_TYPE t";
            if (since != null) {
                sql = sql + ", DM_DEVICE_DETAIL dt";
                isSinceProvided = true;
            }
            sql = sql + " WHERE gd.DEVICE_TYPE_ID = t.ID";
            //Add query for last updated timestamp
            if (isSinceProvided) {
                sql = sql + " AND dt.DEVICE_ID = gd.DEVICE_ID AND dt.UPDATE_TIMESTAMP > ?";
            }
            //Add the query for device-type
            if (deviceType != null && !deviceType.isEmpty()) {
                sql = sql + " AND t.NAME = ?";
                isDeviceTypeProvided = true;
            }
            sql = sql + " ) d1 WHERE  d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ? ";
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
                    stmt.setString(paramIdx++, deviceName + "%");
                }
                if (isSinceProvided) {
                    stmt.setLong(paramIdx++, since.getTime());
                }
                if (isDeviceTypeProvided) {
                    stmt.setString(paramIdx++, deviceType);
                }
                stmt.setInt(paramIdx++, tenantId);
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

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }

    /**
     * Get device count of user.
     *
     * @return device count
     * @throws DeviceManagementDAOException
     */
    @Override
    public int getDeviceCount(String username, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int deviceCount = 0;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(d1.DEVICE_ID) AS DEVICE_COUNT FROM DM_ENROLMENT e, (SELECT d.ID AS DEVICE_ID FROM " +
                    "DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ?) d1 WHERE " +
                    "d1.DEVICE_ID = e.DEVICE_ID AND LOWER(e.OWNER) = LOWER(?) AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, username);
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

    /**
     * Get device count of user.
     *
     * @return device count
     * @throws DeviceManagementDAOException
     */
    @Override
    public int getDeviceCount(String type, String status, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int deviceCount = 0;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(d.ID) AS DEVICE_COUNT FROM (SELECT e.DEVICE_ID FROM DM_ENROLMENT e WHERE " +
                    "TENANT_ID = ? AND STATUS = ?) e, DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.ID = e.DEVICE_ID AND " +
                    "d.DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ? AND t.NAME=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, status);
            stmt.setInt(3, tenantId);
            stmt.setString(4, type);
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
            String sql = "SELECT d.DEVICE_IDENTIFICATION AS DEVICE_IDS FROM (SELECT e.DEVICE_ID FROM DM_ENROLMENT e WHERE " +
                    "TENANT_ID = ? AND STATUS = ?) e, DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.ID = e.DEVICE_ID AND " +
                    "d.DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ? AND t.NAME=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, status);
            stmt.setInt(3, tenantId);
            stmt.setString(4, type);
            rs = stmt.executeQuery();
            while (rs.next()) {
                deviceIDs.add(rs.getString("DEVICE_IDS"));
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
            conn = this.getConnection();
            StringBuilder sql = new StringBuilder("UPDATE DM_ENROLMENT SET STATUS = ? WHERE DEVICE_ID IN " +
                    "(SELECT d.ID FROM DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID AND d.DEVICE_IDENTIFICATION IN (");
            for (int i = 0; i < devices.size(); i++) {
                sql.append("?,");
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") AND t.NAME = ? AND d.TENANT_ID = ?) AND TENANT_ID = ?");
            stmt = conn.prepareStatement(sql.toString());
            stmt.setString(1, status);
            int index = 1;
            for (String device : devices) {
                stmt.setString(++index, device);
            }
            stmt.setString(++index, deviceType);
            stmt.setInt(++index, tenantId);
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
     * @throws DeviceManagementDAOException
     */
    @Override
    public int getDeviceCount(int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int deviceCount = 0;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(d1.DEVICE_ID) AS DEVICE_COUNT FROM DM_ENROLMENT e, (SELECT d.ID AS DEVICE_ID FROM " +
                    "DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ?) d1 WHERE " +
                    "d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
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
                    "d.DEVICE_IDENTIFICATION, " +
                    "t.NAME AS DEVICE_TYPE " +
                    "FROM " +
                    "DM_DEVICE d, " +
                    "DM_DEVICE_TYPE t";
            //Add query for last updated timestamp
            if (since != null) {
                sql = sql + " , DM_DEVICE_DETAIL dt";
                isSinceProvided = true;
            }
            sql = sql + " WHERE DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ?";
            //Add query for last updated timestamp
            if (isSinceProvided) {
                sql = sql + " AND dt.DEVICE_ID = d.ID AND dt.UPDATE_TIMESTAMP > ?";
            }
            if (deviceType != null && !deviceType.isEmpty()) {
                sql = sql + " AND t.NAME = ?";
                isDeviceTypeProvided = true;
            }
            if (deviceName != null && !deviceName.isEmpty()) {
                sql = sql + " AND d.NAME LIKE ?";
                isDeviceNameProvided = true;
            }
            sql = sql + ") d1 WHERE d1.ID = e.DEVICE_ID AND TENANT_ID = ?";
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
                stmt.setInt(paramIdx++, tenantId);
                if (isSinceProvided) {
                    stmt.setLong(paramIdx++, since.getTime());
                }
                if (isDeviceTypeProvided) {
                    stmt.setString(paramIdx++, request.getDeviceType());
                }
                if (isDeviceNameProvided) {
                    stmt.setString(paramIdx++, request.getDeviceName() + "%");
                }
                stmt.setInt(paramIdx++, tenantId);
                if (isOwnershipProvided) {
                    stmt.setString(paramIdx++, request.getOwnership());
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
            String sql = "SELECT COUNT(d1.ID) AS DEVICE_COUNT FROM DM_ENROLMENT e, (SELECT d.ID FROM DM_DEVICE d, " +
                    "DM_DEVICE_TYPE t WHERE DEVICE_TYPE_ID = t.ID AND t.NAME = ? " +
                    "AND d.TENANT_ID = ?) d1 WHERE d1.ID = e.DEVICE_ID AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, type);
            stmt.setInt(2, tenantId);
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

    @Override
    public int getDeviceCountByUser(String username, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int deviceCount = 0;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(e1.DEVICE_ID) AS DEVICE_COUNT FROM DM_DEVICE d, (SELECT e.DEVICE_ID " +
                    "FROM DM_ENROLMENT e WHERE e.TENANT_ID = ? AND LOWER(e.OWNER) = LOWER(?)) " +
                    "e1, DM_DEVICE_TYPE t WHERE d.ID = e1.DEVICE_ID AND t.ID = d.DEVICE_TYPE_ID";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, username);
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
            String sql = "SELECT COUNT(d1.ID) AS DEVICE_COUNT FROM DM_ENROLMENT e, (SELECT d.ID FROM DM_DEVICE d, " +
                    "DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID AND d.NAME LIKE ? AND d.TENANT_ID = ?) d1 " +
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
            String sql = "SELECT COUNT(d.ID) AS DEVICE_COUNT FROM (SELECT e.DEVICE_ID FROM DM_ENROLMENT e WHERE " +
                    "TENANT_ID = ? AND OWNERSHIP = ?) e, DM_DEVICE d, " +
                    "DM_DEVICE_TYPE t WHERE d.ID = e.DEVICE_ID AND d.DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, ownerShip);
            stmt.setInt(3, tenantId);
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
            String sql = "SELECT COUNT(d.ID) AS DEVICE_COUNT FROM (SELECT e.DEVICE_ID FROM DM_ENROLMENT e WHERE " +
                    "TENANT_ID = ? AND STATUS = ?) e, DM_DEVICE d, " +
                    "DM_DEVICE_TYPE t WHERE d.ID = e.DEVICE_ID AND d.DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, status);
            stmt.setInt(3, tenantId);
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
            String sql = "SELECT COUNT(d.ID) AS DEVICE_COUNT FROM (SELECT e.DEVICE_ID FROM DM_ENROLMENT e WHERE " +
                    "TENANT_ID = ? AND STATUS = ?) e, DM_DEVICE d, " +
                    "DM_DEVICE_TYPE t WHERE t.NAME = ? AND d.ID = e.DEVICE_ID AND d.DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, status);
            stmt.setString(3, deviceType);
            stmt.setInt(4, tenantId);
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
            String sql = "INSERT INTO DM_ENROLMENT(DEVICE_ID, OWNER, OWNERSHIP, STATUS, IS_TRANSFERRED, " +
                    "DATE_OF_ENROLMENT, DATE_OF_LAST_UPDATE, TENANT_ID) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, new String[]{"id"});
            stmt.setInt(1, device.getId());
            stmt.setString(2, device.getEnrolmentInfo().getOwner());
            stmt.setString(3, device.getEnrolmentInfo().getOwnership().toString());
            stmt.setString(4, device.getEnrolmentInfo().getStatus().toString());
            stmt.setBoolean(5, device.getEnrolmentInfo().isTransferred());
            stmt.setTimestamp(6, new Timestamp(new Date().getTime()));
            stmt.setTimestamp(7, new Timestamp(new Date().getTime()));
            stmt.setInt(8, tenantId);
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
            String sql = "UPDATE DM_ENROLMENT SET STATUS = ? WHERE DEVICE_ID = (SELECT d.ID FROM DM_DEVICE d, " +
                    "DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID AND d.DEVICE_IDENTIFICATION = ? " +
                    "AND t.NAME = ? AND d.TENANT_ID = ?) AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status.toString());
            stmt.setString(2, deviceId.getId());
            stmt.setString(3, deviceId.getType());
            stmt.setInt(4, tenantId);
            stmt.setString(5, currentOwner);
            stmt.setInt(6, tenantId);
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
            String sql = "SELECT STATUS FROM DM_ENROLMENT WHERE DEVICE_ID = (SELECT d.ID FROM DM_DEVICE d, " +
                    "DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID AND d.DEVICE_IDENTIFICATION = ? " +
                    "AND t.NAME = ? AND d.TENANT_ID = ?) AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId.getId());
            stmt.setString(2, deviceId.getType());
            stmt.setInt(3, tenantId);
            stmt.setString(4, currentOwner);
            stmt.setInt(5, tenantId);

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
                    + "DEVICE_ID = (SELECT d.ID " +
                    "FROM DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID " +
                    "AND d.DEVICE_IDENTIFICATION = ? AND t.NAME = ? AND d.TENANT_ID = ?) "
                    + "AND OWNER = ? ";

            if (!StringUtils.isBlank(request.getOwnership())) {
                sql += "AND OWNERSHIP = ? ";
            }
            sql += "AND TENANT_ID = ?";
            int paramIdx = 1;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(paramIdx++, deviceId.getId());
                stmt.setString(paramIdx++, deviceId.getType());
                stmt.setInt(paramIdx++, tenantId);
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
    public EnrolmentInfo getActiveEnrolment(DeviceIdentifier deviceId, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        EnrolmentInfo enrolmentInfo = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT ID AS ENROLMENT_ID, DEVICE_ID, OWNER, OWNERSHIP, STATUS, IS_TRANSFERRED, DATE_OF_ENROLMENT, " +
                    "DATE_OF_LAST_UPDATE, TENANT_ID FROM DM_ENROLMENT WHERE DEVICE_ID = (SELECT d.ID " +
                    "FROM DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID " +
                    "AND d.DEVICE_IDENTIFICATION = ? AND t.NAME = ? AND d.TENANT_ID = ?) " +
                    "AND TENANT_ID = ? AND STATUS in ('ACTIVE','UNREACHABLE','INACTIVE')";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId.getId());
            stmt.setString(2, deviceId.getType());
            stmt.setInt(3, tenantId);
            stmt.setInt(4, tenantId);
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
            String sql = "SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME AS DEVICE_NAME, t.NAME AS DEVICE_TYPE, " +
                    "d.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ENROLMENT_ID FROM (SELECT e.ID, e.DEVICE_ID, e.OWNER, e" +
                    ".OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_ENROLMENT, e.DATE_OF_LAST_UPDATE, e.ID AS " +
                    "ENROLMENT_ID FROM DM_ENROLMENT e WHERE TENANT_ID = ? AND STATUS = ?) e, DM_DEVICE d, DM_DEVICE_TYPE t " +
                    "WHERE DEVICE_ID = e.DEVICE_ID AND d.DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ?";
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
     * @param tenantId  Tenant id of the currently logged in user.
     * @return A collection of devices that have been updated after the provided timestamp
     * @throws DeviceManagementDAOException
     */
    public List<Device> getDevices(long timestamp, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID AS DEVICE_ID, " +
                    "d.DESCRIPTION, d.NAME, d.DEVICE_IDENTIFICATION, t.NAME AS DEVICE_TYPE, d.LAST_UPDATED_TIMESTAMP FROM DM_DEVICE d, " +
                    "DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ? AND d.LAST_UPDATED_TIMESTAMP < CURRENT_TIMESTAMP) d1 " +
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

    public List<GeoCluster> findGeoClusters(String deviceType, GeoCoordinate southWest, GeoCoordinate northEast,
                                            int geohashLength, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<GeoCluster> geoClusters = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT AVG(DEVICE_LOCATION.LATITUDE) AS LATITUDE,AVG(DEVICE_LOCATION.LONGITUDE) AS LONGITUDE," +
                    " MIN(DEVICE_LOCATION.LATITUDE) AS MIN_LATITUDE, MAX(DEVICE_LOCATION.LATITUDE) AS MAX_LATITUDE," +
                    " MIN(DEVICE_LOCATION.LONGITUDE) AS MIN_LONGITUDE," +
                    " MAX(DEVICE_LOCATION.LONGITUDE) AS MAX_LONGITUDE," +
                    " SUBSTRING(DEVICE_LOCATION.GEO_HASH,1,?) AS GEOHASH_PREFIX, COUNT(*) AS COUNT," +
                    " MIN(DEVICE.DEVICE_IDENTIFICATION) AS DEVICE_IDENTIFICATION," +
                    " MIN(DEVICE_TYPE.NAME) AS TYPE, " +
                    " MIN(DEVICE.LAST_UPDATED_TIMESTAMP) AS LAST_UPDATED_TIMESTAMP " +
                    "FROM DM_DEVICE_LOCATION AS DEVICE_LOCATION,DM_DEVICE AS DEVICE, DM_DEVICE_TYPE AS DEVICE_TYPE " +
                    "WHERE DEVICE_LOCATION.LATITUDE BETWEEN ? AND ? AND " +
                    "DEVICE_LOCATION.LONGITUDE BETWEEN ? AND ? AND " +
                    "DEVICE.TENANT_ID=? AND " +
                    "DEVICE.ID=DEVICE_LOCATION.DEVICE_ID  AND DEVICE.DEVICE_TYPE_ID=DEVICE_TYPE.ID";
            if (deviceType != null && !deviceType.isEmpty()) {
                sql += " AND DEVICE_TYPE.NAME=?";
            }
            sql += " GROUP BY GEOHASH_PREFIX";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, geohashLength);
            stmt.setDouble(2, southWest.getLatitude());
            stmt.setDouble(3, northEast.getLatitude());
            stmt.setDouble(4, southWest.getLongitude());
            stmt.setDouble(5, northEast.getLongitude());
            stmt.setDouble(6, tenantId);
            if (deviceType != null && !deviceType.isEmpty()) {
                stmt.setString(7, deviceType);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                double latitude = rs.getDouble("LATITUDE");
                double longitude = rs.getDouble("LONGITUDE");
                double min_latitude = rs.getDouble("MIN_LATITUDE");
                double max_latitude = rs.getDouble("MAX_LATITUDE");
                double min_longitude = rs.getDouble("MIN_LONGITUDE");
                double max_longitude = rs.getDouble("MAX_LONGITUDE");
                String device_identification = rs.getString("DEVICE_IDENTIFICATION");
                String device_type = rs.getString("TYPE");
                String last_seen = rs.getString("LAST_UPDATED_TIMESTAMP");
                long count = rs.getLong("COUNT");
                String geohashPrefix = rs.getString("GEOHASH_PREFIX");
                geoClusters.add(new GeoCluster(new GeoCoordinate(latitude, longitude),
                        new GeoCoordinate(min_latitude, min_longitude), new GeoCoordinate(max_latitude, max_longitude),
                        count, geohashPrefix, device_identification, device_type, last_seen));
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving information of  " +
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

            StringJoiner joiner = new StringJoiner(",",
                    "SELECT "
                            + "d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, "
                            + "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, "
                            + "e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID "
                            + "FROM "
                            + "DM_ENROLMENT e, "
                            + "(SELECT d.ID, d.DESCRIPTION, d.NAME, t.NAME AS DEVICE_TYPE, d.DEVICE_IDENTIFICATION "
                            + "FROM DM_DEVICE d, DM_DEVICE_TYPE t "
                            + "WHERE "
                            + "t.ID = d.DEVICE_TYPE_ID AND d.DEVICE_IDENTIFICATION IN (",
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
    public List<DeviceLocationHistorySnapshot> getDeviceLocationInfo(DeviceIdentifier deviceIdentifier, long from, long to)
            throws DeviceManagementDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<DeviceLocationHistorySnapshot> deviceLocationHistories = new ArrayList<>();
        try {
            conn = this.getConnection();

            String sql =
                    "SELECT DEVICE_ID, TENANT_ID, DEVICE_ID_NAME, DEVICE_TYPE_NAME, LATITUDE, LONGITUDE, SPEED, " +
                            "HEADING, TIMESTAMP, GEO_HASH, DEVICE_OWNER, DEVICE_ALTITUDE, DISTANCE " +
                            "FROM DM_DEVICE_HISTORY_LAST_SEVEN_DAYS " +
                            "WHERE DEVICE_ID_NAME = ? " +
                            "AND DEVICE_TYPE_NAME = ? " +
                            "AND TIMESTAMP >= ? " +
                            "AND TIMESTAMP <= ?";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceIdentifier.getId());
            stmt.setString(2, deviceIdentifier.getType());
            stmt.setLong(3, from);
            stmt.setLong(4, to);
            rs = stmt.executeQuery();
            while (rs.next()) {
                deviceLocationHistories.add(DeviceManagementDAOUtil.loadDeviceLocation(rs));
            }
        } catch (SQLException e) {
            String errMessage = "Error occurred while obtaining the DB connection to get device location information";
            log.error(errMessage, e);
            throw new DeviceManagementDAOException(errMessage, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return deviceLocationHistories;
    }

    @Override
    public void deleteDevices(List<String> deviceIdentifiers, List<Integer> deviceIds, List<Integer> enrollmentIds)
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
                removeDeviceEnrollment(conn, deviceIds);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully removed device enrollment data of devices: " + deviceIdentifiers);
                }
                removeDeviceGroupMapping(conn, deviceIds);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully removed device group mapping data of devices: " + deviceIdentifiers);
                }
                removeDevice(conn, deviceIds);
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
                "t.NAME AS DEVICE_TYPE, " +
                "d.DEVICE_IDENTIFICATION, " +
                "e.OWNER, " +
                "e.OWNERSHIP, " +
                "e.STATUS, " +
                "e.IS_TRANSFERRED, " +
                "e.DATE_OF_LAST_UPDATE, " +
                "e.DATE_OF_ENROLMENT, " +
                "e.ID AS ENROLMENT_ID " +
                "FROM DM_DEVICE AS d " +
                "INNER JOIN DM_ENROLMENT AS e ON d.ID = e.DEVICE_ID " +
                "INNER JOIN  DM_DEVICE_TYPE AS t ON d.DEVICE_TYPE_ID = t.ID " +
                "WHERE " +
                "t.NAME = ? AND e.TENANT_ID = ? AND d.ID " +
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
                "INNER JOIN  DM_DEVICE_TYPE AS t ON d.DEVICE_TYPE_ID = t.ID " +
                "WHERE " +
                "t.NAME = ? AND e.TENANT_ID = ? AND d.ID " +
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
            String sql = "" +
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
                    "t.NAME AS DEVICE_TYPE " +
                    "FROM dm_device d," +
                    "(SELECT e.owner," +
                    "e.ownership," +
                    "e.id AS ENROLMENT_ID," +
                    "e.device_id," +
                    "e.status, " +
                    "e.date_of_last_update, " +
                    "e.date_of_enrolment " +
                    "FROM dm_enrolment e " +
                    "INNER JOIN " +
                    "(SELECT DEVICE_ID " +
                    "FROM DM_DEVICE_INFO " +
                    "WHERE " +
                    "KEY_FIELD = 'encryptionEnabled' " +
                    "AND VALUE_FIELD = ?) AS di " +
                    "ON di.DEVICE_ID = e.DEVICE_ID " +
                    "WHERE e.tenant_id = ?) e1, " +
                    "dm_device_type t " +
                    "WHERE d.id = e1.device_id " +
                    "AND t.id = d.device_type_id " +
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
            String sql = "" +
                    "SELECT " +
                    "COUNT(e1.DEVICE_ID) AS DEVICE_COUNT " +
                    "FROM dm_device d," +
                    "(SELECT e.id AS ENROLMENT_ID, " +
                    "e.device_id " +
                    "FROM dm_enrolment e " +
                    "INNER JOIN " +
                    "(SELECT DEVICE_ID " +
                    "FROM DM_DEVICE_INFO " +
                    "WHERE KEY_FIELD = 'encryptionEnabled' " +
                    "AND VALUE_FIELD = ?) AS di " +
                    "ON di.DEVICE_ID = e.DEVICE_ID " +
                    "WHERE e.tenant_id = ?) e1, " +
                    "dm_device_type t " +
                    "WHERE d.id = e1.device_id " +
                    "AND t.id = d.device_type_id ";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, isEncrypted);
                ps.setInt(2, tenantId);

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
        String sql = "DELETE DM_DEVICE_OPERATION_RESPONSE_LARGE " +
                "FROM DM_DEVICE_OPERATION_RESPONSE_LARGE " +
                "INNER JOIN DM_DEVICE_OPERATION_RESPONSE ON DM_DEVICE_OPERATION_RESPONSE_LARGE.ID = " +
                "DM_DEVICE_OPERATION_RESPONSE.ID " +
                "WHERE ENROLMENT_ID = ?";
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
     * This method removes records of a given list of enrollments from the DM_ENROLMENT table
     * @param conn Connection object
     * @param enrollmentIds list of enrollment ids (primary keys)
     * @throws DeviceManagementDAOException if deletion fails
     */
    private void removeDeviceEnrollment(Connection conn, List<Integer> enrollmentIds)
            throws DeviceManagementDAOException {
        String sql = "DELETE FROM DM_ENROLMENT WHERE DEVICE_ID = ?";
        try {
            if (!executeBatchOperation(conn, sql, enrollmentIds)) {
                String msg = "Failed to remove enrollments of devices with enrollmentIds : " + enrollmentIds
                        + " while executing batch operation";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while removing enrollments of devices with enrollmentIds : "
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
     * This method removes records of a given list of devices from the DM_DEVICE table
     * @param conn Connection object
     * @param deviceIds list of device ids (primary keys)
     * @throws DeviceManagementDAOException if deletion fails
     */
    private void removeDevice(Connection conn, List<Integer> deviceIds) throws DeviceManagementDAOException {
        String sql = "DELETE FROM DM_DEVICE WHERE ID = ?";
        try {
            if (!executeBatchOperation(conn, sql, deviceIds)) {
                String msg = "Failed to remove devices with deviceIds : " + deviceIds + " while executing batch operation";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
        } catch (SQLException e) {
            String msg = "SQL error occurred while removing devices with deviceIds : " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
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
}
