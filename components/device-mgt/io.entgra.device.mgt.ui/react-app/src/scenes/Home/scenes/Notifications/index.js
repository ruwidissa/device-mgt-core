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
import {
  message,
  notification,
  Button,
  PageHeader,
  Breadcrumb,
  Icon,
  Radio,
} from 'antd';
import { withConfigContext } from '../../../../components/ConfigContext';
import axios from 'axios';
import { Link } from 'react-router-dom';

import NotificationsTable from './Components/NotificationsTable';

class Notifications extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      visible: false,
      data: [],
      notificationType: 'all',
    };
  }

  handleModeChange = e => {
    const notificationType = e.target.value;
    this.setState({ notificationType });
  };

  clearNotifications = () => {
    const config = this.props.context;
    axios
      .put(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/notifications/clear-all',
        { 'Content-Type': 'application/json; charset=utf-8' },
      )
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Done',
            duration: 0,
            description: 'All notifications are cleared.',
          });
        }
      })
      .catch(error => {
        if (error.hasOwnProperty('response') && error.response.status === 401) {
          // todo display a popop with error
          message.error('You are not logged in');
          window.location.href = window.location.origin + '/entgra/login';
        } else {
          notification.error({
            message: 'There was a problem',
            duration: 0,
            description: 'Error occurred while trying to clear notifications.',
          });
        }
      });
  };

  render() {
    const { notificationType } = this.state;
    return (
      <div>
        <PageHeader style={{ paddingTop: 0 }}>
          <Breadcrumb style={{ paddingBottom: 16 }}>
            <Breadcrumb.Item>
              <Link to="/entgra">
                <Icon type="home" /> Home
              </Link>
            </Breadcrumb.Item>
            <Breadcrumb.Item>Notifications</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap" style={{ marginBottom: '10px' }}>
            <h3>DEVICE NOTIFICATIONS</h3>
            <Radio.Group
              onChange={this.handleModeChange}
              defaultValue={'all'}
              value={notificationType}
              style={{ marginBottom: 8, marginRight: 5 }}
            >
              <Radio.Button value={'all'}>All Notifications</Radio.Button>
              <Radio.Button value={'unread'}>Unread Notifications</Radio.Button>
            </Radio.Group>
            <Button
              type="primary"
              style={{
                marginRight: 10,
                marginBottom: 8,
                display: notificationType === 'unread' ? 'inline' : 'none',
              }}
              onClick={this.clearNotifications}
            >
              Clear All Notifications
            </Button>
            <div style={{ backgroundColor: '#ffffff', borderRadius: 5 }}>
              <NotificationsTable notificationType={notificationType} />
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

export default withConfigContext(Notifications);
