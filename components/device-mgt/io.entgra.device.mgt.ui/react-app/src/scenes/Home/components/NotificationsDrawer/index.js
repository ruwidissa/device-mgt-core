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
import { Drawer, message, notification, Card, Button } from 'antd';
import { withConfigContext } from '../../../../components/ConfigContext';
import axios from 'axios';
import { Link } from 'react-router-dom';

// eslint-disable-next-line no-unused-vars
let config = null;

class NotificationsDrawer extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
    config = this.props.context;
    this.state = {
      visible: false,
      data: [],
    };
  }

  showDrawer = () => {
    this.setState({
      visible: true,
    });
    this.fetchNotifications();
  };

  onClose = () => {
    this.setState({
      visible: false,
    });
  };

  fetchNotifications = () => {
    const config = this.props.context;
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/notifications',
      )
      .then(res => {
        if (res.status === 200) {
          this.setState({ data: res.data.data.notifications });
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
            description:
              'Error occurred while trying to load notifications list.',
          });
        }
      });
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
    this.setState({
      visible: false,
    });
  };

  render() {
    const { data } = this.state;
    const notificationItemList = data.map(data => (
      <Card
        key={data.id}
        bordered={true}
        hoverable={true}
        style={{ width: 350 }}
      >
        <h3>{data.deviceType + ':' + data.deviceName}t</h3>
        <p>{data.description}</p>
      </Card>
    ));
    return (
      <div>
        <div onClick={this.showDrawer}>
          <div>
            <span>Notifications</span>
          </div>
        </div>
        <Drawer
          title="Notifications"
          placement="right"
          closable={false}
          onClose={this.onClose}
          visible={this.state.visible}
          width={400}
        >
          <Link to="/entgra/notifications">
            <Button
              type="primary"
              style={{ marginRight: 10, marginBottom: 10 }}
              onClick={this.onClose}
            >
              Show All Notifications
            </Button>
          </Link>
          <Button
            type="primary"
            style={{ marginRight: 10, marginBottom: 10 }}
            onClick={this.clearNotifications}
          >
            Clear All Notifications
          </Button>
          {notificationItemList}
        </Drawer>
      </div>
    );
  }
}

export default withConfigContext(NotificationsDrawer);
