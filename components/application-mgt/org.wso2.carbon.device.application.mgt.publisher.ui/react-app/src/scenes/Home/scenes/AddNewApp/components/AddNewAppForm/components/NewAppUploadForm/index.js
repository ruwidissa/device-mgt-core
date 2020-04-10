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
import {
  Button,
  Col,
  Form,
  Icon,
  Input,
  Row,
  Select,
  Upload,
  Typography,
  Modal,
  Alert,
} from 'antd';
import '@babel/polyfill';

const { Text } = Typography;

const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 8 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 16 },
  },
};
const { Option } = Select;
const { TextArea } = Input;
const InputGroup = Input.Group;

function getBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = error => reject(error);
  });
}

class NewAppUploadForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      icons: [],
      screenshots: [],
      loading: false,
      binaryFiles: [],
      application: null,
      isFree: true,
      previewVisible: false,
      previewImage: '',
      binaryFileHelperText: '',
      iconHelperText: '',
      screenshotHelperText: '',
      osVersionsHelperText: '',
      osVersionsValidateStatus: 'validating',
      metaData: [],
    };
    this.lowerOsVersion = null;
    this.upperOsVersion = null;
  }

  normFile = e => {
    if (Array.isArray(e)) {
      return e;
    }
    return e && e.fileList;
  };

  handleSubmit = e => {
    e.preventDefault();
    const { formConfig } = this.props;
    const { specificElements } = formConfig;

    this.props.form.validateFields((err, values) => {
      if (!err) {
        this.setState({
          loading: true,
        });
        const {
          binaryFile,
          icon,
          screenshots,
          releaseDescription,
          releaseType,
        } = values;

        // add release data
        const release = {
          description: releaseDescription,
          price: 0,
          isSharedWithAllTenants: false,
          metaData: JSON.stringify(this.state.metaData),
          releaseType: releaseType,
        };

        if (specificElements.hasOwnProperty('version')) {
          release.version = values.version;
        }
        if (specificElements.hasOwnProperty('url')) {
          release.url = values.url;
        }
        if (specificElements.hasOwnProperty('packageName')) {
          release.packageName = values.packageName;
        }

        const data = new FormData();
        let isFormValid = true; // flag to check if this form is valid

        if (
          formConfig.installationType !== 'WEB_CLIP' &&
          formConfig.installationType !== 'CUSTOM'
        ) {
          if (this.lowerOsVersion == null || this.upperOsVersion == null) {
            isFormValid = false;
            this.setState({
              osVersionsHelperText: 'Please select supported OS versions',
              osVersionsValidateStatus: 'error',
            });
          } else if (
            parseFloat(this.lowerOsVersion) >= parseFloat(this.upperOsVersion)
          ) {
            isFormValid = false;
            this.setState({
              osVersionsHelperText: 'Please select valid range',
              osVersionsValidateStatus: 'error',
            });
          } else {
            release.supportedOsVersions = `${this.lowerOsVersion}-${this.upperOsVersion}`;
          }
        }

        if (
          specificElements.hasOwnProperty('binaryFile') &&
          this.state.binaryFiles.length !== 1
        ) {
          isFormValid = false;
          this.setState({
            binaryFileHelperText: 'Please select the application',
          });
        }
        if (this.state.icons.length !== 1) {
          isFormValid = false;
          this.setState({
            iconHelperText: 'Please select an icon',
          });
        }
        if (this.state.screenshots.length !== 3) {
          isFormValid = false;
          this.setState({
            screenshotHelperText: 'Please select 3 screenshots',
          });
        }
        if (this.state.screenshots.length !== 3) {
          isFormValid = false;
          this.setState({
            screenshotHelperText: 'Please select 3 screenshots',
          });
        }
        if (isFormValid) {
          data.append('icon', icon[0].originFileObj);
          data.append('screenshot1', screenshots[0].originFileObj);
          data.append('screenshot2', screenshots[1].originFileObj);
          data.append('screenshot3', screenshots[2].originFileObj);
          if (specificElements.hasOwnProperty('binaryFile')) {
            data.append('binaryFile', binaryFile[0].originFileObj);
          }
          this.props.onSuccessReleaseData({ data, release });
        }
      }
    });
  };

  handleIconChange = ({ fileList }) => {
    if (fileList.length === 1) {
      this.setState({
        iconHelperText: '',
      });
    }
    this.setState({
      icons: fileList,
    });
  };
  handleBinaryFileChange = ({ fileList }) => {
    if (fileList.length === 1) {
      this.setState({
        binaryFileHelperText: '',
      });
    }
    this.setState({ binaryFiles: fileList });
  };

  handleScreenshotChange = ({ fileList }) => {
    if (fileList.length === 3) {
      this.setState({
        screenshotHelperText: '',
      });
    }
    this.setState({
      screenshots: fileList,
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

  addNewMetaData = () => {
    this.setState({
      metaData: this.state.metaData.concat({ key: '', value: '' }),
    });
  };

  handleLowerOsVersionChange = lowerOsVersion => {
    this.lowerOsVersion = lowerOsVersion;
    this.setState({
      osVersionsValidateStatus: 'validating',
      osVersionsHelperText: '',
    });
  };

  handleUpperOsVersionChange = upperOsVersion => {
    if (upperOsVersion === 'all') {
      this.upperOsVersion = this.props.supportedOsVersions[
        this.props.supportedOsVersions.length - 1
      ].versionName;
    } else {
      this.upperOsVersion = upperOsVersion;
    }
    this.setState({
      osVersionsValidateStatus: 'validating',
      osVersionsHelperText: '',
    });
  };

  render() {
    const { formConfig, supportedOsVersions } = this.props;
    const { getFieldDecorator } = this.props.form;
    const {
      icons,
      screenshots,
      binaryFiles,
      previewImage,
      previewVisible,
      binaryFileHelperText,
      iconHelperText,
      screenshotHelperText,
      metaData,
      osVersionsHelperText,
      osVersionsValidateStatus,
    } = this.state;
    const uploadButton = (
      <div>
        <Icon type="plus" />
        <div className="ant-upload-text">Select</div>
      </div>
    );

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
              {formConfig.specificElements.hasOwnProperty('binaryFile') && (
                <Form.Item
                  {...formItemLayout}
                  label="Application"
                  validateStatus="error"
                  help={binaryFileHelperText}
                >
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
                          <Icon type="upload" /> Click to upload
                        </Button>
                      )}
                    </Upload>,
                  )}
                </Form.Item>
              )}

              <Form.Item
                {...formItemLayout}
                label="Icon"
                validateStatus="error"
                help={iconHelperText}
                style={{
                  marginBottom: 0,
                }}
              >
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
              <Row
                style={{
                  marginBottom: 24,
                }}
              >
                <Col xs={24} sm={8}></Col>
                <Col xs={24} sm={16}>
                  <Text type="secondary">Recommended : 240px x 240px</Text>
                </Col>
              </Row>
              <Form.Item
                {...formItemLayout}
                label="Screenshots"
                validateStatus="error"
                help={screenshotHelperText}
                style={{
                  marginBottom: 0,
                }}
              >
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
              <Row
                style={{
                  marginBottom: 24,
                }}
              >
                <Col xs={24} sm={8}></Col>
                <Col xs={24} sm={16}>
                  <Text type="secondary">
                    It is mandatory to upload three screenshots and the
                    recommended screenshot size - min. 320px max. 3840px.
                  </Text>
                </Col>
              </Row>
              {formConfig.specificElements.hasOwnProperty('packageName') && (
                <Form.Item {...formItemLayout} label="Package Name">
                  {getFieldDecorator('packageName', {
                    rules: [
                      {
                        required: true,
                        message: 'Please input the package name',
                      },
                    ],
                  })(<Input placeholder="Package Name" />)}
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

              {formConfig.installationType !== 'WEB_CLIP' &&
                formConfig.installationType !== 'CUSTOM' && (
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
                      validateStatus={osVersionsValidateStatus}
                      help={osVersionsHelperText}
                    >
                      {getFieldDecorator('supportedOS', {
                        rules: [
                          {
                            required: true,
                          },
                        ],
                        initialValue: false,
                      })(
                        <div>
                          <InputGroup>
                            <Row gutter={8}>
                              <Col span={11}>
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
                                </Select>
                              </Col>
                              <Col span={2}>
                                <p> - </p>
                              </Col>
                              <Col span={11}>
                                <Select
                                  style={{ width: '100%' }}
                                  placeholder="Upper version"
                                  defaultActiveFirstOption={true}
                                  onChange={this.handleUpperOsVersionChange}
                                >
                                  {supportedOsVersions.length > 0 && (
                                    <Option key="all" value="all">
                                      All
                                    </Option>
                                  )}
                                  {supportedOsVersions.map(version => (
                                    <Option
                                      key={version.versionName}
                                      value={version.versionName}
                                    >
                                      {version.versionName}
                                    </Option>
                                  ))}
                                </Select>
                              </Col>
                            </Row>
                          </InputGroup>
                        </div>,
                      )}
                    </Form.Item>
                  </div>
                )}
              <Form.Item {...formItemLayout} label="Meta Data">
                {getFieldDecorator('meta', {})(
                  <div>
                    {metaData.map((data, index) => {
                      return (
                        <InputGroup key={index}>
                          <Row gutter={8}>
                            <Col span={5}>
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
                            <Col span={8}>
                              <Input
                                placeholder="value"
                                value={data.value}
                                onChange={e => {
                                  metaData[index].value = e.currentTarget.value;
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
              <Form.Item style={{ float: 'right', marginLeft: 8 }}>
                <Button type="primary" htmlType="submit">
                  Submit
                </Button>
              </Form.Item>
              <Form.Item style={{ float: 'right' }}>
                <Button
                  htmlType="button"
                  onClick={this.props.onClickBackButton}
                >
                  Back
                </Button>
              </Form.Item>
            </Form>
          </Col>
        </Row>
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
      </div>
    );
  }
}

export default Form.create({ name: 'app-upload-form' })(NewAppUploadForm);
