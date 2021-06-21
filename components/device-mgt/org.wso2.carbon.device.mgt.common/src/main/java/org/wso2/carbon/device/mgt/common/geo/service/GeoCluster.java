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

import org.wso2.carbon.device.mgt.common.Device;

public class GeoCluster {

    private final GeoCoordinate coordinates;
    private final GeoCoordinate southWestBound;
    private final GeoCoordinate northEastBound;
    private final long count;
    private final String geohashPrefix;
    private final Device device;

    public GeoCluster(GeoCoordinate coordinates, GeoCoordinate southWestBound, GeoCoordinate northEastBound,
                      long count, String geohashPrefix, Device device) {
        this.coordinates = coordinates;
        this.southWestBound = southWestBound;
        this.northEastBound = northEastBound;
        this.count = count;
        this.geohashPrefix = geohashPrefix;
        this.device = device;
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

    public Device getDevice() {
        return device;
    }

}
