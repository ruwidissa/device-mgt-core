/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.operation.mgt;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.ActivityPaginationRequest;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DynamicTaskContext;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.MonitoringOperation;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.ActivityStatus;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationContext;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationExecutionFailedException;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationProvider;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.cache.impl.DeviceCacheManagerImpl;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.EnrollmentDAO;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.OperationResponseMeta;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationMappingDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.util.OperationDAOUtil;
import org.wso2.carbon.device.mgt.core.operation.mgt.util.DeviceIDHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.util.OperationIdComparator;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.task.DeviceTaskManager;
import org.wso2.carbon.device.mgt.core.task.impl.DeviceTaskManagerImpl;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * This class implements all the functionality exposed as part of the OperationManager. Any transaction initiated
 * upon persisting information related to operation state, etc has to be managed, demarcated and terminated via the
 * methods available in OperationManagementDAOFactory.
 */
public class OperationManagerImpl implements OperationManager {

    private static final Log log = LogFactory.getLog(OperationManagerImpl.class);
    private static final int CACHE_VALIDITY_PERIOD = 5 * 60 * 1000;
    private static final String NOTIFIER_TYPE_LOCAL = "LOCAL";
    private static final String SYSTEM = "system";
    public static final int maxOperationCacheSize = 100;

    private final OperationDAO commandOperationDAO;
    private final OperationDAO configOperationDAO;
    private final OperationDAO profileOperationDAO;
    private final OperationDAO policyOperationDAO;
    private final OperationMappingDAO operationMappingDAO;
    private final OperationDAO operationDAO;
    private final DeviceDAO deviceDAO;
    private final EnrollmentDAO enrollmentDAO;
    private String deviceType;
    private DeviceManagementService deviceManagementService;
    private final Map<Integer, NotificationStrategy> notificationStrategies;
    private final Map<Integer, Long> lastUpdatedTimeStamps;
    private final ConcurrentMap<Integer, String> operationsInitBy;

    private final ThreadPoolExecutor notificationExecutor;

    public OperationManagerImpl() {
        commandOperationDAO = OperationManagementDAOFactory.getCommandOperationDAO();
        configOperationDAO = OperationManagementDAOFactory.getConfigOperationDAO();
        profileOperationDAO = OperationManagementDAOFactory.getProfileOperationDAO();
        policyOperationDAO = OperationManagementDAOFactory.getPolicyOperationDAO();
        operationMappingDAO = OperationManagementDAOFactory.getOperationMappingDAO();
        operationDAO = OperationManagementDAOFactory.getOperationDAO();
        deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        enrollmentDAO = DeviceManagementDAOFactory.getEnrollmentDAO();
        notificationStrategies = new HashMap<>();
        lastUpdatedTimeStamps = new HashMap<>();
        operationsInitBy = new ConcurrentHashMap<>();
        notificationExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    public OperationManagerImpl(String deviceType, DeviceManagementService deviceManagementService) {
        this();
        this.deviceType = deviceType;
        this.deviceManagementService = deviceManagementService;
    }

    public NotificationStrategy getNotificationStrategy() {
        // Notification strategy can be set by the platform configurations. Therefore it is needed to
        // get tenant specific notification strategy dynamically in the runtime. However since this is
        // a resource intensive retrieval, we are maintaining tenant aware local cache here to keep device
        // type specific notification strategy.
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(false);
        long lastUpdatedTimeStamp = 0;
        if (lastUpdatedTimeStamps.containsKey(tenantId)) {
            lastUpdatedTimeStamp = lastUpdatedTimeStamps.get(tenantId);
        }
        if (Calendar.getInstance().getTimeInMillis() - lastUpdatedTimeStamp > CACHE_VALIDITY_PERIOD) {
            PushNotificationConfig pushNoteConfig = deviceManagementService.getPushNotificationConfig();
            if (pushNoteConfig != null && !NOTIFIER_TYPE_LOCAL.equals(pushNoteConfig.getType())) {
                PushNotificationProvider provider = DeviceManagementDataHolder.getInstance()
                        .getPushNotificationProviderRepository().getProvider(pushNoteConfig.getType());
                if (provider == null) {
                    log.error("No registered push notification provider found for the type '" +
                            pushNoteConfig.getType() + "' under tenant ID '" + tenantId + "'.");
                    return null;
                }
                notificationStrategies.put(tenantId, provider.getNotificationStrategy(pushNoteConfig));
            } else {
                notificationStrategies.remove(tenantId);
            }
            lastUpdatedTimeStamps.put(tenantId, Calendar.getInstance().getTimeInMillis());
        }
        return notificationStrategies.get(tenantId);
    }

    @Override
    public Activity addOperation(Operation operation, List<DeviceIdentifier> deviceIds)
            throws OperationManagementException, InvalidDeviceException {
        if (log.isDebugEnabled()) {
            log.debug("operation:[" + operation.toString() + "]");
            for (DeviceIdentifier deviceIdentifier : deviceIds) {
                log.debug("device identifier id:[" + deviceIdentifier.getId() + "] type:[" + deviceIdentifier.getType()
                        + "]");
            }
        }

        DeviceIDHolder deviceValidationResult = DeviceManagerUtil.validateDeviceIdentifiers(deviceIds);
        List<DeviceIdentifier> validDeviceIds = deviceValidationResult.getValidDeviceIDList();
        if (!validDeviceIds.isEmpty()) {
            DeviceIDHolder deviceAuthorizationResult = this.authorizeDevices(operation, validDeviceIds);
            List<DeviceIdentifier> authorizedDeviceIds = deviceAuthorizationResult.getValidDeviceIDList();
            if (authorizedDeviceIds.isEmpty()) {
                log.warn("User : " + getUser() + " is not authorized to perform operations on given device-list.");
                Activity activity = new Activity();
                //Send the operation statuses only for admin triggered operations
                activity.setActivityStatus(this.getActivityStatus(deviceValidationResult, deviceAuthorizationResult));
                return activity;
            }

            boolean isScheduledOperation = this.isTaskScheduledOperation(operation);
            String initiatedBy = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            if (initiatedBy == null && (isScheduledOperation
                    || operation.getCode().equalsIgnoreCase(OperationMgtConstants.OperationCodes.EVENT_CONFIG)
                    || operation.getCode().equalsIgnoreCase(OperationMgtConstants.OperationCodes.EVENT_REVOKE))) {
                operation.setInitiatedBy(SYSTEM);
            } else if (StringUtils.isEmpty(operation.getInitiatedBy())) {
                operation.setInitiatedBy(initiatedBy);
            }
            if (log.isDebugEnabled()) {
                log.debug("initiatedBy : " + operation.getInitiatedBy());
            }

            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation operationDto = OperationDAOUtil
                    .convertOperation(operation);
            String operationCode = operationDto.getCode();
            Map<Integer, Device> enrolments = new HashMap<>();
            Device device;
            for (DeviceIdentifier deviceId : authorizedDeviceIds) {
                device = getDevice(deviceId);
                enrolments.put(device.getEnrolmentInfo().getId(), device);
            }

            try {
                OperationManagementDAOFactory.beginTransaction();
                if (operationDto.getControl()
                        == org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Control.NO_REPEAT) {
                    Map<Integer, Integer> pendingOperationIDs = operationDAO
                            .getExistingOperationIDs(enrolments.keySet().toArray(new Integer[0]), operationCode);
                    for (Integer enrolmentId : pendingOperationIDs.keySet()) {
                        operation.setId(pendingOperationIDs.get(enrolmentId));
                        device = enrolments.get(enrolmentId);
                        this.sendNotification(operation, device);
                        //No need to keep this enrollment as it has a pending operation
                        enrolments.remove(enrolmentId);
                    }
                    if (enrolments.size() == 0) {
                        //No operations to be add. All are repeated.
                        if (log.isDebugEnabled()) {
                            log.debug("All the devices contain a pending operation for the Operation Code: "
                                    + operationCode);
                        }
                        Activity activity = new Activity();
                        activity.setActivityId(DeviceManagementConstants.OperationAttributes.ACTIVITY +
                                operation.getId());
                        activity.setActivityStatus(
                                this.getActivityStatus(deviceValidationResult, deviceAuthorizationResult));
                        return activity;
                    }
                }

                persistsOperation(operation, operationDto, enrolments);

                Activity activity = new Activity();
                activity.setActivityId(DeviceManagementConstants.OperationAttributes.ACTIVITY + operation.getId());
                activity.setCode(operationCode);
                activity.setCreatedTimeStamp(new Date().toString());
                activity.setType(Activity.Type.valueOf(operationDto.getType().toString()));
                //For now set the operation statuses only for admin triggered operations
                if (!isScheduledOperation) {
                    activity.setActivityStatus(
                            this.getActivityStatus(deviceValidationResult, deviceAuthorizationResult));
                }
                return activity;
            } catch (OperationManagementDAOException e) {
                OperationManagementDAOFactory.rollbackTransaction();
                String msg = "Error occurred while adding operation";
                log.error(msg, e);
                throw new OperationManagementException(msg, e);
            } catch (TransactionManagementException e) {
                String msg = "Error occurred while initiating the transaction";
                log.error(msg, e);
                throw new OperationManagementException(msg, e);
            } finally {
                OperationManagementDAOFactory.closeConnection();
            }
        } else {
            throw new InvalidDeviceException("Invalid device Identifiers found.");
        }
    }

    @Override
    public void addTaskOperation(List<Device> devices, Operation operation) throws OperationManagementException {
        try {
            OperationManagementDAOFactory.beginTransaction();
            operation.setInitiatedBy(SYSTEM);
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation operationDto =
                    OperationDAOUtil.convertOperation(operation);
            String operationCode = operationDto.getCode();
            Map<Integer, Device> enrolments = new HashMap<>();
            for (Device device : devices) {
                enrolments.put(device.getEnrolmentInfo().getId(), device);
            }
            if (operationDto.getControl() ==
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Control.NO_REPEAT) {
                Map<Integer, Integer> pendingOperationIDs = operationDAO
                        .getExistingOperationIDs(enrolments.keySet().toArray(new Integer[0]), operationCode);
                Device device;
                for (Integer enrolmentId : pendingOperationIDs.keySet()) {
                    operation.setId(pendingOperationIDs.get(enrolmentId));
                    device = enrolments.get(enrolmentId);
                    this.sendNotification(operation, device);
                    //No need to keep this enrollment as it has a pending operation
                    enrolments.remove(enrolmentId);
                }
                if (enrolments.size() == 0) {
                    //No operations to be add. All are repeated.
                    return;
                }
            }
            persistsOperation(operation, operationDto, enrolments);
        } catch (OperationManagementDAOException e) {
            OperationManagementDAOFactory.rollbackTransaction();
            throw new OperationManagementException("Error occurred while adding task operation", e);
        } catch (TransactionManagementException e) {
            throw new OperationManagementException("Error occurred while initiating the transaction", e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void addTaskOperation(String deviceType, Operation operation, DynamicTaskContext dynamicTaskContext) throws OperationManagementException {
        List<String> validStatuses = Arrays.asList(EnrolmentInfo.Status.ACTIVE.toString(),
                EnrolmentInfo.Status.INACTIVE.toString(),
                EnrolmentInfo.Status.UNREACHABLE.toString());
        int batchSize = 2000;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            PaginationRequest paginationRequest;
            boolean hasRecords;
            int start = 0;
            OperationManagementDAOFactory.beginTransaction();
            DeviceManagementDAOFactory.beginTransaction();
            do {
                paginationRequest = new PaginationRequest(start, batchSize);
                paginationRequest.setStatusList(validStatuses);
                paginationRequest.setDeviceType(deviceType);
                List<Device> devices;

                if(dynamicTaskContext != null && dynamicTaskContext.isPartitioningEnabled()) {
                    devices = deviceDAO.getAllocatedDevices(paginationRequest, tenantId,
                                                                         dynamicTaskContext.getActiveServerCount(),
                                                            dynamicTaskContext.getServerHashIndex());
                } else {
                    devices = deviceDAO.getDevices(paginationRequest, tenantId);
                }

                if (devices.size() == batchSize) {
                    hasRecords = true;
                    start += batchSize;
                } else if (devices.size() == 0) {
                    break;
                } else {
                    hasRecords = false;
                }
                operation.setInitiatedBy(SYSTEM);
                org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation operationDto =
                        OperationDAOUtil.convertOperation(operation);
                String operationCode = operationDto.getCode();
                Map<Integer, Device> enrolments = new HashMap<>();
                for (Device device : devices) {
                    enrolments.put(device.getEnrolmentInfo().getId(), device);
                    if(log.isDebugEnabled()){
                        log.info("Adding operation for device Id : " + device.getDeviceIdentifier());
                    }
                }
                if (operationDto.getControl() ==
                        org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Control.NO_REPEAT) {
                    Map<Integer, Integer> pendingOperationIDs = operationDAO
                            .getExistingOperationIDs(enrolments.keySet().toArray(new Integer[0]), operationCode);
                    Device device;
                    for (Integer enrolmentId : pendingOperationIDs.keySet()) {
                        operation.setId(pendingOperationIDs.get(enrolmentId));
                        device = enrolments.get(enrolmentId);
                        this.sendNotification(operation, device);
                        //No need to keep this enrollment as it has a pending operation
                        enrolments.remove(enrolmentId);
                    }
                    if (enrolments.size() == 0) {
                        //No operations to be add. All are repeated.
                        break;
                    }
                }
                persistsOperation(operation, operationDto, enrolments);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignore) {
                    break;
                }
            } while (hasRecords);
        } catch (DeviceManagementDAOException e) {
            throw new OperationManagementException("Error occurred while getting devices to add operations", e);
        } catch (OperationManagementDAOException e) {
            OperationManagementDAOFactory.rollbackTransaction();
            throw new OperationManagementException("Error occurred while adding operation", e);
        } catch (TransactionManagementException e) {
            throw new OperationManagementException("Error occurred while initiating the transaction", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
            OperationManagementDAOFactory.closeConnection();
        }
    }

    private void persistsOperation(Operation operation,
                                   org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation operationDto,
                                   Map<Integer, Device> enrolments)
            throws OperationManagementDAOException, OperationManagementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        int operationId = this.lookupOperationDAO(operation).addOperation(operationDto);
        operationDto.setId(operationId);
        operation.setId(operationId);

        boolean isScheduled = false;
        NotificationStrategy notificationStrategy = getNotificationStrategy();

        // check whether device list is greater than batch size notification strategy has enable to send push
        // notification using scheduler task
        if (DeviceConfigurationManager.getInstance().getDeviceManagementConfig().
                getPushNotificationConfiguration().getSchedulerBatchSize() <= enrolments.size() &&
                notificationStrategy != null) {
            isScheduled = notificationStrategy.getConfig() != null && notificationStrategy.getConfig().isScheduled();
        }
        int failAttempts = 0;
        while (true) {
            try {
                operationMappingDAO.addOperationMapping(operationDto,
                        new ArrayList<>(enrolments.values()), isScheduled, tenantId);
                OperationManagementDAOFactory.commitTransaction();
                break;
            } catch (OperationManagementDAOException e) {
                OperationManagementDAOFactory.rollbackTransaction();
                if (++failAttempts > 3) {
                    String msg = "Error occurred while updating operation mapping. Operation ID: " +
                            operationId;
                    log.error(msg, e);
                    throw new OperationManagementException(msg, e);
                }
                log.warn("Unable to update operation status. Operation ID: " + operationId +
                        ", Attempt: " + failAttempts + ", Error: " + e.getMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignore) {
                    break;
                }
            }
        }
        if (!isScheduled && notificationStrategy != null) {
            for (Device device : enrolments.values()) {
                this.sendNotification(operation, device);
            }
        }
    }

    private void sendNotification(Operation operation, Device device) {
        NotificationStrategy notificationStrategy = getNotificationStrategy();
        /*
         * If notification strategy has not enable to send push notification using scheduler task we will send
         * notification immediately. This is done in separate loop inorder to prevent overlap with DB insert
         * operations with the possible db update operations trigger followed by pending operation call.
         * Otherwise device may call pending operation while DB is locked for write and deadlock can occur.
         */
        if (notificationStrategy != null) {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            notificationExecutor.execute(() -> {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
                if (log.isDebugEnabled()) {
                    log.debug("Sending push notification to " + device.getDeviceIdentifier() + " from add operation method.");
                }
                DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
                try {
                    notificationStrategy.execute(new NotificationContext(deviceIdentifier, operation));
                } catch (PushNotificationExecutionFailedException e) {
                    log.error("Error occurred while sending push notifications to " + device.getType() +
                            " device carrying id '" + device.getDeviceIdentifier() + "'", e);
                    /*
                     * Reschedule if push notification failed. Doing db transactions in atomic way to prevent
                     * deadlocks.
                     */
                    int failAttempts = 0;
                    while (true) {
                        try {
                            operationMappingDAO.updateOperationMapping(operation.getId(), device.getEnrolmentInfo().getId(),
                                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.PushNotificationStatus.SCHEDULED);
                            OperationManagementDAOFactory.commitTransaction();
                            break;
                        } catch (OperationManagementDAOException ex) {
                            OperationManagementDAOFactory.rollbackTransaction();
                            if (++failAttempts > 3) {
                                String msg = "Error occurred while setting push notification status to SCHEDULED. Operation ID: " +
                                        operation.getId() + ", Enrollment ID: " + device.getEnrolmentInfo().getId() +
                                        ", Device ID:" + device.getDeviceIdentifier();
                                log.error(msg, e);
                                break;
                            }
                            log.warn("Unable to set push notification status to SCHEDULED. Operation ID: " +
                                    operation.getId() + ", Enrollment ID: " + device.getEnrolmentInfo().getId() +
                                    ", Device ID:" + device.getDeviceIdentifier() + ", Attempt: " + failAttempts +
                                    ", Error: " + e.getMessage());
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException ignore) {
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error occurred while sending notifications to " + device.getType() +
                            " device carrying id '" + device.getDeviceIdentifier() + "'", e);
                }
                PrivilegedCarbonContext.endTenantFlow();
            });
        }
    }

    private List<ActivityStatus> getActivityStatus(DeviceIDHolder deviceIdValidationResult,
                                                   DeviceIDHolder deviceAuthResult) {
        List<ActivityStatus> activityStatuses = new ArrayList<>();
        ActivityStatus activityStatus;
        //Add the invalid DeviceIds
        for (DeviceIdentifier deviceIdentifier : deviceIdValidationResult.getErrorDeviceIdList()) {
            activityStatus = new ActivityStatus();
            activityStatus.setDeviceIdentifier(deviceIdentifier);
            activityStatus.setStatus(ActivityStatus.Status.INVALID);
            activityStatuses.add(activityStatus);
        }

        //Add the unauthorized DeviceIds
        for (DeviceIdentifier deviceIdentifier : deviceAuthResult.getErrorDeviceIdList()) {
            activityStatus = new ActivityStatus();
            activityStatus.setDeviceIdentifier(deviceIdentifier);
            activityStatus.setStatus(ActivityStatus.Status.UNAUTHORIZED);
            activityStatuses.add(activityStatus);
        }

        //Add the authorized DeviceIds
        for (DeviceIdentifier id : deviceAuthResult.getValidDeviceIDList()) {
            activityStatus = new ActivityStatus();
            activityStatus.setDeviceIdentifier(id);
            activityStatus.setStatus(ActivityStatus.Status.PENDING);
            activityStatuses.add(activityStatus);
        }
        return activityStatuses;
    }

    private DeviceIDHolder authorizeDevices(
            Operation operation, List<DeviceIdentifier> deviceIds) throws OperationManagementException {
        List<DeviceIdentifier> authorizedDeviceList;
        List<DeviceIdentifier> unAuthorizedDeviceList = new ArrayList<>();
        DeviceIDHolder deviceIDHolder = new DeviceIDHolder();
        try {
            if (operation != null && isAuthenticationSkippedOperation(operation)) {
                authorizedDeviceList = deviceIds;
            } else {
                boolean isAuthorized;
                authorizedDeviceList = new ArrayList<>();
                for (DeviceIdentifier devId : deviceIds) {
                    isAuthorized = DeviceManagementDataHolder.getInstance().getDeviceAccessAuthorizationService().
                            isUserAuthorized(devId);
                    if (isAuthorized) {
                        authorizedDeviceList.add(devId);
                    } else {
                        unAuthorizedDeviceList.add(devId);
                    }
                }
            }
        } catch (DeviceAccessAuthorizationException e) {
            throw new OperationManagementException("Error occurred while authorizing access to the devices for user :" +
                    this.getUser(), e);
        }
        deviceIDHolder.setValidDeviceIDList(authorizedDeviceList);
        deviceIDHolder.setErrorDeviceIdList(unAuthorizedDeviceList);
        return deviceIDHolder;
    }

    private Device getDevice(DeviceIdentifier deviceId) throws OperationManagementException {
        try {
            return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceId, false);
        } catch (DeviceManagementException e) {
            throw new OperationManagementException(
                    "Error occurred while retrieving device info.", e);
        }
    }

    @Override
    public List<? extends Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementException {
        List<Operation> operations;

        if (!isActionAuthorized(deviceId)) {
            throw new OperationManagementException("User '" + getUser() + "' is not authorized to access the '" +
                    deviceId.getType() + "' device, which carries the identifier '" +
                    deviceId.getId() + "'");
        }

        EnrolmentInfo enrolmentInfo = this.getActiveEnrolmentInfo(deviceId);
        if (enrolmentInfo == null) {
            return null;
        }

        try {
            OperationManagementDAOFactory.openConnection();
            List<? extends org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation> operationList =
                    operationDAO.getOperationsForDevice(enrolmentInfo.getId());

            operations = new ArrayList<>();
            for (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation : operationList) {
                Operation operation = OperationDAOUtil.convertOperation(dtoOperation);
                operations.add(operation);
            }
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "operations assigned for '" + deviceId.getType() +
                    "' device '" + deviceId.getId() + "'", e);
        } catch (SQLException e) {
            throw new OperationManagementException(
                    "Error occurred while opening a connection to the data source", e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
        return operations;
    }

    @Override
    public PaginationResult getOperations(DeviceIdentifier deviceId, PaginationRequest request)
            throws OperationManagementException {
        PaginationResult paginationResult;
        List<Operation> operations = new ArrayList<>();
        String owner = request.getOwner();
        try {
            if (!DeviceManagerUtil.isDeviceExists(deviceId)) {
                throw new OperationManagementException("Device not found for given device " +
                        "Identifier:" + deviceId.getId() + " and given type : " +
                        deviceId.getType());
            }
        } catch (DeviceManagementException e) {
            throw new OperationManagementException("Error while checking the existence of the device identifier - "
                    + deviceId.getId() + " of the device type - " + deviceId.getType(), e);
        }
        if (!isActionAuthorized(deviceId)) {
            throw new OperationManagementException("User '" + getUser() + "' is not authorized to access the '" +
                    deviceId.getType() + "' device, which carries the identifier '" +
                    deviceId.getId() + "' of owner '" + owner + "'");
        }
        EnrolmentInfo enrolmentInfo = this.getEnrolmentInfo(deviceId, request);
        if (enrolmentInfo == null){
            throw new OperationManagementException("Enrollment info not found for given device which has device "
                    + "Identifier:" + deviceId.getId() + " and device type: " + deviceId.getType() + "Further, device "
                    + "is own to: " + owner);
        }
        int enrolmentId = enrolmentInfo.getId();
        try {
            OperationManagementDAOFactory.openConnection();
            List<? extends org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation> operationList =
                    operationDAO.getOperationsForDevice(enrolmentId, request);
            for (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation : operationList) {
                Operation operation = OperationDAOUtil.convertOperation(dtoOperation);
                operations.add(operation);
            }
            paginationResult = new PaginationResult();
            int count = operationDAO.getOperationCountForDevice(enrolmentId, request);
            paginationResult.setData(operations);
            paginationResult.setRecordsTotal(count);
            paginationResult.setRecordsFiltered(count);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "operations assigned for '" + deviceId.getType() +
                    "' device '" + deviceId.getId() + "'", e);
        } catch (SQLException e) {
            throw new OperationManagementException(
                    "Error occurred while opening a connection to the data source", e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }

        return paginationResult;
    }

    @Override
    public List<? extends Operation> getOperations(DeviceIdentifier deviceId, Operation.Status status)
            throws OperationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Device identifier id:[" + deviceId.getId() + "] type:[" + deviceId.getType() + "]");
        }

        if (!isActionAuthorized(deviceId)) {
            throw new OperationManagementException("User '" + getUser() + "' is not authorized to access the '" +
                    deviceId.getType() + "' device, which carries the identifier '" +
                    deviceId.getId() + "'");
        }
        PaginationRequest request = new PaginationRequest(0, 0);
        request.setOwner(getUser());
        EnrolmentInfo enrolmentInfo = this.getEnrolmentInfo(deviceId, request);
        if (enrolmentInfo == null) {
            throw new OperationManagementException("Device not found for the given device Identifier:" +
                    deviceId.getId() + " and given type:" +
                    deviceId.getType());
        }
        int enrolmentId = enrolmentInfo.getId();
        return getOperations(deviceId, status, enrolmentId);
    }

    @Override
    public List<? extends Operation> getPendingOperations(DeviceIdentifier deviceId) throws
            OperationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Device identifier id:[" + deviceId.getId() + "] type:[" + deviceId.getType() + "]");
        }

        EnrolmentInfo enrolmentInfo = this.getActiveEnrolmentInfo(deviceId);
        if (enrolmentInfo == null) {
            throw new OperationManagementException("Device not found for the given device Identifier:" +
                    deviceId.getId() + " and given type:" +
                    deviceId.getType());
        }
        int enrolmentId = enrolmentInfo.getId();
        //Changing the enrollment status & attempt count if the device is marked as inactive or unreachable
        switch (enrolmentInfo.getStatus()) {
            case INACTIVE:
            case UNREACHABLE:
                this.setEnrolmentStatus(enrolmentId, EnrolmentInfo.Status.ACTIVE);
                int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
                DeviceCacheManagerImpl.getInstance().removeDeviceFromCache(deviceId, tenantId);
                break;
        }

        return getOperations(deviceId, Operation.Status.PENDING, enrolmentId);
    }

    @Override
    public List<? extends Operation> getPendingOperations(Device device) throws OperationManagementException {
        EnrolmentInfo enrolmentInfo = device.getEnrolmentInfo();
        if (enrolmentInfo == null) {
            throw new OperationManagementException("Device not found for the given device Identifier:" +
                    device.getId() + " and given type:" +
                    device.getType());
        }
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(device.getType());
        deviceIdentifier.setId(device.getDeviceIdentifier());
        int enrolmentId = enrolmentInfo.getId();
        //Changing the enrollment status & attempt count if the device is marked as inactive or unreachable
        switch (enrolmentInfo.getStatus()) {
            case INACTIVE:
            case UNREACHABLE:
                this.setEnrolmentStatus(enrolmentId, EnrolmentInfo.Status.ACTIVE);
                enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
                device.setEnrolmentInfo(enrolmentInfo);
                int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
                DeviceCacheManagerImpl.getInstance().addDeviceToCache(deviceIdentifier, device, tenantId);
                break;
        }
        return getOperations(deviceIdentifier, Operation.Status.PENDING, enrolmentId);
    }

    @Override
    public Operation getNextPendingOperation(DeviceIdentifier deviceId) throws OperationManagementException {
        // setting notNowOperationFrequency to -1 to avoid picking notnow operations
        return this.getNextPendingOperation(deviceId, -1);
    }

    @Override
    public Operation getNextPendingOperation(DeviceIdentifier deviceId, long notNowOperationFrequency)
            throws OperationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("device identifier id:[" + deviceId.getId() + "] type:[" + deviceId.getType() + "]");
        }
        Operation operation = null;

        if (!isActionAuthorized(deviceId)) {
            throw new OperationManagementException("User '" + getUser() + "' is not authorized to access the '" +
                    deviceId.getType() + "' device, which carries the identifier '" +
                    deviceId.getId() + "'");
        }

        EnrolmentInfo enrolmentInfo = this.getActiveEnrolmentInfo(deviceId);
        if (enrolmentInfo == null) {
            throw new OperationManagementException("Device not found for given device " +
                    "Identifier:" + deviceId.getId() + " and given type" +
                    deviceId.getType());
        }
        int enrolmentId = enrolmentInfo.getId();
        //Changing the enrollment status & attempt count if the device is marked as inactive or unreachable
        switch (enrolmentInfo.getStatus()) {
            case INACTIVE:
            case UNREACHABLE:
                this.setEnrolmentStatus(enrolmentId, EnrolmentInfo.Status.ACTIVE);
                int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
                DeviceCacheManagerImpl.getInstance().removeDeviceFromCache(deviceId, tenantId);
                break;
        }

        try {
            OperationManagementDAOFactory.openConnection();
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation = null;

            // check whether notnow is set
            if (notNowOperationFrequency > 0) {
                // retrieve Notnow operations
                dtoOperation = operationDAO.getNextOperation(enrolmentInfo.getId(),
                        org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status.NOTNOW);
            }

            if (dtoOperation != null) {
                long currentTime = Calendar.getInstance().getTime().getTime();
                log.info("Current timestamp:" + currentTime);
                long updatedTime = Timestamp.valueOf(dtoOperation.getReceivedTimeStamp()).getTime();
                log.info("Updated timestamp: " + updatedTime);

                // check if notnow frequency is met and set next pending operation if not, otherwise let notnow
                // operation to proceed
                if ((currentTime - updatedTime) < notNowOperationFrequency) {
                    dtoOperation = operationDAO.getNextOperation(enrolmentInfo.getId(),
                            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status.PENDING);
                }
            } else {
                dtoOperation = operationDAO.getNextOperation(enrolmentInfo.getId(),
                        org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status.PENDING);
            }

            if (dtoOperation != null) {
                if (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.COMMAND.equals(dtoOperation.getType()
                )) {
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.CommandOperation commandOperation;
                    commandOperation =
                            (org.wso2.carbon.device.mgt.core.dto.operation.mgt.CommandOperation) commandOperationDAO.
                                    getOperation(dtoOperation.getId());
                    dtoOperation.setEnabled(commandOperation.isEnabled());
                } else if (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.CONFIG.equals(dtoOperation.
                        getType())) {
                    dtoOperation = configOperationDAO.getOperation(dtoOperation.getId());
                } else if (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.PROFILE.equals(dtoOperation.
                        getType())) {
                    dtoOperation = profileOperationDAO.getOperation(dtoOperation.getId());
                } else if (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.POLICY.equals(dtoOperation.
                        getType())) {
                    dtoOperation = policyOperationDAO.getOperation(dtoOperation.getId());
                }
                operation = OperationDAOUtil.convertOperation(dtoOperation);
            }
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving next pending operation", e);
        } catch (SQLException e) {
            throw new OperationManagementException(
                    "Error occurred while opening a connection to the data source", e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
        return operation;
    }

    @Override
    public void updateOperation(DeviceIdentifier deviceId, Operation operation) throws OperationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("operation Id:" + operation.getId() + " status:" + operation.getStatus());
        }

        EnrolmentInfo enrolmentInfo = this.getActiveEnrolmentInfo(deviceId);
        if (enrolmentInfo == null) {
            throw new OperationManagementException(
                    "Device not found for device id:" + deviceId.getId() + " " + "type:" +
                            deviceId.getType());
        }
        updateOperation(enrolmentInfo.getId(), operation, deviceId);
    }

    @Override
    public void updateOperation(int enrolmentId, Operation operation, DeviceIdentifier deviceId)
            throws OperationManagementException {
        int operationId = operation.getId();
        boolean isOperationUpdated = false;
        try {
            OperationManagementDAOFactory.beginTransaction();
            if (operation.getStatus() != null) {
                int failAttempts = 0;
                while (true) {
                    try {
                        isOperationUpdated = operationDAO.updateOperationStatus(enrolmentId, operationId,
                                org.wso2.carbon.device.mgt.core.dto.operation.mgt.
                                        Operation.Status.valueOf(operation.getStatus().
                                        toString()));
                        OperationManagementDAOFactory.commitTransaction();
                        break;
                    } catch (OperationManagementDAOException e) {
                        OperationManagementDAOFactory.rollbackTransaction();
                        if (++failAttempts > 3) {
                            String msg = "Error occurred while updating operation status. Operation ID: " +
                                    operationId + ", Enrollment ID: " + enrolmentId + ", Device ID:" + deviceId;
                            log.error(msg, e);
                            throw new OperationManagementException(msg, e);
                        }
                        log.warn("Unable to update operation status. Operation ID: " + operationId +
                                ", Enrollment ID: " + enrolmentId + ", Device ID:" + deviceId + ", Attempt: " + failAttempts +
                                ", Error: " + e.getMessage());
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ignore) {
                            break;
                        }
                    }
                }
            }
            if (!isOperationUpdated) {
                log.warn("Operation " + operationId + "'s status is not updated");
            }
            if (isOperationUpdated && operation.getOperationResponse() != null) {
                OperationMonitoringTaskConfig operationMonitoringTaskConfig = DeviceManagementDataHolder
                        .getInstance().getDeviceManagementProvider().getDeviceMonitoringConfig(deviceId.getType());
                List<MonitoringOperation> monitoringOperations = operationMonitoringTaskConfig.getMonitoringOperation();
                MonitoringOperation currentMonitoringOperation = null;
                for (MonitoringOperation monitoringOperation : monitoringOperations) {
                    if (monitoringOperation.getTaskName().equals(operation.getCode())) {
                        currentMonitoringOperation = monitoringOperation;
                        break;
                    }
                }
                if (currentMonitoringOperation != null && !currentMonitoringOperation.hasResponsePersistence()) {
                    String initiatedBy = operationsInitBy.get(operationId);
                    if (initiatedBy == null) {
                        try {
                            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation operationDto =
                                    operationDAO.getOperation(operationId);
                            operation.setInitiatedBy(operationDto.getInitiatedBy());
                            if (operationsInitBy.size() > maxOperationCacheSize) {
                                Integer obsoleteOperationId = (Integer) operationsInitBy.keySet().toArray()[0];
                                operationsInitBy.remove(obsoleteOperationId);
                            }
                            operationsInitBy.put(operationId, operation.getInitiatedBy());
                        } catch (OperationManagementDAOException e) {
                            log.warn("Unable to get operationDTO for Operation ID: " + operationId +
                                    ", Error: " + e.getErrorMessage());
                        }
                    } else {
                        operation.setInitiatedBy(initiatedBy);
                    }
                    if (SYSTEM.equals(operation.getInitiatedBy())) {
                        return;
                    }
                }
            }
            OperationResponseMeta responseMeta = null;
            if (isOperationUpdated && operation.getOperationResponse() != null) {
                int failAttempts = 0;
                while (true) {
                    try {
                        responseMeta = operationDAO.addOperationResponse(enrolmentId, operation, deviceId.getId());
                        OperationManagementDAOFactory.commitTransaction();
                        break;
                    } catch (OperationManagementDAOException e) {
                        OperationManagementDAOFactory.rollbackTransaction();
                        if (++failAttempts > 3) {
                            String msg = "Error occurred while updating operation response. Operation ID: " +
                                    operationId + ", Enrollment ID: " + enrolmentId + ", Device ID:" + deviceId;
                            log.error(msg, e);
                            throw new OperationManagementException(msg, e);
                        }
                        log.warn("Unable to update operation response. Operation ID: " + operationId +
                                ", Enrollment ID: " + enrolmentId + ", Device ID:" + deviceId + " Attempt: " + failAttempts +
                                ", Error: " + e.getErrorMessage());
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ignore) {
                            break;
                        }
                    }
                }
            }
            if (responseMeta != null && responseMeta.isLargeResponse() && responseMeta.getId() > 0) {
                int failAttempts = 0;
                while (true) {
                    try {
                        operationDAO.addOperationResponseLarge(responseMeta, operation, deviceId.getId());
                        OperationManagementDAOFactory.commitTransaction();
                        break;
                    } catch (OperationManagementDAOException e) {
                        OperationManagementDAOFactory.rollbackTransaction();
                        if (++failAttempts > 3) {
                            String msg = "Error occurred while updating large operation response. " +
                                    "Enrollment Mapping ID: " + responseMeta.getOperationMappingId() +
                                    ", Response ID: " + responseMeta.getId() + ", Operation ID: " + operationId +
                                    ", Enrollment ID: " + enrolmentId + ", Device ID:" + deviceId;
                            log.error(msg, e);
                            throw new OperationManagementException(msg, e);
                        }
                        log.warn("Unable to update large operation response. " +
                                "Enrollment Mapping ID: " + responseMeta.getOperationMappingId() +
                                ", Response ID: " + responseMeta.getId() + ", Operation ID: " + operationId +
                                ", Enrollment ID: " + enrolmentId + ", Device ID:" + deviceId +
                                ", Attempt: " + failAttempts + ", Error: " + e.getMessage());
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ignore) {
                            break;
                        }
                    }
                }
            }
        } catch (TransactionManagementException e) {
            throw new OperationManagementException("Error occurred while initiating a transaction", e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public Operation getOperationByDeviceAndOperationId(DeviceIdentifier deviceId, int operationId)
            throws OperationManagementException {
        Operation operation;
        if (log.isDebugEnabled()) {
            log.debug("Operation Id: " + operationId + " Device Type: " + deviceId.getType() + " Device Identifier: " +
                    deviceId.getId());
        }

        if (!isActionAuthorized(deviceId)) {
            throw new OperationManagementException("User '" + getUser() + "' is not authorized to access the '" +
                    deviceId.getType() + "' device, which carries the identifier '" +
                    deviceId.getId() + "'");
        }

        EnrolmentInfo enrolmentInfo = this.getActiveEnrolmentInfo(deviceId);
        if (enrolmentInfo == null) {
            throw new OperationManagementException("Device not found for given device identifier: " +
                    deviceId.getId() + " type: " + deviceId.getType());
        }

        try {
            OperationManagementDAOFactory.openConnection();
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation deviceSpecificOperation = operationDAO.
                    getOperationByDeviceAndId(enrolmentInfo.getId(),
                            operationId);
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation = deviceSpecificOperation;
            if (deviceSpecificOperation.getType().
                    equals(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.COMMAND)) {
                org.wso2.carbon.device.mgt.core.dto.operation.mgt.CommandOperation commandOperation;
                commandOperation =
                        (org.wso2.carbon.device.mgt.core.dto.operation.mgt.CommandOperation) commandOperationDAO.
                                getOperation(deviceSpecificOperation.getId());
                dtoOperation.setEnabled(commandOperation.isEnabled());
            } else if (deviceSpecificOperation.getType().
                    equals(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.CONFIG)) {
                dtoOperation = configOperationDAO.getOperation(deviceSpecificOperation.getId());
            } else if (deviceSpecificOperation.getType().equals(
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.PROFILE)) {
                dtoOperation = profileOperationDAO.getOperation(deviceSpecificOperation.getId());
            } else if (deviceSpecificOperation.getType().equals(
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.POLICY)) {
                dtoOperation = policyOperationDAO.getOperation(deviceSpecificOperation.getId());
            }
            if (dtoOperation == null) {
                throw new OperationManagementException("Operation not found for operation Id:" + operationId +
                        " device id:" + deviceId.getId());
            }
            dtoOperation.setStatus(deviceSpecificOperation.getStatus());
            operation = OperationDAOUtil.convertOperation(deviceSpecificOperation);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "operations assigned for '" + deviceId.getType() +
                    "' device '" + deviceId.getId() + "'", e);
        } catch (SQLException e) {
            throw new OperationManagementException("Error occurred while opening connection to the data source",
                    e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }

        return operation;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(
            DeviceIdentifier deviceId, Operation.Status status) throws OperationManagementException {
        List<Operation> operations = new ArrayList<>();
        List<org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation> dtoOperationList = new ArrayList<>();

        if (!isActionAuthorized(deviceId)) {
            throw new OperationManagementException("User '" + getUser() + "' is not authorized to access the '" +
                    deviceId.getType() + "' device, which carries the identifier '" +
                    deviceId.getId() + "'");
        }

        EnrolmentInfo enrolmentInfo = this.getActiveEnrolmentInfo(deviceId);
        if (enrolmentInfo == null) {
            throw new OperationManagementException(
                    "Device not found for device id:" + deviceId.getId() + " " + "type:" +
                            deviceId.getType());
        }

        try {
            int enrolmentId = enrolmentInfo.getId();
            OperationManagementDAOFactory.openConnection();
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status dtoOpStatus =
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status.valueOf(status.toString());
            dtoOperationList.addAll(commandOperationDAO.getOperationsByDeviceAndStatus(enrolmentId, dtoOpStatus));
            dtoOperationList.addAll(configOperationDAO.getOperationsByDeviceAndStatus(enrolmentId,
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.
                            Status.PENDING));
            dtoOperationList.addAll(profileOperationDAO.getOperationsByDeviceAndStatus(enrolmentId,
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.
                            Status.PENDING));
            dtoOperationList.addAll(policyOperationDAO.getOperationsByDeviceAndStatus(enrolmentId,
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.
                            Status.PENDING));

            Operation operation;

            for (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation : dtoOperationList) {
                operation = OperationDAOUtil.convertOperation(dtoOperation);
                operations.add(operation);
            }

        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "operations assigned for '" + deviceId.getType() +
                    "' device '" +
                    deviceId.getId() + "' and status:" + status.toString(), e);
        } catch (SQLException e) {
            throw new OperationManagementException(
                    "Error occurred while opening a connection to the data source", e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
        return operations;
    }

    @Override
    public Operation getOperation(int operationId) throws OperationManagementException {
        Operation operation;
        try {
            OperationManagementDAOFactory.openConnection();
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation = operationDAO.getOperation(
                    operationId);
            if (dtoOperation == null) {
                throw new OperationManagementException("Operation not found for given Id:" + operationId);
            }

            if (dtoOperation.getType()
                    .equals(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.COMMAND)) {
                org.wso2.carbon.device.mgt.core.dto.operation.mgt.CommandOperation commandOperation;
                commandOperation =
                        (org.wso2.carbon.device.mgt.core.dto.operation.mgt.CommandOperation) commandOperationDAO.
                                getOperation(dtoOperation.getId());
                dtoOperation.setEnabled(commandOperation.isEnabled());
            } else if (dtoOperation.getType().
                    equals(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.CONFIG)) {
                dtoOperation = configOperationDAO.getOperation(dtoOperation.getId());
            } else if (dtoOperation.getType().equals(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.
                    PROFILE)) {
                dtoOperation = profileOperationDAO.getOperation(dtoOperation.getId());
            } else if (dtoOperation.getType().equals(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.
                    POLICY)) {
                dtoOperation = policyOperationDAO.getOperation(dtoOperation.getId());
            }
            operation = OperationDAOUtil.convertOperation(dtoOperation);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the operation with operation Id '" +
                    operationId, e);
        } catch (SQLException e) {
            throw new OperationManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
        return operation;
    }

    @Override
    public Activity getOperationByActivityId(String activity) throws OperationManagementException {
        // This parses the operation id from activity id (ex : ACTIVITY_23) and converts to the integer.
        int operationId = Integer.parseInt(
                activity.replace(DeviceManagementConstants.OperationAttributes.ACTIVITY, ""));
        if (operationId == 0) {
            throw new IllegalArgumentException("Operation ID cannot be null or zero (0).");
        }
        try {
            OperationManagementDAOFactory.openConnection();
            return operationDAO.getActivity(operationId);
        } catch (SQLException e) {
            throw new OperationManagementException("Error occurred while opening a connection to the data source.", e);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the operation with activity Id '" +
                    activity, e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Activity> getOperationByActivityIds(List<String> activities)
            throws OperationManagementException {
        List<Integer> operationIds = new ArrayList<>();
        for (String id : activities) {
            int operationId = Integer.parseInt(
                    id.replace(DeviceManagementConstants.OperationAttributes.ACTIVITY, ""));
            if (operationId == 0) {
                throw new IllegalArgumentException("Operation ID cannot be null or zero (0).");
            } else {
                operationIds.add(operationId);
            }
        }

        try {
            OperationManagementDAOFactory.openConnection();
            return operationDAO.getActivityList(operationIds);
        } catch (SQLException e) {
            throw new OperationManagementException(
                    "Error occurred while opening a connection to the data source.", e);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException(
                    "Error occurred while retrieving the operation with activity Id '" + activities
                            .toString(), e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }

    public Activity getOperationByActivityIdAndDevice(String activity, DeviceIdentifier deviceId)
            throws OperationManagementException {
        // This parses the operation id from activity id (ex : ACTIVITY_23) and converts to the integer.
        int operationId = Integer.parseInt(
                activity.replace(DeviceManagementConstants.OperationAttributes.ACTIVITY, ""));
        if (operationId == 0) {
            throw new IllegalArgumentException("Operation ID cannot be null or zero (0).");
        }
        if (!isActionAuthorized(deviceId)) {
            throw new OperationManagementException("User '" + getUser() + "' is not authorized to access the '" +
                    deviceId.getType() + "' device, which carries the identifier '" +
                    deviceId.getId() + "'");
        }
        Device device = this.getDevice(deviceId);
        try {
            OperationManagementDAOFactory.openConnection();
            return operationDAO.getActivityByDevice(operationId, device.getId());
        } catch (SQLException e) {
            throw new OperationManagementException("Error occurred while opening a connection to the data source.", e);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the operation with activity Id '" +
                    activity + " and device Id: " + deviceId.getId(), e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfter(long timestamp, int limit,
                                                    int offset) throws OperationManagementException {
        try {
            OperationManagementDAOFactory.openConnection();
            return operationDAO.getActivitiesUpdatedAfter(timestamp, limit, offset);
        } catch (SQLException e) {
            throw new OperationManagementException("Error occurred while opening a connection to the data source.", e);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while getting the activity list changed after a " +
                    "given time.", e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Activity> getActivities(ActivityPaginationRequest activityPaginationRequest)
            throws OperationManagementException {
        try {
            OperationManagementDAOFactory.openConnection();
            return operationDAO.getActivities(activityPaginationRequest);
        } catch (SQLException e) {
            throw new OperationManagementException("Error occurred while opening a connection to the data source.", e);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while getting the activity list.", e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getActivitiesCount(ActivityPaginationRequest activityPaginationRequest)
            throws OperationManagementException {
        try {
            OperationManagementDAOFactory.openConnection();
            return operationDAO.getActivitiesCount(activityPaginationRequest);
        } catch (SQLException e) {
            throw new OperationManagementException("Error occurred while opening a connection to the data source.", e);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while getting the activity count.", e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Activity> getFilteredActivities(String operationCode, int limit, int offset) throws OperationManagementException {
        try {
            OperationManagementDAOFactory.openConnection();
            return operationDAO.getFilteredActivities(operationCode, limit, offset);
        } catch (SQLException e) {
            throw new OperationManagementException("Error occurred while opening a connection to the data source.", e);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while getting the activity list for the given "
                    + "given operationCode: " + operationCode, e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getTotalCountOfFilteredActivities(String operationCode) throws OperationManagementException {
        try {
            OperationManagementDAOFactory.openConnection();
            return operationDAO.getTotalCountOfFilteredActivities(operationCode);
        } catch (SQLException e) {
            throw new OperationManagementException("Error occurred while opening a connection to the data source.", e);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while getting the activity count for the given "
                    + "operation code:" + operationCode, e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfterByUser(long timestamp, String user, int limit, int offset)
            throws OperationManagementException {
        try {
            OperationManagementDAOFactory.openConnection();
            return operationDAO.getActivitiesUpdatedAfterByUser(timestamp, user, limit, offset);
        } catch (SQLException e) {
            throw new OperationManagementException("Error occurred while opening a connection to the data source.", e);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while getting the activity list changed after a " +
                    "given time which are added by user : " + user, e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getActivityCountUpdatedAfter(long timestamp) throws OperationManagementException {
        try {
            OperationManagementDAOFactory.openConnection();
            return operationDAO.getActivityCountUpdatedAfter(timestamp);
        } catch (SQLException e) {
            throw new OperationManagementException("Error occurred while opening a connection to the data source.", e);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while getting the activity count changed after a " +
                    "given time.", e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getActivityCountUpdatedAfterByUser(long timestamp, String user) throws OperationManagementException {
        try {
            OperationManagementDAOFactory.openConnection();
            return operationDAO.getActivityCountUpdatedAfterByUser(timestamp, user);
        } catch (SQLException e) {
            throw new OperationManagementException("Error occurred while opening a connection to the data source.", e);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while getting the activity count changed after a " +
                    "given time which are added by user :" + user, e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }

    private OperationDAO lookupOperationDAO(Operation operation) {

        if (operation instanceof CommandOperation) {
            return commandOperationDAO;
        } else if (operation instanceof ProfileOperation) {
            return profileOperationDAO;
        } else if (operation instanceof ConfigOperation) {
            return configOperationDAO;
        } else if (operation instanceof PolicyOperation) {
            return policyOperationDAO;
        } else {
            return operationDAO;
        }
    }

    private String getUser() {
        return CarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    private boolean isAuthenticationSkippedOperation(Operation operation) {

        //This is to check weather operations are coming from the task related to retrieving device information.
        DeviceTaskManager taskManager = new DeviceTaskManagerImpl(deviceType);
        if (taskManager.isTaskOperation(operation.getCode())) {
            return true;
        }

        boolean status;
        switch (operation.getCode()) {
            case DeviceManagementConstants.AuthorizationSkippedOperationCodes.POLICY_OPERATION_CODE:
            case DeviceManagementConstants.AuthorizationSkippedOperationCodes.EVENT_CONFIG_OPERATION_CODE:
            case DeviceManagementConstants.AuthorizationSkippedOperationCodes.EVENT_REVOKE_OPERATION_CODE:
            case DeviceManagementConstants.AuthorizationSkippedOperationCodes.POLICY_REVOKE_OPERATION_CODE:
            case DeviceManagementConstants.AuthorizationSkippedOperationCodes.MONITOR_OPERATION_CODE:
                status = true;
                break;
            default:
                status = false;
        }

        return status;
    }

    private boolean isActionAuthorized(DeviceIdentifier deviceId) {
        boolean isUserAuthorized;
        try {
            isUserAuthorized = DeviceManagementDataHolder.getInstance().getDeviceAccessAuthorizationService().
                    isUserAuthorized(deviceId, DeviceGroupConstants.Permissions.DEFAULT_OPERATOR_PERMISSIONS);
        } catch (DeviceAccessAuthorizationException e) {
            log.error("Error occurred while trying to authorize current user upon the invoked operation", e);
            return false;
        }
        return isUserAuthorized;
    }

    private EnrolmentInfo getEnrolmentInfo(DeviceIdentifier deviceId, PaginationRequest request)
            throws OperationManagementException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String user = this.getUser();
        boolean isUserAuthorized;
        try {
            isUserAuthorized = DeviceManagementDataHolder.getInstance()
                    .getDeviceAccessAuthorizationService().isUserAuthorized(deviceId, user);
        } catch (DeviceAccessAuthorizationException e) {
            throw new OperationManagementException("Error occurred while checking the device access permissions for '" +
                    deviceId.getType() + "' device carrying the identifier '" +
                    deviceId.getId() + "' of owner '" + request.getOwner() + "'", e);

        }
        if (!isUserAuthorized) {
            return null;
        }
        EnrolmentInfo enrolmentInfo;
        try {
            DeviceManagementDAOFactory.openConnection();
            enrolmentInfo = deviceDAO.getEnrolment(deviceId, request, tenantId);
        } catch (DeviceManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving enrollment data of '" +
                    deviceId.getType() + "' device carrying the identifier '" +
                    deviceId.getId() + "' of owner '" + request.getOwner() + "'", e);
        } catch (SQLException e) {
            throw new OperationManagementException(
                    "Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return enrolmentInfo;
    }

    private EnrolmentInfo getActiveEnrolmentInfo(DeviceIdentifier deviceId) throws OperationManagementException {
        EnrolmentInfo enrolmentInfo;
        try {
            DeviceManagementDAOFactory.openConnection();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            enrolmentInfo = deviceDAO.getActiveEnrolment(deviceId, tenantId);
        } catch (DeviceManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving enrollment data of '" +
                    deviceId.getType() + "' device carrying the identifier '" +
                    deviceId.getId() + "'", e);
        } catch (SQLException e) {
            throw new OperationManagementException(
                    "Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return enrolmentInfo;
    }

    private void setEnrolmentStatus(int enrolmentId, EnrolmentInfo.Status status) throws OperationManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            int failAttempts = 0;
            while (true) {
                try {
                    enrollmentDAO.setStatus(enrolmentId, status, tenantId);
                    DeviceManagementDAOFactory.commitTransaction();
                    break;
                } catch (DeviceManagementDAOException e) {
                    OperationManagementDAOFactory.rollbackTransaction();
                    if (++failAttempts > 3) {
                        String msg = "Error occurred while updating enrollment status of device of " +
                                "enrolment-id '" + enrolmentId + "'";
                        log.error(msg, e);
                        throw new OperationManagementException(msg, e);
                    }
                    log.warn("Unable to update enrollment status of device of enrolment-id '" +
                            enrolmentId + ", Attempt: " + failAttempts + ", Error: " + e.getMessage());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignore) {
                        break;
                    }
                }
            }
        } catch (TransactionManagementException e) {
            throw new OperationManagementException("Error occurred while initiating a transaction", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    private boolean isTaskScheduledOperation(Operation operation) {
        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder.getInstance().
                getDeviceManagementProvider();
        List<MonitoringOperation> monitoringOperations = deviceManagementProviderService.getMonitoringOperationList(deviceType);//Get task list from each device type
        for (MonitoringOperation op : monitoringOperations) {
            if (operation.getCode().equals(op.getTaskName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isSameUser(String user, String owner) {
        return user.equalsIgnoreCase(owner);
    }

    private List<? extends Operation> getOperations(DeviceIdentifier deviceId, Operation.Status status, int enrolmentId)
            throws OperationManagementException {
        List<Operation> operations = new ArrayList<>();
        List<org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation> dtoOperationList = new ArrayList<>();

        org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status internalStatus =
                org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status.valueOf(status.name());

        try {
            OperationManagementDAOFactory.openConnection();
            dtoOperationList.addAll(commandOperationDAO.getOperationsByDeviceAndStatus(
                    enrolmentId, internalStatus));
            dtoOperationList.addAll(configOperationDAO.getOperationsByDeviceAndStatus(
                    enrolmentId, internalStatus));
            dtoOperationList.addAll(profileOperationDAO.getOperationsByDeviceAndStatus(
                    enrolmentId, internalStatus));
            dtoOperationList.addAll(policyOperationDAO.getOperationsByDeviceAndStatus(
                    enrolmentId, internalStatus));
            Operation operation;
            for (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation : dtoOperationList) {
                operation = OperationDAOUtil.convertOperation(dtoOperation);
                operations.add(operation);
            }
            operations.sort(new OperationIdComparator());
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "pending operations assigned for '" + deviceId.getType() +
                    "' device '" + deviceId.getId() + "'", e);
        } catch (SQLException e) {
            throw new OperationManagementException(
                    "Error occurred while opening a connection to the data source", e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
        return operations;
    }

    @Override
    public boolean isOperationExist(DeviceIdentifier deviceId, int operationId)
            throws OperationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Operation Id: " + operationId + " Device Type: " + deviceId.getType() + " Device Identifier: " +
                    deviceId.getId());
        }
        if (!isActionAuthorized(deviceId)) {
            String msg = "User '" + getUser() + "' is not authorized to access the '" +
                    deviceId.getType() + "' device, which carries the identifier '" +
                    deviceId.getId() + "'";
            log.error(msg);
            throw new OperationManagementException(msg);
        }
        EnrolmentInfo enrolmentInfo = this.getActiveEnrolmentInfo(deviceId);
        if (enrolmentInfo == null) {
            String msg = "Device not found for given device identifier: " +
                    deviceId.getId() + " type: " + deviceId.getType();
            log.error(msg);
            throw new OperationManagementException(msg);
        }

        try {
            OperationManagementDAOFactory.openConnection();
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation deviceSpecificOperation = operationDAO.
                    getOperationByDeviceAndId(enrolmentInfo.getId(),
                            operationId);
            return deviceSpecificOperation != null;
        } catch (OperationManagementDAOException e) {
            String msg = "Error occurred while checking if operation with operation id "
                    + operationId +" exist for " + deviceId.getType() + "' device '" + deviceId.getId() + "'";
            log.error(msg, e);
            throw new OperationManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening connection to the data source";
            log.error(msg, e);
            throw new OperationManagementException(msg,
                    e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Activity> getActivities(List<String> deviceTypes, String operationCode, long updatedSince, String operationStatus)
            throws OperationManagementException {
        try {
            OperationManagementDAOFactory.openConnection();
            return operationDAO.getActivities(deviceTypes, operationCode, updatedSince, operationStatus);
        } catch (SQLException e) {
            throw new OperationManagementException("Error occurred while opening a connection to the data source.", e);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while getting the activity list.", e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }
}
