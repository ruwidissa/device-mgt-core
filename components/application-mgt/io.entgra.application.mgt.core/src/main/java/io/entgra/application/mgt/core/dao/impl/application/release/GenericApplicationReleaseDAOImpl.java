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

package io.entgra.application.mgt.core.dao.impl.application.release;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.application.mgt.common.dto.ApplicationReleaseDTO;
import io.entgra.application.mgt.common.Rating;
import io.entgra.application.mgt.common.exception.DBConnectionException;
import io.entgra.application.mgt.core.dao.ApplicationReleaseDAO;
import io.entgra.application.mgt.core.util.DAOUtil;
import io.entgra.application.mgt.core.dao.impl.AbstractDAOImpl;
import io.entgra.application.mgt.core.exception.ApplicationManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

    /**
 * GenericApplicationReleaseDAOImpl holds the implementation of ApplicationRelease related DAO operations.
 */
public class GenericApplicationReleaseDAOImpl extends AbstractDAOImpl implements ApplicationReleaseDAO {

    private static final Log log = LogFactory.getLog(GenericApplicationReleaseDAOImpl.class);

    @Override
    public ApplicationReleaseDTO createRelease(ApplicationReleaseDTO applicationReleaseDTO, int appId, int tenantId)
            throws ApplicationManagementDAOException {
        String sql = "INSERT INTO AP_APP_RELEASE "
                + "(DESCRIPTION,"
                + "VERSION,"
                + "TENANT_ID,"
                + "UUID,"
                + "RELEASE_TYPE,"
                + "PACKAGE_NAME,"
                + "APP_PRICE, "
                + "INSTALLER_LOCATION,"
                + "ICON_LOCATION,"
                + "BANNER_LOCATION,"
                + "SC_1_LOCATION,"
                + "SC_2_LOCATION,"
                + "SC_3_LOCATION,"
                + "APP_HASH_VALUE,"
                + "SHARED_WITH_ALL_TENANTS,"
                + "APP_META_INFO,"
                + "SUPPORTED_OS_VERSIONS,"
                + "CURRENT_STATE,"
                + "AP_APP_ID) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            Connection connection = this.getDBConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
                statement.setString(1, applicationReleaseDTO.getDescription());
                statement.setString(2, applicationReleaseDTO.getVersion());
                statement.setInt(3, tenantId);
                statement.setString(4, applicationReleaseDTO.getUuid());
                statement.setString(5, String.valueOf(applicationReleaseDTO.getReleaseType()));
                statement.setString(6, String.valueOf(applicationReleaseDTO.getPackageName()));
                statement.setDouble(7, applicationReleaseDTO.getPrice());
                statement.setString(8, applicationReleaseDTO.getInstallerName());
                statement.setString(9, applicationReleaseDTO.getIconName());
                statement.setString(10, applicationReleaseDTO.getBannerName());
                statement.setString(11, applicationReleaseDTO.getScreenshotName1());
                statement.setString(12, applicationReleaseDTO.getScreenshotName2());
                statement.setString(13, applicationReleaseDTO.getScreenshotName3());
                statement.setString(14, applicationReleaseDTO.getAppHashValue());
                statement.setBoolean(15, applicationReleaseDTO.getIsSharedWithAllTenants());
                statement.setString(16, applicationReleaseDTO.getMetaData());
                statement.setString(17, applicationReleaseDTO.getSupportedOsVersions());
                statement.setString(18, applicationReleaseDTO.getCurrentState().toUpperCase());
                statement.setInt(19, appId);
                statement.executeUpdate();
                try(ResultSet resultSet = statement.getGeneratedKeys()){
                    if (resultSet.next()) {
                        applicationReleaseDTO.setId(resultSet.getInt(1));
                    }
                    return applicationReleaseDTO;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Database Connection error occurred while trying to release a new version for application which"
                    + " has app ID: " + appId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Exception while trying to release an application by executing the query " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public ApplicationReleaseDTO getReleaseByUUID( String uuid, int tenantId) throws ApplicationManagementDAOException {
        String sql = "SELECT "
                + "AR.ID AS RELEASE_ID, "
                + "AR.DESCRIPTION AS RELEASE_DESCRIPTION, "
                + "AR.VERSION AS RELEASE_VERSION, "
                + "AR.UUID AS RELEASE_UUID, "
                + "AR.RELEASE_TYPE AS RELEASE_TYPE, "
                + "AR.INSTALLER_LOCATION AS AP_RELEASE_STORED_LOC, "
                + "AR.ICON_LOCATION AS AP_RELEASE_ICON_LOC, "
                + "AR.BANNER_LOCATION AS AP_RELEASE_BANNER_LOC, "
                + "AR.SC_1_LOCATION AS AP_RELEASE_SC1, "
                + "AR.SC_2_LOCATION AS AP_RELEASE_SC2, "
                + "AR.SC_3_LOCATION AS AP_RELEASE_SC3, "
                + "AR.APP_HASH_VALUE AS RELEASE_HASH_VALUE, "
                + "AR.APP_PRICE AS RELEASE_PRICE, "
                + "AR.APP_META_INFO AS RELEASE_META_INFO, "
                + "AR.PACKAGE_NAME AS PACKAGE_NAME, "
                + "AR.SUPPORTED_OS_VERSIONS AS RELEASE_SUP_OS_VERSIONS, "
                + "AR.RATING AS RELEASE_RATING, "
                + "AR.CURRENT_STATE AS RELEASE_CURRENT_STATE, "
                + "AR.RATED_USERS AS RATED_USER_COUNT "
                + "FROM AP_APP_RELEASE AS AR "
                + "WHERE AR.UUID = ? AND AR.TENANT_ID = ?";

        try {
            Connection connection = this.getDBConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid);
                statement.setInt(2, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return DAOUtil.constructAppReleaseDTO(resultSet);
                    }
                    return null;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Database connection error occurred while trying to get application release details which has "
                    + "UUID: " + uuid;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg =
                    "Error while getting application release details which has UUID: " + uuid + " , while executing"
                            + " the query " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void updateRatingValue(String uuid, double rating, int ratedUsers) throws ApplicationManagementDAOException {
        String sql = "UPDATE "
                + "AP_APP_RELEASE "
                + "SET "
                + "RATING = ?, "
                + "RATED_USERS = ? "
                + "WHERE UUID = ?";
        try {
            Connection connection = this.getDBConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)){
                statement.setDouble(1, rating);
                statement.setInt(2, ratedUsers);
                statement.setString(3, uuid);
                statement.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Database connection error occurred while trying to update the application release rating "
                    + "value for UUID: " + uuid;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL exception occured while updating the release rating value. Executed query " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public Rating getReleaseRating(String uuid, int tenantId) throws ApplicationManagementDAOException {
        String sql = "SELECT "
                + "RATING, "
                + "RATED_USERS "
                + "FROM AP_APP_RELEASE "
                + "WHERE UUID = ? AND TENANT_ID = ?";
        try {
            Connection connection = this.getDBConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid);
                statement.setInt(2, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Rating rating = new Rating();
                        rating.setRatingValue(resultSet.getDouble("RATING"));
                        rating.setNoOfUsers(resultSet.getInt("RATED_USERS"));
                        return rating;
                    }
                }
            }
            return null;
        } catch (DBConnectionException e) {
            String msg = "Database connection error occured when try to get application release rating which has "
                    + "application release UUID: " + uuid;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL exception occured whn processing query: " + sql + " to get application release rating "
                    + "which has application release uuid: " + uuid;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Double> getReleaseRatings(String uuid, int tenantId) throws ApplicationManagementDAOException {
        List<Double> ratingValues = new ArrayList<>();
        String sql = "SELECT "
                + "RATING "
                + "FROM AP_APP_RELEASE "
                + "WHERE "
                + "AP_APP_ID = (SELECT AP_APP_ID FROM AP_APP_RELEASE WHERE UUID = ?) AND "
                + "TENANT_ID = ?";
        try {
            Connection connection = this.getDBConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid);
                statement.setInt(2, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        ratingValues.add(resultSet.getDouble("RATING"));
                    } return ratingValues;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Database connection exception occurred when getting all release rating values for a "
                    + "particular application.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL exception occurred while getting all release rating values for a particular application. "
                    + "Executed query is" + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public ApplicationReleaseDTO updateRelease(ApplicationReleaseDTO applicationReleaseDTO, int tenantId)
            throws ApplicationManagementDAOException {
        String sql = "UPDATE AP_APP_RELEASE "
                + "SET "
                    + "DESCRIPTION = ?, "
                    + "VERSION = ?, "
                    + "UUID = ?, "
                    + "RELEASE_TYPE = ?, "
                    + "PACKAGE_NAME = ?, "
                    + "APP_PRICE = ?, "
                    + "INSTALLER_LOCATION = ?, "
                    + "BANNER_LOCATION = ?, "
                    + "ICON_LOCATION = ?, "
                    + "SC_1_LOCATION = ?, "
                    + "SC_2_LOCATION = ?, "
                    + "SC_3_LOCATION = ?, "
                    + "APP_HASH_VALUE = ?, "
                    + "SHARED_WITH_ALL_TENANTS = ?, "
                    + "APP_META_INFO = ?, "
                    + "SUPPORTED_OS_VERSIONS = ?";

        if (applicationReleaseDTO.getCurrentState() != null) {
            sql += ", CURRENT_STATE =  ? ";
        }

        sql +=  " WHERE ID = ? AND TENANT_ID = ? ";

        int x = 17;
        try {
            Connection connection = this.getDBConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, applicationReleaseDTO.getDescription());
                statement.setString(2, applicationReleaseDTO.getVersion());
                statement.setString(3, applicationReleaseDTO.getUuid());
                statement.setString(4, applicationReleaseDTO.getReleaseType());
                statement.setString(5, applicationReleaseDTO.getPackageName());
                statement.setDouble(6, applicationReleaseDTO.getPrice());
                statement.setString(7, applicationReleaseDTO.getInstallerName());
                statement.setString(8, applicationReleaseDTO.getBannerName());
                statement.setString(9, applicationReleaseDTO.getIconName());
                statement.setString(10, applicationReleaseDTO.getScreenshotName1());
                statement.setString(11, applicationReleaseDTO.getScreenshotName2());
                statement.setString(12, applicationReleaseDTO.getScreenshotName3());
                statement.setString(13, applicationReleaseDTO.getAppHashValue());
                statement.setBoolean(14, applicationReleaseDTO.getIsSharedWithAllTenants());
                statement.setString(15, applicationReleaseDTO.getMetaData());
                statement.setString(16, applicationReleaseDTO.getSupportedOsVersions());

                if (applicationReleaseDTO.getCurrentState() != null) {
                    statement.setString(x++, applicationReleaseDTO.getCurrentState().toUpperCase());
                }
                statement.setInt(x++, applicationReleaseDTO.getId());
                statement.setInt(x, tenantId);
                if (statement.executeUpdate() == 0) {
                    return null;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Database connection exception occured while trying to update the application release which "
                    + "has application release ID: " + applicationReleaseDTO.getId();
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL exception occured while updating the application release which has release ID: "
                    + applicationReleaseDTO.getId() + ". Executed query is " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
        return applicationReleaseDTO;
    }

    @Override
    public void deleteRelease(int id) throws ApplicationManagementDAOException {
        String sql = "DELETE "
                + "FROM AP_APP_RELEASE "
                + "WHERE ID = ?";
        try {
            Connection connection = this.getDBConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Database connection exception occurred while trying to delete the application release which "
                    + "has  ID: " + id;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL exception occurred while deleting the release for release ID: " + id + ",while executing"
                    + " the query sql " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteReleases(List<Integer> applicationReleaseIds) throws ApplicationManagementDAOException{
        String sql = "DELETE "
                + "FROM AP_APP_RELEASE "
                + "WHERE ID = ?";
        try {
            Connection connection = this.getDBConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (Integer releaseId : applicationReleaseIds) {
                    statement.setInt(1, releaseId);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Database connection exception occurred while trying to delete application releases for given "
                    + "application release ids";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL exception occurred while execute delete query for deleting given application releases. "
                    + "Executed query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }


    @Override
    public boolean verifyReleaseExistenceByHash(String hashVal, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Verifying application release existence by application hash value: " + hashVal);
        }
        String sql = "SELECT "
                + "AR.ID AS RELEASE_ID "
                + "FROM AP_APP_RELEASE AS AR "
                + "WHERE AR.APP_HASH_VALUE = ? AND "
                + "AR.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, hashVal);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Database connection error occurred while verifying release existence for app release hash "
                    + "value. Hash value: " + hashVal;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while getting application release details for application release hash value: "
                    + hashVal + " While executing query ";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public String getPackageName(String releaseUuid, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting package name of the application release by application id:" + releaseUuid);
        }
        String sql = "SELECT "
                + "AR.PACKAGE_NAME AS PACKAGE_NAME "
                + "FROM AP_APP_RELEASE AS AR "
                + "WHERE AR.UUID = ? "
                + "AND AR.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, releaseUuid);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully retrieved package name of the application release with the UUID: "
                                + releaseUuid);
                    }
                    if (rs.next()) {
                        return rs.getString("PACKAGE_NAME");
                    }
                    return null;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get application release package name "
                    + "which has application release UUID: " + releaseUuid;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while getting package name of the application release with app UUID: "
                    + releaseUuid;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public String getReleaseHashValue(String uuid, int tenantId) throws ApplicationManagementDAOException{
        if (log.isDebugEnabled()) {
            log.debug("Getting application release artifact stored location paths for: " + uuid);
        }
        String sql = "SELECT "
                + "AR.APP_HASH_VALUE AS HASH_VALUE "
                + "FROM AP_APP_RELEASE AR "
                + "WHERE AR.UUID = ? AND AR.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, uuid);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()){
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully retrieved application release hash value for application release "
                                + "which has release UUID: " + uuid);
                    }
                    if(rs.next()){
                        return rs.getString("HASH_VALUE");
                    }
                    return null;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get hash value for application release "
                    + "which has application release UUID: " + uuid;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when executing query to get application release hash value which has "
                    + "application release uuid: " + uuid + ". Executed query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean isActiveReleaseExisitForPackageName(String packageName, int tenantId, String inactiveState)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Verifying application release existence for package name:" + packageName);
        }
        String sql = "SELECT AR.ID AS RELEASE_ID "
                + "FROM AP_APP_RELEASE AS AR "
                + "WHERE AR.PACKAGE_NAME = ? AND "
                + "AR.CURRENT_STATE != ? AND "
                + "AR.TENANT_ID = ? LIMIT 1";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, packageName);
                stmt.setString(2, inactiveState);
                stmt.setInt(3, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to verify the existence of package name for "
                    + "active application release. Package name: " + packageName;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL error occurred while verifying the existence of package name for active application "
                    + "release. package name: " + packageName;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean hasExistInstallableAppRelease(String releaseUuid, String installableStateName, int tenantId)
            throws ApplicationManagementDAOException{
        if (log.isDebugEnabled()) {
            log.debug("Verifying application release existence in the installable state: :" + installableStateName);
        }
        String sql = "SELECT AR.ID AS RELEASE_ID "
                + "FROM AP_APP_RELEASE AS AR "
                + "WHERE AR.CURRENT_STATE = ? AND "
                + "AR.AP_APP_ID = (SELECT AP_APP_ID FROM AP_APP_RELEASE WHERE UUID = ?) AND "
                + "AR.TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, installableStateName);
                stmt.setString(2, releaseUuid);
                stmt.setInt(3, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to verify the existence of app release for "
                    + "application release uuid ;" + releaseUuid + " and application release state "
                    + installableStateName;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to verify the existence of app release for application "
                    + "release uuid ;" + releaseUuid + " and application release state " + installableStateName;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<ApplicationReleaseDTO> getReleaseByPackages(List<String> packages, int tenantId) throws
            ApplicationManagementDAOException {

        List<ApplicationReleaseDTO> releaseDTOs = new ArrayList<>();
        if (packages.isEmpty()) {
            return releaseDTOs;
        }

        String sql = "SELECT "
                + "AR.ID AS RELEASE_ID, "
                + "AR.DESCRIPTION AS RELEASE_DESCRIPTION, "
                + "AR.VERSION AS RELEASE_VERSION, "
                + "AR.UUID AS RELEASE_UUID, "
                + "AR.RELEASE_TYPE AS RELEASE_TYPE, "
                + "AR.INSTALLER_LOCATION AS AP_RELEASE_STORED_LOC, "
                + "AR.ICON_LOCATION AS AP_RELEASE_ICON_LOC, "
                + "AR.BANNER_LOCATION AS AP_RELEASE_BANNER_LOC, "
                + "AR.SC_1_LOCATION AS AP_RELEASE_SC1, "
                + "AR.SC_2_LOCATION AS AP_RELEASE_SC2, "
                + "AR.SC_3_LOCATION AS AP_RELEASE_SC3, "
                + "AR.APP_HASH_VALUE AS RELEASE_HASH_VALUE, "
                + "AR.APP_PRICE AS RELEASE_PRICE, "
                + "AR.APP_META_INFO AS RELEASE_META_INFO, "
                + "AR.PACKAGE_NAME AS PACKAGE_NAME, "
                + "AR.SUPPORTED_OS_VERSIONS AS RELEASE_SUP_OS_VERSIONS, "
                + "AR.RATING AS RELEASE_RATING, "
                + "AR.CURRENT_STATE AS RELEASE_CURRENT_STATE, "
                + "AR.RATED_USERS AS RATED_USER_COUNT "
                + "FROM AP_APP_RELEASE AS AR "
                + "WHERE AR.PACKAGE_NAME IN (";

        StringJoiner joiner = new StringJoiner(",", sql, ") AND AR.TENANT_ID = ? ");
        packages.stream().map(ignored -> "?").forEach(joiner::add);
        sql = joiner.toString();

        try {
            Connection connection = this.getDBConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int index = 1;
                for (String packageName : packages) {
                    statement.setObject(index++, packageName);
                }
                statement.setInt(index, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        releaseDTOs.add(DAOUtil.constructAppReleaseDTO(resultSet));
                    }
                    return releaseDTOs;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Database connection error occurred while trying to get application release details which has "
                    + "packages: " + String.join(", ", packages);
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg =
                    "Error while getting application release details which has packages: " + String.join(", ", packages)
                            + " , while executing the query " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }
}
