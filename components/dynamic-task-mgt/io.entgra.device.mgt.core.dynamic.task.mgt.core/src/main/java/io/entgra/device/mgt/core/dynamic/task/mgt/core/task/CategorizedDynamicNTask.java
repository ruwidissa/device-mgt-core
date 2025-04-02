/*
 *   Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.entgra.device.mgt.core.dynamic.task.mgt.core.task;

import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManagementException;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.CommandOperation;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.task.impl.DynamicPartitionedScheduleTask;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.CategorizedDynamicTask;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.OperationCode;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.constant.Constants;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.internal.DynamicTaskManagementExtensionServiceDataHolder;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.util.DynamicTaskManagementUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class CategorizedDynamicNTask extends DynamicPartitionedScheduleTask {
    private static final Log log = LogFactory.getLog(CategorizedDynamicNTask.class);
    private CategorizedDynamicTask categorizedDynamicTask;
    private int tenantId;

    /**
     * Initialize task properties.
     */
    private void initProperties() {
        String categorizedDynamicTaskProperty = getProperty(Constants.TASK_PROPERTY.CATEGORIZED_DYNAMIC_TASK);
        if (StringUtils.isBlank(categorizedDynamicTaskProperty)) {
            throw new IllegalStateException(Constants.TASK_PROPERTY.CATEGORIZED_DYNAMIC_TASK + " task property can " +
                    "not be null.");
        }
        categorizedDynamicTask =
                DynamicTaskManagementExtensionServiceDataHolder.getGson().fromJson(categorizedDynamicTaskProperty,
                        CategorizedDynamicTask.class);

        String tenantIdProperty = getProperty(Constants.TASK_PROPERTY.TENANT_ID);
        if (StringUtils.isBlank(tenantIdProperty)) {
            throw new IllegalStateException(Constants.TASK_PROPERTY.TENANT_ID + " task property can " +
                    "not be null.");
        }
        tenantId = Integer.parseInt(tenantIdProperty);
    }

    /**
     * Get the executable operations based on the current time stamp.
     *
     * @param deviceType Device type.
     * @param operationExecutedTimeMap Map contains operations and their previous executed time stamps.
     * @return List of executable operations.
     */
    private List<String> getExecutableOperations(Map<String, Long> operationExecutedTimeMap, String deviceType) {
        long currentTimeMillis = System.currentTimeMillis();
        List<String> executableOperations = new ArrayList<>();
        for (OperationCode operationCode : categorizedDynamicTask.getOperationCodes()) {
            if (operationCode.getSupportingDeviceTypes().contains(deviceType)) {
                if (operationExecutedTimeMap.containsKey(operationCode.getOperationCode())) {
                    long evaluationTime =
                            operationExecutedTimeMap.get(operationCode.getOperationCode()) + (categorizedDynamicTask.getFrequency() * operationCode.getRecurrentTime());
                    if (evaluationTime <= currentTimeMillis) {
                        executableOperations.add(operationCode.getOperationCode());
                        operationExecutedTimeMap.put(operationCode.getOperationCode(), currentTimeMillis);
                    }
                } else {
                    executableOperations.add(operationCode.getOperationCode());
                    operationExecutedTimeMap.put(operationCode.getOperationCode(), currentTimeMillis);
                }
            }
        }
        return executableOperations;
    }

    /**
     * Add device operations.
     *
     * @throws OperationManagementException Throws when error encountered while adding device operations.
     */
    private void addOperations() throws OperationManagementException {
        DeviceManagementProviderService deviceManagementProviderService =
                DynamicTaskManagementExtensionServiceDataHolder.getInstance().getDeviceManagementProviderService();
        for (String deviceType : categorizedDynamicTask.getDeviceTypes()) {
            for (String operation :
                    getExecutableOperations(DynamicTaskManagementUtil.getOperationExecutedTimeMap(tenantId,
                            deviceType), deviceType)) {
                CommandOperation commandOperation = new CommandOperation();
                commandOperation.setEnabled(true);
                commandOperation.setType(Operation.Type.COMMAND);
                commandOperation.setCode(operation);
                deviceManagementProviderService.addTaskOperation(deviceType, commandOperation,
                        getTaskContext());
            }
        }
    }

    @Override
    protected void setup() {
        initProperties();
    }

    @Override
    protected void executeDynamicTask() {
        if (!categorizedDynamicTask.isEnable()) {
            if (log.isDebugEnabled()) {
                log.info("Categorized dynamic task [" + categorizedDynamicTask.getCategoryCode()
                        + "] for tenant [" + tenantId + "] is disabled, hence aborting.");
            }
            return;
        }

        DeviceManagementProviderService deviceManagementProviderService =
                DynamicTaskManagementExtensionServiceDataHolder.getInstance().getDeviceManagementProviderService();
        try {
            List<Integer> tenants = deviceManagementProviderService.getDeviceEnrolledTenants();
            if (tenants.contains(tenantId)) {
                if (Objects.equals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), tenantId)) {
                    addOperations();
                } else {
                    try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
                        addOperations();
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
            }
        } catch (Throwable t) {
            log.error("Error encountered while executing categorized dynamic task.");
        }
    }
}
