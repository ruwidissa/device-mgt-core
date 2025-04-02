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

package io.entgra.device.mgt.core.dynamic.task.mgt.common;

import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.CategorizedDynamicTask;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.DynamicTaskPlatformConfigurations;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.DynamicTaskManagementException;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.api.NotFoundException;

import java.util.Set;

public interface DynamicTaskConfigurationManagementService {
    /**
     * Get dynamic task platform configurations for specified tenant domain.
     *
     * @param tenantDomain Tenant domain to get dynamic task platform configurations.
     * @return {@link DynamicTaskPlatformConfigurations}
     * @throws NotFoundException              Throws if dynamic task configuration is not available for the tenant.
     * @throws DynamicTaskManagementException Throws when error encountered while retrieving platform configurations.
     */
    DynamicTaskPlatformConfigurations getDynamicTaskPlatformConfigurations(String tenantDomain) throws NotFoundException,
            DynamicTaskManagementException;

    /**
     * Update dynamic task platform configurations in specified tenant domain.
     *
     * @param tenantDomain                   Tenant domain to update dynamic task platform configurations.
     * @param updatedCategorizedDynamicTasks Updated categorized dynamic task set.
     * @return Updated {@link DynamicTaskPlatformConfigurations}
     * @throws DynamicTaskManagementException Throws when error encountered while updating dynamic task configurations.
     */
    DynamicTaskPlatformConfigurations updateCategorizedDynamicTasks(String tenantDomain,
                                                                    Set<CategorizedDynamicTask> updatedCategorizedDynamicTasks) throws DynamicTaskManagementException;

    /**
     * Rest the dynamic task platform configurations to default settings.
     *
     * @param tenantDomain Tenant domain to reset dynamic task platform configurations.
     * @return Resetted {@link DynamicTaskPlatformConfigurations}
     * @throws DynamicTaskManagementException Throws when error encountered while resetting dynamic task
     *                                        configurations to default settings.
     */
    DynamicTaskPlatformConfigurations resetToDefault(String tenantDomain) throws DynamicTaskManagementException;
}
