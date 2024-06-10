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
package io.entgra.device.mgt.core.application.mgt.common;

import java.util.List;

/**
 * Filter represents a criteria that can be used for searching applications.
 * The default value for limit is 20
 */
public class Filter {

    /***
     * Supported device type for the application.
     * e.g :- Android, iOS, Windows
     */
    private String deviceType;

    /***
     * Name of the application.
     */
    private String appName;

    /***
     * Type of the application.
     * e.g :- ENTERPRISE, PUBLIC
     */
    private String appType;

    /***
     * Subscription type of the application.
     * e.g :- FREE, PAID etc
     */
    private String subscriptionType;

    /***
     * Minimum rating of the application.
     * e.g :- 4,5
     */
    private int minimumRating;

    /***
     * Application release version.
     */
    private String version;

    /***
     * Release type of the application release.
     *  e.g :- Alpha, Beta
     */
    private String appReleaseType;

    /**
     * Category list of the application
     */
    private List<String> categories;

    /**
     * Tag list of the application
     */
    private List<String> tags;

    /***
     * Unrestricted role list. Visibility of the application can restricted through user roles and users can view the
     * application who has at least one role in unrestricted role list
     */
    private List<String> unrestrictedRoles;

    /**
     * Checking the application name matches fully with given name
     */
    private boolean isFullMatch;

    /**
     * Limit of the applications
     * default: 20
     */
    private int limit = 20;

    /**
     * Started from
     */
    private int offset;

    /**
     * Ascending or descending order
     */
    private String sortBy;

    /**
     * Current application release state.
     * e.g :- CREATED. IN_REVIEW, PUBLISHED etc
     */
    private String appReleaseState;

    /**
     * Username of whose favourite apps to be retrieved
     */
    private String favouredBy;

    /**
     * Checking if retired apps needs to be excluded
     */
    private boolean isNotRetired;

    /**
     * To check whether web applications should be returned
     */
    private boolean withWebApps;

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

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getTags() { return tags; }

    public void setTags(List<String> tags) { this.tags = tags; }

    public List<String> getUnrestrictedRoles() { return unrestrictedRoles; }

    public void setUnrestrictedRoles(List<String> unrestrictedRoles) { this.unrestrictedRoles = unrestrictedRoles; }

    public String getAppReleaseState() { return appReleaseState; }

    public void setAppReleaseState(String appReleaseState) { this.appReleaseState = appReleaseState; }

    public String getDeviceType() { return deviceType; }

    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getSubscriptionType() { return subscriptionType; }

    public void setSubscriptionType(String subscriptionType) { this.subscriptionType = subscriptionType; }

    public int getMinimumRating() { return minimumRating; }

    public void setMinimumRating(int minimumRating) { this.minimumRating = minimumRating; }

    public String getVersion() { return version; }

    public void setVersion(String version) { this.version = version; }

    public String getAppReleaseType() { return appReleaseType; }

    public void setAppReleaseType(String appReleaseType) { this.appReleaseType = appReleaseType; }

    public String getFavouredBy() {
        return favouredBy;
    }

    public void setFavouredBy(String favouredBy) {
        this.favouredBy = favouredBy;
    }

    public boolean isNotRetired() {
        return isNotRetired;
    }

    public void setNotRetired(boolean notRetired) {
        isNotRetired = notRetired;
    }

    public boolean isWithWebApps() {
        return withWebApps;
    }

    public void setWithWebApps(boolean withWebApps) {
        this.withWebApps = withWebApps;
    }
}
