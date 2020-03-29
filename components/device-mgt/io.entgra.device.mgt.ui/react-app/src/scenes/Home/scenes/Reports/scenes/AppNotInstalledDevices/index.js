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
import { PageHeader, Breadcrumb, Icon, Button } from 'antd';

import { Link } from 'react-router-dom';
import { withConfigContext } from '../../../../../../components/ConfigContext';

import AppListDropDown from './components/AppListDropDown';
import DevicesTable from './../../components/DevicesTable';
import AppVersionDropDown from './components/AppVersionDropDown';

// eslint-disable-next-line no-unused-vars
let config = null;
let url;
let packageName;
let version;

class AppNotInstalledDevicesReport extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
    config = this.props.context;
    this.state = {
      apiUrl: null,
      visible: false,
      packageName: null,
      version: 'all',
    };
  }

  getAppList = appPackageName => {
    packageName = appPackageName;
    this.setState({ packageName });
  };

  getVersion = appVersion => {
    version = appVersion;
    this.setState({ version });
  };

  onClickGenerateButton = () => {
    const { packageName, version } = this.state;
    if (version === 'all') {
      url =
        window.location.origin +
        config.serverConfig.invoker.uri +
        config.serverConfig.invoker.deviceMgt +
        '/reports/devices/android/' +
        packageName +
        '/not-installed?';
    } else {
      url =
        window.location.origin +
        config.serverConfig.invoker.uri +
        config.serverConfig.invoker.deviceMgt +
        '/reports/devices/android/' +
        packageName +
        '/not-installed?app-version=' +
        version +
        '&';
    }
    this.setState({ apiUrl: url });
  };

  render() {
    const { apiUrl, packageName } = this.state;
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
            <Breadcrumb.Item>App NOT Installed Devices Report</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap" style={{ marginBottom: '10px' }}>
            <h3>Policy Report</h3>

            <div style={{ display: 'flex', marginBottom: '10px' }}>
              <div>
                <AppListDropDown getAppList={this.getAppList} />
              </div>

              <div style={{ marginLeft: '10px' }}>
                <AppVersionDropDown
                  getVersion={this.getVersion}
                  packageName={packageName}
                />
              </div>

              <Button
                type="primary"
                onClick={this.onClickGenerateButton}
                style={{ marginLeft: '10px' }}
              >
                Generate Report
              </Button>
            </div>
            <div style={{ backgroundColor: '#ffffff', borderRadius: 5 }}>
              <DevicesTable apiUrl={apiUrl} />
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

export default withConfigContext(AppNotInstalledDevicesReport);
