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

package io.entgra.device.mgt.core.application.mgt.core.dao.impl.subscription;

import io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This handles Application subscribing operations which are specific to MsSQL.
 */
public class SQLServerSubscriptionDAOImpl extends GenericSubscriptionDAOImpl {

    private static Log log = LogFactory.getLog(SQLServerSubscriptionDAOImpl.class);

    @Override
    public List<String> getAppSubscribedUsers(int offsetValue, int limitValue, int appReleaseId,
                                              int tenantId, Boolean uninstalled, String searchName)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed/unsubscribed users for " +
                    "given app release id.");
        }
        try {
            Connection conn = this.getDBConnection();
            List<String> subscribedUsers = new ArrayList<>();
            String sql = "SELECT "
                    + "US.USER_NAME AS USER_NAME "
                    + "FROM AP_USER_SUBSCRIPTION US "
                    + "WHERE AP_APP_RELEASE_ID = ? "
                    + "AND TENANT_ID = ? ";
            if (uninstalled != null) {
                sql += "AND UNSUBSCRIBED = ? ";
            }
            if (searchName != null && !searchName.trim().isEmpty()) {
                sql += "AND US.USER_NAME LIKE ? ";
            }
            sql += "ORDER BY US.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int index = 1;
                stmt.setInt(index++, appReleaseId);
                stmt.setInt(index++, tenantId);
                if (uninstalled != null) {
                    stmt.setBoolean(index++, uninstalled);
                }
                if (searchName != null && !searchName.trim().isEmpty()) {
                    stmt.setString(index++, "%" + searchName + "%");
                }
                stmt.setInt(index++, offsetValue);
                stmt.setInt(index, limitValue);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        subscribedUsers.add(rs.getString("USER_NAME"));
                    }
                }
                return subscribedUsers;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get already " +
                    "subscribed/unsubscribed users for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting subscribed/unsubscribed users for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<String> getAppSubscribedRoles(int offsetValue, int limitValue, int appReleaseId,
                                              int tenantId, Boolean uninstalled, String searchName)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed/unsubscribed roles for " +
                    "given app release id.");
        }
        try {
            Connection conn = this.getDBConnection();
            List<String> subscribedRoles = new ArrayList<>();
            String sql = "SELECT "
                    + "RS.ROLE_NAME AS ROLE "
                    + "FROM AP_ROLE_SUBSCRIPTION RS "
                    + "WHERE AP_APP_RELEASE_ID = ? "
                    + "AND TENANT_ID = ? ";
            if (uninstalled != null) {
                sql += "AND UNSUBSCRIBED = ? ";
            }
            if (searchName != null && !searchName.trim().isEmpty()) {
                sql += "AND RS.ROLE_NAME LIKE ? ";
            }
            sql += "ORDER BY RS.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int paramIndex = 1;
                ps.setInt(paramIndex++, appReleaseId);
                ps.setInt(paramIndex++, tenantId);
                if (uninstalled != null) {
                    ps.setBoolean(paramIndex++, uninstalled);
                }
                if (searchName != null && !searchName.trim().isEmpty()) {
                    ps.setString(paramIndex++, "%" + searchName + "%");
                }
                ps.setInt(paramIndex++, offsetValue);
                ps.setInt(paramIndex, limitValue);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subscribedRoles.add(rs.getString("ROLE"));
                    }
                }
                return subscribedRoles;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get already " +
                    "subscribed/unsubscribed roles for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting subscribed/unsubscribed roles for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<String> getAppSubscribedGroups(int offsetValue, int limitValue, int appReleaseId,
                                               int tenantId, Boolean uninstalled, String searchName)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get subscribed/unsubscribed groups for the given app release ID.");
        }
        try {
            Connection conn = this.getDBConnection();
            List<String> subscribedGroups = new ArrayList<>();
            String sql = "SELECT "
                    + "GS.GROUP_NAME AS GROUPS "
                    + "FROM AP_GROUP_SUBSCRIPTION GS "
                    + "WHERE AP_APP_RELEASE_ID = ? AND TENANT_ID = ?";
            if (uninstalled != null) {
                sql += " AND UNSUBSCRIBED = ?";
            }
            if (searchName != null && !searchName.trim().isEmpty()) {
                sql += " AND GS.GROUP_NAME LIKE ?";
            }
            sql += " ORDER BY GS.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int paramIndex = 1;
                ps.setInt(paramIndex++, appReleaseId);
                ps.setInt(paramIndex++, tenantId);
                if (uninstalled != null) {
                    ps.setBoolean(paramIndex++, uninstalled);
                }
                if (searchName != null && !searchName.trim().isEmpty()) {
                    ps.setString(paramIndex++, "%" + searchName + "%");
                }
                ps.setInt(paramIndex++, offsetValue);
                ps.setInt(paramIndex, limitValue);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subscribedGroups.add(rs.getString("GROUPS"));
                    }
                }
                return subscribedGroups;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get subscribed/unsubscribed groups for the given app release ID.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting subscribed/unsubscribed groups for the given app release ID.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }
}
