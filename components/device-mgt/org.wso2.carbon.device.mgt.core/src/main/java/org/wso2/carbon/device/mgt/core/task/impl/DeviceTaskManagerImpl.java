/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.device.mgt.core.task.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.MonitoringOperation;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.StartupOperationConfig;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.task.DeviceMgtTaskException;
import org.wso2.carbon.device.mgt.core.task.DeviceTaskManager;
import org.wso2.carbon.device.mgt.core.task.Utils;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.util.*;

public class DeviceTaskManagerImpl implements DeviceTaskManager {

    private static Log log = LogFactory.getLog(DeviceTaskManagerImpl.class);
    private String deviceType;
    static volatile Map<Integer, Map<String, Map<String, Long>>> map = new HashMap<>();
    private static volatile Map<Integer, List<String>> startupConfigMap = new HashMap<>();
    private OperationMonitoringTaskConfig operationMonitoringTaskConfig;
    private StartupOperationConfig startupOperationConfig;

    public DeviceTaskManagerImpl(String deviceType,
                                 OperationMonitoringTaskConfig operationMonitoringTaskConfig,
                                 StartupOperationConfig startupOperationConfig) {
        this.deviceType = deviceType;
        this.operationMonitoringTaskConfig = operationMonitoringTaskConfig;
        this.startupOperationConfig = startupOperationConfig;
    }

    public DeviceTaskManagerImpl(String deviceType,
                                 OperationMonitoringTaskConfig operationMonitoringTaskConfig) {
        this.operationMonitoringTaskConfig = operationMonitoringTaskConfig;
        this.deviceType = deviceType;
    }

    public DeviceTaskManagerImpl(String deviceType) {
        this.deviceType = deviceType;
    }

    //get device type specific operations
    private List<MonitoringOperation> getOperationList() throws DeviceMgtTaskException {
        return operationMonitoringTaskConfig.getMonitoringOperation();
    }

    private List<String> getStartupOperations() {
        if (startupOperationConfig != null) {
            return startupOperationConfig.getStartupOperations();
        }
        return null;
    }

    @Override
    public int getTaskFrequency() throws DeviceMgtTaskException {
        return operationMonitoringTaskConfig.getFrequency();
    }

//    @Override
//    public String getTaskImplementedClazz() throws DeviceMgtTaskException {
//        return DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getTaskConfiguration().
//                getTaskClazz();
//    }

    @Override
    public boolean isTaskEnabled() throws DeviceMgtTaskException {
        return operationMonitoringTaskConfig.isEnabled();
    }


    @Override
    public void addOperations() throws DeviceMgtTaskException {
        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder.getInstance().
                getDeviceManagementProvider();
        try {
            //list operations for device type
            List<String> operations = this.getValidOperationNames();
            if (operations.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("No operations are available.");
                }
                return;
            }
            List<DeviceIdentifier> validDeviceIdentifiers;
            List<String> startupOperations;
            //list devices of device type
            List<Device> devices = deviceManagementProviderService.getAllDevices(deviceType, false);

            if (!devices.isEmpty()) {
                if (log.isDebugEnabled() && deviceType != null) {
                    log.info("Devices exist to add operations and the total number of devices are " + devices.size());
                }
                validDeviceIdentifiers = DeviceManagerUtil.getValidDeviceIdentifiers(devices);
                if (!validDeviceIdentifiers.isEmpty()) {
                    if (log.isDebugEnabled() && deviceType != null) {
                        log.debug("Number of valid device identifier size to add operations: " + validDeviceIdentifiers
                                .size());
                    }
                    for (String str : operations) {
                        CommandOperation operation = new CommandOperation();
                        operation.setEnabled(true);
                        operation.setType(Operation.Type.COMMAND);
                        operation.setCode(str);
                        deviceManagementProviderService.addOperation(deviceType, operation, validDeviceIdentifiers);
                    }
                    startupOperations = getStartupOperations();
                    if (startupOperations != null && !startupOperations.isEmpty()) {
                        addStartupOperations(startupOperations, validDeviceIdentifiers,
                                             deviceManagementProviderService);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("No valid devices are available.");
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No devices are available to perform the operations.");
                }
            }
        } catch (InvalidDeviceException e) {
            throw new DeviceMgtTaskException("Invalid DeviceIdentifiers found.", e);
        } catch (DeviceManagementException e) {
            throw new DeviceMgtTaskException("Error occurred while retrieving the device list.", e);
        } catch (OperationManagementException e) {
            throw new DeviceMgtTaskException("Error occurred while adding the operations to devices", e);
        }
    }

    private List<String> getValidOperationNames() throws DeviceMgtTaskException {

        List<MonitoringOperation> monitoringOperations = this.getOperationList();
        List<String> opNames = new ArrayList<>();
        Long milliseconds = System.currentTimeMillis();
        int frequency = this.getTaskFrequency();
        Map<String, Long> mp = Utils.getTenantedTaskOperationMap(map, deviceType);

        for (MonitoringOperation top : monitoringOperations) {
            if (!mp.containsKey(top.getTaskName())) {
                opNames.add(top.getTaskName());
                mp.put(top.getTaskName(), milliseconds);
            } else {
                Long lastExecutedTime = mp.get(top.getTaskName());
                Long evalTime = lastExecutedTime + (frequency * top.getRecurrentTimes());
                if (evalTime <= milliseconds) {
                    opNames.add(top.getTaskName());
                    mp.put(top.getTaskName(), milliseconds);
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Valid operation names are : " + Arrays.toString(opNames.toArray()));
        }
        return opNames;
    }

    private void addStartupOperations(List<String> startupOperations, List<DeviceIdentifier> validDeviceIdentifiers
            , DeviceManagementProviderService deviceManagementProviderService) throws DeviceMgtTaskException {
        boolean isStartupConfig = Utils.getIsTenantedStartupConfig(startupConfigMap, deviceType);
        if (isStartupConfig) {
            try {
                Operation operation;
                for (String startupOp : startupOperations) {
                    if ("SERVER_VERSION".equals(startupOp)) {
                        operation = new ProfileOperation();
                        operation.setPayLoad(ServerConfiguration.getInstance().getFirstProperty("Version"));
                    } else {
                        operation = new CommandOperation();
                    }
                    operation.setType(Operation.Type.COMMAND);
                    operation.setEnabled(true);
                    operation.setCode(startupOp);
                    deviceManagementProviderService.addOperation(deviceType, operation, validDeviceIdentifiers);
                }
            } catch (InvalidDeviceException e) {
                throw new DeviceMgtTaskException("Invalid DeviceIdentifiers found.", e);
            } catch (OperationManagementException e) {
                throw new DeviceMgtTaskException("Error occurred while adding the operations to devices", e);
            }
        }
    }

    private List<MonitoringOperation> getOperationListforTask() throws DeviceMgtTaskException {

        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder
                .getInstance().
                        getDeviceManagementProvider();

        return deviceManagementProviderService.getMonitoringOperationList(
                deviceType);//Get task list from each device type
    }

    private List<String> getStartupOperationListForTask() {
        DeviceManagementProviderService deviceManagementProviderService = DeviceManagementDataHolder.getInstance()
                .getDeviceManagementProvider();
        return deviceManagementProviderService.getStartupOperations(deviceType);
    }


    @Override
    public boolean isTaskOperation(String opName) {

        try {
            List<MonitoringOperation> monitoringOperations = this.getOperationListforTask();
            List<String> startupOperations = this.getStartupOperationListForTask();
            for (MonitoringOperation taop : monitoringOperations) {
                if (taop.getTaskName().equalsIgnoreCase(opName)) {
                    return true;
                }
            }
            if (startupOperations != null && !startupOperations.isEmpty()) {
                for (String operation : startupOperations) {
                    if (opName.equalsIgnoreCase(operation)) {
                        return true;
                    }
                }
            }
        } catch (DeviceMgtTaskException e) {
            // ignoring the error, no need to throw, If error occurs, return value will be false.
        }

        return false;

    }

}
