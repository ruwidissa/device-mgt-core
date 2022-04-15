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

import java.util.List;

@ApiModel(value = "ApplicationDTO", description = "ApplicationDTO represents an Application details.")
public class ApplicationDTO {

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

    @ApiModelProperty(name = "appCategories",
            value = "Category of the application",
            required = true,
            example = "Educational, Gaming, Travel, Entertainment etc")
    private List<String> appCategories;

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

    @ApiModelProperty(name = "unrestrictedRoles",
            value = "List of roles that users should have to access the application")
    private List<String> unrestrictedRoles;


    @ApiModelProperty(name = "deviceTypeId",
            value = "Id of the Related device type of the application",
            example = "1, 2, 3")
    private int deviceTypeId;

    @ApiModelProperty(name = "appRating",
            value = "Rating of the aplication")
    private double appRating;

    @ApiModelProperty(name = "status",
            value = "Application status",
            required = true,
            example = "RETIRED, ACTIVE")
    private String status;

    @ApiModelProperty(name = "applicationReleaseDTOs",
            value = "List of application releases",
            required = true)
    private List<ApplicationReleaseDTO> applicationReleaseDTOs;

    @ApiModelProperty(name = "packageName",
            value = "package name of the application")
    private String packageName;

    @ApiModelProperty(name = "isFavourite",
            value = "if the app is favoured by the user")
    private boolean isFavourite;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

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

    public List<String> getAppCategories() {
        return appCategories;
    }

    public void setAppCategories(List<String> appCategories) { this.appCategories = appCategories; }

    public List<String> getTags() { return tags; }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    public List<ApplicationReleaseDTO> getApplicationReleaseDTOs() {
        return applicationReleaseDTOs;
    }

    public void setApplicationReleaseDTOs(List<ApplicationReleaseDTO> applicationReleaseDTOs) {
        this.applicationReleaseDTOs = applicationReleaseDTOs;
    }

    public List<String> getUnrestrictedRoles() {
        return unrestrictedRoles;
    }

    public void setUnrestrictedRoles(List<String> unrestrictedRoles) {
        this.unrestrictedRoles = unrestrictedRoles;
    }

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

    public double getAppRating() { return appRating; }

    public void setAppRating(double appRating) { this.appRating = appRating; }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }
}
