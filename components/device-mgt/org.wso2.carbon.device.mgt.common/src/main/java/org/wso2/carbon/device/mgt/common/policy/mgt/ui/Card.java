/*
 * Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.common.policy.mgt.ui;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "Card")
public class Card {

    private String title;
    private String key;
    private List<APIContent> apiContents;
    private APIContent apiContent;
    private Item item;
    private List<Item> items;
    private List<SubContent> subContents;

    @XmlAttribute(name = "title", required = true)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @XmlAttribute(name = "key", required = true)
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @XmlElementWrapper(name = "APIContents")
    @XmlElement(name = "APIContent")
    public List<APIContent> getApiContents() {
        return apiContents;
    }

    public void setApiContents(List<APIContent> apiContents) {
        this.apiContents = apiContents;
    }

    @XmlElement(name = "APIContent")
    public APIContent getApiContent() {
        return apiContent;
    }

    public void setApiContent(APIContent apiContent) {
        this.apiContent = apiContent;
    }

    @XmlElement(name = "Item")
    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    @XmlElementWrapper(name = "Items")
    @XmlElement(name = "Item")
    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    @XmlElementWrapper(name = "SubContents")
    @XmlElement(name = "SubContent")
    public List<SubContent> getSubContents() {
        return subContents;
    }

    public void setSubContents(List<SubContent> subContents) {
        this.subContents = subContents;
    }
}
