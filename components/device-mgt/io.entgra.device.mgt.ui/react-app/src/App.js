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
import 'antd/dist/antd.less';
import RouteWithSubRoutes from './components/RouteWithSubRoutes';
import { BrowserRouter as Router, Redirect, Switch } from 'react-router-dom';
import axios from 'axios';
import { Layout, Spin, Result, notification } from 'antd';
import ConfigContext from './components/ConfigContext';

const { Content } = Layout;
const loadingView = (
  <Layout>
    <Content
      style={{
        padding: '0 0',
        paddingTop: 300,
        backgroundColor: '#fff',
        textAlign: 'center',
      }}
    >
      <Spin tip="Loading..." />
    </Content>
  </Layout>
);

const errorView = (
  <Result
    style={{
      paddingTop: 200,
    }}
    status="500"
    title="Error occurred while loading the configuration"
    subTitle="Please refresh your browser window"
  />
);

class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      loading: true,
      error: false,
      config: {},
    };
  }

  componentDidMount() {
    axios
      .get(window.location.origin + '/entgra/public/conf/config.json')
      .then(res => {
        const config = res.data;
        this.checkUserLoggedIn(config);
      })
      .catch(error => {
        this.setState({
          loading: false,
          error: true,
        });
      });
  }

  checkUserLoggedIn = config => {
    axios
      .post(
        window.location.origin + '/entgra-ui-request-handler/user',
        'platform=entgra',
      )
      .then(res => {
        config.user = res.data.data;
        const pageURL = window.location.pathname;
        const lastURLSegment = pageURL.substr(pageURL.lastIndexOf('/') + 1);
        if (lastURLSegment === 'login') {
          window.location.href = window.location.origin + '/entgra/';
        } else {
          this.getDeviceTypes(config);
        }
      })
      .catch(error => {
        if (error.hasOwnProperty('response') && error.response.status === 401) {
          const redirectUrl = encodeURI(window.location.href);
          const pageURL = window.location.pathname;
          const lastURLSegment = pageURL.substr(pageURL.lastIndexOf('/') + 1);
          if (lastURLSegment !== 'login') {
            window.location.href =
              window.location.origin + `/entgra/login?redirect=${redirectUrl}`;
          } else {
            this.setState({
              loading: false,
              config: config,
            });
          }
        } else {
          this.setState({
            loading: false,
            error: true,
          });
        }
      });
  };

  getDeviceTypes = config => {
    axios
      .get(
        window.location.origin +
          '/entgra-ui-request-handler/invoke/device-mgt/v1.0/device-types',
      )
      .then(res => {
        config.deviceTypes = JSON.parse(res.data.data);
        config.supportedOStypes = [];
        config.deviceTypes.forEach(type => {
          if (['ios', 'android'].includes(type.name)) {
            config.supportedOStypes.push(type);
          }
        });
        this.setState({
          config: config,
          loading: false,
        });
      })
      .catch(error => {
        notification.error({
          message: 'There was a problem',
          duration: 0,
          description: 'Error occurred while trying to load device types.',
        });
      });
  };

  render() {
    const { loading, error } = this.state;
    const applicationView = (
      <Router>
        <ConfigContext.Provider value={this.state.config}>
          <div>
            <Switch>
              <Redirect exact from="/entgra" to="/entgra/reports" />
              {this.props.routes.map(route => (
                <RouteWithSubRoutes key={route.path} {...route} />
              ))}
            </Switch>
          </div>
        </ConfigContext.Provider>
      </Router>
    );

    return (
      <div>
        {loading && loadingView}
        {!loading && !error && applicationView}
        {error && errorView}
      </div>
    );
  }
}

export default App;
