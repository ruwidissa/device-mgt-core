import React from "react";
import "antd/dist/antd.css";
import {
    PageHeader,
    Typography,
    Card,
    Steps,
    Button,
    message,
    Row,
    Col,
    Tag,
    Tooltip,
    Input,
    Icon,
    Select,
    Switch,
    Form,
    Upload,
    Divider, notification
} from "antd";
import IconImage from "./IconImg";
import UploadScreenshots from "./UploadScreenshots";
import axios from "axios";
import config from "../../../public/conf/config.json";

const Paragraph = Typography;
const Dragger = Upload.Dragger;

const props = {
    name: 'file',
    multiple: false,
    action: '//jsonplaceholder.typicode.com/posts/',
    onChange(info) {
        const status = info.file.status;
        if (status !== 'uploading') {
            console.log(info.file, info.fileList);
        }
        if (status === 'done') {
            message.success(`${info.file.name} file uploaded successfully.`);
        } else if (status === 'error') {
            message.error(`${info.file.name} file upload failed.`);
        }
    },
};

//
// const steps = [{
//     title: 'First',
//     content: Step1
// }, {
//     title: 'Second',
//     content: Step2,
// }, {
//     title: 'Last',
//     content: Step3,
// }];


const {Option} = Select;
const {TextArea} = Input;
const InputGroup = Input.Group;

const formItemLayout = {
    labelCol: {
        span: 4,
    },
    wrapperCol: {
        span: 20,
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
            screenshots: []
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
        console.log(`selected ${value}`);
    };

    handleSubmit = e => {
        e.preventDefault();
        this.props.form.validateFields((err, values) => {
            if (!err) {
                const {name, description, appCategories, tags, deviceType, price, isSharedWithAllTenants, binaryFile, icon, screenshots} = values;
                const payload = {
                    binaryFile: binaryFile[0].originFileObj,
                    icon: icon[0].originFileObj,
                    screenshot1: screenshots[0].originFileObj,
                    screenshot2: screenshots[1].originFileObj,
                    screenshot3: screenshots[2].originFileObj,
                    application: {
                        name,
                        description,
                        appCategories,
                        subType: (price === undefined || parseInt(price) === 0) ? "FREE" : "PAID",
                        tags,
                        unrestrictedRoles: [],
                        deviceType,
                        entAppReleaseWrappers: [{
                            description,
                            price: (price === undefined) ? 0 : parseInt(price),
                            isSharedWithAllTenants,
                            metaData: "string",
                            supportedOsVersions: "4.0-10.0"
                        }]
                    }
                };

                console.log(payload);

                // let data = new FormData();
                //
                // const url = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/ent-app";
                //
                // data.append('binaryFile', binaryFile[0].originFileObj);
                // data.append('icon', icon[0].originFileObj);
                // data.append('screenshot1', screenshots[0].originFileObj);
                // data.append('screenshot2', screenshots[1].originFileObj);
                // data.append('screenshot3', screenshots[2].originFileObj);
                // data.append('application', JSON.toString(payload.application));

                // let request = new XMLHttpRequest();
                // request.open('POST', url);
                // request.send(data);


                // var xhr = new XMLHttpRequest();
                // // xhr.withCredentials = true;
                //
                // xhr.addEventListener("readystatechange", function () {
                //     if (this.readyState === 4) {
                //         console.log(this.responseText);
                //     }
                // });
                //
                // xhr.open("POST", "https://localhost:9443/ui-request-handler/invoke/application-mgt-publisher/v1.0/applications/ent-app");
                // xhr.open("GET", "https://localhost:9443/ui-request-handler/invoke/application-mgt-publisher/v1.0/applications/tags");

                // xhr.setRequestHeader("Content-Type", "multipart/mixed");
                // xhr.setRequestHeader("X-Platform", "publisher");
                //

                // // xhr.setRequestHeader("Accept", "*/*");
                //
                // xhr.send(data);

                // xhr.send();

                // const options = {method: 'POST', body: data};
                //
                // fetch(url, options).then(function (response) {
                //     console.log(response);
                // });


                // axios.post(
                //     url,
                //     data,
                //     {
                //         headers:{
                //             'X-Platform': config.serverConfig.platform,
                //             'Content-Type': 'multipart/mixed',
                //             'content-type': 'multipart/form-data'
                //         },
                //         'Content-Type': 'multipart/mixed',
                //         'content-type': 'multipart/form-data'
                //     }
                //     ).then(res => {
                //     if (res.status === 201) {
                //         this.setState({
                //             loading: false,
                //         });
                //
                //         notification["success"]({
                //             message: "Done!",
                //             description:
                //                 "New app was added successfully",
                //         });
                //     }
                //
                // }).catch((error) => {
                //     if (error.response.status === 401) {
                //         window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
                //     } else {
                //         message.warning('Something went wrong');
                //
                //     }
                //     this.setState({
                //         loading: false
                //     });
                // });



                let data = new FormData();
                data.append('binaryFile', binaryFile[0].originFileObj);
                data.append('icon', icon[0].originFileObj);
                data.append('screenshot1', screenshots[0].originFileObj);
                data.append('screenshot2', screenshots[1].originFileObj);
                data.append('screenshot3', screenshots[2].originFileObj);
                const json = JSON.stringify(payload.application);
                const blob = new Blob([json], {
                    type: 'application/json'
                });
                data.append('application', blob);

                let xhr = new XMLHttpRequest();
                xhr.withCredentials = true;

                xhr.addEventListener("readystatechange", function () {
                    if (this.readyState === 4) {
                        console.log(this.responseText);
                    }
                });

                const url = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/ent-app";
                xhr.open("POST", url);
                xhr.setRequestHeader("X-Platform", "publisher");
                xhr.send(data);
            }
        });
    };

    normFile = e => {
        console.log('Upload event:', e);
        if (Array.isArray(e)) {
            return e;
        }
        return e && e.fileList;
    };

    handleIconChange = ({fileList}) => this.setState({icons: fileList});

    handleScreenshotChange = ({fileList}) => this.setState({screenshots: fileList});

    validateIcon = (rule, value, callback) => {
        const {icons} = this.state;
        if (icons.length !== 1) {
            callback("Please select icon file");
        }
        callback();
    };

    render() {
        const {categories, tags, icons, screenshots} = this.state;
        const {getFieldDecorator} = this.props.form;
        return (
            <div>
                <Row>
                    <Col span={20} offset={2}>
                        <Card>
                            <Form labelAlign="left" layout="horizontal"
                                  hideRequiredMark
                                  onSubmit={this.handleSubmit}>
                                <Row>
                                    <Col span={12}>
                                        <div>
                                            {/*device type*/}
                                            <Form.Item {...formItemLayout} label="Device Type">
                                                {getFieldDecorator('deviceType', {
                                                    rules: [{
                                                        required: false,
                                                        message: 'Please select device type'
                                                    },
                                                        {
                                                            validator: this.validateIcon
                                                        }],
                                                })(
                                                    <Select placeholder="select device type">
                                                        <Option key="android">Android</Option>
                                                        <Option key="ios">iOS</Option>
                                                    </Select>
                                                )}
                                            </Form.Item>

                                            {/*app name*/}
                                            <Form.Item {...formItemLayout} label="App Name">
                                                {getFieldDecorator('name', {
                                                    rules: [{
                                                        required: false,
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
                                                        required: false,
                                                        message: 'Please enter a description'
                                                    }],
                                                })(
                                                    <TextArea placeholder="Enter the description..." rows={7}/>
                                                )}
                                            </Form.Item>
                                            <Form.Item {...formItemLayout} label="Categories">
                                                {getFieldDecorator('appCategories', {
                                                    rules: [{
                                                        required: false,
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
                                                        required: false,
                                                        message: 'Please select'
                                                    }],
                                                })(
                                                    <Switch checkedChildren={<Icon type="check"/>}
                                                            unCheckedChildren={<Icon type="close"/>} defaultChecked/>
                                                )}

                                            </Form.Item>
                                            <Divider/>
                                            <Form.Item {...formItemLayout} label="Tags">
                                                {getFieldDecorator('tags', {
                                                    rules: [{
                                                        required: false,
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
                                            <Form.Item {...formItemLayout} label="Meta Daa">
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
                                            <Form.Item wrapperCol={{span: 12, offset: 5}}>
                                                <Button type="primary" htmlType="submit">
                                                    Submit
                                                </Button>
                                            </Form.Item>
                                        </div>
                                    </Col>
                                    <Col span={12} style={{paddingLeft: 20}}>
                                        <Form.Item label="Application">
                                            <div className="dropbox">
                                                {getFieldDecorator('binaryFile', {
                                                    valuePropName: 'fileList',
                                                    getValueFromEvent: this.normFile,
                                                    required: false,
                                                    message: 'Please select tags'
                                                })(
                                                    <Upload.Dragger
                                                        name="files"
                                                        beforeUpload={() => false}
                                                        multiple={false}
                                                    >
                                                        <p className="ant-upload-drag-icon">
                                                            <Icon type="inbox"/>
                                                        </p>
                                                        <p className="ant-upload-text">Click or drag file to this area
                                                            to upload</p>
                                                        <p className="ant-upload-hint">Support for a single or bulk
                                                            upload.</p>
                                                    </Upload.Dragger>,
                                                )}
                                            </div>
                                        </Form.Item>

                                        <Row>
                                            <Col span={12}>
                                                <Form.Item label="Icon">
                                                    {getFieldDecorator('icon', {
                                                        valuePropName: 'icon',
                                                        getValueFromEvent: this.normFile,
                                                        required: false,
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
                                            </Col>
                                        </Row>


                                        <Row style={{marginTop: 40}}>
                                            <Col span={24}>
                                                <Form.Item label="Screenshots">
                                                    {getFieldDecorator('screenshots', {
                                                        valuePropName: 'icon',
                                                        getValueFromEvent: this.normFile,
                                                        required: false,
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
                                            </Col>
                                        </Row>

                                    </Col>

                                </Row>

                            </Form>
                        </Card>
                    </Col>
                </Row>
            </div>

        );
    }
}

const AddNewAppForm = Form.create({name: 'add-new-app'})(AddNewAppFormComponent);
export default AddNewAppForm;
