/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Rule")
public class Rule {
    @XmlElement(name = "required")
    private boolean isRequired;

    @XmlElement(name = "regex")
    private String regexPattern;

    @XmlElement(name = "validationMessage")
    private String errorMessage;

    @XmlElement(name = "customFunction")
    private String customValidation;

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    public String getRegex() {
        return regexPattern;
    }

    public void setRegex(String regexPattern) {
        this.regexPattern = regexPattern;
    }

    public String getValidationMessage() {
        return errorMessage;
    }

    public void setValidationMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCustomFunction() {
        return customValidation;
    }

    public void setCustomFunction(String customValidation) {
        this.customValidation = customValidation;
    }
}
