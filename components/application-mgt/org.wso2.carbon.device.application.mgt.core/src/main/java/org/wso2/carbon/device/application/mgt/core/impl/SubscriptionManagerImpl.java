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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.ApplicationInstallResponse;
import org.wso2.carbon.device.application.mgt.common.ApplicationType;
import org.wso2.carbon.device.application.mgt.common.DeviceTypes;
import org.wso2.carbon.device.application.mgt.common.SubAction;
import org.wso2.carbon.device.application.mgt.common.SubsciptionType;
import org.wso2.carbon.device.application.mgt.common.SubscribingDeviceIdHolder;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.dto.DeviceSubscriptionDTO;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
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
import org.wso2.carbon.device.mgt.common.PaginationResult;

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
            List<Device> filteredDevices = new ArrayList<>();
            List<Device> devices = new ArrayList<>();
            List<String> subscribers = new ArrayList<>();
            List<DeviceIdentifier> errorDeviceIdentifiers = new ArrayList<>();
            ApplicationInstallResponse applicationInstallResponse;

            //todo validate users, groups and roles
            ApplicationDTO applicationDTO = getApplicationDTO(applicationUUID);
            if (SubsciptionType.DEVICE.toString().equals(subType)) {
                for (T param : params) {
                    DeviceIdentifier deviceIdentifier = (DeviceIdentifier) param;
                    if (StringUtils.isEmpty(deviceIdentifier.getId()) || StringUtils
                            .isEmpty(deviceIdentifier.getType())) {
                        log.warn("Found a device identifier which has either empty identity of the device or empty"
                                + " device type. Hence ignoring the device identifier. ");
                    }
                    if (!ApplicationType.WEB_CLIP.toString().equals(applicationDTO.getType())) {
                        DeviceType deviceType = APIUtil.getDeviceTypeData(applicationDTO.getDeviceTypeId());
                        if (!deviceType.getName().equals(deviceIdentifier.getType())) {
                            String msg =
                                    "Found a device identifier which is not matched with the application device Type. "
                                            + "Application device type is " + deviceType.getName() + " and the "
                                            + "identifier of which has a " + "different device type is "
                                            + deviceIdentifier.getId();
                            log.warn(msg);
                            errorDeviceIdentifiers.add(deviceIdentifier);
                        }
                    }
                    devices.add(deviceManagementProviderService.getDevice(deviceIdentifier, false));
                }
            } else if (SubsciptionType.USER.toString().equalsIgnoreCase(subType)) {
                for (T param : params) {
                    String username = (String) param;
                    subscribers.add(username);
                    devices.addAll(deviceManagementProviderService.getDevicesOfUser(username));
                }
            } else if (SubsciptionType.ROLE.toString().equalsIgnoreCase(subType)) {
                for (T param : params) {
                    String roleName = (String) param;
                    subscribers.add(roleName);
                    devices.addAll(deviceManagementProviderService.getAllDevicesOfRole(roleName));
                }
            } else if (SubsciptionType.GROUP.toString().equalsIgnoreCase(subType)) {
                for (T param : params) {
                    String groupName = (String) param;
                    subscribers.add(groupName);
                    devices.addAll(groupManagementProviderService.getAllDevicesOfGroup(groupName));
                }
            }

            if (!ApplicationType.WEB_CLIP.toString().equals(applicationDTO.getType())) {
                DeviceType deviceType = APIUtil.getDeviceTypeData(applicationDTO.getDeviceTypeId());
                String deviceTypeName = deviceType.getName();
                //filter devices by device type
                for (Device device : devices) {
                    if (deviceTypeName.equals(device.getType())) {
                        filteredDevices.add(device);
                    }
                }
                applicationInstallResponse = performActionOnDevices(deviceTypeName, filteredDevices, applicationDTO,
                        subType, subscribers, action);
            } else {
                applicationInstallResponse = performActionOnDevices(null, devices, applicationDTO, subType,
                        subscribers, action);
            }
            applicationInstallResponse.setErrorDevices(errorDeviceIdentifiers);
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

    private <T> void validateRequest(List<T> params, String subType, String action) throws BadRequestException {
        if (params.isEmpty()) {
            String msg = "In order to install application release, you should provide list of subscribers. "
                    + "But found an empty list of users.";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        boolean isValidSubType = Arrays.stream(SubsciptionType.values())
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

    private ApplicationInstallResponse performActionOnDevices(String deviceType, List<Device> devices,
            ApplicationDTO applicationDTO, String subType, List<String> subscribers, String action)
            throws ApplicationManagementException {

        SubscribingDeviceIdHolder subscribingDeviceIdHolder = getSubscribingDeviceIdHolder(devices);
        List<Activity> activityList = new ArrayList<>();
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        List<DeviceIdentifier> ignoredDeviceIdentifiers = new ArrayList<>();
        Map<String, List<DeviceIdentifier>> deviceIdentifierMap = new HashMap<>();

        if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
            deviceIdentifiers = new ArrayList<>(subscribingDeviceIdHolder.getSubscribableDevices().keySet());
            ignoredDeviceIdentifiers = new ArrayList<>(subscribingDeviceIdHolder.getSubscribedDevices().keySet());

            if (deviceIdentifiers.isEmpty()) {
                ApplicationInstallResponse applicationInstallResponse = new ApplicationInstallResponse();
                applicationInstallResponse.setIgnoredDeviceIdentifiers(ignoredDeviceIdentifiers);
                return applicationInstallResponse;
            }
        } else if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
            deviceIdentifiers = new ArrayList<>(subscribingDeviceIdHolder.getSubscribedDevices().keySet());
            ignoredDeviceIdentifiers = new ArrayList<>(subscribingDeviceIdHolder.getSubscribableDevices().keySet());
            if (deviceIdentifiers.isEmpty()) {
                ApplicationInstallResponse applicationInstallResponse = new ApplicationInstallResponse();
                applicationInstallResponse.setIgnoredDeviceIdentifiers(ignoredDeviceIdentifiers);
                return applicationInstallResponse;
            }
        }

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

    private SubscribingDeviceIdHolder getSubscribingDeviceIdHolder(List<Device> devices)
            throws ApplicationManagementException {
        Map<DeviceIdentifier, Integer> subscribedDevices = new HashMap<>();
        Map<DeviceIdentifier, Integer> subscribableDevices = new HashMap<>();

        List<Integer> filteredDeviceIds = devices.stream().map(Device::getId).collect(Collectors.toList());
        //get device subscriptions for given device id list.
        Map<Integer, DeviceSubscriptionDTO> deviceSubscriptions = getDeviceSubscriptions(filteredDeviceIds);
        for (Device device : devices) {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
            DeviceSubscriptionDTO deviceSubscriptionDTO = deviceSubscriptions.get(device.getId());
            if (deviceSubscriptionDTO != null && !deviceSubscriptionDTO.isUnsubscribed() && Operation.Status.COMPLETED
                    .toString().equals(deviceSubscriptionDTO.getStatus())) {
                subscribedDevices.put(deviceIdentifier, device.getId());
            } else {
                subscribableDevices.put(deviceIdentifier, device.getId());
            }
        }

        SubscribingDeviceIdHolder subscribingDeviceIdHolder = new SubscribingDeviceIdHolder();
        subscribingDeviceIdHolder.setSubscribableDevices(subscribableDevices);
        subscribingDeviceIdHolder.setSubscribedDevices(subscribedDevices);
        return subscribingDeviceIdHolder;
    }

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
            List<Integer> deviceSubIds = new ArrayList<>();

            if (SubsciptionType.USER.toString().equalsIgnoreCase(subType)) {
                List<String> subscribedEntities = subscriptionDAO.getSubscribedUserNames(params, tenantId);
                if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                    params.removeAll(subscribedEntities);
                    subscriptionDAO.addUserSubscriptions(tenantId, username, params, applicationReleaseId);
                }
                subscriptionDAO.updateSubscriptions(tenantId, username, subscribedEntities, applicationReleaseId, subType,
                        action);
            } else if (SubsciptionType.ROLE.toString().equalsIgnoreCase(subType)) {
                List<String> subscribedEntities = subscriptionDAO.getSubscribedRoleNames(params, tenantId);
                if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                    params.removeAll(subscribedEntities);
                    subscriptionDAO.addRoleSubscriptions(tenantId, username, params, applicationReleaseId);
                }
                subscriptionDAO.updateSubscriptions(tenantId, username, subscribedEntities, applicationReleaseId, subType,
                        action);
            } else if (SubsciptionType.GROUP.toString().equalsIgnoreCase(subType)) {
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
                List<Integer> operationAddedDeviceIds = getOperationAddedDeviceIds(activity,
                        subscribingDeviceIdHolder.getSubscribableDevices());
                List<Integer> alreadySubscribedDevices = subscriptionDAO
                        .getSubscribedDeviceIds(operationAddedDeviceIds, applicationReleaseId, tenantId);
                if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                    if (!alreadySubscribedDevices.isEmpty()) {
                        List<Integer> deviceResubscribingIds = subscriptionDAO
                                .updateDeviceSubscription(username, alreadySubscribedDevices, false, subType,
                                        Operation.Status.PENDING.toString(), applicationReleaseId, tenantId);
                        operationAddedDeviceIds.removeAll(alreadySubscribedDevices);
                        deviceSubIds.addAll(deviceResubscribingIds);
                    }
                    List<Integer> subscribingDevices = subscriptionDAO
                            .addDeviceSubscription(username, operationAddedDeviceIds, subType,
                                    Operation.Status.PENDING.toString(), applicationReleaseId, tenantId);
                    deviceSubIds.addAll(subscribingDevices);
                } else if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action) && !alreadySubscribedDevices.isEmpty()) {
                    List<Integer> deviceResubscribingIds = subscriptionDAO
                            .updateDeviceSubscription(username, alreadySubscribedDevices, false, subType,
                                    Operation.Status.PENDING.toString(), applicationReleaseId, tenantId);
                    deviceSubIds.addAll(deviceResubscribingIds);
                }
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

    private List<Integer> getOperationAddedDeviceIds(Activity activity, Map<DeviceIdentifier, Integer> deviceMap) {
        List<Integer> deviceIds = new ArrayList<>();
        List<ActivityStatus> activityStatuses = activity.getActivityStatus();
        for (ActivityStatus status : activityStatuses) {
            if (status.getStatus().equals(ActivityStatus.Status.PENDING)) {
                deviceIds.add(deviceMap.get(status.getDeviceIdentifier()));
            }
        }
        return deviceIds;
    }

    private Map<Integer, DeviceSubscriptionDTO> getDeviceSubscriptions(List<Integer> filteredDeviceIds)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

        try {
            ConnectionManagerUtil.openDBConnection();
            return this.subscriptionDAO.getDeviceSubscriptions(filteredDeviceIds, tenantId);
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
            //todo rethink and modify the {@link App} usage
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
                    //todo get application package name and set
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
                    if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                        app.setType(mobileAppType);
                        app.setLocation(application.getApplicationReleases().get(0).getInstallerPath());
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
                        return MDMIOSOperationUtil.createAppUninstallOperation(app);
                    } else {
                        String msg = "Invalid Action is found. Action: " + action;
                        log.error(msg);
                        throw new ApplicationManagementException(msg);
                    }
                } else {
                    if (ApplicationType.CUSTOM.toString().equalsIgnoreCase(application.getType())) {
                        if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                            ProfileOperation operation = new ProfileOperation();
                            operation.setCode(MDMAppConstants.AndroidConstants.OPCODE_INSTALL_APPLICATION);
                            operation.setType(Operation.Type.PROFILE);
                            CustomApplication customApplication = new CustomApplication();
                            customApplication.setType(application.getType());
                            customApplication.setUrl(application.getApplicationReleases().get(0).getInstallerPath());
                            operation.setPayLoad(customApplication.toJSON());
                            return operation;
                        } else if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                            ProfileOperation operation = new ProfileOperation();
                            operation.setCode(MDMAppConstants.AndroidConstants.OPCODE_UNINSTALL_APPLICATION);
                            operation.setType(Operation.Type.PROFILE);
                            CustomApplication customApplication = new CustomApplication();
                            customApplication.setType(application.getType());
                            //todo get application package name and set
                            operation.setPayLoad(customApplication.toJSON());
                            return MDMAndroidOperationUtil.createAppUninstallOperation(app);
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
        } catch (UnknownApplicationTypeException e) {
            String msg = "Unknown Application type is found.";
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
            ConnectionManagerUtil.rollbackDBTransaction();
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
    public PaginationResult getAppInstalledCategories(int offsetValue, int limitValue,
                                                      String appUUID, String subType)
            throws ApplicationManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        PaginationResult paginationResult = new PaginationResult();
        try {
            ConnectionManagerUtil.openDBConnection();

            ApplicationDTO applicationDTO = this.applicationDAO
                    .getAppWithRelatedRelease(appUUID, tenantId);
            int applicationReleaseId = applicationDTO.getApplicationReleaseDTOs().get(0).getId();

            int count = 0;
            List<String> SubscriptionList = new ArrayList<>();

            if (SubsciptionType.USER.toString().equalsIgnoreCase(subType)) {
                SubscriptionList = subscriptionDAO
                        .getAppSubscribedUsers(offsetValue, limitValue, applicationReleaseId, tenantId);
            } else if (SubsciptionType.ROLE.toString().equalsIgnoreCase(subType)) {
                SubscriptionList = subscriptionDAO
                        .getAppSubscribedRoles(offsetValue, limitValue, applicationReleaseId, tenantId);
            } else if (SubsciptionType.GROUP.toString().equalsIgnoreCase(subType)) {
                SubscriptionList = subscriptionDAO
                        .getAppSubscribedGroups(offsetValue, limitValue, applicationReleaseId, tenantId);
            }
            count = SubscriptionList.size();
            paginationResult.setData(SubscriptionList);
            paginationResult.setRecordsFiltered(count);
            paginationResult.setRecordsTotal(count);

            return paginationResult;

        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
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
}
