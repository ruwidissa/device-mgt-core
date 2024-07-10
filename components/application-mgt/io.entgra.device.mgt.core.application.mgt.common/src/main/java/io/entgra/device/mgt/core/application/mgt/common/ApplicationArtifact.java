/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.application.mgt.common;

import java.io.InputStream;
import java.util.Map;

public class ApplicationArtifact {

    private String installerName;

    private InputStream installerStream;
    private String installerPath;

    private String bannerName;

    private InputStream bannerStream;
    private String bannerPath;

    private String iconName;

    private InputStream iconStream;
    private String iconPath;

    private Map<String , InputStream> screenshots;
    private Map<String, String> screenshotPaths;

    public String getInstallerPath() {
        return installerPath;
    }

    public void setInstallerPath(String installerPath) {
        this.installerPath = installerPath;
    }

    public String getBannerPath() {
        return bannerPath;
    }

    public void setBannerPath(String bannerPath) {
        this.bannerPath = bannerPath;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public Map<String, String> getScreenshotPaths() {
        return screenshotPaths;
    }

    public void setScreenshotPaths(Map<String, String> screenshotPaths) {
        this.screenshotPaths = screenshotPaths;
    }

    public String getInstallerName() {
        return installerName;
    }

    public void setInstallerName(String installerName) {
        this.installerName = installerName;
    }

    public InputStream getInstallerStream() {
        return installerStream;
    }

    public void setInstallerStream(InputStream installerStream) {
        this.installerStream = installerStream;
    }

    public String getBannerName() {
        return bannerName;
    }

    public void setBannerName(String bannerName) {
        this.bannerName = bannerName;
    }

    public InputStream getBannerStream() {
        return bannerStream;
    }

    public void setBannerStream(InputStream bannerStream) {
        this.bannerStream = bannerStream;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public InputStream getIconStream() {
        return iconStream;
    }

    public void setIconStream(InputStream iconStream) {
        this.iconStream = iconStream;
    }

    public Map<String, InputStream> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(Map<String, InputStream> screenshots) {
        this.screenshots = screenshots;
    }
}
