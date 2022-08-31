/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*   Copyright (c) 2022, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.dao.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.TrackerDeviceInfo;
import org.wso2.carbon.device.mgt.common.TrackerGroupInfo;
import org.wso2.carbon.device.mgt.common.TrackerPermissionInfo;
import org.wso2.carbon.device.mgt.core.dao.TrackerManagementDAOException;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;
import java.util.StringJoiner;

/**
 * This class represents utilities required to work with group management data
 */
public final class TrackerManagementDAOUtil {

    private static final Log log = LogFactory.getLog(TrackerManagementDAOUtil.class);

    /**
     * Cleanup resources used to transaction
     *
     * @param stmt Prepared statement used
     * @param rs   Obtained results set
     */
    public static void cleanupResources(PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing result set", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing prepared statement", e);
            }
        }
    }

    /**
     * Lookup datasource using name and jndi properties
     *
     * @param dataSourceName Name of datasource to lookup
     * @param jndiProperties Hash table of JNDI Properties
     * @return datasource looked
     */
    public static DataSource lookupDataSource(String dataSourceName, final Hashtable<Object, Object> jndiProperties) {
        try {
            if (jndiProperties == null || jndiProperties.isEmpty()) {
                return (DataSource) InitialContext.doLookup(dataSourceName);
            }
            final InitialContext context = new InitialContext(jndiProperties);
            return (DataSource) context.lookup(dataSourceName);
        } catch (Exception e) {
            String msg = "Error in looking up data source: " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public static TrackerGroupInfo loadTrackerGroup(ResultSet rs) throws SQLException {
        TrackerGroupInfo trackerGroupInfo = new TrackerGroupInfo();
        trackerGroupInfo.setId(rs.getInt("ID"));
        trackerGroupInfo.setTraccarGroupId(rs.getInt("TRACCAR_GROUP_ID"));
        trackerGroupInfo.setGroupId(rs.getInt("GROUP_ID"));
        trackerGroupInfo.setTenantId(rs.getInt("TENANT_ID"));
        trackerGroupInfo.setStatus(rs.getInt("STATUS"));
        return trackerGroupInfo;
    }

    public static TrackerDeviceInfo loadTrackerDevice(ResultSet rs) throws SQLException {
        TrackerDeviceInfo trackerDeviceInfo = new TrackerDeviceInfo();
        trackerDeviceInfo.setId(rs.getInt("ID"));
        trackerDeviceInfo.setTraccarDeviceId(rs.getInt("TRACCAR_DEVICE_ID"));
        trackerDeviceInfo.setDeviceId(rs.getInt("DEVICE_ID"));
        trackerDeviceInfo.setTenantId(rs.getInt("TENANT_ID"));
        trackerDeviceInfo.setStatus(rs.getInt("STATUS"));
        return trackerDeviceInfo;
    }

    public static TrackerPermissionInfo loadPermission(ResultSet rs) throws SQLException {
        TrackerPermissionInfo trackerPermissionInfo = new TrackerPermissionInfo();
        trackerPermissionInfo.setTraccarUserId(rs.getInt("TRACCAR_USER_ID"));
        trackerPermissionInfo.setTraccarDeviceId(rs.getInt("TRACCAR_DEVICE_ID"));
        return trackerPermissionInfo;
    }

    public static String buildDeviceIdNotInQuery(List<Integer> DeviceIdList) throws TrackerManagementDAOException {
        if (DeviceIdList == null || DeviceIdList.isEmpty()) {
            String msg = "SQL query build for Device Id list failed. Device Id list cannot be empty or null";
            log.error(msg);
            throw new TrackerManagementDAOException(msg);
        }
        StringJoiner joiner = new StringJoiner(",", " AND TRACCAR_DEVICE_ID NOT IN(", ")");
        DeviceIdList.stream().map(status -> "?").forEach(joiner::add);
        return joiner.toString();
    }
}
