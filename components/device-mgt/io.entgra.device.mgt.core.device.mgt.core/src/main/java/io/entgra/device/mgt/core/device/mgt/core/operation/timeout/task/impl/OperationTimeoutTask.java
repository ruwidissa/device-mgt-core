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
package io.entgra.device.mgt.core.device.mgt.core.operation.timeout.task.impl;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Activity;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.ActivityStatus;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManagementException;
import io.entgra.device.mgt.core.device.mgt.core.config.operation.timeout.OperationTimeout;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceType;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.task.impl.RandomlyAssignedScheduleTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OperationTimeoutTask extends RandomlyAssignedScheduleTask {

    private static final Log log = LogFactory.getLog(OperationTimeoutTask.class);
    public static final String OPERATION_TIMEOUT_TASK = "OPERATION_TIMEOUT_TASK";
    private Map<String, String> properties;

    @Override
    public final void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public final String getProperty(String name) {
        if (properties == null) {
            return null;
        }
        return properties.get(name);
    }

    @Override
    protected void setup() {

    }

    @Override
    public String getTaskName() {
        return OPERATION_TIMEOUT_TASK;
    }

    @Override
    protected void executeRandomlyAssignedTask() {
        // this task will run only in one node when the deployment has multiple nodes
        String operationTimeoutTaskConfigStr = getProperty(
                OperationTimeoutTaskManagerServiceImpl.OPERATION_TIMEOUT_TASK_CONFIG);
        Gson gson = new Gson();
        OperationTimeout operationTimeoutConfig = gson.fromJson(operationTimeoutTaskConfigStr, OperationTimeout.class);
        try {
            long timeMillis = System.currentTimeMillis() - (long) operationTimeoutConfig.getTimeout();
            List<String> deviceTypes = new ArrayList<>();
            if (operationTimeoutConfig.getDeviceTypes().size() == 1 &&
                    "ALL".equals(operationTimeoutConfig.getDeviceTypes().get(0))) {
                try {
                    List<DeviceType> deviceTypeList = DeviceManagementDataHolder.getInstance()
                            .getDeviceManagementProvider().getDeviceTypes();
                    for (DeviceType deviceType : deviceTypeList) {
                        deviceTypes.add(deviceType.getName());
                    }
                } catch (DeviceManagementException e) {
                    log.error("Error occurred while reading device types", e);
                }
            } else {
                deviceTypes = operationTimeoutConfig.getDeviceTypes();
            }
            List<Activity> activities = DeviceManagementDataHolder.getInstance().getOperationManager()
                    .getActivities(deviceTypes, operationTimeoutConfig.getCode(), timeMillis,
                            operationTimeoutConfig.getInitialStatus());
            String operationId;
            Operation operation;
            for (Activity activity : activities) {
                operationId = activity.getActivityId().replace("ACTIVITY_", "");
                for (ActivityStatus activityStatus : activity.getActivityStatus()) {
                    operation = DeviceManagementDataHolder.getInstance().getOperationManager()
                            .getOperation(Integer.parseInt(operationId));
                    operation.setStatus(Operation.Status.valueOf(operationTimeoutConfig.getNextStatus()));
                    DeviceManagementDataHolder.getInstance().getOperationManager()
                            .updateOperation(activityStatus.getDeviceIdentifier(), operation);
                }
            }
        } catch (OperationManagementException e) {
            String msg = "Error occurred while retrieving operations.";
            log.error(msg, e);
        }
    }

}
