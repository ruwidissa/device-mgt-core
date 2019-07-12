import React from "react";
import {Typography, Row, Col, Form, Icon, Input, Button, Checkbox} from 'antd';
import './Login.css';
import axios from 'axios';
import config from "../../public/conf/config.json";

const {Title} = Typography;
const {Text} = Typography;

class Login extends React.Component {
    render() {
        return (
            <div>
                <div className="background">
                </div>
                <div className="content">
                    <Row>
                        <Col xs={3} sm={3} md={10}>

                        </Col>
                        <Col xs={18} sm={18} md={4}>
                            <Row style={{marginBottom: 20}}>
                                <Col style={{textAlign: "center"}}>
                                    <img style={
                                        {
                                            marginTop: 36,
                                            height: 60
                                        }
                                    }
                                         src={config.theme.logo}/>
                                </Col>
                            </Row>
                            <Title level={2}>Login</Title>
                            <WrappedNormalLoginForm/>

                        </Col>
                    </Row>
                    <Row>
                        <Col span={4} offset={10}>

                        </Col>
                    </Row>
                </div>
            </div>

        );
    }
}

class NormalLoginForm extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            inValid: false,
            loading: false
        };
    }

    handleSubmit = (e) => {
        const thisForm = this;
        e.preventDefault();
        this.props.form.validateFields((err, values) => {
            thisForm.setState({
                inValid: false
            });
            if (!err) {
                thisForm.setState({
                    loading: true
                });
                const parameters = {
                    username: values.username,
                    password: values.password,
                    platform: "store"
                };

                const request = Object.keys(parameters).map(key => key + '=' + parameters[key]).join('&');

                axios.post(config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.loginUri, request
                ).then(res => {
                    if (res.status === 200) {
                        window.location = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + "/store";
                    }
                }).catch(function (error) {
                    if (error.response.status === 400) {
                        thisForm.setState({
                            inValid: true,
                            loading: false
                        });
                    }
                });
            }

        });
    };

    render() {
        const {getFieldDecorator} = this.props.form;
        let errorMsg = "";
        if (this.state.inValid) {
            errorMsg = <Text type="danger">Invalid Login Details</Text>;
        }
        let loading = "";
        if (this.state.loading) {
            loading = <Text type="secondary">Loading..</Text>;
        }
        return (
            <Form onSubmit={this.handleSubmit} className="login-form">
                <Form.Item>
                    {getFieldDecorator('username', {
                        rules: [{required: true, message: 'Please input your username!'}],
                    })(
                        <Input name="username" style={{height: 32}}
                               prefix={<Icon type="user" style={{color: 'rgba(0,0,0,.25)'}}/>}
                               placeholder="Username"/>
                    )}
                </Form.Item>
                <Form.Item>
                    {getFieldDecorator('password', {
                        rules: [{required: true, message: 'Please input your Password!'}],
                    })(
                        <Input name="password" style={{height: 32}}
                               prefix={<Icon type="lock" style={{color: 'rgba(0,0,0,.25)'}}/>} type="password"
                               placeholder="Password"/>
                    )}
                </Form.Item>
                {loading}
                {errorMsg}
                <Form.Item>
                    {getFieldDecorator('remember', {
                        valuePropName: 'checked',
                        initialValue: true,
                    })(
                        <Checkbox>Remember me</Checkbox>
                    )}
                    <br/>
                    <a className="login-form-forgot" href="">Forgot password</a>
                    <Button block type="primary" htmlType="submit" className="login-form-button">
                        Log in
                    </Button>
                </Form.Item>
            </Form>
        );
    }
}

const WrappedNormalLoginForm = Form.create({name: 'normal_login'})(NormalLoginForm);

export default Login;
