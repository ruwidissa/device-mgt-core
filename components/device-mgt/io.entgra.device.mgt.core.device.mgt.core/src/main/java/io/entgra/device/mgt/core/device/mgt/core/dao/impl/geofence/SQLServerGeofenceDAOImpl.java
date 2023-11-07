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

package io.entgra.device.mgt.core.device.mgt.core.dao.impl.geofence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.geo.service.GeofenceData;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.EventManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.impl.AbstractGeofenceDAOImpl;
import org.wso2.carbon.context.CarbonContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLServerGeofenceDAOImpl extends AbstractGeofenceDAOImpl {
    private static final Log log = LogFactory.getLog(SQLServerGeofenceDAOImpl.class);

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
            sql += "ORDER BY FENCE_NAME ";
            sql += "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            int index = 1;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(index++, tenantId);
                if (isNameProvided) {
                    stmt.setString(index++, "%" + request.getProperty(DeviceManagementConstants.GeoServices.FENCE_NAME).toString() + "%");
                }
                stmt.setInt(index++, request.getStartIndex());
                stmt.setInt(index, request.getRowCount());
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
    public GeofenceData getGeofence(int fenceId, boolean requireGroupData) throws DeviceManagementDAOException {
        if (!requireGroupData) {
            return getGeofence(fenceId);
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
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
                    "AND G.ID = ? " +
                    "AND G.TENANT_ID = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                stmt.setInt(1, fenceId);
                stmt.setInt(2, tenantId);
                try (ResultSet rst = stmt.executeQuery()) {
                    Map<Integer, String> groupMap = new HashMap<>();
                    GeofenceData geofenceData = null;
                    while (rst.next()) {
                        groupMap.put(rst.getInt("GROUP_ID"), rst.getString("GROUP_NAME"));
                    }
                    if (!groupMap.isEmpty()) {
                        rst.beforeFirst();
                        rst.next();
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
                    return geofenceData;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving Geo fence data " + fenceId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<GeofenceData> getGeoFences(int groupId, int tenantId) throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            String sql = "SELECT " +
                    "G.ID AS FENCE_ID, " +
                    "FENCE_NAME, " +
                    "DESCRIPTION, " +
                    "LATITUDE, " +
                    "LONGITUDE, " +
                    "RADIUS, " +
                    "GEO_JSON, " +
                    "FENCE_SHAPE " +
                    "FROM DM_GEOFENCE G " +
                    "JOIN DM_GEOFENCE_GROUP_MAPPING M ON G.ID = M.FENCE_ID " +
                    "WHERE M.GROUP_ID = ? AND TENANT_ID = ?";

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
}
