/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocationHistorySnapshotWrapper;

import java.io.Serializable;
import java.util.List;

@ApiModel(value = "DeviceBilling", description = "This class carries all information related to a managed device.")
public class DeviceBilling implements Serializable {

    private static final long serialVersionUID = 1998101711L;

    @ApiModelProperty(name = "id", value = "ID of the device in the device information database.",
            required = false)
    private int id;

    @ApiModelProperty(name = "name", value = "The device name that can be set on the device by the device user.",
            required = true)
    private String name;
//
//    @ApiModelProperty(name = "type", value = "The OS type of the device.", required = true)
//    private String type;

    @ApiModelProperty(name = "description", value = "Additional information on the device.", required = false)
    private String description;

    @ApiModelProperty(name = "cost", value = "Cost charged per device.", required = false)
    private Double cost;

    @ApiModelProperty(name = "deviceIdentifier", value = "This is a 64-bit number (as a hex string) that is randomly" +
            " generated when the user first sets up the device and should" +
            " remain constant for the lifetime of the user's device." +
            " The value may change if a factory reset is performed on " +
            "the device.",
            required = false)
    private String deviceIdentifier;

    @ApiModelProperty(name = "daysSinceEnrolled", value = "Number of days gone since device enrollment date to current date.",
            required = false)
    private int daysSinceEnrolled;

    @ApiModelProperty(name = "daysUsed", value = "Number of days gone since device enrollment date to date device was removed.",
            required = false)
    private int daysUsed;

    @ApiModelProperty(name = "enrolmentInfo", value = "This defines the device registration related information. " +
            "It is mandatory to define this information.", required = false)
    private EnrolmentInfo enrolmentInfo;

//    @ApiModelProperty(name = "features", value = "List of features.", required = false)
//    private List<Feature> features;

//    private List<DeviceBilling.Property> properties;

    @ApiModelProperty(name = "advanceInfo", value = "This defines the device registration related information. " +
            "It is mandatory to define this information.", required = false)
    private DeviceInfo deviceInfo;

//    @ApiModelProperty(name = "applications", value = "This represents the application list installed into the device",
//    required = false)
//    private List<Application> applications;

//    @ApiModelProperty(
//            name = "historySnapshot",
//            value = "device history snapshots")
//    @JsonProperty(value = "historySnapshot")
//    private DeviceLocationHistorySnapshotWrapper historySnapshot;

    public DeviceBilling() {
    }

    public DeviceBilling(String name, String description, EnrolmentInfo enrolmentInfo) {
        this.name = name;
        this.description = description;
        this.enrolmentInfo = enrolmentInfo;
    }

    public DeviceBilling(String name, String description, String deviceId, EnrolmentInfo enrolmentInfo) {
        this.name = name;
        this.description = description;
        this.deviceIdentifier = deviceId;
        this.enrolmentInfo = enrolmentInfo;
    }

    public int getDaysUsed() {
        return daysUsed;
    }

    public void setDaysUsed(int daysUsed) {
        this.daysUsed = daysUsed;
    }

    public int getDaysSinceEnrolled() {
        return daysSinceEnrolled;
    }

    public void setDaysSinceEnrolled(int daysSinceEnrolled) {
        this.daysSinceEnrolled = daysSinceEnrolled;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public EnrolmentInfo getEnrolmentInfo() {
        return enrolmentInfo;
    }

    public void setEnrolmentInfo(EnrolmentInfo enrolmentInfo) {
        this.enrolmentInfo = enrolmentInfo;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public static class Property {

        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DeviceBilling))
            return false;

        DeviceBilling device = (DeviceBilling) o;

        return getDeviceIdentifier().equals(device.getDeviceIdentifier());

    }

    @Override
    public int hashCode() {
        return getDeviceIdentifier().hashCode();
    }

}
