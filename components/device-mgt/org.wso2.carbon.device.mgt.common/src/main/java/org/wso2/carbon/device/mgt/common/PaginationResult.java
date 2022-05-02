/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * This class holds necessary data to represent a paginated result.
 */
@ApiModel(value = "PaginationResult", description = "This class carries all information related Pagination Result")
public class PaginationResult implements Serializable {

    private static final long serialVersionUID = 1998101711L;

    @ApiModelProperty(name = "recordsTotal", value = "The total number of records that are given before filtering", required = true)
    private int recordsTotal;

    @ApiModelProperty(name = "recordsFiltered", value = "The total number of records that are given after filtering", required = true)
    private int recordsFiltered;

    @ApiModelProperty(name = "draw", value = "The draw counter that this object is a response to, from the draw parameter sent as part of the data request", required = true)
    private int draw;

    @ApiModelProperty(name = "data", value = "This holds the database records that matches given criteria", required = true)
    private List<?> data;

    @ApiModelProperty(name = "totalCost", value = "Total cost of all devices per tenant", required = false)
    private double totalCost;

    @ApiModelProperty(name = "billedDateIsValid", value = "Check if user entered date is valid", required = false)
    private boolean billedDateIsValid;

    @ApiModelProperty(name = "message", value = "Send information text to the billing UI", required = false)
    private String message;

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

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(int recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    public void setRecordsTotal(int recordsTotal) {
        this.recordsTotal = recordsTotal;

    }

    public List<?> getData() {
        return data;
    }

    public void setData(List<?> data) {
        this.data = data;
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }
}
