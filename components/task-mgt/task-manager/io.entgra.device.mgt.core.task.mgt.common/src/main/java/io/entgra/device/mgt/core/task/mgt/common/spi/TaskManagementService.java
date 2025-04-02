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
package io.entgra.device.mgt.core.task.mgt.common.spi;

import io.entgra.device.mgt.core.task.mgt.common.bean.DynamicTask;
import io.entgra.device.mgt.core.task.mgt.common.exception.TaskManagementException;
import io.entgra.device.mgt.core.task.mgt.common.exception.TaskNotFoundException;

import java.util.List;
import java.util.Map;

public interface TaskManagementService {

    void createTask(DynamicTask dynamicTask) throws TaskManagementException;

    void updateTask(int dynamicTaskId, DynamicTask dynamicTask) throws TaskManagementException, TaskNotFoundException;

    void toggleTask(int dynamicTaskId, boolean isEnabled) throws TaskManagementException, TaskNotFoundException;

    void deleteTask(int dynamicTaskId) throws TaskManagementException, TaskNotFoundException;

    List<DynamicTask> getAllDynamicTasks() throws TaskManagementException;

    Map<Integer, List<DynamicTask>> getDynamicTasksForAllTenants() throws TaskManagementException;

    DynamicTask getDynamicTask(int dynamicTaskId) throws TaskManagementException;

    List<DynamicTask> getActiveDynamicTasks() throws TaskManagementException;
}
