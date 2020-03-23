/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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
import { Modal, Spin, Tabs } from 'antd';
import UserInstall from './components/UserInstall';
import GroupInstall from './components/GroupInstall';
import RoleInstall from './components/RoleInstall';
import DeviceInstall from './components/DeviceInstall';

const { TabPane } = Tabs;

class Install extends React.Component {
  state = {
    data: [],
  };

  render() {
    const { deviceType } = this.props;
    return (
      <div>
        <Modal
          title="Install App"
          visible={this.props.visible}
          onCancel={this.props.onClose}
          footer={null}
        >
          <Spin spinning={this.props.loading}>
            <Tabs defaultActiveKey="device">
              <TabPane tab="Device" key="device">
                <DeviceInstall
                  deviceType={deviceType}
                  onInstall={this.props.onInstall}
                />
              </TabPane>
              <TabPane tab="User" key="user">
                <UserInstall onInstall={this.props.onInstall} />
              </TabPane>
              <TabPane tab="Role" key="role">
                <RoleInstall onInstall={this.props.onInstall} />
              </TabPane>
              <TabPane tab="Group" key="group">
                <GroupInstall onInstall={this.props.onInstall} />
              </TabPane>
            </Tabs>
          </Spin>
        </Modal>
      </div>
    );
  }
}

export default Install;
