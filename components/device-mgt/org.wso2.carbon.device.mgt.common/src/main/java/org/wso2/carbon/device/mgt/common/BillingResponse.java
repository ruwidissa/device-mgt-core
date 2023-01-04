/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.List;

@ApiModel(value = "BillingResponse", description = "This class carries all information related to a billing response.")
public class BillingResponse implements Serializable {

    private static final long serialVersionUID = 1998101711L;

    @ApiModelProperty(name = "year", value = "Year of the billed period",
            required = false)
    private String year;

    @ApiModelProperty(name = "totalCostPerYear", value = "Bill for a period of year", required = false)
    private double totalCostPerYear;

    @ApiModelProperty(name = "devices", value = "Billed list of devices per year", required = false)
    private List<Device> device;

    @ApiModelProperty(name = "billPeriod", value = "Billed period", required = false)
    private String billPeriod;

    @ApiModelProperty(name = "startDate", value = "Start Date of period", required = false)
    private String startDate;

    @ApiModelProperty(name = "endDate", value = "End Date of period", required = false)
    private String endDate;

    @ApiModelProperty(name = "deviceCount", value = "Device count for a billing period",
            required = false)
    private int deviceCount;

    public BillingResponse() {
    }

    public BillingResponse(String year, double totalCostPerYear, List<Device> device, String billPeriod, String startDate, String endDate, int deviceCount) {
        this.year = year;
        this.totalCostPerYear = totalCostPerYear;
        this.device = device;
        this.billPeriod = billPeriod;
        this.startDate = startDate;
        this.endDate = endDate;
        this.deviceCount = deviceCount;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public double getTotalCostPerYear() {
        return totalCostPerYear;
    }

    public void setTotalCostPerYear(double totalCostPerYear) {
        this.totalCostPerYear = totalCostPerYear;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public List<Device> getDevice() {
        return device;
    }

    public void setDevice(List<Device> device) {
        this.device = device;
    }

    public String getBillPeriod() {
        return billPeriod;
    }

    public void setBillPeriod(String billPeriod) {
        this.billPeriod = billPeriod;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

}
