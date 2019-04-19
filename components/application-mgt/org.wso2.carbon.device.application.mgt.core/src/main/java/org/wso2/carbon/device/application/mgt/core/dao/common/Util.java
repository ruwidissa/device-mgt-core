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
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.PaginationRequest;

import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.application.mgt.core.config.Configuration;
import org.wso2.carbon.device.application.mgt.core.config.ConfigurationManager;
import org.wso2.carbon.device.application.mgt.core.exception.UnexpectedServerErrorException;
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
    public static List<ApplicationDTO> loadApplications(ResultSet rs) throws SQLException, JSONException {

        List<ApplicationDTO> applications = new ArrayList<>();
        ApplicationDTO application = null;
        int applicationId = -1;
        boolean hasNext = rs.next();

        while (hasNext) {
            if (applicationId != rs.getInt("APP_ID")) {
                if (application != null) {
                    applications.add(application);
                }
                application = new ApplicationDTO();
                application.setApplicationReleaseDTOs(new ArrayList<>());
                applicationId = rs.getInt("APP_ID");
                application.setId(applicationId);
                application.setName(rs.getString("APP_NAME"));
                application.setDescription(rs.getString("APP_DESCRIPTION"));
                application.setType(rs.getString("APP_TYPE"));
                application.setSubType(rs.getString("APP_SUB_TYPE"));
                application.setPaymentCurrency(rs.getString("APP_CURRENCY"));
                application.setStatus(rs.getString("APP_STATUS"));
                application.setAppRating(rs.getInt("APP_RATING"));
                application.setDeviceTypeId(rs.getInt("APP_DEVICE_TYPE_ID"));
                application.getApplicationReleaseDTOs().add(loadAppRelease(rs));
            } else {
                if (application != null && application.getApplicationReleaseDTOs() != null) {
                    application.getApplicationReleaseDTOs().add(loadAppRelease(rs));
                }
            }
            hasNext = rs.next();
            if (!hasNext) {
                applications.add(application);
            }
        }
        return applications;
    }

    /**
     * Populates {@link ApplicationReleaseDTO} object with the result obtained from the database.
     *
     * @param rs {@link ResultSet} from obtained from the database
     * @return {@link ApplicationReleaseDTO} object populated with the data
     * @throws SQLException If unable to populate {@link ApplicationReleaseDTO} object with the data
     */
    public static ApplicationReleaseDTO loadAppRelease(ResultSet rs) throws SQLException {
        ApplicationReleaseDTO appRelease = new ApplicationReleaseDTO();
        appRelease.setDescription(rs.getString("RELEASE_DESCRIPTION"));
        appRelease.setUuid(rs.getString("RELEASE_UUID"));
        appRelease.setReleaseType(rs.getString("RELEASE_TYPE"));
        appRelease.setVersion(rs.getString("RELEASE_VERSION"));
        appRelease.setInstallerName(rs.getString("AP_RELEASE_STORED_LOC"));
        appRelease.setBannerName(rs.getString("AP_RELEASE_BANNER_LOC"));
        appRelease.setScreenshotName1("AP_RELEASE_SC1");
        appRelease.setScreenshotName2("AP_RELEASE_SC2");
        appRelease.setScreenshotName3("AP_RELEASE_SC3");
        appRelease.setPrice(rs.getDouble("RELEASE_PRICE"));
        appRelease.setMetaData(rs.getString("RELEASE_META_INFO"));
        appRelease.setSupportedOsVersions(rs.getString("RELEASE_SUP_OS_VERSIONS"));
        appRelease.setRating(rs.getDouble("RELEASE_RATING"));
        appRelease.setCurrentState(rs.getString("RELEASE_CURRENT_STATE"));
        appRelease.setRatedUsers(rs.getInt("RATED_USER_COUNT"));
        return appRelease;
    }


    /**
     * To create application object from the result set retrieved from the Database.
     *
     * @param rs ResultSet
     * @return ApplicationDTO that is retrieved from the Database.
     * @throws SQLException  SQL Exception
     * @throws JSONException JSONException.
     */
    public static ApplicationDTO loadApplication(ResultSet rs)
            throws SQLException, JSONException, UnexpectedServerErrorException {
        List<ApplicationDTO> applicationDTOs = loadApplications(rs);
        if (applicationDTOs.isEmpty()) {
            return null;
        }
        if (applicationDTOs.size() > 1) {
            String msg = "Internal server error. Found more than one application for requested application ID";
            log.error(msg);
            throw new UnexpectedServerErrorException(msg);
        }
        return applicationDTOs.get(0);
    }

    /**
     * Populates {@link ApplicationReleaseDTO} object with the result obtained from the database.
     *
     * @param resultSet {@link ResultSet} from obtained from the database
     * @return {@link ApplicationReleaseDTO} object populated with the data
     * @throws SQLException If unable to populate {@link ApplicationReleaseDTO} object with the data
     */
    public static ApplicationReleaseDTO loadApplicationRelease(ResultSet resultSet) throws SQLException {
        ApplicationReleaseDTO applicationRelease = new ApplicationReleaseDTO();
        applicationRelease.setId(resultSet.getInt("RELEASE_ID"));
        applicationRelease.setVersion(resultSet.getString("RELEASE_VERSION"));
        applicationRelease.setUuid(resultSet.getString("UUID"));
        applicationRelease.setReleaseType(resultSet.getString("RELEASE_TYPE"));
        applicationRelease.setPackageName(resultSet.getString("PACKAGE_NAME"));
        applicationRelease.setPrice(resultSet.getDouble("APP_PRICE"));
        applicationRelease.setInstallerName(resultSet.getString("STORED_LOCATION"));
        applicationRelease.setBannerName(resultSet.getString("BANNER_LOCATION"));
        applicationRelease.setIconName(resultSet.getString("ICON_LOCATION"));
        applicationRelease.setScreenshotName1(resultSet.getString("SCREEN_SHOT_1"));
        applicationRelease.setScreenshotName2(resultSet.getString("SCREEN_SHOT_2"));
        applicationRelease.setScreenshotName3(resultSet.getString("SCREEN_SHOT_3"));
        applicationRelease.setAppHashValue(resultSet.getString("HASH_VALUE"));
        applicationRelease.setIsSharedWithAllTenants(resultSet.getBoolean("SHARED"));
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
                        "ApplicationDTO Management configuration has not initialized. Please check the application-mgt.xml file.");
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
                        String msg = "ApplicationDTO Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
        return applicationManager;
    }

    /**
     * To get the ApplicationDTO Storage Manager from the osgi context.
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
                        String msg = "ApplicationDTO Storage Manager service has not initialized.";
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
