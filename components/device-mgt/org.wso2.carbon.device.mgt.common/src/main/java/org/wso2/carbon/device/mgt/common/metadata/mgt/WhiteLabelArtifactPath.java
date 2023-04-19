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

package org.wso2.carbon.device.mgt.common.metadata.mgt;

public class WhiteLabelArtifactPath {
    private String faviconPath;
    private String logoPath;
    private String logoIconPath;

    public WhiteLabelArtifactPath() {

    }

    public WhiteLabelArtifactPath(String faviconPath, String logoPath, String logoIconPath) {
        this.faviconPath = faviconPath;
        this.logoPath = logoPath;
        this.logoIconPath = logoIconPath;
    }

    public String getFaviconPath() {
        return faviconPath;
    }

    public void setFaviconPath(String faviconPath) {
        this.faviconPath = faviconPath;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getLogoIconPath() {
        return logoIconPath;
    }

    public void setLogoIconPath(String logoIconPath) {
        this.logoIconPath = logoIconPath;
    }
}
