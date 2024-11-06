/*
 * Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dangerZoneTooltipTexts", propOrder = {
        "toolTipTitle",
        "toolTipPopConfirmText",
        "confirmText",
        "cancelText",
        "toolTipAvailable"
})
public class DangerZoneTooltipTexts {

    @XmlElement(name = "toolTipTitle")
    private String toolTipTitle;

    @XmlElement(name = "toolTipPopConfirmText")
    private String toolTipPopConfirmText;

    @XmlElement(name = "confirmText")
    private String confirmText;

    @XmlElement(name = "cancelText")
    private String cancelText;

    @XmlElement(name = "toolTipAvailable")
    private String toolTipAvailable;

    public String getToolTipAvailable() {
        return toolTipAvailable;
    }

    public void setToolTipAvailable(String toolTipAvailable) {
        this.toolTipAvailable = toolTipAvailable;
    }

    public String getToolTipTitle() {
        return toolTipTitle;
    }

    public void setToolTipTitle(String toolTipTitle) {
        this.toolTipTitle = toolTipTitle;
    }

    public String getToolTipPopConfirmText() {
        return toolTipPopConfirmText;
    }

    public void setToolTipPopConfirmText(String toolTipPopConfirmText) {
        this.toolTipPopConfirmText = toolTipPopConfirmText;
    }

    public String getConfirmText() {
        return confirmText;
    }

    public void setConfirmText(String confirmText) {
        this.confirmText = confirmText;
    }

    public String getCancelText() {
        return cancelText;
    }

    public void setCancelText(String cancelText) {
        this.cancelText = cancelText;
    }
}
