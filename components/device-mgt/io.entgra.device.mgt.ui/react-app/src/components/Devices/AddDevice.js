import React from 'react';
import {Button, Form, Row, Col, Card, Steps, Input, message, Modal, notification, Typography} from "antd";
import axios from "axios";
import {withConfigContext} from "../../context/ConfigContext";
import DeviceType from "./DeviceType";
import EnrollAgent from "./EnrollAgent";
const {Step} = Steps;

class AddDevice extends React.Component {

    constructor(props) {
        super(props);
        this.config =  this.props.context;
        this.state = {
            isAddDeviceModalVisible: false,
            current : 0,
        }
    };

    onClickType = () =>{
        this.setState({
            current: 1,
        })
    };

    openAddDeviceModal = () =>{
        this.setState({
            isAddDeviceModalVisible : true,
        })
    };

    render() {
        const {loading, current, isError, supportedOsVersions, errorText, forbiddenErrors} = this.state;
        const { getFieldDecorator } = this.props.form;
        return (
            <div>
                <Row>
                    <Col span={16} offset={4}>
                        <Steps style={{minHeight: 32}} current={current}>
                            <Step key="DeviceType" title="Device Type"/>
                            <Step key="EnrollAgent" title="Enroll Agent"/>
                            <Step key="Result" title="Result"/>
                        </Steps>
                    </Col>
                    <Col span={16} offset={4}>

                        <Card style={{marginTop: 24}}>
                            <div style={{display: (current === 0 ? 'unset' : 'none')}}>
                                <DeviceType onClickType={this.onClickType}/>
                            </div>
                            <div style={{display: (current === 1 ? 'unset' : 'none')}}>
                                <EnrollAgent />
                            </div>

                            <div style={{display: (current === 2 ? 'unset' : 'none')}}>
                            </div>
                        </Card>
                    </Col>
                </Row>

            </div>
        );
    }
}

export default withConfigContext(Form.create({name: 'add-device'})(AddDevice));