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
  Modal,
  Button,
  Icon,
  notification,
  Spin,
  Tooltip,
  Upload,
  Input,
  Form,
  Divider,
  Row,
  Col,
  Select,
  Alert,
} from 'antd';
import axios from 'axios';
import '@babel/polyfill';
import { withConfigContext } from '../../../../../../../../../../components/ConfigContext';

const { TextArea } = Input;
const InputGroup = Input.Group;
const { Option } = Select;

const formItemLayout = {
  labelCol: {
    span: 8,
  },
  wrapperCol: {
    span: 16,
  },
};

function getBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = error => reject(error);
  });
}

class EditReleaseModal extends React.Component {
  // To add subscription type & tenancy sharing, refer https://gitlab.com/entgra/carbon-device-mgt/merge_requests/331
  constructor(props) {
    super(props);
    this.state = {
      visible: false,
      current: 0,
      categories: [],
      tags: [],
      icons: [],
      screenshots: [],
      loading: false,
      binaryFiles: [],
      metaData: [],
      formConfig: {
        specificElements: {},
      },
    };
    this.lowerOsVersion = null;
    this.upperOsVersion = null;
  }

  componentDidMount = () => {
    this.generateConfig();
  };

  generateConfig = () => {
    const { type } = this.props;
    const formConfig = {
      type,
    };

    switch (type) {
      case 'ENTERPRISE':
        formConfig.endpoint = '/ent-app-release';
        formConfig.specificElements = {
          binaryFile: {
            required: true,
          },
        };
        break;
      case 'PUBLIC':
        formConfig.endpoint = '/public-app-release';
        formConfig.specificElements = {
          packageName: {
            required: true,
          },
          version: {
            required: true,
          },
        };
        break;
      case 'WEB_CLIP':
        formConfig.endpoint = '/web-app-release';
        formConfig.specificElements = {
          version: {
            required: true,
          },
          url: {
            required: true,
          },
        };
        break;
      case 'CUSTOM':
        formConfig.endpoint = '/custom-app-release';
        formConfig.specificElements = {
          binaryFile: {
            required: true,
          },
          packageName: {
            required: true,
          },
          version: {
            required: true,
          },
        };
        break;
    }

    this.setState({
      formConfig,
    });
  };

  showModal = () => {
    const config = this.props.context;
    const { release } = this.props;
    const { formConfig } = this.state;
    const { specificElements } = formConfig;
    let metaData = [];

    try {
      metaData = JSON.parse(release.metaData);
    } catch (e) {
      console.log(e);
    }

    this.props.form.setFields({
      releaseType: {
        value: release.releaseType,
      },
      releaseDescription: {
        value: release.description,
      },
    });

    if (config.deviceTypes.mobileTypes.includes(this.props.deviceType)) {
      const osVersions = release.supportedOsVersions.split('-');
      this.lowerOsVersion = osVersions[0];
      this.upperOsVersion = osVersions[1];
      this.props.form.setFields({
        lowerOsVersion: {
          value: osVersions[0],
        },
        upperOsVersion: {
          value: osVersions[1],
        },
      });
    }
    if (specificElements.hasOwnProperty('version')) {
      this.props.form.setFields({
        version: {
          value: release.version,
        },
      });
    }

    if (specificElements.hasOwnProperty('url')) {
      this.props.form.setFields({
        url: {
          value: release.url,
        },
      });
    }

    if (specificElements.hasOwnProperty('packageName')) {
      this.props.form.setFields({
        packageName: {
          value: release.packageName,
        },
      });
    }

    this.setState({
      visible: true,
      metaData,
    });
  };

  handleOk = e => {
    this.setState({
      visible: false,
    });
  };

  handleCancel = e => {
    this.setState({
      visible: false,
    });
  };

  normFile = e => {
    if (Array.isArray(e)) {
      return e;
    }
    return e && e.fileList;
  };

  handleIconChange = ({ fileList }) => this.setState({ icons: fileList });
  handleBinaryFileChange = ({ fileList }) =>
    this.setState({ binaryFiles: fileList });

  handleScreenshotChange = ({ fileList }) =>
    this.setState({ screenshots: fileList });

  handleSubmit = e => {
    e.preventDefault();
    const { uuid } = this.props.release;
    const config = this.props.context;

    const { formConfig } = this.state;
    const { specificElements } = formConfig;

    this.props.form.validateFields((err, values) => {
      if (!err) {
        this.setState({
          loading: true,
        });
        const { releaseDescription, releaseType } = values;

        const { icons, screenshots, binaryFiles } = this.state;

        const data = new FormData();

        // add release data
        const release = {
          description: releaseDescription,
          price: 0,
          isSharedWithAllTenants: false,
          metaData: JSON.stringify(this.state.metaData),
          releaseType: releaseType,
        };

        if (config.deviceTypes.mobileTypes.includes(this.props.deviceType)) {
          release.supportedOsVersions = `${this.lowerOsVersion}-${this.upperOsVersion}`;
        }

        if (
          specificElements.hasOwnProperty('binaryFile') &&
          binaryFiles.length === 1
        ) {
          data.append('binaryFile', binaryFiles[0].originFileObj);
        }

        if (specificElements.hasOwnProperty('version')) {
          release.version = values.version;
        }

        if (specificElements.hasOwnProperty('url')) {
          release.url = values.url;
        }

        if (icons.length === 1) {
          data.append('icon', icons[0].originFileObj);
        }

        if (screenshots.length > 0) {
          data.append('screenshot1', screenshots[0].originFileObj);
        }

        if (screenshots.length > 1) {
          data.append('screenshot2', screenshots[1].originFileObj);
        }

        if (screenshots.length > 2) {
          data.append('screenshot3', screenshots[2].originFileObj);
        }

        const json = JSON.stringify(release);
        const blob = new Blob([json], {
          type: 'application/json',
        });

        data.append('applicationRelease', blob);

        const url =
          window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.publisher +
          '/applications' +
          formConfig.endpoint +
          '/' +
          uuid;

        axios
          .put(url, data)
          .then(res => {
            if (res.status === 200) {
              const updatedRelease = res.data.data;

              this.setState({
                loading: false,
                visible: false,
              });

              notification.success({
                message: 'Done!',
                description: 'Saved!',
              });
              // console.log(updatedRelease);
              this.props.updateRelease(updatedRelease);
            }
          })
          .catch(error => {
            if (
              error.hasOwnProperty('response') &&
              error.response.status === 401
            ) {
              window.location.href =
                window.location.origin + '/publisher/login';
            } else {
              notification.error({
                message: 'Something went wrong!',
                description: 'Sorry, we were unable to complete your request.',
              });
            }
            this.setState({
              loading: false,
            });
          });
      }
    });
  };

  addNewMetaData = () => {
    this.setState({
      metaData: this.state.metaData.concat({ key: '', value: '' }),
    });
  };

  handlePreviewCancel = () => this.setState({ previewVisible: false });

  handlePreview = async file => {
    if (!file.url && !file.preview) {
      file.preview = await getBase64(file.originFileObj);
    }

    this.setState({
      previewImage: file.url || file.preview,
      previewVisible: true,
    });
  };

  handleLowerOsVersionChange = lowerOsVersion => {
    this.lowerOsVersion = lowerOsVersion;
  };

  handleUpperOsVersionChange = upperOsVersion => {
    this.upperOsVersion = upperOsVersion;
  };

  render() {
    const {
      formConfig,
      icons,
      screenshots,
      loading,
      binaryFiles,
      metaData,
      previewImage,
      previewVisible,
    } = this.state;
    const { getFieldDecorator } = this.props.form;
    const { isAppUpdatable, supportedOsVersions, deviceType } = this.props;
    const config = this.props.context;
    const uploadButton = (
      <div>
        <Icon type="plus" />
        <div className="ant-upload-text">Select</div>
      </div>
    );

    return (
      <div>
        <Tooltip
          title={
            isAppUpdatable
              ? 'Edit this release'
              : "This release isn't in an editable state"
          }
        >
          <Button
            disabled={!isAppUpdatable}
            size="small"
            type="primary"
            onClick={this.showModal}
          >
            <Icon type="edit" /> Edit
          </Button>
        </Tooltip>
        <Modal
          title="Edit release"
          visible={this.state.visible}
          footer={null}
          width={580}
          onCancel={this.handleCancel}
        >
          <div>
            <Spin tip="Uploading..." spinning={loading}>
              <Form
                labelAlign="left"
                layout="horizontal"
                hideRequiredMark
                onSubmit={this.handleSubmit}
              >
                {formConfig.specificElements.hasOwnProperty('binaryFile') && (
                  <Form.Item {...formItemLayout} label="Application">
                    {getFieldDecorator('binaryFile', {
                      valuePropName: 'binaryFile',
                      getValueFromEvent: this.normFile,
                      required: true,
                      message: 'Please select application',
                    })(
                      <Upload
                        name="binaryFile"
                        onChange={this.handleBinaryFileChange}
                        beforeUpload={() => false}
                      >
                        {binaryFiles.length !== 1 && (
                          <Button>
                            <Icon type="upload" /> Change
                          </Button>
                        )}
                      </Upload>,
                    )}
                  </Form.Item>
                )}

                {formConfig.specificElements.hasOwnProperty('url') && (
                  <Form.Item {...formItemLayout} label="URL">
                    {getFieldDecorator('url', {
                      rules: [
                        {
                          required: true,
                          message: 'Please input the url',
                        },
                      ],
                    })(<Input placeholder="url" />)}
                  </Form.Item>
                )}

                {formConfig.specificElements.hasOwnProperty('version') && (
                  <Form.Item {...formItemLayout} label="Version">
                    {getFieldDecorator('version', {
                      rules: [
                        {
                          required: true,
                          message: 'Please input the version',
                        },
                      ],
                    })(<Input placeholder="Version" />)}
                  </Form.Item>
                )}

                <Form.Item {...formItemLayout} label="Icon">
                  {getFieldDecorator('icon', {
                    valuePropName: 'icon',
                    getValueFromEvent: this.normFile,
                    required: true,
                    message: 'Please select a icon',
                  })(
                    <Upload
                      name="logo"
                      listType="picture-card"
                      onChange={this.handleIconChange}
                      beforeUpload={() => false}
                      onPreview={this.handlePreview}
                    >
                      {icons.length === 1 ? null : uploadButton}
                    </Upload>,
                  )}
                </Form.Item>

                <Form.Item {...formItemLayout} label="Screenshots">
                  {getFieldDecorator('screenshots', {
                    valuePropName: 'icon',
                    getValueFromEvent: this.normFile,
                    required: true,
                    message: 'Please select a icon',
                  })(
                    <Upload
                      name="screenshots"
                      listType="picture-card"
                      onChange={this.handleScreenshotChange}
                      beforeUpload={() => false}
                      onPreview={this.handlePreview}
                    >
                      {screenshots.length >= 3 ? null : uploadButton}
                    </Upload>,
                  )}
                </Form.Item>

                <Form.Item {...formItemLayout} label="Release Type">
                  {getFieldDecorator('releaseType', {
                    rules: [
                      {
                        required: true,
                        message: 'Please input the Release Type',
                      },
                    ],
                  })(<Input placeholder="Release Type" />)}
                </Form.Item>

                <Form.Item {...formItemLayout} label="Description">
                  {getFieldDecorator('releaseDescription', {
                    rules: [
                      {
                        required: true,
                        message: 'Please enter a description for release',
                      },
                    ],
                  })(
                    <TextArea
                      placeholder="Enter a description for release"
                      rows={5}
                    />,
                  )}
                </Form.Item>
                {config.deviceTypes.mobileTypes.includes(deviceType) && (
                  <div>
                    {this.props.forbiddenErrors.supportedOsVersions && (
                      <Alert
                        message="You don't have permission to view supported OS versions."
                        type="warning"
                        banner
                        closable
                      />
                    )}
                    <Form.Item
                      {...formItemLayout}
                      label="Supported OS Versions"
                    >
                      {getFieldDecorator('supportedOS')(
                        <div>
                          <InputGroup>
                            <Row gutter={8}>
                              <Col span={11}>
                                <Form.Item>
                                  {getFieldDecorator('lowerOsVersion', {
                                    rules: [
                                      {
                                        required: true,
                                        message: 'Please select Value',
                                      },
                                    ],
                                  })(
                                    <Select
                                      placeholder="Lower version"
                                      style={{ width: '100%' }}
                                      onChange={this.handleLowerOsVersionChange}
                                    >
                                      {supportedOsVersions.map(version => (
                                        <Option
                                          key={version.versionName}
                                          value={version.versionName}
                                        >
                                          {version.versionName}
                                        </Option>
                                      ))}
                                    </Select>,
                                  )}
                                </Form.Item>
                              </Col>
                              <Col span={2}>
                                <p> - </p>
                              </Col>
                              <Col span={11}>
                                <Form.Item>
                                  {getFieldDecorator('upperOsVersion', {
                                    rules: [
                                      {
                                        required: true,
                                        message: 'Please select Value',
                                      },
                                    ],
                                  })(
                                    <Select
                                      style={{ width: '100%' }}
                                      placeholder="Upper version"
                                      onChange={this.handleUpperOsVersionChange}
                                    >
                                      {supportedOsVersions.map(version => (
                                        <Option
                                          key={version.versionName}
                                          value={version.versionName}
                                        >
                                          {version.versionName}
                                        </Option>
                                      ))}
                                    </Select>,
                                  )}
                                </Form.Item>
                              </Col>
                            </Row>
                          </InputGroup>
                        </div>,
                      )}
                    </Form.Item>
                  </div>
                )}
                <Form.Item {...formItemLayout} label="Meta Data">
                  {getFieldDecorator('meta', {
                    rules: [
                      {
                        required: true,
                        message: 'Please fill empty fields',
                      },
                    ],
                    initialValue: false,
                  })(
                    <div>
                      {metaData.map((data, index) => {
                        return (
                          <InputGroup key={index}>
                            <Row gutter={8}>
                              <Col span={10}>
                                <Input
                                  placeholder="key"
                                  value={data.key}
                                  onChange={e => {
                                    metaData[index].key = e.currentTarget.value;
                                    this.setState({
                                      metaData,
                                    });
                                  }}
                                />
                              </Col>
                              <Col span={10}>
                                <Input
                                  placeholder="value"
                                  value={data.value}
                                  onChange={e => {
                                    metaData[index].value =
                                      e.currentTarget.value;
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
                        Add
                      </Button>
                    </div>,
                  )}
                </Form.Item>
                <Divider />
                <Form.Item style={{ float: 'right', marginLeft: 8 }}>
                  <Button type="primary" htmlType="submit">
                    Update
                  </Button>
                </Form.Item>
                <Form.Item style={{ float: 'right' }}>
                  <Button htmlType="button" onClick={this.handleCancel}>
                    Back
                  </Button>
                </Form.Item>
                <br />
              </Form>
            </Spin>
          </div>
          <Modal
            visible={previewVisible}
            footer={null}
            onCancel={this.handlePreviewCancel}
          >
            <img
              alt="Preview Image"
              style={{ width: '100%' }}
              src={previewImage}
            />
          </Modal>
        </Modal>
      </div>
    );
  }
}

const EditRelease = withConfigContext(
  Form.create({ name: 'add-new-release' })(EditReleaseModal),
);

export default EditRelease;
