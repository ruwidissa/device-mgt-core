/*
 * Copyright (c) 2018-2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.common.geo.service;

import org.wso2.carbon.device.mgt.common.EnrolmentInfo;

import java.util.List;

public class GeoQuery {

    private final GeoCoordinate southWest;
    private final GeoCoordinate northEast;
    private final int geohashLength;
    private List<String> deviceTypes;
    private List<String> deviceIdentifiers;
    private List<EnrolmentInfo.Status> statuses;
    private List<String> ownerships;
    private List<String> owners;
    private boolean noClusters;
    private long createdBefore;
    private long createdAfter;
    private long updatedBefore;
    private long updatedAfter;

    public GeoQuery(GeoCoordinate southWest, GeoCoordinate northEast, int geohashLength) {
        this.southWest = southWest;
        this.northEast = northEast;
        this.geohashLength = geohashLength;
    }

    public GeoCoordinate getSouthWest() {
        return southWest;
    }

    public GeoCoordinate getNorthEast() {
        return northEast;
    }

    public int getGeohashLength() {
        return geohashLength;
    }

    public List<String> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(List<String> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    public List<String> getDeviceIdentifiers() {
        return deviceIdentifiers;
    }

    public void setDeviceIdentifiers(List<String> deviceIdentifiers) {
        this.deviceIdentifiers = deviceIdentifiers;
    }

    public List<EnrolmentInfo.Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<EnrolmentInfo.Status> statuses) {
        this.statuses = statuses;
    }

    public List<String> getOwnerships() {
        return ownerships;
    }

    public void setOwnerships(List<String> ownerships) {
        this.ownerships = ownerships;
    }

    public List<String> getOwners() {
        return owners;
    }

    public void setOwners(List<String> owners) {
        this.owners = owners;
    }

    public boolean isNoClusters() {
        return noClusters;
    }

    public void setNoClusters(boolean noClusters) {
        this.noClusters = noClusters;
    }

    public long getCreatedBefore() {
        return createdBefore;
    }

    public void setCreatedBefore(long createdBefore) {
        this.createdBefore = createdBefore;
    }

    public long getCreatedAfter() {
        return createdAfter;
    }

    public void setCreatedAfter(long createdAfter) {
        this.createdAfter = createdAfter;
    }

    public long getUpdatedBefore() {
        return updatedBefore;
    }

    public void setUpdatedBefore(long updatedBefore) {
        this.updatedBefore = updatedBefore;
    }

    public long getUpdatedAfter() {
        return updatedAfter;
    }

    public void setUpdatedAfter(long updatedAfter) {
        this.updatedAfter = updatedAfter;
    }

}
