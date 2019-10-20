/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.application.mgt.core.dao.impl.subscription;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * This handles Application subscribing operations which are specific to MsSQL.
 */
public class SQLServerSubscriptionDAOImpl extends GenericSubscriptionDAOImpl {

    private static Log log = LogFactory.getLog(SQLServerSubscriptionDAOImpl.class);

    @Override
    public List<String> getAppSubscribedUsers(int offsetValue, int limitValue, int appReleaseId,
            int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed users for " +
                    "given app release id.");
        }
        try {
            Connection conn = this.getDBConnection();
            List<String> subscribedUsers = new ArrayList<>();
            String sql = "SELECT "
                    + "US.USER_NAME AS USER "
                    + "FROM AP_USER_SUBSCRIPTION US "
                    + "WHERE "
                    + "AP_APP_RELEASE_ID = ? AND TENANT_ID = ? ORDER BY US.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, appReleaseId);
                stmt.setInt(2, tenantId);
                stmt.setInt(3, offsetValue);
                stmt.setInt(4, limitValue);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        subscribedUsers.add(rs.getString("USER"));
                    }
                }
                return subscribedUsers;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get already " +
                    "subscribed users for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting subscribed users for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<String> getAppSubscribedRoles(int offsetValue, int limitValue, int appReleaseId,
            int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed roles for " +
                    "given app release id.");
        }
        try {
            Connection conn = this.getDBConnection();
            List<String> subscribedRoles = new ArrayList<>();
            String sql = "SELECT "
                    + "RS.ROLE_NAME AS ROLE "
                    + "FROM AP_ROLE_SUBSCRIPTION RS "
                    + "WHERE "
                    + "AP_APP_RELEASE_ID = ? AND TENANT_ID = ? ORDER BY RS.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);
                ps.setInt(3, offsetValue);
                ps.setInt(4, limitValue);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subscribedRoles.add(rs.getString("ROLE"));
                    }
                }
                return subscribedRoles;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get already " +
                    "subscribed roles for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting subscribed roles for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<String> getAppSubscribedGroups(int offsetValue, int limitValue, int appReleaseId,
            int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed groups for " +
                    "given app release id.");
        }
        try {
            Connection conn = this.getDBConnection();
            List<String> subscribedGroups = new ArrayList<>();
            String sql = "SELECT "
                    + "GS.GROUP_NAME AS GROUPS "
                    + "FROM AP_GROUP_SUBSCRIPTION GS "
                    + "WHERE "
                    + "AP_APP_RELEASE_ID = ? AND TENANT_ID = ? ORDER BY GS.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);
                ps.setInt(3, offsetValue);
                ps.setInt(4, limitValue);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subscribedGroups.add(rs.getString("GROUPS"));
                    }
                }
                return subscribedGroups;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get already " +
                    "subscribed groups for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting subscribed groups for given " +
                    "app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> addDeviceSubscription(String subscribedBy, List<Integer> deviceIds,
            String subscribedFrom, String installStatus, int releaseId, int tenantId)
            throws ApplicationManagementDAOException {
        String sql = "INSERT INTO "
                + "AP_DEVICE_SUBSCRIPTION("
                + "SUBSCRIBED_BY, "
                + "SUBSCRIBED_TIMESTAMP, "
                + "ACTION_TRIGGERED_FROM, "
                + "STATUS, "
                + "DM_DEVICE_ID, "
                + "AP_APP_RELEASE_ID,"
                + "TENANT_ID) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                Calendar calendar = Calendar.getInstance();
                Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
                List<Integer> deviceSubIds = new ArrayList<>();
                for (Integer deviceId : deviceIds) {
                    stmt.setString(1, subscribedBy);
                    stmt.setTimestamp(2, timestamp);
                    stmt.setString(3, subscribedFrom);
                    stmt.setString(4, installStatus);
                    stmt.setInt(5, deviceId);
                    stmt.setInt(6, releaseId);
                    stmt.setInt(7, tenantId);
                    if (log.isDebugEnabled()) {
                        log.debug("Adding a device subscription for device id " + deviceId + " and application "
                                + "release which has release id" + releaseId);
                    }
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            deviceSubIds.add(rs.getInt(1));
                        }
                    }
                }
                return deviceSubIds;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occured while obtaining database connection to add device subscription for application "
                    + "release which has release Id" + releaseId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occured when processing SQL to add device subscription for application release which"
                    + " has release Id " + releaseId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }
}
