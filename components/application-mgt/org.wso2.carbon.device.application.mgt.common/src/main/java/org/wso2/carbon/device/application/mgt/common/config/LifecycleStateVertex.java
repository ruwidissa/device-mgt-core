package org.wso2.carbon.device.application.mgt.common.config;/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

public class LifecycleStateVertex {
    private String label;
    private boolean isAppUpdatable;
    private boolean isAppInstallable;
    private boolean isInitialState;
    private boolean isEndState;

    public LifecycleStateVertex(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isAppUpdatable() {
        return isAppUpdatable;
    }

    public void setAppUpdatable(boolean appUpdatable) {
        isAppUpdatable = appUpdatable;
    }

    public boolean isAppInstallable() {
        return isAppInstallable;
    }

    public void setAppInstallable(boolean appInstallable) {
        isAppInstallable = appInstallable;
    }

    public boolean isInitialState() {
        return isInitialState;
    }

    public void setInitialState(boolean initialState) {
        isInitialState = initialState;
    }

    public boolean isEndState() {
        return isEndState;
    }

    public void setEndState(boolean endState) {
        isEndState = endState;
    }

    @Override
    public int hashCode(){
        return label == null ? 0 : label.hashCode();
    }

    // Overriding equals() to compare two LifecycleStateVertex objects
    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of LifecycleStateVertex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof LifecycleStateVertex)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        LifecycleStateVertex c = (LifecycleStateVertex) o;

        // Compare the equality of label name and return accordingly
        return label.equals(c.label);
    }
}
