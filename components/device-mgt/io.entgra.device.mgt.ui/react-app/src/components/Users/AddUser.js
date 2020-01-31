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
  Button,
  Form,
  Select,
  Input,
  message,
  Modal,
  notification,
} from 'antd';
import axios from 'axios';
import { withConfigContext } from '../../context/ConfigContext';
const { Option } = Select;

class AddUser extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      isModalVisible: false,
      roles: [],
    };
  }

  componentDidMount() {
    this.getRole();
  }

  openAddModal = () => {
    this.setState({
      isModalVisible: true,
    });
  };

  onSubmitHandler = e => {
    this.props.form.validateFields((err, values) => {
      if (!err) {
        this.onConfirmAddUser(values);
      }
    });
  };

  onConfirmAddUser = value => {
    const userData = {
      username: value.userStoreDomain + '/' + value.userName,
      firstname: value.firstName,
      lastname: value.lastName,
      emailAddress: value.email,
      roles: value.userRoles,
    };
    axios
      .post(
        window.location.origin +
          this.config.serverConfig.invoker.uri +
          this.config.serverConfig.invoker.deviceMgt +
          '/users',
        userData,
        { headers: { 'Content-Type': 'application-json' } },
      )
      .then(res => {
        if (res.status === 201) {
          this.props.fetchUsers();
          this.setState({
            isModalVisible: false,
          });
          notification.success({
            message: 'Done',
            duration: 4,
            description: 'Successfully added the user.',
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
            description: 'Error occurred while trying to add user.',
          });
        }
      });
  };

  onCancelHandler = e => {
    this.setState({
      isModalVisible: false,
    });
  };

  getRole = () => {
    let apiURL =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      '/roles?user-store=PRIMARY&limit=100';

    axios
      .get(apiURL)
      .then(res => {
        if (res.status === 200) {
          const roles = [];
          for (let i = 0; i < res.data.data.roles.length; i++) {
            roles.push(
              <Option key={res.data.data.roles[i]}>
                {res.data.data.roles[i]}
              </Option>,
            );
          }
          this.setState({
            roles: roles,
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

  render() {
    const { getFieldDecorator } = this.props.form;
    return (
      <div>
        <div>
          <Button
            type="primary"
            icon="plus"
            size={'default'}
            onClick={this.openAddModal}
            style={{ marginBottom: '10px' }}
          >
            Add User
          </Button>
        </div>
        <div>
          <Modal
            title="ADD NEW USER"
            width="40%"
            visible={this.state.isModalVisible}
            onOk={this.onSubmitHandler}
            onCancel={this.onCancelHandler}
            footer={[
              <Button key="cancel" onClick={this.onCancelHandler}>
                Cancel
              </Button>,
              <Button
                key="submit"
                type="primary"
                onClick={this.onSubmitHandler}
              >
                Submit
              </Button>,
            ]}
          >
            <div style={{ alignItems: 'center' }}>
              <p>Create new user on IoT Server.</p>
              <Form labelCol={{ span: 5 }} wrapperCol={{ span: 18 }}>
                <Form.Item
                  label="User Store Domain"
                  style={{ display: 'block' }}
                >
                  {getFieldDecorator('userStoreDomain', {
                    initialValue: 'PRIMARY',
                  })(
                    <Select>
                      <Option key="PRIMARY">PRIMARY</Option>
                    </Select>,
                  )}
                </Form.Item>
                <Form.Item label="User Name" style={{ display: 'block' }}>
                  {getFieldDecorator('userName', {
                    rules: [
                      {
                        required: true,
                        message:
                          'This field is required. Username should be at least 3 characters long with no white spaces.',
                      },
                    ],
                  })(<Input />)}
                </Form.Item>
                <Form.Item label="First Name" style={{ display: 'block' }}>
                  {getFieldDecorator('firstName', {
                    rules: [
                      {
                        required: true,
                        message: 'This field is required',
                      },
                    ],
                  })(<Input />)}
                </Form.Item>
                <Form.Item label="Last Name" style={{ display: 'block' }}>
                  {getFieldDecorator('lastName', {
                    rules: [
                      {
                        required: true,
                        message: 'This field is required',
                      },
                    ],
                  })(<Input />)}
                </Form.Item>
                <Form.Item label="Email Address" style={{ display: 'block' }}>
                  {getFieldDecorator('email', {
                    rules: [
                      {
                        type: 'email',
                        message: 'Invalid Email Address',
                      },
                      {
                        required: true,
                        message: 'This field is required',
                      },
                    ],
                  })(<Input />)}
                </Form.Item>
                <Form.Item label="User Roles" style={{ display: 'block' }}>
                  {getFieldDecorator('userRoles', {})(
                    <Select mode="multiple" style={{ width: '100%' }}>
                      {this.state.roles}
                    </Select>,
                  )}
                </Form.Item>
              </Form>
            </div>
          </Modal>
        </div>
      </div>
    );
  }
}

export default withConfigContext(Form.create({ name: 'add-user' })(AddUser));
