/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.common;

import com.google.gson.Gson;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;

@ApiModel(value = "Billing", description = "This class carries all information related to a device billing.")
public class Billing implements Serializable {

    private static final long serialVersionUID = 1998101711L;

    @ApiModelProperty(name = "invoiceId", value = "ID of the billing.",
            required = false)
    private int invoiceId;

    @ApiModelProperty(name = "deviceId", value = "Device id of the billing.",
            required = false)
    private int deviceId;

    @ApiModelProperty(name = "tenantId", value = "Tenant of the device.",
            required = false)
    private int tenantId;

    @ApiModelProperty(name = "billingStart", value = "Start date of the billing period.", required = false)
    private Long billingStart;

    @ApiModelProperty(name = "billingEnd", value = "End date of the billing period.", required = false)
    private Long billingEnd;

    @ApiModelProperty(name = "deviceCount", value = "Device count for a billing period",
            required = false)
    private int deviceCount;


    public Billing() {
    }

    public Billing(int invoiceId, int deviceId, int tenantId, Long billingStart, Long billingEnd) {
        this.invoiceId = invoiceId;
        this.deviceId = deviceId;
        this.tenantId = tenantId;
        this.billingStart = billingStart;
        this.billingEnd = billingEnd;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public Long getBillingStart() {
        return billingStart;
    }

    public void setBillingStart(Long billingStart) {
        this.billingStart = billingStart;
    }

    public Long getBillingEnd() {
        return billingEnd;
    }

    public void setBillingEnd(Long billingEnd) {
        this.billingEnd = billingEnd;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }


}
