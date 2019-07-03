import React from "react";
import {Avatar, Card, Col, Row, Table, Typography, Input, Divider, Checkbox, Select, Button, Form, message} from "antd";
import axios from "axios";
import config from "../../../../public/conf/config.json";

const {Option} = Select;
const {Title, Text} = Typography;


class FiltersForm extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            categories: [],
            tags: [],
            deviceTypes: []
        };
    }

    handleSubmit = e => {
        e.preventDefault();
        this.props.form.validateFields((err, values) => {
            for (const [key, value] of Object.entries(values)) {
                if (value === undefined) {
                    delete values[key];
                }
            }

            this.props.setFilters(values);
        });
    };

    componentDidMount() {
        this.getCategories();
        this.getTags();
        this.getDeviceTypes();
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


    getDeviceTypes = () => {
        axios.get(
            config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt + "/device-types",
            {
                headers: {'X-Platform': config.serverConfig.platform}
            }).then(res => {
            if (res.status === 200) {
                const deviceTypes = JSON.parse(res.data.data);
                this.setState({
                    deviceTypes,
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


    render() {
        const {categories, tags, deviceTypes} = this.state;
        const {getFieldDecorator} = this.props.form;

        return (

            <Card>
                <Form labelAlign="left" layout="horizontal"
                      hideRequiredMark
                      onSubmit={this.handleSubmit}>
                    <Row>
                        <Col span={12}>
                            <Title level={4}>Filter</Title>
                        </Col>
                        <Col span={12}>
                            <Form.Item style={{
                                float: "right",
                                marginBottom: 0,
                                marginTop: -5
                            }}>
                                <Button
                                    size="small"
                                    type="primary"
                                    htmlType="submit">
                                    Submit
                                </Button>
                            </Form.Item>
                        </Col>
                    </Row>
                    <Divider/>

                    {/*<Text strong={true}>Category</Text>*/}
                    {/*<br/><br/>*/}
                    <Form.Item label="Categories">
                        {getFieldDecorator('categories', {
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
                    {/*<Select*/}
                    {/*mode="multiple"*/}
                    {/*style={{width: '100%'}}*/}
                    {/*placeholder="All Categories"*/}
                    {/*>*/}
                    {/*<Option key={1}>IoT</Option>*/}
                    {/*<Option key={2}>EMM</Option>*/}
                    {/*</Select>*/}
                    <Divider/>

                    <Form.Item label="Device Types">
                        {getFieldDecorator('deviceTypes', {
                            rules: [{
                                required: false,
                                message: 'Please select device types'
                            }],
                        })(
                            <Select
                                style={{width: '100%'}}
                                placeholder="Select device types"
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

                    {/*<Text strong={true}>Tags</Text>*/}
                    <Form.Item label="Tags">
                        {getFieldDecorator('tags', {
                            rules: [{
                                required: false,
                                message: 'Please select tags'
                            }],
                        })(
                            <Select
                                mode="multiple"
                                style={{width: '100%'}}
                                placeholder="Select tags"
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

                    <Divider/>
                    <Form.Item label="App Type">
                        {getFieldDecorator('appType', {})(
                            <Select
                                style={{width: '100%'}}
                                placeholder="Select app type"
                            >
                                <Option value="ENTERPRISE">Enterprise</Option>
                                <Option value="PUBLIC">Public</Option>
                                <Option value="WEB_CLIP">Web APP</Option>
                            </Select>
                        )}
                    </Form.Item>
                    <Divider/>

                    <Form.Item label="Subscription Type">
                        {getFieldDecorator('subscriptionType', {})(
                            <Checkbox.Group style={{width: '100%'}}>
                                <Checkbox value="FREE">Free</Checkbox><br/>
                                <Checkbox value="PAID">Paid</Checkbox><br/>
                            </Checkbox.Group>,
                        )}
                    </Form.Item>
                    <Divider/>
                </Form>
            </Card>
        );
    }
}


const Filters = Form.create({name: 'filter-apps'})(FiltersForm);


export default Filters;