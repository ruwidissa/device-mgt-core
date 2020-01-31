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
package org.wso2.carbon.device.mgt.common.ui.policy.mgt;

import javax.xml.bind.annotation.XmlElement;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.List;


@ApiModel(
        value = "Policy",
        description = "This class carries all information related to a policies."
)
public class Policy implements Serializable {

    private static final long serialVersionUID = -2884635400482180628L;

    @ApiModelProperty(
            name = "id",
            value = "Policy Id.",
            required = true
    )
    private int id;

    @ApiModelProperty(
            name = "name",
            value = "A name that describes a policy.",
            required = true
    )
    private String name;

    @ApiModelProperty(
            name = "description",
            value = "Provides a description of the policy.",
            required = true
    )
    private String description;

    @ApiModelProperty(
            name = "panels",
            value = "Properties related to policy.",
            required = true
    )
    private List<DataPanels> panels;

    @XmlElement
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DataPanels> getPanels() {
        return panels;
    }

    public void setPanels(List<DataPanels> panels) {
        this.panels = panels;
    }

    public static class DataPanels implements Serializable {
        private Object panel;

        public Object getPanel() {
            return panel;
        }

        public void setPanel(Object value) {
            this.panel = value;
        }

    }
}
