/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.device.application.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.AppOperation;
import org.wso2.carbon.device.application.mgt.common.ApplicationInstallResponse;
import org.wso2.carbon.device.application.mgt.common.ApplicationType;
import org.wso2.carbon.device.application.mgt.common.SubsciptionType;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.ApplicationInstallResponseTmp;
import org.wso2.carbon.device.application.mgt.common.dto.DeviceSubscriptionDTO;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.TransactionManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
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
import org.wso2.carbon.device.application.mgt.core.util.HelperUtil;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.ActivityStatus;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is the default implementation for the Subscription Manager.
 */
public class SubscriptionManagerImpl implements SubscriptionManager {

    private static final Log log = LogFactory.getLog(SubscriptionManagerImpl.class);
    private static final String INSTALL_APPLICATION = "INSTALL_APPLICATION";
    private SubscriptionDAO subscriptionDAO;
    private ApplicationDAO applicationDAO;
    private LifecycleStateManager lifecycleStateManager;

    public SubscriptionManagerImpl() {
        lifecycleStateManager = DataHolder.getInstance().getLifecycleStateManager();
        this.subscriptionDAO = ApplicationManagementDAOFactory.getSubscriptionDAO();
        this.applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
    }

    @Override
    public ApplicationInstallResponse installApplicationForDevices(String applicationUUID,
            List<DeviceIdentifier> deviceIdentifiers) throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Install application which has UUID: " + applicationUUID + " to " + deviceIdentifiers.size()
                    + "devices.");
        }
        ApplicationDTO applicationDTO = getApplicationDTO(applicationUUID);
        List<Integer> operationTriggeredDeviceIds = new ArrayList<>();
        List <Device> filteredDevices = validateAppInstallingForDevicesRequest(applicationDTO, deviceIdentifiers);
        List<Integer> filteredDeviceIds = new ArrayList<>();
        List<DeviceIdentifier> installedDeviceIdentifiers = new ArrayList<>();
        Map<DeviceIdentifier , Integer> compatibleDevices = new HashMap<>();
        Map<Integer, DeviceSubscriptionDTO> deviceSubscriptions;

        for (Device device : filteredDevices){
            filteredDeviceIds.add(device.getId());
        }


        deviceSubscriptions = getDeviceSubscriptions(filteredDeviceIds);
        for (Device device : filteredDevices) {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(),
                    device.getType());
            DeviceSubscriptionDTO deviceSubscriptionDTO = deviceSubscriptions.get(device.getId());
            if (deviceSubscriptionDTO != null && !deviceSubscriptionDTO.isUnsubscribed()
                    && Operation.Status.COMPLETED.toString().equals(deviceSubscriptionDTO.getStatus())) {
                installedDeviceIdentifiers.add(deviceIdentifier);
            } else {
                compatibleDevices.put(deviceIdentifier, device.getId());
            }
        }
        Activity activity = installToDevices(applicationDTO, deviceIdentifiers, deviceIdentifiers.get(0).getType());

        List<ActivityStatus> activityStatuses = activity.getActivityStatus();
        for (ActivityStatus status : activityStatuses) {
            if (status.getStatus().equals(ActivityStatus.Status.PENDING)){
                operationTriggeredDeviceIds.add(compatibleDevices.get(status.getDeviceIdentifier()));
            }
        }
        ApplicationInstallResponse applicationInstallResponse = new ApplicationInstallResponse();
        applicationInstallResponse.setActivity(activity);
        applicationInstallResponse.setAlreadyInstalledDevices(installedDeviceIdentifiers);

        int operationId = Integer
                .parseInt(activity.getActivityId().split(DeviceManagementConstants.OperationAttributes.ACTIVITY)[1]);
        addDeviceSubscriptionForUser(applicationDTO.getApplicationReleaseDTOs().get(0).getId(),
                operationTriggeredDeviceIds, new ArrayList<>(deviceSubscriptions.keySet()), null, operationId,
                SubsciptionType.DEVICE.toString());
        return applicationInstallResponse;
    }

    private ApplicationDTO getApplicationDTO(String uuid) throws ApplicationManagementException {
        ApplicationDTO applicationDTO;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.openDBConnection();
            applicationDTO = this.applicationDAO.getApplicationByUUID(uuid, tenantId);
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
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred while getting application data for application release UUID: " + uuid;
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private List <Device> validateAppInstallingForDevicesRequest(ApplicationDTO applicationDTO,
            List<DeviceIdentifier> deviceIdentifiers) throws ApplicationManagementException {
        DeviceType deviceType = null;
        List <Device> existingDevices = new ArrayList<>();
        DeviceManagementProviderService deviceManagementProviderService = HelperUtil
                .getDeviceManagementProviderService();
        if (!ApplicationType.WEB_CLIP.toString().equals(applicationDTO.getType())) {
            deviceType = APIUtil.getDeviceTypeData(applicationDTO.getDeviceTypeId());
        }

        for (DeviceIdentifier deviceIdentifier : deviceIdentifiers) {
            if (!ApplicationType.WEB_CLIP.toString().equals(applicationDTO.getType()) && deviceType != null
                    && !deviceType.getName().equals(deviceIdentifier.getType())) {
                String msg =
                        "Found a device identifier which is not matched with the application device Type. Application "
                                + "device type is " + deviceType.getName()
                                + " and identifier which has different device" + " type is " + deviceIdentifier.getId();
                log.error(msg);
                throw new BadRequestException(msg);
            }
            try {
                Device device = deviceManagementProviderService.getDevice(deviceIdentifier, false);
                if (device == null) {
                    String msg = "Couldn't found an device for device identifier " + deviceIdentifier.getId()
                            + " and device type: " + deviceIdentifier.getType();
                    log.error(msg);
                } else {
                    existingDevices.add(device);
                }
            } catch (DeviceManagementException e) {
                String msg = "Error occuered when getting device data for divice identifier " + deviceIdentifier.getId()
                        + " and device type " + deviceIdentifier.getType();
                log.error(msg);
                throw new ApplicationManagementException(msg, e);
            }
        }
        return existingDevices;
    }

    @Override
    public ApplicationInstallResponse installApplicationForUsers(String applicationUUID,
            List<String> userList) throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Install application release which has UUID " + applicationUUID + " to " + userList.size()
                    + " users.");
        }

        //todo check valid user list - throw BadRequest exception
        ApplicationDTO applicationDTO = getApplicationDTO(applicationUUID);
        DeviceType appDeviceType = APIUtil.getDeviceTypeData(applicationDTO.getDeviceTypeId());
        Map<DeviceIdentifier , Integer> compatibleDevices = new HashMap<>();
        List<Integer> operationTriggeredDeviceIds = new ArrayList<>();
        List<DeviceIdentifier> installedDeviceIdentifiers = new ArrayList<>();
        Map<Integer, DeviceSubscriptionDTO> deviceSubscriptions = new HashMap<>();

        for (String user : userList) {
            try {
                List<Device> userDevices = HelperUtil.getDeviceManagementProviderService().getDevicesOfUser(user);
                List<Integer> filteredDeviceIds = new ArrayList<>();
                List<Device> filteredDevices = new ArrayList<>();

                //todo improve for web clips
                for (Device device : userDevices) {
                    if (appDeviceType.getName().equals(device.getType())) {
                        filteredDevices.add(device);
                        filteredDeviceIds.add(device.getId());
                    }
                }
                deviceSubscriptions = getDeviceSubscriptions(filteredDeviceIds);
                for (Device device : filteredDevices) {
                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(),
                            device.getType());
                    DeviceSubscriptionDTO deviceSubscriptionDTO = deviceSubscriptions.get(device.getId());
                    if (deviceSubscriptionDTO != null && !deviceSubscriptionDTO.isUnsubscribed()
                            && Operation.Status.COMPLETED.toString().equals(deviceSubscriptionDTO.getStatus())) {
                        installedDeviceIdentifiers.add(deviceIdentifier);
                    } else {
                        compatibleDevices.put(deviceIdentifier, device.getId());
                    }
                }
            } catch (DeviceManagementException e) {
                String msg = "Error occurred when extracting the device list of user[" + user + "].";
                log.error(msg);
                throw new ApplicationManagementException(msg, e);
            }
        }
        Activity activity = installToDevices(applicationDTO, new ArrayList<>(compatibleDevices.keySet()),
                appDeviceType.getName());

        List<ActivityStatus> activityStatuses = activity.getActivityStatus();
        for (ActivityStatus status : activityStatuses) {
            if (status.getStatus().equals(ActivityStatus.Status.PENDING)){
                operationTriggeredDeviceIds.add(compatibleDevices.get(status.getDeviceIdentifier()));
            }
        }
        ApplicationInstallResponse applicationInstallResponse = new ApplicationInstallResponse();
        applicationInstallResponse.setActivity(activity);
        applicationInstallResponse.setAlreadyInstalledDevices(installedDeviceIdentifiers);

        int operationId = Integer
                .parseInt(activity.getActivityId().split(DeviceManagementConstants.OperationAttributes.ACTIVITY)[1]);
        addDeviceSubscriptionForUser(applicationDTO.getApplicationReleaseDTOs().get(0).getId(),
                operationTriggeredDeviceIds, new ArrayList<>(deviceSubscriptions.keySet()), userList, operationId, SubsciptionType.USER.toString());
        return applicationInstallResponse;
    }

    private void addDeviceSubscriptionForUser(int applicationReleaseId, List<Integer> deviceIds,
            List<Integer> subDeviceIds, List<String> userList, int operationId, String subType)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String subscriber = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        try {
            ConnectionManagerUtil.beginDBTransaction();
            List<Integer> deviceResubscribingIds = new ArrayList<>();
            List<Integer> deviceSubscriptingIds;

            if (SubsciptionType.USER.toString().equals(subType)){
                List<String> subscribedUsers = subscriptionDAO.getSubscribedUsernames(userList, tenantId);
                if (!subscribedUsers.isEmpty()) {
                    subscriptionDAO
                            .updateUserSubscription(tenantId, subscriber, false, subscribedUsers, applicationReleaseId);
                    userList.removeAll(subscribedUsers);
                }
                subscriptionDAO.subscribeUserToApplication(tenantId, subscriber, userList, applicationReleaseId);
            }

            if (!subDeviceIds.isEmpty()) {
                deviceResubscribingIds = subscriptionDAO
                        .updateDeviceSubscription(subscriber, subDeviceIds, subType, Operation.Status.PENDING.toString(),
                                applicationReleaseId, tenantId);
                deviceIds.removeAll(subDeviceIds);
            }
            deviceSubscriptingIds = subscriptionDAO
                    .subscribeDeviceToApplication(subscriber, deviceIds, subType, Operation.Status.PENDING.toString(),
                            applicationReleaseId, tenantId);
            deviceSubscriptingIds.addAll(deviceResubscribingIds);
            subscriptionDAO.addOperationMapping(operationId, deviceSubscriptingIds, tenantId);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg =
                    "Error occurred when adding subscription data for application release UUID: " + applicationReleaseId;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred when getting database connection to add new device subscriptions to application.";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg =
                    "SQL Error occurred when adding new device subscription to application release which has UUID: "
                            + applicationReleaseId;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private Map<Integer, DeviceSubscriptionDTO> getDeviceSubscriptions (List<Integer> filteredDeviceIds)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

        try {
            ConnectionManagerUtil.openDBConnection();
            return this.subscriptionDAO
                    .getDeviceSubscriptions(filteredDeviceIds, tenantId);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occured when getting device subscriptions for given device IDs";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } catch (DBConnectionException e) {
            String msg = "Error occured while getting database connection for getting device subscriptions.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }

    }

    @Override
    public ApplicationInstallResponseTmp installApplicationForRoles(String applicationUUID, List<String> roleList)
            throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Install application: " + applicationUUID + " to " + roleList.size() + " roles.");
        }
        ApplicationManager applicationManager = DataHolder.getInstance().getApplicationManager();
        ApplicationDTO application = applicationManager.getApplicationByRelease(applicationUUID);
        List<DeviceIdentifier> deviceList = new ArrayList<>();
        for (String role : roleList) {
            try {
                List<Device> devicesOfRole = HelperUtil.getDeviceManagementProviderService().getAllDevicesOfRole(role);
                devicesOfRole.stream()
                        .map(device -> new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()))
                        .forEach(deviceList::add);
                if (log.isDebugEnabled()) {
                    log.debug(devicesOfRole.size() + " found for role: " + role);
                }
            } catch (DeviceManagementException e) {
                throw new ApplicationManagementException(
                        "Error when extracting the device list from role[" + role + "].", e);
            }
        }

        ApplicationInstallResponseTmp response = installToDevicesTmp(application, deviceList);

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String subscriber = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        int applicationReleaseId = application.getApplicationReleaseDTOs().get(0).getId();

        try {
            ConnectionManagerUtil.openDBConnection();
            subscriptionDAO.subscribeRoleToApplication(tenantId, subscriber, roleList, application.getId(),
                    applicationReleaseId);
        } catch (ApplicationManagementDAOException e) {
            //todo
            throw new ApplicationManagementException("");
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }

        return response;
    }

    @Override
    public ApplicationInstallResponseTmp installApplicationForGroups(String applicationUUID, List<String> deviceGroupList)
            throws ApplicationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Install application: " + applicationUUID + " to " + deviceGroupList.size() + " groups.");
        }
        ApplicationManager applicationManager = DataHolder.getInstance().getApplicationManager();
        ApplicationDTO application = applicationManager.getApplicationByRelease(applicationUUID);
        GroupManagementProviderService groupManagementProviderService = HelperUtil.getGroupManagementProviderService();
        List<DeviceGroup> groupList = new ArrayList<>();
        List<DeviceIdentifier> deviceList = new ArrayList<>();
        for (String groupName : deviceGroupList) {
            try {
                DeviceGroup deviceGroup = groupManagementProviderService.getGroup(groupName);
                groupList.add(deviceGroup);
                int deviceCount = groupManagementProviderService.getDeviceCount(deviceGroup.getGroupId());
                List<Device> devicesOfGroups = groupManagementProviderService
                        .getDevices(deviceGroup.getGroupId(), 0, deviceCount);
                devicesOfGroups.stream()
                        .map(device -> new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()))
                        .forEach(deviceList::add);
            } catch (GroupManagementException e) {
                throw new ApplicationManagementException(
                        "Error when extracting the device list from group[" + groupName + "].", e);
            }
        }

        ApplicationInstallResponseTmp response = installToDevicesTmp(application, deviceList);

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String subscriber = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        int applicationReleaseId = application.getApplicationReleaseDTOs().get(0).getId();

        try {
            ConnectionManagerUtil.openDBConnection();
            subscriptionDAO.subscribeGroupToApplication(tenantId, subscriber, groupList, application.getId(),
                    applicationReleaseId);
        } catch (ApplicationManagementDAOException e) {
            //todo
            throw new ApplicationManagementException("");
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }

        return response;
    }

    @Override public List<DeviceIdentifier> uninstallApplication(String applicationUUID,
            List<DeviceIdentifier> deviceList) throws ApplicationManagementException {
        return null;
    }

    private Activity installToDevices(ApplicationDTO application,
            List<DeviceIdentifier> deviceIdentifierList, String deviceType) throws ApplicationManagementException {
        DeviceManagementProviderService deviceManagementProviderService = HelperUtil
                .getDeviceManagementProviderService();
        try {
            Operation operation = generateOperationPayloadByDeviceType(deviceType, application);
            //todo refactor add operation code to get successful operations
            return deviceManagementProviderService
                    .addOperation(deviceType, operation, deviceIdentifierList);
        } catch (OperationManagementException e) {
            throw new ApplicationManagementException("Error occurred while adding the application install "
                    + "operation to devices", e);
        } catch (InvalidDeviceException e) {
            //This exception should not occur because the validation has already been done.
            throw new ApplicationManagementException("The list of device identifiers are invalid");
        }
    }


    private ApplicationInstallResponseTmp installToDevicesTmp(ApplicationDTO application,
            List<DeviceIdentifier> deviceIdentifierList) throws ApplicationManagementException {
        DeviceManagementProviderService deviceManagementProviderService = HelperUtil
                .getDeviceManagementProviderService();

        ApplicationInstallResponseTmp response = validateDevices(deviceIdentifierList);
        /*
        Group the valid device list by device type. Following lambda expression produces a map containing device type
        as the key and the list of device identifiers as the corresponding value.
         */
        Map<String, List<DeviceIdentifier>> deviceTypeIdentifierMap = response.getSuccessfulDevices().stream()
                .collect(Collectors.groupingBy(DeviceIdentifier::getType));

        for (Map.Entry<String, List<DeviceIdentifier>> entry : deviceTypeIdentifierMap.entrySet()) {
            Operation operation = generateOperationPayloadByDeviceType(entry.getKey(), application);
            try {
                Activity activity = deviceManagementProviderService
                        .addOperation(entry.getKey(), operation, entry.getValue());
                response.setActivity(activity);
            } catch (OperationManagementException e) {
                response.setSuccessfulDevices(null);
                response.setFailedDevices(deviceIdentifierList);
                throw new ApplicationManagementException("Error occurred while adding the application install "
                        + "operation to devices", e);
            } catch (InvalidDeviceException e) {
                //This exception should not occur because the validation has already been done.
                throw new ApplicationManagementException("The list of device identifiers are invalid");
            }
        }

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String subscriber = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        int applicationReleaseId = application.getApplicationReleaseDTOs().get(0).getId();
        try {
            ConnectionManagerUtil.openDBConnection();
            List<Device> deviceList = new ArrayList<>();
            for (DeviceIdentifier deviceIdentifier : response.getSuccessfulDevices()) {
                try {
                    deviceList.add(deviceManagementProviderService.getDevice(deviceIdentifier));
                } catch (DeviceManagementException e) {
                    log.error("Unable to fetch device for device identifier: " + deviceIdentifier.toString());
                }
            }
            subscriptionDAO.subscribeDeviceToApplicationTmp(tenantId, subscriber, deviceList, application.getId(),
                    applicationReleaseId, String.valueOf(AppOperation.InstallState.UNINSTALLED));
        } catch (ApplicationManagementDAOException e) {
            //todo
            throw new ApplicationManagementException("");
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }

        return response;
    }

    private Operation generateOperationPayloadByDeviceType(String deviceType, ApplicationDTO application) {
        ProfileOperation operation = new ProfileOperation();
        operation.setCode(INSTALL_APPLICATION);
        operation.setType(Operation.Type.PROFILE);

        //todo: generate operation payload correctly for all types of devices.
        operation.setPayLoad(
                "{'type':'enterprise', 'url':'" + application.getApplicationReleaseDTOs().get(0).getInstallerName()
                        + "', 'app':'" + application.getApplicationReleaseDTOs().get(0).getUuid() + "'}");
        return operation;
    }

    /**
     * Validates the preconditions which is required to satisfy from the device which is required to install the
     * application.
     *
     * This method check two preconditions whether the application type is compatible to install in the device and
     * whether the device is enrolled in the system.
     *
     * @param deviceIdentifierList List of {@link DeviceIdentifier} which the validation happens
     * @return {@link ApplicationInstallResponseTmp} which contains compatible and incompatible device identifiers
     */
    private ApplicationInstallResponseTmp validateDevices(List<DeviceIdentifier> deviceIdentifierList) {
        ApplicationInstallResponseTmp applicationInstallResponseTmp = new ApplicationInstallResponseTmp();
        List<DeviceIdentifier> failedDevices = new ArrayList<>();
        List<DeviceIdentifier> compatibleDevices = new ArrayList<>();

        for (DeviceIdentifier deviceIdentifier : deviceIdentifierList) {
            try {
                if (!DeviceManagerUtil.isValidDeviceIdentifier(deviceIdentifier)) {
                    log.error("Device with ID: [" + deviceIdentifier.getId() + "] is not valid to install the "
                            + "application.");
                    applicationInstallResponseTmp.getFailedDevices().add(deviceIdentifier);
                }
            } catch (DeviceManagementException e) {
                log.error("Error occurred while validating the device: [" + deviceIdentifier.getId() + "]", e);
                failedDevices.add(deviceIdentifier);
            }
            compatibleDevices.add(deviceIdentifier);
        }
        applicationInstallResponseTmp.setFailedDevices(failedDevices);
        applicationInstallResponseTmp.setSuccessfulDevices(compatibleDevices);

        return applicationInstallResponseTmp;
    }


}
