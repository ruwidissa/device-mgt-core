/*
 * Copyright (c) 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.dao.impl.geofence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.event.config.EventConfig;
import org.wso2.carbon.device.mgt.common.geo.service.GeofenceData;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.EventManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.GeofenceDAO;
import org.wso2.carbon.device.mgt.core.dao.impl.AbstractGeofenceDAOImpl;
import org.wso2.carbon.device.mgt.core.dto.event.config.GeoFenceGroupMap;

import java.sql.*;
import java.util.Date;
import java.util.*;

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
                    stmt.setString(index++, request.getProperty(DeviceManagementConstants.GeoServices.FENCE_NAME).toString() + "%");
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
}
