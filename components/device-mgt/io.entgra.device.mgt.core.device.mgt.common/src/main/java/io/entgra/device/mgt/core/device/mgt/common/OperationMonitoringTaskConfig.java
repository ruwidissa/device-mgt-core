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

package io.entgra.device.mgt.core.device.mgt.common;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds configurations for Operation Monitoring Task.
 */
public class OperationMonitoringTaskConfig {

    private boolean isEnabled;
    private int frequency;
    private List<MonitoringOperation> monitoringOperation = new ArrayList<>();

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public List<MonitoringOperation> getMonitoringOperation() {
        return monitoringOperation;
    }

    public void setMonitoringOperation(List<MonitoringOperation> monitoringOperation) {
        this.monitoringOperation = monitoringOperation;

    }
}
