/*
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.event.config.EventConfig;
import org.wso2.carbon.device.mgt.common.geo.service.GeofenceData;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.EventManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.GeofenceDAO;
import org.wso2.carbon.device.mgt.core.dto.event.config.GeoFenceGroupMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GeofenceDAOImpl implements GeofenceDAO {
    private static final Log log = LogFactory.getLog(GeofenceDAOImpl.class);
    @Override
    public GeofenceData saveGeofence(GeofenceData geofenceData) throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            String sql = "INSERT INTO DM_GEOFENCE(" +
                    "FENCE_NAME, " +
                    "DESCRIPTION, " +
                    "LATITUDE, " +
                    "LONGITUDE, " +
                    "RADIUS, " +
                    "GEO_JSON, " +
                    "FENCE_SHAPE, " +
                    "CREATED_TIMESTAMP, " +
                    "OWNER, " +
                    "TENANT_ID) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, geofenceData.getFenceName());
                stmt.setString(2, geofenceData.getDescription());
                stmt.setDouble(3, geofenceData.getLatitude());
                stmt.setDouble(4, geofenceData.getLongitude());
                stmt.setFloat(5, geofenceData.getRadius());
                stmt.setString(6, geofenceData.getGeoJson());
                stmt.setString(7, geofenceData.getFenceShape());
                stmt.setTimestamp(8, new Timestamp(new Date().getTime()));
                stmt.setString(9, geofenceData.getOwner());
                stmt.setInt(10, geofenceData.getTenantId());
                if (stmt.executeUpdate() > 0) {
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        geofenceData.setId(generatedKeys.getInt(1));
                    }
                }
                return geofenceData;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while creating Geofence for the tenant id "+geofenceData.getTenantId();
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public GeofenceData getGeofence(int fenceId) throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            GeofenceData geofenceData = null;
            String sql = "SELECT " +
                    "ID, " +
                    "FENCE_NAME, " +
                    "DESCRIPTION, " +
                    "LATITUDE, " +
                    "LONGITUDE, " +
                    "RADIUS, " +
                    "GEO_JSON, " +
                    "FENCE_SHAPE, " +
                    "OWNER, " +
                    "TENANT_ID " +
                    "FROM DM_GEOFENCE " +
                    "WHERE ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, fenceId);
                try (ResultSet rst = stmt.executeQuery()) {
                    List<GeofenceData> geofenceDataList = extractGeofenceData(rst);
                    if (!geofenceDataList.isEmpty()) {
                        geofenceData = geofenceDataList.get(0);
                    }
                }
            }
            return geofenceData;
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving Geofence with id "+fenceId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<GeofenceData> getGeoFencesOfTenant(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            boolean isNameProvided = false;
            List<GeofenceData> geofenceData;
            String sql = "SELECT " +
                    "ID, " +
                    "FENCE_NAME, " +
                    "DESCRIPTION, " +
                    "LATITUDE, " +
                    "LONGITUDE, " +
                    "RADIUS, " +
                    "GEO_JSON, " +
                    "FENCE_SHAPE, " +
                    "OWNER, " +
                    "TENANT_ID " +
                    "FROM DM_GEOFENCE " +
                    "WHERE TENANT_ID = ? ";

            if (request.getProperty(DeviceManagementConstants.GeoServices.FENCE_NAME) != null) {
                sql += "AND FENCE_NAME LIKE ?";
                isNameProvided = true;
            }
            sql += "LIMIT ? OFFSET ?";
            int index = 1;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(index++, tenantId);
                if (isNameProvided) {
                    stmt.setString(index++, request.getProperty(DeviceManagementConstants.GeoServices.FENCE_NAME).toString() + "%");
                }
                stmt.setInt(index++, request.getRowCount());
                stmt.setInt(index, request.getStartIndex());
                try (ResultSet rst = stmt.executeQuery()) {
                    geofenceData = extractGeofenceData(rst);
                }
            }
            return geofenceData;
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving Geofence of the tenant " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<GeofenceData> getGeoFencesOfTenant(String fenceName, int tenantId)
            throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            List<GeofenceData> geofenceData;
            String sql = "SELECT " +
                    "ID, " +
                    "FENCE_NAME, " +
                    "DESCRIPTION, " +
                    "LATITUDE, " +
                    "LONGITUDE, " +
                    "RADIUS, " +
                    "GEO_JSON, " +
                    "FENCE_SHAPE, " +
                    "OWNER, " +
                    "TENANT_ID " +
                    "FROM DM_GEOFENCE " +
                    "WHERE FENCE_NAME LIKE ?" +
                    "AND TENANT_ID = ? ";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, fenceName + "%");
                stmt.setInt(2, tenantId);
                try (ResultSet rst = stmt.executeQuery()) {
                    geofenceData = extractGeofenceData(rst);
                }
            }
            return geofenceData;
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving Geofence of the tenant " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<GeofenceData> getGeoFencesOfTenant(int tenantId)
            throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            List<GeofenceData> geofenceData;
            String sql = "SELECT " +
                    "ID, " +
                    "FENCE_NAME, " +
                    "DESCRIPTION, " +
                    "LATITUDE, " +
                    "LONGITUDE, " +
                    "RADIUS, " +
                    "GEO_JSON, " +
                    "FENCE_SHAPE, " +
                    "OWNER, " +
                    "TENANT_ID " +
                    "FROM DM_GEOFENCE " +
                    "WHERE TENANT_ID = ? ";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                try (ResultSet rst = stmt.executeQuery()) {
                    geofenceData = extractGeofenceData(rst);
                }
            }
            return geofenceData;
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving Geofence of the tenant " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public int deleteGeofenceById(int fenceId) throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            String sql = "DELETE FROM DM_GEOFENCE WHERE ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, fenceId);
                return stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting Geofence with ID " + fenceId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public int updateGeofence(GeofenceData geofenceData, int fenceId) throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            String sql = "UPDATE DM_GEOFENCE SET " +
                    "FENCE_NAME = ?, " +
                    "DESCRIPTION = ?, " +
                    "LATITUDE = ?, " +
                    "LONGITUDE = ?, " +
                    "RADIUS = ?, " +
                    "GEO_JSON = ?, " +
                    "FENCE_SHAPE = ? " +
                    "WHERE ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, geofenceData.getFenceName());
                stmt.setString(2, geofenceData.getDescription());
                stmt.setDouble(3, geofenceData.getLatitude());
                stmt.setDouble(4, geofenceData.getLongitude());
                stmt.setFloat(5, geofenceData.getRadius());
                stmt.setString(6, geofenceData.getGeoJson());
                stmt.setString(7, geofenceData.getFenceShape());
                stmt.setInt(8, fenceId);
                return stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating Geofence record with id " + fenceId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean createGeofenceGroupMapping(GeofenceData geofenceData, List<Integer> groupIds) throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            String sql = "INSERT INTO DM_GEOFENCE_GROUP_MAPPING(" +
                    "FENCE_ID, " +
                    "GROUP_ID) " +
                    "VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Integer groupId : groupIds) {
                    stmt.setInt(1, geofenceData.getId());
                    stmt.setInt(2, groupId);
                    stmt.addBatch();
                }
                return stmt.executeBatch().length > 0;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while creating geofence group mapping records";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    private Connection getConnection() throws SQLException {
        return EventManagementDAOFactory.getConnection();
    }

    private List<GeofenceData> extractGeofenceData(ResultSet rst) throws SQLException {
        List <GeofenceData> geofenceDataList = new ArrayList<>();
        while (rst.next()) {
            GeofenceData geofenceData = new GeofenceData();
            geofenceData.setId(rst.getInt("ID"));
            geofenceData.setFenceName(rst.getString("FENCE_NAME"));
            geofenceData.setDescription(rst.getString("DESCRIPTION"));
            geofenceData.setLatitude(rst.getDouble("LATITUDE"));
            geofenceData.setLongitude(rst.getDouble("LONGITUDE"));
            geofenceData.setRadius(rst.getFloat("RADIUS"));
            geofenceData.setGeoJson(rst.getString("GEO_JSON"));
            geofenceData.setFenceShape(rst.getString("FENCE_SHAPE"));
            geofenceData.setOwner(rst.getString("OWNER"));
            geofenceData.setTenantId(rst.getInt("TENANT_ID"));
            geofenceDataList.add(geofenceData);
        }
        return geofenceDataList;
    }

    @Override
    public List<Integer> getGroupIdsOfGeoFence(int fenceId) throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            String sql = "SELECT " +
                    "GROUP_ID " +
                    "FROM DM_GEOFENCE_GROUP_MAPPING " +
                    "WHERE FENCE_ID = ? ";
            List<Integer> groupIds = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, fenceId);
                try (ResultSet rst = stmt.executeQuery()) {
                    while (rst.next()) {
                        groupIds.add(rst.getInt(1));
                    }
                }
            }
            return groupIds;
        } catch (SQLException e) {
            String msg = "Error occurred while fetching group IDs of the fence " + fenceId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteGeofenceGroupMapping(List<Integer> groupIdsToDelete, int fenceId) throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            String sql = "DELETE FROM DM_GEOFENCE_GROUP_MAPPING WHERE GROUP_ID = ? AND FENCE_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Integer groupId : groupIdsToDelete) {
                    stmt.setInt(1, groupId);
                    stmt.setInt(2, fenceId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting Geofence group mapping records";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public void createGeofenceEventMapping(int fenceId, List<Integer> eventIds) throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            String sql = "INSERT INTO DM_GEOFENCE_EVENT_MAPPING(" +
                    "FENCE_ID, "+
                    "EVENT_ID) " +
                    "VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Integer createdEventId : eventIds) {
                    stmt.setInt(1, fenceId);
                    stmt.setInt(2, createdEventId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while creating geofence event group mapping records";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteGeofenceEventMapping(List<Integer> removedEventIdList) throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            String sql = "DELETE FROM DM_GEOFENCE_EVENT_MAPPING WHERE EVENT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Integer eventId : removedEventIdList) {
                    stmt.setInt(1, eventId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting Geofence event mapping records";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public Map<Integer, List<EventConfig>> getEventsOfGeoFences(List<Integer> geofenceIds) throws DeviceManagementDAOException {
        try {
            Map<Integer, List<EventConfig>> geoFenceEventMap = new HashMap<>();
            if (geofenceIds.isEmpty()) {
                return geoFenceEventMap;
            }
            Connection conn = this.getConnection();
            String sql = "SELECT " +
                    "E.ID AS EVENT_ID, " +
                    "M.FENCE_ID AS FENCE_ID, " +
                    "EVENT_SOURCE, " +
                    "EVENT_LOGIC, " +
                    "ACTIONS " +
                    "FROM DM_DEVICE_EVENT E, DM_GEOFENCE_EVENT_MAPPING M " +
                    "WHERE E.ID = M.EVENT_ID " +
                    "AND M.FENCE_ID IN (%s)";
            String inClause = String.join(", ", Collections.nCopies(geofenceIds.size(), "?"));
            sql = String.format(sql, inClause);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int index = 1;
                for (Integer geofenceId : geofenceIds) {
                    stmt.setInt(index++, geofenceId);
                }
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    int fenceId = resultSet.getInt("FENCE_ID");
                    List<EventConfig> eventConfigList = geoFenceEventMap.get(fenceId);
                    if (eventConfigList == null) {
                        eventConfigList = new ArrayList<>();
                    }
                    EventConfig event = new EventConfig();
                    event.setEventId(resultSet.getInt("EVENT_ID"));
                    event.setEventSource(resultSet.getString("EVENT_SOURCE"));
                    event.setEventLogic(resultSet.getString("EVENT_LOGIC"));
                    event.setActions(resultSet.getString("ACTIONS"));
                    eventConfigList.add(event);
                    geoFenceEventMap.put(fenceId, eventConfigList);
                }
                return geoFenceEventMap;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating Geofence record with id ";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<EventConfig> getEventsOfGeoFence(int geofenceId) throws DeviceManagementDAOException {
        try {
            List<EventConfig> eventList = new ArrayList<>();
            Connection conn = this.getConnection();
            String sql = "SELECT " +
                    "E.ID AS EVENT_ID, " +
                    "EVENT_SOURCE, " +
                    "EVENT_LOGIC, " +
                    "ACTIONS " +
                    "FROM DM_DEVICE_EVENT E, DM_GEOFENCE_EVENT_MAPPING G " +
                    "WHERE E.ID = G.EVENT_ID " +
                    "AND G.FENCE_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, geofenceId);
                return getEventConfigs(stmt);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating Geofence record with id ";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public Set<GeoFenceGroupMap> getGroupIdsOfGeoFences(List<Integer> fenceIds) throws DeviceManagementDAOException {
        try {
            Set<GeoFenceGroupMap> geoFenceGroupSet = new HashSet<>();
            if (fenceIds.isEmpty()) {
                return geoFenceGroupSet;
            }
            Connection conn = this.getConnection();
            String sql = "SELECT " +
                    "FENCE_ID, " +
                    "M.GROUP_ID, " +
                    "G.GROUP_NAME " +
                    "FROM DM_GEOFENCE_GROUP_MAPPING M, DM_GROUP G " +
                    "WHERE M.GROUP_ID = G.ID " +
                    "AND FENCE_ID IN (%s)";
            String inClause = String.join(", ", Collections.nCopies(fenceIds.size(), "?"));
            sql = String.format(sql, inClause);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int index = 1;
                for (Integer fenceId : fenceIds) {
                    stmt.setInt(index++, fenceId);
                }
                ResultSet rst = stmt.executeQuery();
                while (rst.next()) {
                    GeoFenceGroupMap geoFenceGroupMap = new GeoFenceGroupMap();
                    geoFenceGroupMap.setFenceId(rst.getInt("FENCE_ID"));
                    geoFenceGroupMap.setGroupId(rst.getInt("GROUP_ID"));
                    geoFenceGroupMap.setGroupName(rst.getString("GROUP_NAME"));
                    geoFenceGroupSet.add(geoFenceGroupMap);
                }
            }
            return geoFenceGroupSet;
        } catch (SQLException e) {
            String msg = "Error occurred while fetching group IDs of the fences";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /**
     * Retrieve the geofence event extracted from the DB
     * @param stmt prepared statement to retrieve data from the DB
     * @return Retrieved Event list from the DB
     * @throws SQLException for the errors occur while accessing the DB
     */
    private List<EventConfig> getEventConfigs(PreparedStatement stmt) throws SQLException {
        List<EventConfig> eventList = new ArrayList<>();
        ResultSet resultSet = stmt.executeQuery();
        EventConfig event;
        while (resultSet.next()) {
            event = new EventConfig();
            event.setEventId(resultSet.getInt("EVENT_ID"));
            event.setEventSource(resultSet.getString("EVENT_SOURCE"));
            event.setEventLogic(resultSet.getString("EVENT_LOGIC"));
            event.setActions(resultSet.getString("ACTIONS"));
            eventList.add(event);
        }
        return eventList;
    }

    @Override
    public List<GeofenceData> getGeoFences(int groupId, int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            String sql = "SELECT " +
                    "G.ID AS FENCE_ID, " +
                    "FENCE_NAME, " +
                    "DESCRIPTION, " +
                    "LATITUDE," +
                    "LONGITUDE, " +
                    "RADIUS, " +
                    "GEO_JSON, " +
                    "FENCE_SHAPE " +
                    "FROM DM_GEOFENCE G, DM_GEOFENCE_GROUP_MAPPING M " +
                    "WHERE M.GROUP_ID = ? AND TENANT_ID = ? " +
                    "GROUP BY G.ID";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, groupId);
                stmt.setInt(2, tenantId);
                ResultSet rst = stmt.executeQuery();
                List <GeofenceData> geofenceDataList = new ArrayList<>();
                while (rst.next()) {
                    GeofenceData geofenceData = new GeofenceData();
                    geofenceData.setId(rst.getInt("FENCE_ID"));
                    geofenceData.setFenceName(rst.getString("FENCE_NAME"));
                    geofenceData.setDescription(rst.getString("DESCRIPTION"));
                    geofenceData.setLatitude(rst.getDouble("LATITUDE"));
                    geofenceData.setLongitude(rst.getDouble("LONGITUDE"));
                    geofenceData.setRadius(rst.getFloat("RADIUS"));
                    geofenceData.setGeoJson(rst.getString("GEO_JSON"));
                    geofenceData.setFenceShape(rst.getString("FENCE_SHAPE"));
                    geofenceDataList.add(geofenceData);
                }
                return geofenceDataList;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving Geo fences of group " + groupId
                    + " and tenant " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public GeofenceData getGeofence(int fenceId, boolean requireGroupData) throws DeviceManagementDAOException {
        if (!requireGroupData) {
            return getGeofence(fenceId);
        }

        try {
            Connection con = this.getConnection();
            String sql = "SELECT " +
                    "G.ID AS FENCE_ID, " +
                    "FENCE_NAME, " +
                    "G.DESCRIPTION, " +
                    "LATITUDE, " +
                    "LONGITUDE, " +
                    "RADIUS, " +
                    "GEO_JSON, " +
                    "FENCE_SHAPE, " +
                    "M.GROUP_ID AS GROUP_ID, " +
                    "GR.GROUP_NAME " +
                    "FROM DM_GEOFENCE G, DM_GEOFENCE_GROUP_MAPPING M, DM_GROUP GR " +
                    "WHERE G.ID = M.FENCE_ID " +
                    "AND M.GROUP_ID = GR.ID " +
                    "AND G.ID = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)){
                stmt.setInt(1, fenceId);
                ResultSet rst = stmt.executeQuery();
                Map<Integer, String> groupMap = new HashMap<>();
                GeofenceData geofenceData = null;
                while (rst.next()) {
                    groupMap.put(rst.getInt("GROUP_ID"), rst.getString("GROUP_NAME"));
                    if (rst.isLast()) {
                        geofenceData = new GeofenceData();
                        geofenceData.setId(rst.getInt("FENCE_ID"));
                        geofenceData.setFenceName(rst.getString("FENCE_NAME"));
                        geofenceData.setDescription(rst.getString("DESCRIPTION"));
                        geofenceData.setLatitude(rst.getDouble("LATITUDE"));
                        geofenceData.setLongitude(rst.getDouble("LONGITUDE"));
                        geofenceData.setRadius(rst.getFloat("RADIUS"));
                        geofenceData.setGeoJson(rst.getString("GEO_JSON"));
                        geofenceData.setFenceShape(rst.getString("FENCE_SHAPE"));
                        geofenceData.setGroupData(groupMap);
                    }
                }
                return geofenceData;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving Geo fence data " + fenceId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }
}
