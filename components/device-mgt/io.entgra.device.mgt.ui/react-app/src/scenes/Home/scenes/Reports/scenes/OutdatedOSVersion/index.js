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
import axios from 'axios';
import {
  PageHeader,
  Breadcrumb,
  Icon,
  Button,
  Select,
  message,
  notification,
} from 'antd';

import { Link } from 'react-router-dom';
import { withConfigContext } from '../../../../../../components/ConfigContext';
import DeviceTable from '../../components/DevicesTable';

let config = null;
const { Option } = Select;
let { typeSelected, versionSelected } = false;

class OutdatedOSversionReport extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
    config = this.props.context;

    this.state = {
      deviceType: null,
      osVersion: null,
      apiURL: null,
      visible: false,
      supportedOsVersions: [],
    };
  }

  onDeviceTypeChange = value => {
    typeSelected = true;
    this.setState({ deviceType: value });
    this.getSupportedOsVersions(value);
  };

  onOSVersionChange = value => {
    versionSelected = true;
    this.setState({ osVersion: value });
  };

  onClickGenerateButton = () => {
    const { osVersion, deviceType } = this.state;
    if (osVersion && deviceType != null) {
      let apiURL =
        window.location.origin +
        config.serverConfig.invoker.uri +
        config.serverConfig.invoker.deviceMgt +
        `/reports/expired-devices/${deviceType}?osVersion=${osVersion}&`;
      this.setState({ apiURL });
    }
  };

  getSupportedOsVersions = deviceType => {
    const config = this.props.context;
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          `/admin/device-types/${deviceType}/versions`,
      )
      .then(res => {
        if (res.status === 200) {
          let supportedOsVersions = JSON.parse(res.data.data);
          this.setState({
            supportedOsVersions,
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
            description: 'Error occurred while trying to load OS Versions.',
          });
        }
      });
  };

  render() {
    const { apiURL, supportedOsVersions } = this.state;
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
            <Breadcrumb.Item>Outdated OS Version</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap" style={{ marginBottom: '10px' }}>
            <h3>Outdated OS Version Report</h3>
            <p>
              This report displays devices with an OS Version below the
              specified version.
            </p>
            <div className="wrap" style={{ marginBottom: '10px' }}>
              <Select
                showSearch
                style={{ width: 200, marginLeft: '10px' }}
                placeholder="Select a Device type"
                onChange={this.onDeviceTypeChange}
              >
                {config.supportedOStypes.map(data => (
                  <Option value={data.name} key={data.id}>
                    {data.name}
                  </Option>
                ))}
              </Select>
              <Select
                style={{ width: 200, marginLeft: '10px' }}
                placeholder="
                Select an OS Version"
                onChange={this.onOSVersionChange}
              >
                {supportedOsVersions.map(data => {
                  return (
                    <Option key={data.versionName}>{data.versionName}</Option>
                  );
                })}
              </Select>
              {typeSelected && versionSelected ? (
                <Button
                  type="primary"
                  onClick={this.onClickGenerateButton}
                  style={{ marginLeft: '10px' }}
                >
                  Generate Report
                </Button>
              ) : null}
            </div>
            <div
              id="table"
              style={{ backgroundColor: '#ffffff', borderRadius: 5 }}
            >
              <DeviceTable apiUrl={apiURL} />
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

export default withConfigContext(OutdatedOSversionReport);
