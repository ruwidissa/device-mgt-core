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
public class WebAppReleaseWrapper {

    @ApiModelProperty(name = "description",
            value = "Description of the web clip release")
    @NotNull
    private String description;

    @ApiModelProperty(name = "releaseType",
            value = "Release type of the web clip release",
            required = true,
            example = "alpha, beta etc")
    @NotNull
    private String releaseType;

    @ApiModelProperty(name = "price",
            value = "Price of the web clip release",
            required = true)
    private Double price;

    @ApiModelProperty(name = "isSharedWithAllTenants",
            value = "If web clip release is shared with all tenants it is equal to 1 otherwise 0",
            required = true)
    @NotNull
    private boolean isSharedWithAllTenants;

    @ApiModelProperty(name = "metaData",
            value = "Meta data of the web clip release",
            required = true)
    private String metaData;

    @ApiModelProperty(name = "version",
            value = "Version of the web app release.",
            required = true)
    @NotNull
    private String version;

    @ApiModelProperty(name = "url",
            value = "URL which is used for WEB-CLIP")
    @NotNull
    private String url;

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

    public boolean isSharedWithAllTenants() { return isSharedWithAllTenants; }

    public void setSharedWithAllTenants(boolean sharedWithAllTenants) { isSharedWithAllTenants = sharedWithAllTenants; }

    public String getVersion() { return version; }

    public void setVersion(String version) { this.version = version; }
}
