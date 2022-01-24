/*
 * Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.operation.timeout.task;

import org.wso2.carbon.device.mgt.core.config.operation.timeout.OperationTimeout;

/**
 * This interface defines the methods that should be implemented by the management service of OperationTimeoutTask
 */
public interface OperationTimeoutTaskManagerService {

    /**
     * This method will start the task
     * @param config
     * @throws OperationTimeoutTaskException
     */
    void startTask(OperationTimeout config)
            throws OperationTimeoutTaskException;

    /**
     * This method will stop the task.
     * @param config
     * @throws OperationTimeoutTaskException
     */
    void stopTask(OperationTimeout config)
            throws OperationTimeoutTaskException;

    /**
     * This will update the task frequency which it runs.
     *
     * @param config
     * @throws OperationTimeoutTaskException
     */
    void updateTask(OperationTimeout config)
            throws OperationTimeoutTaskException;

    /**
     * This will check weather the task is scheduled.
     * @return
     * @throws OperationTimeoutTaskException
     */
    boolean isTaskScheduled(OperationTimeout config) throws OperationTimeoutTaskException;
}