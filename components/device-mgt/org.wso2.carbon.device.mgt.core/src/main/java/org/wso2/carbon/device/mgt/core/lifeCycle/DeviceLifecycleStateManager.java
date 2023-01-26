/*
 *   Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.device.mgt.core.lifeCycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.configuration.mgt.DeviceLifecycleState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* this class has the methods to check whether the status is valid or status change is valid
deviceLifecycleStates details are coming from lifecycle-states.xml file*/
public class DeviceLifecycleStateManager {
    private Map<String, DeviceLifecycleState> deviceLifecycleStates;
    private static final Log log = LogFactory.getLog(DeviceLifecycleStateManager.class);

    public Map<String, DeviceLifecycleState> getDeviceLifecycleStates() {
        return deviceLifecycleStates;
    }

    public void setDeviceLifecycleStates(Map<String, DeviceLifecycleState> deviceLifecycleStates) {
        this.deviceLifecycleStates = deviceLifecycleStates;
    }

    public void init(List<DeviceLifecycleState> states) {
        deviceLifecycleStates = new HashMap<>();
        for (DeviceLifecycleState deviceLifecycleState : states) {
            deviceLifecycleStates.put(deviceLifecycleState.getName(), deviceLifecycleState);
        }
    }

    public List<String> getNextLifecycleStates(String currentLifecycleState) {
        return deviceLifecycleStates.get(currentLifecycleState).getProceedingStates();
    }

    public boolean isValidStateChange(String currentStatus, String nextStatus) {
        boolean validChange = false;
        List<String> proceedingstates = deviceLifecycleStates.get(currentStatus).getProceedingStates();
        for (String proceedingState : proceedingstates) {
            if (proceedingState.equals(nextStatus)) {
                validChange = true;
                break;
            }
        }
        return validChange;
    }

    public boolean isValidState(String nextStatus) {
        boolean isValid = false;
        List<String> states = new ArrayList<>(deviceLifecycleStates.keySet());
        for (String state : states) {
            if (state.equals(nextStatus)) {
                isValid = true;
                break;
            }
        }
        return isValid;
    }
}
