import React from "react";
import {Modal, Button, Icon, notification, Spin, Tooltip, Upload, Input, Switch, Form, Divider} from 'antd';
import axios from "axios";
import {withConfigContext} from "../../../../context/ConfigContext";

const {TextArea} = Input;

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
        }

        this.setState({
            formConfig
        });
    };


    showModal = () => {
        const {release} = this.props;
        const {formConfig} = this.state;
        const {specificElements} = formConfig;

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
                    metaData: "string",
                    releaseType: releaseType,
                    supportedOsVersions: "4.0-10.0"
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


    render() {
        const {formConfig, icons, screenshots, loading, binaryFiles} = this.state;
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