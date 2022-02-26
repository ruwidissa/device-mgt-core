/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.entgra.application.mgt.core.dao.impl.application.spapplication;

import io.entgra.application.mgt.common.IdentityServer;
import io.entgra.application.mgt.common.dto.ApplicationDTO;
import io.entgra.application.mgt.common.exception.DBConnectionException;
import io.entgra.application.mgt.core.dao.SPApplicationDAO;
import io.entgra.application.mgt.core.dao.impl.AbstractDAOImpl;
import io.entgra.application.mgt.core.dao.impl.application.GenericApplicationDAOImpl;
import io.entgra.application.mgt.core.exception.ApplicationManagementDAOException;
import io.entgra.application.mgt.core.exception.UnexpectedServerErrorException;
import io.entgra.application.mgt.core.util.DAOUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class GenericSPApplicationDAOImpl extends AbstractDAOImpl implements SPApplicationDAO {

    private static final Log log = LogFactory.getLog(GenericApplicationDAOImpl.class);
    @Override
    public List<IdentityServer> getIdentityServers(int tenantId) throws ApplicationManagementDAOException {
        String sql = "SELECT ID, NAME, DESCRIPTION, URL, SP_APPS_URI, SP_APPS_API, TENANT_ID, USERNAME, PASSWORD "
                + "FROM AP_IDENTITY_SERVER "
                + "WHERE TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1, tenantId);
                try (ResultSet rs = stmt.executeQuery()){
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully retrieved available identity servers" );
                    }
                    return DAOUtil.loadIdentityServers(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to retrieve available identity servers";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to retrieve available identity servers";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public IdentityServer getIdentityServerById(int id, int tenantId) throws ApplicationManagementDAOException {
        String sql = "SELECT ID, NAME, DESCRIPTION, URL, SP_APPS_URI, SP_APPS_API,  TENANT_ID, USERNAME, PASSWORD "
                + "FROM AP_IDENTITY_SERVER "
                + "WHERE TENANT_ID = ? AND "
                + "ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1, tenantId);
                stmt.setInt(2, id);
                try (ResultSet rs = stmt.executeQuery()){
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully retrieved available identity servers" );
                    }
                    return DAOUtil.loadIdentityServer(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to retrieve available identity servers";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to retrieve available identity servers";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (UnexpectedServerErrorException e) {
            String msg = "Found more than one identity server for id: " + id;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<ApplicationDTO> getSPApplications(int identityServerId, String spUID, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting identity server applications from the database");
        }
        String sql = "SELECT "
                + "AP_APP.ID AS APP_ID, "
                + "AP_APP.NAME AS APP_NAME, "
                + "AP_APP.DESCRIPTION AS APP_DESCRIPTION, "
                + "AP_APP.TYPE AS APP_TYPE, "
                + "AP_APP.STATUS AS APP_STATUS, "
                + "AP_APP.SUB_TYPE AS APP_SUB_TYPE, "
                + "AP_APP.CURRENCY AS APP_CURRENCY, "
                + "AP_APP.RATING AS APP_RATING, "
                + "AP_APP.DEVICE_TYPE_ID AS APP_DEVICE_TYPE_ID, "
                + "AP_APP_RELEASE.ID AS RELEASE_ID, "
                + "AP_APP_RELEASE.DESCRIPTION AS RELEASE_DESCRIPTION, "
                + "AP_APP_RELEASE.VERSION AS RELEASE_VERSION, "
                + "AP_APP_RELEASE.UUID AS RELEASE_UUID, "
                + "AP_APP_RELEASE.RELEASE_TYPE AS RELEASE_TYPE, "
                + "AP_APP_RELEASE.INSTALLER_LOCATION AS AP_RELEASE_STORED_LOC, "
                + "AP_APP_RELEASE.ICON_LOCATION AS AP_RELEASE_ICON_LOC, "
                + "AP_APP_RELEASE.BANNER_LOCATION AS AP_RELEASE_BANNER_LOC, "
                + "AP_APP_RELEASE.SC_1_LOCATION AS AP_RELEASE_SC1, "
                + "AP_APP_RELEASE.SC_2_LOCATION AS AP_RELEASE_SC2, "
                + "AP_APP_RELEASE.SC_3_LOCATION AS AP_RELEASE_SC3, "
                + "AP_APP_RELEASE.APP_HASH_VALUE AS RELEASE_HASH_VALUE, "
                + "AP_APP_RELEASE.APP_PRICE AS RELEASE_PRICE, "
                + "AP_APP_RELEASE.APP_META_INFO AS RELEASE_META_INFO, "
                + "AP_APP_RELEASE.PACKAGE_NAME AS PACKAGE_NAME, "
                + "AP_APP_RELEASE.SUPPORTED_OS_VERSIONS AS RELEASE_SUP_OS_VERSIONS, "
                + "AP_APP_RELEASE.RATING AS RELEASE_RATING, "
                + "AP_APP_RELEASE.CURRENT_STATE AS RELEASE_CURRENT_STATE, "
                + "AP_APP_RELEASE.RATED_USERS AS RATED_USER_COUNT "
                + "FROM AP_APP "
                + "LEFT JOIN AP_APP_RELEASE ON "
                + "AP_APP.ID = AP_APP_RELEASE.AP_APP_ID AND "
                + "AP_APP.TENANT_ID = AP_APP_RELEASE.TENANT_ID "
                + "INNER JOIN AP_IS_SP_APP_MAPPING as SP_APPS on SP_APPS.AP_APP_ID = AP_APP.ID "
                + "WHERE "
                + "SP_APPS.SP_UID = ? "
                + "AND SP_APPS.IS_ID = ? "
                + "AND AP_APP.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, spUID);
                stmt.setInt(2, identityServerId);
                stmt.setInt(3, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully retrieved basic details of the identity server applications");
                    }
                    return DAOUtil.loadApplications(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get identity server applications for application release";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while getting identity server applications while executing query. Executed query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean isSPApplicationExist(int identityServerId, String spUID, int appId, int tenantId) throws ApplicationManagementDAOException {
        String sql = "SELECT AP_APP_ID AS ID "
                + "FROM AP_IS_SP_APP_MAPPING SP_MAPPING "
                + "WHERE "
                + "SP_UID = ? AND AP_APP_ID = ? "
                + "AND IS_ID = ? AND "
                + "TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, spUID);
                stmt.setInt(2, appId);
                stmt.setInt(3, identityServerId);
                stmt.setInt(4, tenantId);
                try (ResultSet rs = stmt.executeQuery()){
                    return rs.next();
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to check whether the existence of "
                    + "sp application with id " + appId + " for service provider which has UID " + spUID;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to check whether the existence of service provider application " +
                    "with id " + appId + " for service provider which has  UID " + spUID + ". executed query " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void detachSPApplication(int identityServerId, String spUID, int appId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to map an application wit identity server:service provider");
            log.debug("Application Details : ");
            log.debug("App ID : " + appId + " SP UID : " + spUID + " IS ID : " + identityServerId);
        }
        String sql = "DELETE FROM AP_IS_SP_APP_MAPPING "
                + "WHERE SP_UID = ? "
                + "AND AP_APP_ID = ? "
                + "AND IS_ID = ? "
                + "AND TENANT_ID = ? ";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, spUID);
                stmt.setInt(2, appId);
                stmt.setInt(3, identityServerId);
                stmt.setInt(4, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to create an sp application mapping which has "
                    + "application id " + appId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to create an application which has application id "
                    + appId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int attachSPApplication(int identityServerId, String spUID, int appId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to map an application wit identity server:service provider");
            log.debug("Application Details : ");
            log.debug("App ID : " + appId + " SP UID : " + spUID + " IS ID : " + identityServerId);
        }
        String sql = "INSERT INTO AP_IS_SP_APP_MAPPING "
                + "(SP_UID, "
                + "AP_APP_ID, "
                + "IS_ID, TENANT_ID) "
                + "VALUES (?, ?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, spUID);
                stmt.setInt(2, appId);
                stmt.setInt(3, identityServerId);
                stmt.setInt(4, tenantId);
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return -1;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to create an sp application mapping which has "
                    + "application id " + appId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to create an application which has application id "
                    + appId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteApplicationFromServiceProviders(int applicationId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete application with the id: " + applicationId + " from service providers");
        }
        String sql = "DELETE FROM AP_IS_SP_APP_MAPPING WHERE AP_APP_ID = ? AND TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, applicationId);
                stmt.setInt(2, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to create an sp application mapping which has "
                    + "application id " + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to create an application which has application id "
                    + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

}
