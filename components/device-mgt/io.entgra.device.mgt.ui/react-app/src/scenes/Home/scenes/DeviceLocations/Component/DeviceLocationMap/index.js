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

import React, { Component } from 'react';
import { Map, TileLayer, Marker, Popup, CircleMarker } from 'react-leaflet';
import L from 'leaflet';
import { withConfigContext } from '../../../../../../components/ConfigContext';
import axios from 'axios';
import { message, notification } from 'antd';

// const { Title, Text } = Typography;

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

class DeviceLocationMap extends Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.customMap = React.createRef();
    this.state = {
      defaultZoomLevel: 3,
      locations: [],
      deviceData: {},
      minLatitute: -37.43997405227057,
      maxLatitute: 37.43997405227057,
      minLongtitute: -151.171875,
      maxLongtitute: 151.34765625,
      zoomLevel: 3,
      deviceName: null,
      isPopup: false,
    };
  }

  componentDidMount() {
    this.getDeviceLocations(
      -37.43997405227057,
      37.43997405227057,
      -151.171875,
      151.34765625,
      3,
    );
  }

  getDeviceLocations = (
    minLatitute,
    maxLatitute,
    minLongtitute,
    maxLongtitute,
    zoomLevel,
  ) => {
    axios
      .get(
        window.location.origin +
          this.config.serverConfig.invoker.uri +
          this.config.serverConfig.invoker.deviceMgt +
          '/geo-services/1.0.0/stats' +
          '/device-locations?' +
          'minLat=' +
          minLatitute +
          '&maxLat=' +
          maxLatitute +
          '&minLong=' +
          minLongtitute +
          '&maxLong=' +
          maxLongtitute +
          '&zoom=' +
          zoomLevel,
      )
      .then(res => {
        if (res.status === 200) {
          // console.log(JSON.parse(res.data.data));
          this.setState({
            locations: JSON.parse(res.data.data),
          });
        }
      })
      .catch(error => {
        if (error.hasOwnProperty('response') && error.response.status === 401) {
          message.error('You are not logged in');
          window.location.href = window.location.origin + '/entgra/login';
        } else {
          notification.error({
            message: 'There was a problem',
            duration: 0,
            description: 'Error occurred while trying to fetch locations......',
          });
        }
      });
  };

  setMarkers = positions => {
    let markers = [];
    positions.map(location => {
      if (location.count > 1) {
        markers.push(
          <CircleMarker
            key={location.deviceIdentification}
            center={[
              location.coordinates.latitude,
              location.coordinates.longitude,
            ]}
            radius={10}
            fillOpacity={1}
            color={'#c0392b'}
            stroke={false}
          >
            <Popup>{location.count}</Popup>
          </CircleMarker>,
        );
      } else {
        markers.push(
          <Marker
            key={location.deviceIdentification}
            icon={pinStart}
            position={[
              location.coordinates.latitude,
              location.coordinates.longitude,
            ]}
            onMouseOver={this
              .mouseOver
              // location.deviceType,
              // location.deviceIdentification,
              ()}
            onMouseOut={this.mouseOut}
          >
            <Popup
              autoClose={true}
              style={{ display: this.state.isPopup ? 'block' : 'none' }}
            >
              {/* {this.getPopupData(*/}
              {/* // location.deviceType, // location.deviceIdentification, // )}*/}
              {/* {this.state.deviceData.name}*/}
              {this.state.deviceName}
            </Popup>
          </Marker>,
        );
      }
    });

    return (
      <div>
        {markers.map(markker => {
          return markker;
        })}
      </div>
    );
  };

  mouseOut = () => {
    this.setState({
      isPopup: false,
    });
  };

  // todo get  device details when mouse over the maker
  mouseOver = (deviceType, deviceIdentification) => {
    // let aaa = null;
    // axios
    //   .get(
    //     window.location.origin +
    //       this.config.serverConfig.invoker.uri +
    //       this.config.serverConfig.invoker.deviceMgt +
    //       `/devices/1.0.0/${deviceType}/${deviceIdentification}`,
    //   )
    //   .then(res => {
    //     if (res.status === 200) {
    //       // console.log(res.data.data);
    //       this.setState({
    //         deviceData: res.data.data,
    //         isPopup: true,
    //       });
    //       // aaa = res.data.data;
    //       // return (
    //       //   <div>
    //       //     <Title>{aaa.name}</Title>
    //       //     <Row>
    //       //       <Text> Type : {aaa.type}</Text>
    //       //     </Row>
    //       //     <Row>
    //       //       <Text> Status : {aaa.enrolmentInfo.status}</Text>
    //       //     </Row>
    //       //     <Row>
    //       //       <Text> Owner : {aaa.enrolmentInfo.owner}</Text>
    //       //     </Row>
    //       //   </div>
    //       // );
    //     }
    //   })
    //   .catch(error => {
    //     if (error.hasOwnProperty('response') && error.response.status === 401) {
    //       message.error('You are not logged in');
    //       window.location.href = window.location.origin + '/entgra/login';
    //     } else {
    //       notification.error({
    //         message: 'There was a problem',
    //         duration: 0,
    //         description: 'Error occurred while trying to fetch locations......',
    //       });
    //     }
    //   });
  };

  // Todo : get bound and zoom level when doing movement and pass id to getDeviceLocations function
  handleMovements = event => {
    console.log(this.customMap.current.leafletElement.getZoom());
  };

  render() {
    const position = [this.state.lat, this.state.lng];
    const attribution = this.config.geoMap.attribution;
    const url = this.config.geoMap.url;
    return (
      <div style={{ backgroundColor: '#ffffff', borderRadius: 5, padding: 5 }}>
        <Map
          ref={this.customMap}
          center={position}
          zoom={this.state.defaultZoomLevel}
          resize={this.onResize}
          onMoveEnd={this.handleMovements}
        >
          <TileLayer url={url} attribution={attribution} />
          {this.setMarkers(this.state.locations)}
        </Map>
      </div>
    );
  }
}

export default withConfigContext(DeviceLocationMap);
