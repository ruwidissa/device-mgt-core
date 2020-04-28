/*
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (https://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.common.configuration.mgt;

import java.io.Serializable;
import java.util.List;

public class EnrollmentConfiguration implements Serializable {

    private static final long serialVersionUID = 9141110402306622023L;

    private List<String> serialNumbers;
    private List<UserConfiguration> userConfigurations;
    private List<GroupConfiguration> groupConfigurations;

    public List<String> getSerialNumbers() {
        return serialNumbers;
    }

    public void setSerialNumbers(List<String> serialNumbers) {
        this.serialNumbers = serialNumbers;
    }

    public List<UserConfiguration> getUserConfigurations() {
        return userConfigurations;
    }

    public void setUserConfigurations(
            List<UserConfiguration> userConfigurations) {
        this.userConfigurations = userConfigurations;
    }

    public List<GroupConfiguration> getGroupConfigurations() {
        return groupConfigurations;
    }

    public void setGroupConfigurations(
            List<GroupConfiguration> groupConfigurations) {
        this.groupConfigurations = groupConfigurations;
    }

    public class UserConfiguration implements Serializable {

        private static final long serialVersionUID = 2787527415452188898L;

        private String username;
        private List<String> serialNumbers;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public List<String> getSerialNumbers() {
            return serialNumbers;
        }

        public void setSerialNumbers(List<String> serialNumbers) {
            this.serialNumbers = serialNumbers;
        }
    }

    public class GroupConfiguration implements Serializable {

        private static final long serialVersionUID = 6168826487754358181L;

        private String groupName;
        private List<String> serialNumbers;

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public List<String> getSerialNumbers() {
            return serialNumbers;
        }

        public void setSerialNumbers(List<String> serialNumbers) {
            this.serialNumbers = serialNumbers;
        }
    }
}
