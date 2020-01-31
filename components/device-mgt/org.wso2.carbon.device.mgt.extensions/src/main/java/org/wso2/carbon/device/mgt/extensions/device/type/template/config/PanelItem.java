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

@XmlAccessorType(XmlAccessType.FIELD)
public class PanelItem {
    @XmlElement(name = "Label", required = true)
    private String label;

    @XmlAttribute(name = "type", required = true)
    protected String type;

    @XmlElement(name = "Tooltip")
    protected String tooltip;

    @XmlElement(name = "ItemId")
    protected String id;

    @XmlElement(name = "Optional")
    protected OptionalData optional;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OptionalData getOptionalData() {
        return optional;
    }

    public void setOptionalData(OptionalData optional) {
        this.optional = optional;
    }

}
