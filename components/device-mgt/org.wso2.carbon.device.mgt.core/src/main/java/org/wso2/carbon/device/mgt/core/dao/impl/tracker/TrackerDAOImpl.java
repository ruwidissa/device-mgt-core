/*
 *   Copyright (c) 2022, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.dao.impl.tracker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.TrackerDeviceInfo;
import org.wso2.carbon.device.mgt.common.TrackerGroupInfo;
import org.wso2.carbon.device.mgt.common.TrackerPermissionInfo;
import org.wso2.carbon.device.mgt.core.dao.TrackerManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.TrackerManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.TrackerDAO;
import org.wso2.carbon.device.mgt.core.dao.util.TrackerManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.traccar.common.TraccarHandlerConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TrackerDAOImpl implements TrackerDAO {

    private static final Log log = LogFactory.getLog(TrackerDAOImpl.class);

    @Override
    public void addTrackerDevice(int traccarDeviceId, int deviceId, int tenantId)
            throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_EXT_DEVICE_MAPPING(TRACCAR_DEVICE_ID, DEVICE_ID, TENANT_ID) VALUES(?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, traccarDeviceId);
            stmt.setInt(2, deviceId);
            stmt.setInt(3, tenantId);
            stmt.execute();
        } catch (SQLException e) {
            String msg = "Error occurred while adding on trackerDevice mapping table";
            log.error(msg, e);
            throw new TrackerManagementDAOException(msg, e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void updateTrackerDeviceIdANDStatus(int traccarDeviceId, int deviceId, int tenantId, int status)
            throws TrackerManagementDAOException {
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
        } catch (SQLException e) {
            String msg = "Error occurred while updating status on trackerDevice mapping table";
            log.error(msg, e);
            throw new TrackerManagementDAOException(msg, e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void removeTrackerDevice(int deviceId, int tenantId) throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_EXT_DEVICE_MAPPING WHERE DEVICE_ID = ? AND TENANT_ID = ? ";
            stmt = conn.prepareStatement(sql, new String[]{"id"});
            stmt.setInt(1, deviceId);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            String msg = "Error occurred while removing on trackerDevice table";
            log.error(msg, e);
            throw new TrackerManagementDAOException(msg, e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public TrackerDeviceInfo getTrackerDevice(int deviceId, int tenantId) throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "SELECT ID, TRACCAR_DEVICE_ID, DEVICE_ID, TENANT_ID, STATUS FROM DM_EXT_DEVICE_MAPPING WHERE " +
                    "DEVICE_ID = ? AND TENANT_ID = ? ORDER BY ID DESC LIMIT 1";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return TrackerManagementDAOUtil.loadTrackerDevice(rs);
            }
            return null;
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving data from the trackerDevice table ";
            log.error(msg, e);
            throw new TrackerManagementDAOException(msg, e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void addTrackerGroup(int traccarGroupId, int groupId, int tenantId)
            throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_EXT_GROUP_MAPPING(TRACCAR_GROUP_ID, GROUP_ID, TENANT_ID, STATUS) VALUES(?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, traccarGroupId);
            stmt.setInt(2, groupId);
            stmt.setInt(3, tenantId);
            stmt.setInt(4, 1);
            stmt.execute();
        } catch (SQLException e) {
            String msg = "Error occurred while adding on traccarGroup mapping table";
            log.error(msg, e);
            throw new TrackerManagementDAOException(msg, e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public Boolean updateTrackerGroupIdANDStatus(int traccarGroupId, int groupId, int tenantId, int status)
            throws TrackerManagementDAOException {
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
            String msg = "Error occurred while updating status on traccarGroup mapping table";
            log.error(msg, e);
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
            stmt = conn.prepareStatement(sql, new String[]{"id"});
            stmt.setInt(1, id);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                status = 1;
            }
            return status;
        } catch (SQLException e) {
            String msg = "Error occurred while removing from traccarGroup mapping table";
            log.error(msg, e);
            throw new TrackerManagementDAOException(msg, e);
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
                trackerGroupInfo = TrackerManagementDAOUtil.loadTrackerGroup(rs);
            }
            return trackerGroupInfo;
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving data from the traccarGroup mapping table ";
            log.error(msg, e);
            throw new TrackerManagementDAOException(msg, e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public Boolean addTrackerUserDevicePermission(int traccarUserId, int deviceId)
            throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_EXT_PERMISSION_MAPPING(TRACCAR_USER_ID, TRACCAR_DEVICE_ID) VALUES(?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, traccarUserId);
            stmt.setInt(2, deviceId);
            stmt.execute();

            return true;
        } catch (SQLException e) {
            String msg = "Error occurred while adding permission on permissions mapping table";
            log.error(msg, e);
            throw new TrackerManagementDAOException(msg, e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public Boolean removeTrackerUserDevicePermission(int deviceId, int userId, int removeType)
            throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_EXT_PERMISSION_MAPPING WHERE TRACCAR_DEVICE_ID = ?";
            // TODO: Recheck the usage of below if condition
            if (removeType != TraccarHandlerConstants.Types.REMOVE_TYPE_MULTIPLE) {
                sql = sql + " AND TRACCAR_USER_ID = ? ";
            }
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            if (removeType != TraccarHandlerConstants.Types.REMOVE_TYPE_MULTIPLE) {
                stmt.setInt(2, userId);
            }
            stmt.execute();
            return true;
        } catch (SQLException e) {
            String msg = "Error occurred while removing permission from permissions mapping table";
            log.error(msg, e);
            throw new TrackerManagementDAOException(msg, e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public List<TrackerPermissionInfo> getUserIdofPermissionByDeviceId(int deviceId)
            throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<TrackerPermissionInfo> trackerPermissionInfo = null;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "SELECT TRACCAR_DEVICE_ID, TRACCAR_USER_ID FROM DM_EXT_PERMISSION_MAPPING WHERE " +
                    "TRACCAR_DEVICE_ID = ? ORDER BY TRACCAR_DEVICE_ID ASC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            rs = stmt.executeQuery();
            trackerPermissionInfo = new ArrayList<>();
            while (rs.next()) {
                TrackerPermissionInfo loadPermission = TrackerManagementDAOUtil.loadPermission(rs);
                trackerPermissionInfo.add(loadPermission);
            }
            return trackerPermissionInfo;
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving permissions data from permissions mapping table ";
            log.error(msg, e);
            throw new TrackerManagementDAOException(msg, e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<TrackerPermissionInfo> getUserIdofPermissionByUserIdNIdList(int userId, List<Integer> NotInDeviceIdList)
            throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<TrackerPermissionInfo> trackerPermissionInfo = null;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "SELECT TRACCAR_DEVICE_ID, TRACCAR_USER_ID FROM DM_EXT_PERMISSION_MAPPING WHERE " +
                    "TRACCAR_USER_ID = ? ";
            if (NotInDeviceIdList != null && (!NotInDeviceIdList.isEmpty())) {
                sql += TrackerManagementDAOUtil.buildDeviceIdNotInQuery(NotInDeviceIdList);
            }
            sql += " ORDER BY TRACCAR_USER_ID ASC";

            stmt = conn.prepareStatement(sql);
            int paramIdx = 1;
            stmt.setInt(paramIdx++, userId);
            if (NotInDeviceIdList != null && (!NotInDeviceIdList.isEmpty())) {
                for (int id : NotInDeviceIdList) {
                    stmt.setInt(paramIdx++, id);
                }
            }
            rs = stmt.executeQuery();
            trackerPermissionInfo = new ArrayList<>();
            while (rs.next()) {
                TrackerPermissionInfo loadPermission = TrackerManagementDAOUtil.loadPermission(rs);
                trackerPermissionInfo.add(loadPermission);
            }
            return trackerPermissionInfo;
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving data from the permissions mapping table ";
            log.error(msg, e);
            throw new TrackerManagementDAOException(msg, e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public Boolean getUserIdofPermissionByDeviceIdNUserId(int deviceId, int userId) throws TrackerManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = TrackerManagementDAOFactory.getConnection();
            String sql = "SELECT TRACCAR_DEVICE_ID, TRACCAR_USER_ID FROM DM_EXT_PERMISSION_MAPPING WHERE " +
                    "TRACCAR_DEVICE_ID = ? AND TRACCAR_USER_ID = ? ORDER BY TRACCAR_DEVICE_ID DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, userId);

            rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving permissions data from permissions mapping table ";
            log.error(msg, e);
            throw new TrackerManagementDAOException(msg, e);
        } finally {
            TrackerManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

}
