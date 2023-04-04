/*
 *  Copyright (c) 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.core.config.metadata.mgt.whitelabel;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "WhiteLabelConfiguration")
public class WhiteLabelConfiguration {
    private String footerText;
    private String appTitle;
    private WhiteLabelImages whiteLabelImages;

    @XmlElement(name = "FooterText", required = true)
    public String getFooterText() {
        return footerText;
    }

    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    @XmlElement(name = "WhiteLabelImages", required = true)
    public WhiteLabelImages getWhiteLabelImages() {
        return whiteLabelImages;
    }

    public void setWhiteLabelImages(WhiteLabelImages whiteLabelImages) {
        this.whiteLabelImages = whiteLabelImages;
    }

    @XmlElement(name = "AppTitle", required = true)
    public String getAppTitle() {
        return appTitle;
    }

    public void setAppTitle(String appTitle) {
        this.appTitle = appTitle;
    }
}
