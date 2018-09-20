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
package org.wso2.carbon.device.application.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Filter represents a criteria that can be used for searching applications.
 */

@ApiModel(value = "Filter", description = "This is related to the application filtering.")
public class Filter {

    @ApiModelProperty(
            name = "appName",
            value = "Name of the application",
            required = false)
    private String appName;

    @ApiModelProperty(
            name = "appType",
            value = "Type of the application",
            required = false)
    private String appType;

    @ApiModelProperty(
            name = "isFullMatch",
            value = "Checking the application name matches fully with given name",
            required = false)
    private boolean isFullMatch;

    @ApiModelProperty(
            name = "limit",
            value = "Limit of the applications",
            required = false)
    private int limit;

    @ApiModelProperty(
            name = "offset",
            value = "Started from",
            required = false)
    private int offset;

    @ApiModelProperty(
            name = "sortBy",
            value = "Ascending or descending order",
            required = false)
    private String sortBy;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isFullMatch() {
        return isFullMatch;
    }

    public void setFullMatch(boolean fullMatch) {
        isFullMatch = fullMatch;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }
}
