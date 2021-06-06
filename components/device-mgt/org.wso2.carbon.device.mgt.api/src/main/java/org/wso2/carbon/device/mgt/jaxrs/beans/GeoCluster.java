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

package org.wso2.carbon.device.mgt.jaxrs.beans;

import org.wso2.carbon.device.mgt.common.geo.service.GeoCoordinate;

public class GeoCluster {
    private final GeoCoordinate coordinates;
    private final GeoCoordinate southWestBound;
    private final GeoCoordinate northEastBound;
    private final long count;
    private final String geohashPrefix;
    private final String deviceIdentification;
    private final String deviceName;
    private final String deviceType;
    private final String lastSeen;

    public GeoCluster(GeoCoordinate coordinates, GeoCoordinate southWestBound, GeoCoordinate northEastBound,
                      long count, String geohashPrefix, String deviceIdentification, String deviceName,
                      String deviceType, String lastSeen) {
        this.coordinates = coordinates;
        this.southWestBound = southWestBound;
        this.northEastBound = northEastBound;
        this.count = count;
        this.geohashPrefix = geohashPrefix;
        this.deviceIdentification = deviceIdentification;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.lastSeen = lastSeen;
    }

    public String getGeohashPrefix() {
        return geohashPrefix;
    }

    public long getCount() {
        return count;
    }

    public GeoCoordinate getCoordinates() {
        return coordinates;
    }

    public GeoCoordinate getSouthWestBound() {
        return southWestBound;
    }

    public GeoCoordinate getNorthEastBound() {
        return northEastBound;
    }

    public String getDeviceIdentification() {
        return deviceIdentification;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getLastSeen() {
        return lastSeen;
    }

}
