/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.application.mgt.common.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * This class represents the lifecycle state config
 */
@XmlRootElement(name = "LifecycleState")
public class LifecycleState {

    private String name;
    private String permission;
    private List<String> proceedingStates;
    private boolean isAppInstallable;
    private boolean isAppUpdatable;
    private boolean isInitialState;
    private boolean isEndState;
    private boolean isDeletableState;

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElementWrapper(name = "ProceedingStates")
    @XmlElement(name = "State")
    public List<String> getProceedingStates() {
        return proceedingStates;
    }

    public void setProceedingStates(List<String> proceedingStates) {
        this.proceedingStates = proceedingStates;
    }

    @XmlElement(name = "Permission")
    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @XmlElement(name = "IsAppInstallable")
    public boolean isAppInstallable() {
        return isAppInstallable;
    }

    public void setAppInstallable(boolean isAppInstallable) {
        this.isAppInstallable = isAppInstallable;
    }

    @XmlElement(name = "IsAppUpdatable")
    public boolean isAppUpdatable() {
        return isAppUpdatable;
    }

    public void setAppUpdatable(boolean isAppUpdatable) {
        this.isAppUpdatable = isAppUpdatable;
    }

    @XmlElement(name = "IsInitialState")
    public boolean isInitialState() {
        return isInitialState;
    }

    public void setInitialState(boolean isInitialState) {
        this.isInitialState = isInitialState;
    }

    @XmlElement(name = "IsEndState")
    public boolean isEndState() {
        return isEndState;
    }

    public void setEndState(boolean isEndState) {
        this.isEndState = isEndState;
    }

    @XmlElement(name = "IsDeletableState")
    public boolean isDeletableState() { return isDeletableState; }

    public void setDeletableState(boolean deletableState) { isDeletableState = deletableState; }
}
