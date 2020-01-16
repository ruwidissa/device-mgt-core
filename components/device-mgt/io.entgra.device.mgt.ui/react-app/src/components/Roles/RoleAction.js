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
  Tree,
  Typography,
} from 'antd';
import axios from 'axios';
import { withConfigContext } from '../../context/ConfigContext';
const { Option } = Select;
const { Text } = Typography;
const { TreeNode } = Tree;

class RoleAction extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.selected = [];
    this.expandKeys = [];
    this.state = {
      roleData: [],
      nodeList: [],
      isNodeList: false,
      users: [],
      isEditRoleModalVisible: false,
      isEditPermissionModalVisible: false,
      expandedKeys: [],
      autoExpandParent: true,
      checkedKeys: [],
    };
  }

  openEditRoleModal = () => {
    let apiUrl =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      '/roles/' +
      this.props.data;

    axios
      .get(apiUrl)
      .then(res => {
        if (res.status === 200) {
          this.setState({
            roleData: res.data.data,
            isEditRoleModalVisible: true,
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
            description: 'Error occurred while trying to load role.',
          });
        }
      });
  };

  openEditPermissionModal = () => {
    this.loadPermissionList();
    this.setState({
      isEditPermissionModalVisible: true,
    });
  };

  loadPermissionList = () => {
    let apiURL =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      '/roles/' +
      this.props.data +
      '/permissions';

    axios
      .get(apiURL)
      .then(res => {
        if (res.status === 200) {
          this.getCheckedPermissions(res.data.data.nodeList);
          this.setState({
            nodeList: res.data.data.nodeList,
            isNodeList: true,
            checkedKeys: this.selected,
            expandedKeys: this.expandKeys,
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
            description: 'Error occurred while trying to load permission.',
          });
        }
      });
  };

  getCheckedPermissions = data => {
    data.forEach(item => {
      if (item !== null) {
        this.expandKeys.push(item.resourcePath);
        if (item.isSelected) {
          this.selected.push(item.resourcePath);
        }
        this.getCheckedPermissions(item.nodeList);
      } else {
        return null;
      }
    });
  };

  onExpand = expandedKeys => {
    this.setState({
      expandedKeys,
      autoExpandParent: false,
    });
  };

  onCheck = checkedKeys => {
    this.setState({ checkedKeys });
  };

  renderTreeNodes = data => {
    return data.map(item => {
      if (item !== null) {
        if (item.hasOwnProperty('nodeList')) {
          return (
            <TreeNode
              title={item.displayName}
              key={item.resourcePath}
              dataRef={item}
            >
              {this.renderTreeNodes(item.nodeList)}
            </TreeNode>
          );
        }
        return <TreeNode key={item.resourcePath} {...item} />;
      }
      // eslint-disable-next-line react/jsx-key
      return <TreeNode />;
    });
  };

  onUpdateRole = e => {
    this.props.form.validateFields((err, values) => {
      if (!err) {
        this.onConfirmUpdateRole(values);
      }
      console.log(values);
    });
  };

  onCancelHandler = e => {
    this.setState({
      isEditRoleModalVisible: false,
      isEditPermissionModalVisible: false,
    });
  };

  onConfirmUpdateRole = value => {
    const roleData = {
      roleName: value.roleName,
      users: value.users,
    };
    axios
      .put(
        window.location.origin +
          this.config.serverConfig.invoker.uri +
          this.config.serverConfig.invoker.deviceMgt +
          '/roles/' +
          this.props.data,
        roleData,
        { headers: { 'Content-Type': 'application-json' } },
      )
      .then(res => {
        if (res.status === 200) {
          this.props.fetchUsers();
          this.setState({
            isEditRoleModalVisible: false,
          });
          notification.success({
            message: 'Done',
            duration: 4,
            description: 'Successfully Updated the role.',
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
            description: 'Error occurred while trying to add role.',
          });
        }
      });
  };

  onAssignPermission = () => {
    const roleData = {
      roleName: this.props.data,
      permissions: this.state.checkedKeys,
    };
    axios
      .put(
        window.location.origin +
          this.config.serverConfig.invoker.uri +
          this.config.serverConfig.invoker.deviceMgt +
          '/roles/' +
          this.props.data,
        roleData,
        { headers: { 'Content-Type': 'application-json' } },
      )
      .then(res => {
        if (res.status === 200) {
          this.props.fetchUsers();
          notification.success({
            message: 'Done',
            duration: 4,
            description: 'Successfully Updated the Permissions.',
          });
          this.setState({
            isEditPermissionModalVisible: false,
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
            description: 'Error occurred while trying to add permissions.',
          });
        }
      });
  };

  loadUsersList = value => {
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
          let user = JSON.parse(res.data.data);
          let users = [];
          for (let i = 0; i < user.length; i++) {
            users.push(
              <Option key={user[i].username}>{user[i].username}</Option>,
            );
          }
          this.setState({
            users: users,
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

  onDeleteRole = () => {
    axios
      .delete(
        window.location.origin +
          this.config.serverConfig.invoker.uri +
          this.config.serverConfig.invoker.deviceMgt +
          '/roles/' +
          this.props.data,
        { headers: { 'Content-Type': 'application/json' } },
      )
      .then(res => {
        if (res.status === 200) {
          this.props.fetchUsers();
          notification.success({
            message: 'Done',
            duration: 4,
            description: 'Successfully deleted the Role.',
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
            description: 'Error occurred while trying to delete role.',
          });
        }
      });
  };

  render() {
    const isAdminRole = this.props.data === 'admin';
    const { getFieldDecorator } = this.props.form;
    return (
      <div>
        <div style={{ display: isAdminRole ? 'none' : 'inline' }}>
          <Tooltip placement="top" title={'Edit Role'}>
            <a>
              <Icon type="edit" onClick={this.openEditRoleModal} />
            </a>
          </Tooltip>
          <Divider type="vertical" />
          <Tooltip placement="top" title={'Edit Permissions'}>
            <a>
              <Icon type="file-add" onClick={this.openEditPermissionModal} />
            </a>
          </Tooltip>
          <Divider type="vertical" />
          <Tooltip placement="bottom" title={'Remove Role'}>
            <Popconfirm
              placement="top"
              title={'Are you sure?'}
              onConfirm={this.onDeleteRole}
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
            title="EDIT ROLE"
            width="40%"
            visible={this.state.isEditRoleModalVisible}
            onOk={this.onUpdateRole}
            onCancel={this.onCancelHandler}
            footer={[
              <Button key="cancel" onClick={this.onCancelHandler}>
                Cancel
              </Button>,
              <Button key="submit" type="primary" onClick={this.onUpdateRole}>
                Add Role
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
                <Form.Item label="Role Name" style={{ display: 'block' }}>
                  {getFieldDecorator('roleName', {
                    initialValue: this.state.roleData.roleName,
                    rules: [
                      {
                        pattern: new RegExp('^(((?!(\\@|\\/|\\s)).){3,})*$'),
                        message:
                          'Role name should be in minimum 3 characters long and not ' +
                          'include any whitespaces or @ or /',
                      },
                      {
                        required: true,
                        message: 'This field is required.',
                      },
                    ],
                  })(<Input />)}
                </Form.Item>
                <Form.Item label="User List" style={{ display: 'block' }}>
                  {getFieldDecorator('users', {
                    initialValue: this.state.roleData.users,
                  })(
                    <Select
                      mode="multiple"
                      style={{ width: '100%' }}
                      onSearch={this.loadUsersList}
                    >
                      {this.state.users}
                    </Select>,
                  )}
                </Form.Item>
              </Form>
            </div>
          </Modal>
        </div>
        <div>
          <Modal
            title="CHANGE ROLE PERMISSION"
            width="40%"
            visible={this.state.isEditPermissionModalVisible}
            onOk={this.onAssignPermission}
            onCancel={this.onCancelHandler}
            footer={[
              <Button key="cancel" onClick={this.onCancelHandler}>
                Cancel
              </Button>,
              <Button
                key="submit"
                type="primary"
                onClick={this.onAssignPermission}
              >
                Assign
              </Button>,
            ]}
            bodyStyle={{
              overflowY: 'scroll',
              maxHeight: '500px',
              marginLeft: '10px',
            }}
          >
            <div>
              {this.state.isNodeList && (
                <Tree
                  checkable
                  onExpand={this.onExpand}
                  expandedKeys={this.state.expandedKeys}
                  autoExpandParent={this.state.autoExpandParent}
                  onCheck={this.onCheck}
                  checkedKeys={this.state.checkedKeys}
                >
                  {this.renderTreeNodes(this.state.nodeList)}
                </Tree>
              )}
            </div>
          </Modal>
        </div>
      </div>
    );
  }
}

export default withConfigContext(
  Form.create({ name: 'role-actions' })(RoleAction),
);
