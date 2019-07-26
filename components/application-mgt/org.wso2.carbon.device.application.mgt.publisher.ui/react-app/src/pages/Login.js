import React from "react";
import {Typography, Row, Col, Form, Icon, Input, Button, Checkbox, message, notification} from 'antd';
import './Login.css';
import axios from 'axios';
import "./Login.css";
import {withConfigContext} from "../context/ConfigContext";

const {Title} = Typography;
const {Text} = Typography;

class Login extends React.Component {
    render() {
        const config = this.props.context;
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
        const config = this.props.context;
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
                    platform: "publisher"
                };

                const request = Object.keys(parameters).map(key => key + '=' + parameters[key]).join('&');

                axios.post(window.location.origin+ config.serverConfig.loginUri, request
                ).then(res=>{
                    if (res.status === 200) {
                        window.location = window.location.origin+"/publisher";
                    }
                }).catch(function (error) {
                    if (error.hasOwnProperty("response") && error.response.status === 400) {
                        thisForm.setState({
                            inValid: true
                        });
                    } else {
                        notification["error"]({
                            message: "There was a problem",
                            duration: 0,
                            description:
                                "Error occurred while trying to login.",
                        });
                    }
                    thisForm.setState({
                        loading: false
                    });
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
                        rules: [{required: true, message: 'Please enter your username'}],
                    })(
                        <Input style={{height: 32}} prefix={<Icon type="user" style={{color: 'rgba(0,0,0,.25)'}}/>}
                               placeholder="Username"/>
                    )}
                </Form.Item>
                <Form.Item>
                    {getFieldDecorator('password', {
                        rules: [{required: true, message: 'Please enter your password'}],
                    })(
                        <Input style={{height: 32}}
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

const WrappedNormalLoginForm = Form.create({name: 'normal_login'})(withConfigContext(NormalLoginForm));

export default withConfigContext(Login);
