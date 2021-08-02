/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.application.mgt.core.impl;

import com.google.gson.Gson;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.ApplicationInstallResponse;
import org.wso2.carbon.device.application.mgt.common.ApplicationSubscriptionInfo;
import org.wso2.carbon.device.application.mgt.common.ApplicationType;
import org.wso2.carbon.device.application.mgt.common.DeviceSubscriptionData;
import org.wso2.carbon.device.application.mgt.common.DeviceTypes;
import org.wso2.carbon.device.application.mgt.common.ExecutionStatus;
import org.wso2.carbon.device.application.mgt.common.SubAction;
import org.wso2.carbon.device.application.mgt.common.SubscriptionType;
import org.wso2.carbon.device.application.mgt.common.SubscribingDeviceIdHolder;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationPolicyDTO;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.dto.DeviceSubscriptionDTO;
import org.wso2.carbon.device.application.mgt.common.dto.ScheduledSubscriptionDTO;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.SubscriptionManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.TransactionManagementException;
import org.wso2.carbon.device.application.mgt.common.response.Application;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.SubscriptionDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.BadRequestException;
import org.wso2.carbon.device.application.mgt.core.exception.ForbiddenException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.lifecycle.LifecycleStateManager;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.application.mgt.core.util.Constants;
import org.wso2.carbon.device.application.mgt.core.util.HelperUtil;
import org.wso2.carbon.device.application.mgt.core.util.OAuthUtils;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.app.mgt.App;
import org.wso2.carbon.device.mgt.common.app.mgt.MobileAppTypes;
import org.wso2.carbon.device.mgt.common.app.mgt.android.CustomApplication;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.exceptions.UnknownApplicationTypeException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.ActivityStatus;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.util.MDMAndroidOperationUtil;
import org.wso2.carbon.device.mgt.core.util.MDMIOSOperationUtil;
import org.wso2.carbon.device.mgt.core.util.MDMWindowsOperationUtil;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * This is the default implementation for the Subscription Manager.
 */
public class SubscriptionManagerImpl implements SubscriptionManager {

    private static final Log log = LogFactory.getLog(SubscriptionManagerImpl.class);
    private SubscriptionDAO subscriptionDAO;
    private ApplicationDAO applicationDAO;
    private LifecycleStateManager lifecycleStateManager;

    public SubscriptionManagerImpl() {
        this.lifecycleStateManager = DataHolder.getInstance().getLifecycleStateManager();
        this.subscriptionDAO = ApplicationManagementDAOFactory.getSubscriptionDAO();
        this.applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
    }

    @Override
    public <T> ApplicationInstallResponse performBulkAppOperation(String applicationUUID, List<T> params,
                                                                  String subType, String action, Properties properties) throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Install application release which has UUID " + applicationUUID + " to " + params.size()
                    + " users.");
        }

        validateRequest(params, subType, action);
        //todo validate users, groups and roles
        ApplicationDTO applicationDTO = getApplicationDTO(applicationUUID);
        ApplicationSubscriptionInfo applicationSubscriptionInfo = getAppSubscriptionInfo(applicationDTO, subType,
                params);
        ApplicationInstallResponse applicationInstallResponse = performActionOnDevices(
                applicationSubscriptionInfo.getAppSupportingDeviceTypeName(), applicationSubscriptionInfo.getDevices(),
                applicationDTO, subType, applicationSubscriptionInfo.getSubscribers(), action, properties);

        applicationInstallResponse.setErrorDeviceIdentifiers(applicationSubscriptionInfo.getErrorDeviceIdentifiers());
        return applicationInstallResponse;
    }

    @Override
    public void createScheduledSubscription(ScheduledSubscriptionDTO subscriptionDTO)
            throws SubscriptionManagementException {
        try {
            ConnectionManagerUtil.beginDBTransaction();
            ScheduledSubscriptionDTO existingEntry = subscriptionDAO
                    .getPendingScheduledSubscriptionByTaskName(subscriptionDTO.getTaskName());
            boolean transactionStatus;
            if (existingEntry == null) {
                transactionStatus = subscriptionDAO.createScheduledSubscription(subscriptionDTO);
            } else {
                transactionStatus = subscriptionDAO
                        .updateScheduledSubscription(existingEntry.getId(), subscriptionDTO.getScheduledAt(),
                                subscriptionDTO.getScheduledBy());
            }
            if (!transactionStatus) {
                ConnectionManagerUtil.rollbackDBTransaction();
            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while creating the scheduled subscription entry.";
            log.error(msg, e);
            throw new SubscriptionManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while executing database transaction";
            log.error(msg, e);
            throw new SubscriptionManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while observing the database connection to update subscription status.";
            log.error(msg, e);
            throw new SubscriptionManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public String checkAppSubscription(int id, String packageName) throws SubscriptionManagementException {
        try {
            ConnectionManagerUtil.openDBConnection();
            return subscriptionDAO.getUUID(id, packageName);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred while checking the application is " + "subscribed one or not";
            log.error(msg, e);
            throw new SubscriptionManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while observing the database connection while checking the application is "
                    + "subscribed one or not";
            log.error(msg, e);
            throw new SubscriptionManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public List<ScheduledSubscriptionDTO> cleanScheduledSubscriptions() throws SubscriptionManagementException {
        try {
            // Cleaning up already executed, missed and failed tasks
            ConnectionManagerUtil.beginDBTransaction();
            List<ScheduledSubscriptionDTO> taskList = subscriptionDAO
                    .getScheduledSubscriptionByStatus(ExecutionStatus.EXECUTED, false);
            taskList.addAll(subscriptionDAO.getNonExecutedSubscriptions());
            taskList.addAll(subscriptionDAO.getScheduledSubscriptionByStatus(ExecutionStatus.FAILED, false));
            List<Integer> tasksToClean = taskList.stream().map(ScheduledSubscriptionDTO::getId)
                    .collect(Collectors.toList());
            if (!subscriptionDAO.deleteScheduledSubscription(tasksToClean)) {
                ConnectionManagerUtil.rollbackDBTransaction();
            }
            ConnectionManagerUtil.commitDBTransaction();
            return taskList;
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while cleaning up the old subscriptions.";
            log.error(msg, e);
            throw new SubscriptionManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while executing database transaction";
            log.error(msg, e);
            throw new SubscriptionManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while retrieving the database connection to clean the scheduled subscriptions";
            log.error(msg, e);
            throw new SubscriptionManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public ScheduledSubscriptionDTO getPendingScheduledSubscription(String taskName)
            throws SubscriptionManagementException {
        try {
            ConnectionManagerUtil.openDBConnection();
            return subscriptionDAO.getPendingScheduledSubscriptionByTaskName(taskName);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred while retrieving subscription for task: " + taskName;
            log.error(msg, e);
            throw new SubscriptionManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while retrieving the database connection";
            log.error(msg, e);
            throw new SubscriptionManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void updateScheduledSubscriptionStatus(int id, ExecutionStatus status)
            throws SubscriptionManagementException {
        try {
            ConnectionManagerUtil.beginDBTransaction();
            if (!subscriptionDAO.updateScheduledSubscriptionStatus(id, status)) {
                ConnectionManagerUtil.rollbackDBTransaction();
                String msg = "Unable to update the status of the subscription: " + id;
                log.error(msg);
                throw new SubscriptionManagementException(msg);
            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while updating the status of the subscription.";
            log.error(msg, e);
            throw new SubscriptionManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while executing database transaction.";
            log.error(msg, e);
            throw new SubscriptionManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while retrieving the database connection";
            log.error(msg, e);
            throw new SubscriptionManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public <T> void performEntAppSubscription(String applicationUUID, List<T> params, String subType, String action,
                                              boolean requiresUpdatingExternal) throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Google Ent app Install operation is received to application which has UUID " + applicationUUID
                    + " to perform on " + params.size() + " params.");
        }
        if (params.isEmpty()) {
            String msg = "In order to subscribe/unsubscribe application release, you should provide list of "
                    + "subscribers. But found an empty list of subscribers.";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        ApplicationDTO applicationDTO = getApplicationDTO(applicationUUID);
        ApplicationReleaseDTO applicationReleaseDTO = applicationDTO.getApplicationReleaseDTOs().get(0);
        int applicationReleaseId = applicationReleaseDTO.getId();
        if (!ApplicationType.PUBLIC.toString().equals(applicationDTO.getType())) {
            String msg = "Application type is not public. Hence you can't perform google ent.install operation on "
                    + "this application. Application name " + applicationDTO.getName() + " Application Type "
                    + applicationDTO.getType();
            log.error(msg);
            throw new BadRequestException(msg);
        }

        List<String> categories = getApplicationCategories(applicationDTO.getId());
        if (!categories.contains("GooglePlaySyncedApp")) {
            String msg = "This is not google play store synced application. Hence can't perform enterprise app "
                    + "installation.";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        List<Integer> appReSubscribingDeviceIds = new ArrayList<>();
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();

        ApplicationSubscriptionInfo applicationSubscriptionInfo = getAppSubscriptionInfo(applicationDTO, subType,
                params);
        applicationSubscriptionInfo.getDevices().forEach(device -> {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(device.getDeviceIdentifier());
            deviceIdentifier.setType(device.getType());
            deviceIdentifiers.add(deviceIdentifier);
        });

        if (requiresUpdatingExternal) {
            //Installing the application
            ApplicationPolicyDTO applicationPolicyDTO = new ApplicationPolicyDTO();
            applicationPolicyDTO.setApplicationDTO(applicationDTO);
            applicationPolicyDTO.setDeviceIdentifierList(deviceIdentifiers);
            applicationPolicyDTO.setAction(action.toUpperCase());
            installEnrollmentApplications(applicationPolicyDTO);
        }

        List<Integer> appSubscribingDeviceIds = applicationSubscriptionInfo.getDevices().stream().map(Device::getId)
                .collect(Collectors.toList());
        Map<Integer, DeviceSubscriptionDTO> deviceSubscriptions = getDeviceSubscriptions(appSubscribingDeviceIds,
                applicationReleaseId);
        for (Map.Entry<Integer, DeviceSubscriptionDTO> deviceSubscription : deviceSubscriptions.entrySet()) {
            appReSubscribingDeviceIds.add(deviceSubscription.getKey());
            appSubscribingDeviceIds.remove(deviceSubscription.getKey());
        }

        updateSubscriptionsForEntInstall(applicationReleaseId, appSubscribingDeviceIds, appReSubscribingDeviceIds,
                applicationSubscriptionInfo.getSubscribers(), subType, action);
    }

    @Override
    public void installAppsForDevice(DeviceIdentifier deviceIdentifier, List<App> apps)
            throws ApplicationManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        Device device;
        try {
            device = DataHolder.getInstance().getDeviceManagementService().getDevice(deviceIdentifier, false);
            if (device == null) {
                String msg = "Invalid device identifier is received and couldn't find an deveice for the requested "
                        + "device identifier. Device UUID: " + deviceIdentifier.getId() + " Device Type: "
                        + deviceIdentifier.getType();
                log.error(msg);
                throw new BadRequestException(msg);
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occured while getting device data for given device identifier.Device UUID: "
                    + deviceIdentifier.getId() + " Device Type: " + deviceIdentifier.getType();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }

        List<DeviceIdentifier> appInstallingDevices = new ArrayList<>();

        for (App app : apps) {
            String releaseUUID = app.getId();
            try {
                ConnectionManagerUtil.openDBConnection();
                ApplicationDTO applicationDTO = this.applicationDAO.getAppWithRelatedRelease(releaseUUID, tenantId);
                if (applicationDTO != null) {
                    List<DeviceSubscriptionDTO> deviceSubscriptionDTOS = this.subscriptionDAO
                            .getDeviceSubscriptions(applicationDTO.getApplicationReleaseDTOs().get(0).getId(),
                                    tenantId, null, null);

                    AtomicBoolean isAppSubscribable = new AtomicBoolean(true);
                    for (DeviceSubscriptionDTO deviceSubscriptionDTO : deviceSubscriptionDTOS) {
                        if (device.getId() == deviceSubscriptionDTO.getDeviceId() && (
                                Operation.Status.PENDING.toString().equals(deviceSubscriptionDTO.getStatus())
                                        || Operation.Status.IN_PROGRESS.toString()
                                        .equals(deviceSubscriptionDTO.getStatus()))) {
                            isAppSubscribable.set(false);
                            break;
                        }
                    }
                    if (isAppSubscribable.get()) {
                        appInstallingDevices.add(deviceIdentifier);
                    }
                }
            } catch (DBConnectionException e) {
                String msg = " Error occurred while getting DB connection to retrieve app data data from DB. Device "
                        + "UUID: " + deviceIdentifier.getId() + " Device Type: " + deviceIdentifier.getType();
                log.error(msg, e);
                throw new ApplicationManagementException(msg, e);
            } catch (ApplicationManagementDAOException e) {
                String msg = " Error occurred while getting application data from DB. Device UUID: " + deviceIdentifier
                        .getId() + " Device Type: " + deviceIdentifier.getType();
                log.error(msg, e);
                throw new ApplicationManagementException(msg, e);
            } finally {
                ConnectionManagerUtil.closeDBConnection();
            }

            if (!appInstallingDevices.isEmpty()) {
                performBulkAppOperation(releaseUUID, appInstallingDevices, SubscriptionType.DEVICE.toString(),
                        SubAction.INSTALL.toString(), app.getProperties());
            }
        }
    }

    /**
     * Gets Application subscribing info by using requesting params
     *
     * @param applicationDTO Application DTO
     * @param params         list of subscribers. This list can be of either
     *                       {@link org.wso2.carbon.device.mgt.common.DeviceIdentifier} if {@param subType} is equal
     *                       to DEVICE or
     *                       {@link String} if {@param subType} is USER, ROLE or GROUP
     * @param subType        subscription type. E.g. <code>DEVICE, USER, ROLE, GROUP</code> {@see {
     * @param <T>            generic type of the method.
     * @return {@link ApplicationSubscriptionInfo}
     * @throws ApplicationManagementException if error occurred while getting Application subscription info
     */
    private <T> ApplicationSubscriptionInfo getAppSubscriptionInfo(ApplicationDTO applicationDTO, String subType,
            List<T> params) throws ApplicationManagementException {

        DeviceManagementProviderService deviceManagementProviderService = HelperUtil
                .getDeviceManagementProviderService();
        GroupManagementProviderService groupManagementProviderService = HelperUtil.getGroupManagementProviderService();
        List<Device> devices = new ArrayList<>();
        List<String> subscribers = new ArrayList<>();
        List<DeviceIdentifier> errorDeviceIdentifiers = new ArrayList<>();
        String deviceTypeName = null;

        List<String> allowingDeviceStatuses = new ArrayList<>();
        allowingDeviceStatuses.add(EnrolmentInfo.Status.ACTIVE.toString());
        allowingDeviceStatuses.add(EnrolmentInfo.Status.INACTIVE.toString());
        allowingDeviceStatuses.add(EnrolmentInfo.Status.UNREACHABLE.toString());

        try {
            if (!ApplicationType.WEB_CLIP.toString().equals(applicationDTO.getType())) {
                deviceTypeName = APIUtil.getDeviceTypeData(applicationDTO.getDeviceTypeId()).getName();
            }

            if (SubscriptionType.DEVICE.toString().equals(subType)) {
                for (T param : params) {
                    DeviceIdentifier deviceIdentifier = (DeviceIdentifier) param;
                    if (StringUtils.isEmpty(deviceIdentifier.getId()) || StringUtils
                            .isEmpty(deviceIdentifier.getType())) {
                        log.warn("Found a device identifier which has either empty identity of the device or empty"
                                + " device type. Hence ignoring the device identifier. ");
                        continue;
                    }
                    if (!ApplicationType.WEB_CLIP.toString().equals(applicationDTO.getType()) && !deviceIdentifier
                            .getType().equals(deviceTypeName)) {
                        log.warn("Found a device identifier which is not matched with the supported device type "
                                + "of the application release which has UUID " + applicationDTO
                                .getApplicationReleaseDTOs().get(0).getUuid() + " Application "
                                + "supported device type is " + deviceTypeName + " and the identifier of which has a "
                                + "different device type is " + deviceIdentifier.getId());
                        errorDeviceIdentifiers.add(deviceIdentifier);
                        continue;
                    }
                    devices.add(deviceManagementProviderService.getDevice(deviceIdentifier, false));
                }
            } else {
                if (SubscriptionType.USER.toString().equalsIgnoreCase(subType)) {
                    for (T param : params) {
                        String username = (String) param;
                        subscribers.add(username);
                        devices.addAll(deviceManagementProviderService.getDevicesOfUser(username,
                                allowingDeviceStatuses, false  ));
                    }
                } else {
                    if (SubscriptionType.ROLE.toString().equalsIgnoreCase(subType)) {
                        for (T param : params) {
                            String roleName = (String) param;
                            subscribers.add(roleName);
                            devices.addAll(deviceManagementProviderService
                                    .getAllDevicesOfRole(roleName, allowingDeviceStatuses, false));
                        }
                    } else {
                        if (SubscriptionType.GROUP.toString().equalsIgnoreCase(subType)) {
                            for (T param : params) {
                                String groupName = (String) param;
                                subscribers.add(groupName);
                                devices.addAll(groupManagementProviderService.getAllDevicesOfGroup(groupName,
                                        allowingDeviceStatuses, true));
                            }
                        } else {
                            String msg =
                                    "Found invalid subscription type " + subType + " to install application release";
                            log.error(msg);
                            throw new BadRequestException(msg);
                        }
                    }
                }
            }

            if (!ApplicationType.WEB_CLIP.toString().equals(applicationDTO.getType()) && !SubscriptionType.DEVICE
                    .toString().equals(subType)) {
                //filter devices by device type
                String tmpDeviceTypeName = deviceTypeName;
                devices.removeIf(device -> !tmpDeviceTypeName.equals(device.getType()));
            }

            ApplicationSubscriptionInfo applicationSubscriptionInfo = new ApplicationSubscriptionInfo();
            applicationSubscriptionInfo.setDevices(devices);
            applicationSubscriptionInfo.setSubscribers(subscribers);
            applicationSubscriptionInfo.setErrorDeviceIdentifiers(errorDeviceIdentifiers);
            applicationSubscriptionInfo.setAppSupportingDeviceTypeName(deviceTypeName);
            return applicationSubscriptionInfo;
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting devices of given users or given roles or while getting device "
                    + "type info.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (GroupManagementException e) {
            String msg = "Error occurred while getting devices of given groups";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    /**
     * This method is responsible to update subscription data for google enterprise install.
     *
     * @param applicationReleaseId Application release Id
     * @param params               subscribers. If subscription is performed via user, group or role, params is a list of
     *                             {@link String}
     * @param subType              Subscription type. i.e USER, GROUP, ROLE or DEVICE
     * @param action               Performing action. (i.e INSTALL or UNINSTALL)
     * @throws ApplicationManagementException if error occurred while getting or updating subscription data.
     */
    private void updateSubscriptionsForEntInstall(int applicationReleaseId, List<Integer> appSubscribingDeviceIds,
            List<Integer> appReSubscribingDeviceIds, List<String> params, String subType, String action)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        try {
            ConnectionManagerUtil.beginDBTransaction();
            updateBulkSubscribers(applicationReleaseId, params, subType, action, tenantId, username);

            if (SubAction.INSTALL.toString().equalsIgnoreCase(action) && !appSubscribingDeviceIds.isEmpty()) {
                subscriptionDAO.addDeviceSubscription(username, appSubscribingDeviceIds, subType,
                        Operation.Status.COMPLETED.toString(), applicationReleaseId, tenantId);
            }
            if (!appReSubscribingDeviceIds.isEmpty()) {
                subscriptionDAO.updateDeviceSubscription(username, appReSubscribingDeviceIds, action, subType,
                        Operation.Status.COMPLETED.toString(), applicationReleaseId, tenantId);
            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg =
                    "Error occurred when adding subscription data for application release ID: " + applicationReleaseId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred when getting database connection to add new device subscriptions to application.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "SQL Error occurred when adding new device subscription to application release which has ID: "
                    + applicationReleaseId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * THis method is responsible to validate application install or uninstall request.
     *
     * @param params  params could be either list of {@link DeviceIdentifier} or list of username or list of group
     *                names or list or role names.
     * @param subType Subscription type. i.e DEVICE or USER or ROLE or GROUP
     * @param action  performing action. i.e Install or Uninstall
     * @throws BadRequestException if incompatible data is found with app install/uninstall request.
     */
    private <T> void validateRequest(List<T> params, String subType, String action) throws BadRequestException {
        if (params.isEmpty()) {
            String msg = "In order to subscribe/unsubscribe application release, you should provide list of "
                    + "subscribers. But found an empty list of subscribers.";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        boolean isValidSubType = Arrays.stream(SubscriptionType.values())
                .anyMatch(sub -> sub.name().equalsIgnoreCase(subType));
        if (!isValidSubType) {
            String msg = "Found invalid subscription type " + subType + " to subscribe application release";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        boolean isValidAction = Arrays.stream(SubAction.values()).anyMatch(sub -> sub.name().equalsIgnoreCase(action));
        if (!isValidAction) {
            String msg = "Found invalid action " + action + " to perform on application release";
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }

    /**
     * This method perform given action (i.e APP INSTALL or APP UNINSTALL) on given set of devices.
     *
     * @param deviceType     Application supported device type.
     * @param devices        List of devices that action is triggered.
     * @param applicationDTO Application data
     * @param subType        Subscription type (i.e USER, ROLE, GROUP or DEVICE)
     * @param subscribers    Subscribers
     * @param action         Performing action. (i.e INSTALL or UNINSTALL)
     * @return {@link ApplicationInstallResponse}
     * @throws ApplicationManagementException if error occured when adding operation on device or updating subscription
     *                                        data.
     */
    private ApplicationInstallResponse performActionOnDevices(String deviceType, List<Device> devices,
            ApplicationDTO applicationDTO, String subType, List<String> subscribers, String action, Properties properties)
            throws ApplicationManagementException {

        //Get app subscribing info of each device
        SubscribingDeviceIdHolder subscribingDeviceIdHolder = getSubscribingDeviceIdHolder(devices,
                applicationDTO.getApplicationReleaseDTOs().get(0).getId());

        List<Activity> activityList = new ArrayList<>();
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        List<DeviceIdentifier> ignoredDeviceIdentifiers = new ArrayList<>();
        Map<String, List<DeviceIdentifier>> deviceIdentifierMap = new HashMap<>();

        if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
            deviceIdentifiers.addAll(new ArrayList<>(subscribingDeviceIdHolder.getAppInstallableDevices().keySet()));
            deviceIdentifiers.addAll(new ArrayList<>(subscribingDeviceIdHolder.getAppReInstallableDevices().keySet()));
            deviceIdentifiers.addAll(new ArrayList<>(subscribingDeviceIdHolder.getAppInstalledDevices().keySet()));
        } else {
            if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                deviceIdentifiers.addAll(new ArrayList<>(subscribingDeviceIdHolder.getAppInstalledDevices().keySet()));
                deviceIdentifiers
                        .addAll(new ArrayList<>(subscribingDeviceIdHolder.getAppReUnInstallableDevices().keySet()));
                ignoredDeviceIdentifiers
                        .addAll(new ArrayList<>(subscribingDeviceIdHolder.getAppInstallableDevices().keySet()));
            }
        }

        if (deviceIdentifiers.isEmpty()) {
            ApplicationInstallResponse applicationInstallResponse = new ApplicationInstallResponse();
            applicationInstallResponse.setIgnoredDeviceIdentifiers(ignoredDeviceIdentifiers);
            return applicationInstallResponse;
        }

        //device type is getting null when we try to perform action on Web Clip.
        if (deviceType == null) {
            for (DeviceIdentifier identifier : deviceIdentifiers) {
                List<DeviceIdentifier> identifiers;
                if (!deviceIdentifierMap.containsKey(identifier.getType())) {
                    identifiers = new ArrayList<>();
                } else {
                    identifiers = deviceIdentifierMap.get(identifier.getType());
                }
                identifiers.add(identifier);
                deviceIdentifierMap.put(identifier.getType(), identifiers);
            }
            for (Map.Entry<String, List<DeviceIdentifier>> entry : deviceIdentifierMap.entrySet()) {
                Activity activity = addAppOperationOnDevices(applicationDTO, new ArrayList<>(entry.getValue()),
                        entry.getKey(), action, properties);
                activityList.add(activity);
            }
        } else {
            Activity activity = addAppOperationOnDevices(applicationDTO, deviceIdentifiers, deviceType, action, properties);
            activityList.add(activity);
        }

        ApplicationInstallResponse applicationInstallResponse = new ApplicationInstallResponse();
        applicationInstallResponse.setActivities(activityList);
        applicationInstallResponse.setIgnoredDeviceIdentifiers(ignoredDeviceIdentifiers);

        updateSubscriptions(applicationDTO.getApplicationReleaseDTOs().get(0).getId(), activityList,
                subscribingDeviceIdHolder, subscribers, subType, action);
        return applicationInstallResponse;
    }

    /**
     * Filter given devices and davide given list of device into two sets, those are already application installed
     * devices and application installable devices.
     *
     * @param devices      List of {@link Device}
     * @param appReleaseId Application release id.
     * @return {@link SubscribingDeviceIdHolder}
     * @throws ApplicationManagementException if error occured while getting device subscriptions for applicaion.
     */
    private SubscribingDeviceIdHolder getSubscribingDeviceIdHolder(List<Device> devices, int appReleaseId)
            throws ApplicationManagementException {

        SubscribingDeviceIdHolder subscribingDeviceIdHolder = new SubscribingDeviceIdHolder();
        subscribingDeviceIdHolder.setAppInstallableDevices(new HashMap<>());
        subscribingDeviceIdHolder.setAppInstalledDevices(new HashMap<>());
        subscribingDeviceIdHolder.setAppReInstallableDevices(new HashMap<>());
        subscribingDeviceIdHolder.setAppReUnInstallableDevices(new HashMap<>());
        subscribingDeviceIdHolder.setSkippedDevices(new HashMap<>());

        List<Integer> deviceIds = devices.stream().map(Device::getId).collect(Collectors.toList());
        //get device subscriptions for given device id list.
        Map<Integer, DeviceSubscriptionDTO> deviceSubscriptions = getDeviceSubscriptions(deviceIds, appReleaseId);
        for (Device device : devices) {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
            DeviceSubscriptionDTO deviceSubscriptionDTO = deviceSubscriptions.get(device.getId());
            if (deviceSubscriptionDTO != null) {
                if (Operation.Status.PENDING.toString().equals(deviceSubscriptionDTO.getStatus())
                        || Operation.Status.IN_PROGRESS.toString().equals(deviceSubscriptionDTO.getStatus())) {
                    subscribingDeviceIdHolder.getSkippedDevices().put(deviceIdentifier, device.getId());
                } else {
                    if (deviceSubscriptionDTO.isUnsubscribed()) {
                        if (!Operation.Status.COMPLETED.toString().equals(deviceSubscriptionDTO.getStatus())) {
                        /*We can't ensure whether app is uninstalled successfully or not hence allow to perform both
                        install and uninstall operations*/
                            subscribingDeviceIdHolder.getAppReUnInstallableDevices()
                                    .put(deviceIdentifier, device.getId());
                        }
                        subscribingDeviceIdHolder.getAppReInstallableDevices().put(deviceIdentifier, device.getId());
                    } else {
                        if (!deviceSubscriptionDTO.isUnsubscribed() && Operation.Status.COMPLETED.toString()
                                .equals(deviceSubscriptionDTO.getStatus())) {
                            subscribingDeviceIdHolder.getAppInstalledDevices().put(deviceIdentifier, device.getId());
                        } else {
                            subscribingDeviceIdHolder.getAppReInstallableDevices()
                                    .put(deviceIdentifier, device.getId());
                        }
                    }
                }
            } else {
                subscribingDeviceIdHolder.getAppInstallableDevices().put(deviceIdentifier, device.getId());
            }
        }
        return subscribingDeviceIdHolder;
    }

    /**
     * This method returns the application categories of a particular application
     *
     * @param id Application Id
     * @return List of application categories.
     * @throws ApplicationManagementException if error occurred while getting application categories from the DB.
     */
    private List<String> getApplicationCategories(int id) throws ApplicationManagementException {
        List<String> categories;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.openDBConnection();
            categories = this.applicationDAO.getAppCategories(id, tenantId);
            return categories;
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred while getting categories for application : " + id;
            log.error(msg, e);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /***
     * Get Application with application release which has given UUID.
     *
     * @param uuid UUID of the application release.
     * @return {@link ApplicationDTO}
     * @throws ApplicationManagementException if error occurred while getting application data from database or
     * verifying whether application is in installable state.
     */
    private ApplicationDTO getApplicationDTO(String uuid) throws ApplicationManagementException {
        ApplicationDTO applicationDTO;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.openDBConnection();
            applicationDTO = this.applicationDAO.getAppWithRelatedRelease(uuid, tenantId);
            if (applicationDTO == null) {
                String msg = "Couldn't fond an application for application release UUID: " + uuid;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            if (!lifecycleStateManager.getInstallableState()
                    .equals(applicationDTO.getApplicationReleaseDTOs().get(0).getCurrentState())) {
                String msg = "You are trying to install an application which is not in the installable state of "
                        + "its Life-Cycle. hence you are not permitted to install this application. If you "
                        + "required to install this particular application, please change the state of "
                        + "application release from : " + applicationDTO.getApplicationReleaseDTOs().get(0)
                        .getCurrentState() + " to " + lifecycleStateManager.getInstallableState();
                log.error(msg);
                throw new ForbiddenException(msg);
            }
            applicationDTO.setTags(this.applicationDAO.getAppTags(applicationDTO.getId(), tenantId));
            applicationDTO.setAppCategories(this.applicationDAO.getAppCategories(applicationDTO.getId(), tenantId));
            return applicationDTO;
        } catch (LifecycleManagementException e) {
            String msg = "Error occured when getting life-cycle state from life-cycle state manager.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred while getting application data for application release UUID: " + uuid;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * This method is responsible to update subscription data.
     *
     * @param applicationReleaseId      Application release Id
     * @param activities                List of {@link Activity}
     * @param subscribingDeviceIdHolder Subscribing device id holder.
     * @param params                    subscribers. If subscription is performed via user, group or role, params is a list of
     *                                  {@link String}
     * @param subType                   Subscription type. i.e USER, GROUP, ROLE or DEVICE
     * @param action                    performing action. ie INSTALL or UNINSTALL>
     * @throws ApplicationManagementException if error occurred while getting or updating subscription data.
     */
    private void updateSubscriptions(int applicationReleaseId, List<Activity> activities,
            SubscribingDeviceIdHolder subscribingDeviceIdHolder, List<String> params, String subType, String action)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        try {
            ConnectionManagerUtil.beginDBTransaction();
            updateBulkSubscribers(applicationReleaseId, params, subType, action, tenantId, username);
            for (Activity activity : activities) {
                int operationId = Integer.parseInt(activity.getActivityId().split("ACTIVITY_")[1]);
                List<Integer> subUpdatingDeviceIds = new ArrayList<>();
                List<Integer> subInsertingDeviceIds = new ArrayList<>();

                if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                    subUpdatingDeviceIds.addAll(getOperationAddedDeviceIds(activity,
                            subscribingDeviceIdHolder.getAppReInstallableDevices()));
                    subUpdatingDeviceIds.addAll(getOperationAddedDeviceIds(activity,
                            subscribingDeviceIdHolder.getAppInstalledDevices()));
                    subInsertingDeviceIds.addAll(getOperationAddedDeviceIds(activity,
                            subscribingDeviceIdHolder.getAppInstallableDevices()));
                } else {
                    if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                        subUpdatingDeviceIds.addAll(getOperationAddedDeviceIds(activity,
                                subscribingDeviceIdHolder.getAppInstalledDevices()));
                        subUpdatingDeviceIds.addAll(getOperationAddedDeviceIds(activity,
                                subscribingDeviceIdHolder.getAppReUnInstallableDevices()));
                    }
                }

                subscriptionDAO.addDeviceSubscription(username, subInsertingDeviceIds, subType,
                        Operation.Status.PENDING.toString(), applicationReleaseId, tenantId);
                if (!subUpdatingDeviceIds.isEmpty()) {
                    subscriptionDAO.updateDeviceSubscription(username, subUpdatingDeviceIds, action, subType,
                            Operation.Status.PENDING.toString(), applicationReleaseId, tenantId);
                }
                subUpdatingDeviceIds.addAll(subInsertingDeviceIds);
                if (!subUpdatingDeviceIds.isEmpty()) {
                    List<Integer> deviceSubIds = new ArrayList<>(
                            subscriptionDAO.getDeviceSubIds(subUpdatingDeviceIds, applicationReleaseId, tenantId));
                    subscriptionDAO.addOperationMapping(operationId, deviceSubIds, tenantId);
                }
            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg =
                    "Error occurred when adding subscription data for application release ID: " + applicationReleaseId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred when getting database connection to add new device subscriptions to application.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "SQL Error occurred when adding new device subscription to application release which has ID: "
                    + applicationReleaseId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * This method is responsible to update bulk subscriber's data. i.e USER, ROLE, GROUP. Before invoke this method it
     * is required to start DB transaction
     *
     * @param applicationReleaseId Application release Id
     * @param params               subscribers. If subscription is performed via user, group or role, params is a list of
     *                             {@link String}
     * @param subType              Subscription type. i.e USER, GROUP, ROLE or DEVICE
     * @param action               performing action. ie INSTALL or UNINSTALL>
     * @param tenantId             Tenant Id
     * @param username             Username
     * @throws ApplicationManagementDAOException if error occurred while updating or inserting subscriber entities
     */
    private void updateBulkSubscribers(int applicationReleaseId, List<String> params, String subType, String action,
            int tenantId, String username) throws ApplicationManagementDAOException {
        List<String> subscribedEntities = new ArrayList<>();
        if (SubscriptionType.USER.toString().equalsIgnoreCase(subType)) {
            subscribedEntities = subscriptionDAO.getAppSubscribedUserNames(params, applicationReleaseId, tenantId);
            params.removeAll(subscribedEntities);
            if (!params.isEmpty()) {
                subscriptionDAO.addUserSubscriptions(tenantId, username, params, applicationReleaseId, action);
            }
        } else {
            if (SubscriptionType.ROLE.toString().equalsIgnoreCase(subType)) {
                subscribedEntities = subscriptionDAO.getAppSubscribedRoleNames(params, applicationReleaseId, tenantId);
                params.removeAll(subscribedEntities);
                if (!params.isEmpty()) {
                    subscriptionDAO.addRoleSubscriptions(tenantId, username, params, applicationReleaseId, action);
                }
            } else {
                if (SubscriptionType.GROUP.toString().equalsIgnoreCase(subType)) {
                    subscribedEntities = subscriptionDAO
                            .getAppSubscribedGroupNames(params, applicationReleaseId, tenantId);
                    params.removeAll(subscribedEntities);
                    if (!params.isEmpty()) {
                        subscriptionDAO.addGroupSubscriptions(tenantId, username, params, applicationReleaseId, action);
                    }
                }
            }
        }

        if (!subscribedEntities.isEmpty()) {
            subscriptionDAO
                    .updateSubscriptions(tenantId, username, subscribedEntities, applicationReleaseId, subType, action);
        }
    }

    /**
     * This method is responsible to get device IDs thta operation has added.
     *
     * @param activity  Activity
     * @param deviceMap Device map, key is device identifier and value is primary key of device.
     * @return List of device primary keys
     */
    private List<Integer> getOperationAddedDeviceIds(Activity activity, Map<DeviceIdentifier, Integer> deviceMap) {
        List<ActivityStatus> activityStatuses = activity.getActivityStatus();
        return activityStatuses.stream().filter(status -> deviceMap.get(status.getDeviceIdentifier()) != null)
                .map(status -> deviceMap.get(status.getDeviceIdentifier())).collect(Collectors.toList());
    }

    /**
     * This method is responsible to get device subscription of particular application releasee for given set of devices.
     *
     * @param deviceIds    Set of device Ids
     * @param appReleaseId Application release Id
     * @return {@link HashMap} with key as device id and value as {@link DeviceSubscriptionDTO}
     * @throws ApplicationManagementException if error occured while executing SQL query or if more than one data found
     *                                        for a device id.
     */
    private Map<Integer, DeviceSubscriptionDTO> getDeviceSubscriptions(List<Integer> deviceIds, int appReleaseId)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

        try {
            ConnectionManagerUtil.openDBConnection();
            return this.subscriptionDAO.getDeviceSubscriptions(deviceIds, appReleaseId, tenantId);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occured when getting device subscriptions for given device IDs";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occured while getting database connection for getting device subscriptions.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * This method is responsible to add operation on given devices.
     *
     * @param applicationDTO       application.
     * @param deviceIdentifierList list of device identifiers.
     * @param deviceType           device type
     * @param action               action e.g :- INSTALL, UNINSTALL
     * @return {@link Activity}
     * @throws ApplicationManagementException if found an invalid device.
     */
    private Activity addAppOperationOnDevices(ApplicationDTO applicationDTO,
            List<DeviceIdentifier> deviceIdentifierList, String deviceType, String action, Properties properties)
            throws ApplicationManagementException {
        DeviceManagementProviderService deviceManagementProviderService = HelperUtil
                .getDeviceManagementProviderService();
        try {
            Application application = APIUtil.appDtoToAppResponse(applicationDTO);
            Operation operation = generateOperationPayloadByDeviceType(deviceType, application, action, properties);
            return deviceManagementProviderService.addOperation(deviceType, operation, deviceIdentifierList);
        } catch (OperationManagementException e) {
            String msg = "Error occurred while adding the application install operation to devices";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (InvalidDeviceException e) {
            //This exception should not occur because the validation has already been done.
            throw new ApplicationManagementException("The list of device identifiers are invalid");
        }
    }

    /**
     * This method constructs operation payload to install/uninstall an application.
     *
     * @param deviceType  Device type
     * @param application {@link Application} data.
     * @param action      Action is either ININSTALL or UNINSTALL
     * @return {@link Operation}
     * @throws ApplicationManagementException if unknown application type is found to generate operation payload or
     *                                        invalid action is found to generate operation payload.
     */
    private Operation generateOperationPayloadByDeviceType(String deviceType, Application application, String action, Properties properties)
            throws ApplicationManagementException {
        try {
            if (ApplicationType.CUSTOM.toString().equalsIgnoreCase(application.getType())) {
                ProfileOperation operation = new ProfileOperation();
                if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                    operation.setCode(MDMAppConstants.AndroidConstants.OPCODE_INSTALL_APPLICATION);
                    operation.setType(Operation.Type.PROFILE);
                    CustomApplication customApplication = new CustomApplication();
                    customApplication.setType(application.getType());
                    customApplication.setUrl(application.getApplicationReleases().get(0).getInstallerPath());
                    operation.setPayLoad(customApplication.toJSON());
                    return operation;
                } else {
                    if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                        operation.setCode(MDMAppConstants.AndroidConstants.OPCODE_UNINSTALL_APPLICATION);
                        operation.setType(Operation.Type.PROFILE);
                        CustomApplication customApplication = new CustomApplication();
                        customApplication.setType(application.getType());
                        customApplication.setAppIdentifier(application.getPackageName());
                        operation.setPayLoad(customApplication.toJSON());
                        return operation;
                    } else {
                        String msg = "Invalid Action is found. Action: " + action;
                        log.error(msg);
                        throw new ApplicationManagementException(msg);
                    }
                }
            } else {
                App app = new App();
                MobileAppTypes mobileAppType = MobileAppTypes.valueOf(application.getType());
                if (DeviceTypes.ANDROID.toString().equalsIgnoreCase(deviceType)) {
                    app.setType(mobileAppType);
                    app.setLocation(application.getApplicationReleases().get(0).getInstallerPath());
                    app.setIdentifier(application.getPackageName());
                    app.setName(application.getName());
                    app.setProperties(properties);
                    if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                        return MDMAndroidOperationUtil.createInstallAppOperation(app);
                    } else {
                        if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                            return MDMAndroidOperationUtil.createAppUninstallOperation(app);
                        } else {
                            String msg = "Invalid Action is found. Action: " + action;
                            log.error(msg);
                            throw new ApplicationManagementException(msg);
                        }
                    }
                } else {
                    if (DeviceTypes.IOS.toString().equalsIgnoreCase(deviceType)) {
                        if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                            String plistDownloadEndpoint =
                                    APIUtil.getArtifactDownloadBaseURL() + MDMAppConstants.IOSConstants.PLIST
                                            + Constants.FORWARD_SLASH + application.getApplicationReleases().get(0)
                                            .getUuid();
                            app.setType(mobileAppType);
                            app.setLocation(plistDownloadEndpoint);
                            app.setIconImage(application.getApplicationReleases().get(0).getIconPath());
                            properties.put(MDMAppConstants.IOSConstants.IS_PREVENT_BACKUP, true);
                            properties.put(MDMAppConstants.IOSConstants.IS_REMOVE_APP, true);
                            properties.put(MDMAppConstants.IOSConstants.I_TUNES_ID, application.getPackageName());
                            properties.put(MDMAppConstants.IOSConstants.LABEL, application.getName());
                            properties.put(MDMAppConstants.IOSConstants.WEB_CLIP_URL,
                                    application.getApplicationReleases().get(0).getInstallerPath());
                            app.setProperties(properties);
                            return MDMIOSOperationUtil.createInstallAppOperation(app);
                        } else {
                            if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                                if (ApplicationType.PUBLIC.toString().equals(mobileAppType.toString())) {
                                    String bundleId = getBundleId(application.getPackageName());
                                    if (bundleId == null) {
                                        String msg = "Couldn't find the bundle Id for iOS public app uninstallation";
                                        log.error(msg);
                                        throw new ApplicationManagementException(msg);
                                    }
                                    application.setPackageName(bundleId);
                                }

                                app.setType(mobileAppType);
                                app.setIdentifier(application.getPackageName());
                                app.setLocation(application.getApplicationReleases().get(0).getInstallerPath());
                                return MDMIOSOperationUtil.createAppUninstallOperation(app);
                            } else {
                                String msg = "Invalid Action is found. Action: " + action;
                                log.error(msg);
                                throw new ApplicationManagementException(msg);
                            }
                        }
                    } else {
                        if (DeviceTypes.WINDOWS.toString().equalsIgnoreCase(deviceType)) {
                            app.setType(mobileAppType);
                            app.setIdentifier(application.getPackageName());
                            app.setMetaData(application.getApplicationReleases().get(0).getMetaData());
                            app.setName(application.getInstallerName());
                            if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                                return MDMWindowsOperationUtil.createInstallAppOperation(app);
                            } else {
                                String msg = "Invalid Action is found. Action: " + action;
                                log.error(msg);
                                throw new ApplicationManagementException(msg);
                            }
                        } else {
                            String msg = "Invalid device type is found. Device Type: " + deviceType;
                            log.error(msg);
                            throw new ApplicationManagementException(msg);
                        }
                    }
                }
            }
        } catch (UnknownApplicationTypeException e) {
            String msg = "Unknown Application type is found.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    /**
     * Get the bundle id of the iOS public application.
     * @param appId Application Id
     * @return {@link String} bundle Id
     * @throws ApplicationManagementException if error occurred while getting the bundle if of the requesting public
     * application
     */
    private String getBundleId(String appId) throws ApplicationManagementException {
        try {

            URL url = new URL(Constants.APPLE_LOOKUP_URL + appId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream responseStream = connection.getInputStream();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(responseStream))) {
                StringBuilder response = new StringBuilder();
                String readLine;
                while ((readLine = in.readLine()) != null) {
                    response.append(readLine);
                }
                JSONObject obj = new JSONObject(response.toString());
                JSONArray results = obj.getJSONArray("results");
                for (int i = 0; i < results.length(); ++i) {
                    JSONObject result = results.getJSONObject(i);
                    if (StringUtils.isNotBlank(result.getString("bundleId"))) {
                        return result.getString("bundleId");
                    }
                }
                return null;
            }
        } catch (MalformedURLException e) {
            String msg = "Error occurred while constructing to get iOS public app bundle Id.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (IOException e) {
            String msg = "Error occurred while getting bundle Id of the iOS public app.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    public int installEnrollmentApplications(ApplicationPolicyDTO applicationPolicyDTO)
            throws ApplicationManagementException {

        HttpClient httpClient;
        PostMethod request;
        try {
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            ApiApplicationKey apiApplicationKey = OAuthUtils.getClientCredentials(tenantDomain);
            String username =
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration()
                            .getAdminUserName() + Constants.ApplicationInstall.AT + tenantDomain;
            AccessTokenInfo tokenInfo = OAuthUtils.getOAuthCredentials(apiApplicationKey, username);
            String requestUrl = Constants.ApplicationInstall.ENROLLMENT_APP_INSTALL_PROTOCOL + System
                    .getProperty(Constants.ApplicationInstall.IOT_CORE_HOST) + Constants.ApplicationInstall.COLON
                    + System.getProperty(Constants.ApplicationInstall.IOT_CORE_PORT)
                    + Constants.ApplicationInstall.GOOGLE_APP_INSTALL_URL;
            Gson gson = new Gson();
            String payload = gson.toJson(applicationPolicyDTO);

            StringRequestEntity requestEntity = new StringRequestEntity(payload, MediaType.APPLICATION_JSON,
                    Constants.ApplicationInstall.ENCODING);
            httpClient = new HttpClient();
            request = new PostMethod(requestUrl);
            request.addRequestHeader(Constants.ApplicationInstall.AUTHORIZATION,
                    Constants.ApplicationInstall.AUTHORIZATION_HEADER_VALUE + tokenInfo.getAccessToken());
            request.setRequestEntity(requestEntity);
            httpClient.executeMethod(request);
            return request.getStatusCode();

        } catch (UserStoreException e) {
            String msg = "Error while accessing user store for user with Android device.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (APIManagerException e) {
            String msg = "Error while retrieving access token for Android device";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (HttpException e) {
            String msg = "Error while calling the app store to install enrollment app with id: " + applicationPolicyDTO
                    .getApplicationDTO().getId() + " on device";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (IOException e) {
            String msg =
                    "Error while installing the enrollment with id: " + applicationPolicyDTO.getApplicationDTO().getId()
                            + " on device";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    @Override
    public PaginationResult getAppInstalledDevices(PaginationRequest request, String appUUID)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        DeviceManagementProviderService deviceManagementProviderService = HelperUtil
                .getDeviceManagementProviderService();

        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationDTO applicationDTO = this.applicationDAO.getAppWithRelatedRelease(appUUID, tenantId);
            int applicationReleaseId = applicationDTO.getApplicationReleaseDTOs().get(0).getId();

            List<DeviceSubscriptionDTO> deviceSubscriptionDTOS = subscriptionDAO
                    .getDeviceSubscriptions(applicationReleaseId, tenantId, null, null);
            if (deviceSubscriptionDTOS.isEmpty()) {
                PaginationResult paginationResult = new PaginationResult();
                paginationResult.setData(new ArrayList<>());
                paginationResult.setRecordsFiltered(0);
                paginationResult.setRecordsTotal(0);
                return paginationResult;
            }
            List<Integer> deviceIdList = new ArrayList<>();
            deviceSubscriptionDTOS.forEach(deviceSubscriptionDTO -> {
                if ((!deviceSubscriptionDTO.isUnsubscribed() && Operation.Status.COMPLETED.toString()
                        .equalsIgnoreCase(deviceSubscriptionDTO.getStatus())) || (deviceSubscriptionDTO.isUnsubscribed()
                        && !Operation.Status.COMPLETED.toString()
                        .equalsIgnoreCase(deviceSubscriptionDTO.getStatus()))) {
                    deviceIdList.add(deviceSubscriptionDTO.getDeviceId());
                }
            });

            if (deviceIdList.isEmpty()) {
                PaginationResult paginationResult = new PaginationResult();
                paginationResult.setData(deviceIdList);
                paginationResult.setRecordsFiltered(0);
                paginationResult.setRecordsTotal(0);
                return paginationResult;
            }
            //pass the device id list to device manager service method
            try {
                PaginationResult deviceDetails = deviceManagementProviderService.getAppSubscribedDevices
                        (request, deviceIdList);

                if (deviceDetails == null) {
                    String msg = "Couldn't found an subscribed devices details for device ids: " + deviceIdList;
                    log.error(msg);
                    throw new NotFoundException(msg);
                }
                return deviceDetails;

            } catch (DeviceManagementException e) {
                String msg = "service error occurred while getting data from the service";
                log.error(msg, e);
                throw new ApplicationManagementException(msg, e);
            }
        } catch (ApplicationManagementDAOException e) {
            String msg =
                    "Error occurred when get application release data for application" + " release UUID: " + appUUID;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "DB Connection error occurred while getting device details that " + "given application id";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public PaginationResult getAppInstalledSubscribers(int offsetValue, int limitValue, String appUUID, String subType)
            throws ApplicationManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        PaginationResult paginationResult = new PaginationResult();
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationDTO applicationDTO = this.applicationDAO.getAppWithRelatedRelease(appUUID, tenantId);
            int applicationReleaseId = applicationDTO.getApplicationReleaseDTOs().get(0).getId();

            List<String> subscriptionList = new ArrayList<>();
            int count = 0;

            if (SubscriptionType.USER.toString().equalsIgnoreCase(subType)) {
                subscriptionList = subscriptionDAO
                        .getAppSubscribedUsers(offsetValue, limitValue, applicationReleaseId, tenantId);
                count = subscriptionDAO.getSubscribedUserCount(applicationReleaseId, tenantId);
            } else {
                if (SubscriptionType.ROLE.toString().equalsIgnoreCase(subType)) {
                    subscriptionList = subscriptionDAO
                            .getAppSubscribedRoles(offsetValue, limitValue, applicationReleaseId, tenantId);
                    count = subscriptionDAO.getSubscribedRoleCount(applicationReleaseId, tenantId);
                } else {
                    if (SubscriptionType.GROUP.toString().equalsIgnoreCase(subType)) {
                        subscriptionList = subscriptionDAO
                                .getAppSubscribedGroups(offsetValue, limitValue, applicationReleaseId, tenantId);
                        count = subscriptionDAO.getSubscribedGroupCount(applicationReleaseId, tenantId);
                    }
                }
            }

            paginationResult.setData(subscriptionList);
            paginationResult.setRecordsFiltered(count);
            paginationResult.setRecordsTotal(count);
            return paginationResult;
        } catch (ApplicationManagementDAOException e) {
            String msg =
                    "Error occurred when get application release data for application" + " release UUID: " + appUUID;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "DB Connection error occurred while getting category details that " + "given application id";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public PaginationResult getAppSubscriptionDetails(PaginationRequest request, String appUUID, String actionStatus,
                                                      String action) throws ApplicationManagementException {
        int limitValue = request.getRowCount();
        int offsetValue = request.getStartIndex();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        DeviceManagementProviderService deviceManagementProviderService = HelperUtil
                .getDeviceManagementProviderService();
        if (offsetValue < 0 || limitValue <= 0) {
            String msg = "Found incompatible values for offset and limit. Hence please check the request and resend. "
                    + "Offset " + offsetValue + " limit " + limitValue;
            log.error(msg);
            throw new BadRequestException(msg);
        }

        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationDTO applicationDTO = this.applicationDAO.getAppWithRelatedRelease(appUUID, tenantId);
            if (applicationDTO == null) {
                String msg = "Couldn't find an application with application release which has UUID " + appUUID;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            int applicationReleaseId = applicationDTO.getApplicationReleaseDTOs().get(0).getId();

            List<DeviceSubscriptionDTO> deviceSubscriptionDTOS = subscriptionDAO
                    .getDeviceSubscriptions(applicationReleaseId, tenantId, actionStatus, action);
            if (deviceSubscriptionDTOS.isEmpty()) {
                PaginationResult paginationResult = new PaginationResult();
                paginationResult.setData(new ArrayList<>());
                paginationResult.setRecordsFiltered(0);
                paginationResult.setRecordsTotal(0);
                return paginationResult;
            }
            List<Integer> deviceIdList = deviceSubscriptionDTOS.stream().map(DeviceSubscriptionDTO::getDeviceId)
                    .collect(Collectors.toList());
            try {
                //pass the device id list to device manager service method
                PaginationResult paginationResult = deviceManagementProviderService.getAppSubscribedDevices
                        (request, deviceIdList);
                List<DeviceSubscriptionData> deviceSubscriptionDataList = new ArrayList<>();

                if (!paginationResult.getData().isEmpty()) {
                    List<Device> devices = (List<Device>) paginationResult.getData();
                    for (Device device : devices) {
                        DeviceSubscriptionData deviceSubscriptionData = new DeviceSubscriptionData();
                        for (DeviceSubscriptionDTO subscription : deviceSubscriptionDTOS) {
                            if (subscription.getDeviceId() == device.getId()) {
                                deviceSubscriptionData.setDevice(device);
                                if (subscription.isUnsubscribed()) {
                                    deviceSubscriptionData.setAction(Constants.UNSUBSCRIBED);
                                    deviceSubscriptionData.setActionTriggeredBy(subscription.getUnsubscribedBy());
                                    deviceSubscriptionData
                                            .setActionTriggeredTimestamp(subscription.getUnsubscribedTimestamp().getTime() / 1000);
                                } else {
                                    deviceSubscriptionData.setAction(Constants.SUBSCRIBED);
                                    deviceSubscriptionData.setActionTriggeredBy(subscription.getSubscribedBy());
                                    deviceSubscriptionData
                                            .setActionTriggeredTimestamp(subscription.getSubscribedTimestamp().getTime() / 1000);
                                }
                                deviceSubscriptionData.setActionType(subscription.getActionTriggeredFrom());
                                deviceSubscriptionData.setStatus(subscription.getStatus());
                                deviceSubscriptionDataList.add(deviceSubscriptionData);
                                break;
                            }
                        }
                    }
                }
                paginationResult.setData(deviceSubscriptionDataList);
                return paginationResult;
            } catch (DeviceManagementException e) {
                String msg = "service error occurred while getting device data from the device management service. "
                        + "Device ids " + deviceIdList;
                log.error(msg, e);
                throw new ApplicationManagementException(msg, e);
            }
        } catch (ApplicationManagementDAOException e) {
            String msg =
                    "Error occurred when getting application release data for application release UUID: " + appUUID;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "DB Connection error occurred while trying to get subscription data of application which has "
                    + "application release UUID " + appUUID;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public PaginationResult getAppInstalledSubscribeDevices(PaginationRequest request, String appUUID, String subType,
                                                            String subTypeName) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        DeviceManagementProviderService deviceManagementProviderService = HelperUtil
                .getDeviceManagementProviderService();
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationDTO applicationDTO = this.applicationDAO.getAppWithRelatedRelease(appUUID, tenantId);
            int applicationReleaseId = applicationDTO.getApplicationReleaseDTOs().get(0).getId();
            List<Integer> subscriptionDeviceList = new ArrayList<>();
            //todo update the API for other subscription types
            if (SubscriptionType.GROUP.toString().equalsIgnoreCase(subType)) {
                subscriptionDeviceList = subscriptionDAO
                        .getAppSubscribedDevicesForGroups(applicationReleaseId, subType, tenantId);
            } else {
                String msg = "Found invalid sub type: " + subType;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            if (subscriptionDeviceList.isEmpty()) {
                PaginationResult paginationResult = new PaginationResult();
                paginationResult.setData(subscriptionDeviceList);
                paginationResult.setRecordsFiltered(0);
                paginationResult.setRecordsTotal(0);
                return paginationResult;
            }
            return deviceManagementProviderService.getDevicesDetails(request, subscriptionDeviceList, subTypeName);
        } catch (DeviceManagementException e) {
            String msg = "service error occurred while getting device data from the device management service.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred when get application release devices data for application release UUID: "
                    + appUUID;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "DB Connection error occurred while getting category details that given application id";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }
}
