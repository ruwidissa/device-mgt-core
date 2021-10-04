/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.application.mgt.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@ApiModel(value = "ApplicationReleaseDTO", description = "This class holds the details when releasing an ApplicationDTO to application store")
@JsonIgnoreProperties({"appHashValue"})
public class ApplicationReleaseDTO {

    @ApiModelProperty(name = "id",
            value = "ID of the application release")
    private int id;

    @ApiModelProperty(name = "description",
            value = "Description of the application release")
    private String description;

    @ApiModelProperty(name = "version",
            value = "Version of the application release")
    private String version;

    @ApiModelProperty(name = "uuid",
            value = "UUID of the application release")
    private String uuid;

    @ApiModelProperty(name = "installerName",
            value = "ApplicationDTO storing location")
    private String installerName;

    @ApiModelProperty(name = "bannerName",
            value = "Banner file storing location")
    private String bannerName;

    @ApiModelProperty(name = "iconName",
            value = "icon file storing location")
    private String iconName;

    @ApiModelProperty(name = "screenshotName1",
            value = "Screenshot storing location")
    private String screenshotName1;

    @ApiModelProperty(name = "screenshotName2",
            value = "Screenshot storing location")
    private String screenshotName2;

    @ApiModelProperty(name = "screenshotName3",
            value = "Screenshot storing location")
    private String screenshotName3;

    @ApiModelProperty(name = "releaseType",
            value = "Release type of the application release",
            required = true,
            example = "alpha, beta etc")
    private String releaseType;

    @ApiModelProperty(name = "price",
            value = "Price of the application release",
            required = true)
    private double price;

    @ApiModelProperty(name = "appHashValue",
            value = "Hash value of the application release")
    private String appHashValue;

    @ApiModelProperty(name = "isSharedWithAllTenants",
            value = "If application release is shared with all tenants it is eqal to 1 otherwise 0",
            required = true)
    private boolean isSharedWithAllTenants;

    @ApiModelProperty(name = "metaData",
            value = "Meta data of the application release",
            required = true)
    private String metaData;

    @ApiModelProperty(name = "ratedUsers",
            value = "Number of users who has rated the application release")
    private int ratedUsers;

    @ApiModelProperty(name = "rating",
            value = "Rating value of the application release")
    private double rating;

    @ApiModelProperty(name = "url",
            value = "URL which is used for WEB-CLIP")
    private String url;

    @ApiModelProperty(name = "supportedOsVersions",
            value = "ApplicationDTO release supported OS versions")
    private String supportedOsVersions;

    @ApiModelProperty(name = "currentState",
            value = "Current state of the application release")
    private String currentState;

    @ApiModelProperty(name = "packageName",
            value = "ApplicationDTO bundle identifier")
    private String packageName;

    public ApplicationReleaseDTO() {
    }

    public int getRatedUsers() {
        return ratedUsers;
    }

    public void setRatedUsers(int ratedUsers) {
        this.ratedUsers = ratedUsers;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getReleaseType() {
        return releaseType;
    }

    public void setReleaseType(String releaseType) {
        this.releaseType = releaseType;
    }

    public void setAppHashValue(String appHashValue) {
        this.appHashValue = appHashValue;
    }

    public void setIsSharedWithAllTenants(boolean isSharedWithAllTenants) {
        this.isSharedWithAllTenants = isSharedWithAllTenants;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public int getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getAppHashValue() {
        return appHashValue;
    }

    public boolean getIsSharedWithAllTenants() { return isSharedWithAllTenants; }

    public String getMetaData() {
        return metaData;
    }

    public String getInstallerName() {
        return installerName;
    }

    public void setInstallerName(String installerName) {
        this.installerName = installerName;
    }

    public String getBannerName() {
        return bannerName;
    }

    public void setBannerName(String bannerName) {
        this.bannerName = bannerName;
    }

    public String getScreenshotName1() {
        return screenshotName1;
    }

    public void setScreenshotName1(String screenshotName1) {
        this.screenshotName1 = screenshotName1;
    }

    public String getScreenshotName2() {
        return screenshotName2;
    }

    public void setScreenshotName2(String screenshotName2) {
        this.screenshotName2 = screenshotName2;
    }

    public String getScreenshotName3() {
        return screenshotName3;
    }

    public void setScreenshotName3(String screenshotName3) {
        this.screenshotName3 = screenshotName3;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getSupportedOsVersions() { return supportedOsVersions; }

    public void setSupportedOsVersions(String supportedOsVersions) { this.supportedOsVersions = supportedOsVersions; }

    public String getCurrentState() { return currentState; }

    public void setCurrentState(String currentState) { this.currentState = currentState; }
}
