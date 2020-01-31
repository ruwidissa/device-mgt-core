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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * <p>Java class for Feature complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * <xs:element name="Policy">
 *   <xs:complexType>
 *     <xs:sequence>
 *       <xs:element name="Name" type="xs:string" />
 *       <xs:element name="Description" type="xs:string" />
 *     </xs:sequence>
 *   </xs:complexType>
 * </xs:element>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Policy", propOrder = {
        "name",
        "dataPanel"
})
public class Policy {
    @XmlElement(name = "Name", required = true)
    protected String name;

    @XmlElementWrapper(name = "Panels")
    @XmlElement(name = "Panel")
    private List<DataPanel> dataPanel;

    /**
     * Gets the value of the name property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    public List<DataPanel> getPanels() {
        return this.dataPanel;
    }

    public void setPanels(List<DataPanel> dataPanel) {
        this.dataPanel = dataPanel;
    }
}
