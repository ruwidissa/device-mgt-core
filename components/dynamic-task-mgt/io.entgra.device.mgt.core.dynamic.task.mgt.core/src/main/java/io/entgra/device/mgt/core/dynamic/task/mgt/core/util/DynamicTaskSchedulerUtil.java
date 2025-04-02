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
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.CategorizedDynamicTask;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.DynamicTaskManagementException;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.DynamicTaskScheduleException;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.api.NotFoundException;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.constant.Constants;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.internal.DynamicTaskManagementExtensionServiceDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DynamicTaskSchedulerUtil {
    private static final Log log = LogFactory.getLog(DynamicTaskSchedulerUtil.class);

    /**
     * Schedule dynamic tasks.
     *
     * @throws DynamicTaskScheduleException Throws when error encountered while scheduling the task.
     */
    public static void scheduleDynamicTasks() throws DynamicTaskScheduleException {
        try {
            scheduleDynamicTasks(DynamicTaskManagementUtil.getDynamicTaskPlatformConfigurations(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain()).getCategorizedDynamicTasks());
        } catch (NotFoundException e) {
            String msg = "Failed to find the dynamic task platform configurations entry from metadata registry";
            log.error(msg, e);
            throw new DynamicTaskScheduleException(msg, e);
        } catch (DynamicTaskManagementException e) {
            String msg = "Error encountered while retrieving dynamic task platform configuration from metadata " +
                    "registry.";
            log.error(msg, e);
            throw new DynamicTaskScheduleException(msg, e);
        }
    }

    /**
     * Schedule dynamic task in super tenant space.
     *
     * @param categorizedDynamicTask {@link CategorizedDynamicTask}
     * @param taskOwnTenantId Tenant ID of the task owner
     * @throws DynamicTaskScheduleException Throws when error encountered while scheduling the task.
     */
    public static void scheduleDynamicTask(CategorizedDynamicTask categorizedDynamicTask, int taskOwnTenantId,
                                           String taskOwnTenantDomain) throws DynamicTaskScheduleException {
        try {
            if (!Objects.equals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(),
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                    DynamicTaskManagementExtensionServiceDataHolder.getInstance().getTaskManagementService()
                            .createTask(getDynamicTask(categorizedDynamicTask, taskOwnTenantId, taskOwnTenantDomain));
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            } else {
                DynamicTaskManagementExtensionServiceDataHolder.getInstance().getTaskManagementService()
                        .createTask(getDynamicTask(categorizedDynamicTask, taskOwnTenantId, taskOwnTenantDomain));
            }
        } catch (TaskManagementException e) {
            String msg =
                    "Failed to create the dynamic task for categorized dynamic task [" + categorizedDynamicTask.getCategoryCode() + "]";
            log.error(msg);
            throw new DynamicTaskScheduleException(msg, e);
        }
    }

    /**
     * Schedule dynamic tasks in super tenant space.
     *
     * @param categorizedDynamicTasks Set of {@link CategorizedDynamicTask}
     * @throws DynamicTaskScheduleException Throws when error encountered while scheduling the set of tasks.
     */
    private static void scheduleDynamicTasks(Set<CategorizedDynamicTask> categorizedDynamicTasks) throws DynamicTaskScheduleException {
        int taskOwnTenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String taskOwnTenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            for (CategorizedDynamicTask categorizedDynamicTask : categorizedDynamicTasks) {
                scheduleDynamicTask(categorizedDynamicTask, taskOwnTenantId, taskOwnTenantDomain);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Map {@link CategorizedDynamicTask} to {@link DynamicTask}
     *
     * @param categorizedDynamicTask {@link CategorizedDynamicTask}
     * @param taskOwnTenantId Tenant ID of the task owner.
     * @param taskOwnTenantDomain Tenant Domain of the task owner.
     * @return Mapped {@link DynamicTask}
     */
    private static DynamicTask getDynamicTask(CategorizedDynamicTask categorizedDynamicTask, int taskOwnTenantId,
                                              String taskOwnTenantDomain) {
        DynamicTask dynamicTask = new DynamicTask();
        dynamicTask.setEnabled(categorizedDynamicTask.isEnable());
        dynamicTask.setName(DynamicTaskManagementUtil.generateTenantAwareNTaskName(taskOwnTenantDomain,
                categorizedDynamicTask.getCategoryCode()));
        dynamicTask.setTaskClassName(Constants.TASK_PROPERTY.CATEGORIZED_DYNAMIC_TASK_CLAZZ);
        dynamicTask.setIntervalMillis(dynamicTask.getIntervalMillis());
        Map<String, String> properties = new HashMap<>();
        properties.put(Constants.TASK_PROPERTY.CATEGORIZED_DYNAMIC_TASK,
                DynamicTaskManagementExtensionServiceDataHolder.getGson().toJson(categorizedDynamicTask));
        properties.put(Constants.TASK_PROPERTY.TENANT_ID, String.valueOf(taskOwnTenantId));
        dynamicTask.setProperties(properties);
        return dynamicTask;
    }
}
