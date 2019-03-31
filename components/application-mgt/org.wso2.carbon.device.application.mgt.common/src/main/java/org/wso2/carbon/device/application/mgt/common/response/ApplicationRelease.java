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
package org.wso2.carbon.device.application.mgt.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "ApplicationReleaseEntity", description = "This class holds the details when releasing an ApplicationEntity to application store")
public class ApplicationRelease {

    @ApiModelProperty(name = "description",
            value = "Description of the application release")
    private String description;

    @ApiModelProperty(name = "releaseType",
            value = "Release type of the application release",
            required = true,
            example = "alpha, beta etc")
    private String releaseType;

    @ApiModelProperty(name = "price",
            value = "Price of the application release",
            required = true)
    private Double price;

    @ApiModelProperty(name = "isSharedWithAllTenants",
            value = "If application release is shared with all tenants it is eqal to 1 otherwise 0",
            required = true)
    private boolean isSharedWithAllTenants;

    @ApiModelProperty(name = "metaData",
            value = "Meta data of the application release",
            required = true)
    private String metaData;

    @ApiModelProperty(name = "url",
            value = "URL which is used for WEB-CLIP")
    private String url;

    @ApiModelProperty(name = "supportedOsVersions",
            value = "ApplicationEntity release supported OS versions")
    private String supportedOsVersions;

    public String getReleaseType() {
        return releaseType;
    }

    public void setReleaseType(String releaseType) {
        this.releaseType = releaseType;
    }

    public void setIsSharedWithAllTenants(boolean isSharedWithAllTenants) {
        this.isSharedWithAllTenants = isSharedWithAllTenants;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public boolean getIsSharedWithAllTenants() {
        return isSharedWithAllTenants;
    }

    public String getMetaData() {
        return metaData;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getSupportedOsVersions() { return supportedOsVersions; }

    public void setSupportedOsVersions(String supportedOsVersions) { this.supportedOsVersions = supportedOsVersions; }
}
