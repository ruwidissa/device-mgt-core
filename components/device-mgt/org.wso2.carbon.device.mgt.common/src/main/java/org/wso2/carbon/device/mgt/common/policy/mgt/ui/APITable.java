/* Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.common.policy.mgt.ui;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "APITable")
public class APITable {
    private String requestMethod;
    private String url;
    private String payload;
    private String mainAttribute;
    private String cardTitle;
    private String cardKey;
    private Modal modal;

    @XmlElement(name = "RequestMethod")
    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    @XmlElement(name = "Url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlElement(name = "Payload")
    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @XmlElement(name = "Modal")
    public Modal getModal() {
        return modal;
    }

    public void setModal(Modal modal) {
        this.modal = modal;
    }

    @XmlElement(name = "MainAttribute")
    public String getMainAttribute() {
        return mainAttribute;
    }

    public void setMainAttribute(String mainAttribute) {
        this.mainAttribute = mainAttribute;
    }

    @XmlElement(name = "CardTitle")
    public String getCardTitle() {
        return cardTitle;
    }

    public void setCardTitle(String cardTitle) {
        this.cardTitle = cardTitle;
    }

    @XmlElement(name = "CardKey")
    public String getCardKey() {
        return cardKey;
    }

    public void setCardKey(String cardKey) {
        this.cardKey = cardKey;
    }
}
