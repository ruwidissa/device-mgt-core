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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

public class SubPanel {
    @XmlElement(name = "PanelKey", required = true)
    protected String id;

    @XmlElement(name = "SubPanelValues")
    private SubPanelValuesList others;

    @XmlElementWrapper(name = "PanelItems")
    @XmlElement(name = "PanelItem")
    private List<PanelItem> panelItem;

    public String getPanelKey() {
        return id;
    }

    public void setPanelKey(String id) {
        this.id = id;
    }

    public SubPanelValuesList getPayloadAttributes() {
        return others;
    }

    public void setPayloadAttributes(SubPanelValuesList others) {
        this.others = others;
    }

    public List<PanelItem> getPanelItemList1() {
        return panelItem;
    }

    public void setPanelItemList1(List<PanelItem> panelItem) {
        this.panelItem = panelItem;
    }
}


