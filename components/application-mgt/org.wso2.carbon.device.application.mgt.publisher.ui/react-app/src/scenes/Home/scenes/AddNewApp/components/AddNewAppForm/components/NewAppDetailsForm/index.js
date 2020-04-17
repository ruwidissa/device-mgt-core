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
import { Alert, Button, Col, Form, Input, Row, Select, Spin } from 'antd';
import axios from 'axios';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import { handleApiError } from '../../../../../../../../services/utils/errorHandler';
import debounce from 'lodash.debounce';
import Authorized from '../../../../../../../../components/Authorized/Authorized';

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 5 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 19 },
  },
};
const { Option } = Select;
const { TextArea } = Input;

class NewAppDetailsForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      categories: [],
      tags: [],
      deviceTypes: [],
      fetching: false,
      roleSearchValue: [],
      unrestrictedRoles: [],
    };
    this.lastFetchId = 0;
    this.fetchRoles = debounce(this.fetchRoles, 800);
  }

  handleSubmit = e => {
    e.preventDefault();
    const { formConfig } = this.props;

    this.props.form.validateFields((err, values) => {
      if (!err) {
        this.setState({
          loading: true,
        });
        const {
          name,
          description,
          categories,
          tags,
          unrestrictedRoles,
        } = values;
        const unrestrictedRolesData = [];
        unrestrictedRoles.map(val => {
          unrestrictedRolesData.push(val.key);
        });
        const application = {
          name,
          description,
          categories,
          tags,
          unrestrictedRoles: unrestrictedRolesData,
        };

        if (formConfig.installationType !== 'WEB_CLIP') {
          application.deviceType = values.deviceType;
        } else {
          application.type = 'WEB_CLIP';
          application.deviceType = 'ALL';
        }

        this.props.onSuccessApplicationData(application);
      }
    });
  };

  componentDidMount() {
    this.getCategories();
    this.getTags();
    this.getDeviceTypes();
  }

  getCategories = () => {
    const config = this.props.context;
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.publisher +
          '/applications/categories',
      )
      .then(res => {
        if (res.status === 200) {
          let categories = JSON.parse(res.data.data);
          this.setState({
            categories: categories,
            loading: false,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to load categories.',
          true,
        );
        this.setState({
          loading: false,
        });
      });
  };

  getTags = () => {
    const config = this.props.context;
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.publisher +
          '/applications/tags',
      )
      .then(res => {
        if (res.status === 200) {
          let tags = JSON.parse(res.data.data);
          this.setState({
            tags: tags,
            loading: false,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to load tags.',
          true,
        );
        this.setState({
          loading: false,
        });
      });
  };

  getDeviceTypes = () => {
    const config = this.props.context;
    const { formConfig } = this.props;
    const { installationType } = formConfig;

    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/device-types',
      )
      .then(res => {
        if (res.status === 200) {
          const allDeviceTypes = JSON.parse(res.data.data);
          const mobileDeviceTypes = config.deviceTypes.mobileTypes;
          const allowedDeviceTypes = [];

          // exclude mobile device types if installation type is custom
          if (installationType === 'CUSTOM') {
            allDeviceTypes.forEach(deviceType => {
              if (!mobileDeviceTypes.includes(deviceType.name)) {
                allowedDeviceTypes.push(deviceType);
              }
            });
          } else {
            allDeviceTypes.forEach(deviceType => {
              if (mobileDeviceTypes.includes(deviceType.name)) {
                allowedDeviceTypes.push(deviceType);
              }
            });
          }

          this.setState({
            deviceTypes: allowedDeviceTypes,
            loading: false,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to load device types.',
          true,
        );
        if (error.hasOwnProperty('response') && error.response.status === 403) {
          const { forbiddenErrors } = this.state;
          forbiddenErrors.deviceTypes = true;
          this.setState({
            forbiddenErrors,
            loading: false,
          });
        } else {
          this.setState({
            loading: false,
          });
        }
      });
  };

  fetchRoles = value => {
    const config = this.props.context;
    this.lastFetchId += 1;
    const fetchId = this.lastFetchId;
    this.setState({ data: [], fetching: true });

    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/roles?filter=' +
          value,
      )
      .then(res => {
        if (res.status === 200) {
          if (fetchId !== this.lastFetchId) {
            // for fetch callback order
            return;
          }

          const data = res.data.data.roles.map(role => ({
            text: role,
            value: role,
          }));

          this.setState({
            unrestrictedRoles: data,
            fetching: false,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to load roles.',
          true,
        );
        this.setState({
          fetching: false,
        });
      });
  };

  handleRoleSearch = roleSearchValue => {
    this.setState({
      roleSearchValue,
      unrestrictedRoles: [],
      fetching: false,
    });
  };

  render() {
    const { formConfig } = this.props;
    const {
      categories,
      tags,
      deviceTypes,
      fetching,
      unrestrictedRoles,
    } = this.state;
    const { getFieldDecorator } = this.props.form;

    return (
      <div>
        <Row>
          <Col md={5}></Col>
          <Col md={14}>
            <Form
              labelAlign="right"
              layout="horizontal"
              onSubmit={this.handleSubmit}
            >
              {formConfig.installationType !== 'WEB_CLIP' && (
                <div>
                  <Authorized
                    permission="/permission/admin/device-mgt/admin/device-type/view"
                    no={
                      <Alert
                        message="You don't have permission to view device types."
                        type="warning"
                        banner
                        closable
                      />
                    }
                  />
                  <Form.Item {...formItemLayout} label="Device Type">
                    {getFieldDecorator('deviceType', {
                      rules: [
                        {
                          required: true,
                          message: 'Please select device type',
                        },
                      ],
                    })(
                      <Select
                        style={{ width: '100%' }}
                        placeholder="select device type"
                      >
                        {deviceTypes.map(deviceType => {
                          return (
                            <Option key={deviceType.name}>
                              {deviceType.name}
                            </Option>
                          );
                        })}
                      </Select>,
                    )}
                  </Form.Item>
                </div>
              )}

              {/* app name*/}
              <Form.Item {...formItemLayout} label="App Name">
                {getFieldDecorator('name', {
                  rules: [
                    {
                      required: true,
                      message: 'Please input a name',
                    },
                  ],
                })(<Input placeholder="ex: Lorem App" />)}
              </Form.Item>

              {/* description*/}
              <Form.Item {...formItemLayout} label="Description">
                {getFieldDecorator('description', {
                  rules: [
                    {
                      required: true,
                      message: 'Please enter a description',
                    },
                  ],
                })(
                  <TextArea placeholder="Enter the description..." rows={7} />,
                )}
              </Form.Item>

              {/* Unrestricted Roles*/}
              <Authorized
                permission="/permission/admin/device-mgt/roles/view"
                no={
                  <Alert
                    message="You don't have permission to view roles."
                    type="warning"
                    banner
                  />
                }
              />
              <Form.Item {...formItemLayout} label="Visible Roles">
                {getFieldDecorator('unrestrictedRoles', {
                  rules: [],
                  initialValue: [],
                })(
                  <Select
                    mode="multiple"
                    labelInValue
                    // value={roleSearchValue}
                    placeholder="Search roles"
                    notFoundContent={fetching ? <Spin size="small" /> : null}
                    filterOption={false}
                    onSearch={this.fetchRoles}
                    onChange={this.handleRoleSearch}
                    style={{ width: '100%' }}
                  >
                    {unrestrictedRoles.map(d => (
                      <Option key={d.value}>{d.text}</Option>
                    ))}
                  </Select>,
                )}
              </Form.Item>
              <Form.Item {...formItemLayout} label="Categories">
                {getFieldDecorator('categories', {
                  rules: [
                    {
                      required: true,
                      message: 'Please select categories',
                    },
                  ],
                })(
                  <Select
                    mode="multiple"
                    style={{ width: '100%' }}
                    placeholder="Select a Category"
                    onChange={this.handleCategoryChange}
                  >
                    {categories.map(category => {
                      return (
                        <Option key={category.categoryName}>
                          {category.categoryName}
                        </Option>
                      );
                    })}
                  </Select>,
                )}
              </Form.Item>
              <Form.Item {...formItemLayout} label="Tags">
                {getFieldDecorator('tags', {
                  rules: [
                    {
                      required: true,
                      message: 'Please select tags',
                    },
                  ],
                })(
                  <Select
                    mode="tags"
                    style={{ width: '100%' }}
                    placeholder="Tags"
                  >
                    {tags.map(tag => {
                      return <Option key={tag.tagName}>{tag.tagName}</Option>;
                    })}
                  </Select>,
                )}
              </Form.Item>
              <Form.Item style={{ float: 'right' }}>
                <Button type="primary" htmlType="submit">
                  Next
                </Button>
              </Form.Item>
            </Form>
          </Col>
        </Row>
      </div>
    );
  }
}

export default withConfigContext(
  Form.create({ name: 'app-details-form' })(NewAppDetailsForm),
);
