/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.application.mgt.core.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.entgra.device.mgt.core.application.mgt.common.dto.*;
import io.entgra.device.mgt.core.application.mgt.core.exception.UnexpectedServerErrorException;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Activity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import io.entgra.device.mgt.core.application.mgt.common.ExecutionStatus;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionType;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
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

    public static ApplicationDTO loadDeviceApp(ResultSet rs) throws SQLException {
        ApplicationDTO application = new ApplicationDTO();
        application.setId( rs.getInt("APP_ID"));
        application.setName(rs.getString("APP_NAME"));
        application.setDescription(rs.getString("APP_DESCRIPTION"));
        application.setType(rs.getString("APP_TYPE"));
        application.setSubType(rs.getString("APP_SUB_TYPE"));
        application.setPaymentCurrency(rs.getString("APP_CURRENCY"));
        application.setStatus(rs.getString("APP_STATUS"));
        application.setAppRating(rs.getDouble("APP_RATING"));
        application.setDeviceTypeId(rs.getInt("APP_DEVICE_TYPE_ID"));
        ApplicationReleaseDTO releaseDTO = constructAppReleaseDTO(rs);
        List<ApplicationReleaseDTO> releaseDtoList = new ArrayList<>();
        if (releaseDTO != null) {
            releaseDtoList.add(constructAppReleaseDTO(rs));
            application.setApplicationReleaseDTOs(releaseDtoList);
        }
        return application;
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

    public static Activity loadOperationActivity(ResultSet rs) throws SQLException, UnexpectedServerErrorException {
        List<Activity> activity  = loadOperationActivities(rs);
        if (activity.isEmpty()) {
            return null;
        }
        if (activity.size() > 1) {
            String msg = "Internal server error. Found more than one app for operation";
            log.error(msg);
            throw new UnexpectedServerErrorException(msg);
        }
        return activity.get(0);
    }

    public static  List<Activity> loadOperationActivities (ResultSet rs) throws SQLException {
        List<Activity> activities = new ArrayList<>();
        while (rs.next()) {
            Activity activity = new Activity();
            activity.setAppName(rs.getString("NAME"));
            activity.setAppType(rs.getString("TYPE"));
            activity.setUsername(rs.getString("SUBSCRIBED_BY"));
            activity.setPackageName(rs.getString("PACKAGE_NAME"));
            activity.setVersion(rs.getString("VERSION"));
            activity.setTriggeredBy(rs.getString("ACTION_TRIGGERED_FROM"));
            activities.add(activity);
        }
        return activities;
    }

    public static VppUserDTO loadVppUser(ResultSet rs) throws SQLException, UnexpectedServerErrorException {
        List<VppUserDTO> vppUserDTOS = loadVppUsers(rs);
        if (vppUserDTOS.isEmpty()) {
            return null;
        }
        if (vppUserDTOS.size() > 1) {
            String msg = "Internal server error. Found more than one vpp user for requested emmUsername";
            log.error(msg);
            throw new UnexpectedServerErrorException(msg);
        }
        return vppUserDTOS.get(0);
    }

    public static List<VppUserDTO> loadVppUsers (ResultSet rs) throws SQLException {
        List<VppUserDTO> vppUserDTOS = new ArrayList<>();
        while (rs.next()) {
            VppUserDTO vppUserDTO = new VppUserDTO();
            vppUserDTO.setId(rs.getInt("ID"));
            vppUserDTO.setClientUserId(rs.getString("CLIENT_USER_ID"));
            vppUserDTO.setTenantId(rs.getInt("TENANT_ID"));
            vppUserDTO.setEmail(rs.getString("EMAIL"));
            vppUserDTO.setInviteCode(rs.getString("INVITE_CODE"));
            if (columnExist(rs,"STATUS")) {
                vppUserDTO.setStatus(rs.getString("STATUS"));
            }
            if (columnExist(rs,"MANAGED_ID")) {
                vppUserDTO.setManagedId(rs.getString("MANAGED_ID"));
            }
            if (columnExist(rs,"TEMP_PASSWORD")) {
                vppUserDTO.setTmpPassword(rs.getString("TEMP_PASSWORD"));
            }
            if (columnExist(rs,"DM_USERNAME")) {
                vppUserDTO.setDmUsername(rs.getString("DM_USERNAME"));
            }
            if (rs.getLong("CREATED_TIME") != 0) {
                vppUserDTO.setCreatedTime(new Timestamp(rs.getLong("CREATED_TIME") * 1000L).toString());
            }
            if (rs.getLong("LAST_UPDATED_TIME") != 0) {
                vppUserDTO.setLastUpdatedTime(new Timestamp(rs.getLong("LAST_UPDATED_TIME") * 1000L).toString());
            }
            vppUserDTOS.add(vppUserDTO);
        }
        return vppUserDTOS;
    }

    private static boolean columnExist(ResultSet rs, String column){
        try{
            rs.findColumn(column);
            return true;
        } catch (SQLException sqlex){
        }

        return false;
    }

    public static VppAssetDTO loadAsset(ResultSet rs) throws SQLException, UnexpectedServerErrorException {
        List<VppAssetDTO> vppAssetDTOS = loadAssets(rs);
        if (vppAssetDTOS.isEmpty()) {
            return null;
        }
        if (vppAssetDTOS.size() > 1) {
            String msg = "Internal server error. Found more than one asset for given app id.";
            log.error(msg);
            throw new UnexpectedServerErrorException(msg);
        }
        return vppAssetDTOS.get(0);
    }

    public static List<VppAssetDTO> loadAssets (ResultSet rs) throws SQLException {
        List<VppAssetDTO> vppAssetDTOS = new ArrayList<>();
        while (rs.next()) {
            VppAssetDTO vppAssetDTO = new VppAssetDTO();
            vppAssetDTO.setId(rs.getInt("ID"));
            vppAssetDTO.setAppId(rs.getInt("APP_ID"));
            vppAssetDTO.setTenantId(rs.getInt("TENANT_ID"));
            if (rs.getLong("CREATED_TIME") != 0) {
                Date date = new Date(rs.getLong("CREATED_TIME"));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateString = dateFormat.format(date);
                vppAssetDTO.setCreatedTime(dateString);
            }
            if (rs.getLong("LAST_UPDATED_TIME") != 0) {
                Date date = new Date(rs.getLong("LAST_UPDATED_TIME"));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateString = dateFormat.format(date);
                vppAssetDTO.setLastUpdatedTime(dateString);
            }
            vppAssetDTO.setAdamId(rs.getString("ADAM_ID"));
            vppAssetDTO.setAssignedCount(rs.getString("ASSIGNED_COUNT"));
            vppAssetDTO.setDeviceAssignable(rs.getString("DEVICE_ASSIGNABLE"));
            vppAssetDTO.setPricingParam(rs.getString("PRICING_PARAMS"));
            vppAssetDTO.setProductType(rs.getString("PRODUCT_TYPE"));
            vppAssetDTO.setRetiredCount(rs.getString("RETIRED_COUNT"));
            vppAssetDTO.setRevocable(rs.getString("REVOCABLE"));
//            String jsonString = rs.getString("SUPPORTED_PLATFORMS");
//            ObjectMapper objectMapper = new ObjectMapper();
//            try {
//                List<String> platformList = objectMapper.readValue(jsonString, new TypeReference<List<String>>() {});
//                vppAssetDTO.setSupportedPlatforms(platformList);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            vppAssetDTOS.add(vppAssetDTO);
        }
        return vppAssetDTOS;
    }

    public static VppAssociationDTO loadAssignment(ResultSet rs) throws SQLException, UnexpectedServerErrorException {
        List<VppAssociationDTO> vppAssociationDTOS = loadAssignments(rs);
        if (vppAssociationDTOS.isEmpty()) {
            return null;
        }
        if (vppAssociationDTOS.size() > 1) {
            String msg = "Internal server error. Found more than one asset for given app id.";
            log.error(msg);
            throw new UnexpectedServerErrorException(msg);
        }
        return vppAssociationDTOS.get(0);
    }

    public static List<VppAssociationDTO> loadAssignments (ResultSet rs) throws SQLException {
        List<VppAssociationDTO> vppAssociationDTOS = new ArrayList<>();
        while (rs.next()) {
            VppAssociationDTO vppAssociationDTO = new VppAssociationDTO();
            vppAssociationDTO.setId(rs.getInt("ID"));
            vppAssociationDTO.setAssociationType(rs.getString("ASSOCIATION_TYPE"));
            if (rs.getLong("CREATED_TIME") != 0) {
                Date date = new Date(rs.getLong("CREATED_TIME"));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateString = dateFormat.format(date);
                vppAssociationDTO.setCreatedTime(dateString);
            }
            if (rs.getLong("LAST_UPDATED_TIME") != 0) {
                Date date = new Date(rs.getLong("LAST_UPDATED_TIME"));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateString = dateFormat.format(date);
                vppAssociationDTO.setLastUpdatedTime(dateString);
            }
            vppAssociationDTO.setPricingParam(rs.getString("PRICING_PARAMS"));
            vppAssociationDTOS.add(vppAssociationDTO);
        }
        return vppAssociationDTOS;
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
