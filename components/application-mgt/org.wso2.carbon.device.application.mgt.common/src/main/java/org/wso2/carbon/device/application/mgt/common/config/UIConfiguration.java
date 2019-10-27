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
package org.wso2.carbon.device.application.mgt.common.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

public class UIConfiguration {

    private AppRegistration appRegistration;
    private List<String> scopes;
    private boolean isSsoEnable;
    private ErrorCallback errorCallback;

    @XmlElement(name = "AppRegistration", required=true)
    public AppRegistration getAppRegistration() {
        return appRegistration;
    }

    public void setAppRegistration(AppRegistration appRegistration) {
        this.appRegistration = appRegistration;
    }

    @XmlElementWrapper(name = "Scopes")
    @XmlElement(name = "Scope")
    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    @XmlElement(name = "EnableSSO")
    public boolean isSsoEnable() {
        return isSsoEnable;
    }

    public void setSsoEnable(boolean ssoEnable) {
        isSsoEnable = ssoEnable;
    }

    @XmlElement(name = "ErrorCallback", required=true)
    public ErrorCallback getErrorCallback() { return errorCallback; }

    public void setErrorCallback(ErrorCallback errorCallback) { this.errorCallback = errorCallback; }
}
