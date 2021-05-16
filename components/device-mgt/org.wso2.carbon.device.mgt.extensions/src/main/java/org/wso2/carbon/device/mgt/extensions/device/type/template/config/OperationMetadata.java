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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * Java class for metadata complex type.
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * <xs:element name="metadata">
 *   <xs:complexType>
 *     <xs:sequence>
 *       <xs:element name="uri" type="xs:string" />
 *       <xs:element name="method" type="xs:string" />
 *       <xs:element name="contentType" type="xs:string" />
 *       <xs:element name="permission" type="xs:string" />
 *       <xs:element name="filters">
 *         <xs:complexType>
 *           <xs:sequence>
 *             <xs:element name="property" type="xs:string" />
 *             <xs:element name="value" type="xs:string" />
 *             <xs:element name="description" type="xs:string" />
 *           </xs:sequence>
 *         </xs:complexType>
 *       </xs:element>
 *     </xs:sequence>
 *   </xs:complexType>
 * </xs:element>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "metadata", propOrder = {
        "uri",
        "method",
        "contentType",
        "permission",
        "scope",
        "filterList"
})
public class OperationMetadata {

    @XmlElement(name = "uri", required = true)
    private String uri;

    @XmlElement(name = "method", required = true)
    private String method;

    @XmlElement(name = "contentType")
    private String contentType;

    @XmlElement(name = "permission")
    private String permission;

    @XmlElement(name = "scope")
    private String scope;

    @XmlElementWrapper(name = "filters")
    @XmlElement(name = "filter")
    private List<Filter> filterList;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<Filter> getFilterList() {
        return filterList;
    }

    public void setFilters(List<Filter> filterList) {
        this.filterList = filterList;
    }
}
