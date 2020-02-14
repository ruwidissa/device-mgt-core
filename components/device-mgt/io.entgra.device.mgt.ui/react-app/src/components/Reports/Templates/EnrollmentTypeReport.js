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
import { PageHeader, Breadcrumb, Icon, Radio, Popover, Button } from 'antd';

import { Link } from 'react-router-dom';
import { withConfigContext } from '../../../context/ConfigContext';
import axios from 'axios';
import DateRangePicker from '../DateRangePicker';
import moment from 'moment';
import { Chart, Geom, Axis, Tooltip, Legend } from 'bizcharts';
import DataSet from '@antv/data-set';
import { handleApiError } from '../../../js/Utils';

class EnrollmentTypeReport extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
    this.state = {
      paramsObject: {
        from: moment()
          .subtract(6, 'days')
          .format('YYYY-MM-DD'),
        to: moment()
          .add(1, 'days')
          .format('YYYY-MM-DD'),
      },
      data: [],
      fields: [],
      durationMode: 'weekly',
      visible: false,
    };
  }

  componentDidMount() {
    this.fetchData();
  }

  handleDurationModeChange = e => {
    const durationMode = e.target.value;
    switch (durationMode) {
      case 'daily':
        this.updateDurationValue(
          moment().format('YYYY-MM-DD'),
          moment()
            .add(1, 'days')
            .format('YYYY-MM-DD'),
        );
        break;
      case 'weekly':
        this.updateDurationValue(
          moment()
            .subtract(6, 'days')
            .format('YYYY-MM-DD'),
          moment()
            .add(1, 'days')
            .format('YYYY-MM-DD'),
        );
        break;
      case 'monthly':
        this.updateDurationValue(
          moment()
            .subtract(29, 'days')
            .format('YYYY-MM-DD'),
          moment()
            .add(1, 'days')
            .format('YYYY-MM-DD'),
        );
        break;
    }
    this.setState({ durationMode });
  };

  handlePopoverVisibleChange = visible => {
    this.setState({ visible });
  };

  // Get modified value from datepicker and set it to paramsObject
  updateDurationValue = (modifiedFromDate, modifiedToDate) => {
    let tempParamObj = this.state.paramsObject;
    tempParamObj.from = modifiedFromDate;
    tempParamObj.to = modifiedToDate;
    this.setState({ paramsObject: tempParamObj });
    this.fetchData();
  };

  // Call count APIs and get count for given parameters, then create data object to build pie chart
  fetchData = () => {
    this.setState({ loading: true });

    const { paramsObject } = this.state;
    const config = this.props.context;

    const encodedExtraParams = Object.keys(paramsObject)
      .map(key => key + '=' + paramsObject[key])
      .join('&');

    axios
      .all([
        axios.get(
          window.location.origin +
            config.serverConfig.invoker.uri +
            config.serverConfig.invoker.deviceMgt +
            '/reports/count?ownership=BYOD&' +
            encodedExtraParams,
          'BYOD',
        ),
        axios.get(
          window.location.origin +
            config.serverConfig.invoker.uri +
            config.serverConfig.invoker.deviceMgt +
            '/reports/count?ownership=COPE&' +
            encodedExtraParams,
          'COPE',
        ),
      ])
      .then(res => {
        let keys = Object.keys(res[0].data.data);
        let byod = res[0].data.data;
        let cope = res[1].data.data;
        if (Object.keys(byod).length != 0) {
          byod.name = 'BYOD';
          cope.name = 'COPE';
        }

        const finalData = [byod, cope];

        this.setState({
          data: finalData,
          fields: keys,
        });
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to get device count.',
        );
      });
  };

  render() {
    const { durationMode } = this.state;

    const ds = new DataSet();
    const dv = ds.createView().source(this.state.data);
    dv.transform({
      type: 'fold',
      fields: this.state.fields,
      key: 'Time',
      value: 'Number of Devices',
    });

    return (
      <div>
        <PageHeader style={{ paddingTop: 0 }}>
          <Breadcrumb style={{ paddingBottom: 16 }}>
            <Breadcrumb.Item>
              <Link to="/entgra">
                <Icon type="home" /> Home
              </Link>
            </Breadcrumb.Item>
            <Breadcrumb.Item>
              <Link to="/entgra/reports">Reports</Link>
            </Breadcrumb.Item>
            <Breadcrumb.Item>Device Enrollment Type Report</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap" style={{ marginBottom: '10px' }}>
            <h3>Device Enrollment Type Report</h3>
            <Radio.Group
              onChange={this.handleDurationModeChange}
              defaultValue={'weekly'}
              value={durationMode}
              style={{ marginBottom: 8, marginRight: 5 }}
            >
              <Radio.Button value={'daily'}>Today</Radio.Button>
              <Radio.Button value={'weekly'}>Last Week</Radio.Button>
              <Radio.Button value={'monthly'}>Last Month</Radio.Button>
            </Radio.Group>

            <Popover
              trigger="hover"
              content={
                <div>
                  <DateRangePicker
                    updateDurationValue={this.updateDurationValue}
                  />
                </div>
              }
              visible={this.state.visible}
              onVisibleChange={this.handlePopoverVisibleChange}
            >
              <Button style={{ marginRight: 10 }}>Custom Date</Button>
            </Popover>

            <div
              style={{
                backgroundColor: '#ffffff',
                borderRadius: 5,
                marginTop: 10,
              }}
            >
              <Chart height={400} data={dv} forceFit>
                <Axis name="Time" />
                <Axis name="Number of Devices" />
                <Legend />
                <Tooltip
                  crosshairs={{
                    type: 'y',
                  }}
                />
                <Geom
                  type="interval"
                  position="Time*Number of Devices"
                  color={'name'}
                  adjust={[
                    {
                      type: 'dodge',
                      marginRatio: 1 / 32,
                    },
                  ]}
                />
              </Chart>
            </div>
          </div>
        </PageHeader>
        <div
          style={{ background: '#f0f2f5', padding: 24, minHeight: 720 }}
        ></div>
      </div>
    );
  }
}

export default withConfigContext(EnrollmentTypeReport);
