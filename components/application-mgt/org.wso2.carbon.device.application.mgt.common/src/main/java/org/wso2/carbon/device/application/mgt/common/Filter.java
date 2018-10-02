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

/**
 * Filter represents a criteria that can be used for searching applications.
 */
public class Filter {

    /**
     * Name of the application
     */
    private String appName;

    /**
     * Type of the application
     */
    private String appType;

    /**
     * Category of the application
     */
    private String appCategory;

    /**
     * Checking the application name matches fully with given name
     */
    private boolean isFullMatch;

    /**
     * Limit of the applications
     */
    private int limit;

    /**
     * Started from
     */
    private int offset;

    /**
     * Ascending or descending order
     */
    private String sortBy;

    /**
     * Set as True if required to have only published application release, otherwise set to False
     */
    private String currentAppReleaseState;

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

    public String getAppCategory() {
        return appCategory;
    }

    public void setAppCategory(String appCategory) {
        this.appCategory = appCategory;
    }

    public String getCurrentAppReleaseState() {
        return currentAppReleaseState;
    }

    public void setCurrentAppReleaseState(String currentAppReleaseState) {
        this.currentAppReleaseState = currentAppReleaseState;
    }
}
