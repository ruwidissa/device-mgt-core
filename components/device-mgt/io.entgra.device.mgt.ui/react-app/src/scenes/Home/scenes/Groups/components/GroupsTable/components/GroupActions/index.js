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
import { withConfigContext } from '../../../../../../../../components/ConfigContext';

const { Text } = Typography;

class GroupActions extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      editModalVisible: false,
      shareModalVisible: false,
      name: this.props.data.name,
      description: this.props.data.description,
      groupDataObject: {},
      rolesData: [],
      shareRolesData: [],
    };
  }

  onConfirmDeleteGroup = () => {
    const config = this.props.context;

    // send request to the invoker
    axios
      .delete(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/groups/id/' +
          this.props.data.id,
        { headers: { 'Content-Type': 'application/json' } },
      )
      .then(res => {
        if (res.status === 200) {
          this.props.fetchGroups();
          notification.success({
            message: 'Done',
            duration: 4,
            description: 'Successfully deleted the group.',
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
            description: 'Error occurred while trying to delete group.',
          });
        }
      });
  };

  onConfirmUpdateGroup = data => {
    const config = this.props.context;

    // send request to the invoker
    axios
      .put(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/groups/id/' +
          this.props.data.id,
        data,
      )
      .then(res => {
        if (res.status === 200) {
          this.props.fetchGroups();
          notification.success({
            message: 'Done',
            duration: 4,
            description: 'Successfully updated the group.',
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
            description: 'Error occurred while trying to update group.',
          });
        }
      });
  };

  fetchUserRoles = (params = {}) => {
    const config = this.props.context;

    const apiUrl =
      window.location.origin +
      config.serverConfig.invoker.uri +
      config.serverConfig.invoker.deviceMgt +
      '/roles';

    // send request to the invokerss
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

  onConfirmShareGroup = data => {
    const config = this.props.context;

    // send request to the invoker
    axios
      .post(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/groups/id/' +
          this.props.data.id +
          '/share',
        data,
      )
      .then(res => {
        if (res.status === 200) {
          this.props.fetchGroups();
          notification.success({
            message: 'Done',
            duration: 4,
            description: 'Successfully shared the group.',
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
            description: 'Error occurred while trying to share group.',
          });
        }
      });
  };

  openEditModal = () => {
    this.setState({
      editModalVisible: true,
    });
  };

  openShareModal = () => {
    this.fetchUserRoles();
    this.setState({
      shareModalVisible: true,
    });
  };

  handleEditOk = e => {
    const groupDataObject = {
      name: this.state.name,
      description: this.state.description,
      id: this.props.data.id,
      owner: this.props.data.owner,
      groupProperties: this.props.data.groupProperties,
    };

    this.setState({ groupDataObject });

    this.props.form.validateFields(err => {
      if (!err) {
        this.onConfirmUpdateGroup(this.state.groupDataObject);
        this.setState({
          editModalVisible: false,
        });
      }
    });
  };

  handleEditCancel = e => {
    this.setState({
      editModalVisible: false,
    });
  };

  handleShareOk = e => {
    this.setState({
      shareModalVisible: false,
    });
    this.onConfirmShareGroup(this.state.shareRolesData);
  };

  handleShareCancel = e => {
    this.setState({
      shareModalVisible: false,
    });
  };

  onChangeName = e => {
    this.setState({
      name: e.currentTarget.value,
    });
  };

  onChangeDescription = e => {
    this.setState({
      description: e.currentTarget.value,
    });
  };

  handleRolesDropdownChange = value => {
    this.setState({
      shareRolesData: value,
    });
  };

  render() {
    const isAdminGroups = this.props.data.id == 1 || this.props.data.id == 2;
    const { getFieldDecorator } = this.props.form;
    let item = this.state.rolesData.map(data => (
      <Select.Option value={data} key={data}>
        {data}
      </Select.Option>
    ));
    return (
      <div>
        <div style={{ display: isAdminGroups ? 'none' : 'inline' }}>
          <Tooltip placement="top" title={'Share Group'}>
            <a>
              <Icon type="share-alt" onClick={this.openShareModal} />
            </a>
          </Tooltip>
          <Divider type="vertical" />
          <Tooltip placement="top" title={'Edit Group'}>
            <a>
              <Icon type="edit" onClick={this.openEditModal} />
            </a>
          </Tooltip>
          <Divider type="vertical" />
          <Tooltip placement="bottom" title={'Delete Group'}>
            <Popconfirm
              placement="top"
              title={'Are you sure?'}
              onConfirm={this.onConfirmDeleteGroup}
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
            title="Update Group"
            width="40%"
            visible={this.state.editModalVisible}
            onOk={this.handleEditOk}
            onCancel={this.handleEditCancel}
            footer={[
              <Button key="cancel" onClick={this.handleEditCancel}>
                Cancel
              </Button>,
              <Button key="submit" type="primary" onClick={this.handleEditOk}>
                Submit
              </Button>,
            ]}
          >
            <div style={{ alignItems: 'center' }}>
              <p>Enter new name and description for the group</p>
              <Form labelCol={{ span: 5 }} wrapperCol={{ span: 18 }}>
                <Form.Item label="Name" style={{ display: 'block' }}>
                  {getFieldDecorator('name', {
                    initialValue: this.props.data.name,
                    rules: [
                      {
                        required: true,
                        message: 'Please input group name',
                      },
                    ],
                  })(<Input onChange={this.onChangeName} />)}
                </Form.Item>
                <Form.Item label="Description" style={{ display: 'block' }}>
                  {getFieldDecorator('description', {
                    initialValue: this.props.data.description,
                    rules: [
                      {
                        required: true,
                        message: 'Please input group description',
                      },
                    ],
                  })(<Input onChange={this.onChangeDescription} />)}
                </Form.Item>
              </Form>
            </div>
          </Modal>
        </div>
        <div>
          <Modal
            title="Share Group"
            width="500px"
            visible={this.state.shareModalVisible}
            onOk={this.handleShareOk}
            onCancel={this.handleShareCancel}
            footer={[
              <Button key="new-role" onClick={this.handleShareCancel}>
                New Role
              </Button>,
              <Button key="new-role-selection" onClick={this.handleShareCancel}>
                New Role from Selection
              </Button>,
              <Button key="submit" type="primary" onClick={this.handleShareOk}>
                Share
              </Button>,
            ]}
          >
            <p>Select user role(s)</p>
            <Select
              mode="multiple"
              defaultValue={'admin'}
              style={{ width: '100%' }}
              onChange={this.handleRolesDropdownChange}
            >
              {item}
            </Select>
            ,
          </Modal>
        </div>
      </div>
    );
  }
}

export default withConfigContext(
  Form.create({ name: 'group-actions' })(GroupActions),
);
