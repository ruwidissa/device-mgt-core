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
package io.entgra.task.mgt.core.dao;

import io.entgra.task.mgt.common.bean.DynamicTask;
import io.entgra.task.mgt.common.exception.TaskManagementDAOException;

import java.util.List;

/**
 * This class represents the key operations associated with dynamic tasks.
 */
public interface DynamicTaskDAO {

    int addTask(DynamicTask dynamicTask) throws TaskManagementDAOException;

    boolean updateDynamicTask(DynamicTask dynamicTask) throws TaskManagementDAOException;

    void deleteDynamicTask(int dynamicTaskId) throws TaskManagementDAOException;

    DynamicTask getDynamicTaskById(int dynamicTaskId) throws TaskManagementDAOException;

    List<DynamicTask> getAllDynamicTasks() throws TaskManagementDAOException;

    List<DynamicTask> getActiveDynamicTasks() throws TaskManagementDAOException;

}
