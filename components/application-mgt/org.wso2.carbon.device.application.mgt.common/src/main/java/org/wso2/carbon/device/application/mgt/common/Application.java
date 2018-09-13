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


import org.wso2.carbon.device.application.mgt.common.jaxrs.Exclude;

import java.util.List;

/**
 * Application represents the an Application in Application Store.
 */
public class Application {

    @Exclude
    private int id;

    /**
     * Name of the application
     */
    private String name;

    /**
     * Category of the application.
     * e.g: Educational, Gaming, Travel, Entertainment etc.
     */
    private String appCategory;

    /**
     * Type of the application
     * e.g. Mobile, Web, Web Clip etc
     */
    private String type;

    /**
     * Subscription type of the application.
     * e.g: PAID, FREE
     */
    private String subType;

    /**
     * Payment currency of the application and the default value is '$'.
     */
    private String paymentCurrency;

    /**
     * List of application tags
     */
    private List<Tag> tags;

    /**
     * Application creating user
     */
    private User user;

    /**
     * List of roles that users should have to access the application
     */
    private List<UnrestrictedRole> unrestrictedRoles;

    /**
     * If unrestricted roles are defined then isRestricted value is true otherwise it is false.
     */
    private boolean isRestricted;

    /**
     * Related device type of the application.
     * e.g: IoS, Android, Arduino, RaspberryPi etc
     */
    private String deviceType;

    private List<ApplicationRelease> applicationReleases;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public String getAppCategory() {
        return appCategory;
    }

    public void setAppCategory(String appCategory) {
        this.appCategory = appCategory;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getPaymentCurrency() {
        return paymentCurrency;
    }

    public void setPaymentCurrency(String paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
    }

    public boolean getIsRestricted() {
        return isRestricted;
    }

    public void setIsRestricted(boolean isRestricted) {
        this.isRestricted = isRestricted;
    }

    public List<ApplicationRelease> getApplicationReleases() {
        return applicationReleases;
    }

    public void setApplicationReleases(List<ApplicationRelease> applicationReleases) {
        this.applicationReleases = applicationReleases;
    }

    public List<UnrestrictedRole> getUnrestrictedRoles() {
        return unrestrictedRoles;
    }

    public void setUnrestrictedRoles(List<UnrestrictedRole> unrestrictedRoles) {
        this.unrestrictedRoles = unrestrictedRoles;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
