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
package io.entgra.task.mgt.common.spi;

import io.entgra.task.mgt.common.exception.TaskNotFoundException;
import io.entgra.task.mgt.common.exception.TaskManagementException;
import io.entgra.task.mgt.common.bean.DynamicTask;

import java.util.List;

public interface TaskManagementService {

    void init() throws TaskManagementException;

    void createTask(DynamicTask dynamicTask) throws TaskManagementException;

    void updateTask(int dynamicTaskId, DynamicTask dynamicTask) throws TaskManagementException, TaskNotFoundException;

    void toggleTask(int dynamicTaskId, boolean isEnabled) throws TaskManagementException, TaskNotFoundException;

    void deleteTask(int dynamicTaskId) throws TaskManagementException, TaskNotFoundException;

    List<DynamicTask> getAllDynamicTasks() throws TaskManagementException;

    DynamicTask getDynamicTaskById(int dynamicTaskId) throws TaskManagementException;

    List<DynamicTask> getActiveDynamicTasks() throws TaskManagementException;
}
