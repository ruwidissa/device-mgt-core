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
            binaryFiles: []
        };
    }

    componentDidMount() {

    }

    handleSubmit = e => {
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

                const url = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/ent-app/" + appId;

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

                        console.log(res);

                        const uuid = res.data.data.uuid;

                        this.props.history.push('/publisher/apps/releases/'+uuid);
                    }

                }).catch((error) => {
                    console.log(error);
                    if (error.hasOwnProperty("response") && error.response.status === 401) {
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
                    console.log(error);
                });
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
    handleBinaryFileChange = ({fileList}) => this.setState({binaryFiles: fileList});

    handleScreenshotChange = ({fileList}) => this.setState({screenshots: fileList});

    render() {
        const {categories, tags, icons, screenshots, loading, binaryFiles} = this.state;
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

const AddReleaseForm = withRouter(Form.create({name: 'add-new-release'})(AddNewReleaseFormComponent));
export default AddReleaseForm;
