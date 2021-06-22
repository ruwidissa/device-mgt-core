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
 * This interface is to decide a length for the geohash prefix
 * which will be used to group the clusters based on geohash
 */
public interface GeoHashLengthStrategy {

    int getGeohashLength(GeoCoordinate southWest, GeoCoordinate northEast, int zoom);

}
