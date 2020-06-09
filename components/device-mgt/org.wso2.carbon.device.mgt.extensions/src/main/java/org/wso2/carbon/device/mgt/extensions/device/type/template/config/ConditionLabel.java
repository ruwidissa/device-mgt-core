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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "conditionLabel")
public class ConditionLabel {
    @XmlAttribute(name = "id")
    private String conditionID;

    @XmlAttribute(name = "value")
    private String conditionValue;

    @XmlAttribute(name = "label")
    private String conditionLabel;

    public String getId() {
        return conditionID;
    }

    public void setId(String conditionID) {
        this.conditionID = conditionID;
    }

    public String getValue() {
        return conditionValue;
    }

    public void setValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    public String getLabel() {
        return conditionLabel;
    }

    public void setLabel(String conditionLabel) {
        this.conditionLabel = conditionLabel;
    }
}
