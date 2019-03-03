/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.device.application.mgt.core.dao.impl.visibility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.VisibilityDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
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
            log.debug("Request received in DAO Layer to add unrestricted roles");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "INSERT INTO AP_UNRESTRICTED_ROLE (ROLE, TENANT_ID, AP_APP_ID) VALUES (?, ?, ?)";
        try{
            conn = this.getDBConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            for (String role : unrestrictedRoles) {
                stmt.setString(1, role);
                stmt.setInt(2, tenantId);
                stmt.setInt(3, applicationId);
                stmt.addBatch();
            }
            stmt.executeBatch();

        }catch (DBConnectionException e) {
            throw new VisibilityManagementDAOException("Error occurred while obtaining the DB connection when adding roles", e);
        }catch (SQLException e) {
            throw new VisibilityManagementDAOException("Error occurred while adding unrestricted roles", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<String> getUnrestrictedRoles(int applicationId, int tenantId) throws VisibilityManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get unrestricted roles");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<String> unrestrictedRoles = new ArrayList<>();
        String sql = "SELECT ID, ROLE FROM AP_UNRESTRICTED_ROLE WHERE AP_APP_ID = ? AND TENANT_ID = ?;";
        try{
            conn = this.getDBConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, applicationId);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();

            while (rs.next()){
                unrestrictedRoles.add(rs.getString("ROLE").toLowerCase());
            }
            return unrestrictedRoles;

        }catch (DBConnectionException e) {
            throw new VisibilityManagementDAOException("Error occurred while obtaining the DB connection when adding roles", e);
        }catch (SQLException e) {
            throw new VisibilityManagementDAOException("Error occurred while adding unrestricted roles", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void deleteUnrestrictedRoles(List<String> unrestrictedRoles, int applicationId, int tenantId) throws VisibilityManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete unrestricted roles");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "DELETE FROM AP_UNRESTRICTED_ROLE WHERE AP_APP_ID = ? AND ROLE = ? AND TENANT_ID = ?;";
        try{
            conn = this.getDBConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);

            for (String role : unrestrictedRoles) {
                stmt.setInt(1, applicationId);
                stmt.setString(2, role);
                stmt.setInt(3, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();

        }catch (DBConnectionException e) {
            throw new VisibilityManagementDAOException("Error occurred while obtaining the DB connection when adding roles", e);
        }catch (SQLException e) {
            throw new VisibilityManagementDAOException("Error occurred while adding unrestricted roles", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }
}
