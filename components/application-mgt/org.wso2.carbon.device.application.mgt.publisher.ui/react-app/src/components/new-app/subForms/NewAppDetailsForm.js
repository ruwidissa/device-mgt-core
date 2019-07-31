import React from "react";
import {Button, Col, Divider, Form, Icon, Input, notification, Row, Select, Switch, Upload} from "antd";
import axios from "axios";
import {withConfigContext} from "../../../context/ConfigContext";

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

class NewAppDetailsForm extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            categories: [],
            tags: [],
            deviceTypes:[]
        }
    }


    handleSubmit = e => {
        e.preventDefault();
        const {formConfig} = this.props;
        const {specificElements} = formConfig;

        this.props.form.validateFields((err, values) => {
            if (!err) {
                this.setState({
                    loading: true
                });
                const {name, description, categories, tags, price, isSharedWithAllTenants, binaryFile, icon, screenshots, releaseDescription, releaseType} = values;
                const application = {
                    name,
                    description,
                    categories,
                    subMethod: (price === undefined || parseInt(price) === 0) ? "FREE" : "PAID",
                    tags,
                    unrestrictedRoles: [],
                };

                if (formConfig.installationType !== "WEB_CLIP") {
                    application.deviceType = values.deviceType;
                } else {
                    application.type = "WEB_CLIP";
                    application.deviceType = "ALL";
                }

                this.props.onSuccessApplicationData(application);
            }
        });
    };

    componentDidMount() {
        this.getCategories();
        this.getTags();
        this.getDeviceTypes();
    }

    getCategories = () => {
        const config = this.props.context;
        axios.get(
            window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/categories"
        ).then(res => {
            if (res.status === 200) {
                let categories = JSON.parse(res.data.data);
                this.setState({
                    categories: categories,
                    loading: false
                });
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                window.location.href = window.location.origin + '/publisher/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "Error occurred while trying to load categories.",
                });
            }
            this.setState({
                loading: false
            });
        });
    };

    getTags = () => {
        const config = this.props.context;
        axios.get(
            window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/tags"
        ).then(res => {
            if (res.status === 200) {
                let tags = JSON.parse(res.data.data);
                this.setState({
                    tags: tags,
                    loading: false,
                });
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                window.location.href = window.location.origin + '/publisher/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "Error occurred while trying to load tags.",
                });
            }
            this.setState({
                loading: false
            });
        });
    };

    getDeviceTypes = () => {
        const config = this.props.context;
        axios.get(
            window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt + "/device-types"
        ).then(res => {
            if (res.status === 200) {
                const deviceTypes = JSON.parse(res.data.data);
                this.setState({
                    deviceTypes,
                    loading: false,
                });
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                window.location.href = window.location.origin + '/publisher/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "Error occurred while trying to load device types.",
                });
            }
            this.setState({
                loading: false
            });
        });
    };

    render() {
        const {formConfig} = this.props;
        const {categories, tags, deviceTypes} = this.state;
        const {getFieldDecorator} = this.props.form;

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
                            {formConfig.installationType !== "WEB_CLIP" && (
                                <Form.Item {...formItemLayout} label="Device Type">
                                    {getFieldDecorator('deviceType', {
                                            rules: [
                                                {
                                                    required: true,
                                                    message: 'Please select device type'
                                                }
                                            ],

                                        }
                                    )(
                                        <Select
                                            mode="multiple"
                                            style={{width: '100%'}}
                                            placeholder="select device type"
                                            onChange={this.handleCategoryChange}
                                        >
                                            {
                                                deviceTypes.map(deviceType => {
                                                    return (
                                                        <Option
                                                            key={deviceType.name}>
                                                            {deviceType.name}
                                                        </Option>
                                                    )
                                                })
                                            }
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
                            {/* //todo implement add meta data */}
                            {/*<Form.Item {...formItemLayout} label="Meta Data">*/}
                            {/*<InputGroup>*/}
                            {/*<Row gutter={8}>*/}
                            {/*<Col span={10}>*/}
                            {/*<Input placeholder="Key"/>*/}
                            {/*</Col>*/}
                            {/*<Col span={12}>*/}
                            {/*<Input placeholder="value"/>*/}
                            {/*</Col>*/}
                            {/*<Col span={2}>*/}
                            {/*<Button type="dashed" shape="circle" icon="plus"/>*/}
                            {/*</Col>*/}
                            {/*</Row>*/}
                            {/*</InputGroup>*/}
                            {/*</Form.Item>*/}
                            <Form.Item style={{float: "right"}}>
                                <Button type="primary" htmlType="submit">
                                    Next
                                </Button>
                            </Form.Item>
                        </Form>
                    </Col>
                </Row>
            </div>
        );
    }

}

export default withConfigContext(Form.create({name: 'app-details-form'})(NewAppDetailsForm));
