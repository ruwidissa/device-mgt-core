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

package io.entgra.device.mgt.core.device.mgt.core.internal;

import io.entgra.device.mgt.core.device.mgt.core.archival.dao.ArchivalDestinationDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.archival.dao.ArchivalSourceDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceManagementConfig;
import io.entgra.device.mgt.core.device.mgt.core.config.datasource.DataSourceConfig;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.task.ArchivalTaskManager;
import io.entgra.device.mgt.core.device.mgt.core.task.impl.ArchivalTaskManagerImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.ntask.core.service.TaskService;

@Component(
        name = "io.entgra.device.mgt.core.device.mgt.core.internal.ActivityDataPurgingServiceComponent",
        immediate = true)
public class ActivityDataPurgingServiceComponent {
    private static Log log = LogFactory.getLog(ActivityDataPurgingServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing activity data archival task manager bundle.");
            }

            /* Initialising data archival configurations */
            DeviceManagementConfig config =
                    DeviceConfigurationManager.getInstance().getDeviceManagementConfig();

            boolean archivalTaskEnabled = false;
            boolean purgingTaskEnabled = false;

            if (config.getArchivalConfiguration() != null
                && config.getArchivalConfiguration().getArchivalTaskConfiguration() != null){
                archivalTaskEnabled = config.getArchivalConfiguration().getArchivalTaskConfiguration().isEnabled();
                purgingTaskEnabled = config.getArchivalConfiguration().getArchivalTaskConfiguration()
                                              .getPurgingTaskConfiguration() != null
                                      && config.getArchivalConfiguration()
                                              .getArchivalTaskConfiguration().getPurgingTaskConfiguration().isEnabled();
            }

            if (archivalTaskEnabled || purgingTaskEnabled) {
                DataSourceConfig dsConfig = config.getDeviceManagementConfigRepository().getDataSourceConfig();
                ArchivalSourceDAOFactory.init(dsConfig);
                DataSourceConfig purgingDSConfig = config.getArchivalConfiguration().getDataSourceConfig();
                ArchivalDestinationDAOFactory.init(purgingDSConfig);
            }

            ArchivalTaskManager archivalTaskManager = new ArchivalTaskManagerImpl();

            // This will start the data archival task
            if (archivalTaskEnabled) {
                archivalTaskManager.scheduleArchivalTask();
                log.info("Data archival task has been scheduled.");
            } else {
                log.warn("Data archival task has been disabled. It is recommended to enable archival task to " +
                         "prune the transactional databases tables time to time if you are using MySQL.");
            }
            
            // This will start the data deletion task.
            if (purgingTaskEnabled) {
                archivalTaskManager.scheduleDeletionTask();
                log.info("Data purging task has been scheduled for archived data.");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing activity data archival task manager service.", e);
        }
    }

    @Reference(
            name = "task.service",
            service = org.wso2.carbon.ntask.core.service.TaskService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTaskService")
    protected void setTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the task service.");
        }
        DeviceManagementDataHolder.getInstance().setTaskService(taskService);
    }

    protected void unsetTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing the task service.");
        }
        DeviceManagementDataHolder.getInstance().setTaskService(null);
    }

    @Reference(
            name = "device.mgt.provider.service",
            service = io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDeviceManagementService")
    protected void setDeviceManagementService(DeviceManagementProviderService deviceManagementService){

    }

    protected void unsetDeviceManagementService(DeviceManagementProviderService deviceManagementService){

    }

    @SuppressWarnings("unused")
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {

    }

}
