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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Column")
public class Column {

    private String name;
    private String valueType;
    private String key;
    private String value;
    private Select select;
    private Input input;
    private Upload upload;
    private Checkbox checkbox;
    private boolean isRequired;
    private String tooltip;
    private String docLink;

    @XmlAttribute(name = "name", required = true)
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    @XmlAttribute(name = "valueType", required = true)
    public String getValueType() { return valueType; }

    public void setValueType(String valueType) { this.valueType = valueType; }

    @XmlElement(name = "Key")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @XmlElement(name = "Value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @XmlElement(name = "Select")
    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    @XmlElement(name = "Input")
    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    @XmlElement(name = "Upload")
    public Upload getUpload() {
        return upload;
    }

    public void setUpload(Upload upload) {
        this.upload = upload;
    }

    @XmlElement(name = "Checkbox")
    public Checkbox getCheckbox() {
        return checkbox;
    }

    public void setCheckbox(Checkbox checkbox) {
        this.checkbox = checkbox;
    }

    @XmlElement(name = "RequiredItem")
    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    @XmlElement(name = "Tooltip")
    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    @XmlElement(name = "DocLink")
    public String getDocLink() {
        return docLink;
    }

    public void setDocLink(String docLink) {
        this.docLink = docLink;
    }
}
