package org.wso2.carbon.device.application.mgt.common;/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import java.util.List;

public class ApplicationReleaseArtifactPaths {

    private String installerPath;
    private String iconPath;
    private String bannerPath;
    private List<String> screenshotPaths;

    public String getInstallerPath() { return installerPath; }

    public void setInstallerPath(String installerPath) { this.installerPath = installerPath; }

    public String getIconPath() { return iconPath; }

    public void setIconPath(String iconPath) { this.iconPath = iconPath; }

    public String getBannerPath() { return bannerPath; }

    public void setBannerPath(String bannerPath) { this.bannerPath = bannerPath; }

    public List<String> getScreenshotPaths() { return screenshotPaths; }

    public void setScreenshotPaths(List<String> screenshotPaths) { this.screenshotPaths = screenshotPaths; }
}
