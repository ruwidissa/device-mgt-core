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
import {Modal, Button, Icon, notification, Spin, Tooltip, Upload, Input, Switch, Form, Divider, Row, Col} from 'antd';
import axios from "axios";
import {withConfigContext} from "../../../../context/ConfigContext";

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

class EditReleaseModal extends React.Component {

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
                specificElements: {}
            }
        };
    }

    componentDidMount = () => {
        this.generateConfig();
    };

    generateConfig = () => {
        const {type} = this.props;
        const formConfig = {
            type
        };

        switch (type) {
            case "ENTERPRISE":
                formConfig.endpoint = "/ent-app-release";
                formConfig.specificElements = {
                    binaryFile: {
                        required: true
                    }
                };
                break;
            case "PUBLIC":
                formConfig.endpoint = "/public-app-release";
                formConfig.specificElements = {
                    packageName: {
                        required: true
                    },
                    version: {
                        required: true
                    }
                };
                break;
            case "WEB_CLIP":
                formConfig.endpoint = "/web-app-release";
                formConfig.specificElements = {
                    version: {
                        required: true
                    },
                    url: {
                        required: true
                    }
                };
                break;
            case "CUSTOM":
                formConfig.endpoint = "/custom-app-release";
                formConfig.specificElements = {
                    binaryFile: {
                        required: true
                    },
                    packageName: {
                        required: true
                    },
                    version: {
                        required: true
                    }
                };
                break;
        }

        this.setState({
            formConfig
        });
    };


    showModal = () => {
        const {release} = this.props;
        const {formConfig} = this.state;
        const {specificElements} = formConfig;
        let metaData = [];
        
        try{
            metaData =JSON.parse(release.metaData);
        }catch (e) {
            
        }

        this.props.form.setFields({
            releaseType: {
                value: release.releaseType
            },
            releaseDescription: {
                value: release.description
            },
            price: {
                value: release.price
            },
            isSharedWithAllTenants: {
                value: release.isSharedWithAllTenants
            }
        });

        if (specificElements.hasOwnProperty("version")) {
            this.props.form.setFields({
                version: {
                    value: release.version
                }
            });
        }

        if (specificElements.hasOwnProperty("url")) {
            this.props.form.setFields({
                url: {
                    value: release.url
                }
            });
        }

        if (specificElements.hasOwnProperty("packageName")) {
            this.props.form.setFields({
                packageName: {
                    value: release.packageName
                }
            });
        }

        this.setState({
            visible: true,
            metaData
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

    handleIconChange = ({fileList}) => this.setState({icons: fileList});
    handleBinaryFileChange = ({fileList}) => this.setState({binaryFiles: fileList});

    handleScreenshotChange = ({fileList}) => this.setState({screenshots: fileList});


    handleSubmit = e => {
        e.preventDefault();
        const {uuid} = this.props.release;
        const config = this.props.context;

        const {formConfig} = this.state;
        const {specificElements} = formConfig;

        this.props.form.validateFields((err, values) => {
            if (!err) {
                this.setState({
                    loading: true
                });
                const {price, isSharedWithAllTenants, releaseDescription, releaseType} = values;

                const {icons, screenshots, binaryFiles} = this.state;

                const data = new FormData();

                //add release data
                const release = {
                    description: releaseDescription,
                    price: (price === undefined) ? 0 : parseInt(price),
                    isSharedWithAllTenants,
                    metaData: JSON.stringify(this.state.metaData),
                    releaseType: releaseType,
                    supportedOsVersions: "4-30"
                };

                if (specificElements.hasOwnProperty("binaryFile") && binaryFiles.length === 1) {
                    data.append('binaryFile', binaryFiles[0].originFileObj);
                }

                if (specificElements.hasOwnProperty("version")) {
                    release.version = values.version;
                }

                if (specificElements.hasOwnProperty("url")) {
                    release.url = values.url;
                }

                if (specificElements.hasOwnProperty("packageName")) {
                    release.packageName = values.packageName;
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
                    type: 'application/json'
                });

                data.append("applicationRelease", blob);

                const url = window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications" + formConfig.endpoint + "/" + uuid;

                axios.put(
                    url,
                    data
                ).then(res => {
                    if (res.status === 200) {

                        const updatedRelease = res.data.data;

                        this.setState({
                            loading: false,
                            visible: false,
                        });

                        notification["success"]({
                            message: "Done!",
                            description:
                                "Saved!",
                        });
                        // console.log(updatedRelease);
                        this.props.updateRelease(updatedRelease);
                    }
                }).catch((error) => {
                    if (error.hasOwnProperty("response") && error.response.status === 401) {
                        window.location.href = window.location.origin + '/publisher/login';
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

    addNewMetaData = () => {
        this.setState({
            metaData: this.state.metaData.concat({'key': '', 'value': ''})
        })
    };

    render() {
        const {formConfig, icons, screenshots, loading, binaryFiles, metaData} = this.state;
        const {getFieldDecorator} = this.props.form;
        const {isAppUpdatable} = this.props;

        return (
            <div>
                <Tooltip title={isAppUpdatable ? "Edit this release" : "This release isn't in an editable state"}>
                    <Button
                        disabled={!isAppUpdatable}
                        size="small" type="primary" onClick={this.showModal}>
                        <Icon type="edit"/> Edit
                    </Button>
                </Tooltip>
                <Modal
                    title="Edit release"
                    visible={this.state.visible}
                    footer={null}
                    onCancel={this.handleCancel}
                >
                    <div>
                        <Spin tip="Uploading..." spinning={loading}>
                            <Form labelAlign="left" layout="horizontal"
                                  hideRequiredMark
                                  onSubmit={this.handleSubmit}>
                                {formConfig.specificElements.hasOwnProperty("binaryFile") && (
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
                                                        <Icon type="upload"/> Change
                                                    </Button>
                                                )}
                                            </Upload>,
                                        )}
                                    </Form.Item>
                                )}

                                {formConfig.specificElements.hasOwnProperty("packageName") && (
                                    <Form.Item {...formItemLayout} label="Package Name">
                                        {getFieldDecorator('packageName', {
                                            rules: [{
                                                required: true,
                                                message: 'Please input the package name'
                                            }],
                                        })(
                                            <Input placeholder="Package Name"/>
                                        )}
                                    </Form.Item>
                                )}

                                {formConfig.specificElements.hasOwnProperty("url") && (
                                    <Form.Item {...formItemLayout} label="URL">
                                        {getFieldDecorator('url', {
                                            rules: [{
                                                required: true,
                                                message: 'Please input the url'
                                            }],
                                        })(
                                            <Input placeholder="url"/>
                                        )}
                                    </Form.Item>
                                )}

                                {formConfig.specificElements.hasOwnProperty("version") && (
                                    <Form.Item {...formItemLayout} label="Version">
                                        {getFieldDecorator('version', {
                                            rules: [{
                                                required: true,
                                                message: 'Please input the version'
                                            }],
                                        })(
                                            <Input placeholder="Version"/>
                                        )}
                                    </Form.Item>
                                )}

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
                                                    <Icon type="upload"/> Change
                                                </Button>
                                            )}
                                        </Upload>,
                                    )}
                                </Form.Item>


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
                                        <TextArea placeholder="Enter a description for release"
                                                  rows={5}/>
                                    )}
                                </Form.Item>

                                <Form.Item {...formItemLayout} label="Price">
                                    {getFieldDecorator('price', {
                                        rules: [{
                                            required: false
                                        }],
                                    })(
                                        <Input prefix="$" placeholder="00.00"/>
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
                                <Form.Item {...formItemLayout} label="Meta Data">
                                    {getFieldDecorator('meta', {
                                        rules: [{
                                            required: true,
                                            message: 'Please fill empty fields'
                                        }],
                                        initialValue: false
                                    })(
                                        <div>
                                            {
                                                metaData.map((data, index) => {
                                                        return (
                                                            <InputGroup key={index}>
                                                                <Row gutter={8}>
                                                                    <Col span={10}>
                                                                        <Input
                                                                            placeholder="key"
                                                                            value={data.key}
                                                                            onChange={(e) => {
                                                                                metaData[index]['key'] = e.currentTarget.value;
                                                                                this.setState({
                                                                                    metaData
                                                                                })
                                                                            }}/>
                                                                    </Col>
                                                                    <Col span={10}>
                                                                        <Input
                                                                            placeholder="value"
                                                                            value={data.value}
                                                                            onChange={(e) => {
                                                                                metaData[index].value = e.currentTarget.value;
                                                                                this.setState({
                                                                                    metaData
                                                                                });
                                                                            }}/>
                                                                    </Col>
                                                                    <Col span={3}>
                                                                        <Button type="dashed"
                                                                                shape="circle"
                                                                                icon="minus"
                                                                                onClick={() => {
                                                                                    metaData.splice(index, 1);
                                                                                    this.setState({
                                                                                        metaData
                                                                                    });
                                                                                }}/>
                                                                    </Col>
                                                                </Row>
                                                            </InputGroup>
                                                        )
                                                    }
                                                )
                                            }
                                            <Button type="dashed" icon="plus" onClick={this.addNewMetaData}>
                                                Add
                                            </Button>
                                        </div>
                                    )}

                                </Form.Item>
                                <Divider/>
                                <Form.Item style={{float: "right", marginLeft: 8}}>
                                    <Button type="primary" htmlType="submit">
                                        Update
                                    </Button>
                                </Form.Item>
                                <Form.Item style={{float: "right"}}>
                                    <Button htmlType="button" onClick={this.handleCancel}>
                                        Back
                                    </Button>
                                </Form.Item>
                                <br/>
                            </Form>
                        </Spin>
                    </div>
                </Modal>
            </div>
        );
    }
}

const EditRelease = withConfigContext(Form.create({name: 'add-new-release'})(EditReleaseModal));

export default EditRelease;