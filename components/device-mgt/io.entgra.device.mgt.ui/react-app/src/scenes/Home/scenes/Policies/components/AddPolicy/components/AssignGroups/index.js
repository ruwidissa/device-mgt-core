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
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import { Button, Col, Form, message, notification, Radio, Select } from 'antd';
import axios from 'axios';
const { Option } = Select;

class AssignGroups extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.userSelector = React.createRef();
    this.roleSelector = React.createRef();
    this.state = {
      roles: [],
      users: [],
      groups: [],
    };
  }
  componentDidMount() {
    this.getRolesList();
    this.getGroupsList();
  }

  handleSetUserRoleFormItem = event => {
    if (event.target.value === 'roleSelector') {
      this.roleSelector.current.style.cssText = 'display: block;';
      this.userSelector.current.style.cssText = 'display: none;';
    } else {
      this.roleSelector.current.style.cssText = 'display: none;';
      this.userSelector.current.style.cssText = 'display: block;';
    }
  };

  getRolesList = () => {
    let apiURL =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      '/roles?user-store=PRIMARY&limit=100';

    axios
      .get(apiURL)
      .then(res => {
        if (res.status === 200) {
          this.setState({
            roles: res.data.data.roles,
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
            description: 'Error occurred while trying to load roles.',
          });
        }
      });
  };

  getUsersList = value => {
    let apiURL =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      '/users/search/usernames?filter=' +
      value +
      '&domain=Primary';
    axios
      .get(apiURL)
      .then(res => {
        if (res.status === 200) {
          let users = JSON.parse(res.data.data);
          this.setState({
            users,
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
            description: 'Error occurred while trying to load users.',
          });
        }
      });
  };

  // fetch data from api
  getGroupsList = () => {
    let apiUrl =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      '/admin/groups';

    // send request to the invokerss
    axios
      .get(apiUrl)
      .then(res => {
        if (res.status === 200) {
          this.setState({
            groups: res.data.data.deviceGroups,
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
            description: 'Error occurred while trying to load device groups.',
          });
        }
      });
  };

  render() {
    const { getFieldDecorator } = this.props.form;
    return (
      <div>
        <div>
          <Radio.Group
            defaultValue={'roleSelector'}
            onChange={this.handleSetUserRoleFormItem}
          >
            <Radio value="roleSelector">Set User role(s)</Radio>
            <Radio value="userSelector">Set User(s)</Radio>
          </Radio.Group>
          <div
            id={'roleSelector'}
            ref={this.roleSelector}
            style={{ display: 'block' }}
          >
            <Form.Item>
              {getFieldDecorator('roles', {})(
                <Select
                  mode="multiple"
                  style={{ width: '100%' }}
                  defaultActiveFirstOption={true}
                >
                  <Option value={'ANY'}>Any</Option>
                  {this.state.roles.map(role => (
                    <Option key={role} value={role}>
                      {role}
                    </Option>
                  ))}
                </Select>,
              )}
            </Form.Item>
          </div>
          <div
            id={'userSelector'}
            ref={this.userSelector}
            style={{ display: 'none' }}
          >
            <Form.Item>
              {getFieldDecorator('users', {})(
                <Select
                  mode="multiple"
                  style={{ width: '100%' }}
                  onSearch={this.getUsersList}
                >
                  {this.state.users.map(user => (
                    <Option key={user.username} value={user.username}>
                      {user.username}
                    </Option>
                  ))}
                </Select>,
              )}
            </Form.Item>
          </div>
        </div>
        <Form.Item label={'Select Groups'} style={{ display: 'block' }}>
          {getFieldDecorator('deviceGroups', {})(
            <Select mode="multiple" style={{ width: '100%' }}>
              <Option value={'NONE'}>NONE</Option>
              {this.state.groups.map(group => (
                <Option key={group.name} value={group.name}>
                  {group.name}
                </Option>
              ))}
            </Select>,
          )}
        </Form.Item>
        <Col span={16} offset={20}>
          <div style={{ marginTop: 24 }}>
            <Button style={{ marginRight: 8 }} onClick={this.props.getPrevStep}>
              Back
            </Button>
            <Button type="primary" onClick={this.props.getNextStep}>
              Continue
            </Button>
          </div>
        </Col>
      </div>
    );
  }
}

export default withConfigContext(Form.create()(AssignGroups));
