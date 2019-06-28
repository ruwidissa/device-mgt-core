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
 *   Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *   Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.device.mgt.core.dao.impl;

import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo.Status;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public abstract class AbstractDeviceDAOImpl implements DeviceDAO {

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(AbstractDeviceDAOImpl.class);

    private static final String PROPERTY_KEY_COLUMN_NAME = "PROPERTY_NAME";
    private static final String PROPERTY_VALUE_COLUMN_NAME = "PROPERTY_VALUE";
    private static final String PROPERTY_DEVICE_TYPE_NAME = "DEVICE_TYPE_NAME";
    private static final String PROPERTY_DEVICE_IDENTIFICATION = "DEVICE_IDENTIFICATION";

    @Override
    public int addDevice(int typeId, Device device, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int deviceId = -1;
        try {
            conn = this.getConnection();
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
    public Device getDevice(DeviceIdentifier deviceIdentifier, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Device device = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
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
                         "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
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
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID, d.DESCRIPTION, d.NAME, " +
                    "t.NAME AS DEVICE_TYPE, d.DEVICE_IDENTIFICATION FROM DM_DEVICE d, DM_DEVICE_TYPE t WHERE " +
                    "t.NAME = ? AND t.ID = d.DEVICE_TYPE_ID AND d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ?) d1 WHERE d1.ID = e.DEVICE_ID " +
                    "AND TENANT_ID = ? AND e.OWNER = ?";
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
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
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

    public List<Device> getDeviceBasedOnDeviceProperties(Map<String,String> deviceProps, int tenantId)
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
            for(String deviceId : deviceIds){
                devices.add(getDevice(deviceId, tenantId));
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
                         "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
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
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID, d.DESCRIPTION, d.NAME, " +
                    "t.NAME AS DEVICE_TYPE, d.DEVICE_IDENTIFICATION FROM DM_DEVICE d, DM_DEVICE_TYPE t, DM_DEVICE_DETAIL dt " +
                    "WHERE t.NAME = ? AND t.ID = d.DEVICE_TYPE_ID AND d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ? AND dt.DEVICE_ID = d.ID " +
                    "AND dt.UPDATE_TIMESTAMP > ?) d1 WHERE d1.ID = e.DEVICE_ID AND TENANT_ID = ? AND e.OWNER = ?";
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
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
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
    public HashMap<Integer, Device> getDevice(DeviceIdentifier deviceIdentifier) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Device device;
        HashMap<Integer, Device> deviceHashMap = new HashMap<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, e.TENANT_ID, " +
                        "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
                        "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID, d.DESCRIPTION, d.NAME, " +
                        "t.NAME AS DEVICE_TYPE, d.DEVICE_IDENTIFICATION FROM DM_DEVICE d, DM_DEVICE_TYPE t WHERE " +
                        "t.NAME = ? AND t.ID = d.DEVICE_TYPE_ID AND d.DEVICE_IDENTIFICATION = ? ) d1 WHERE d1.ID = e.DEVICE_ID ORDER BY e.DATE_OF_LAST_UPDATE DESC";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, deviceIdentifier.getType());
                stmt.setString(2, deviceIdentifier.getId());
            rs = stmt.executeQuery();
            if (rs.next()) {
                device = DeviceManagementDAOUtil.loadDevice(rs);
                deviceHashMap.put(rs.getInt("TENANT_ID"), device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while listing devices for type " +
                    "'" + deviceIdentifier.getType() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return deviceHashMap;
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
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
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
        List<Device> devices = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
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
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
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
            String sql = "SELECT e1.OWNER, e1.OWNERSHIP, e1.ENROLMENT_ID, e1.DEVICE_ID, e1.STATUS, e1.DATE_OF_LAST_UPDATE," +
                    " e1.DATE_OF_ENROLMENT, d.DESCRIPTION, d.NAME AS DEVICE_NAME, d.DEVICE_IDENTIFICATION, t.NAME " +
                    "AS DEVICE_TYPE FROM DM_DEVICE d, (SELECT e.OWNER, e.OWNERSHIP, e.ID AS ENROLMENT_ID, " +
                    "e.DEVICE_ID, e.STATUS, e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT FROM DM_ENROLMENT e WHERE " +
                    "e.TENANT_ID = ? AND LOWER(e.OWNER) = LOWER(?) ORDER BY e.DATE_OF_LAST_UPDATE DESC) e1, DM_DEVICE_TYPE t WHERE d.ID = e1.DEVICE_ID " +
                    "AND t.ID = d.DEVICE_TYPE_ID";
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
            String sql = "SELECT e1.OWNER, e1.OWNERSHIP, e1.ENROLMENT_ID, e1.DEVICE_ID, e1.STATUS, e1.DATE_OF_LAST_UPDATE," +
                    " e1.DATE_OF_ENROLMENT, d.DESCRIPTION, d.NAME AS DEVICE_NAME, d.DEVICE_IDENTIFICATION, t.NAME " +
                    "AS DEVICE_TYPE FROM DM_DEVICE d, (SELECT e.OWNER, e.OWNERSHIP, e.ID AS ENROLMENT_ID, " +
                    "e.DEVICE_ID, e.STATUS, e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT FROM DM_ENROLMENT e WHERE " +
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
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
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
        String status = request.getStatus();
        boolean isStatusProvided = false;
        Date since = request.getSince();
        boolean isSinceProvided = false;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(d1.ID) AS DEVICE_COUNT FROM DM_ENROLMENT e, (SELECT d.ID, d.NAME, d.DEVICE_IDENTIFICATION, " +
                    "t.NAME AS DEVICE_TYPE FROM DM_DEVICE d, DM_DEVICE_TYPE t";

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

            if (status != null && !status.isEmpty()) {
                sql = sql + " AND e.STATUS = ?";
                isStatusProvided = true;
            }

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            int paramIdx = 2;
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
                stmt.setString(paramIdx++, request.getStatus());
            }
            rs = stmt.executeQuery();
            if (rs.next()) {
                deviceCount = rs.getInt("DEVICE_COUNT");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving information of all " +
                    "registered devices", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return deviceCount;
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
            String sql = "INSERT INTO DM_ENROLMENT(DEVICE_ID, OWNER, OWNERSHIP, STATUS,DATE_OF_ENROLMENT, " +
                    "DATE_OF_LAST_UPDATE, TENANT_ID) VALUES(?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, new String[]{"id"});
            stmt.setInt(1, device.getId());
            stmt.setString(2, device.getEnrolmentInfo().getOwner());
            stmt.setString(3, device.getEnrolmentInfo().getOwnership().toString());
            stmt.setString(4, device.getEnrolmentInfo().getStatus().toString());
            stmt.setTimestamp(5, new Timestamp(new Date().getTime()));
            stmt.setTimestamp(6, new Timestamp(new Date().getTime()));
            stmt.setInt(7, tenantId);
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
    public EnrolmentInfo getEnrolment(DeviceIdentifier deviceId, String currentOwner,
                                      int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        EnrolmentInfo enrolmentInfo = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT ID AS ENROLMENT_ID, DEVICE_ID, OWNER, OWNERSHIP, STATUS, DATE_OF_ENROLMENT, " +
                    "DATE_OF_LAST_UPDATE, TENANT_ID FROM DM_ENROLMENT WHERE DEVICE_ID = (SELECT d.ID " +
                    "FROM DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID " +
                    "AND d.DEVICE_IDENTIFICATION = ? AND t.NAME = ? AND d.TENANT_ID = ?) " +
                    "AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId.getId());
            stmt.setString(2, deviceId.getType());
            stmt.setInt(3, tenantId);
            stmt.setString(4, currentOwner);
            stmt.setInt(5, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                enrolmentInfo = DeviceManagementDAOUtil.loadMatchingEnrolment(rs);
            }
            return enrolmentInfo;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the enrolment " +
                    "information of user '" + currentOwner + "' upon device '" + deviceId + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public EnrolmentInfo getActiveEnrolment(DeviceIdentifier deviceId, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        EnrolmentInfo enrolmentInfo = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT ID AS ENROLMENT_ID, DEVICE_ID, OWNER, OWNERSHIP, STATUS, DATE_OF_ENROLMENT, " +
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
                    "d.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ENROLMENT_ID FROM (SELECT e.ID, e.DEVICE_ID, e.OWNER, e" +
                    ".OWNERSHIP, e.STATUS, e.DATE_OF_ENROLMENT, e.DATE_OF_LAST_UPDATE, e.ID AS " +
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
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
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
                            + "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, "
                            + "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID "
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
                    if (rs.next()) {
                        Device device = DeviceManagementDAOUtil.loadDevice(rs);
                        devices.add(device);
                    }
                }
            }
            return devices;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while obtaining the DB connection when adding tags",
                    e);
        }
    }
    
    public void deleteDevice(DeviceIdentifier deviceIdentifier, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        try {
            conn = this.getConnection();
            int deviceId = getDeviceId(conn, deviceIdentifier, tenantId);
            if (deviceId == -1) {
                String msg = "Device " + deviceIdentifier.getId() + " of type " + deviceIdentifier.getType() +
                             " is not found";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            } else {
                int enrollmentId = getEnrollmentId(conn, deviceId, tenantId);
                if (enrollmentId == -1) {
                    String msg = "Enrollment not found for the device " + deviceIdentifier.getId() + " of type " +
                                 deviceIdentifier.getType();
                    log.error(msg);
                    throw new DeviceManagementDAOException(msg);
                } else {
                    removeDeviceDetail(conn, deviceId);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully removed device detail data");
                    }
                    removeDeviceLocation(conn, deviceId);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully removed device location data");
                    }
                    removeDeviceInfo(conn, deviceId);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully removed device info data");
                    }
                    removeDeviceNotification(conn, deviceId);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully removed device notification data");
                    }
                    removeDeviceApplicationMapping(conn, deviceId);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully removed device application mapping data");
                    }
                    removeDevicePolicyApplied(conn, deviceId);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully removed device applied policy data");
                    }
                    removeDevicePolicy(conn, deviceId);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully removed device policy data");
                    }
                    removeEnrollmentDeviceDetail(conn, enrollmentId);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully removed enrollment device detail data");
                    }
                    removeEnrollmentDeviceLocation(conn, enrollmentId);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully removed enrollment device location data");
                    }
                    removeEnrollmentDeviceInfo(conn, enrollmentId);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully removed enrollment device info data");
                    }
                    removeEnrollmentDeviceApplicationMapping(conn, enrollmentId);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully removed enrollment device application mapping data");
                    }
                    removeDeviceOperationResponse(conn, enrollmentId);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully removed device operation response data");
                    }
                    removeEnrollmentOperationMapping(conn, enrollmentId);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully removed enrollment operation mapping data");
                    }
                    removeDeviceEnrollment(conn, deviceId);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully removed device enrollment data");
                    }
                    removeDeviceGroupMapping(conn, deviceId);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully removed device group mapping data");
                    }
                    removeDevice(conn, deviceId);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully permanently deleted the device");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while deleting the device", e);
        }
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

    private int getEnrollmentId(Connection conn, int deviceId, int tenantId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int enrollmentId = -1;
        try {
            String sql = "SELECT ID FROM DM_ENROLMENT WHERE DEVICE_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                enrollmentId = rs.getInt("ID");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving enrollment id of the device", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return enrollmentId;
    }

    private void removeDeviceDetail(Connection conn, int deviceId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "DELETE FROM DM_DEVICE_DETAIL WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device detail", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private void removeDeviceLocation(Connection conn, int deviceId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "DELETE FROM DM_DEVICE_LOCATION WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device location", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private void removeDeviceInfo(Connection conn, int deviceId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "DELETE FROM DM_DEVICE_INFO WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device info", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private void removeDeviceNotification(Connection conn, int deviceId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "DELETE FROM DM_NOTIFICATION WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device notification", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private void removeDeviceApplicationMapping(Connection conn, int deviceId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "DELETE FROM DM_DEVICE_APPLICATION_MAPPING WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device application mapping", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private void removeDevicePolicyApplied(Connection conn, int deviceId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "DELETE FROM DM_DEVICE_POLICY_APPLIED WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device policy applied", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private void removeDevicePolicy(Connection conn, int deviceId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "DELETE FROM DM_DEVICE_POLICY WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device policy", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private void removeEnrollmentDeviceDetail(Connection conn, int enrollmentId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "DELETE FROM DM_DEVICE_DETAIL WHERE ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrollmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing enrollment device detail", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private void removeEnrollmentDeviceLocation(Connection conn, int enrollmentId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "DELETE FROM DM_DEVICE_LOCATION WHERE ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrollmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing enrollment device location", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private void removeEnrollmentDeviceInfo(Connection conn, int enrollmentId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "DELETE FROM DM_DEVICE_INFO WHERE ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrollmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing enrollment device info", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private void removeEnrollmentDeviceApplicationMapping(Connection conn, int enrollmentId)
            throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "DELETE FROM DM_DEVICE_APPLICATION_MAPPING WHERE ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrollmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing enrollment device application " +
                                                   "mapping", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private void removeDeviceOperationResponse(Connection conn, int enrollmentId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "DELETE FROM DM_DEVICE_OPERATION_RESPONSE WHERE ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrollmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device operation response", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private void removeEnrollmentOperationMapping(Connection conn, int enrollmentId)
            throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "DELETE FROM DM_ENROLMENT_OP_MAPPING WHERE ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrollmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing enrollment operation mapping", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private void removeDeviceEnrollment(Connection conn, int deviceId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "DELETE FROM DM_ENROLMENT WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device enrollment", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private void removeDeviceGroupMapping(Connection conn, int deviceId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "DELETE FROM DM_DEVICE_GROUP_MAP WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device group mapping", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private void removeDevice(Connection conn, int deviceId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "DELETE FROM DM_DEVICE WHERE ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }
}
