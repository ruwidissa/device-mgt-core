/*
 * Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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

import React, { Component, Fragment } from 'react';
import {
  Map,
  TileLayer,
  Marker,
  Polyline,
  Popup,
  Tooltip,
} from 'react-leaflet';
import L from 'leaflet';
import { withConfigContext } from '../../../../../../components/ConfigContext';

// Icons for the location markers
const pinStart = new L.Icon({
  iconUrl: require('./pin_start.svg'),
  iconRetinaUrl: require('./pin_start.svg'),
  shadowUrl: require('./pin_shadow.svg'),
  shadowSize: new L.Point(51, 51),
  shadowAnchor: [22, 22],
  popupAnchor: [0, -22],
  iconSize: new L.Point(50, 50),
  tooltipAnchor: [0, -22],
});
const pinEnd = new L.Icon({
  iconUrl: require('./pin_end.svg'),
  iconRetinaUrl: require('./pin_end.svg'),
  shadowUrl: require('./pin_shadow.svg'),
  shadowSize: new L.Point(51, 51),
  shadowAnchor: [22, 22],
  popupAnchor: [0, -22],
  iconSize: new L.Point(65, 65),
  tooltipAnchor: [0, -28],
});

class CustomMap extends Component {
  constructor(props) {
    super(props);
  }

  /**
   * Polyline draw for historical locations
   * @param locationData - location data object
   * @returns content
   */
  polylineMarker = locationData => {
    const locationPoints = [...locationData];
    const polyLines = [];

    while (locationPoints.length > 0) {
      // Array to store positions for next polyline
      const positions = [];
      // Make a copy of remaining location points
      const cachedLocationPoints = [...locationPoints];
      // Iterate the remaining cached locations
      for (let i = 0; i < cachedLocationPoints.length; i++) {
        positions.push([
          cachedLocationPoints[i].latitude,
          cachedLocationPoints[i].longitude,
        ]);
        const currentPoint = cachedLocationPoints[i];
        // Remove the current location from the locationPoints
        locationPoints.shift();
        if (i < cachedLocationPoints.length - 1) {
          const nextPoint = cachedLocationPoints[i + 1];
          // Draw a dashed line for long for location points with long interval
          if (
            nextPoint.timestamp - currentPoint.timestamp >
            this.props.context.geoMap.timeout * 1000
          ) {
            // Create a dashed line
            polyLines.push(
              <Polyline
                key={polyLines.length}
                color="#414042"
                positions={[
                  [currentPoint.latitude, currentPoint.longitude],
                  [nextPoint.latitude, nextPoint.longitude],
                ]}
                smoothFactor={10}
                weight={5}
                dashArray="7"
              />,
            );
            break;
          }
        }
      }
      // Create a polyline from provided positions
      polyLines.push(
        <Polyline
          key={polyLines.length}
          color="#414042"
          positions={positions}
          smoothFactor={10}
          weight={5}
        />,
      );
    }

    return (
      <div style={{ display: 'none' }}>
        {polyLines.map(polyLine => {
          return polyLine;
        })}
      </div>
    );
  };

  render() {
    const locationData = this.props.locationData;
    const config = this.props.context;
    const attribution = config.geoMap.attribution;
    const url = config.geoMap.url;
    const startingPoint = [locationData[0].latitude, locationData[0].longitude];
    const endPoint = [
      locationData[locationData.length - 1].latitude,
      locationData[locationData.length - 1].longitude,
    ];
    const zoom = config.geoMap.defaultZoomLevel;
    return (
      <div style={{ backgroundColor: '#ffffff', borderRadius: 5, padding: 5 }}>
        <Map center={startingPoint} zoom={zoom}>
          <TileLayer url={url} attribution={attribution} />
          <Fragment>
            {this.polylineMarker(locationData)}
            <Marker icon={pinStart} position={startingPoint}>
              <Popup keepInView={true}>Start</Popup>
              <Tooltip direction="top" permanent={true}>
                Start
              </Tooltip>
            </Marker>
            <Marker icon={pinEnd} position={endPoint}>
              <Tooltip direction="top" permanent={true}>
                End
              </Tooltip>
            </Marker>
          </Fragment>
        </Map>
      </div>
    );
  }
}

export default withConfigContext(CustomMap);
