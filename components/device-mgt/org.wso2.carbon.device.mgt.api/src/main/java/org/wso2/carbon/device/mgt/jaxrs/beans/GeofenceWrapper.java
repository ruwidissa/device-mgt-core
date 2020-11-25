/*
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.jaxrs.beans;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

public class GeofenceWrapper {

    @ApiModelProperty(
            name = "id",
            value = "Id of the geo fence")
    private int id;

    @ApiModelProperty(
            name = "fenceName",
            value = "Name of the geo fence",
            required = true)
    private String fenceName;

    @ApiModelProperty(
            name = "description",
            value = "Description of the geo fence")
    private String description;

    @ApiModelProperty(
            name = "latitude",
            value = "Latitude of center of the geo fence")
    private double latitude;

    @ApiModelProperty(
            name = "longitude",
            value = "Longitude of center of the geo fence")
    private double longitude;

    @ApiModelProperty(
            name = "radius",
            value = "Radius from the center")
    private float radius;

    @ApiModelProperty(
            name = "geoJson",
            value = "JSON data set of the polygon fence")
    private String geoJson;

    @ApiModelProperty(
            name = "fenceShape",
            value = "Shape of the fence",
            required = true)
    private String fenceShape;

    @ApiModelProperty(
            name = "eventConfig",
            value = "Event configuration of the geofence",
            required = true)
    private List<EventConfig> eventConfig;

    @ApiModelProperty(
            name = "groupIds",
            value = "Group ids to add geo fences",
            required = true)
    private List<Integer> groupIds;

    private Map<Integer, String> groupNames;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFenceName() {
        return fenceName;
    }

    public void setFenceName(String fenceName) {
        this.fenceName = fenceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public String getGeoJson() {
        return geoJson;
    }

    public void setGeoJson(String geoJson) {
        this.geoJson = geoJson;
    }

    public String getFenceShape() {
        return fenceShape;
    }

    public void setFenceShape(String fenceShape) {
        this.fenceShape = fenceShape;
    }

    public List<EventConfig> getEventConfig() {
        return eventConfig;
    }

    public void setEventConfig(List<EventConfig> eventConfig) {
        this.eventConfig = eventConfig;
    }

    public List<Integer> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<Integer> groupIds) {
        this.groupIds = groupIds;
    }

    public Map<Integer, String> getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(Map<Integer, String> groupNames) {
        this.groupNames = groupNames;
    }
}
