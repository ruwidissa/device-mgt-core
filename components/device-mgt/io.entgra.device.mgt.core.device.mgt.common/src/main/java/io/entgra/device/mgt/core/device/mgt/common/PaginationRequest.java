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

package io.entgra.device.mgt.core.device.mgt.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds required parameters for a querying a paginated device response.
 */
public class PaginationRequest {

    private int startIndex;
    private int rowCount;
    private int groupId;
    private String owner;
    private String ownerPattern;
    private String deviceType;
    private String deviceName;
    private String ownership;
    private String ownerRole;
    private Date since;
    private String filter;
    private String serialNumber;
    private String groupName;
    private String roleName;
    private String userName;
    private String deviceStatus;
    private String tabActionStatus;
    private String actionStatus;
    private String actionType;
    private String actionTriggeredBy;
    private Map<String, String> customProperty = new HashMap<>();
    private Map<String, Object> property = new HashMap<>();
    private List<String> statusList = new ArrayList<>();
    private OperationLogFilters operationLogFilters = new OperationLogFilters();
    private List<SortColumn> sortColumn = new ArrayList<>();
    private int deviceTypeId;
    public OperationLogFilters getOperationLogFilters() {
        return operationLogFilters;
    }
    public void setOperationLogFilters(OperationLogFilters operationLogFilters) {
        this.operationLogFilters = operationLogFilters;
    }
    public PaginationRequest(int start, int rowCount) {
        this.startIndex = start;
        this.rowCount = rowCount;
    }
    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<String> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<String> statusList) {
        this.statusList = statusList;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getOwnership() {
        return ownership;
    }

    public Map<String, String> getCustomProperty() {
        return customProperty;
    }

    public void setCustomProperty(Map<String, String> customProperty) {
        this.customProperty = customProperty;
    }

    public void addCustomProperty(String key, String value) {
        customProperty.put(key, value);
    }

    public void setOwnership(String ownership) {
        this.ownership = ownership;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public String getOwnerRole() {
        return ownerRole;
    }

    public void setOwnerRole(String ownerRole) {
        this.ownerRole = ownerRole;
    }

    public String getOwnerPattern() {
        return ownerPattern;
    }

    public void setOwnerPattern(String ownerPattern) {
        this.ownerPattern = ownerPattern;
    }

    public void setProperty(String key, Object value) {
        this.property.put(key, value);
    }

    public void setProperties(Map<String, Object> parameters) {
        this.property.putAll(parameters);
    }

    public Object getProperty(String key) {
        return this.property.get(key);
    }

    public String getSerialNumber() { return serialNumber; }

    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public Map<String, Object> getProperties() {
        Map<String, Object> temp = new HashMap<>();
        temp.putAll(property);
        return temp;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setSortColumn(List<SortColumn> sortColumn) { this.sortColumn = sortColumn; }

    public List<SortColumn> getSortColumn() { return sortColumn; }

    /**
     * Convert SortColumns field parameter and splitting string into columnName and sortType
     *
     * @param sortColumns which is separated by a colon(:) and first will be the columnNane and the second will be type ASC or DESC,
     *                    if there is no colon(:) detected, ASC will be default
     *                    (Ex: sort=col1:ASC&sort=col2:DESC, sort=col1&sort=col2:DESC)
     * @return sortColumnList as a list of sortColumn
     */
    public void setSortColumns(List<String> sortColumns) {
        List<SortColumn> sortColumnList = new ArrayList<>();
        SortColumn sortColumn;
        String[] sorting;
        for (String sortBy: sortColumns) {
            sortColumn = new SortColumn();
            sorting = sortBy.split(":");
            sortColumn.setName(sorting[0]);
            sortColumn.setType(sorting.length >= 2 && (sorting[1].equalsIgnoreCase("desc"))
                    ? SortColumn.types.DESC : SortColumn.types.ASC);
            sortColumnList.add(sortColumn);
        }
        setSortColumn(sortColumnList);
    }

    @Override
    public String toString() {
        return "Device type '" + this.deviceType + "' Device Name '" + this.deviceName + "' row count: " + this.rowCount
                + " Owner role '" + this.ownerRole + "' owner pattern '" + this.ownerPattern + "' ownership "
                + this.ownership + "' Status '" + this.statusList + "' owner '" + this.owner + "' groupId: " + this.groupId
                + " start index: " + this.startIndex + ", SortColumns: " + this.sortColumn;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public String getActionStatus() {
        return actionStatus;
    }

    public void setActionStatus(String actionStatus) {
        this.actionStatus = actionStatus;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionTriggeredBy() {
        return actionTriggeredBy;
    }

    public void setActionTriggeredBy(String actionTriggeredBy) {
        this.actionTriggeredBy = actionTriggeredBy;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTabActionStatus() {
        return tabActionStatus;
    }

    public void setTabActionStatus(String tabActionStatus) {
        this.tabActionStatus = tabActionStatus;
    }

    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(int deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }
}
