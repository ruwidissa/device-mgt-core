import React from 'react';
import {
  Button,
  Divider,
  Form,
  Icon,
  Input,
  message,
  Modal,
  notification,
  Popconfirm,
  Select,
  Tooltip,
  Typography,
} from 'antd';
import axios from 'axios';
import { withConfigContext } from '../../context/ConfigContext';
const { Option } = Select;
const { Text } = Typography;

class UserActions extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      isEditModalVisible: false,
      isResetPasswordModalVisible: false,
      rolesData: [],
    };
  }
  openEditModal = () => {
    this.setState({
      isEditModalVisible: true,
    });
    this.fetchRoles(this.props.data.username);
  };
  openPasswordResetModal = () => {
    this.setState({
      isResetPasswordModalVisible: true,
    });
  };

  onCancelHandler = () => {
    this.setState({
      isEditModalVisible: false,
      isResetPasswordModalVisible: false,
    });
  };

  compareToFirstPassword = (rule, value, callback) => {
    if (value && value !== this.props.form.getFieldValue('password')) {
      callback("New password doesn't match the confirmation.");
    } else {
      callback();
    }
  };

  onSavePassword = () => {
    this.props.form.validateFields(
      ['password', 'confirmPassword'],
      (err, values) => {
        if (!err) {
          this.onResetPassword(values);
        }
      },
    );
  };

  onResetPassword = value => {
    const password = {
      newPassword: value.password,
    };
    axios
      .post(
        window.location.origin +
          this.config.serverConfig.invoker.uri +
          this.config.serverConfig.invoker.deviceMgt +
          '/admin/users/' +
          this.props.data.username +
          '/credentials',
        password,
        { headers: { 'Content-Type': 'application-json' } },
      )
      .then(res => {
        if (res.status === 200) {
          this.props.fetchUsers();
          this.setState({
            isResetPasswordModalVisible: false,
          });
          notification.success({
            message: 'Done',
            duration: 4,
            description: 'Successfully reset the password',
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
            description: 'Error occurred while trying to reset password.',
          });
        }
      });
  };

  componentDidMount() {
    this.getRole();
  }

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

  onConfirmDeleteUser = () => {
    axios
      .delete(
        window.location.origin +
          this.config.serverConfig.invoker.uri +
          this.config.serverConfig.invoker.deviceMgt +
          '/users/' +
          this.props.data.username,
        { headers: { 'Content-Type': 'application/json' } },
      )
      .then(res => {
        if (res.status === 200) {
          this.props.fetchUsers();
          notification.success({
            message: 'Done',
            duration: 4,
            description: 'Successfully deleted the user.',
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
            description: 'Error occurred while trying to delete user.',
          });
        }
      });
  };

  fetchRoles = username => {
    let apiUrl =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      '/users/' +
      username +
      '/roles';

    axios
      .get(apiUrl)
      .then(res => {
        if (res.status === 200) {
          this.setState({
            rolesData: res.data.data.roles,
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

  handleEditOk = e => {
    this.props.form.validateFields(
      [
        'userStoreDomain',
        'userName',
        'firstName',
        'lastName',
        'email',
        'userRoles',
      ],
      (err, values) => {
        if (!err) {
          this.onUpdateUser(values);
        }
      },
    );
  };

  onUpdateUser = value => {
    const userData = {
      username: value.userStoreDomain + '/' + value.userName,
      firstname: value.firstName,
      lastname: value.lastName,
      emailAddress: value.email,
      roles: value.userRoles,
    };
    axios
      .put(
        window.location.origin +
          this.config.serverConfig.invoker.uri +
          this.config.serverConfig.invoker.deviceMgt +
          '/users/' +
          this.props.data.username,
        userData,
        { headers: { 'Content-Type': 'application-json' } },
      )
      .then(res => {
        if (res.status === 200) {
          this.props.fetchUsers();
          this.setState({
            isEditModalVisible: false,
          });
          notification.success({
            message: 'Done',
            duration: 4,
            description: 'Successfully updated the user.',
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
            description: 'Error occurred while trying to update user.',
          });
        }
      });
  };

  render() {
    const isAdminUser = this.props.data.username === 'admin';
    const { getFieldDecorator } = this.props.form;
    return (
      <div>
        <div style={{ display: isAdminUser ? 'none' : 'inline' }}>
          <Tooltip placement="top" title={'Edit User'}>
            <a>
              <Icon type="edit" onClick={this.openEditModal} />
            </a>
          </Tooltip>
          <Divider type="vertical" />
          <Tooltip placement="top" title={'Reset Password'}>
            <a>
              <Icon type="key" onClick={this.openPasswordResetModal} />
            </a>
          </Tooltip>
          <Divider type="vertical" />
          <Tooltip placement="bottom" title={'Remove User'}>
            <Popconfirm
              placement="top"
              title={'Are you sure?'}
              onConfirm={this.onConfirmDeleteUser}
              okText="Ok"
              cancelText="Cancel"
            >
              <a>
                <Text type="danger">
                  <Icon type="delete" />
                </Text>
              </a>
            </Popconfirm>
          </Tooltip>
        </div>

        <div>
          <Modal
            title="Reset Password"
            width="40%"
            visible={this.state.isResetPasswordModalVisible}
            onCancel={this.onCancelHandler}
            onOk={this.onSavePassword}
            footer={[
              <Button key="cancel" onClick={this.onCancelHandler}>
                Cancel
              </Button>,
              <Button key="submit" type="primary" onClick={this.onSavePassword}>
                Save
              </Button>,
            ]}
          >
            <div style={{ alignItems: 'center' }}>
              <Form labelCol={{ span: 6 }} wrapperCol={{ span: 17 }}>
                <Form.Item label="New Password" style={{ display: 'block' }}>
                  {getFieldDecorator('password', {
                    rules: [
                      {
                        required: true,
                        message: 'This field is required',
                      },
                    ],
                  })(<Input.Password />)}
                </Form.Item>
                <Form.Item
                  label="Retype New Password"
                  style={{ display: 'block' }}
                >
                  {getFieldDecorator('confirmPassword', {
                    rules: [
                      {
                        required: true,
                        message: 'This field is required',
                      },
                      {
                        validator: this.compareToFirstPassword,
                      },
                    ],
                  })(<Input.Password />)}
                </Form.Item>
              </Form>
            </div>
          </Modal>
        </div>

        <div>
          <Modal
            title="EDIT USER"
            width="40%"
            visible={this.state.isEditModalVisible}
            onOk={this.handleEditOk}
            onCancel={this.onCancelHandler}
            footer={[
              <Button key="cancel" onClick={this.onCancelHandler}>
                Cancel
              </Button>,
              <Button key="submit" type="primary" onClick={this.handleEditOk}>
                Update
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
                    <Select disabled={true}>
                      <Option key="PRIMARY">PRIMARY</Option>
                    </Select>,
                  )}
                </Form.Item>
                <Form.Item label="User Name" style={{ display: 'block' }}>
                  {getFieldDecorator('userName', {
                    initialValue: this.props.data.username,
                    rules: [
                      {
                        required: true,
                        message:
                          'This field is required. Username should be at least 3 characters long with no white spaces.',
                      },
                    ],
                  })(<Input disabled={true} />)}
                </Form.Item>
                <Form.Item label="First Name" style={{ display: 'block' }}>
                  {getFieldDecorator('firstName', {
                    initialValue: this.props.data.firstname,
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
                    initialValue: this.props.data.lastname,
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
                    initialValue: this.props.data.emailAddress,
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
                  {getFieldDecorator('userRoles', {
                    initialValue: this.state.rolesData,
                  })(
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

export default withConfigContext(
  Form.create({ name: 'user-actions' })(UserActions),
);
