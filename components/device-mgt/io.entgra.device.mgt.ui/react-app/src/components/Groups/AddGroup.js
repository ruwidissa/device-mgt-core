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
import { Button, Form, Input, message, Modal, notification } from 'antd';
import axios from 'axios';
import { withConfigContext } from '../../context/ConfigContext';

class AddGroup extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      addModalVisible: false,
      name: '',
      description: '',
    };
  }

  onConfirmAdGroup = () => {
    const config = this.props.context;

    const groupData = {
      name: this.state.name,
      description: this.state.description,
    };

    // send request to the invoker
    axios
      .post(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/groups',
        groupData,
        { headers: { 'Content-Type': 'application/json' } },
      )
      .then(res => {
        if (res.status === 201) {
          this.props.fetchGroups();
          notification.success({
            message: 'Done',
            duration: 4,
            description: 'Successfully added the group.',
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
            description: 'Error occurred while trying to add group.',
          });
        }
      });
  };

  openAddModal = () => {
    this.setState({
      addModalVisible: true,
    });
  };

  handleAddOk = e => {
    this.props.form.validateFields(err => {
      if (!err) {
        this.onConfirmAdGroup();
        this.setState({
          addModalVisible: false,
        });
      }
    });
  };

  handleAddCancel = e => {
    this.setState({
      addModalVisible: false,
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
          >
            Add Group
          </Button>
        </div>
        <div>
          <Modal
            title="ADD NEW GROUP"
            width="40%"
            visible={this.state.addModalVisible}
            onOk={this.handleAddOk}
            onCancel={this.handleAddCancel}
            footer={[
              <Button key="cancel" onClick={this.handleAddCancel}>
                Cancel
              </Button>,
              <Button key="submit" type="primary" onClick={this.handleAddOk}>
                Submit
              </Button>,
            ]}
          >
            <div style={{ alignItems: 'center' }}>
              <p>Create new device group on IoT Server.</p>
              <Form labelCol={{ span: 5 }} wrapperCol={{ span: 18 }}>
                <Form.Item label="Name" style={{ display: 'block' }}>
                  {getFieldDecorator('name', {
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
      </div>
    );
  }
}

export default withConfigContext(Form.create({ name: 'add-group' })(AddGroup));
