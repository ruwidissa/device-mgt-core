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

package io.entgra.device.mgt.core.device.mgt.common.group.mgt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Holds Device Group details and expose to external access
 */
@ApiModel(value = "DeviceGroupRoleWrapper", description = "This class carries all information related to a managed device group.")
public class DeviceGroupRoleWrapper implements Serializable {

    private static final long serialVersionUID = 1998121711L;

    @ApiModelProperty(name = "id", value = "ID of the device group in the device group information database.")
    private int id;

    @ApiModelProperty(name = "description", value = "The device group description that can be set on the device group by the user.",
            required = true)
    private String description;

    @ApiModelProperty(name = "name", value = "The device group name that can be set on the device group by the user.",
            required = true)
    private String name;

    private String owner;

    @ApiModelProperty(name = "status", value = "The status of group that needs updating/retrieval.")
    private String status;

    @ApiModelProperty(name = "parentGroupId", value = "Group ID of parent group")
    private int parentGroupId;

    @ApiModelProperty(name = "parentPath", value = "Path of parent group")
    private String parentPath;

    @ApiModelProperty(name = "childrenGroups", value = "Children groups")
    private List<DeviceGroup> childrenGroups;

    @ApiModelProperty(name = "userRoles", value = "User roles")
    private List<String> userRoles;

//    @ApiModelProperty(name = "DeviceGroupRoleWrapper", value = "Device group role wrapper")
//    private List<DeviceGroupRoleWrapper> DeviceGroupRoleWrapper;

    public DeviceGroupRoleWrapper() {}

    public DeviceGroupRoleWrapper(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private Map<String,String> groupProperties;

    public int getGroupId() {
        return id;
    }

    public void setGroupId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Map<String,String> getGroupProperties() {
        return groupProperties;
    }

    public void setGroupProperties(Map<String,String> groupProperties) {
        this.groupProperties = groupProperties;
    }

    public int getParentGroupId() {
        return parentGroupId;
    }

    public void setParentGroupId(int parentGroupId) {
        this.parentGroupId = parentGroupId;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public List<DeviceGroup> getChildrenGroups() {
        return childrenGroups;
    }

    public void setChildrenGroups(List<DeviceGroup> childrenGroups) {
        this.childrenGroups = childrenGroups;
    }

    public List<String> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<String> userRoles) {
        this.userRoles = userRoles;
    }
}
