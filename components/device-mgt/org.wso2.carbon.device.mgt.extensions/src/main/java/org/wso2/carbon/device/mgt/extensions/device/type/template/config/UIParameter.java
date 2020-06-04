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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

/**
 * Java class for uiParams complex type.
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * <xs:element name="uiParam" maxOccurs="unbounded">
 *   <xs:complexType>
 *     <xs:sequence>
 *       <xs:element name="type" type="xs:string" />
 *       <xs:element name="name" type="xs:string" />
 *       <xs:element name="id" type="xs:string" />
 *       <xs:element name="values">
 *         <xs:complexType>
 *           <xs:sequence>
 *             <xs:element name="value" type="xs:string" />
 *           </xs:sequence>
 *         </xs:complexType>
 *       </xs:element>
 *     </xs:sequence>
 *     <xs:attribute name="optional" type="xs:string" />
 *   </xs:complexType>
 * </xs:element>
 * </pre>
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class UIParameter {

    @XmlElement(name = "id", required = true)
    protected String id;

    @XmlAttribute(name = "optional", required = true)
    private boolean optional;

    @XmlElement(name = "type", required = true)
    protected String type;

    @XmlElement(name = "name")
    protected String name;

    @XmlElement(name = "label")
    private String label;

    @XmlElement(name = "helper")
    private String helper;

    @XmlElementWrapper(name = "values")
    @XmlElement(name = "value")
    protected List<String> value;

    @XmlElement(name = "key")
    protected String key;

    @XmlElementWrapper(name = "Conditions")
    @XmlElement(name = "Condition")
    private List<Condition> conditions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getHelper() {
        return helper;
    }

    public void setHelper(String helper) {
        this.helper = helper;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(
            List<Condition> conditions) {
        this.conditions = conditions;
    }
}
