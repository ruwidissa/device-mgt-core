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

public class OperationCode {
    private String operationCode;
    private int recurrentTime;
    private Set<String> supportingDeviceTypes;

    public String getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }

    public int getRecurrentTime() {
        return recurrentTime;
    }

    public Set<String> getSupportingDeviceTypes() {
        return supportingDeviceTypes;
    }

    public void setSupportingDeviceTypes(Set<String> supportingDeviceTypes) {
        this.supportingDeviceTypes = supportingDeviceTypes;
    }

    public void setRecurrentTime(int recurrentTime) {
        this.recurrentTime = recurrentTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OperationCode))
            return false;
        OperationCode that = (OperationCode) o;
        return recurrentTime == that.recurrentTime && Objects.equals(operationCode, that.operationCode) && Objects.equals(supportingDeviceTypes, that.supportingDeviceTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationCode, recurrentTime, supportingDeviceTypes);
    }
}
