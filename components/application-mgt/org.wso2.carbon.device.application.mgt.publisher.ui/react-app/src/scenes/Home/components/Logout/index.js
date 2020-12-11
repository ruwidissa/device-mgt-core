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
import { LogoutOutlined } from '@ant-design/icons';
import { notification, Menu } from 'antd';
import axios from 'axios';
import { withConfigContext } from '../../../../components/ConfigContext';
import { getUiConfig } from '../../../../services/utils/uiConfigHandler';

/*
This class for call the logout api by sending request
 */
class Logout extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      inValid: false,
      loading: false,
    };
  }
  /*
    This function call the logout api when the request is success
     */
  handleSubmit = () => {
    const thisForm = this;
    const config = this.props.context;

    thisForm.setState({
      inValid: false,
    });

    let logoutUri;
    getUiConfig(config).then(uiConfig => {
      if (uiConfig !== undefined) {
        if (uiConfig.isSsoEnable) {
          logoutUri = window.location.origin + config.serverConfig.ssoLogoutUri;
        } else {
          logoutUri = window.location.origin + config.serverConfig.logoutUri;
        }
        axios
          .post(logoutUri)
          .then(res => {
            // if the api call status is correct then user
            // will logout and then it goes to login page
            if (res.status === 200) {
              window.location =
                window.location.origin + `/${config.appName}/login`;
            }
          })
          .catch(function(error) {
            notification.error({
              message: 'There was a problem',
              duration: 0,
              description: 'Error occurred while trying to logout.',
            });
          });
      } else {
        this.setState({
          loading: false,
          error: true,
        });
      }
    });
  };

  render() {
    return (
      <Menu>
        <Menu.Item key="1" onClick={this.handleSubmit}>
          <LogoutOutlined />
          Logout
        </Menu.Item>
      </Menu>
    );
  }
}

export default withConfigContext(Logout);
