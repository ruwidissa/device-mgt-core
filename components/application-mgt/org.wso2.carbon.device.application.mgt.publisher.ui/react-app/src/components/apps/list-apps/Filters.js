import React from "react";
import {
    Card,
    Col,
    Row,
    Typography,
    Input,
    Divider,
    Icon,
    Select,
    Button,
    Form,
    message,
    Radio,
    notification
} from "antd";
import axios from "axios";
import config from "../../../../public/conf/config.json";

const {Option} = Select;
const {Title} = Typography;


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

            if(values.hasOwnProperty("deviceType") && values.deviceType==="ALL"){
                delete values["deviceType"];
            }

            if(values.hasOwnProperty("subscriptionType") && values.subscriptionType==="ALL"){
                delete values["subscriptionType"];
            }
            if(values.hasOwnProperty("appType") && values.appType==="ALL"){
                delete values["appType"];
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
            config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/categories"
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
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
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
        axios.get(
            config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/tags"
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
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
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
        axios.get(
            config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt + "/device-types"
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
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
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


                    <Form.Item label="Device Type">
                        {getFieldDecorator('deviceType', {
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
                                <Option
                                    key="ALL">All
                                </Option>
                            </Select>
                        )}
                    </Form.Item>

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

                    <Form.Item label="App Type">
                        {getFieldDecorator('appType', {})(
                            <Select
                                style={{width: '100%'}}
                                placeholder="Select app type"
                            >
                                <Option value="ENTERPRISE">Enterprise</Option>
                                <Option value="PUBLIC">Public</Option>
                                <Option value="WEB_CLIP">Web APP</Option>
                                <Option value="ALL">All</Option>
                            </Select>
                        )}
                    </Form.Item>
                    <Divider/>

                    <Form.Item label="Subscription Type">
                        {getFieldDecorator('subscriptionType', {})(
                            <Radio.Group style={{width: '100%'}}>
                                <Radio value="FREE">Free</Radio>
                                <Radio value="PAID">Paid</Radio>
                                <Radio value="ALL">All</Radio>
                            </Radio.Group>,
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