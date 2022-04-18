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
package io.entgra.application.mgt.core.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.entgra.application.mgt.common.dto.IdentityServerDTO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import io.entgra.application.mgt.common.ExecutionStatus;
import io.entgra.application.mgt.common.SubscriptionType;
import io.entgra.application.mgt.common.dto.ApplicationDTO;

import io.entgra.application.mgt.common.dto.ApplicationReleaseDTO;
import io.entgra.application.mgt.common.dto.DeviceSubscriptionDTO;
import io.entgra.application.mgt.common.dto.ReviewDTO;
import io.entgra.application.mgt.common.dto.ScheduledSubscriptionDTO;
import io.entgra.application.mgt.core.exception.UnexpectedServerErrorException;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class is responsible for handling the utils of the Application Management DAO.
 */
public class DAOUtil {

    private static final Log log = LogFactory.getLog(DAOUtil.class);

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
                application.setAppRating(rs.getDouble("APP_RATING"));
                application.setDeviceTypeId(rs.getInt("APP_DEVICE_TYPE_ID"));
                application.setPackageName(rs.getString("PACKAGE_NAME"));
                ApplicationReleaseDTO releaseDTO = constructAppReleaseDTO(rs);
                if (releaseDTO != null) {
                    application.getApplicationReleaseDTOs().add(constructAppReleaseDTO(rs));
                }
            } else {
                if (application != null && application.getApplicationReleaseDTOs() != null) {
                    ApplicationReleaseDTO releaseDTO = constructAppReleaseDTO(rs);
                    if (releaseDTO != null) {
                        application.getApplicationReleaseDTOs().add(constructAppReleaseDTO(rs));
                    }
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
     * To create list of device subscription objects from the result set retrieved from the Database.
     *
     * @param rs ResultSet
     * @return List of device subscriptions that is retrieved from the Database.
     * @throws SQLException  SQL Exception
     * @throws JSONException JSONException.
     */
    public static List<DeviceSubscriptionDTO> loadDeviceSubscriptions(ResultSet rs) throws SQLException {
        List<DeviceSubscriptionDTO> deviceSubscriptionDTOS = new ArrayList<>();
        while (rs.next()) {
            deviceSubscriptionDTOS.add(constructDeviceSubscriptionDTO(rs));
        }
        return deviceSubscriptionDTOS;
    }

    public static DeviceSubscriptionDTO constructDeviceSubscriptionDTO(ResultSet rs ) throws SQLException {
        DeviceSubscriptionDTO deviceSubscriptionDTO = new DeviceSubscriptionDTO();
        deviceSubscriptionDTO.setId(rs.getInt("ID"));
        deviceSubscriptionDTO.setSubscribedBy(rs.getString("SUBSCRIBED_BY"));
        deviceSubscriptionDTO.setSubscribedTimestamp(rs.getTimestamp("SUBSCRIBED_AT"));
        deviceSubscriptionDTO.setUnsubscribed(rs.getBoolean("IS_UNSUBSCRIBED"));
        deviceSubscriptionDTO.setUnsubscribedBy(rs.getString("UNSUBSCRIBED_BY"));
        deviceSubscriptionDTO.setUnsubscribedTimestamp(rs.getTimestamp("UNSUBSCRIBED_AT"));
        deviceSubscriptionDTO.setActionTriggeredFrom(rs.getString("ACTION_TRIGGERED_FROM"));
        deviceSubscriptionDTO.setDeviceId(rs.getInt("DEVICE_ID"));
        deviceSubscriptionDTO.setStatus(rs.getString("STATUS"));
        return  deviceSubscriptionDTO;
    }

    /**
     * Populates {@link ApplicationReleaseDTO} object with the result obtained from the database.
     *
     * @param rs {@link ResultSet} from obtained from the database
     * @return {@link ApplicationReleaseDTO} object populated with the data
     * @throws SQLException If unable to populate {@link ApplicationReleaseDTO} object with the data
     */
    public static ApplicationReleaseDTO constructAppReleaseDTO(ResultSet rs) throws SQLException {
        ApplicationReleaseDTO appRelease = new ApplicationReleaseDTO();
        if (rs.getString("RELEASE_UUID") != null) {
            appRelease.setId(rs.getInt("RELEASE_ID"));
            appRelease.setDescription(rs.getString("RELEASE_DESCRIPTION"));
            appRelease.setUuid(rs.getString("RELEASE_UUID"));
            appRelease.setReleaseType(rs.getString("RELEASE_TYPE"));
            appRelease.setVersion(rs.getString("RELEASE_VERSION"));
            appRelease.setInstallerName(rs.getString("AP_RELEASE_STORED_LOC"));
            appRelease.setIconName(rs.getString("AP_RELEASE_ICON_LOC"));
            appRelease.setBannerName(rs.getString("AP_RELEASE_BANNER_LOC"));
            appRelease.setScreenshotName1(rs.getString("AP_RELEASE_SC1"));
            appRelease.setScreenshotName2(rs.getString("AP_RELEASE_SC2"));
            appRelease.setScreenshotName3(rs.getString("AP_RELEASE_SC3"));
            appRelease.setAppHashValue(rs.getString("RELEASE_HASH_VALUE"));
            appRelease.setPrice(rs.getDouble("RELEASE_PRICE"));
            appRelease.setMetaData(rs.getString("RELEASE_META_INFO"));
            appRelease.setPackageName(rs.getString("PACKAGE_NAME"));
            appRelease.setSupportedOsVersions(rs.getString("RELEASE_SUP_OS_VERSIONS"));
            appRelease.setRating(rs.getDouble("RELEASE_RATING"));
            appRelease.setCurrentState(rs.getString("RELEASE_CURRENT_STATE"));
            appRelease.setRatedUsers(rs.getInt("RATED_USER_COUNT"));
            return appRelease;
        }
        return null;
    }

    /**
     * To create application object from the result set retrieved from the Database.
     *
     * @param rs ResultSet
     * @return IdentityServerDTO that is retrieved from the Database.
     * @throws SQLException  SQL Exception
     * @throws JSONException JSONException.
     */
    public static IdentityServerDTO loadIdentityServer(ResultSet rs)
            throws SQLException, JSONException, UnexpectedServerErrorException {
        List<IdentityServerDTO> identityServerDTOS = loadIdentityServers(rs);
        if (identityServerDTOS.isEmpty()) {
            return null;
        }
        if (identityServerDTOS.size() > 1) {
            String msg = "Internal server error. Found more than one identity server for requested ID";
            log.error(msg);
            throw new UnexpectedServerErrorException(msg);
        }
        return identityServerDTOS.get(0);
    }

    /**
     * To create application object from the result set retrieved from the Database.
     *
     * @param rs ResultSet
     * @return List of Identity Servers that is retrieved from the Database.
     * @throws SQLException  SQL Exception
     * @throws JSONException JSONException.
     */
    public static List<IdentityServerDTO> loadIdentityServers(ResultSet rs) throws SQLException, JSONException {
        List<IdentityServerDTO> identityServerDTOS = new ArrayList<>();
        while (rs.next()) {
            IdentityServerDTO identityServerDTO = new IdentityServerDTO();
            identityServerDTO.setId(rs.getInt("ID"));
            identityServerDTO.setProviderName(rs.getString("PROVIDER_NAME"));
            identityServerDTO.setName(rs.getString("NAME"));
            identityServerDTO.setDescription(rs.getString("DESCRIPTION"));
            identityServerDTO.setUrl(rs.getString("URL"));
            String apiParamsJson = rs.getString("API_PARAMS");
            Map<String, String> apiParams = new Gson().fromJson(apiParamsJson, new TypeToken<HashMap<String, String>>() {}.getType());
            identityServerDTO.setApiParams(apiParams);
            identityServerDTO.setUsername(rs.getString("USERNAME"));
            identityServerDTO.setPassword(rs.getString("PASSWORD"));
            identityServerDTOS.add(identityServerDTO);
        }
        return identityServerDTOS;
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

    public static ReviewDTO loadReview(ResultSet rs) throws SQLException, UnexpectedServerErrorException {
        List<ReviewDTO> reviewDTOs = loadReviews(rs);
        if (reviewDTOs.isEmpty()) {
            return null;
        }
        if (reviewDTOs.size() > 1) {
            String msg = "Internal server error. Found more than one review for requested review ID";
            log.error(msg);
            throw new UnexpectedServerErrorException(msg);
        }
        return reviewDTOs.get(0);
    }

    public static List<ReviewDTO> loadReviews (ResultSet rs) throws SQLException {
        List<ReviewDTO> reviewDTOs = new ArrayList<>();
        while (rs.next()) {
            ReviewDTO reviewDTO = new ReviewDTO();
            reviewDTO.setId(rs.getInt("ID"));
            reviewDTO.setContent(rs.getString("COMMENT"));
            reviewDTO.setCreatedAt(new Timestamp(rs.getLong("CREATED_AT") * 1000L));
            reviewDTO.setModifiedAt(new Timestamp(rs.getLong("MODIFIED_AT") * 1000L));
            reviewDTO.setRootParentId(rs.getInt("ROOT_PARENT_ID"));
            reviewDTO.setImmediateParentId(rs.getInt("IMMEDIATE_PARENT_ID"));
            reviewDTO.setUsername(rs.getString("USERNAME"));
            reviewDTO.setRating(rs.getInt("RATING"));
            reviewDTO.setReleaseUuid(rs.getString("UUID"));
            reviewDTO.setReleaseVersion(rs.getString("VERSION"));
            reviewDTOs.add(reviewDTO);
        }
        return reviewDTOs;
    }

    public  static ScheduledSubscriptionDTO loadScheduledSubscription(ResultSet rs)
            throws SQLException, UnexpectedServerErrorException {
        List<ScheduledSubscriptionDTO> subscriptionDTOs = loadScheduledSubscriptions(rs);

        if (subscriptionDTOs.isEmpty()) {
            return null;
        }
        if (subscriptionDTOs.size() > 1) {
            String msg = "Internal server error. Found more than one subscription for requested pending subscription";
            log.error(msg);
            throw new UnexpectedServerErrorException(msg);
        }
        return subscriptionDTOs.get(0);
    }

    public static List<ScheduledSubscriptionDTO> loadScheduledSubscriptions(ResultSet rs) throws SQLException {
        List<ScheduledSubscriptionDTO> subscriptionDTOS = new ArrayList<>();
        while (rs.next()) {
            ScheduledSubscriptionDTO subscription = new ScheduledSubscriptionDTO();
            subscription.setId(rs.getInt("ID"));
            subscription.setTaskName(rs.getString("TASK_NAME"));
            subscription.setApplicationUUID(rs.getString("APPLICATION_UUID"));

            if (subscription.getTaskName().startsWith(SubscriptionType.DEVICE.toString())) {
                List<DeviceIdentifier> deviceIdentifiers = new Gson().fromJson(rs.getString("SUBSCRIBER_LIST"),
                        new TypeToken<List<DeviceIdentifier>>() {
                        }.getType());
                subscription.setSubscriberList(deviceIdentifiers);
            } else {
                List<String> subscriberList = Pattern.compile(",").splitAsStream(rs.getString("SUBSCRIBER_LIST"))
                        .collect(Collectors.toList());
                subscription.setSubscriberList(subscriberList);
            }

            subscription.setStatus(ExecutionStatus.valueOf(rs.getString("STATUS")));
            subscription.setScheduledAt(rs.getLong("SCHEDULED_AT"));
            subscription.setScheduledBy(rs.getString("SCHEDULED_BY"));
            subscription.setDeleted(rs.getBoolean("DELETED"));
            subscriptionDTOS.add(subscription);
        }
        return subscriptionDTOS;
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

    public static long getCurrentUTCTime() {
        return Instant.now().getEpochSecond();
    }
}
