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

@XmlRootElement(name = "Toggle")
public class Toggle {
    private String toggleOnValue;
    private String toggleOffValue;
    private String toggleOnLabel;
    private String toggleOffLabel;

    public String getToggleOnValue() {
        return toggleOnValue;
    }

    @XmlElement(name = "ToggleOnValue", required = true)
    public void setToggleOnValue(String toggleOnValue) {
        this.toggleOnValue = toggleOnValue;
    }

    public String getToggleOffValue() {
        return toggleOffValue;
    }

    @XmlElement(name = "ToggleOffValue", required = true)
    public void setToggleOffValue(String toggleOffValue) {
        this.toggleOffValue = toggleOffValue;
    }

    public String getToggleOnLabel() {
        return toggleOnLabel;
    }

    @XmlElement(name = "ToggleOnLabel", required = true)
    public void setToggleOnLabel(String toggleOnLabel) {
        this.toggleOnLabel = toggleOnLabel;
    }

    public String getToggleOffLabel() {
        return toggleOffLabel;
    }

    @XmlElement(name = "ToggleOffLabel", required = true)
    public void setToggleOffLabel(String toggleOffLabel) {
        this.toggleOffLabel = toggleOffLabel;
    }
}
