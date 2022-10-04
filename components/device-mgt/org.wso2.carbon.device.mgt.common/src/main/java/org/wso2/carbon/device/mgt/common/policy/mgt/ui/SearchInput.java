/* Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "SearchInput")
public class SearchInput {

    private String valueType;
    private String placeholderValue;
    private String apiUrl;
    private String defineValueKey;
    private String displayValueKey;
    private String arrayPath;
    private String paramValue;
    private String iteratorKeyValue;

    @XmlElement(name = "Url")
    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
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

    @XmlElement(name = "ValueType", required = true)
    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    @XmlElement(name = "Placeholder")
    public String getPlaceholderValue() {
        return placeholderValue;
    }

    public void setPlaceholderValue(String placeholderValue) {
        this.placeholderValue = placeholderValue;
    }

    @XmlElement(name = "ParamValue")
    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    @XmlElement(name = "IteratorKeyValue")
    public String getIteratorKeyValue() {
        return iteratorKeyValue;
    }

    public void setIteratorKeyValue(String iteratorKeyValue) {
        this.iteratorKeyValue = iteratorKeyValue;
    }
}
