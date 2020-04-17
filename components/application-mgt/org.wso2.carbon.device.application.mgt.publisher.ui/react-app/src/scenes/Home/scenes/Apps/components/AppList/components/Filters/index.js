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
  Card,
  Col,
  Row,
  Typography,
  Divider,
  Select,
  Button,
  Form,
} from 'antd';
import axios from 'axios';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import { handleApiError } from '../../../../../../../../services/utils/errorHandler';
import Authorized from '../../../../../../../../components/Authorized/Authorized';

const { Option } = Select;
const { Title } = Typography;

class FiltersForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      categories: [],
      tags: [],
      deviceTypes: [],
    };
  }

  handleSubmit = e => {
    e.preventDefault();
    this.props.form.validateFields((err, values) => {
      for (const [key, value] of Object.entries(values)) {
        if (value === undefined) {
          delete values[key];
        }
      }

      if (values.hasOwnProperty('deviceType') && values.deviceType === 'ALL') {
        delete values.deviceType;
      }

      if (values.hasOwnProperty('appType') && values.appType === 'ALL') {
        delete values.appType;
      }

      this.props.setFilters(values);
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
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/device-types',
      )
      .then(res => {
        if (res.status === 200) {
          const deviceTypes = JSON.parse(res.data.data);
          this.setState({
            deviceTypes,
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
        this.setState({
          loading: false,
        });
      });
  };

  render() {
    const { categories, tags, deviceTypes } = this.state;
    const { getFieldDecorator } = this.props.form;

    return (
      <Card>
        <Form
          labelAlign="left"
          layout="horizontal"
          hideRequiredMark
          onSubmit={this.handleSubmit}
        >
          <Row>
            <Col span={12}>
              <Title level={4}>Filter</Title>
            </Col>
            <Col span={12}>
              <Form.Item
                style={{
                  float: 'right',
                  marginBottom: 0,
                  marginTop: -5,
                }}
              >
                <Button size="small" type="primary" htmlType="submit">
                  Submit
                </Button>
              </Form.Item>
            </Col>
          </Row>
          <Authorized
            permission="/permission/admin/app-mgt/publisher/application/update"
            yes={
              <div>
                <Form.Item label="Categories">
                  {getFieldDecorator('categories', {
                    rules: [
                      {
                        required: false,
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
                <Form.Item label="Tags">
                  {getFieldDecorator('tags', {
                    rules: [
                      {
                        required: false,
                        message: 'Please select tags',
                      },
                    ],
                  })(
                    <Select
                      mode="multiple"
                      style={{ width: '100%' }}
                      placeholder="Select tags"
                    >
                      {tags.map(tag => {
                        return <Option key={tag.tagName}>{tag.tagName}</Option>;
                      })}
                    </Select>,
                  )}
                </Form.Item>
              </div>
            }
          />
          <Authorized
            permission="/permission/admin/device-mgt/admin/device-type/view"
            yes={
              <Form.Item label="Device Type">
                {getFieldDecorator('deviceType', {
                  rules: [
                    {
                      required: false,
                      message: 'Please select device types',
                    },
                  ],
                })(
                  <Select
                    style={{ width: '100%' }}
                    placeholder="Select device types"
                  >
                    {deviceTypes.map(deviceType => {
                      return (
                        <Option key={deviceType.name}>{deviceType.name}</Option>
                      );
                    })}
                    <Option key="ALL">All</Option>
                  </Select>,
                )}
              </Form.Item>
            }
          />
          <Form.Item label="App Type">
            {getFieldDecorator('appType', {})(
              <Select style={{ width: '100%' }} placeholder="Select app type">
                <Option value="ENTERPRISE">Enterprise</Option>
                <Option value="PUBLIC">Public</Option>
                <Option value="WEB_CLIP">Web APP</Option>
                <Option value="CUSTOM">Custom</Option>
                <Option value="ALL">All</Option>
              </Select>,
            )}
          </Form.Item>
          <Divider />
        </Form>
      </Card>
    );
  }
}

const Filters = withConfigContext(
  Form.create({ name: 'filter-apps' })(FiltersForm),
);

export default withConfigContext(Filters);
