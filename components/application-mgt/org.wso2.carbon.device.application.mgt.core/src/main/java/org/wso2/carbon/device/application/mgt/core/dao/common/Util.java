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
package org.wso2.carbon.device.application.mgt.core.dao.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.*;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.PaginationRequest;

import org.wso2.carbon.device.application.mgt.common.exception.ReviewManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.application.mgt.core.config.Configuration;
import org.wso2.carbon.device.application.mgt.core.config.ConfigurationManager;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for handling the utils of the Application Management DAO.
 */
public class Util {

    private static final Log log = LogFactory.getLog(Util.class);

    /**
     * To create application object from the result set retrieved from the Database.
     *
     * @param rs ResultSet
     * @return List of Applications that is retrieved from the Database.
     * @throws SQLException  SQL Exception
     * @throws JSONException JSONException.
     */
    public static List<Application> loadApplications(ResultSet rs) throws SQLException, JSONException {

        List<Application> applications = new ArrayList<>();
        Application application = null;
        int applicationId = -1;

        while (rs.next()) {
            if (applicationId != rs.getInt("APP_ID")) {
                if (application != null) {
                    applications.add(application);
                }
                applicationId = rs.getInt("APP_ID");
                application = new Application();
                application.setTags(new ArrayList<>());
                application.setUnrestrictedRoles(new ArrayList<>());
                application.setId(applicationId);
                application.setName(rs.getString("APP_NAME"));
                application.setType(rs.getString("APP_TYPE"));
                application.setAppCategory(rs.getString("APP_CATEGORY"));
                application.setSubType(rs.getString("SUB_TYPE"));
                application.setPaymentCurrency(rs.getString("CURRENCY"));
                application.setIsRestricted(rs.getBoolean("RESTRICTED"));
                String tag = rs.getString("APP_TAG");
                String unrestrictedRole = rs.getString("ROLE");
                if (tag != null) {
                    application.getTags().add(tag);
                }
                if (unrestrictedRole != null) {
                    application.getUnrestrictedRoles().add(unrestrictedRole);
                }
            } else {
                String tag = rs.getString("APP_TAG");
                String unrestrictedRole = rs.getString("ROLE");
                if (application != null) {
                    if (tag != null && !application.getTags().contains(tag)) {
                        application.getTags().add(tag);
                    }
                    if (unrestrictedRole != null && !application.getUnrestrictedRoles().contains(unrestrictedRole)) {
                        application.getUnrestrictedRoles().add(unrestrictedRole);
                    }
                }
            }
            if (rs.last()) {
                applications.add(application);
            }
        }
        return applications;
    }


    /**
     * To create application object from the result set retrieved from the Database.
     *
     * @param rs ResultSet
     * @return Application that is retrieved from the Database.
     * @throws SQLException  SQL Exception
     * @throws JSONException JSONException.
     */
    public static Application loadApplication(ResultSet rs) throws SQLException, JSONException {

        Application application = null;
        int applicatioId;
        int iteration = 0;
        if (rs != null) {
            while (rs.next()) {
                if (iteration == 0) {
                    application = new Application();
                    application.setTags(new ArrayList<>());
                    application.setUnrestrictedRoles(new ArrayList<>());
                    applicatioId = rs.getInt("APP_ID");
                    application.setId(applicatioId);
                    application.setName(rs.getString("APP_NAME"));
                    application.setType(rs.getString("APP_TYPE"));
                    application.setAppCategory(rs.getString("APP_CATEGORY"));
                    application.setSubType(rs.getString("SUB_TYPE"));
                    application.setPaymentCurrency(rs.getString("CURRENCY"));
                    application.setIsRestricted(rs.getBoolean("RESTRICTED"));
                    application.setDeviceTypeId(rs.getInt("DEVICE_TYPE_ID"));
                }

                String tag = rs.getString("APP_TAG");
                String unrestrictedRole = rs.getString("ROLE");
                if (tag != null && !application.getTags().contains(tag)) {
                    application.getTags().add(tag);
                }
                if (unrestrictedRole != null && !application.getUnrestrictedRoles().contains(unrestrictedRole)) {
                    application.getUnrestrictedRoles().add(unrestrictedRole);
                }
                iteration++;
            }
        }
        return application;

    }

    /**
     * Populates {@link ApplicationRelease} object with the result obtained from the database.
     *
     * @param resultSet {@link ResultSet} from obtained from the database
     * @return {@link ApplicationRelease} object populated with the data
     * @throws SQLException If unable to populate {@link ApplicationRelease} object with the data
     */
    public static ApplicationRelease loadApplicationRelease(ResultSet resultSet) throws SQLException {
        ApplicationRelease applicationRelease = new ApplicationRelease();
        applicationRelease.setId(resultSet.getInt("RELEASE_ID"));
        applicationRelease.setVersion(resultSet.getString("RELEASE_VERSION"));
        applicationRelease.setUuid(resultSet.getString("UUID"));
        applicationRelease.setReleaseType(resultSet.getString("RELEASE_TYPE"));
        applicationRelease.setPackageName(resultSet.getString("PACKAGE_NAME"));
        applicationRelease.setPrice(resultSet.getDouble("APP_PRICE"));
        applicationRelease.setAppStoredLoc(resultSet.getString("STORED_LOCATION"));
        applicationRelease.setBannerLoc(resultSet.getString("BANNER_LOCATION"));
        applicationRelease.setIconLoc(resultSet.getString("ICON_LOCATION"));
        applicationRelease.setScreenshotLoc1(resultSet.getString("SCREEN_SHOT_1"));
        applicationRelease.setScreenshotLoc2(resultSet.getString("SCREEN_SHOT_2"));
        applicationRelease.setScreenshotLoc3(resultSet.getString("SCREEN_SHOT_3"));
        applicationRelease.setAppHashValue(resultSet.getString("HASH_VALUE"));
        applicationRelease.setIsSharedWithAllTenants(resultSet.getInt("SHARED"));
        applicationRelease.setMetaData(resultSet.getString("APP_META_INFO"));
        applicationRelease.setRating(resultSet.getDouble("RATING"));
        return applicationRelease;
    }

    /**
     * Cleans up the statement and resultset after executing the query
     *
     * @param stmt Statement executed.
     * @param rs   Resultset retrived.
     */
    public static void cleanupResources(PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing result set", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing prepared statement", e);
            }
        }
    }

    public static PaginationRequest validateCommentListPageSize(PaginationRequest paginationRequest) throws
            ReviewManagementException {
        if (paginationRequest.getLimit() == 0) {
            Configuration commentManagementConfig = ConfigurationManager.getInstance().getConfiguration();
            if (commentManagementConfig != null) {
                paginationRequest.setLimit(
                        commentManagementConfig.getPaginationConfiguration().getCommentListPageSize());
            } else {
                throw new ReviewManagementException(
                        "Application Management configuration has not initialized. Please check the application-mgt.xml file.");
            }
        }
        return paginationRequest;
    }

    private static ApplicationManager applicationManager;
    private static ApplicationStorageManager applicationStorageManager;
    private static SubscriptionManager subscriptionManager;

    public static ApplicationManager getApplicationManager() {
        if (applicationManager == null) {
            synchronized (Util.class) {
                if (applicationManager == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    applicationManager =
                            (ApplicationManager) ctx.getOSGiService(ApplicationManager.class, null);
                    if (applicationManager == null) {
                        String msg = "Application Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
        return applicationManager;
    }

    /**
     * To get the Application Storage Manager from the osgi context.
     * @return ApplicationStoreManager instance in the current osgi context.
     */
    public static ApplicationStorageManager getApplicationStorageManager() {
        if (applicationStorageManager == null) {
            synchronized (Util.class) {
                if (applicationStorageManager == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    applicationStorageManager = (ApplicationStorageManager) ctx
                            .getOSGiService(ApplicationStorageManager.class, null);
                    if (applicationStorageManager == null) {
                        String msg = "Application Storage Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
        return applicationStorageManager;
    }


    /**
     * To get the Subscription Manager from the osgi context.
     * @return SubscriptionManager instance in the current osgi context.
     */
    public static SubscriptionManager getSubscriptionManager() {
        if (subscriptionManager == null) {
            synchronized (Util.class) {
                if (subscriptionManager == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    subscriptionManager =
                            (SubscriptionManager) ctx.getOSGiService(SubscriptionManager.class, null);
                    if (subscriptionManager == null) {
                        String msg = "Subscription Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }

        return subscriptionManager;
    }

    public static DeviceManagementProviderService getDeviceManagementService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceManagementProviderService deviceManagementProviderService =
                (DeviceManagementProviderService) ctx.getOSGiService(DeviceManagementProviderService.class, null);
        if (deviceManagementProviderService == null) {
            String msg = "DeviceImpl Management provider service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return deviceManagementProviderService;
    }
}
