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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "Item")
public class Item {

    private String label;
    private String tooltip;
    private String key;
    private String value;
    private boolean isRequired;
    private String subTitle;
    private List<Condition> conditions;
    private Checkbox checkbox;
    private Select select;
    private Input input;
    private TimeSelector timeSelector;
    private Table table;
    private RadioGroup radioGroup;
    private List<Notification> notifications;
    private Upload upload;
    private APITable apiTable;
    private Text text;
    private InputList inputList;

    @XmlElement(name = "Label")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @XmlElement(name = "Tooltip")
    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

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

    @XmlElement(name = "RequiredItem")
    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    @XmlElement(name = "SubTitle")
    public String getSubTitle() { return subTitle; }

    public void setSubTitle(String subTitle) { this.subTitle = subTitle; }

    @XmlElementWrapper(name = "Conditions")
    @XmlElement(name = "Condition")
    public List<Condition> getConditions() { return conditions; }

    public void setConditions(List<Condition> conditions) { this.conditions = conditions; }

    @XmlElement(name = "Checkbox")
    public Checkbox getCheckbox() {
        return checkbox;
    }

    public void setCheckbox(Checkbox checkbox) {
        this.checkbox = checkbox;
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

    @XmlElement(name = "TimeSelector")
    public TimeSelector getTimeSelector() { return timeSelector; }

    public void setTimeSelector(TimeSelector timeSelector) { this.timeSelector = timeSelector; }

    @XmlElement(name = "Table")
    public Table getTable() { return table; }

    public void setTable(Table table) { this.table = table; }

    @XmlElement(name = "RadioGroup")
    public RadioGroup getRadioGroup() {
        return radioGroup;
    }

    public void setRadioGroup(RadioGroup radioGroup) {
        this.radioGroup = radioGroup;
    }

    @XmlElementWrapper(name = "Notifications")
    @XmlElement(name = "Notification")
    public List<Notification> getNotifications() { return notifications; }

    public void setNotifications(List<Notification> notifications) { this.notifications = notifications; }

    @XmlElement(name = "Upload")
    public Upload getUpload() {
        return upload;
    }

    public void setUpload(Upload upload) {
        this.upload = upload;
    }

    @XmlElement(name = "APITable")
    public APITable getApiTable() {
        return apiTable;
    }

    public void setApiTable(APITable apiTable) {
        this.apiTable = apiTable;
    }

    @XmlElement(name = "Text")
    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    @XmlElement(name = "InputList")
    public InputList getInputList() {
        return inputList;
    }

    public void setInputList(InputList inputList) {
        this.inputList = inputList;
    }
}
