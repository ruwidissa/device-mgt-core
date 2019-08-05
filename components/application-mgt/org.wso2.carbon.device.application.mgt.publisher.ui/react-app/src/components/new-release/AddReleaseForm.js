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

import React from "react";
import {
    Card,
    Button,
    message,
    Row,
    Col,
    Input,
    Icon,
    Select,
    Switch,
    Form,
    Upload,
    Divider,
    notification,
    Spin, InputNumber
} from "antd";
import axios from "axios";
import {withRouter} from 'react-router-dom'
import {withConfigContext} from "../../context/ConfigContext";

const {Option} = Select;
const {TextArea} = Input;
const InputGroup = Input.Group;

const formItemLayout = {
    labelCol: {
        span: 8,
    },
    wrapperCol: {
        span: 16,
    },
};

class AddNewReleaseFormComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            current: 0,
            categories: [],
            tags: [],
            icons: [],
            screenshots: [],
            loading: false,
            binaryFiles: [],
            isFree: true
        };
    }

    handleSubmit = e => {
        const config = this.props.context;
        e.preventDefault();
        const {appId} = this.props;

        this.props.form.validateFields((err, values) => {
            if (!err) {
                this.setState({
                    loading: true
                });
                const {price, isSharedWithAllTenants, icon, screenshots, releaseDescription, releaseType, binaryFile} = values;


                const data = new FormData();

                //add release data
                const release = {
                    description: releaseDescription,
                    price: (price === undefined) ? 0 : parseInt(price),
                    isSharedWithAllTenants,
                    metaData: "string",
                    releaseType: releaseType,
                    supportedOsVersions: "4.0-10.0"
                };

                data.append('binaryFile', binaryFile[0].originFileObj);
                data.append('icon', icon[0].originFileObj);
                data.append('screenshot1', screenshots[0].originFileObj);
                data.append('screenshot2', screenshots[1].originFileObj);
                data.append('screenshot3', screenshots[2].originFileObj);

                const json = JSON.stringify(release);
                const blob = new Blob([json], {
                    type: 'application/json'
                });

                data.append("applicationRelease", blob);

                const url = window.location.origin+ config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/ent-app/" + appId;

                axios.post(
                    url,
                    data,
                    {
                        headers: {
                            'X-Platform': config.serverConfig.platform
                        },
                    }
                ).then(res => {
                    if (res.status === 201) {
                        this.setState({
                            loading: false,
                        });

                        notification["success"]({
                            message: "Done!",
                            description:
                                "New release was added successfully",
                        });

                        const uuid = res.data.data.uuid;

                        this.props.history.push('/publisher/apps/releases/'+uuid);
                    }

                }).catch((error) => {
                    if (error.hasOwnProperty("response") && error.response.status === 401) {
                        window.location.href = window.location.origin+ '/publisher/login';
                    } else {
                        notification["error"]({
                            message: "Something went wrong!",
                            description:
                                "Sorry, we were unable to complete your request.",
                        });

                    }
                    this.setState({
                        loading: false
                    });
                });
            }
        });
    };

    normFile = e => {
        if (Array.isArray(e)) {
            return e;
        }
        return e && e.fileList;
    };

    handleIconChange = ({fileList}) => this.setState({icons: fileList});
    handleBinaryFileChange = ({fileList}) => this.setState({binaryFiles: fileList});

    handleScreenshotChange = ({fileList}) => this.setState({screenshots: fileList});

    handlePriceTypeChange = (value) => {
        this.setState({
            isFree: (value === 'free')
        });
    };

    render() {
        const {isFree, icons, screenshots, loading, binaryFiles} = this.state;
        const {getFieldDecorator} = this.props.form;
        return (
            <div>
                <Spin tip="Uploading..." spinning={loading}>
                    <Row>
                        <Col span={12} offset={6}>
                            <Card>
                                <Form labelAlign="left" layout="horizontal"
                                      hideRequiredMark
                                      onSubmit={this.handleSubmit}>
                                    <Row>
                                        <Col span={12} style={{paddingLeft: 20}}>
                                            <Form.Item {...formItemLayout} label="Application">
                                                {getFieldDecorator('binaryFile', {
                                                    valuePropName: 'binaryFile',
                                                    getValueFromEvent: this.normFile,
                                                    required: true,
                                                    message: 'Please select application'
                                                })(
                                                    <Upload
                                                        name="binaryFile"
                                                        onChange={this.handleBinaryFileChange}
                                                        beforeUpload={() => false}
                                                    >
                                                        {binaryFiles.length !== 1 && (
                                                            <Button>
                                                                <Icon type="upload"/> Click to upload
                                                            </Button>
                                                        )}
                                                    </Upload>,
                                                )}
                                            </Form.Item>

                                            <Form.Item {...formItemLayout} label="Icon">
                                                {getFieldDecorator('icon', {
                                                    valuePropName: 'icon',
                                                    getValueFromEvent: this.normFile,
                                                    required: true,
                                                    message: 'Please select a icon'
                                                })(
                                                    <Upload
                                                        name="logo"
                                                        onChange={this.handleIconChange}
                                                        beforeUpload={() => false}
                                                    >
                                                        {icons.length !== 1 && (
                                                            <Button>
                                                                <Icon type="upload"/> Click to upload
                                                            </Button>
                                                        )}
                                                    </Upload>,
                                                )}
                                            </Form.Item>

                                            <Row style={{marginTop: 40}}>
                                                <Col span={24}>

                                                </Col>
                                            </Row>

                                            <Form.Item {...formItemLayout} label="Screenshots">
                                                {getFieldDecorator('screenshots', {
                                                    valuePropName: 'icon',
                                                    getValueFromEvent: this.normFile,
                                                    required: true,
                                                    message: 'Please select a icon'
                                                })(
                                                    <Upload
                                                        name="screenshots"
                                                        onChange={this.handleScreenshotChange}
                                                        beforeUpload={() => false}
                                                        multiple
                                                    >

                                                        {screenshots.length < 3 && (
                                                            <Button>
                                                                <Icon type="upload"/> Click to upload
                                                            </Button>
                                                        )}


                                                    </Upload>,
                                                )}
                                            </Form.Item>

                                            <Form.Item {...formItemLayout} label="Release Type">
                                                {getFieldDecorator('releaseType', {
                                                    rules: [{
                                                        required: true,
                                                        message: 'Please input the Release Type'
                                                    }],
                                                })(
                                                    <Input placeholder="Release Type"/>
                                                )}
                                            </Form.Item>

                                            <Form.Item {...formItemLayout} label="Description">
                                                {getFieldDecorator('releaseDescription', {
                                                    rules: [{
                                                        required: true,
                                                        message: 'Please enter a description for release'
                                                    }],
                                                })(
                                                    <TextArea placeholder="Enter a description for release" rows={5}/>
                                                )}
                                            </Form.Item>

                                            <Form.Item {...formItemLayout} label="Price Type">
                                                {getFieldDecorator('select', {
                                                    rules: [{required: true, message: 'Please select price Type'}],
                                                })(
                                                    <Select
                                                        placeholder="Please select a price type"
                                                        onChange={this.handlePriceTypeChange}>
                                                        <Option value="free">Free</Option>
                                                        <Option value="paid">Paid</Option>
                                                    </Select>,
                                                )}
                                            </Form.Item>

                                            <Form.Item {...formItemLayout} label="Price">
                                                {getFieldDecorator('price', {
                                                    rules: [{
                                                        required: !isFree
                                                    }],
                                                })(
                                                    <InputNumber
                                                        disabled={isFree}
                                                        options={{
                                                            initialValue: 1
                                                        }}
                                                        min={0}
                                                        max={10000}
                                                        formatter={value => `$ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                                                        parser={value => value.replace(/\$\s?|(,*)/g, '')}
                                                    />
                                                )}
                                            </Form.Item>

                                            <Form.Item {...formItemLayout} label="Is Shared?">
                                                {getFieldDecorator('isSharedWithAllTenants', {
                                                    rules: [{
                                                        required: true,
                                                        message: 'Please select'
                                                    }],
                                                    initialValue: false
                                                })(
                                                    <Switch checkedChildren={<Icon type="check"/>}
                                                            unCheckedChildren={<Icon type="close"/>}
                                                    />
                                                )}

                                            </Form.Item>
                                        </Col>

                                    </Row>
                                    <Form.Item style={{float: "right"}}>
                                        <Button type="primary" htmlType="submit">
                                            Submit
                                        </Button>
                                    </Form.Item>
                                </Form>
                            </Card>
                        </Col>
                    </Row>
                </Spin>
            </div>

        );
    }
}

const AddReleaseForm = withRouter(Form.create({name: 'add-new-release'})(AddNewReleaseFormComponent));
export default withConfigContext(AddReleaseForm);
