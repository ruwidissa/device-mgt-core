/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.common.bean.ui;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Supportability")
public class Supportability {
    private boolean support;
    private String infoText;
    private String defaultValue;

    public boolean isSupport() {
        return support;
    }

    @XmlElement(name = "Support", defaultValue = "true")
    public void setSupport(boolean support) {
        this.support = support;
    }

    public String getInfoText() {
        return infoText;
    }

    @XmlElement(name = "InfoText")
    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @XmlElement(name = "DefaultValue")
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
