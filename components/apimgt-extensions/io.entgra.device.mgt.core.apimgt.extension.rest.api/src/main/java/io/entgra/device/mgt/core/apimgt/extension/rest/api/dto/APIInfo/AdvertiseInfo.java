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
package io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo;

/**
 * This hold the advertisement information of an API.
 */
public class AdvertiseInfo {

    private boolean advertised;
    private String originalDevPortalUrl;
    private String apiOwner;
    private String vendor;

    public boolean isAdvertised() {
        return advertised;
    }

    public void setAdvertised(boolean advertised) {
        this.advertised = advertised;
    }

    public String getOriginalDevPortalUrl() {
        return originalDevPortalUrl;
    }

    public void setOriginalDevPortalUrl(String originalDevPortalUrl) {
        this.originalDevPortalUrl = originalDevPortalUrl;
    }

    public String getApiOwner() {
        return apiOwner;
    }

    public void setApiOwner(String apiOwner) {
        this.apiOwner = apiOwner;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
}
