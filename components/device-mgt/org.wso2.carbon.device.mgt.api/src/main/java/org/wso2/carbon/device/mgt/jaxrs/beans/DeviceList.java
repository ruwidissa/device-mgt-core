/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.jaxrs.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.Device;

import java.util.ArrayList;
import java.util.List;

public class DeviceList extends BasePaginatedResult {

    private List<Device> devices = new ArrayList<>();

    @ApiModelProperty(name = "totalCost", value = "Total cost of all devices per tenant", required = false)
    private double totalCost;

    @ApiModelProperty(name = "message", value = "Send information text to the billing UI", required = false)
    private String message;

    @ApiModelProperty(name = "billedDateIsValid", value = "Check if user entered date is valid", required = false)
    private boolean billedDateIsValid;

    @ApiModelProperty(value = "List of devices returned")
    @JsonProperty("devices")
    public List<Device> getList() {
        return devices;
    }

    public void setList(List<Device> devices) {
        this.devices = devices;
    }

    public boolean isBilledDateIsValid() {
        return billedDateIsValid;
    }

    public void setBilledDateIsValid(boolean billedDateIsValid) {
        this.billedDateIsValid = billedDateIsValid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        sb.append("  count: ").append(getCount()).append(",\n");
        sb.append("  devices: [").append(devices).append("\n");
        sb.append("]}\n");
        return sb.toString();
    }

}

