/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the String LicenseString ); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * String AS ISString  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.application.mgt.common.dto;

import java.util.List;

public class VppItuneAssetDTO {


    String adamId;
    String assignedCount;
    String availableCount;
    String deviceAssignable;
    String pricingParam;
    String productType;
    String retiredCount;
    String revocable;

    List<String> supportedPlatforms;

    public List<String> getSupportedPlatforms() {
        return supportedPlatforms;
    }

    public void setSupportedPlatforms(List<String> supportedPlatforms) {
        this.supportedPlatforms = supportedPlatforms;
    }

    public String getAdamId() {
        return adamId;
    }

    public void setAdamId(String adamId) {
        this.adamId = adamId;
    }

    public String getAssignedCount() {
        return assignedCount;
    }

    public void setAssignedCount(String assignedCount) {
        this.assignedCount = assignedCount;
    }

    public String getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(String availableCount) {
        this.availableCount = availableCount;
    }

    public String getDeviceAssignable() {
        return deviceAssignable;
    }

    public void setDeviceAssignable(String deviceAssignable) {
        this.deviceAssignable = deviceAssignable;
    }

    public String getPricingParam() {
        return pricingParam;
    }

    public void setPricingParam(String pricingParam) {
        this.pricingParam = pricingParam;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getRetiredCount() {
        return retiredCount;
    }

    public void setRetiredCount(String retiredCount) {
        this.retiredCount = retiredCount;
    }

    public String getRevocable() {
        return revocable;
    }

    public void setRevocable(String revocable) {
        this.revocable = revocable;
    }


}