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
package org.wso2.carbon.device.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "TrackerGroupInfo", description = "This class carries all information related to a add groups.")
public class TrackerInfo implements Serializable {

    private static final long serialVersionUID = 1998101712L;

    @ApiModelProperty(name = "id", value = "ID of the device in the WSO2 EMM device information database.",
            required = true)
    private int id;
    private int traccarGroupId;
    private int traccarDeviceId;
    private int groupId;
    private int tenantId;

    public TrackerInfo() {
    }

    public TrackerInfo(int traccarGroupId, int groupId, int tenantId) {
        this.traccarGroupId = traccarGroupId;
        this.groupId = groupId;
        this.tenantId = tenantId;
    }

    public TrackerInfo(int traccarDeviceId, int groupId) {
        this.traccarDeviceId = traccarDeviceId;
        this.groupId = groupId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTraccarGroupId() {
        return traccarGroupId;
    }

    public void setTraccarGroupId(int traccarGroupId) {
        this.traccarGroupId = traccarGroupId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public int getTraccarDeviceId() {
        return traccarDeviceId;
    }

    public void setTraccarDeviceId(int traccarDeviceId) {
        this.traccarDeviceId = traccarDeviceId;
    }
}
