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

import React from 'react';
import moment from 'moment';
import { message, notification, Empty, DatePicker } from 'antd';
import axios from 'axios';
import { withConfigContext } from '../../../../../../components/ConfigContext';
import GeoCustomMap from '../CustomMap';
import './styles.css';

class GeoDashboard extends React.Component {
  constructor(props) {
    super(props);
    let start = moment(
      new Date(
        new Date().getFullYear(),
        new Date().getMonth(),
        new Date().getDate(),
        0,
        0,
        0,
        0,
      ),
    );
    let end = moment(start)
      .add(1, 'days')
      .subtract(1, 'seconds');
    this.state = {
      deviceData: [],
      selectedDevice: '',
      locationHistorySnapshots: [],
      loading: false,
      start: start,
      end: end,
      buttonTooltip: 'Fetch Locations',
    };
  }

  componentDidMount() {
    this.fetchLocationHistory();
  }
  /**
   * Call back on apply button in the date time picker
   * @param startDate - start date
   * @param endDate - end date
   */
  applyCallback = (dates, dateStrings) => {
    this.setState({
      start: dateStrings[0],
      end: dateStrings[1],
    });
  };

  fetchLocationHistory = () => {
    const toInMills = moment(this.state.end);
    const fromInMills = moment(this.state.start);
    const config = this.props.context;
    this.setState({ loading: true });

    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/devices/' +
          this.props.deviceType +
          '/' +
          this.props.deviceIdentifier +
          '/location-history?' +
          'from=' +
          fromInMills +
          '&to=' +
          toInMills,
      )
      .then(res => {
        if (res.status === 200) {
          this.setState({
            loading: false,
            locationHistorySnapshots: res.data.data.locationHistorySnapshots,
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

        this.setState({ loading: false });
        console.log(error);
      });
  };

  /**
   * Geo Dashboard controller
   */
  controllerBar = () => {
    const { RangePicker } = DatePicker;
    let now = new Date();
    let start = moment(
      new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0),
    );
    let end = moment(start)
      .add(1, 'days')
      .subtract(1, 'seconds');
    let ranges = {
      'Today Only': [moment(start), moment(end)],
      'Yesterday Only': [
        moment(start).subtract(1, 'days'),
        moment(end).subtract(1, 'days'),
      ],
      '3 Days': [moment(start).subtract(3, 'days'), moment(end)],
      '5 Days': [moment(start).subtract(5, 'days'), moment(end)],
      '1 Week': [moment(start).subtract(7, 'days'), moment(end)],
      '2 Weeks': [moment(start).subtract(14, 'days'), moment(end)],
      '1 Month': [moment(start).subtract(1, 'months'), moment(end)],
    };

    return (
      <div className="controllerDiv">
        <RangePicker
          ranges={ranges}
          style={{ marginRight: 20, width: 400 }}
          showTime
          format="YYYY-MM-DD HH:mm:ss"
          defaultValue={[this.state.start, this.state.end]}
          onChange={this.applyCallback}
          onOk={this.fetchLocationHistory}
          size="large"
        />
      </div>
    );
  };

  render() {
    const { locationHistorySnapshots } = this.state;
    return (
      <div className="container">
        {this.controllerBar()}
        {locationHistorySnapshots.length > 0 ? (
          <GeoCustomMap locationHistorySnapshots={locationHistorySnapshots} />
        ) : (
          <Empty />
        )}
      </div>
    );
  }
}

export default withConfigContext(GeoDashboard);
