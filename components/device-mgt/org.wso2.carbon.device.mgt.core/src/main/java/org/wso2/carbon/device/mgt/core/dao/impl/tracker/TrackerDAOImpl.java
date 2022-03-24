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
package org.wso2.carbon.device.mgt.core.dao.impl.tracker;

import org.wso2.carbon.device.mgt.common.TrackerDeviceInfo;
import org.wso2.carbon.device.mgt.common.TrackerGroupInfo;
import org.wso2.carbon.device.mgt.core.dao.TrackerManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.TrackerManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.TrackerDAO;
import org.wso2.carbon.device.mgt.core.dao.util.TrackerManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TrackerDAOImpl implements TrackerDAO {

    @Override
    public Boolean addTrackerDevice(int traccarDeviceId, int deviceId, int tenantId) throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_EXT_DEVICE_MAPPING(TRACCAR_DEVICE_ID, DEVICE_ID, TENANT_ID) VALUES(?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, traccarDeviceId);
            stmt.setInt(2, deviceId);
            stmt.setInt(3, tenantId);
            stmt.execute();

            return true;
        } catch (SQLException e) {
            throw new TrackerManagementDAOException("Error occurred while adding traccar device mapping", e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public Boolean updateTrackerDeviceIdANDStatus(int traccarDeviceId, int deviceId, int tenantId, int status) throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "UPDATE DM_EXT_DEVICE_MAPPING SET STATUS=?, TRACCAR_DEVICE_ID=? WHERE DEVICE_ID=? AND TENANT_ID=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, status);
            stmt.setInt(2, traccarDeviceId);
            stmt.setInt(3, deviceId);
            stmt.setInt(4, tenantId);
            stmt.execute();

            return true;
        } catch (SQLException e) {
            String msg = "Error occurred while updating trackerDevice mapping table";
            throw new TrackerManagementDAOException(msg, e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public int removeTrackerDevice(int deviceId, int tenantId) throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int status = -1;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_EXT_DEVICE_MAPPING WHERE DEVICE_ID = ? AND TENANT_ID = ? ";
            stmt = conn.prepareStatement(sql, new String[] {"id"});
            stmt.setInt(1, deviceId);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                status = 1;
            }
            return status;
        } catch (SQLException e) {
            throw new TrackerManagementDAOException("Error occurred while removing trackerDevice", e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public TrackerDeviceInfo getTrackerDevice(int deviceId, int tenantId) throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        TrackerDeviceInfo trackerDeviceInfo = null;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "SELECT ID, TRACCAR_DEVICE_ID, DEVICE_ID, TENANT_ID, STATUS FROM DM_EXT_DEVICE_MAPPING WHERE " +
                    "DEVICE_ID = ? AND TENANT_ID = ? ORDER BY ID DESC LIMIT 1";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                trackerDeviceInfo = this.loadTrackerDevice(rs);
            }
            return trackerDeviceInfo;
        } catch (SQLException e) {
            throw new TrackerManagementDAOException("Error occurred while retrieving the trackerDevice information ", e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public Boolean addTrackerGroup(int traccarGroupId, int groupId, int tenantId) throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        int status = 1 ;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_EXT_GROUP_MAPPING(TRACCAR_GROUP_ID, GROUP_ID, TENANT_ID, STATUS) VALUES(?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, traccarGroupId);
            stmt.setInt(2, groupId);
            stmt.setInt(3, tenantId);
            stmt.setInt(4, status);
            stmt.execute();

            return true;
        } catch (SQLException e) {
            String msg = "Error occurred while adding traccar group mapping";
            throw new TrackerManagementDAOException(msg, e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public Boolean updateTrackerGroupIdANDStatus(int traccarGroupId, int groupId, int tenantId, int status) throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "UPDATE DM_EXT_GROUP_MAPPING SET STATUS=?, TRACCAR_GROUP_ID=? WHERE GROUP_ID=? AND TENANT_ID=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, status);
            stmt.setInt(2, traccarGroupId);
            stmt.setInt(3, groupId);
            stmt.setInt(4, tenantId);
            stmt.execute();

            return true;
        } catch (SQLException e) {
            String msg = "Error occurred while updating traccar group mapping table";
            throw new TrackerManagementDAOException(msg, e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public int removeTrackerGroup(int id) throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int status = -1;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_EXT_GROUP_MAPPING WHERE ID = ? ";
            stmt = conn.prepareStatement(sql, new String[] {"id"});
            stmt.setInt(1, id);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                status = 1;
            }
            return status;
        } catch (SQLException e) {
            throw new TrackerManagementDAOException("Error occurred while removing traccar group", e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public TrackerGroupInfo getTrackerGroup(int groupId, int tenantId) throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        TrackerGroupInfo trackerGroupInfo = null;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "SELECT ID, TRACCAR_GROUP_ID, GROUP_ID, TENANT_ID, STATUS FROM DM_EXT_GROUP_MAPPING WHERE " +
                    "GROUP_ID = ? AND TENANT_ID = ? ORDER BY ID DESC LIMIT 1";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                trackerGroupInfo = this.loadTrackerGroup(rs);
            }
            return trackerGroupInfo;
        } catch (SQLException e) {
            throw new TrackerManagementDAOException("Error occurred while retrieving the traccar group information ", e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private TrackerGroupInfo loadTrackerGroup(ResultSet rs) throws SQLException {
        TrackerGroupInfo trackerGroupInfo = new TrackerGroupInfo();
        trackerGroupInfo.setId(rs.getInt("ID"));
        trackerGroupInfo.setTraccarGroupId(rs.getInt("TRACCAR_GROUP_ID"));
        trackerGroupInfo.setGroupId(rs.getInt("GROUP_ID"));
        trackerGroupInfo.setTenantId(rs.getInt("TENANT_ID"));
        trackerGroupInfo.setStatus(rs.getInt("STATUS"));
        return trackerGroupInfo;
    }

    private TrackerDeviceInfo loadTrackerDevice(ResultSet rs) throws SQLException {
        TrackerDeviceInfo trackerDeviceInfo = new TrackerDeviceInfo();
        trackerDeviceInfo.setId(rs.getInt("ID"));
        trackerDeviceInfo.setTraccarDeviceId(rs.getInt("TRACCAR_DEVICE_ID"));
        trackerDeviceInfo.setDeviceId(rs.getInt("DEVICE_ID"));
        trackerDeviceInfo.setTenantId(rs.getInt("TENANT_ID"));
        trackerDeviceInfo.setStatus(rs.getInt("STATUS"));
        return trackerDeviceInfo;
    }
}
