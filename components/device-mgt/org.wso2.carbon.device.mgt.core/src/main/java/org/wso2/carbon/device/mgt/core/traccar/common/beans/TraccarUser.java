/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.traccar.common.beans;

import java.io.Serializable;

public class TraccarUser implements Serializable {

    private static final long serialVersionUID = 1998101712L;

    private int id;
    private String name;
    private String login;
    private String email;
    private String phone;
    private Boolean readonly;
    private Boolean administrator;
    private Boolean disabled;
    private String expirationTime;
    private int deviceLimit;
    private int userLimit;
    private Boolean deviceReadonly;
    private String token;
    private String password;

    public TraccarUser() {
    }

    public TraccarUser(String name, String login, String  email, String  phone, Boolean  readonly, Boolean  administrator,
                       Boolean disabled, String expirationTime, int deviceLimit, int userLimit,
                       Boolean deviceReadonly, String token, String password) {
        this.name = name;
        this.login = login;
        this.email = email;
        this.phone = phone;
        this.readonly = readonly;
        this.administrator = administrator;
        this.disabled = disabled;
        this.expirationTime = expirationTime;
        this.deviceLimit = deviceLimit;
        this.userLimit = userLimit;
        this.deviceReadonly = deviceReadonly;
        this.token = token;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    public Boolean getAdministrator() {
        return administrator;
    }

    public void setAdministrator(Boolean administrator) {
        this.administrator = administrator;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public String getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(String expirationTime) {
        this.expirationTime = expirationTime;
    }

    public int getDeviceLimit() {
        return deviceLimit;
    }

    public void setDeviceLimit(int deviceLimit) {
        this.deviceLimit = deviceLimit;
    }

    public int getUserLimit() {
        return userLimit;
    }

    public void setUserLimit(int userLimit) {
        this.userLimit = userLimit;
    }

    public Boolean getDeviceReadonly() {
        return deviceReadonly;
    }

    public void setDeviceReadonly(Boolean deviceReadonly) {
        this.deviceReadonly = deviceReadonly;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
