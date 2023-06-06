/*
 * Copyright (c) 2023, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.task.mgt.watcher.internal;

import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.service.HeartBeatManagementService;
import io.entgra.device.mgt.core.task.mgt.common.spi.TaskManagementService;
import io.entgra.device.mgt.core.task.mgt.core.config.TaskConfigurationManager;
import io.entgra.device.mgt.core.task.mgt.core.config.TaskManagementConfig;
import io.entgra.device.mgt.core.task.mgt.watcher.IoTSStartupHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.user.core.service.RealmService;
import org.osgi.service.component.annotations.*;

@Component(
        name = "io.entgra.device.mgt.core.task.mgt.watcher.internal.TaskWatcherServiceComponent",
        immediate = true)
public class TaskWatcherServiceComponent {

    private static final Log log = LogFactory.getLog(TaskWatcherServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Activating Task Watcher Service Component");
        }
        try {
            TaskManagementConfig taskManagementConfig = TaskConfigurationManager.getInstance().getTaskManagementConfig();
            if (taskManagementConfig.isTaskWatcherEnabled()) {
                BundleContext bundleContext = ctx.getBundleContext();
                bundleContext.registerService(ServerStartupObserver.class.getName(), new IoTSStartupHandler(), null);
            } else {
                String msg = "Task watcher is not enabled in this environment hence wso2 carbon ntask will not " +
                        "update according to the task manager ";
                log.debug(msg);
            }
            if (log.isDebugEnabled()) {
                log.debug("Task Watcher Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Task Watcher Service Component", e);
        }
    }
    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Task Watcher Service Component");
        }
    }

    @SuppressWarnings("unused")
    @Reference(
            name = "task.service",
            service = org.wso2.carbon.ntask.core.service.TaskService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTaskService")
    public void setTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the task service to Task Watcher Service Component ");
        }
        TaskWatcherDataHolder.getInstance().setnTaskService(taskService);
    }

    @SuppressWarnings("unused")
    protected void unsetTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing the task service from Task Watcher Service Component ");
        }
        TaskWatcherDataHolder.getInstance().setnTaskService(null);
    }

    @SuppressWarnings("unused")
    @Reference(
            name = "task.mgt.service",
            service = io.entgra.device.mgt.core.task.mgt.common.spi.TaskManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTaskMgtService")
    protected void setTaskMgtService(TaskManagementService taskManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the task service to Task Watcher Service Component ");
        }
        TaskWatcherDataHolder.getInstance().setTaskManagementService(taskManagementService);
    }

    @SuppressWarnings("unused")
    protected void unsetTaskMgtService(TaskManagementService taskManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing the task service from Task Watcher Service Component ");
        }
        TaskWatcherDataHolder.getInstance().setTaskManagementService(null);
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
        TaskWatcherDataHolder.getInstance().setHeartBeatService(heartBeatService);
    }

    @SuppressWarnings("unused")
    protected void unsetHeartBeatService(HeartBeatManagementService heartBeatManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing heart beat service from Task Manager Service Component");
        }
        TaskWatcherDataHolder.getInstance().setHeartBeatService(null);
    }

    /**
     * Sets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    @SuppressWarnings("unused")
    @Reference(
            name = "realm.service",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Realm Service");
        }
        TaskWatcherDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Unsets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    @SuppressWarnings("unused")
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Realm Service");
        }
        TaskWatcherDataHolder.getInstance().setRealmService(null);
    }

}
