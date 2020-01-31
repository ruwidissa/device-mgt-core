/*
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

public class OptionalData {
    @XmlElement(name = "Checked")
    private boolean ischecked;

    @XmlElementWrapper(name = "Options")
    @XmlElement(name = "Option")
    private List<Option> option;

    @XmlElementWrapper(name = "RadioGroup")
    @XmlElement(name = "Radio")
    private List<Option> radio;

    @XmlElementWrapper(name = "SubPanels")
    @XmlElement(name = "SubPanel")
    private List<SubPanel> subPanel;

    @XmlElementWrapper(name = "Columns")
    @XmlElement(name = "Column")
    private List<Column> columns;

    @XmlElement(name = "Placeholder")
    private String placeholder;

    @XmlElement(name = "InitialValue")
    private String initialValue;

    @XmlElement(name = "InitialDataIndex")
    private String initialDataIndex;

    @XmlElement(name = "inputType")
    private String inputType;

    @XmlElement(name = "DataSource")
    private String dataSource;

    @XmlElement(name = "Rules")
    private ValidationRules rules;

    @XmlElement(name = "Button")
    private Buttons button;

    @XmlElement(name = "RowCount")
    private int row;

    @XmlElement(name = "LabelDescription")
    private String labelDescription;

    @XmlElement(name = "StartOptionValue")
    private int firstOptionValue;

    @XmlElement(name = "LastOptionValue")
    private int lastOptionValue;

    @XmlElement(name = "ValueDifference")
    private int valueDifference;

    public boolean getChecked() {
        return ischecked;
    }

    public void setChecked(boolean hidden) {
        this.ischecked = hidden;
    }

    public List<Option> getRadioGroup() {
        return radio;
    }

    public void setRadioGroup(List<Option> radio) {
        this.radio = radio;
    }

    public List<Option> getOptions() {
        return option;
    }

    public void setOptions(List<Option> option) {
        this.option = option;
    }

    public List<SubPanel>  getSubPanels(){
        return subPanel;
    }

    public void setSubPanels(List<SubPanel>  subPanel){
        this.subPanel = subPanel;
    }

    public String getPlaceholders(){
        return placeholder;
    }

    public void setPlaceholders(String placeholder){
        this.placeholder = placeholder;
    }

    public String getInitialRadioValue(){
        return initialValue;
    }

    public void setInitialRadioValue(String initialValue){
        this.initialValue = initialValue;
    }

    public String getDataSourceName(){
        return dataSource;
    }

    public void setDataSourceName(String dataSource){
        this.dataSource = dataSource;
    }

    public void setInitialOptionValue(String initialDataIndex){
        this.initialDataIndex = initialDataIndex;
    }

    public String getInitialOptionValue(){
        return initialDataIndex;
    }

    public void setInputTypes(String inputType){
        this.inputType = inputType;
    }

    public String getInputTypes(){
        return inputType;
    }

    public Buttons getButtons(){
        return button;
    }

    public void setButtons(Buttons button){
        this.button = button;
    }

    public ValidationRules getRule(){
        return rules;
    }

    public void setRule(ValidationRules rules){
        this.rules = rules;
    }

    public int getRowCount(){
        return row;
    }

    public void setRowCount(int row){
        this.row = row;
    }

    public void setLabelDescriptions(String labelDescription){
        this.labelDescription = labelDescription;
    }

    public String getLabelDescriptions(){
        return labelDescription;
    }

    public void setStartOption(int firstOptionValue){
        this.firstOptionValue = firstOptionValue;
    }

    public int getStartOption(){
        return firstOptionValue;
    }

    public void setLastOption(int lastOptionValue){
        this.lastOptionValue = lastOptionValue;
    }

    public int getLastOption(){
        return lastOptionValue;
    }

    public void setDifference(int valueDifference){
        this.valueDifference = valueDifference;
    }

    public int getDifference(){
        return valueDifference;
    }

}
