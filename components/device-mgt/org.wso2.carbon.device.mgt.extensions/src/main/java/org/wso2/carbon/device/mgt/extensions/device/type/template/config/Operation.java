/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

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
import javax.xml.bind.annotation.XmlType;


/**
 * Java class for Operation complex type.
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 *   <xs:element name="Operation">
 *     <xs:complexType>
 *       <xs:sequence>
 *         <xs:element name="params" type="{}Params"/>
 *         <xs:element name="metadata" type="{}OperationMetadata"/>
 *       </xs:sequence>
 *       <xs:attribute name="hidden" type="xs:boolean"/>
 *       <xs:attribute name="icon" type="xs:string"/>
 *     </xs:complexType>
 *   </xs:element>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Operation", propOrder = {
    "params",
    "metadata"
})
public class Operation {

    @XmlElement(name = "params")
    private Params params;

    @XmlElement(name = "metadata", required = true)
    private OperationMetadata metadata;

    @XmlAttribute(name = "hidden", required = true)
    private boolean hidden;

    @XmlAttribute(name = "icon")
    private String icon;

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public OperationMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(OperationMetadata metadata) {
        this.metadata = metadata;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
