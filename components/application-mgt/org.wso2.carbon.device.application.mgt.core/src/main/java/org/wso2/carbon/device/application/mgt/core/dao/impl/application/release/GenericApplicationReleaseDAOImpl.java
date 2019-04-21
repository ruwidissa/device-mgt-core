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

package org.wso2.carbon.device.application.mgt.core.dao.impl.application.release;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.AppLifecycleState;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.ApplicationReleaseArtifactPaths;
import org.wso2.carbon.device.application.mgt.common.Rating;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationReleaseDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * GenericApplicationReleaseDAOImpl holds the implementation of ApplicationReleaseDTO related DAO operations.
 */
public class GenericApplicationReleaseDAOImpl extends AbstractDAOImpl implements ApplicationReleaseDAO {

    private static final Log log = LogFactory.getLog(GenericApplicationReleaseDAOImpl.class);

    /**
     * To insert the ApplicationDTO Release Details.
     *
     * @param appId              Id of the application
     * @param applicationRelease ApplicationDTO Release the properties of which that need to be inserted.
     * @param tenantId           Tenant Id
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    @Override public ApplicationReleaseDTO createRelease(ApplicationReleaseDTO applicationRelease, int appId, int tenantId)
            throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

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
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

        String generatedColumns[] = { "ID" };
        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql, generatedColumns);
            statement.setString(1, applicationRelease.getDescription());
            statement.setString(2, applicationRelease.getVersion());
            statement.setInt(3, tenantId);
            statement.setString(4, applicationRelease.getUuid());
            statement.setString(5, String.valueOf(applicationRelease.getReleaseType()));
            statement.setString(6, String.valueOf(applicationRelease.getPackageName()));
            statement.setDouble(7, applicationRelease.getPrice());
            statement.setString(8, applicationRelease.getInstallerName());
            statement.setString(9, applicationRelease.getIconName());
            statement.setString(10, applicationRelease.getBannerName());
            statement.setString(11, applicationRelease.getScreenshotName1());
            statement.setString(12, applicationRelease.getScreenshotName2());
            statement.setString(13, applicationRelease.getScreenshotName3());
            statement.setString(14, applicationRelease.getAppHashValue());
            statement.setBoolean(15, applicationRelease.getIsSharedWithAllTenants());
            statement.setString(16, applicationRelease.getMetaData());
            statement.setString(17, applicationRelease.getSupportedOsVersions());
            statement.setString(18, applicationRelease.getCurrentState());
            statement.setInt(19, appId);
            statement.executeUpdate();
            resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                applicationRelease.setId(resultSet.getInt(1));
            }
            return applicationRelease;
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL Exception while trying to release an application by executing the query " + sql, e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database Connection Exception while trying to release a new version", e);
        } finally {
            Util.cleanupResources(statement, resultSet);
        }
    }

    /**
     * To get latest updated app release details of a specific application.
     *
     * @param applicationName Name of the application.
     * @param applicationType Type of the application.
     * @param versionName     version name of the application.
     * @param releaseType     type of the application release.
     * @param tenantId        Tenant Id
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    @Override public ApplicationReleaseDTO getRelease(String applicationName, String applicationType, String versionName,
            String releaseType, int tenantId) throws ApplicationManagementDAOException {
        //todo no usage
        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sql = "SELECT AR.ID AS RELEASE_ID, AR.VERSION AS RELEASE_VERSION, AR.UUID AS UUID, AR.RELEASE_TYPE AS "
                + "RELEASE_TYPE, AR.PACKAGE_NAME AS PACKAGE_NAME, AR.APP_PRICE AS APP_PRICE, AR.STORED_LOCATION AS "
                + "STORED_LOCATION, AR.BANNER_LOCATION AS BANNER_LOCATION, AR.ICON_LOCATION AS "
                + "ICON_LOCATION, AR.SC_1_LOCATION AS SCREEN_SHOT_1, AR.SC_2_LOCATION AS SCREEN_SHOT_2, "
                + "AR.SC_3_LOCATION AS SCREEN_SHOT_3, AR.APP_HASH_VALUE AS HASH_VALUE, AR.SHARED_WITH_ALL_TENANTS "
                + "AS SHARED, AR.APP_META_INFO AS APP_META_INFO, AR.RATING AS RATING, AL.CURRENT_STATE, "
                + "AL.PREVIOUS_STATE, AL.UPDATED_BY, AL.UPDATED_AT FROM AP_APP_RELEASE AS AR, AP_APP_LIFECYCLE_STATE "
                + "AS AL WHERE AR.AP_APP_ID=(SELECT ID FROM AP_APP WHERE NAME=? AND TYPE=? AND TENANT_ID=?) "
                + "AND AR.VERSION=? AND AR.RELEASE_TYPE=? AND AL.AP_APP_RELEASE_ID=AR.ID "
                + "AND AL.TENANT_ID=AR.TENANT_ID ORDER BY AL.UPDATED_AT DESC;";

        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, applicationName);
            statement.setString(2, applicationType);
            statement.setInt(3, tenantId);
            statement.setString(4, versionName);
            statement.setString(5, releaseType);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Util.loadApplicationRelease(resultSet);
            }
            return null;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Database connection exception while trying to get the "
                    + "release details of the application with " + applicationName + " and version " + versionName, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error while getting release details of the application " + applicationName + " and version " + versionName + " , while executing the query " + sql, e);
        } finally {
            Util.cleanupResources(statement, resultSet);
        }
    }

    /**
     * To get release details of a specific application.
     *
     * @param applicationId ID of the application.
     * @param releaseUuid   UUID of the application release.
     * @param tenantId      Tenant Id
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    @Override public ApplicationReleaseDTO getReleaseByIds(int applicationId, String releaseUuid, int tenantId)
            throws ApplicationManagementDAOException {

        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sql =
                "SELECT AR.ID AS RELEASE_ID, AR.VERSION AS RELEASE_VERSION, AR.UUID, AR.RELEASE_TYPE, AR.APP_PRICE, "
                        + "AR.STORED_LOCATION, AR.BANNER_LOCATION, AR.SC_1_LOCATION AS SCREEN_SHOT_1, ICON_LOCATION,"
                        + "AR.SC_2_LOCATION AS SCREEN_SHOT_2, AR.SC_3_LOCATION AS SCREEN_SHOT_3, AR.PACKAGE_NAME AS "
                        + "PACKAGE_NAME, AR.APP_HASH_VALUE AS HASH_VALUE, AR.SHARED_WITH_ALL_TENANTS AS SHARED, "
                        + "AR.APP_META_INFO AS APP_META_INFO, AR.RATING AS RATING, AL.CURRENT_STATE, AL.PREVIOUS_STATE, "
                        + "AL.UPDATED_BY, AL.UPDATED_AT FROM AP_APP_RELEASE AS AR, AP_APP_LIFECYCLE_STATE AS AL "
                        + "WHERE AR.AP_APP_ID = ? AND AR.UUID = ? AND AR.TENANT_ID = ? AND AL.AP_APP_RELEASE_ID=AR.ID "
                        + "AND AL.TENANT_ID = AR.TENANT_ID ORDER BY AL.UPDATED_AT DESC;";

        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, applicationId);
            statement.setString(2, releaseUuid);
            statement.setInt(3, tenantId);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Util.loadApplicationRelease(resultSet);
            }
            return null;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database connection exception while trying to get the release details of the " + "application id: "
                            + applicationId + "and UUID of the application release:  " + releaseUuid, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error while getting release details of the application id: " + applicationId
                            + " and theUUID of the application " + "release: " + releaseUuid + " , while executing the query " + sql, e);
        } finally {
            Util.cleanupResources(statement, resultSet);
        }
    }

    @Override
    public ApplicationReleaseDTO getReleaseByUUID( String uuid, int tenantId) throws ApplicationManagementDAOException {
        Connection connection;
        String sql =
                "SELECT AR.DESCRIPTION AS RELEASE_DESCRIPTION, "
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
                        + "AR.SUPPORTED_OS_VERSIONS AS RELEASE_SUP_OS_VERSIONS, "
                        + "AR.RATING AS RELEASE_RATING, "
                        + "AR.CURRENT_STATE AS RELEASE_CURRENT_STATE, AR.RATED_USERS AS RATED_USER_COUNT "
                + "FROM AP_APP_RELEASE AS AR "
                + "WHERE AR.UUID = ? AND AR.TENAT_ID = ?";

        try {
            connection = this.getDBConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid);
                statement.setInt(2, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Util.loadAppRelease(resultSet);
                    }
                    return null;
                }
            }
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database connection exception while trying to get the release details of the UUID of the application release:  "
                            + uuid, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error while getting release details of the UUID of the application " + "release: " + uuid
                            + " , while executing the query " + sql, e);
        }
    }

    /**
     * To insert the application release properties.
     *
     * @param applicationId Id of the application.
     * @param tenantId      Tenant Id
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    @Override public List<ApplicationReleaseDTO> getReleases(int applicationId, int tenantId)
            throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<ApplicationReleaseDTO> applicationReleases = new ArrayList<>();
        String sql = "SELECT AR.ID AS RELEASE_ID, AR.VERSION AS RELEASE_VERSION, AR.UUID, AR.RELEASE_TYPE "
                + "AS RELEASE_TYPE, AR.PACKAGE_NAME AS PACKAGE_NAME, AR.APP_PRICE, AR.STORED_LOCATION, AR.ICON_LOCATION, "
                + "AR.BANNER_LOCATION, AR.SC_1_LOCATION AS SCREEN_SHOT_1, AR.SC_2_LOCATION AS SCREEN_SHOT_2, "
                + "AR.SC_3_LOCATION AS SCREEN_SHOT_3, AR.APP_HASH_VALUE AS HASH_VALUE, "
                + "AR.SHARED_WITH_ALL_TENANTS AS SHARED, AR.APP_META_INFO AS APP_META_INFO, "
                + "AR.RATING AS RATING FROM AP_APP_RELEASE AS AR where AR.AP_APP_ID=? AND AR.TENANT_ID = ?;";

        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, applicationId);
            statement.setInt(2, tenantId);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ApplicationReleaseDTO applicationRelease = Util.loadApplicationRelease(resultSet);
                applicationReleases.add(applicationRelease);
            }
            return applicationReleases;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Database connection exception while trying to get the "
                    + "release details of the application with app ID: " + applicationId, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error while getting all the release details of the app ID: " + applicationId
                            + ", while executing the query " + sql, e);
        } finally {
            Util.cleanupResources(statement, resultSet);
        }
    }

    //todo this has to be removed
    @Override
    public List<ApplicationReleaseDTO> getReleaseByState(int appId, int tenantId, String state) throws
            ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<ApplicationReleaseDTO> applicationReleases = new ArrayList<>();
        String sql = "SELECT AR.ID AS RELEASE_ID, AR.VERSION AS RELEASE_VERSION, AR.UUID AS UUID, AR.RELEASE_TYPE AS "
                + "RELEASE_TYPE, AR.PACKAGE_NAME AS PACKAGE_NAME, AR.APP_PRICE AS APP_PRICE, AR.STORED_LOCATION AS "
                + "STORED_LOCATION, AR.BANNER_LOCATION AS BANNER_LOCATION, ICON_LOCATION, AR.SC_1_LOCATION AS "
                + "SCREEN_SHOT_1, AR.SC_2_LOCATION AS SCREEN_SHOT_2, AR.SC_3_LOCATION AS SCREEN_SHOT_3, "
                + "AR.APP_HASH_VALUE AS HASH_VALUE, AR.SHARED_WITH_ALL_TENANTS AS SHARED, AR.APP_META_INFO AS "
                + "APP_META_INFO , AR.RATING AS RATING, AL.CURRENT_STATE, AL.PREVIOUS_STATE, AL.UPDATED_BY, "
                + "AL.UPDATED_AT FROM AP_APP_RELEASE AS AR, AP_APP_LIFECYCLE_STATE AS AL "
                + "WHERE AR.AP_APP_ID=? AND AL.AP_APP_RELEASE_ID=AR.ID AND AL.CURRENT_STATE=? AND AR.TENANT_ID=? AND "
                + "AL.TENANT_ID=AR.TENANT_ID ORDER BY AL.UPDATED_AT DESC;";

        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, appId);
            statement.setString(2, state);
            statement.setInt(3, tenantId);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ApplicationReleaseDTO appRelease = Util.loadApplicationRelease(resultSet);
                applicationReleases.add(appRelease);
            }
            return applicationReleases;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Database connection exception while trying to get the "
                    + "release details of the application with id " + appId, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error while getting all the release details of the app id" + appId + " application"
                            + ", while executing the query " + sql, e);
        } finally {
            Util.cleanupResources(statement, resultSet);
        }
    }

    /**
     * To Update starts of an application release.
     *
     * @param uuid   UUID of the application Release.
     * @param rating given stars for the application release.
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    @Override public void updateRatingValue(String uuid, double rating, int ratedUsers)
            throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        String sql = "UPDATE AP_APP_RELEASE SET RATING = ?, RATED_USERS = ? WHERE UUID = ?;";
        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setDouble(1, rating);
            statement.setInt(2, ratedUsers);
            statement.setString(3, uuid);
            statement.executeUpdate();
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database connection exception while trying to update the application release rating value", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL exception while updating the release rating value ,while executing the query " + sql, e);
        } finally {
            Util.cleanupResources(statement, null);
        }
    }

    /**
     * To retrieve rating of an application release.
     *
     * @param uuid UUID of the application Release.
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    @Override public Rating getRating(String uuid, int tenantId) throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Rating rating = null;
        String sql = "SELECT RATING, RATED_USERS FROM AP_APP_RELEASE WHERE UUID = ? AND TENANT_ID=?;";
        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, uuid);
            statement.setInt(2, tenantId);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                rating = new Rating();
                rating.setRatingValue(resultSet.getDouble("RATING"));
                rating.setNoOfUsers(resultSet.getInt("RATED_USERS"));
            }
            return rating;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database connection exception while trying to update the application release", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL exception while updating the release ,while executing the query " + sql, e);
        } finally {
            Util.cleanupResources(statement, resultSet);
        }
    }

    /**
     * To insert the application release properties.
     *
     * @param applicationReleaseDTO ApplicationDTO Release the properties of which that need to be inserted.
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    @Override
    public ApplicationReleaseDTO updateRelease(ApplicationReleaseDTO applicationReleaseDTO, int tenantId)
            throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
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
                    + "SUPPORTED_OS_VERSIONS = ?, "
                    + "CURRENT_STATE =  ? "
                + "WHERE  ID = ? AND TENANT_ID = ? ";
        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
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
            statement.setString(17, applicationReleaseDTO.getCurrentState());
            statement.setInt(18, tenantId);
            statement.setInt(19, applicationReleaseDTO.getId());
            if (statement.executeUpdate() == 0) {
                return null;
            }
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database connection exception while trying to update the application release", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL exception while updating the release ,while executing the query " + sql, e);
        } finally {
            Util.cleanupResources(statement, null);
        }
        return applicationReleaseDTO;
    }

    /**
     * To delete an application release.
     *
     * @param id      Id of the application Release.
     * @param version version name of the application release.
     * @throws ApplicationManagementDAOException ApplicationDTO Management DAO Exception.
     */
    @Override public void deleteRelease(int id, String version) throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        String sql = "DELETE FROM AP_APP_RELEASE WHERE ID = ? AND VERSION = ?";
        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.setString(2, version);
            statement.executeUpdate();
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database connection exception while trying to delete the release with version " + version, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL exception while deleting the release with version " + version + ",while executing the query "
                            + "sql", e);
        } finally {
            Util.cleanupResources(statement, null);
        }
    }

    @Override
    public boolean verifyReleaseExistenceByHash(String hashVal, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Verifying application release existence by application hash value: " + hashVal);
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql = "SELECT AR.ID AS RELEASE_ID FROM AP_APP_RELEASE AS AR WHERE AR.APP_HASH_VALUE = ? AND "
                    + "AR.TENANT_ID = ?;";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, hashVal);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while getting application release details for application release hash value: "
                            + hashVal + " While executing query ", e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public String getPackageName(int appId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting package name of the application release by application id:" + appId);
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql = "SELECT AR.PACKAGE_NAME AS PACKAGE_NAME FROM AP_APP_RELEASE AS AR WHERE AR.AP_APP_ID = ? "
                    + "AND AR.TENANT_ID = ? LIMIT 1;";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, appId);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();

            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved package name of the application release with the application ID "
                        + appId);
            }
            if (rs.next()){
                return rs.getString("PACKAGE_NAME");
            }
            return null;
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while getting package name of the application release with app ID: " + appId, e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while obtaining the DB connection to get application release package name.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public boolean verifyReleaseExistence(int appId, String uuid, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Verifying application release existence by application id:" + appId
                    + " and application release uuid: " + uuid);
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql =
                    "SELECT AR.ID AS RELEASE_ID FROM AP_APP_RELEASE AS AR WHERE AR.AP_APP_ID = ? AND AR.UUID = ? AND "
                            + "AR.TENANT_ID = ?;";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, appId);
            stmt.setString(2, uuid);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();

            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved basic details of the application release with the application ID "
                        + appId + " ApplicationDTO release uuid: " + uuid);
            }
            return rs.next();
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while getting application release details with app ID: " + appId
                            + " App release uuid: " + uuid + " While executing query ", e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public boolean verifyReleaseExistenceByUuid(String uuid, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Verifying application release existence by application release uuid: " + uuid);
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql =
                    "SELECT AR.ID AS RELEASE_ID FROM AP_APP_RELEASE AS AR WHERE AR.UUID = ? AND AR.TENANT_ID = ?;";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, uuid);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();

            if (log.isDebugEnabled()) {
                log.debug("Successfully retrieved basic details of the application release with the application UUID: "
                        + uuid);
            }
            return rs.next();
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred when executing query to get application release details for App release uuid: "
                            + uuid, e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public ApplicationReleaseArtifactPaths getReleaseArtifactPaths(String uuid, int tenantId) throws ApplicationManagementDAOException{
        if (log.isDebugEnabled()) {
            log.debug("Getting application release artifact stored location paths for: " + uuid);
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ApplicationReleaseArtifactPaths applicationReleaseArtifactPaths = null;
        try {
            conn = this.getDBConnection();
            String sql = "SELECT AR.INSTALLER_LOCATION AS INSTALLER,"
                            + "AR.ICON_LOCATION AS ICON,"
                            + "AR.BANNER_LOCATION AS BANNER,"
                            + "AR.SC_1_LOCATION AS SC1,"
                            + "AR.SC_2_LOCATION AS SC2,"
                            + "AR.SC_3_LOCATION AS SC3 "
                            + "FROM AP_APP_RELEASE AS AR "
                    + "WHERE AR.UUID = ? AND AR.TENANT_ID = ?;";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, uuid);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();

            if (log.isDebugEnabled()) {
                log.debug(
                        "Successfully retrieved application release artifact details of the application release with the application UUID: "
                                + uuid);
            }

            if (rs.getFetchSize() == 0 || rs.getFetchSize() >1){
                return null;
            }
            while(rs.next()){
                applicationReleaseArtifactPaths = new ApplicationReleaseArtifactPaths();
                List<String> scs = new ArrayList<>();
                applicationReleaseArtifactPaths.setInstallerPath(rs.getString("INSTALLER"));
                applicationReleaseArtifactPaths.setIconPath(rs.getString("ICON"));
                applicationReleaseArtifactPaths.setBannerPath(rs.getString("BANNER"));
                scs.add(rs.getString("SC1"));
                scs.add(rs.getString("SC2"));
                scs.add(rs.getString("SC3"));
                applicationReleaseArtifactPaths.setScreenshotPaths(scs);
            }
            return applicationReleaseArtifactPaths;
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred when executing query to get application release artifact paths for App release uuid: "
                            + uuid, e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public boolean isActiveReleaseExisitForPackageName(String packageName, int tenantId, String inactiveState)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Verifying application release existence for package name:" + packageName);
        }
        Connection conn;
        try {
            conn = this.getDBConnection();
            String sql = "SELECT AR.ID AS RELEASE_ID "
                    + "FROM AP_APP_RELEASE AS AR "
                    + "WHERE AR.PACKAGE_NAME = ? AND AR.CURRENT_STATE != ? AND AR.TENANT_ID = ? LIMIT 1";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, packageName);
                stmt.setString(2, inactiveState);
                stmt.setInt(3, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error occurred while getting application release details for package name: " + packageName, e);
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        }
    }

}