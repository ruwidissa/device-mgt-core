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
package io.entgra.device.mgt.core.task.mgt.core.internal;

import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.service.HeartBeatManagementService;
import io.entgra.device.mgt.core.task.mgt.common.spi.TaskManagementService;
import io.entgra.device.mgt.core.task.mgt.core.config.TaskConfigurationManager;
import io.entgra.device.mgt.core.task.mgt.core.config.TaskManagementConfig;
import io.entgra.device.mgt.core.task.mgt.core.config.datasource.DataSourceConfig;
import io.entgra.device.mgt.core.task.mgt.core.dao.common.TaskManagementDAOFactory;
import io.entgra.device.mgt.core.task.mgt.core.service.TaskManagementServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.osgi.service.component.annotations.*;

@Component(
        name = "io.entgra.device.mgt.core.task.mgt.core.internal.TaskManagerServiceComponent",
        immediate = true)
public class TaskManagerServiceComponent {

    private static final Log log = LogFactory.getLog(TaskManagerServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctx) {

        if (log.isDebugEnabled()) {
            log.debug("Activating Task Manager Service Component");
        }
        try {
            TaskManagementConfig taskManagementConfig = TaskConfigurationManager.getInstance()
                    .getTaskManagementConfig();
            DataSourceConfig dataSourceConfig = taskManagementConfig.getTaskMgtConfigRepository()
                    .getDataSourceConfig();
            TaskManagementDAOFactory.init(dataSourceConfig);
            BundleContext bundleContext = ctx.getBundleContext();
            TaskManagementService taskManagementService = new TaskManagementServiceImpl();
            TaskManagerDataHolder.getInstance().setTaskManagementService(taskManagementService);
            bundleContext.registerService(TaskManagementService.class.getName()
                    , taskManagementService, null);
            if (log.isDebugEnabled()) {
                log.debug("Task Manager Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Task Manager Service Component", e);
        }
    }
    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Task Manager Service Component");
        }
    }

    @Reference(
            name = "datasource.service",
            service = org.wso2.carbon.ndatasource.core.DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDataSourceService")
    protected void setDataSourceService(DataSourceService dataSourceService) {
        /* This is to avoid Task Manager Service Component getting initialized before the underlying datasources
        are registered */
        if (log.isDebugEnabled()) {
            log.debug("Data source service set to Task Manager Service Component ");
        }
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        //do nothing
    }

    @Reference(
            name = "task.service",
            service = org.wso2.carbon.ntask.core.service.TaskService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTaskService")
    @SuppressWarnings("unused")
    public void setTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the task service to Task Manager Service Component");
        }
        TaskManagerDataHolder.getInstance().setnTaskService(taskService);
    }

    @SuppressWarnings("unused")
    protected void unsetTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing the task service from Task Manager Service Component");
        }
        TaskManagerDataHolder.getInstance().setnTaskService(null);
    }

    @Reference(
            name = "heartbeat.service",
            service = io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.service.HeartBeatManagementService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHeartBeatService")
    @SuppressWarnings("unused")
    protected void setHeartBeatService(HeartBeatManagementService heartBeatService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting heart beat service to Task Manager Service Component");
        }
        TaskManagerDataHolder.getInstance().setHeartBeatService(heartBeatService);
    }

    @SuppressWarnings("unused")
    protected void unsetHeartBeatService(HeartBeatManagementService heartBeatManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing heart beat service from Task Manager Service Component");
        }
        TaskManagerDataHolder.getInstance().setHeartBeatService(null);
    }

}