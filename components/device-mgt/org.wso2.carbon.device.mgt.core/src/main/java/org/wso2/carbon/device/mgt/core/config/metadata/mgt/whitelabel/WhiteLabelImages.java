/*
 *  Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

@XmlRootElement(name = "WhiteLabelImages")
public class WhiteLabelImages {

    private String storagePath;
    private String defaultImagesLocation;
    private String defaultFaviconName;
    private String defaultLogoName;

    @XmlElement(name = "StoragePath", required = true)
    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    @XmlElement(name = "DefaultFaviconName", required = true)
    public String getDefaultFaviconName() {
        return defaultFaviconName;
    }

    public void setDefaultFaviconName(String defaultFaviconName) {
        this.defaultFaviconName = defaultFaviconName;
    }

    @XmlElement(name = "DefaultLogoName", required = true)
    public String getDefaultLogoName() {
        return defaultLogoName;
    }

    public void setDefaultLogoName(String defaultLogoName) {
        this.defaultLogoName = defaultLogoName;
    }

    @XmlElement(name = "DefaultImagesLocation", required = true)
    public String getDefaultImagesLocation() {
        return defaultImagesLocation;
    }

    public void setDefaultImagesLocation(String defaultImagesLocation) {
        this.defaultImagesLocation = defaultImagesLocation;
    }
}
