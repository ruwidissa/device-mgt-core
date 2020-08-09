/* Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.common.otp.mgt.wrapper;

import org.wso2.carbon.device.mgt.common.metadata.mgt.Metadata;

import java.util.List;

public class OTPWrapper {

    private String email;
    private String emailType;
    private String username;
    private List<Metadata> properties;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailType() {
        return emailType;
    }

    public void setEmailType(String emailType) {
        this.emailType = emailType;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public List<Metadata> getProperties() { return properties; }

    public void setProperties(List<Metadata> properties) { this.properties = properties; }
}
