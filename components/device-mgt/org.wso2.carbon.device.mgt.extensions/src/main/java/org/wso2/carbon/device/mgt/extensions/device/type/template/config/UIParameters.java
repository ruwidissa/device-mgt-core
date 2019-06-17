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
import java.util.List;

/**
 * Java class for uiParams complex type.
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * <xs:element name="uiParams">
 *   <xs:complexType>
 *     <xs:sequence>
 *       <xs:element name="uiParam" type="{}UIParameter"/>
 *     </xs:sequence>
 *   </xs:complexType>
 * </xs:element>
 * </pre>
 *
 */
public class UIParameters {

    @XmlElement(name = "uiParam")
    private List<UIParameter> uiParams;

    public List<UIParameter> getUiParameterList() {
        return this.uiParams;
    }
}
