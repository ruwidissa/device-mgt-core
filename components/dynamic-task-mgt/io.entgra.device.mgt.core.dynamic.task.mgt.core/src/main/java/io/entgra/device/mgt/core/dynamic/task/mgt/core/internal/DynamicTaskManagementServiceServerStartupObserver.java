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

package io.entgra.device.mgt.core.dynamic.task.mgt.core.internal;

import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataKeyAlreadyExistsException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.task.mgt.common.bean.DynamicTask;
import io.entgra.device.mgt.core.task.mgt.common.exception.TaskManagementException;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.CategorizedDynamicTask;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.DynamicTaskPlatformConfigurations;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.DynamicTaskManagementConfigException;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.DynamicTaskManagementException;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.DynamicTaskScheduleException;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.api.NotFoundException;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.constant.Constants;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.util.ConfigManager;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.util.DynamicTaskManagementUtil;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.util.DynamicTaskSchedulerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.List;
import java.util.Objects;

public class DynamicTaskManagementServiceServerStartupObserver implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(DynamicTaskManagementServiceServerStartupObserver.class);
    private static final MetadataManagementService metadataManagementService =
            DynamicTaskManagementExtensionServiceDataHolder.getInstance().getMetadataManagementService();

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {
        try {
            addDefaultConfigurationTemplateToSuperTenant();
            addDefaultConfigurablePlatformConfigurationToSuperTenant();
            scheduleDynamicTasksForSuperTenant();
        } catch (DynamicTaskManagementConfigException e) {
            String msg = "Error encountered while retrieving dynamic task configurations.";
            log.error(msg, e);
            throw new IllegalStateException(msg, e);
        } catch (MetadataKeyAlreadyExistsException e) {
            String msg = "Metadata registry entry already exists for default dynamic tasks configurations.";
            log.error(msg, e);
            throw new IllegalStateException(msg, e);
        } catch (MetadataManagementException e) {
            String msg = "Error encountered while recording metadata entry containing default dynamic task " +
                    "configurations.";
            log.error(msg, e);
            throw new IllegalStateException(msg, e);
        } catch (DynamicTaskManagementException e) {
            String msg = "Error encountered while recording metadata entry containing default categorized dynamic " +
                    "task configurations.";
            log.error(msg, e);
            throw new IllegalStateException(msg, e);
        } catch (DynamicTaskScheduleException e) {
            String msg = "Error encountered while scheduling categorized dynamic tasks for available tenants.";
            log.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }

    /**
     * Add or update the default dynamic task configuration template in super tenant. This configuration template
     * will be used by sub tenants.
     *
     * @throws MetadataManagementException          Throws when setting default dynamic task platform configuration
     *                                              entry in metadata registry;
     * @throws MetadataKeyAlreadyExistsException    Throws when metadata registry entry is already exists.
     * @throws DynamicTaskManagementConfigException Throws when failed to read dynamic task configurations.
     */
    private void addDefaultConfigurationTemplateToSuperTenant() throws MetadataManagementException,
            MetadataKeyAlreadyExistsException, DynamicTaskManagementConfigException {
        // No need of tenant safety check here, since the startup thread spawns under the super tenant
        String key =
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain() + Constants.CONFIG_PREFIX.DEFAULT_CATEGORIZED_DYNAMIC_TASKS_TEMPLATE_PREFIX;
        Metadata defaultCategorizedDynamicTaskPlatformConfigEntry = metadataManagementService.retrieveMetadata(key);
        boolean isConfigExists = defaultCategorizedDynamicTaskPlatformConfigEntry != null;
        defaultCategorizedDynamicTaskPlatformConfigEntry = new Metadata();
        defaultCategorizedDynamicTaskPlatformConfigEntry.setMetaKey(key);
        defaultCategorizedDynamicTaskPlatformConfigEntry.setMetaValue(DynamicTaskManagementExtensionServiceDataHolder.getGson().toJson(ConfigManager.getConfigurations()));

        if (!isConfigExists) {
            metadataManagementService.createMetadata(defaultCategorizedDynamicTaskPlatformConfigEntry);
            log.info("[" + key + "] entry successfully created in metadata registry.");
        } else {
            metadataManagementService.updateMetadata(defaultCategorizedDynamicTaskPlatformConfigEntry);
            log.info("[" + key + "] entry successfully updated in metadata registry.");
        }
    }

    /**
     * Add configurable dynamic task configuration entry to the super tenant if it isn't exists. This configuration
     * is the one getting updated and used by the component internals.
     *
     * @throws MetadataManagementException    Throws when retrieve existing dynamic task configuration entry from
     *                                        metadata registry.
     * @throws DynamicTaskManagementException Throws when error encountered while adding dynamic task configuration
     *                                        entry to the metadata registry.
     */
    private void addDefaultConfigurablePlatformConfigurationToSuperTenant() throws MetadataManagementException,
            DynamicTaskManagementException {
        Metadata configurableCategorizedDynamicTaskEntry = metadataManagementService
                .retrieveMetadata(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain() +
                        Constants.CONFIG_PREFIX.CONFIGURABLE_CATEGORIZED_DYNAMIC_TASK_CONFIG_PREFIX);
        if (configurableCategorizedDynamicTaskEntry == null) {
            DynamicTaskManagementUtil.addConfigurableDefaultPlatformConfigurationEntryToTenant();
        }
    }

    /**
     * Schedule categorized dynamic tasks in super tenant space in initial server startup.
     *
     * @throws DynamicTaskScheduleException Throws when error encountered while scheduling dynamic tasks in super
     *                                      tenant.
     */
    private void scheduleDynamicTasksForSuperTenant() throws DynamicTaskScheduleException {
        try {
            DynamicTaskPlatformConfigurations dynamicTaskPlatformConfigurations =
                    DynamicTaskManagementUtil.getDynamicTaskPlatformConfigurations(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            List<DynamicTask> dynamicTasks =
                    DynamicTaskManagementExtensionServiceDataHolder.getInstance().getTaskManagementService().getAllDynamicTasks();
            // These tasks are only need to be schedule for the super tenant for the first server startup time only,
            // subsequent schedules will be handled by the task mgt watcher.
            for (CategorizedDynamicTask categorizedDynamicTask :
                    dynamicTaskPlatformConfigurations.getCategorizedDynamicTasks()) {
                boolean isAlreadyScheduled = false;
                for (DynamicTask dynamicTask : dynamicTasks) {
                    if (Objects.equals(dynamicTask.getName(),
                            DynamicTaskManagementUtil.generateTenantAwareNTaskName(PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                    .getTenantDomain(), categorizedDynamicTask.getCategoryCode()))) {
                        isAlreadyScheduled = true;
                    }
                }
                if (!isAlreadyScheduled) {
                    DynamicTaskSchedulerUtil.scheduleDynamicTask(categorizedDynamicTask,
                            MultitenantConstants.SUPER_TENANT_ID, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                }
            }
        } catch (NotFoundException e) {
            String msg =
                    "Dynamic task platform configurations can not be found for tenant [" + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME + "].";
            log.error(msg, e);
            throw new DynamicTaskScheduleException(msg, e);
        } catch (DynamicTaskManagementException e) {
            String msg = "Error encountered while retrieving dynamic task platform configurations.";
            log.error(msg, e);
            throw new DynamicTaskScheduleException(msg, e);
        } catch (TaskManagementException e) {
            String msg = "Error encountered while scheduling the categorized dynamic task.";
            log.error(msg, e);
            throw new DynamicTaskScheduleException(msg, e);
        }
    }
}
