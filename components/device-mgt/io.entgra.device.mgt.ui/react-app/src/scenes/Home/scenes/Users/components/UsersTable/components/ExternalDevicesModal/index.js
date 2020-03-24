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
import { Button, Form, Input, Modal, notification, Col, Row } from 'antd';
import axios from 'axios';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import { handleApiError } from '../../../../../../../../services/utils/errorHandler';
const InputGroup = Input.Group;

class ExternalDevicesModal extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      isDeviceEditModalVisible: false,
      metaData: [],
    };
  }

  openDeviceEditModal = () => {
    this.setState({
      isDeviceEditModalVisible: true,
    });
    this.getExternalDevicesForUser(this.props.user);
  };

  onCancelHandler = () => {
    this.setState({
      isDeviceEditModalVisible: false,
    });
  };

  getExternalDevicesForUser = userName => {
    let apiURL =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      `/users/claims/${userName}`;

    axios
      .get(apiURL)
      .then(res => {
        if (res.status === 200) {
          if (res.data.data.hasOwnProperty('http://wso2.org/claims/devices')) {
            this.setState({
              metaData: JSON.parse(
                res.data.data['http://wso2.org/claims/devices'],
              ),
            });
          }
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to retrieve claims.',
        );
      });
  };

  setExternalDevicesForUser = (userName, payload) => {
    let apiURL =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      `/users/claims/${userName}`;

    axios
      .put(apiURL, payload)
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Done',
            duration: 0,
            description: 'Succussfully updated.',
          });
        }
        this.setState({
          isDeviceEditModalVisible: false,
        });
      })
      .catch(error => {
        handleApiError(error, 'Error occurred while trying to update claims.');
      });
  };

  onSubmitClaims = e => {
    this.props.form.validateFields(['meta'], (err, values) => {
      if (!err) {
        this.setExternalDevicesForUser(this.props.user, this.state.metaData);
      }
    });
  };

  addNewMetaData = () => {
    this.setState({
      metaData: [...this.state.metaData, { deviceName: '', id: '' }],
    });
  };

  render() {
    const { getFieldDecorator } = this.props.form;
    const { metaData } = this.state;
    return (
      <div>
        <div>
          <Button
            type="primary"
            size={'small'}
            icon="desktop"
            onClick={this.openDeviceEditModal}
          >
            External Devices
          </Button>
        </div>
        <div>
          <Modal
            title="EDIT EXTERNAL DEVICE CLAIMS"
            width="40%"
            visible={this.state.isDeviceEditModalVisible}
            onOk={this.onSubmitClaims}
            onCancel={this.onCancelHandler}
            footer={[
              <Button key="cancel" onClick={this.onCancelHandler}>
                Cancel
              </Button>,
              <Button key="submit" type="primary" onClick={this.onSubmitClaims}>
                Update
              </Button>,
            ]}
          >
            <div style={{ alignItems: 'center' }}>
              <p>Add or edit external device information</p>
              <Form labelCol={{ span: 5 }} wrapperCol={{ span: 18 }}>
                <Form.Item style={{ display: 'block' }}>
                  {getFieldDecorator('meta', {})(
                    <div>
                      {metaData.map((data, index) => {
                        return (
                          <InputGroup key={index}>
                            <Row gutter={8}>
                              <Col span={5}>
                                <Input
                                  placeholder="key"
                                  defaultValue={data.deviceName}
                                  onChange={e => {
                                    metaData[index].deviceName =
                                      e.currentTarget.value;
                                    this.setState({
                                      metaData,
                                    });
                                  }}
                                />
                              </Col>
                              <Col span={8}>
                                <Input
                                  placeholder="value"
                                  defaultValue={data.id}
                                  onChange={e => {
                                    metaData[index].id = e.currentTarget.value;
                                    this.setState({
                                      metaData,
                                    });
                                  }}
                                />
                              </Col>
                              <Col span={3}>
                                <Button
                                  type="dashed"
                                  shape="circle"
                                  icon="minus"
                                  onClick={() => {
                                    metaData.splice(index, 1);
                                    this.setState({
                                      metaData,
                                    });
                                  }}
                                />
                              </Col>
                            </Row>
                          </InputGroup>
                        );
                      })}
                      <Button
                        type="dashed"
                        icon="plus"
                        onClick={this.addNewMetaData}
                      >
                        Addd
                      </Button>
                    </div>,
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
  Form.create({ name: 'external-device-modal' })(ExternalDevicesModal),
);
