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
    Spin
} from "antd";
import axios from "axios";
import {withRouter} from 'react-router-dom'
import config from "../../../public/conf/config.json";

const {Option} = Select;
const {TextArea} = Input;
const InputGroup = Input.Group;

const formItemLayout = {
    labelCol: {
        span: 5,
    },
    wrapperCol: {
        span: 19,
    },
};

class AddNewAppFormComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            current: 0,
            categories: [],
            tags: [],
            icons: [],
            screenshots: [],
            loading: false,
            binaryFiles: []
        };
    }

    componentDidMount() {
        this.getCategories();
        this.getTags();
    }

    getCategories = () => {
        axios.get(
            config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/categories",
            {
                headers: {'X-Platform': config.serverConfig.platform}
            }).then(res => {
            if (res.status === 200) {
                let categories = JSON.parse(res.data.data);
                this.setState({
                    categories: categories,
                    loading: false
                });
            }

        }).catch((error) => {
            if (error.response.status === 401) {
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
            } else {
                message.warning('Something went wrong');

            }
            this.setState({
                loading: false
            });
        });
    };

    getTags = () => {
        axios.get(
            config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/tags",
            {
                headers: {'X-Platform': config.serverConfig.platform}
            }).then(res => {
            if (res.status === 200) {
                let tags = JSON.parse(res.data.data);
                this.setState({
                    tags: tags,
                    loading: false,
                });
            }

        }).catch((error) => {
            if (error.response.status === 401) {
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
            } else {
                message.warning('Something went wrong');

            }
            this.setState({
                loading: false
            });
        });
    };

    handleCategoryChange = (value) => {
        // console.log(`selected ${value}`);
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
                const {name, description, categories, tags, price, isSharedWithAllTenants, binaryFile, icon, screenshots, releaseDescription,releaseType} = values;
                const application = {
                    name,
                    description,
                    categories,
                    subMethod: (price === undefined || parseInt(price) === 0) ? "FREE" : "PAID",
                    tags,
                    unrestrictedRoles: [],
                };

                const data = new FormData();

                if (formConfig.installationType !== "WEB_CLIP") {
                    application.deviceType = values.deviceType;
                }else{
                    application.type = "WEB_CLIP";
                    application.deviceType ="ALL";
                }

                if (specificElements.hasOwnProperty("binaryFile")) {
                    data.append('binaryFile', binaryFile[0].originFileObj);
                }

                //add release data
                const release = {
                    description: releaseDescription,
                    price: (price === undefined) ? 0 : parseInt(price),
                    isSharedWithAllTenants,
                    metaData: "string",
                    releaseType: releaseType
                };

                if (formConfig.installationType !== "WEB_CLIP") {
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

                //add release wrapper
                application[formConfig.releaseWrapperName] = [release];

                data.append('icon', icon[0].originFileObj);
                data.append('screenshot1', screenshots[0].originFileObj);
                data.append('screenshot2', screenshots[1].originFileObj);
                data.append('screenshot3', screenshots[2].originFileObj);

                const json = JSON.stringify(application);
                const blob = new Blob([json], {
                    type: 'application/json'
                });
                data.append(formConfig.jsonPayloadName, blob);

                const url = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications" + formConfig.endpoint;

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
                                "New app was added successfully",
                        });

                        this.props.history.push('/publisher/apps');

                        // window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/apps';

                    }

                }).catch((error) => {
                    if (error.response.status === 401) {
                        window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
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

    validateIcon = (rule, value, callback) => {
        const {icons} = this.state;
        if (icons.length !== 1) {
            callback("Please select icon file");
        }
        callback();
    };

    render() {
        const {categories, tags, icons, screenshots, loading, binaryFiles} = this.state;
        const {getFieldDecorator} = this.props.form;
        const {formConfig} = this.props;
        return (
            <div>
                <Spin tip="Uploading..." spinning={loading}>
                    <Row>
                        <Col span={20} offset={2}>
                            <Card>
                                <Form labelAlign="left" layout="horizontal"
                                      hideRequiredMark
                                      onSubmit={this.handleSubmit}>
                                    <Row>
                                        <Col span={12}>
                                            <div>

                                                {formConfig.installationType !== "WEB_CLIP" && (
                                                    <Form.Item {...formItemLayout} label="Device Type">
                                                        {getFieldDecorator('deviceType', {
                                                                rules: [
                                                                    {
                                                                        required: true,
                                                                        message: 'Please select device type'
                                                                    },
                                                                    {
                                                                        validator: this.validateIcon
                                                                    }
                                                                ],

                                                            }
                                                        )(
                                                            <Select placeholder="select device type">
                                                                <Option key="android">Android</Option>
                                                                <Option key="ios">iOS</Option>
                                                            </Select>
                                                        )}
                                                    </Form.Item>
                                                )}

                                                {/*app name*/}
                                                <Form.Item {...formItemLayout} label="App Name">
                                                    {getFieldDecorator('name', {
                                                        rules: [{
                                                            required: true,
                                                            message: 'Please input a name'
                                                        }],
                                                    })(
                                                        <Input placeholder="ex: Lorem App"/>
                                                    )}
                                                </Form.Item>

                                                {/*description*/}
                                                <Form.Item {...formItemLayout} label="Description">
                                                    {getFieldDecorator('description', {
                                                        rules: [{
                                                            required: true,
                                                            message: 'Please enter a description'
                                                        }],
                                                    })(
                                                        <TextArea placeholder="Enter the description..." rows={7}/>
                                                    )}
                                                </Form.Item>
                                                <Form.Item {...formItemLayout} label="Categories">
                                                    {getFieldDecorator('categories', {
                                                        rules: [{
                                                            required: true,
                                                            message: 'Please select categories'
                                                        }],
                                                    })(
                                                        <Select
                                                            mode="multiple"
                                                            style={{width: '100%'}}
                                                            placeholder="Select a Category"
                                                            onChange={this.handleCategoryChange}
                                                        >
                                                            {
                                                                categories.map(category => {
                                                                    return (
                                                                        <Option
                                                                            key={category.categoryName}>
                                                                            {category.categoryName}
                                                                        </Option>
                                                                    )
                                                                })
                                                            }
                                                        </Select>
                                                    )}
                                                </Form.Item>
                                                <Divider/>
                                                <Form.Item {...formItemLayout} label="Tags">
                                                    {getFieldDecorator('tags', {
                                                        rules: [{
                                                            required: true,
                                                            message: 'Please select tags'
                                                        }],
                                                    })(
                                                        <Select
                                                            mode="tags"
                                                            style={{width: '100%'}}
                                                            placeholder="Tags"
                                                        >
                                                            {
                                                                tags.map(tag => {
                                                                    return (
                                                                        <Option
                                                                            key={tag.tagName}>
                                                                            {tag.tagName}
                                                                        </Option>
                                                                    )
                                                                })
                                                            }
                                                        </Select>
                                                    )}
                                                </Form.Item>
                                                <Form.Item {...formItemLayout} label="Meta Data">
                                                    <InputGroup>
                                                        <Row gutter={8}>
                                                            <Col span={10}>
                                                                <Input placeholder="Key"/>
                                                            </Col>
                                                            <Col span={12}>
                                                                <Input placeholder="value"/>
                                                            </Col>
                                                            <Col span={2}>
                                                                <Button type="dashed" shape="circle" icon="plus"/>
                                                            </Col>
                                                        </Row>
                                                    </InputGroup>
                                                </Form.Item>
                                            </div>
                                        </Col>
                                        <Col span={12} style={{paddingLeft: 20}}>
                                            <p>Release Data</p>

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
                                                                    <Icon type="upload"/> Click to upload
                                                                </Button>
                                                            )}
                                                        </Upload>,
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

const AddNewAppForm = withRouter(Form.create({name: 'add-new-app'})(AddNewAppFormComponent));
export default AddNewAppForm;
