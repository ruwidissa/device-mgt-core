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
import java.util.List;

/**
 * Java class for uiParams complex type.
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * <xs:element name="Panel" maxOccurs="unbounded">
 *   <xs:complexType>
 *     <xs:sequence>
 *       <xs:element name="title" type="xs:string" />
 *       <xs:element name="description" type="xs:string" />
 *       <xs:element name="id" type="xs:string" />
 *       <xs:element name="panelItems">
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
public class DataPanel {

    @XmlAttribute(name = "id", required = true)
    private String panelId;

    @XmlElement(name = "Title", required = true)
    protected String title;

    @XmlElement(name = "Description", required = true)
    protected String description;

    @XmlElementWrapper(name = "PanelItems")
    @XmlElement(name = "PanelItem")
    private List<PanelItem> panelItem;

    @XmlElementWrapper(name = "SubFormsList")
    @XmlElement(name = "SubForm")
    private List<SubFormList> subFormLists;

    public String getPaneId() {
        return panelId;
    }

    public void setPanelId(String panelId) {
        this.panelId = panelId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public List<PanelItem> getPanelItemList() {
        return panelItem;
    }

    public void setPanelItemList(List<PanelItem> panelItem) {
        this.panelItem = panelItem;
    }

    public List<SubFormList> getSubPanelLists() {
        return subFormLists;
    }

    public void setSubPanelLists(List<SubFormList> subFormLists) {
        this.subFormLists = subFormLists;
    }
}
