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
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.util.List;

@ApiModel(value = "Application", description = "Application represents the an Application in Application Store")
public class Application {

    @ApiModelProperty(name = "id",
            value = "The ID given to the application when it is stored in the APPM database")
    private int id;

    @ApiModelProperty(name = "name",
            value = "Name of the application",
            required = true)
    private String name;

    @ApiModelProperty(name = "description",
            value = "Description of the application",
            required = true)
    private String description;

    @ApiModelProperty(name = "appCategory",
            value = "Category of the application",
            required = true,
            example = "Educational, Gaming, Travel, Entertainment etc")
    private String appCategory;

    @ApiModelProperty(name = "type",
            value = "Type of the application",
            required = true,
            example = "ENTERPRISE, PUBLIC, WEB, WEB_CLIP etc")
    private String type;

    @ApiModelProperty(name = "subType",
            value = "Subscription type of the application",
            required = true,
            example = "PAID, FREE")
    private String subType;

    @ApiModelProperty(name = "paymentCurrency",
            value = "Payment currency of the application",
            required = true,
            example = "$")
    private String paymentCurrency;

    @ApiModelProperty(name = "tags",
            value = "List of application tags")
    private List<String> tags;

    @ApiModelProperty(name = "user",
            value = "Application creating user")
    private User user;

    @ApiModelProperty(name = "unrestrictedRoles",
            value = "List of roles that users should have to access the application")
    private List<String> unrestrictedRoles;

    @ApiModelProperty(name = "isRestricted",
            value = "If unrestricted roles are defined then isRestricted value is true otherwise it is false")
    private boolean isRestricted;

    @ApiModelProperty(name = "deviceTypeId",
            value = "Id of the Related device type of the application",
            example = "1, 2, 3")
    private int deviceTypeId;

    @ApiModelProperty(name = "appRating",
            value = "Rating of the aplication")
    private int appRating;

    @ApiModelProperty(name = "deviceType",
            value = "Related device type of the application",
            required = true,
            example = "IoS, Android, Arduino, RaspberryPi etc")
    private String deviceType;

    @ApiModelProperty(name = "status",
            value = "Application status",
            required = true,
            example = "REMOVED, ACTIVE")
    private String status;

    private DeviceType deviceTypeObj;

    @ApiModelProperty(name = "applicationReleases",
            value = "List of application releases",
            required = true)
    private List<ApplicationRelease> applicationReleases;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public String getAppCategory() {
        return appCategory;
    }

    public void setAppCategory(String appCategory) {
        this.appCategory = appCategory;
    }

    public List<String> getTags() { return tags; }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getPaymentCurrency() {
        return paymentCurrency;
    }

    public void setPaymentCurrency(String paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
    }

    public boolean getIsRestricted() {
        return isRestricted;
    }

    public void setIsRestricted(boolean isRestricted) {
        this.isRestricted = isRestricted;
    }

    public List<ApplicationRelease> getApplicationReleases() {
        return applicationReleases;
    }

    public void setApplicationReleases(List<ApplicationRelease> applicationReleases) {
        this.applicationReleases = applicationReleases;
    }

    public List<String> getUnrestrictedRoles() {
        return unrestrictedRoles;
    }

    public void setUnrestrictedRoles(List<String> unrestrictedRoles) {
        this.unrestrictedRoles = unrestrictedRoles;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(int deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public int getAppRating() { return appRating; }

    public void setAppRating(int appRating) { this.appRating = appRating; }

    public DeviceType getDeviceTypeObj() { return deviceTypeObj; }

    public void setDeviceTypeObj(DeviceType deviceTypeObj) { this.deviceTypeObj = deviceTypeObj; }
}
