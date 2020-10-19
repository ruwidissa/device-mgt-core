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
import org.wso2.carbon.device.mgt.common.geo.service.GeoLocationBasedServiceException;
import org.wso2.carbon.device.mgt.common.geo.service.GeofenceData;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.GeofenceDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            GeofenceData geofenceData = null;
            Connection conn = this.getConnection();
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
            boolean isNameProvided = false;
            List<GeofenceData> geofenceData;
            Connection conn = this.getConnection();
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
        return null;
    }

    @Override
    public List<GeofenceData> getGeoFencesOfTenant(int tenantId)
            throws DeviceManagementDAOException {
        try {
            List<GeofenceData> geofenceData;
            Connection conn = this.getConnection();
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

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
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
}
