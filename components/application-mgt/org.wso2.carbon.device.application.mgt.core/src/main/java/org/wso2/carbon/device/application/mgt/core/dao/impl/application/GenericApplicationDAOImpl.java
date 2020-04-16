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
package org.wso2.carbon.device.application.mgt.core.dao.impl.application;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.AppLifecycleState;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.dto.CategoryDTO;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.dto.TagDTO;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.util.DAOUtil;
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
 * This handles Application related operations.
 */
public class GenericApplicationDAOImpl extends AbstractDAOImpl implements ApplicationDAO {

    private static final Log log = LogFactory.getLog(GenericApplicationDAOImpl.class);

    @Override
    public int createApplication(ApplicationDTO applicationDTO, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to create an application");
            log.debug("ApplicationDTO Details : ");
            log.debug("App Name : " + applicationDTO.getName() + " App Type : " + applicationDTO.getType());
        }
        String sql = "INSERT INTO AP_APP "
                + "(NAME, "
                + "DESCRIPTION, "
                + "TYPE, "
                + "SUB_TYPE, "
                + "TENANT_ID, "
                + "DEVICE_TYPE_ID) VALUES (?, ?, ?, ?, ?, ?)";
        int applicationId = -1;
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, applicationDTO.getName());
                stmt.setString(2, applicationDTO.getDescription());
                stmt.setString(3, applicationDTO.getType());
                stmt.setString(4, applicationDTO.getSubType());
                stmt.setInt(5, tenantId);
                stmt.setInt(6, applicationDTO.getDeviceTypeId());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        applicationId = rs.getInt(1);
                    }
                    return applicationId;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to create an application which has "
                    + "application name " + applicationDTO.getName();
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to create an application which has application name "
                    + applicationDTO.getName();
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<ApplicationDTO> getApplications(Filter filter, int deviceTypeId, int tenantId) throws
            ApplicationManagementDAOException {
        if (filter == null) {
            String msg = "Filter is not instantiated for tenant "+tenantId;
            log.error(msg);
            throw new ApplicationManagementDAOException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Getting application data from the database");
            log.debug(String.format("Filter: limit=%s, offset=%s", filter.getLimit(), filter.getOffset()));
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
                + "INNER JOIN AP_APP_RELEASE ON "
                + "AP_APP.ID = AP_APP_RELEASE.AP_APP_ID "
                + "INNER JOIN (SELECT AP_APP.ID FROM AP_APP ";
        if (!StringUtils.isEmpty(filter.getVersion()) || !StringUtils.isEmpty(filter.getAppReleaseState())
                || !StringUtils.isEmpty(filter.getAppReleaseType())) {
            sql += "INNER JOIN AP_APP_RELEASE ON AP_APP.ID = AP_APP_RELEASE.AP_APP_ID ";
        }
        sql += "WHERE AP_APP.TENANT_ID = ? ";

        if (!StringUtils.isEmpty(filter.getAppType())) {
            sql += "AND AP_APP.TYPE = ? ";
        }
        if (!StringUtils.isEmpty(filter.getAppName())) {
            sql += "AND LOWER (AP_APP.NAME) ";
            if (filter.isFullMatch()) {
                sql += "= ? ";
            } else {
                sql += "LIKE ? ";
            }
        }
        if (!StringUtils.isEmpty(filter.getSubscriptionType())) {
            sql += "AND AP_APP.SUB_TYPE = ? ";
        }
        if (filter.getMinimumRating() > 0) {
            sql += "AND AP_APP.RATING >= ? ";
        }
        if (!StringUtils.isEmpty(filter.getVersion())) {
            sql += "AND AP_APP_RELEASE.VERSION = ? ";
        }
        if (!StringUtils.isEmpty(filter.getAppReleaseType())) {
            sql += "AND AP_APP_RELEASE.RELEASE_TYPE = ? ";
        }
        if (!StringUtils.isEmpty(filter.getAppReleaseState())) {
            sql += "AND AP_APP_RELEASE.CURRENT_STATE = ? ";
        }
        if (deviceTypeId != -1) {
            sql += "AND AP_APP.DEVICE_TYPE_ID = ? ";
        }
        sql += "GROUP BY AP_APP.ID ";
        if (!StringUtils.isEmpty(filter.getSortBy())) {
            sql += "ORDER BY ID " + filter.getSortBy() +" ";
        }
        if (filter.getLimit() != -1) {
            sql += "LIMIT ? OFFSET ? ";
        }
        sql += ") AS app_data ON app_data.ID = AP_APP.ID WHERE AP_APP.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIndex = 1;
                stmt.setInt(paramIndex++, tenantId);
                if (!StringUtils.isEmpty(filter.getAppType())) {
                    stmt.setString(paramIndex++, filter.getAppType());
                }
                if (!StringUtils.isEmpty(filter.getAppName())) {
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
                if (deviceTypeId > 0) {
                    stmt.setInt(paramIndex++, deviceTypeId);
                }
                if (filter.getLimit() != -1) {
                    stmt.setInt(paramIndex++, filter.getLimit());
                    stmt.setInt(paramIndex++, filter.getOffset());
                }
                stmt.setInt(paramIndex, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return DAOUtil.loadApplications(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection while getting application list for the "
                    + "tenant " + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while getting application list for the tenant " + tenantId + ". While "
                    + "executing " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int getApplicationCount(Filter filter,int deviceTypeId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application count for filtering app data from the database");
        }
        int paramIndex = 1;
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT count(AP_APP.ID) AS APP_COUNT "
                + "FROM AP_APP "
                + "INNER JOIN AP_APP_RELEASE ON "
                + "AP_APP.ID = AP_APP_RELEASE.AP_APP_ID "
                + "INNER JOIN (SELECT ID FROM AP_APP) AS app_data ON app_data.ID = AP_APP.ID "
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
        if (deviceTypeId != -1) {
            sql += " AND AP_APP.DEVICE_TYPE_ID = ?";
        }

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
                stmt.setInt(paramIndex, deviceTypeId);
            }
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("APP_COUNT");
            }
            return 0;
        } catch (SQLException e) {
            String msg = "Error occurred while getting application list for the tenant" + " " + tenantId
                    + ". While executing " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection while getting application list for the "
                    + "tenant " + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } finally {
            DAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public ApplicationDTO getApplication(String releaseUuid, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application with the release UUID: " + releaseUuid + " from the database");
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
                + "INNER JOIN AP_APP_RELEASE ON "
                + "AP_APP.ID = AP_APP_RELEASE.AP_APP_ID AND "
                + "AP_APP.TENANT_ID = AP_APP_RELEASE.TENANT_ID "
                + "WHERE "
                + "AP_APP.ID = (SELECT AP_APP_RELEASE.AP_APP_ID FROM AP_APP_RELEASE WHERE AP_APP_RELEASE.UUID = ?) "
                + "AND AP_APP.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, releaseUuid);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully retrieved basic details of the application for the application "
                                + "release UUID:  " + releaseUuid);
                    }
                    return DAOUtil.loadApplication(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get application for application release "
                    + "UUID: " + releaseUuid;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while getting application details with app release uuid " + releaseUuid
                    + " while executing query. Executed query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (UnexpectedServerErrorException e) {
            String msg = "Found more than one application for application release UUID: " + releaseUuid;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public ApplicationDTO getAppWithRelatedRelease(String releaseUuid, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application and releated application release for the release UUID: " + releaseUuid +
                    " from the database");
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
                + "INNER JOIN AP_APP_RELEASE ON "
                + "AP_APP.ID = AP_APP_RELEASE.AP_APP_ID AND "
                + "AP_APP.TENANT_ID = AP_APP_RELEASE.TENANT_ID "
                + "WHERE "
                + "AP_APP_RELEASE.UUID = ? "
                + "AND AP_APP.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, releaseUuid);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully retrieved basic details of the application and related application "
                                + "release for the application release which has UUID:  " + releaseUuid);
                    }
                    return DAOUtil.loadApplication(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get application and related application "
                    + "release for release UUID: " + releaseUuid;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while getting application and related app release details for app release "
                    + "uuid " + releaseUuid + " while executing query. Executed query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (UnexpectedServerErrorException e) {
            String msg = "Found more than one application for application release UUID: " + releaseUuid;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<ApplicationDTO> getAppWithRelatedReleases(List<String> packageNames, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application and related application releases which has package names: " + packageNames
                    + " from the database");
        }
        try {
            Connection conn = this.getDBConnection();
            int index = 1;
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT "
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
                            + "INNER JOIN AP_APP_RELEASE ON "
                            + "AP_APP.ID = AP_APP_RELEASE.AP_APP_ID AND "
                            + "AP_APP.TENANT_ID = AP_APP_RELEASE.TENANT_ID "
                            + "WHERE "
                            + "AP_APP_RELEASE.PACKAGE_NAME IN (",
                    ") AND AP_APP.TENANT_ID = ?");
            packageNames.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (String packageName : packageNames) {
                    ps.setObject(index++, packageName);
                }
                ps.setInt(index, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully retrieved basic details of the application and related application "
                                + "release for the application release which has package names:  " + packageNames);
                    }
                    return DAOUtil.loadApplications(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get application and related application "
                    + "releases which has package names: " + packageNames;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while getting application and related app release details of releases which "
                    + "has package names " + packageNames + " while executing query.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public ApplicationDTO getApplication(int applicationId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting application with the id (" + applicationId + ") from the database");
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
                + "INNER JOIN AP_APP_RELEASE ON "
                + "AP_APP.ID = AP_APP_RELEASE.AP_APP_ID AND "
                + "AP_APP.TENANT_ID = AP_APP_RELEASE.TENANT_ID "
                + "WHERE "
                + "AP_APP.ID =? AND "
                + "AP_APP.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, applicationId);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully retrieved basic details of the application with the id "
                                + applicationId);
                    }
                    return DAOUtil.loadApplication(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get application for application ID: "
                    + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred to get application details with app id " + applicationId + " while executing "
                    + "query. Query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (UnexpectedServerErrorException e) {
            String msg = "Found more than one application for application ID: " + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean updateApplication(ApplicationDTO applicationDTO, int tenantId)
            throws ApplicationManagementDAOException {
        String sql = "UPDATE AP_APP " +
                "SET " +
                "NAME = ?,  " +
                "DESCRIPTION = ?, " +
                "SUB_TYPE = ?, " +
                "CURRENCY = ? " +
                "WHERE ID = ? AND TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
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
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when executing SQL to update an application. Executed query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void updateApplicationRating(String uuid, double rating, int tenantId)
            throws ApplicationManagementDAOException {
        String sql = "UPDATE AP_APP " +
                "SET " +
                "RATING = ? " +
                "WHERE " +
                "ID = (SELECT AP_APP_ID FROM AP_APP_RELEASE WHERE UUID = ?) AND " +
                "TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, rating);
                stmt.setString(2, uuid);
                stmt.setInt(3, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to update the application rating.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when obtaining database connection for updating the application rating.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }


    @Override
    public void retireApplication(int appId) throws ApplicationManagementDAOException {
        String sql = "UPDATE "
                + "AP_APP "
                + "SET STATUS = ? "
                + "WHERE ID = ? ";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, AppLifecycleState.RETIRED.toString());
                stmt.setInt(2, appId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to retire application which has application "
                    + "ID: " + appId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to retire an application which has application ID "
                    + appId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void addTags(List<String> tags, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to add tags");
        }
        String sql = "INSERT INTO AP_APP_TAG "
                + "(TAG, "
                + " TENANT_ID) "
                + "VALUES (?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (String tag : tags) {
                    stmt.setString(1, tag);
                    stmt.setInt(2, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when adding tags";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while adding tags. Executed Query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<TagDTO> getAllTags(int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get all tags");
        }
        String sql = "SELECT "
                + "AP_APP_TAG.ID AS ID, "
                + "AP_APP_TAG.TAG AS TAG "
                + "FROM AP_APP_TAG "
                + "WHERE TENANT_ID = ?";
        try {
            List<TagDTO> tagEntities = new ArrayList<>();
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        TagDTO tagDTO = new TagDTO();
                        tagDTO.setId(rs.getInt("ID"));
                        tagDTO.setTagName(rs.getString("TAG"));
                        tagEntities.add(tagDTO);
                    }
                    return tagEntities;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when getting all tags";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting all tags";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<CategoryDTO> getAllCategories(int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get all categories.");
        }
        String sql = "SELECT "
                + "AP_APP_CATEGORY.ID AS ID, "
                + "AP_APP_CATEGORY.CATEGORY AS CATEGORY "
                + "FROM AP_APP_CATEGORY "
                + "WHERE TENANT_ID = ?";
        try {
            List<CategoryDTO> categories = new ArrayList<>();
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        CategoryDTO category = new CategoryDTO();
                        category.setId(rs.getInt("ID"));
                        category.setCategoryName(rs.getString("CATEGORY"));
                        categories.add(category);
                    }
                    return categories;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when getting all categories.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting all categories. Executed query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> getCategoryIdsForCategoryNames(List<String> categoryNames, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get category ids for given category names");
        }
        try {
            Connection conn = this.getDBConnection();
            int index = 1;
            List<Integer> tagIds = new ArrayList<>();
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT AP_APP_CATEGORY.ID AS ID FROM AP_APP_CATEGORY WHERE AP_APP_CATEGORY.CATEGORY IN (",
                    ") AND TENANT_ID = ?");
            categoryNames.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (String categoryName : categoryNames) {
                    ps.setObject(index++, categoryName);
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
            String msg = "Error occurred while obtaining the DB connection when getting category ids for given "
                    + "category names";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting all categories.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> getDistinctCategoryIdsInCategoryMapping() throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get distinct category ids in category mapping.");
        }
        try {
            Connection conn = this.getDBConnection();
            List<Integer> distinctCategoryIds = new ArrayList<>();
            String sql = "SELECT DISTINCT AP_APP_CATEGORY_ID AS ID FROM AP_APP_CATEGORY_MAPPING;";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        distinctCategoryIds.add(rs.getInt("ID"));
                    }
                }
            }
            return distinctCategoryIds;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when getting distinct category ids in "
                    + "category mapping";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting distinct category ids in category mapping.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public CategoryDTO getCategoryForCategoryName(String categoryName, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get category for given category name.");
        }
        String sql = "SELECT AP_APP_CATEGORY.ID AS ID "
                + "FROM AP_APP_CATEGORY "
                + "WHERE AP_APP_CATEGORY.CATEGORY = ? AND "
                + "AP_APP_CATEGORY.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, categoryName);
                ps.setInt(2, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        CategoryDTO categoryDTO = new CategoryDTO();
                        categoryDTO.setId(rs.getInt("ID"));
                        categoryDTO.setCategoryName(categoryName);
                        return categoryDTO;
                    }
                    return null;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when getting category for given category "
                    + "name.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting category for category name. Executed query " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void addCategories(List<String> categories, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("DAO Request received in DAO Layer to add categories.");
        }
        String sql = "INSERT INTO AP_APP_CATEGORY "
                + "(CATEGORY,"
                + " TENANT_ID) "
                + "VALUES (?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (String category : categories) {
                    stmt.setString(1, category);
                    stmt.setInt(2, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when adding categories.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while adding categories. Executed query " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void addCategoryMapping(List<Integer> categoryIds, int applicationId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to add category mappings");
        }
        String sql = "INSERT INTO AP_APP_CATEGORY_MAPPING "
                + "(AP_APP_CATEGORY_ID, "
                + "AP_APP_ID, "
                + " TENANT_ID) "
                + "VALUES (?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Integer categoryId : categoryIds) {
                    stmt.setInt(1, categoryId);
                    stmt.setInt(2, applicationId);
                    stmt.setInt(3, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when adding data into category mapping.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while adding data into category mapping.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteAppCategories(int applicationId, int tenantId) throws ApplicationManagementDAOException{
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete Category mappings.");
        }
        String sql = "DELETE FROM "
                + "AP_APP_CATEGORY_MAPPING "
                + "WHERE "
                + "AP_APP_ID = ? AND "
                + "TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, applicationId);
                stmt.setInt(2, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when deleting category mapping of "
                    + "application ID: " + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred when deleting category mapping of application ID: " + applicationId
                    + " Executed query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteAppCategories(List<Integer> categoryIds, int applicationId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete application category.");
        }
        String sql = "DELETE FROM "
                + "AP_APP_CATEGORY_MAPPING WHERE "
                + "AP_APP_CATEGORY_ID = ? AND "
                + "AP_APP_ID = ? AND "
                + "TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                for (Integer categoryId : categoryIds){
                    stmt.setInt(1, categoryId);
                    stmt.setInt(2, applicationId);
                    stmt.setInt(3, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when deleting category mapping.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred when deleting category mapping. Executed query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteCategory(int categoryId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete category.");
        }
        String sql = "DELETE FROM " +
                "AP_APP_CATEGORY " +
                "WHERE " +
                "ID = ? AND " +
                "TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, categoryId);
                stmt.setInt(2, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when deleting category which has ID: "
                    + categoryId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred when deleting category which has ID: " + categoryId + ". Query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void updateCategory(CategoryDTO categoryDTO, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to update a category.");
        }
        String sql = "UPDATE " +
                "AP_APP_CATEGORY " +
                "SET CATEGORY = ? " +
                "WHERE " +
                "ID = ? AND " +
                "TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, categoryDTO.getCategoryName());
                stmt.setInt(2, categoryDTO.getId());
                stmt.setInt(3, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when updating category which has ID: "
                    + categoryDTO.getId();
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when updating category which has ID: " + categoryDTO.getId() + ". Executed "
                    + "query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
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
                    "SELECT AP_APP_TAG.ID AS ID FROM AP_APP_TAG WHERE AP_APP_TAG.TAG IN (",
                    ") AND TENANT_ID = ?");
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
            String msg = "Error occurred while obtaining the DB connection when getting tag IDs for given tag names.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting tag IDs for given tag names";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public TagDTO getTagForTagName(String tagName, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get tag for given tag name.");
        }
        String sql = "SELECT AP_APP_TAG.ID AS ID"
                + " FROM AP_APP_TAG "
                + "WHERE AP_APP_TAG.TAG = ? AND "
                + "AP_APP_TAG.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tagName);
                ps.setInt(2, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        TagDTO tagDTO = new TagDTO();
                        tagDTO.setId(rs.getInt("ID"));
                        tagDTO.setTagName(tagName);
                        return tagDTO;
                    }
                }
            }
            return null;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when getting tag for given tag name: "
                    + tagName;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting tag for tag name: " + tagName + ". Executed query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> getDistinctTagIdsInTagMapping() throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get distinct tag ids in tag mapping.");
        }
        String sql = "SELECT "
                + "DISTINCT "
                + "tm.AP_APP_TAG_ID AS ID "
                + "FROM AP_APP_TAG_MAPPING tm";
        try {
            Connection conn = this.getDBConnection();
            List<Integer> distinctTagIds = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        distinctTagIds.add(rs.getInt("ID"));
                    }
                }
            }
            return distinctTagIds;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when getting distinct tag ids in tag "
                    + "mapping";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting distinct tag ids in tag mapping. Executed query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void addTagMapping(List<Integer> tagIds, int applicationId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to add application tags which has application ID: "
                    + applicationId);
        }
        String sql = "INSERT INTO AP_APP_TAG_MAPPING "
                + "(AP_APP_TAG_ID, "
                + "AP_APP_ID, "
                + " TENANT_ID) "
                + "VALUES (?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Integer tagId : tagIds) {
                    stmt.setInt(1, tagId);
                    stmt.setInt(2, applicationId);
                    stmt.setInt(3, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to add tags for application which has ID: "
                    + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred when adding tags for application which has the ID: " + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<String> getAppTags(int appId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get tags of application which has application Id " + appId);
        }
        List<String> tags = new ArrayList<>();
        String sql = "SELECT tag.TAG AS TAG "
                + "FROM "
                + "AP_APP_TAG tag INNER JOIN AP_APP_TAG_MAPPING tag_map ON tag.ID = tag_map.AP_APP_TAG_ID "
                + "INNER JOIN AP_APP app ON tag_map.AP_APP_ID = app.ID "
                + "WHERE app.ID = ? AND app.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
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
            String msg = "Error occurred while obtaining the DB connection to get application tags. Application Id: "
                    + appId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL error occured while getting application tags. ApplicationId: " + appId + " Executed "
                    + "query " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean hasTagMapping (int tagId, int applicationId, int tenantId) throws ApplicationManagementDAOException{
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to verify whether tag is associated with an application.");
        }
        String sql = "SELECT tm.AP_APP_ID AS ID "
                + "FROM AP_APP_TAG_MAPPING tm "
                + "WHERE "
                + "tm.AP_APP_TAG_ID = ? AND "
                + "tm.AP_APP_ID = ? AND "
                + "tm.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tagId);
                stmt.setInt(2, applicationId);
                stmt.setInt(3, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when verifying the existence of a tag "
                    + "mapping. Application ID " + applicationId + " tag ID: " + tagId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred when verifying the existence of a tag mapping. Application ID "
                    + applicationId + " tag ID " + tagId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean hasTagMapping (int tagId, int tenantId) throws ApplicationManagementDAOException{
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to verify whether tag is associated with at least one "
                    + "application.");
        }
        String sql = "SELECT tm.AP_APP_ID AS ID "
                + "FROM AP_APP_TAG_MAPPING tm "
                + "WHERE "
                + "tm.AP_APP_TAG_ID = ? AND "
                + "tm.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tagId);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to verify whether tag is associated with at "
                    + "least one application. Tag ID " + tagId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing the query to verify whether tag is associated with at least "
                    + "one application. Tag ID " + tagId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteApplicationTags(List<Integer> tagIds, int applicationId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete Tag mappings.");
        }
        String sql = "DELETE FROM "
                + "AP_APP_TAG_MAPPING WHERE "
                + "AP_APP_TAG_ID = ? AND "
                + "AP_APP_ID = ? AND "
                + "TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
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
            String msg = "Error occurred while obtaining the DB connection to delete tag mapping. Application ID: "
                    + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while executing the query to delete tag mapping. Application ID: "
                    + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteApplicationTag (Integer tagId, int applicationId, int tenantId) throws ApplicationManagementDAOException{
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete Tag mapping.");
        }
        String sql = "DELETE FROM "
                + "AP_APP_TAG_MAPPING "
                + "WHERE "
                + "AP_APP_TAG_ID = ? AND "
                + "AP_APP_ID = ? AND "
                + "TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tagId);
                stmt.setInt(2, applicationId);
                stmt.setInt(3, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to delete a tag mapping. Application ID "
                    + applicationId + " tag ID " + tagId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while executing the query to delete a tag mapping. Application ID "
                    + applicationId + " tag ID " + tagId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteApplicationTags(int applicationId, int tenantId) throws ApplicationManagementDAOException{
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete application tags for application ID " + applicationId);
        }
        String sql = "DELETE FROM "
                + "AP_APP_TAG_MAPPING "
                + "WHERE "
                + "AP_APP_ID = ? AND "
                + "TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, applicationId);
                stmt.setInt(2, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when deleting application tags for "
                    + "application ID: " + applicationId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred when deleting application tags for application ID: " + applicationId + "."
                    + " Executed query " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteTagMapping(int tagId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete application tag. Tag Id " + tagId);
        }
        String sql = "DELETE FROM " +
                "AP_APP_TAG_MAPPING " +
                "WHERE " +
                "AP_APP_ID = ? AND " +
                "TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tagId);
                stmt.setInt(2, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when deleting application tag which has tag"
                    + " ID: " + tagId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL error occurred when deleting application tag which has tag ID: " + tagId + ". executed "
                    + "query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteTag(int tagId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete Tag which has tag ID " + tagId);
        }
        String sql = "DELETE FROM " +
                "AP_APP_TAG " +
                "WHERE " +
                "ID = ? AND " +
                "TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tagId);
                stmt.setInt(2, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when deleting tag which has ID: " + tagId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred when deleting tag which has ID: " + tagId + ". Executed query " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void updateTag(TagDTO tagDTO, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to update a Tag.");
        }
        String sql = "UPDATE " +
                "AP_APP_TAG " +
                "SET TAG = ? " +
                "WHERE " +
                "ID = ? AND " +
                "TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, tagDTO.getTagName());
                stmt.setInt(2, tagDTO.getId());
                stmt.setInt(3, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to update tag which has ID: "
                    + tagDTO.getId();
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred when updating tag which has ID: " + tagDTO.getId() + ". Executed query: "
                    + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<String> getAppCategories(int appId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get application categories for given application.");
        }
        List<String> categories = new ArrayList<>();
        String sql = "SELECT CATEGORY "
                + "FROM "
                + "AP_APP_CATEGORY cat INNER JOIN AP_APP_CATEGORY_MAPPING cat_map ON cat.ID = cat_map.AP_APP_CATEGORY_ID "
                + "INNER JOIN AP_APP app ON cat_map.AP_APP_ID = app.ID "
                + "WHERE app.ID = ? AND app.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
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
            String msg = "Error occurred while obtaining the DB connection to get application categories for "
                    + "application which has ID " + appId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL error occurred while executing query to get application categories for "
                    + "application which has ID " + appId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean hasCategoryMapping (int categoryId, int tenantId) throws ApplicationManagementDAOException{
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to verify whether tag is associated with at least one application.");
        }
        String sql = "SELECT cm.AP_APP_ID AS ID "
                + "FROM AP_APP_CATEGORY_MAPPING cm "
                + "WHERE "
                + "cm.AP_APP_CATEGORY_ID = ? AND "
                + "cm.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, categoryId);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when verifying the existence of a category "
                    + "mapping for category ID " + categoryId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred when verifying the existence of a category mapping for category ID "
                    + categoryId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean isExistingAppName(String appName, int deviceTypeId, int tenantId) throws ApplicationManagementDAOException {
        String sql = "SELECT AP_APP.ID AS ID "
                + "FROM AP_APP "
                + "WHERE "
                + "AP_APP.NAME = ? AND "
                + "AP_APP.DEVICE_TYPE_ID = ? AND "
                + "AP_APP.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, appName);
                stmt.setInt(2, deviceTypeId);
                stmt.setInt(3, tenantId);
                try (ResultSet rs = stmt.executeQuery()){
                    return rs.next();
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to check whether the existence of "
                    + "application name for device type which has device type ID " + deviceTypeId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to check whether the existence of application name for "
                    + "device type which has device type ID " + deviceTypeId + ". executed query " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteApplication(int appId, int tenantId) throws ApplicationManagementDAOException {
        String sql = "DELETE FROM AP_APP "
                + "WHERE ID = ? AND "
                + "TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1, appId);
                stmt.setInt(2, tenantId);
                stmt.executeUpdate();

            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to delete application for application id::."
                    + appId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while deleting application for application ID: " + appId + " Executed "
                    + "query " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

}
