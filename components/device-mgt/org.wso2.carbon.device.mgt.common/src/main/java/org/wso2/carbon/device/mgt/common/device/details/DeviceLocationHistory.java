/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.common.device.details;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "DeviceLocationHistory", description = "This class carries all information related to the device location History" +
        "details provided by a device.")

public class DeviceLocationHistory implements Serializable {

    @ApiModelProperty(name = "deviceId", value = "Device id", required = true)
    private int deviceId;
    @ApiModelProperty(name = "geoHash", value = "Geo Hash", required = true)
    private String geoHash;
    @ApiModelProperty(name = "deviceType", value = "Device type", required = true)
    private String deviceType;
    @ApiModelProperty(name = "deviceIdentifier", value = "Device Id Name", required = true)
    private String deviceIdentifier;
    @ApiModelProperty(name = "latitude", value = "Device GPS latitude.", required = true)
    private Double latitude;
    @ApiModelProperty(name = "longitude", value = "Device GPS longitude.", required = true)
    private Double longitude;
    @ApiModelProperty(name = "tenantId", value = "Tenant Id.", required = true)
    private int tenantId;
    @ApiModelProperty(name = "altitude", value = "Device altitude.", required = true)
    private Double altitude;
    @ApiModelProperty(name = "speed", value = "Device speed.", required = true)
    private Float speed;
    @ApiModelProperty(name = "bearing", value = "Device bearing.", required = true)
    private Float bearing;
    @ApiModelProperty(name = "distance", value = "Device distance.", required = true)
    private Double distance;
    @ApiModelProperty(name = "timestamp", value = "Timestamp.", required = true)
    private Long timestamp;
    @ApiModelProperty(name = "owner", value = "Owner.", required = true)
    private String owner;

    public DeviceLocationHistory() {
    }

    public String getGeoHash() {
        return geoHash;
    }

    public void setGeoHash(String geoHash) {
        this.geoHash = geoHash;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public Float getBearing() {
        return bearing;
    }

    public void setBearing(Float bearing) {
        this.bearing = bearing;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
