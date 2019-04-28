/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.core.dao.impl.application;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.wso2.carbon.device.application.mgt.common.AppLifecycleState;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.dto.CategoryDTO;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.dto.TagDTO;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.UnexpectedServerErrorException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * This handles ApplicationDAO related operations.
 */
public class GenericApplicationDAOImpl extends AbstractDAOImpl implements ApplicationDAO {

    private static final Log log = LogFactory.getLog(GenericApplicationDAOImpl.class);

    @Override
    public int createApplication(ApplicationDTO application, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to create an application");
            log.debug("ApplicationDTO Details : ");
            log.debug("App Name : " + application.getName() + " App Type : " + application.getType());
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int applicationId = -1;
        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement("INSERT INTO AP_APP "
                            + "(NAME, "
                            + "DESCRIPTION, "
                            + "TYPE, "
                            + "SUB_TYPE, "
                            + "TENANT_ID, "
                            + "DEVICE_TYPE_ID) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, application.getName());
            stmt.setString(2, application.getDescription());
            stmt.setString(3, application.getType());
            stmt.setString(4, application.getSubType());
            stmt.setInt(5, tenantId);
            stmt.setInt(6, application.getDeviceTypeId());
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                applicationId = rs.getInt(1);
            }
            return applicationId;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while obtaining the DB connection when application creation", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding the application", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public boolean isExistApplication(String appName, String type, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to verify whether the registering app is registered or not");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM AP_APP WHERE NAME = ? AND TYPE = ? AND TENANT_ID = ?";
        try {
            conn = this.getDBConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, appName);
            stmt.setString(2, type);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            return rs.next();

        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while obtaining the DB connection when verifying application existence", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "DB connection error occured while checking whether application exist or not.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<ApplicationDTO> getApplications(Filter filter,int deviceTypeId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application data from the database");
            log.debug(String.format("Filter: limit=%s, offset=%s", filter.getLimit(), filter.getOffset()));
        }
        int paramIndex = 1;
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
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
                + "AP_APP_RELEASE.SUPPORTED_OS_VERSIONS AS RELEASE_SUP_OS_VERSIONS, "
                + "AP_APP_RELEASE.RATING AS RELEASE_RATING, "
                + "AP_APP_RELEASE.CURRENT_STATE AS RELEASE_CURRENT_STATE, "
                + "AP_APP_RELEASE.RATED_USERS AS RATED_USER_COUNT "
                + "FROM AP_APP "
                + "INNER JOIN AP_APP_RELEASE ON "
                + "AP_APP.ID = AP_APP_RELEASE.AP_APP_ID AND "
                + "AP_APP.TENANT_ID = AP_APP_RELEASE.TENANT_ID "
                + "WHERE AP_APP.TENANT_ID = ?";

        if (filter == null) {
            throw new ApplicationManagementDAOException("Filter need to be instantiated");
        }

        if (!StringUtils.isEmpty(filter.getAppType())) {
            sql += " AND AP_APP.TYPE = ?";
        }
        if (!StringUtils.isEmpty(filter.getAppName())) {
            sql += " AND LOWER (AP_APP.NAME) ";
            if (filter.isFullMatch()) {
                sql += "= ?";
            } else {
                sql += "LIKE ?";
            }
        }
        if (!StringUtils.isEmpty(filter.getSubscriptionType())) {
            sql += " AND AP_APP.SUB_TYPE = ?";
        }
        if (filter.getMinimumRating() > 0) {
            sql += " AND AP_APP.RATING >= ?";
        }
        if (!StringUtils.isEmpty(filter.getVersion())) {
            sql += " AND AP_APP_RELEASE.VERSION = ?";
        }
        if (!StringUtils.isEmpty(filter.getAppReleaseType())) {
            sql += " AND AP_APP_RELEASE.RELEASE_TYPE = ?";
        }
        if (!StringUtils.isEmpty(filter.getAppReleaseState())) {
            sql += " AND AP_APP_RELEASE.CURRENT_STATE = ?";
        }
        if (deviceTypeId > 0) {
            sql += " AND AP_APP.DEVICE_TYPE_ID = ?";
        }

        String sortingOrder = "ASC";
        if (!StringUtils.isEmpty(filter.getSortBy() )) {
            sortingOrder = filter.getSortBy();
        }
        sql += " ORDER BY APP_ID " + sortingOrder +" LIMIT ? OFFSET ? ";

        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(paramIndex++, tenantId);

            if (filter.getAppType() != null && !filter.getAppType().isEmpty()) {
                stmt.setString(paramIndex++, filter.getAppType());
            }
            if (filter.getAppName() != null && !filter.getAppName().isEmpty()) {
                if (filter.isFullMatch()) {
                    stmt.setString(paramIndex++, filter.getAppName().toLowerCase());
                } else {
                    stmt.setString(paramIndex++, "%" + filter.getAppName().toLowerCase() + "%");
                }
            }
            if (!StringUtils.isEmpty(filter.getSubscriptionType())) {
                stmt.setString(paramIndex++, filter.getSubscriptionType());
            }
            if (filter.getMinimumRating() > 0) {
                stmt.setInt(paramIndex++, filter.getMinimumRating());
            }
            if (!StringUtils.isEmpty(filter.getVersion())) {
                stmt.setString(paramIndex++, filter.getVersion());
            }
            if (!StringUtils.isEmpty(filter.getAppReleaseType())) {
                stmt.setString(paramIndex++, filter.getAppReleaseType());
            }
            if (!StringUtils.isEmpty(filter.getAppReleaseState())) {
                stmt.setString(paramIndex++, filter.getAppReleaseState());
            }
            if (deviceTypeId > 0 ) {
                stmt.setInt(paramIndex++, deviceTypeId);
            }
            if (filter.getLimit() == 0) {
                stmt.setInt(paramIndex++, 100);
            } else {
                stmt.setInt(paramIndex++, filter.getLimit());
            }
            stmt.setInt(paramIndex, filter.getOffset());
            rs = stmt.executeQuery();
            return Util.loadApplications(rs);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while getting application list for the tenant"
                    + " " + tenantId + ". While executing " + sql, e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection while "
                    + "getting application list for the tenant " + tenantId,
                    e);
        } catch (JSONException e) {
            throw new ApplicationManagementDAOException("Error occurred while parsing JSON ", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public String getUuidOfLatestRelease(int appId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting UUID from the latest app release");
        }

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "";
        String uuId = null;
        try {
            conn = this.getDBConnection();
            sql += "SELECT APP_RELEASE.UUID AS UUID FROM AP_APP_RELEASE AS APP_RELEASE, AP_APP_LIFECYCLE_STATE "
                    + "AS LIFECYCLE WHERE APP_RELEASE.AP_APP_ID=? AND APP_RELEASE.ID = LIFECYCLE.AP_APP_RELEASE_ID "
                    + "AND LIFECYCLE.CURRENT_STATE = ? ORDER BY APP_RELEASE.ID DESC;";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, appId);
            stmt.setString(2, AppLifecycleState.PUBLISHED.toString());
            rs = stmt.executeQuery();
            if (rs.next()) {
                uuId = rs.getString("UUID");
            }
            return uuId;
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while getting uuid of latest app release", e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection for "
                                                                + "getting app release id", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public int getApplicationCount(Filter filter, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application count from the database");
            log.debug(String.format("Filter: limit=%s, offset=%s", filter.getLimit(), filter.getOffset()));
        }
        int paramIndex = 1;
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "";
        int count = 0;

        if (filter == null) {
            throw new ApplicationManagementDAOException("Filter need to be instantiated");
        }

        try {
            conn = this.getDBConnection();
            sql += "SELECT count(APP.ID) AS APP_COUNT FROM AP_APP AS APP WHERE TENANT_ID = ?";

            if (filter.getAppName() != null) {
                sql += " AND LOWER (APP.NAME) LIKE ? ";
            }
            sql += ";";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(paramIndex++, tenantId);
            if (filter.getAppName() != null) {
                stmt.setString(paramIndex, "%" + filter.getAppName().toLowerCase() + "%");
            }
            rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt("APP_COUNT");
            }
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while getting application List", e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
        return count;
    }

    @Override
    public ApplicationDTO getApplication(String appName, String appType, int tenantId) throws
                                                                                    ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application with the type(" + appType + " and Name " + appName +
                              " ) from the database");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql =
                    "SELECT AP_APP.ID AS APP_ID, AP_APP.NAME AS APP_NAME, AP_APP.TYPE AS APP_TYPE, AP_APP.APP_CATEGORY "
                            + "AS APP_CATEGORY, AP_APP.SUB_TYPE AS SUB_TYPE ,AP_APP.CURRENCY AS CURRENCY,"
                            + " AP_APP.RESTRICTED AS RESTRICTED, AP_APP_TAG.TAG AS APP_TAG, AP_UNRESTRICTED_ROLE.ROLE "
                            + "AS ROLE FROM AP_APP, AP_APP_TAG, AP_UNRESTRICTED_ROLE WHERE AP_APP.NAME=? AND "
                            + "AP_APP.TYPE= ? AND AP_APP.TENANT_ID=?;";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, appName);
            stmt.setString(2, appType);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();

            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved basic details of the application with the type "
                                  + appType + "and app name " + appName);
            }

            return Util.loadApplication(rs);

        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while getting application details with app name " + appName +
                            " while executing query.", e);
        } catch (JSONException e) {
            throw new ApplicationManagementDAOException("Error occurred while parsing JSON", e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (UnexpectedServerErrorException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public ApplicationDTO getApplicationById(String id, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application with the id:" + id);
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql =
                    "SELECT AP_APP.ID AS APP_ID, AP_APP.NAME AS APP_NAME, AP_APP.TYPE AS APP_TYPE, AP_APP.APP_CATEGORY "
                            + "AS APP_CATEGORY, AP_APP.SUB_TYPE AS SUB_TYPE ,AP_APP.CURRENCY AS CURRENCY,"
                            + " AP_APP.RESTRICTED AS RESTRICTED, AP_APP_TAG.TAG AS APP_TAG, AP_UNRESTRICTED_ROLE.ROLE "
                            + "AS ROLE FROM AP_APP, AP_APP_TAG, AP_UNRESTRICTED_ROLE WHERE AP_APP.NAME=? AND "
                            + "AP_APP.APP_ID= ? AND AP_APP.TENANT_ID=?;";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();

            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved basic details of the application with the id:" + id);
            }

            return Util.loadApplication(rs);

        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while getting application details with app id " + id +
                            " while executing query.", e);
        } catch (JSONException e) {
            throw new ApplicationManagementDAOException("Error occurred while parsing JSON", e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (UnexpectedServerErrorException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public ApplicationDTO getApplicationByUUID(String releaseUuid, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application with the release UUID: " + releaseUuid + " from the database");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql =
                    "SELECT AP_APP.ID AS APP_ID, AP_APP.NAME AS APP_NAME, AP_APP.TYPE AS APP_TYPE, AP_APP.APP_CATEGORY "
                            + "AS APP_CATEGORY, AP_APP.SUB_TYPE AS SUB_TYPE, AP_APP.CURRENCY AS CURRENCY, "
                            + "AP_APP.RESTRICTED AS RESTRICTED, AP_APP.DEVICE_TYPE_ID AS DEVICE_TYPE_ID, "
                            + "AP_APP_TAG.TAG AS APP_TAG, AP_UNRESTRICTED_ROLE.ROLE AS "
                            + "ROLE FROM ((AP_APP LEFT JOIN AP_APP_TAG ON AP_APP.ID = AP_APP_TAG.AP_APP_ID) "
                            + "LEFT JOIN AP_UNRESTRICTED_ROLE ON AP_APP.ID = AP_UNRESTRICTED_ROLE.AP_APP_ID) "
                            + "WHERE AP_APP.ID = (SELECT AP_APP_ID FROM AP_APP_RELEASE WHERE UUID =? ) AND "
                            + "AP_APP.TENANT_ID = ? AND AP_APP.STATUS != ?;";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, releaseUuid);
            stmt.setInt(2, tenantId);
            stmt.setString(3, AppLifecycleState.REMOVED.toString());
            rs = stmt.executeQuery();

            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved basic details of the application for the application release UUID:  "
                                  + releaseUuid);
            }

            return Util.loadApplication(rs);

        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while getting application details with app release uuid " + releaseUuid +
                            " while executing query.", e);
        } catch (JSONException e) {
            throw new ApplicationManagementDAOException("Error occurred while parsing JSON", e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (UnexpectedServerErrorException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public ApplicationDTO getApplicationById(int applicationId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application with the id (" + applicationId + ") from the database");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
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
                    + "AP_APP_RELEASE.SUPPORTED_OS_VERSIONS AS RELEASE_SUP_OS_VERSIONS, "
                    + "AP_APP_RELEASE.RATING AS RELEASE_RATING, "
                    + "AP_APP_RELEASE.CURRENT_STATE AS RELEASE_CURRENT_STATE, "
                    + "AP_APP_RELEASE.RATED_USERS AS RATED_USER_COUNT "
                    + "FROM AP_APP "
                    + "INNER JOIN AP_APP_RELEASE ON "
                    + "AP_APP.ID = AP_APP_RELEASE.AP_APP_ID AND "
                    + "AP_APP.TENANT_ID = AP_APP_RELEASE.TENANT_ID "
                    + "WHERE "
                    + "AP_APP.ID =? AND "
                    + "AP_APP.TENANT_ID = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, applicationId);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved basic details of the application with the id "
                        + applicationId);
            }
            return Util.loadApplication(rs);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while getting application details with app id " + applicationId +
                            " while executing query.", e);
        } catch (JSONException e) {
            throw new ApplicationManagementDAOException("Error occurred while parsing JSON", e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (UnexpectedServerErrorException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public boolean verifyApplicationExistenceById(int appId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application with the application ID(" + appId + " ) from the database");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql =
                    "SELECT AP_APP.ID AS APP_ID FROM AP_APP WHERE AP_APP.ID = ? AND AP_APP.TENANT_ID=?;";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, appId);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();

            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved basic details of the application with the application ID " + appId);
            }
            return rs.next();
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while getting application details with app ID " + appId + " while executing query.",
                    e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public boolean updateApplication(ApplicationDTO applicationDTO, int tenantId)
            throws ApplicationManagementDAOException {
        Connection conn;
        try {
            conn = this.getDBConnection();
            String sql = "UPDATE AP_APP AP " +
                    "SET " +
                    "AP.NAME = ?,  " +
                    "AP.DESCRIPTION = ?, " +
                    "AP.SUB_TYPE = ?, " +
                    "AP.CURRENCY = ? " +
                    "WHERE AP.ID = ? AND AP.TENANT_ID = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, applicationDTO.getName());
                stmt.setString(2, applicationDTO.getDescription());
                stmt.setString(3, applicationDTO.getSubType());
                stmt.setString(4, applicationDTO.getPaymentCurrency());
                stmt.setInt(5, applicationDTO.getId());
                stmt.setInt(6, tenantId);
                return stmt.executeUpdate() > 0;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to update the application.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when obtaining database connection for updating the application.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteApplication(int appId) throws ApplicationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getDBConnection();
            String sql = "UPDATE AP_APP SET STATUS = ? WHERE ID = ? ";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, AppLifecycleState.REMOVED.toString());
            stmt.setInt(2, appId);
            stmt.executeUpdate();

        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while deleting the application: ", e);
        } finally {
            Util.cleanupResources(stmt, null);
        }
    }

    @Override
    public void addTags(List<String> tags, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to add tags");
        }
        Connection conn;
        PreparedStatement stmt = null;
        String sql = "INSERT INTO AP_APP_TAG "
                + "(TAG,"
                + " TENANT_ID) "
                + "VALUES (?, ?)";
        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            for (String tag : tags) {
                stmt.setString(1, tag);
                stmt.setInt(2, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();

        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while obtaining the DB connection when adding tags", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding tags", e);
        } finally {
            Util.cleanupResources(stmt, null);
        }
    }

    @Override
    public List<TagDTO> getAllTags(int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get all tags");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            List<TagDTO> tagEntities = new ArrayList<>();
            String sql = "SELECT "
                    + "AP_APP_TAG.ID AS ID, "
                    + "AP_APP_TAG.TAG AS TAG "
                    + "FROM AP_APP_TAG "
                    + "WHERE TENANT_ID = ?";
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            rs = stmt.executeQuery();

            while(rs.next()){
                TagDTO tagDTO = new TagDTO();
                tagDTO.setId(rs.getInt("ID"));
                tagDTO.setTagName(rs.getString("TAG"));
                tagEntities.add(tagDTO);
            }
            return tagEntities;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while obtaining the DB connection when adding tags", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding tags", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<CategoryDTO> getAllCategories(int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get all tags");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            List<CategoryDTO> categories = new ArrayList<>();
            String sql = "SELECT "
                    + "AP_APP_CATEGORY.ID AS ID, "
                    + "AP_APP_CATEGORY.CATEGORY AS CATEGORY "
                    + "FROM AP_APP_CATEGORY "
                    + "WHERE TENANT_ID = ?";
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            rs = stmt.executeQuery();

            while(rs.next()){
                CategoryDTO category = new CategoryDTO();
                category.setId(rs.getInt("ID"));
                category.setCategoryName(rs.getString("CATEGORY"));
                categories.add(category);
            }
            return categories;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while obtaining the DB connection when getting categories", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while getting categories", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void addCategories(List<String> categories, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to add tags");
        }
        Connection conn;
        PreparedStatement stmt = null;
        String sql = "INSERT INTO AP_APP_CATEGORY "
                + "(CATEGORY,"
                + " TENANT_ID) "
                + "VALUES (?, ?)";
        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            for (String category : categories) {
                stmt.setString(1, category);
                stmt.setInt(2, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();

        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while obtaining the DB connection when adding categories.", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding categories.", e);
        } finally {
            Util.cleanupResources(stmt, null);
        }
    }

    @Override
    public void addCategoryMapping (List<Integer>  categoryIds, int applicationId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to add categories");
        }
        Connection conn;
        PreparedStatement stmt = null;
        String sql = "INSERT INTO AP_APP_CATEGORY_MAPPING "
                + "(AP_APP_CATEGORY_ID, "
                + "AP_APP_ID, "
                + " TENANT_ID) "
                + "VALUES (?, ?, ?)";
        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            for (Integer categoryId : categoryIds) {
                stmt.setInt(1, categoryId);
                stmt.setInt(2, applicationId);
                stmt.setInt(3, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();

        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while obtaining the DB connection when adding data into category mapping.", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding data into category mapping.", e);
        } finally {
            Util.cleanupResources(stmt, null);
        }
    }

    @Override
    public List<Integer> getTagIdsForTagNames(List<String> tagNames, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get tag ids for given tag names");
        }
        try {
            Connection conn = this.getDBConnection();
            int index = 1;
            List<Integer> tagIds = new ArrayList<>();
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT AP_APP_TAG.ID AS ID FROM AP_APP_TAG WHERE AP_APP_TAG.TAG IN (", ") AND TENANT_ID = ?");
            tagNames.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (String tagName : tagNames) {
                    ps.setObject(index++, tagName);
                }
                ps.setInt(index, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        tagIds.add(rs.getInt("ID"));
                    }
                }
            }
            return tagIds;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while obtaining the DB connection when adding tags", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding tags", e);
        }
    }

    public void addTagMapping (List<Integer>  tagIds, int applicationId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to add tags");
        }
        Connection conn;
        PreparedStatement stmt = null;
        String sql = "INSERT INTO AP_APP_TAG_MAPPING "
                + "(AP_APP_TAG_ID, "
                + "AP_APP_ID, "
                + " TENANT_ID) "
                + "VALUES (?, ?, ?)";
        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            for (Integer tagId : tagIds) {
                stmt.setInt(1, tagId);
                stmt.setInt(2, applicationId);
                stmt.setInt(3, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();

        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while obtaining the DB connection when adding tags", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding tags", e);
        } finally {
            Util.cleanupResources(stmt, null);
        }
    }

    @Override
    public List<String> getAppTags(int appId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get tags for given application.");
        }
        Connection conn;
        List<String> tags = new ArrayList<>();
        String sql = "SELECT tag.TAG AS TAG "
                + "FROM "
                + "AP_APP_TAG tag INNER JOIN AP_APP_TAG_MAPPING tag_map ON tag.ID = tag_map.AP_APP_TAG_ID "
                + "INNER JOIN AP_APP app ON tag_map.AP_APP_ID = app.ID "
                + "WHERE app.ID = ? AND app.TENANT_ID = ?";
        try {
            conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1, appId);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        tags.add(rs.getString("TAG"));
                    }
                }
            }
            return tags;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while obtaining the DB connection when adding tags", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding tags", e);
        }
    }

    @Override
    public void deleteTagMapping (List<Integer> tagIds, int applicationId, int tenantId) throws ApplicationManagementDAOException{
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete Tag mappings.");
        }
        Connection conn;
        String sql = "DELETE FROM "
                + "AP_APP_TAG_MAPPING tm "
                + "WHERE "
                + "tm.AP_APP_TAG_ID = ? AND "
                + "tm.AP_APP_ID = ? AND "
                + "tm.TENANT_ID = ?";
        try {
            conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                for (Integer tagId : tagIds){
                    stmt.setInt(1, tagId);
                    stmt.setInt(2, applicationId);
                    stmt.setInt(3, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while obtaining the DB connection when deleting tag mapppig", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred when deleting tag mapping", e);
        }
    }

    @Override
    public List<String> getAppCategories(int appId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get categories for given application.");
        }
        Connection conn;
        List<String> categories = new ArrayList<>();
        String sql = "SELECT CATEGORY "
                + "FROM "
                + "AP_APP_CATEGORY cat INNER JOIN AP_APP_CATEGORY_MAPPING cat_map ON cat.ID = cat_map.AP_APP_CATEGORY_ID "
                + "INNER JOIN AP_APP app ON cat_map.AP_APP_ID = app.ID "
                + "WHERE app.ID = ? AND app.TENANT_ID = ?";
        try {
            conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1, appId);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        categories.add(rs.getString("CATEGORY"));
                    }
                }
            }
            return categories;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while obtaining the DB connection when adding tags", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while adding tags", e);
        }
    }


    @Override
    public void deleteTags(List<String> tags, int applicationId, int tenantId) throws ApplicationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;

            String sql = "DELETE FROM AP_APP_TAG WHERE TAG = ? AND AP_APP_ID = ? AND TENANT_ID = ?;";
            try{
                conn = this.getDBConnection();
                conn.setAutoCommit(false);
                stmt = conn.prepareStatement(sql);

                for (String tag : tags) {
                    stmt.setString(1, tag);
                    stmt.setInt(2, applicationId);
                    stmt.setInt(3, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while deleting tags of application: " + applicationId, e);
        } finally {
            Util.cleanupResources(stmt, null);
        }
    }

    @Override
    public ApplicationDTO getApplicationByRelease(String appReleaseUUID, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application with the UUID (" + appReleaseUUID + ") from the database");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql = "SELECT AP_APP_RELEASE.ID AS RELEASE_ID, AP_APP_RELEASE.VERSION, AP_APP_RELEASE.TENANT_ID,"
                    + "AP_APP_RELEASE.UUID, AP_APP_RELEASE.RELEASE_TYPE, AP_APP_RELEASE.APP_PRICE, "
                    + "AP_APP_RELEASE.STORED_LOCATION, AP_APP_RELEASE.BANNER_LOCATION, AP_APP_RELEASE.SC_1_LOCATION,"
                    + "AP_APP_RELEASE.SC_2_LOCATION, AP_APP_RELEASE.SC_3_LOCATION, AP_APP_RELEASE.APP_HASH_VALUE,"
                    + "AP_APP_RELEASE.SHARED_WITH_ALL_TENANTS, AP_APP_RELEASE.APP_META_INFO, AP_APP_RELEASE.CREATED_BY,"
                    + "AP_APP_RELEASE.CREATED_AT, AP_APP_RELEASE.PUBLISHED_BY, AP_APP_RELEASE.PUBLISHED_AT, "
                    + "AP_APP_RELEASE.STARS,"
                    + "AP_APP.ID AS APP_ID, AP_APP.NAME AS APP_NAME, AP_APP.TYPE AS APP_TYPE, "
                    + "AP_APP.APP_CATEGORY AS APP_CATEGORY, AP_APP.SUB_TYPE AS SUB_TYPE, AP_APP.CURRENCY AS CURRENCY, "
                    + "AP_UNRESTRICTED_ROLE.ROLE AS ROLE FROM AP_APP, AP_UNRESTRICTED_ROLE, AP_APP_RELEASE "
                    + "WHERE AP_APP_RELEASE.UUID=? AND AP_APP.TENANT_ID=?;";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, appReleaseUUID);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();

            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved details of the application with the UUID " + appReleaseUUID);
            }

            ApplicationDTO application = null;
            while (rs.next()) {
                ApplicationReleaseDTO appRelease = Util.loadApplicationRelease(rs);
                application = new ApplicationDTO();

                application.setId(rs.getInt("APP_ID"));
                application.setName(rs.getString("APP_NAME"));
                application.setType(rs.getString("APP_TYPE"));
                application.setAppCategory(rs.getString("APP_CATEGORY"));
                application.setSubType(rs.getString("SUB_TYPE"));
                application.setPaymentCurrency(rs.getString("CURRENCY"));
//                application.setIsRestricted(rs.getBoolean("RESTRICTED"));

                String unrestrictedRole = rs.getString("ROLE").toLowerCase();
                List<String> unrestrictedRoleList = new ArrayList<>();
                unrestrictedRoleList.add(unrestrictedRole);

                application.setUnrestrictedRoles(unrestrictedRoleList);

                List<ApplicationReleaseDTO> applicationReleaseList = new ArrayList<>();
                applicationReleaseList.add(appRelease);

                application.setApplicationReleaseDTOs(applicationReleaseList);
            }
            return application;
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while getting application details with UUID "
                                                                + appReleaseUUID + " while executing query.", e);
        } catch (JSONException e) {
            throw new ApplicationManagementDAOException("Error occurred while parsing JSON", e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public boolean isValidAppName(String appName, int deviceTypeId, int tenantId) throws ApplicationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql;
        try {
            conn = this.getDBConnection();
            sql = "SELECT AP_APP.ID AS ID "
                    + "FROM AP_APP "
                    + "WHERE "
                    + "AP_APP.NAME = ? AND "
                    + "AP_APP.DEVICE_TYPE_ID = ? AND "
                    + "AP_APP.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, appName);
            stmt.setInt(2, deviceTypeId);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while getting application List", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }
}
