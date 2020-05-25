/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
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
package org.wso2.carbon.device.application.mgt.core.dao.impl.visibility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.VisibilityDAO;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.VisibilityManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic database level implementation for the DAO which can be used by different databases.
 */
public class GenericVisibilityDAOImpl extends AbstractDAOImpl implements VisibilityDAO {

    private static final Log log = LogFactory.getLog(GenericVisibilityDAOImpl.class);

    @Override
    public void addUnrestrictedRoles(List<String> unrestrictedRoles, int applicationId, int tenantId) throws
            VisibilityManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to add unrestricted roles for application which has application"
                    + " ID " + applicationId);
        }
        String sql = "INSERT INTO "
                + "AP_UNRESTRICTED_ROLE "
                + "(ROLE, "
                + "TENANT_ID, "
                + "AP_APP_ID) "
                + "VALUES (?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (String role : unrestrictedRoles) {
                    stmt.setString(1, role);
                    stmt.setInt(2, tenantId);
                    stmt.setInt(3, applicationId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when adding unrestricted roles for "
                    + "application which has Id " + applicationId;
            log.error(msg, e);
            throw new VisibilityManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to add unrestricted roles for application which has Id "
                    + applicationId + ". Executed query " + sql;
            log.error(msg, e);
            throw new VisibilityManagementDAOException(msg, e);
        }
    }

    @Override
    public List<String> getUnrestrictedRoles(int applicationId, int tenantId) throws VisibilityManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get unrestricted roles for application which has application "
                    + "ID " + applicationId);
        }
        List<String> unrestrictedRoles = new ArrayList<>();
        String sql = "SELECT ROLE "
                + "FROM AP_UNRESTRICTED_ROLE "
                + "WHERE AP_APP_ID = ? AND "
                + "TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, applicationId);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        unrestrictedRoles.add(rs.getString("ROLE"));
                    }
                    return unrestrictedRoles;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get unrestricted roles for application "
                    + "which has application Id " + applicationId;
            log.error(msg, e);
            throw new VisibilityManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to get unrestricted roles for application which has "
                    + "application Id " + applicationId + ". Executed query: " + sql;
            log.error(msg, e);
            throw new VisibilityManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteUnrestrictedRoles(List<String> unrestrictedRoles, int applicationId, int tenantId)
            throws VisibilityManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete unrestricted roles of application which has "
                    + "application Id " + applicationId);
        }
        String sql = "DELETE "
                + "FROM AP_UNRESTRICTED_ROLE "
                + "WHERE AP_APP_ID = ? AND "
                + "ROLE = ? AND "
                + "TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (String role : unrestrictedRoles) {
                    stmt.setInt(1, applicationId);
                    stmt.setString(2, role);
                    stmt.setInt(3, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to delete unrestricted roles of an "
                    + "application which has application Id " + applicationId;
            log.error(msg, e);
            throw new VisibilityManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to delete unrestricted roles of an application which has"
                    + " application Id " + applicationId + ". executed query: " + sql;
            log.error(msg, e);
            throw new VisibilityManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteAppUnrestrictedRoles(int applicationId, int tenantId) throws VisibilityManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete Application unrestricted roles of application which "
                    + "has application Id " + applicationId);
        }
        String sql = "DELETE "
                + "FROM AP_UNRESTRICTED_ROLE "
                + "WHERE AP_APP_ID = ? AND "
                + "TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, applicationId);
                stmt.setInt(2, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to delete application unrestricted roles "
                    + "which has application Id " + applicationId;
            log.error(msg, e);
            throw new VisibilityManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to delete application unrestricted roles which has"
                    + " application Id " + applicationId + ". executed query: " + sql;
            log.error(msg, e);
            throw new VisibilityManagementDAOException(msg, e);
        }
    }
}
