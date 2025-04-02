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

package io.entgra.device.mgt.core.dynamic.task.mgt.core.util;

import io.entgra.device.mgt.core.task.mgt.common.bean.DynamicTask;
import io.entgra.device.mgt.core.task.mgt.common.exception.TaskManagementException;
import io.entgra.device.mgt.core.task.mgt.common.exception.TaskNotFoundException;
import io.entgra.device.mgt.core.task.mgt.common.spi.TaskManagementService;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.CategorizedDynamicTask;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.constant.Constants;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.internal.DynamicTaskManagementExtensionServiceDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DynamicTaskPatch implements Runnable {
    private static final Log log = LogFactory.getLog(DynamicTaskPatch.class);
    private static final TaskManagementService taskManagementService =
            DynamicTaskManagementExtensionServiceDataHolder.getInstance().getTaskManagementService();
    private final Set<CategorizedDynamicTask> categorizedDynamicTasks;
    private final String tenantDomain;

    public DynamicTaskPatch(String tenantDomain, Set<CategorizedDynamicTask> categorizedDynamicTasks) {
        this.tenantDomain = tenantDomain;
        this.categorizedDynamicTasks = categorizedDynamicTasks;
    }

    /**
     * Patch and update the dynamic task context.
     */
    private void patchDynamicTask() {
        try {
            List<DynamicTask> dynamicTasks = taskManagementService.getAllDynamicTasks();
            for (CategorizedDynamicTask categorizedDynamicTask : categorizedDynamicTasks) {
                for (DynamicTask dynamicTask : dynamicTasks) {
                    if (Objects.equals(dynamicTask.getName(),
                            DynamicTaskManagementUtil.generateTenantAwareNTaskName(tenantDomain,
                                    categorizedDynamicTask.getCategoryCode()))) {
                        dynamicTask.setEnabled(categorizedDynamicTask.isEnable());
                        dynamicTask.setIntervalMillis(categorizedDynamicTask.getFrequency());
                        dynamicTask.getProperties().put(Constants.TASK_PROPERTY.CATEGORIZED_DYNAMIC_TASK,
                                DynamicTaskManagementExtensionServiceDataHolder.getGson().toJson(categorizedDynamicTask));
                        taskManagementService.updateTask(dynamicTask.getDynamicTaskId(), dynamicTask);
                    }
                }
            }
        } catch (TaskNotFoundException e) {
            log.error("NTask can not be found found in tenant domain [" + tenantDomain + "].", e);
        } catch (TaskManagementException e) {
            log.error("Error encountered while updating dynamic task context.", e);
        }
    }

    @Override
    public void run() {
        if (Objects.equals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(),
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            patchDynamicTask();
        } else {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                patchDynamicTask();
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }
}
