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
package io.entgra.task.mgt.core.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents Task Mgt configuration.
 */
@XmlRootElement(name = "TaskMgtConfiguration")
@SuppressWarnings("unused")
public final class TaskManagementConfig {

    private TaskManagementConfigRepository taskMgtConfigRepository;
    private boolean isTaskWatcherEnabled;

    @XmlElement(name = "ManagementRepository", required = true)
    public TaskManagementConfigRepository getTaskMgtConfigRepository() {
        return taskMgtConfigRepository;
    }

    public void setTaskMgtConfigRepository(TaskManagementConfigRepository taskMgtConfigRepository) {
        this.taskMgtConfigRepository = taskMgtConfigRepository;
    }

    @XmlElement(name = "TaskWatcherEnable", required = true)
    public boolean isTaskWatcherEnabled() {
        return isTaskWatcherEnabled;
    }

    public void setTaskWatcherEnabled(boolean enabled) {
        this.isTaskWatcherEnabled = enabled;
    }
}

