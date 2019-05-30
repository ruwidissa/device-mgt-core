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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for params complex type.
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 *  <xs:element name="params">
 *    <xs:complexType>
 *      <xs:sequence>
 *        <xs:element name="queryParams" type="{}QueryParameters"/>
 *        <xs:element name="formParams" type="{}FormParameters"/>
 *        <xs:element name="uiParams" type="{}UIParameters"/>
 *      </xs:sequence>
 *    </xs:complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "params")
public class Params {

    @XmlElement(name = "queryParams")
    private QueryParameters queryParameters;

    @XmlElement(name = "formParams")
    private FormParameters formParameters;

    @XmlElement(name = "uiParams")
    private UIParameters uiParameters;

    public QueryParameters getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(QueryParameters queryParameters) {
        this.queryParameters = queryParameters;
    }

    public FormParameters getFormParameters() {
        return formParameters;
    }

    public void setFormParameters(FormParameters formParameters) {
        this.formParameters = formParameters;
    }

    public UIParameters getUiParameters() {
        return uiParameters;
    }

    public void setUiParameters(UIParameters uiParameters) {
        this.uiParameters = uiParameters;
    }
}
