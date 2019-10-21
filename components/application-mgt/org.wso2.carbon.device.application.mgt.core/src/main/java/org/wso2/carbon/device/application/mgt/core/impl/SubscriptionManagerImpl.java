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
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.ApplicationInstallResponse;
import org.wso2.carbon.device.application.mgt.common.ApplicationType;
import org.wso2.carbon.device.application.mgt.common.DeviceSubscriptionData;
import org.wso2.carbon.device.application.mgt.common.DeviceTypes;
import org.wso2.carbon.device.application.mgt.common.ExecutionStatus;
import org.wso2.carbon.device.application.mgt.common.SubAction;
import org.wso2.carbon.device.application.mgt.common.SubscriptionType;
import org.wso2.carbon.device.application.mgt.common.SubscribingDeviceIdHolder;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationPolicyDTO;
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
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.MDMAppConstants;
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
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.util.MDMAndroidOperationUtil;
import org.wso2.carbon.device.mgt.core.util.MDMIOSOperationUtil;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.device.mgt.common.PaginationResult;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
        lifecycleStateManager = DataHolder.getInstance().getLifecycleStateManager();
        this.subscriptionDAO = ApplicationManagementDAOFactory.getSubscriptionDAO();
        this.applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
    }

    @Override
    public <T> ApplicationInstallResponse performBulkAppOperation(String applicationUUID, List<T> params,
            String subType, String action) throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Install application release which has UUID " + applicationUUID + " to " + params.size()
                    + " users.");
        }
        try {
            validateRequest(params, subType, action);
            DeviceManagementProviderService deviceManagementProviderService = HelperUtil
                    .getDeviceManagementProviderService();
            GroupManagementProviderService groupManagementProviderService = HelperUtil
                    .getGroupManagementProviderService();
            String deviceTypeName = null;
            List<Device> devices = new ArrayList<>();
            List<String> subscribers = new ArrayList<>();
            List<DeviceIdentifier> errorDeviceIdentifiers = new ArrayList<>();
            ApplicationInstallResponse applicationInstallResponse;

            //todo validate users, groups and roles
            ApplicationDTO applicationDTO = getApplicationDTO(applicationUUID);
            if (SubscriptionType.DEVICE.toString().equals(subType)) {
                for (T param : params) {
                    DeviceIdentifier deviceIdentifier = (DeviceIdentifier) param;
                    if (StringUtils.isEmpty(deviceIdentifier.getId()) || StringUtils
                            .isEmpty(deviceIdentifier.getType())) {
                        log.warn("Found a device identifier which has either empty identity of the device or empty"
                                + " device type. Hence ignoring the device identifier. ");
                        continue;
                    }
                    if (!ApplicationType.WEB_CLIP.toString().equals(applicationDTO.getType())) {
                        DeviceType deviceType = APIUtil.getDeviceTypeData(applicationDTO.getDeviceTypeId());
                        if (!deviceType.getName().equals(deviceIdentifier.getType())) {
                            log.warn("Found a device identifier which is not matched with the supported device type "
                                    + "of the application release which has UUID " + applicationUUID + " Application "
                                    + "supported device type is " + deviceType.getName() + " and the "
                                    + "identifier of which has a different device type is " + deviceIdentifier.getId());
                            errorDeviceIdentifiers.add(deviceIdentifier);
                            continue;
                        }
                    }
                    devices.add(deviceManagementProviderService.getDevice(deviceIdentifier, false));
                }
            } else if (SubscriptionType.USER.toString().equalsIgnoreCase(subType)) {
                for (T param : params) {
                    String username = (String) param;
                    subscribers.add(username);
                    devices.addAll(deviceManagementProviderService.getDevicesOfUser(username));
                }
            } else if (SubscriptionType.ROLE.toString().equalsIgnoreCase(subType)) {
                for (T param : params) {
                    String roleName = (String) param;
                    subscribers.add(roleName);
                    devices.addAll(deviceManagementProviderService.getAllDevicesOfRole(roleName));
                }
            } else if (SubscriptionType.GROUP.toString().equalsIgnoreCase(subType)) {
                for (T param : params) {
                    String groupName = (String) param;
                    subscribers.add(groupName);
                    devices.addAll(groupManagementProviderService.getAllDevicesOfGroup(groupName));
                }
            }

            if (!ApplicationType.WEB_CLIP.toString().equals(applicationDTO.getType())) {
                DeviceType deviceType = APIUtil.getDeviceTypeData(applicationDTO.getDeviceTypeId());
                deviceTypeName = deviceType.getName();
                //filter devices by device type
                String tmpDeviceTypeName = deviceTypeName;
                devices.removeIf(device -> !tmpDeviceTypeName.equals(device.getType()));
            }

            applicationInstallResponse = performActionOnDevices(deviceTypeName, devices, applicationDTO,
                    subType, subscribers, action);
            applicationInstallResponse.setErrorDeviceIdentifiers(errorDeviceIdentifiers);
            return applicationInstallResponse;
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting devices of given users or given roles.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (GroupManagementException e) {
            String msg = "Error occurred while getting devices of given groups";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    @Override
    public void createScheduledSubscription(ScheduledSubscriptionDTO subscriptionDTO)
            throws SubscriptionManagementException {
        try {
            ConnectionManagerUtil.beginDBTransaction();
            ScheduledSubscriptionDTO existingEntry = subscriptionDAO.getPendingScheduledSubscriptionByTaskName(
                    subscriptionDTO.getTaskName());
            boolean transactionStatus;
            if (existingEntry == null) {
                transactionStatus = subscriptionDAO.createScheduledSubscription(subscriptionDTO);
            } else {
                transactionStatus = subscriptionDAO.updateScheduledSubscription(existingEntry.getId(),
                        subscriptionDTO.getScheduledAt(), subscriptionDTO.getScheduledBy());
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
    public List<ScheduledSubscriptionDTO> cleanScheduledSubscriptions() throws SubscriptionManagementException {
        try {
            // Cleaning up already executed, missed and failed tasks
            ConnectionManagerUtil.beginDBTransaction();
            List<ScheduledSubscriptionDTO> taskList = subscriptionDAO.getScheduledSubscriptionByStatus(
                    ExecutionStatus.EXECUTED, false);
            taskList.addAll(subscriptionDAO.getNonExecutedSubscriptions());
            taskList.addAll(subscriptionDAO.getScheduledSubscriptionByStatus(ExecutionStatus.FAILED, false));
            List<Integer> tasksToClean = taskList.stream().map(ScheduledSubscriptionDTO::getId).collect(
                    Collectors.toList());
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
            String msg = "Error occurred while retrieving the database connection";
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

    private <T> void validateRequest(List<T> params, String subType, String action) throws BadRequestException {
        if (params.isEmpty()) {
            String msg = "In order to install application release, you should provide list of subscribers. "
                    + "But found an empty list of users.";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        boolean isValidSubType = Arrays.stream(SubscriptionType.values())
                .anyMatch(sub -> sub.name().equalsIgnoreCase(subType));
        if (!isValidSubType) {
            String msg = "Found invalid subscription type " + subType+  " to install application release" ;
            log.error(msg);
            throw new BadRequestException(msg);
        }
        boolean isValidAction = Arrays.stream(SubAction.values())
                .anyMatch(sub -> sub.name().equalsIgnoreCase(action));
        if (!isValidAction) {
            String msg = "Found invalid action " + action +" to perform on application release";
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }

    /***
     * This method perform given action (i.e APP INSTALL or APP UNINSTALL) on given set of devices.
     *
     * @param deviceType Application supported device type.
     * @param devices List of devices that action is triggered.
     * @param applicationDTO Application data
     * @param subType Subscription type (i.e USER, ROLE, GROUP or DEVICE)
     * @param subscribers Subscribers
     * @param action Performing action. (i.e INSTALL or UNINSTALL)
     * @return {@link ApplicationInstallResponse}
     * @throws ApplicationManagementException if error occured when adding operation on device or updating subscription
     * data.
     */
    private ApplicationInstallResponse performActionOnDevices(String deviceType, List<Device> devices,
            ApplicationDTO applicationDTO, String subType, List<String> subscribers, String action)
            throws ApplicationManagementException {

        SubscribingDeviceIdHolder subscribingDeviceIdHolder = getSubscribingDeviceIdHolder(devices,
                applicationDTO.getApplicationReleaseDTOs().get(0).getId());
        List<Activity> activityList = new ArrayList<>();
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        List<DeviceIdentifier> ignoredDeviceIdentifiers = new ArrayList<>();
        Map<String, List<DeviceIdentifier>> deviceIdentifierMap = new HashMap<>();

        if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
            deviceIdentifiers.addAll(new ArrayList<>(subscribingDeviceIdHolder.getAppInstallableDevices().keySet()));
            deviceIdentifiers.addAll(new ArrayList<>(subscribingDeviceIdHolder.getAppReInstallableDevices().keySet()));
            ignoredDeviceIdentifiers
                    .addAll(new ArrayList<>(subscribingDeviceIdHolder.getAppInstalledDevices().keySet()));
        } else if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
            deviceIdentifiers.addAll(new ArrayList<>(subscribingDeviceIdHolder.getAppInstalledDevices().keySet()));
            ignoredDeviceIdentifiers
                    .addAll(new ArrayList<>(subscribingDeviceIdHolder.getAppInstallableDevices().keySet()));
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
                    identifiers.add(identifier);
                    deviceIdentifierMap.put(identifier.getType(), identifiers);
                } else {
                    identifiers = deviceIdentifierMap.get(identifier.getType());
                    identifiers.add(identifier);
                    deviceIdentifierMap.put(identifier.getType(), identifiers);
                }
            }
            for (Map.Entry<String, List<DeviceIdentifier>> entry : deviceIdentifierMap.entrySet()) {
                Activity activity = addAppOperationOnDevices(applicationDTO, new ArrayList<>(entry.getValue()),
                        entry.getKey(), action);
                activityList.add(activity);
            }
        } else if (applicationDTO.getType().equals(ApplicationType.PUBLIC.toString())) {
            List<String> categories = getApplicationCategories(applicationDTO.getId());
            if (categories.contains("GooglePlaySyncedApp")) {
                ApplicationPolicyDTO applicationPolicyDTO = new ApplicationPolicyDTO();
                applicationPolicyDTO.setApplicationDTO(applicationDTO);
                applicationPolicyDTO.setDeviceIdentifierList(deviceIdentifiers);
                applicationPolicyDTO.setAction(action);
                installEnrollmentApplications(applicationPolicyDTO);
            } else {
                Activity activity = addAppOperationOnDevices(applicationDTO, deviceIdentifiers, deviceType, action);
                activityList.add(activity);
            }
        } else {
            Activity activity = addAppOperationOnDevices(applicationDTO, deviceIdentifiers, deviceType, action);
            activityList.add(activity);
        }

        ApplicationInstallResponse applicationInstallResponse = new ApplicationInstallResponse();
        applicationInstallResponse.setActivities(activityList);
        applicationInstallResponse.setIgnoredDeviceIdentifiers(ignoredDeviceIdentifiers);

        updateSubscriptions(applicationDTO.getApplicationReleaseDTOs().get(0).getId(), activityList,
                subscribingDeviceIdHolder, subscribers, subType, action);
        return applicationInstallResponse;
    }

    /***
     * Filter given devices and davide given list of device into two sets, those are already application installed
     * devices and application installable devices.
     *
     * @param devices List of {@link Device}
     * @param appReleaseId Application release id.
     * @return {@link SubscribingDeviceIdHolder}
     * @throws ApplicationManagementException if error occured while getting device subscriptions for applicaion.
     */
    private SubscribingDeviceIdHolder getSubscribingDeviceIdHolder(List<Device> devices, int appReleaseId)
            throws ApplicationManagementException {
        Map<DeviceIdentifier, Integer> appInstalledDevices = new HashMap<>();
        Map<DeviceIdentifier, Integer> appInstallableDevices = new HashMap<>();
        Map<DeviceIdentifier, Integer> appReInstallableDevices = new HashMap<>();
        Map<DeviceIdentifier, Integer> skippedDevices = new HashMap<>();

        List<Integer> deviceIds = devices.stream().map(Device::getId).collect(Collectors.toList());
        //get device subscriptions for given device id list.
        Map<Integer, DeviceSubscriptionDTO> deviceSubscriptions = getDeviceSubscriptions(deviceIds, appReleaseId);
        for (Device device : devices) {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
            DeviceSubscriptionDTO deviceSubscriptionDTO = deviceSubscriptions.get(device.getId());
            if (deviceSubscriptionDTO != null) {
                if (!deviceSubscriptionDTO.isUnsubscribed() && Operation.Status.COMPLETED.toString()
                        .equals(deviceSubscriptionDTO.getStatus())) {
                    appInstalledDevices.put(deviceIdentifier, device.getId());
                } else if (Operation.Status.PENDING.toString().equals(deviceSubscriptionDTO.getStatus())
                        || Operation.Status.IN_PROGRESS.toString().equals(deviceSubscriptionDTO.getStatus())) {
                    skippedDevices.put(deviceIdentifier, device.getId());
                } else {
                    appReInstallableDevices.put(deviceIdentifier, device.getId());
                }
            } else {
                appInstallableDevices.put(deviceIdentifier, device.getId());
            }
        }

        SubscribingDeviceIdHolder subscribingDeviceIdHolder = new SubscribingDeviceIdHolder();
        subscribingDeviceIdHolder.setAppInstallableDevices(appInstallableDevices);
        subscribingDeviceIdHolder.setAppInstalledDevices(appInstalledDevices);
        subscribingDeviceIdHolder.setAppReInstallableDevices(appReInstallableDevices);
        subscribingDeviceIdHolder.setSkippedDevices(skippedDevices);
        return subscribingDeviceIdHolder;
    }

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

    private void updateSubscriptions(int applicationReleaseId, List<Activity> activities,
            SubscribingDeviceIdHolder subscribingDeviceIdHolder, List<String> params, String subType,
            String action) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        try {
            ConnectionManagerUtil.beginDBTransaction();
            if (SubscriptionType.USER.toString().equalsIgnoreCase(subType)) {
                List<String> subscribedEntities = subscriptionDAO.getSubscribedUserNames(params, tenantId);
                if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                    params.removeAll(subscribedEntities);
                    subscriptionDAO.addUserSubscriptions(tenantId, username, params, applicationReleaseId);
                }
                subscriptionDAO.updateSubscriptions(tenantId, username, subscribedEntities, applicationReleaseId, subType,
                        action);
            } else if (SubscriptionType.ROLE.toString().equalsIgnoreCase(subType)) {
                List<String> subscribedEntities = subscriptionDAO.getSubscribedRoleNames(params, tenantId);
                if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                    params.removeAll(subscribedEntities);
                    subscriptionDAO.addRoleSubscriptions(tenantId, username, params, applicationReleaseId);
                }
                subscriptionDAO.updateSubscriptions(tenantId, username, subscribedEntities, applicationReleaseId, subType,
                        action);
            } else if (SubscriptionType.GROUP.toString().equalsIgnoreCase(subType)) {
                List<String> subscribedEntities = subscriptionDAO.getSubscribedGroupNames(params, tenantId);
                if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                    params.removeAll(subscribedEntities);
                    subscriptionDAO.addGroupSubscriptions(tenantId, username, params, applicationReleaseId);
                }
                subscriptionDAO.updateSubscriptions(tenantId, username, subscribedEntities, applicationReleaseId, subType,
                        action);
            }

            for (Activity activity : activities) {
                int operationId = Integer.parseInt(activity.getActivityId().split("ACTIVITY_")[1]);
                List<Integer> subUpdatingDeviceIds = new ArrayList<>();
                List<Integer> subInsertingDeviceIds = new ArrayList<>();
                List<Integer> deviceSubIds = new ArrayList<>();

                if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                    subUpdatingDeviceIds.addAll(getOperationAddedDeviceIds(activity,
                            subscribingDeviceIdHolder.getAppReInstallableDevices()));
                    subInsertingDeviceIds.addAll(getOperationAddedDeviceIds(activity,
                            subscribingDeviceIdHolder.getAppInstallableDevices()));

                } else if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                    subUpdatingDeviceIds.addAll(getOperationAddedDeviceIds(activity,
                            subscribingDeviceIdHolder.getAppInstalledDevices()));
                }

                List<Integer> subscribingDevices = subscriptionDAO
                        .addDeviceSubscription(username, subInsertingDeviceIds, subType,
                                Operation.Status.PENDING.toString(), applicationReleaseId, tenantId);
                subscriptionDAO.updateDeviceSubscription(username, subUpdatingDeviceIds, action, subType,
                        Operation.Status.PENDING.toString(), applicationReleaseId, tenantId);

                if (!subUpdatingDeviceIds.isEmpty()) {
                    deviceSubIds.addAll(subscriptionDAO
                            .getDeviceSubIds(subUpdatingDeviceIds, applicationReleaseId, tenantId));
                }
                deviceSubIds.addAll(subscribingDevices);

                subscriptionDAO.addOperationMapping(operationId, deviceSubIds, tenantId);
            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred when adding subscription data for application release ID: "
                    + applicationReleaseId;
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
     * This method is responsible to get device IDs thta operation has added.
     *
     * @param activity Activity
     * @param deviceMap Device map, key is device identifier and value is primary key of device.
     * @return List of device primary keys
     */
    private List<Integer> getOperationAddedDeviceIds(Activity activity, Map<DeviceIdentifier, Integer> deviceMap) {
        List<ActivityStatus> activityStatuses = activity.getActivityStatus();
        return activityStatuses.stream()
                .filter(status -> deviceMap.get(status.getDeviceIdentifier()) != null)
                .map(status -> deviceMap.get(status.getDeviceIdentifier())).collect(Collectors.toList());
    }

    /**
     * This method is responsible to get device subscription of particular application releasee for given set of devices.
     *
     * @param deviceIds Set of device Ids
     * @param appReleaseId Application release Id
     * @return {@link HashMap} with key as device id and value as {@link DeviceSubscriptionDTO}
     * @throws ApplicationManagementException if error occured while executing SQL query or if more than one data found
     * for a device id.
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
     * @param applicationDTO application.
     * @param deviceIdentifierList list of device identifiers.
     * @param deviceType device type
     * @param action action e.g :- INSTALL, UNINSTALL
     * @return {@link Activity}
     * @throws ApplicationManagementException if found an invalid device.
     */
    private Activity addAppOperationOnDevices(ApplicationDTO applicationDTO,
            List<DeviceIdentifier> deviceIdentifierList, String deviceType, String action)
            throws ApplicationManagementException {
        DeviceManagementProviderService deviceManagementProviderService = HelperUtil
                .getDeviceManagementProviderService();
        try {
            Application application = APIUtil.appDtoToAppResponse(applicationDTO);
            Operation operation = generateOperationPayloadByDeviceType(deviceType, application, action);
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

    private Operation generateOperationPayloadByDeviceType(String deviceType, Application application, String action)
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
                } else if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
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
            } else {
                App app = new App();
                MobileAppTypes mobileAppType = MobileAppTypes.valueOf(application.getType());
                if (DeviceTypes.ANDROID.toString().equalsIgnoreCase(deviceType)) {
                    app.setType(mobileAppType);
                    app.setLocation(application.getApplicationReleases().get(0).getInstallerPath());
                    app.setIdentifier(application.getPackageName());
                    app.setName(application.getName());
                    if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                        return MDMAndroidOperationUtil.createInstallAppOperation(app);
                    } else if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                        return MDMAndroidOperationUtil.createAppUninstallOperation(app);
                    } else {
                        String msg = "Invalid Action is found. Action: " + action;
                        log.error(msg);
                        throw new ApplicationManagementException(msg);
                    }
                } else if (DeviceTypes.IOS.toString().equalsIgnoreCase(deviceType)) {
                    if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                        String plistDownloadEndpoint =
                                APIUtil.getArtifactDownloadBaseURL() + MDMAppConstants.IOSConstants.PLIST
                                        + Constants.FORWARD_SLASH + application.getApplicationReleases().get(0)
                                        .getUuid();
                        app.setType(mobileAppType);
                        app.setLocation(plistDownloadEndpoint);
                        Properties properties = new Properties();
                        properties.put(MDMAppConstants.IOSConstants.IS_PREVENT_BACKUP, true);
                        properties.put(MDMAppConstants.IOSConstants.IS_REMOVE_APP, true);
                        app.setProperties(properties);
                        return MDMIOSOperationUtil.createInstallAppOperation(app);
                    } else if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                        app.setType(mobileAppType);
                        app.setIdentifier(application.getPackageName());
                        return MDMIOSOperationUtil.createAppUninstallOperation(app);
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
        } catch (UnknownApplicationTypeException e) {
            String msg = "Unknown Application type is found.";
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
            String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                    .getRealmConfiguration().getAdminUserName() + Constants.ApplicationInstall.AT + tenantDomain;
            AccessTokenInfo tokenInfo = OAuthUtils.getOAuthCredentials(apiApplicationKey, username);
            String requestUrl = Constants.ApplicationInstall.ENROLLMENT_APP_INSTALL_PROTOCOL +
                    System.getProperty(Constants.ApplicationInstall.IOT_CORE_HOST) +
                    Constants.ApplicationInstall.COLON +
                    System.getProperty(Constants.ApplicationInstall.IOT_CORE_PORT) +
                    Constants.ApplicationInstall.GOOGLE_APP_INSTALL_URL;
            Gson gson = new Gson();
            String payload = gson.toJson(applicationPolicyDTO);

            StringRequestEntity requestEntity = new StringRequestEntity(payload, MediaType.APPLICATION_JSON
                    , Constants.ApplicationInstall.ENCODING);;
            httpClient = new HttpClient();
            request = new PostMethod(requestUrl);
            request.addRequestHeader(Constants.ApplicationInstall.AUTHORIZATION
                    , Constants.ApplicationInstall.AUTHORIZATION_HEADER_VALUE + tokenInfo.getAccessToken());
            request.setRequestEntity(requestEntity);
            httpClient.executeMethod(request);
            return request.getStatusCode();

        } catch (UserStoreException e) {
            String msg = "Error while accessing user store for user with Android device.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (APIManagerException e) {
            String msg = "Error while retrieving access token for Android device" ;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (HttpException e) {
            String msg = "Error while calling the app store to install enrollment app with id: " +
                    applicationPolicyDTO.getApplicationDTO().getId() +
                    " on device";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (IOException e) {
            String msg = "Error while installing the enrollment with id: " + applicationPolicyDTO.getApplicationDTO().getId()
                    + " on device";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    @Override
    public PaginationResult getAppInstalledDevices(int offsetValue, int limitValue, String appUUID,
                                                   String status)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        DeviceManagementProviderService deviceManagementProviderService = HelperUtil
                .getDeviceManagementProviderService();

        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationDTO applicationDTO = this.applicationDAO.getAppWithRelatedRelease(appUUID, tenantId);
            int applicationReleaseId = applicationDTO.getApplicationReleaseDTOs().get(0).getId();

            List<DeviceSubscriptionDTO> deviceSubscriptionDTOS = subscriptionDAO
                    .getDeviceSubscriptions(applicationReleaseId, tenantId);
            if (deviceSubscriptionDTOS.isEmpty()) {
                String msg = "Couldn't found an subscribed devices for application release id: "
                             + applicationReleaseId;
                log.info(msg);
            }
            List<Integer> deviceIdList = new ArrayList<>();
            for (DeviceSubscriptionDTO deviceIds : deviceSubscriptionDTOS) {
                deviceIdList.add(deviceIds.getDeviceId());
            }

            if (deviceIdList.isEmpty()){
                PaginationResult paginationResult = new PaginationResult();
                paginationResult.setData(deviceIdList);
                paginationResult.setRecordsFiltered(0);
                paginationResult.setRecordsTotal(0);
                return paginationResult;
            }
            //pass the device id list to device manager service method
            try {
                PaginationResult deviceDetails = deviceManagementProviderService
                        .getAppSubscribedDevices(offsetValue ,limitValue, deviceIdList, status);

                if (deviceDetails == null) {
                    String msg = "Couldn't found an subscribed devices details for device ids: "
                                 + deviceIdList;
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
            String msg = "Error occurred when get application release data for application" +
                         " release UUID: " + appUUID;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "DB Connection error occurred while getting device details that " +
                         "given application id";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public PaginationResult getAppInstalledCategories(int offsetValue, int limitValue, String appUUID, String subType)
            throws ApplicationManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        PaginationResult paginationResult = new PaginationResult();
        try {
            ConnectionManagerUtil.openDBConnection();
            ApplicationDTO applicationDTO = this.applicationDAO
                    .getAppWithRelatedRelease(appUUID, tenantId);
            int applicationReleaseId = applicationDTO.getApplicationReleaseDTOs().get(0).getId();

            List<String> subscriptionList = new ArrayList<>();

            if (SubscriptionType.USER.toString().equalsIgnoreCase(subType)) {
                subscriptionList = subscriptionDAO
                        .getAppSubscribedUsers(offsetValue, limitValue, applicationReleaseId, tenantId);
            } else if (SubscriptionType.ROLE.toString().equalsIgnoreCase(subType)) {
                subscriptionList = subscriptionDAO
                        .getAppSubscribedRoles(offsetValue, limitValue, applicationReleaseId, tenantId);
            } else if (SubscriptionType.GROUP.toString().equalsIgnoreCase(subType)) {
                subscriptionList = subscriptionDAO
                        .getAppSubscribedGroups(offsetValue, limitValue, applicationReleaseId, tenantId);
            }
            int count = subscriptionList.size();
            paginationResult.setData(subscriptionList);
            paginationResult.setRecordsFiltered(count);
            paginationResult.setRecordsTotal(count);
            return paginationResult;
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred when get application release data for application" +
                         " release UUID: " + appUUID;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "DB Connection error occurred while getting category details that " +
                         "given application id";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public PaginationResult getAppSubscriptionDetails(int offsetValue, int limitValue, String appUUID)
            throws ApplicationManagementException {
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
                String msg = "Couldn't found an application with application release which has UUID " + appUUID;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            int applicationReleaseId = applicationDTO.getApplicationReleaseDTOs().get(0).getId();

            List<DeviceSubscriptionDTO> deviceSubscriptionDTOS = subscriptionDAO
                    .getDeviceSubscriptions(applicationReleaseId, tenantId);
            if (deviceSubscriptionDTOS.isEmpty()) {
                String msg = "Couldn't found an subscribed devices for application release id: " + applicationReleaseId;
                log.info(msg);
            }
            List<Integer> deviceIdList = deviceSubscriptionDTOS.stream().map(DeviceSubscriptionDTO::getDeviceId)
                    .collect(Collectors.toList());
            if (deviceIdList.isEmpty()) {
                PaginationResult paginationResult = new PaginationResult();
                paginationResult.setData(deviceIdList);
                paginationResult.setRecordsFiltered(0);
                paginationResult.setRecordsTotal(0);
                return paginationResult;
            }
            try {
                //pass the device id list to device manager service method
                PaginationResult paginationResult = deviceManagementProviderService
                        .getAppSubscribedDevices(offsetValue, limitValue, deviceIdList, null);
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
                                            .setActionTriggeredTimestamp(subscription.getUnsubscribedTimestamp());
                                } else {
                                    deviceSubscriptionData.setAction(Constants.SUBSCRIBED);
                                    deviceSubscriptionData.setActionTriggeredBy(subscription.getSubscribedBy());
                                    deviceSubscriptionData
                                            .setActionTriggeredTimestamp(subscription.getSubscribedTimestamp());
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
            String msg = "Error occurred when getting application release data for application release UUID: "
                    + appUUID;
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
}
