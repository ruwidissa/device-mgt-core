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
package io.entgra.application.mgt.common.wrapper;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel(value = "ApplicationReleaseDTO", description = "This class holds the details when releasing an ApplicationDTO to application store")
public class EntAppReleaseWrapper {

    @ApiModelProperty(name = "description",
            value = "Description of the application release")
    @NotNull
    private String description;

    @ApiModelProperty(name = "releaseType",
            value = "Release type of the application release",
            required = true,
            example = "alpha, beta etc")
    @NotNull
    private String releaseType;

    @ApiModelProperty(name = "price",
            value = "Price of the application release",
            required = true)
    @NotNull
    private Double price;

    @ApiModelProperty(name = "isSharedWithAllTenants",
            value = "If application release is shared with all tenants it is equal to true otherwise false",
            required = true)
    @NotNull
    private boolean isSharedWithAllTenants;

    @ApiModelProperty(name = "metaData",
            value = "Meta data of the application release",
            required = true)
    private String metaData;

    @ApiModelProperty(name = "supportedOsVersions",
            value = "Application release supported OS versions",
            required = true,
            example = "4.0-10.0")
    @NotNull
    private String supportedOsVersions;

    @ApiModelProperty(name = "version",
            value = "Version number of the applications installer specifically for windows")
    private String version;

    @ApiModelProperty(name = "packageName",
            value = "PackageName of the application installer specifically for windows")
    private String packageName;

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

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getSupportedOsVersions() { return supportedOsVersions; }

    public void setSupportedOsVersions(String supportedOsVersions) { this.supportedOsVersions = supportedOsVersions; }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
