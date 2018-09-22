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
import org.json.JSONException;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.Rating;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationReleaseDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.dao.impl.application.GenericApplicationDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * GenericApplicationReleaseDAOImpl holds the implementation of ApplicationRelease related DAO operations.
 */
public class GenericApplicationReleaseDAOImpl extends AbstractDAOImpl implements ApplicationReleaseDAO {

    private static final Log log = LogFactory.getLog(GenericApplicationReleaseDAOImpl.class);


    /**
     * To insert the Application Release Details.
     *
     * @param appId Id of the application
     * @param applicationRelease Application Release the properties of which that need to be inserted.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    @Override
    public ApplicationRelease createRelease(ApplicationRelease applicationRelease, int appId, int tenantId) throws
            ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String sql = "INSERT INTO AP_APP_RELEASE (VERSION,TENANT_ID,UUID,RELEASE_TYPE, PACKAGE_NAME, APP_PRICE,"
                + "STORED_LOCATION, BANNER_LOCATION, SC_1_LOCATION,SC_2_LOCATION,SC_3_LOCATION, APP_HASH_VALUE,"
                + "SHARED_WITH_ALL_TENANTS, APP_META_INFO,CREATED_BY,AP_APP_ID) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

        int index = 0;
        String generatedColumns[] = {"ID"};
        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql, generatedColumns);
            statement.setString(++index, applicationRelease.getVersion());
            statement.setInt(++index, tenantId);
            statement.setString(++index, applicationRelease.getUuid());
            statement.setString(++index, String.valueOf(applicationRelease.getReleaseType()));
            statement.setString(++index, String.valueOf(applicationRelease.getPackageName()));
            statement.setDouble(++index, applicationRelease.getPrice());
            statement.setString(++index, applicationRelease.getAppStoredLoc());
            statement.setString(++index, applicationRelease.getBannerLoc());
            statement.setString(++index, applicationRelease.getScreenshotLoc1());
            statement.setString(++index, applicationRelease.getScreenshotLoc2());
            statement.setString(++index, applicationRelease.getScreenshotLoc3());
            statement.setString(++index, applicationRelease.getAppHashValue());
            statement.setInt(++index, applicationRelease.getIsSharedWithAllTenants());
            statement.setString(++index, applicationRelease.getMetaData());
            statement.setString(++index, applicationRelease.getApplicationCreator());
            statement.setInt(++index, appId);
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
                    "Database Connection Exception while trying to release a new version" , e);
        } finally {
            Util.cleanupResources(statement, resultSet);
        }
    }

    /**
     * To get release details of a specific application.
     *
     * @param applicationName Name of the application.
     * @param applicationType Type of the application.
     * @param versionName version name of the application.
     * @param releaseType type of the application release.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    @Override
    public ApplicationRelease getRelease(String applicationName, String applicationType, String versionName,
            String releaseType, int tenantId) throws ApplicationManagementDAOException {

        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sql = "SELECT AR.ID AS RELESE_ID, AR.VERSION AS RELEASE_VERSION, AR.UUID, AR.RELEASE_TYPE, "
                + "AR.PACKAGE_NAME AS PACKAGE_NAME, AR.APP_PRICE, AR.STORED_LOCATION, AR.BANNER_LOCATION, "
                + "AR.SC_1_LOCATION AS SCREEN_SHOT_1, AR.SC_2_LOCATION AS SCREEN_SHOT_2, AR.SC_3_LOCATION AS "
                + "SCREEN_SHOT_3, AR.APP_HASH_VALUE AS HASH_VALUE, AR.SHARED_WITH_ALL_TENANTS AS SHARED, "
                + "AR.APP_META_INFO, AR.CREATED_BY, AR.CREATED_AT, AR.PUBLISHED_BY, AR.PUBLISHED_AT, AR.STARS, "
                + "AL.CURRENT_STATE, AL.PREVIOUSE_STATE, AL.UPDATED_BY, AL.UPDATED_AT FROM "
                + "AP_APP_RELEASE AS AR, AP_APP_LIFECYCLE_STATE AS AL WHERE "
                + "AR.AP_APP_ID=(SELECT ID FROM AP_APP WHERE NAME=? AND TYPE=? AND TENANT_ID=?) "
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
                return constructApplicationRelease(resultSet);
            }
            return null;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Database connection exception while trying to get the "
                    + "release details of the application with " + applicationName + " and version " +
                    versionName, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error while getting release details of the application " +
                    applicationName + " and version " + versionName + " , while executing the query " + sql, e);
        } finally {
            Util.cleanupResources(statement, resultSet);
        }
    }

    /**
     * To get release details of a specific application.
     *
     * @param applicationId ID of the application.
     * @param releaseUuid UUID of the application release.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    @Override
    public ApplicationRelease getReleaseByIds(int applicationId, String releaseUuid, int tenantId) throws
                                                                               ApplicationManagementDAOException {

        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sql = "SELECT AR.ID AS RELESE_ID, AR.VERSION AS RELEASE_VERSION, AR.UUID, AR.RELEASE_TYPE, AR.APP_PRICE,"
                + " AR.STORED_LOCATION, AR.BANNER_LOCATION, AR.SC_1_LOCATION AS SCREEN_SHOT_1, "
                + "AR.SC_2_LOCATION AS SCREEN_SHOT_2, AR.SC_3_LOCATION AS SCREEN_SHOT_3, AR.APP_HASH_VALUE AS " +
                "HASH_VALUE, AR.SHARED_WITH_ALL_TENANTS AS SHARED, AR.APP_META_INFO, AR.CREATED_BY, AR.CREATED_AT, AR" +
                ".PUBLISHED_BY, AR.PUBLISHED_AT, AR.STARS, AL.CURRENT_STATE, AL.PREVIOUSE_STATE, AL.UPDATED_BY, " +
                "AL.UPDATED_AT FROM AP_APP_RELEASE AS AR, AP_APP_LIFECYCLE_STATE AS AL WHERE " +
                "AR.AP_APP_ID = ? AND AR.UUID = ? AND AR.TENANT_ID = ? AND AL.AP_APP_RELEASE_ID=AR.ID AND " +
                "AL.TENANT_ID = AR.TENANT_ID ORDER BY AL.UPDATED_AT DESC;";

        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, applicationId);
            statement.setString(2, releaseUuid);
            statement.setInt(3, tenantId);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return constructApplicationRelease(resultSet);
            }
            return null;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database connection exception while trying to get the release details of the " +
                            "application id: " + applicationId + "and UUID of the application release:  " +
                            releaseUuid, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error while getting release details of the application id: " + applicationId +
                            " and theUUID of the application " +
                            "release: " + releaseUuid + " , while executing the query " + sql, e);
        } finally {
            Util.cleanupResources(statement, resultSet);
        }
    }

    /**
     * To insert the application release properties.
     *
     * @param applicationName Name of the application.
     * @param applicationType Type of the application.
     * @param tenantId Tenant Id
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    @Override
    public List<ApplicationRelease> getReleases(String applicationName, String applicationType, int tenantId)
            throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<ApplicationRelease> applicationReleases = new ArrayList<>();
        String sql = "SELECT AR.ID AS RELESE_ID, AR.VERSION AS RELEASE_VERSION, AR.UUID, AR.RELEASE_TYPE, AR.APP_PRICE,"
                + " AR.STORED_LOCATION, AR.BANNER_LOCATION, AR.SC_1_LOCATION AS SCREEN_SHOT_1, AR.SC_2_LOCATION AS "
                + "SCREEN_SHOT_2, AR.SC_3_LOCATION AS SCREEN_SHOT_3, AR.APP_HASH_VALUE AS HASH_VALUE, "
                + "AR.SHARED_WITH_ALL_TENANTS AS SHARED, AR.APP_META_INFO, AR.CREATED_BY, AR.CREATED_AT, "
                + "AR.PUBLISHED_BY, AR.PUBLISHED_AT, AR.STARS FROM AP_APP_RELEASE AS "
                + "AR where AR.AP_APP_ID=(SELECT ID FROM AP_APP WHERE NAME = ? AND TYPE = ? "
                + "AND TENANT_ID = ?) AND AR.TENANT_ID = ? ;";

        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, applicationName);
            statement.setString(2, applicationType);
            statement.setInt(3, tenantId);
            statement.setInt(4, tenantId);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ApplicationRelease applicationRelease = new ApplicationRelease();
                applicationRelease.setId(resultSet.getInt("RELESE_ID"));
                applicationRelease.setVersion(resultSet.getString("RELEASE_VERSION"));
                applicationRelease.setUuid(resultSet.getString("UUID"));
                applicationRelease.setReleaseType(resultSet.getString("RELEASE_TYPE"));
                applicationRelease.setPrice(resultSet.getDouble("APP_PRICE"));
                applicationRelease.setAppStoredLoc(resultSet.getString("STORED_LOCATION"));
                applicationRelease.setBannerLoc(resultSet.getString("BANNER_LOCATION"));
                applicationRelease.setScreenshotLoc1(resultSet.getString("SCREEN_SHOT_1"));
                applicationRelease.setScreenshotLoc2(resultSet.getString("SCREEN_SHOT_2"));
                applicationRelease.setScreenshotLoc3(resultSet.getString("SCREEN_SHOT_3"));
                applicationRelease.setAppHashValue(resultSet.getString("HASH_VALUE"));
                applicationRelease.setIsSharedWithAllTenants(resultSet.getInt("SHARED"));
                applicationRelease.setMetaData(resultSet.getString("APP_META_INFO"));
                applicationRelease.setApplicationCreator(resultSet.getString("CREATED_BY"));
                applicationRelease.setRating(resultSet.getDouble("RATING"));
                applicationReleases.add(applicationRelease);

            }
            return applicationReleases;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Database connection exception while trying to get the "
                    + "release details of the application with Name " + applicationName, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "Error while getting all the release details of the " + applicationName + " application"
                            + ", while executing the query " + sql, e);
        } finally {
            Util.cleanupResources(statement, resultSet);
        }
    }

    /**
     * To Update starts of an application release.
     *
     * @param id Id of the application Release.
     * @param rating given stars for the application release.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    @Override
    public int updateRatingValue(int id, double rating, int ratedUsers) throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        String sql = "UPDATE AP_APP_RELEASE SET RATING = ? AND RATED_USERS = ? WHERE ID = ?;";
        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setDouble(1, rating);
            statement.setInt(2, ratedUsers);
            statement.setInt(2, id);
            return statement.executeUpdate();
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database connection exception while trying to update the application release", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL exception while updating the release ,while executing the query " + sql, e);
        } finally {
            Util.cleanupResources(statement, null);
        }
    }

    /**
     * To retrieve rating of an application release.
     *
     * @param uuid UUID of the application Release.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    @Override
    public Rating getRating(String uuid) throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Rating rating = null;
        String sql = "SELECT RATING, RATED_USERS FROM AP_APP_RELEASE WHERE UUID = ?;";
        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, uuid);
            resultSet = statement.executeQuery();

            if (resultSet.next()){
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
     * @param applicationRelease Application Release the properties of which that need to be inserted.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    @Override
    public ApplicationRelease updateRelease(int applicationId, ApplicationRelease applicationRelease, int tenantId)
            throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        String sql = "UPDATE AP_APP_RELEASE SET VERSION = ? AND UUID = ? AND RELEASE_TYPE = ? AND PACKAGE_NAME = ? "
                + "AND APP_PRICE = ? AND STORED_LOCATION = ? AND BANNER_LOCATION = ? AND SC_1_LOCATION = ? "
                + "AND SC_2_LOCATION = ? AND SC_3_LOCATION = ? AND APP_HASH_VALUE = ? AND SHARED_WITH_ALL_TENANTS = ? "
                + "AND APP_META_INFO = ? AND CREATED_BY = ? AND CREATED_AT = ? WHERE AP_APP_ID = ? AND TENANT_ID = ? "
                + "AND ID = ?;";
        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, applicationRelease.getVersion());
            statement.setString(2, applicationRelease.getUuid());
            statement.setString(3, applicationRelease.getReleaseType());
            statement.setString(4, applicationRelease.getPackageName());
            statement.setDouble(5, applicationRelease.getPrice());
            statement.setString(6, applicationRelease.getAppStoredLoc());
            statement.setString(7, applicationRelease.getBannerLoc());
            statement.setString(8, applicationRelease.getScreenshotLoc1());
            statement.setString(9, applicationRelease.getScreenshotLoc2());
            statement.setString(10, applicationRelease.getScreenshotLoc3());
            statement.setString(11, applicationRelease.getAppHashValue());
            statement.setInt(12, applicationRelease.getIsSharedWithAllTenants());
            statement.setString(13, applicationRelease.getMetaData());
            statement.setString(14, applicationRelease.getApplicationCreator());
            statement.setTimestamp(15, new Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database connection exception while trying to update the application release", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL exception while updating the release ,while executing the query " + sql, e);
        } finally {
            Util.cleanupResources(statement, null);
        }
        return applicationRelease;
    }

    /**
     * To delete an application release.
     *
     * @param id Id of the application Release.
     * @param version version name of the application release.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    @Override
    public void deleteRelease(int id, String version) throws ApplicationManagementDAOException {
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

    /**
     * This method is capable to construct {@link ApplicationRelease} and return the object
     * @param resultSet result set obtained from the query executing.
     * @throws SQLException SQL exception while accessing result set data.
     */
    private ApplicationRelease constructApplicationRelease(ResultSet resultSet) throws SQLException {
        ApplicationRelease applicationRelease = new ApplicationRelease();
        applicationRelease.setId(resultSet.getInt("RELEASE_ID"));
        applicationRelease.setVersion(resultSet.getString("RELEASE_VERSION"));
        applicationRelease.setUuid(resultSet.getString("UUID"));
        applicationRelease.setReleaseType(resultSet.getString("RELEASE_TYPE"));
        applicationRelease.setPackageName(resultSet.getString("PACKAGE_NAME"));
        applicationRelease.setPrice(resultSet.getDouble("APP_PRICE"));
        applicationRelease.setAppStoredLoc(resultSet.getString("STORED_LOCATION"));
        applicationRelease.setBannerLoc(resultSet.getString("BANNER_LOCATION"));
        applicationRelease.setScreenshotLoc1(resultSet.getString("SCREEN_SHOT_1"));
        applicationRelease.setScreenshotLoc2(resultSet.getString("SCREEN_SHOT_2"));
        applicationRelease.setScreenshotLoc3(resultSet.getString("SCREEN_SHOT_3"));
        applicationRelease.setAppHashValue(resultSet.getString("HASH_VALUE"));
        applicationRelease.setIsSharedWithAllTenants(resultSet.getInt("SHARED"));
        applicationRelease.setMetaData(resultSet.getString("APP_META_INFO"));
        applicationRelease.setApplicationCreator(resultSet.getString("CREATED_BY"));
        applicationRelease.setRating(resultSet.getDouble("RATING"));

        return applicationRelease;
    }

}
