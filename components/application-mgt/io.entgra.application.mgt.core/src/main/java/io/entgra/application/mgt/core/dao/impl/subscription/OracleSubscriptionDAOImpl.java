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

package io.entgra.application.mgt.core.dao.impl.subscription;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.application.mgt.common.exception.DBConnectionException;
import io.entgra.application.mgt.core.exception.ApplicationManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This handles Application subscribing operations which are specific to Oracle.
 */
public class OracleSubscriptionDAOImpl extends GenericSubscriptionDAOImpl {

    private static Log log = LogFactory.getLog(OracleSubscriptionDAOImpl.class);

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
}
