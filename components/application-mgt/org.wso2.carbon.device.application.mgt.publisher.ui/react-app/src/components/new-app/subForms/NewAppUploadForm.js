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
import {Button, Col, Form, Icon, Input, Row, Select, Switch, Upload, InputNumber, Modal} from "antd";
import "@babel/polyfill";

const formItemLayout = {
    labelCol: {
        xs: {span: 24},
        sm: {span: 5},
    },
    wrapperCol: {
        xs: {span: 24},
        sm: {span: 19},
    },
};
const {Option} = Select;
const {TextArea} = Input;

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
            screenshotHelperText: ''
        }
    }

    normFile = e => {
        if (Array.isArray(e)) {
            return e;
        }
        return e && e.fileList;
    };

    handleSubmit = e => {
        e.preventDefault();
        const {formConfig} = this.props;
        const {specificElements} = formConfig;

        this.props.form.validateFields((err, values) => {
            if (!err) {
                this.setState({
                    loading: true
                });

                console.log(values);

                const {price, isSharedWithAllTenants, binaryFile, icon, screenshots, releaseDescription, releaseType} = values;

                //add release data
                const release = {
                    description: releaseDescription,
                    price: (price === undefined) ? 0 : parseInt(price),
                    isSharedWithAllTenants,
                    metaData: "string",
                    releaseType: releaseType
                };

                if (formConfig.installationType !== "WEB_CLIP" && formConfig.installationType !== "CUSTOM") {
                    release.supportedOsVersions = "4.0-10.0";
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

                const data = new FormData();

                if (specificElements.hasOwnProperty("binaryFile") && this.state.binaryFiles.length !== 1) {
                    this.setState({
                        binaryFileHelperText: 'Please select the application'
                    });
                } else if (this.state.icons.length !== 1) {
                    this.setState({
                        iconHelperText: 'Please select an icon'
                    });
                } else if (this.state.screenshots.length !== 3) {
                    this.setState({
                        screenshotHelperText: 'Please select 3 screenshots'
                    });
                } else {
                    data.append('icon', icon[0].originFileObj);
                    data.append('screenshot1', screenshots[0].originFileObj);
                    data.append('screenshot2', screenshots[1].originFileObj);
                    data.append('screenshot3', screenshots[2].originFileObj);
                    if (specificElements.hasOwnProperty("binaryFile")) {
                        data.append('binaryFile', binaryFile[0].originFileObj);
                    }
                    this.props.onSuccessReleaseData({data, release});
                }
            }
        });
    };

    handleIconChange = ({fileList}) => {
        if (fileList.length === 1) {
            this.setState({
                iconHelperText: ''
            });
        }
        this.setState({
            icons: fileList
        });
    };
    handleBinaryFileChange = ({fileList}) => {
        if (fileList.length === 1) {
            this.setState({
                binaryFileHelperText: ''
            });
        }
        this.setState({binaryFiles: fileList});
    };

    handleScreenshotChange = ({fileList}) => {
        if (fileList.length === 3) {
            this.setState({
                screenshotHelperText: ''
            });
        }
        this.setState({
            screenshots: fileList
        });
    };

    handlePriceTypeChange = (value) => {
        this.setState({
            isFree: (value === 'free')
        });
    };

    handlePreviewCancel = () => this.setState({previewVisible: false});
    handlePreview = async file => {
        if (!file.url && !file.preview) {
            file.preview = await getBase64(file.originFileObj);
        }

        this.setState({
            previewImage: file.url || file.preview,
            previewVisible: true,
        });
    };

    render() {
        const {formConfig} = this.props;
        const {getFieldDecorator} = this.props.form;
        const {
            icons,
            screenshots,
            binaryFiles,
            isFree,
            previewImage,
            previewVisible,
            binaryFileHelperText,
            iconHelperText,
            screenshotHelperText
        } = this.state;
        const uploadButton = (
            <div>
                <Icon type="plus"/>
                <div className="ant-upload-text">Select</div>
            </div>
        );

        return (
            <div>
                <Row>
                    <Col md={5}>

                    </Col>
                    <Col md={14}>
                        <Form
                            labelAlign="right"
                            layout="horizontal"
                            onSubmit={this.handleSubmit}
                        >
                            {formConfig.specificElements.hasOwnProperty("binaryFile") && (
                                <Form.Item {...formItemLayout}
                                           label="Application"
                                           validateStatus="error"
                                           help={binaryFileHelperText}>
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
                            )}

                            <Form.Item {...formItemLayout}
                                       label="Icon"
                                       validateStatus="error"
                                       help={iconHelperText}>
                                {getFieldDecorator('icon', {
                                    valuePropName: 'icon',
                                    getValueFromEvent: this.normFile,
                                    required: true,
                                    message: 'Please select a icon'
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

                            <Form.Item {...formItemLayout}
                                       label="Screenshots"
                                       validateStatus="error"
                                       help={screenshotHelperText}>
                                {getFieldDecorator('screenshots', {
                                    valuePropName: 'icon',
                                    getValueFromEvent: this.normFile,
                                    required: true,
                                    message: 'Please select a icon'
                                })(
                                    <Upload
                                        name="screenshots"
                                        listType="picture-card"
                                        onChange={this.handleScreenshotChange}
                                        beforeUpload={() => false}
                                        onPreview={this.handlePreview}>
                                        {screenshots.length >= 3 ? null : uploadButton}
                                    </Upload>,
                                )}
                            </Form.Item>

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
                            <Form.Item style={{float: "right", marginLeft: 8}}>
                                <Button type="primary" htmlType="submit">
                                    Submit
                                </Button>
                            </Form.Item>
                            <Form.Item style={{float: "right"}}>
                                <Button htmlType="button" onClick={this.props.onClickBackButton}>
                                    Back
                                </Button>
                            </Form.Item>
                        </Form>
                    </Col>
                </Row>
                <Modal visible={previewVisible} footer={null} onCancel={this.handlePreviewCancel}>
                    <img alt="Preview Image" style={{width: '100%'}} src={previewImage}/>
                </Modal>
            </div>
        );
    }

}

export default (Form.create({name: 'app-upload-form'})(NewAppUploadForm));