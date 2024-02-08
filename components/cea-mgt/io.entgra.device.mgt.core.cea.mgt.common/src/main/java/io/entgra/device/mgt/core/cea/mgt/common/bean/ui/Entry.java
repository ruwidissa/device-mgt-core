/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.common.bean.ui;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Entry")
public class Entry {
    private String code;
    private boolean required;
    private String label;
    private String tooltip;
    private boolean hidden;
    private Supportability supportability;
    private String docLink;
    private Input input;
    private Select select;
    private CheckboxGroup checkboxGroup;
    private Switch inputSwitch;

    public String getCode() {
        return code;
    }

    @XmlElement(name = "Code", required = true)
    public void setCode(String code) {
        this.code = code;
    }

    public boolean isRequired() {
        return required;
    }

    @XmlElement(name = "Required", defaultValue = "false")
    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getLabel() {
        return label;
    }

    @XmlElement(name = "Label", required = true)
    public void setLabel(String label) {
        this.label = label;
    }

    public String getTooltip() {
        return tooltip;
    }

    @XmlElement(name = "Tooltip")
    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public boolean isHidden() {
        return hidden;
    }

    @XmlElement(name = "Hidden", defaultValue = "false")
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public Supportability getSupportability() {
        return supportability;
    }

    @XmlElement(name = "Supportability")
    public void setSupportability(Supportability supportability) {
        this.supportability = supportability;
    }

    public String getDocLink() {
        return docLink;
    }

    @XmlElement(name = "DocLink")
    public void setDocLink(String docLink) {
        this.docLink = docLink;
    }

    public Input getInput() {
        return input;
    }

    @XmlElement(name = "Input", nillable = true)
    public void setInput(Input input) {
        this.input = input;
    }

    public Select getSelect() {
        return select;
    }

    @XmlElement(name = "Select", nillable = true)
    public void setSelect(Select select) {
        this.select = select;
    }

    public CheckboxGroup getCheckboxGroup() {
        return checkboxGroup;
    }

    @XmlElement(name = "CheckboxGroup", nillable = true)
    public void setCheckboxGroup(CheckboxGroup checkboxGroup) {
        this.checkboxGroup = checkboxGroup;
    }

    public Switch getInputSwitch() {
        return inputSwitch;
    }

    @XmlElement(name = "Switch", nillable = true)
    public void setInputSwitch(Switch inputSwitch) {
        this.inputSwitch = inputSwitch;
    }
}
