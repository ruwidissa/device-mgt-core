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

package org.wso2.carbon.device.mgt.core.geo.geoHash.geoHashStrategy;

import org.wso2.carbon.device.mgt.common.geo.service.GeoCoordinate;

/**
 * A class that will decide the geoHashLength based on the zoom level and
 * the boundaries of the map
 **/

public class ZoomGeoHashLengthStrategy implements GeoHashLengthStrategy {

    private int minGeohashLength = 1;
    private int maxGeohashLength = 16;
    private int minZoom = 1;
    private int maxZoom = 17;

    @Override
    public int getGeohashLength(GeoCoordinate southWest, GeoCoordinate northEast, int zoom) {
        double a = minGeohashLength / Math.exp(minZoom / (maxZoom - minZoom) * Math.log(maxGeohashLength / minGeohashLength));
        double b = Math.log(maxGeohashLength / minGeohashLength) / (maxZoom - minZoom);
        return (int) Math.max(minGeohashLength, Math.min(a * Math.exp(b * zoom), maxGeohashLength));
    }

    public int getMinGeohashLength() {
        return minGeohashLength;
    }

    public void setMinGeohashLength(int minGeohashLength) {
        this.minGeohashLength = minGeohashLength;
    }

    public int getMaxGeohashLength() {
        return maxGeohashLength;
    }

    public void setMaxGeohashLength(int maxGeohashLength) {
        this.maxGeohashLength = maxGeohashLength;
    }

    public int getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(int minZoom) {
        this.minZoom = minZoom;
    }

    public int getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
    }

}
