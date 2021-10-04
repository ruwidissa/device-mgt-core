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
package io.entgra.application.mgt.common.response;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class Application {
    @ApiModelProperty(name = "id",
            value = "ID of the application",
            required = true)
    private int id;

    @ApiModelProperty(name = "name",
            value = "Name of the application",
            required = true)
    private String name;

    @ApiModelProperty(name = "installerName",
            value = "Application Installer Name")
    private String installerName;

    @ApiModelProperty(name = "description",
            value = "Description of the application",
            required = true)
    private String description;

    @ApiModelProperty(name = "categories",
            value = "CategoryDTO of the application",
            required = true,
            example = "Educational, Gaming, Travel, Entertainment etc")
    private List<String> categories;

    @ApiModelProperty(name = "type",
            value = "Type of the application",
            required = true,
            example = "ENTERPRISE, PUBLIC, WEB, WEB_CLIP etc")
    private String type;

    @ApiModelProperty(name = "isAndroidEnterpriseApp",
            value = "Android enterprise app or not",
            required = true,
            example = "true or false")
    private boolean isAndroidEnterpriseApp;

    @ApiModelProperty(name = "subMethod",
            value = "Subscription type of the application",
            required = true,
            example = "PAID, FREE")
    private String subMethod;

    @ApiModelProperty(name = "paymentCurrency",
            value = "Payment currency of the application",
            required = true,
            example = "$")
    private String paymentCurrency;

    @ApiModelProperty(name = "tags",
            value = "List of application tags")
    private List<String> tags;

    @ApiModelProperty(name = "unrestrictedRoles",
            value = "List of roles that users should have to access the application")
    private List<String> unrestrictedRoles;

    @ApiModelProperty(name = "deviceType",
            value = "Related device type of the application",
            required = true,
            example = "IoS, Android, Arduino, RaspberryPi etc")
    private String deviceType;

    @ApiModelProperty(name = "rating",
            value = "Application Rating")
    private double rating;

    @ApiModelProperty(name = "isDeletableApp", value = "Is Deletable Application")
    private boolean isDeletableApp;

    @ApiModelProperty(name = "isHideableApp", value = "Is Hideable application")
    private boolean isHideableApp;

    @ApiModelProperty(name = "applicationReleases",
            value = "List of application releases",
            required = true)
    private List<ApplicationRelease> applicationReleases;

    @ApiModelProperty(name = "packageName",
            value = "package name of the application")
    private String packageName;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getTags() { return tags; }

    public void setTags(List<String> tags) { this.tags = tags; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getSubMethod() { return subMethod; }

    public void setSubMethod(String subMethod) { this.subMethod = subMethod; }

    public String getPaymentCurrency() { return paymentCurrency; }

    public void setPaymentCurrency(String paymentCurrency) { this.paymentCurrency = paymentCurrency; }

    public List<ApplicationRelease> getApplicationReleases() { return applicationReleases; }

    public void setApplicationReleases(List<ApplicationRelease> applicationReleases) {
        this.applicationReleases = applicationReleases; }

    public List<String> getUnrestrictedRoles() { return unrestrictedRoles; }

    public void setUnrestrictedRoles(List<String> unrestrictedRoles) { this.unrestrictedRoles = unrestrictedRoles; }

    public String getDeviceType() { return deviceType; }

    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public double getRating() { return rating; }

    public void setRating(double rating) { this.rating = rating; }

    public boolean isDeletableApp() { return isDeletableApp; }

    public void setDeletableApp(boolean deletableApp) { isDeletableApp = deletableApp; }

    public boolean isHideableApp() { return isHideableApp; }

    public void setHideableApp(boolean hideableApp) { isHideableApp = hideableApp; }

    public boolean isAndroidEnterpriseApp() { return isAndroidEnterpriseApp; }

    public void setAndroidEnterpriseApp(boolean androidEnterpriseApp) { isAndroidEnterpriseApp = androidEnterpriseApp; }

    public String getInstallerName() { return installerName; }

    public void setInstallerName(String installerName) { this.installerName = installerName; }
}
