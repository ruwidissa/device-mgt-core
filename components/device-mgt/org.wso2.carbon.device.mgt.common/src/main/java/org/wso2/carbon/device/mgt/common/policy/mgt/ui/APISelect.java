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

package org.wso2.carbon.device.mgt.common.policy.mgt.ui;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "APISelect")
public class APISelect {
    private String apiUrl;  // may be need to change
    private String defineValueKey;
    private String displayValueKey;
    private String arrayPath;

    @XmlElement(name = "Url")
    public String getUrl() {
        return apiUrl;
    }

    public void setUrl(String url) {
        this.apiUrl = url;
    }

    @XmlElement(name = "DefineValueKey")
    public String getDefineValueKey() {
        return defineValueKey;
    }

    public void setDefineValueKey(String defineValueKey) {
        this.defineValueKey = defineValueKey;
    }

    @XmlElement(name = "DisplayValueKey")
    public String getDisplayValueKey() {
        return displayValueKey;
    }

    public void setDisplayValueKey(String displayValueKey) {
        this.displayValueKey = displayValueKey;
    }

    @XmlElement(name = "ArrayPath")
    public String getArrayPath() {
        return arrayPath;
    }

    public void setArrayPath(String arrayPath) {
        this.arrayPath = arrayPath;
    }
}
