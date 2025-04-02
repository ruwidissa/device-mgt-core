/*
 *   Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.entgra.device.mgt.core.dynamic.task.mgt.common.bean;

import java.util.Objects;
import java.util.Set;

public class CategorizedDynamicTask {
    private String categoryCode;
    private String name;
    private Set<OperationCode> operationCodes;
    private boolean enable;
    private Set<String> deviceTypes;
    private long frequency;
    private Set<String> configurableDeviceTypes;

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<OperationCode> getOperationCodes() {
        return operationCodes;
    }

    public void setOperationCodes(Set<OperationCode> operationCodes) {
        this.operationCodes = operationCodes;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Set<String> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(Set<String> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    public long getFrequency() {
        return frequency;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    public Set<String> getConfigurableDeviceTypes() {
        return configurableDeviceTypes;
    }

    public void setConfigurableDeviceTypes(Set<String> configurableDeviceTypes) {
        this.configurableDeviceTypes = configurableDeviceTypes;
    }

    public boolean isContentEquals(CategorizedDynamicTask categorizedDynamicTask) {
        return enable == categorizedDynamicTask.enable && frequency == categorizedDynamicTask.frequency && Objects.equals(categoryCode,
                categorizedDynamicTask.categoryCode) && Objects.equals(deviceTypes, categorizedDynamicTask.deviceTypes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CategorizedDynamicTask))
            return false;
        CategorizedDynamicTask that = (CategorizedDynamicTask) o;
        return Objects.equals(categoryCode, that.categoryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryCode, enable, deviceTypes, frequency);
    }
}
