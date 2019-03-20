/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "ApplicationRelease", description = "This class holds the details when releasing an Application to application store")
public class ApplicationRelease {

    @ApiModelProperty(name = "id",
            value = "ID of the application release")
    private int id;

    @ApiModelProperty(name = "version",
            value = "Version of the application release")
    private String version;

    @ApiModelProperty(name = "uuid",
            value = "UUID of the application release")
    private String uuid;

    @ApiModelProperty(name = "appStoredLoc",
            value = "Application storing location")
    private String appStoredLoc;

    @ApiModelProperty(name = "bannerLoc",
            value = "Banner file storing location")
    private String bannerLoc;

    @ApiModelProperty(name = "screenshotLoc1",
            value = "Screenshot storing location")
    private String screenshotLoc1;

    @ApiModelProperty(name = "screenshotLoc2",
            value = "Screenshot storing location")
    private String screenshotLoc2;

    @ApiModelProperty(name = "screenshotLoc3",
            value = "Screenshot storing location")
    private String screenshotLoc3;

    @ApiModelProperty(name = "iconLoc",
            value = "icon file storing location")
    private String iconLoc;

    @ApiModelProperty(name = "releaseType",
            value = "Release type of the application release",
            required = true,
            example = "alpha, beta etc")
    private String releaseType;

    @ApiModelProperty(name = "price",
            value = "Price of the application release",
            required = true)
    private Double price;

    @ApiModelProperty(name = "appHashValue",
            value = "Hash value of the application release")
    private String appHashValue;

//    todo change this to boolean
    @ApiModelProperty(name = "isSharedWithAllTenants",
            value = "If application release is shared with all tenants it is eqal to 1 otherwise 0",
            required = true)
    private int isSharedWithAllTenants;

    @ApiModelProperty(name = "metaData",
            value = "Meta data of the application release",
            required = true)
    private String metaData;

    @ApiModelProperty(name = "ratedUsers",
            value = "Number of users who has rated the application release")
    private int ratedUsers;

    @ApiModelProperty(name = "rating",
            value = "Rating value of the application release")
    private Double rating;

    @ApiModelProperty(name = "url",
            value = "URL which is used for WEB-CLIP")
    private String url;

    @ApiModelProperty(name = "lifecycleState",
            value = "Latest Lifecycle state of the application release")
    private LifecycleState lifecycleState;

    @ApiModelProperty(name = "packageName",
            value = "Application bundle identifier")
    private String packageName;

    public int getRatedUsers() {
        return ratedUsers;
    }

    public void setRatedUsers(int ratedUsers) {
        this.ratedUsers = ratedUsers;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
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

    public void setIsSharedWithAllTenants(int isSharedWithAllTenants) {
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getAppHashValue() {
        return appHashValue;
    }

    public int getIsSharedWithAllTenants() {
        return isSharedWithAllTenants;
    }

    public String getMetaData() {
        return metaData;
    }

    public String getAppStoredLoc() {
        return appStoredLoc;
    }

    public void setAppStoredLoc(String appStoredLoc) {
        this.appStoredLoc = appStoredLoc;
    }

    public String getBannerLoc() {
        return bannerLoc;
    }

    public void setBannerLoc(String bannerLoc) {
        this.bannerLoc = bannerLoc;
    }

    public String getScreenshotLoc1() {
        return screenshotLoc1;
    }

    public void setScreenshotLoc1(String screenshotLoc1) {
        this.screenshotLoc1 = screenshotLoc1;
    }

    public String getScreenshotLoc2() {
        return screenshotLoc2;
    }

    public void setScreenshotLoc2(String screenshotLoc2) {
        this.screenshotLoc2 = screenshotLoc2;
    }

    public String getScreenshotLoc3() {
        return screenshotLoc3;
    }

    public void setScreenshotLoc3(String screenshotLoc3) {
        this.screenshotLoc3 = screenshotLoc3;
    }

    public String getIconLoc() {
        return iconLoc;
    }

    public void setIconLoc(String iconLoc) {
        this.iconLoc = iconLoc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LifecycleState getLifecycleState() {
        return lifecycleState;
    }

    public void setLifecycleState(LifecycleState lifecycleState) {
        this.lifecycleState = lifecycleState;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }
}
