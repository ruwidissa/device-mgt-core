import React from 'react';
import {Tabs, Row, Col, Switch, Menu,Input, Typography, Form, Checkbox, Select} from "antd";
import {withConfigContext} from "../../context/ConfigContext";
const { Title } = Typography;
const { TabPane } = Tabs;
const {Option} = Select;


class ConfigureProfile extends React.Component {
    constructor(props) {
        super(props);
        this.config =  this.props.context;
    };

    render() {
        const { getFieldDecorator } = this.props.form;
        return (
            <div>
                <Tabs tabPosition={"left"} type={"card"} size={"large"}>
                    <TabPane tab="Passcode Policy" key="1">
                        <Row>
                            <Col offset={0} span={8}>
                                <Title level={4}>Passcode Policy</Title>
                            </Col>
                            <Col offset={12} span={2} >
                                <Switch checkedChildren=" ON " unCheckedChildren="OFF"/>
                            </Col>
                        </Row>
                        <Row>
                            Enforce a configured passcode policy on Android devices.
                            Once this profile is applied, the device
                            owners won't be able to modify the password settings on their devices.
                        </Row>
                        <div>
                            <Form>
                                <Form.Item >
                                    {getFieldDecorator('allowSimpleValue', {
                                    })(<Checkbox>Allow simple value</Checkbox>)}
                                </Form.Item>
                                <Form.Item >
                                    {getFieldDecorator('remember', {
                                    })(<Checkbox>Require alphanumeric value</Checkbox>)}
                                </Form.Item>
                                <Form.Item label="Minimum passcode length" style={{display: "block"}}>
                                    {getFieldDecorator('minPasscodeLength', {
                                        initialValue: '0'
                                    })(
                                        <Select>
                                            <Option key="0">None</Option>
                                            <Option key="4">4</Option>
                                            <Option key="5">5</Option>
                                            <Option key="6">6</Option>
                                            <Option key="7">7</Option>
                                            <Option key="8">8</Option>
                                            <Option key="9">9</Option>
                                            <Option key="10">10</Option>
                                            <Option key="11">11</Option>
                                            <Option key="12">12</Option>
                                            <Option key="13">13</Option>
                                            <Option key="14">14</Option>
                                            <Option key="15">15</Option>
                                        </Select>
                                    )}
                                </Form.Item>
                                <Form.Item label="Minimum number of complex characters" style={{display: "block"}}>
                                    {getFieldDecorator('minComplexChar', {
                                        initialValue: '0'
                                    })(
                                        <Select>
                                            <Option key="0">None</Option>
                                            <Option key="1">1</Option>
                                            <Option key="2">2</Option>
                                            <Option key="3">3</Option>
                                            <Option key="4">4</Option>
                                            <Option key="5">5</Option>
                                        </Select>
                                    )}
                                </Form.Item>
                                <Form.Item label="Maximum passcode age in days* " style={{display: "block"}}>
                                    {getFieldDecorator('maxPasscodeAge', {

                                    })(
                                        <Input/>
                                    )}
                                </Form.Item>
                                <Form.Item label="Passcode history*" style={{display: "block"}}>
                                    {getFieldDecorator('passcodeHistory', {
                                    })(
                                        <Input/>
                                    )}
                                </Form.Item>
                                <Form.Item label="Maximum number of failed attempts" style={{display: "block"}}>
                                    {getFieldDecorator('maxFailedAttemps', {
                                        initialValue: '0'
                                    })(
                                        <Select>
                                            <Option key="0">None</Option>
                                            <Option key="3">3</Option>
                                            <Option key="4">4</Option>
                                            <Option key="5">5</Option>
                                            <Option key="6">6</Option>
                                            <Option key="7">7</Option>
                                            <Option key="8">8</Option>
                                            <Option key="9">9</Option>
                                            <Option key="10">10</Option>
                                        </Select>
                                    )}
                                </Form.Item>
                            </Form>
                        </div>
                    </TabPane>
                    <TabPane tab="Restrictions" key="2">
                        <Row>
                            <Col offset={0} span={8}>
                                <Title level={4}>Restrictions</Title>
                            </Col>
                            <Col offset={12} span={2} >
                                <Switch checkedChildren=" ON " unCheckedChildren="OFF" />
                            </Col>
                        </Row>
                        <Row>
                            <p>This configurations can be used to restrict certain settings on an Android device.
                            Once this configuration profile is installed on a device, corresponding users will
                            not be able to modify these settings on their devices.</p>
                        </Row>
                    </TabPane>
                    <TabPane tab="Encryption Settings" key="3">
                        <Row>
                            <Col offset={0} span={8}>
                                <Title level={4}>Encryption Settings</Title>
                            </Col>
                            <Col offset={12} span={2} >
                                <Switch checkedChildren=" ON " unCheckedChildren="OFF" />
                            </Col>
                        </Row>
                        <Row>
                            <p>This configuration can be used to encrypt data on an Android device,
                            when the device is locked and make it readable when the passcode is entered.
                            Once this configuration profile is installed on a device, corresponding users will
                            not be able to modify these settings on their devices.</p>
                        </Row>
                    </TabPane>
                    <TabPane tab="Wi-Fi Settings" key="4">
                        <Row>
                            <Col offset={0} span={8}>
                                <Title level={4}>Wi-Fi Settings</Title>
                            </Col>
                            <Col offset={12} span={2} >
                                <Switch checkedChildren=" ON " unCheckedChildren="OFF"/>
                            </Col>
                        </Row>
                        <Row>
                            <p>This configurations can be used to configure Wi-Fi access on an Android device.
                            Once this configuration profile is installed on a device, corresponding
                            users will not be able to modify these settings on their devices.</p>
                        </Row>
                    </TabPane>
                    <TabPane tab="Global Proxy Settings" key="5">
                        <Row>
                            <Col offset={0} span={8}>
                                <Title level={4}>Global Proxy Settings</Title>
                            </Col>
                            <Col offset={12} span={2} >
                                <Switch checkedChildren=" ON " unCheckedChildren="OFF" />
                            </Col>
                        </Row>
                        <Row>
                            <p>This configurations can be used to set a network-independent global HTTP
                            proxy on an Android device. Once this configuration profile is installed on a device,
                            all the network traffic will be routed through the proxy server.</p>
                        </Row>
                    </TabPane>
                    <TabPane tab="Virtual Private Network" key="6">
                        <div>
                            <Row>
                                <Col offset={0} span={8}>
                                    <Title level={4}>VPN Settings</Title>
                                </Col>
                                <Col offset={12} span={2} >
                                    <Switch checkedChildren=" ON " unCheckedChildren="OFF" />
                                </Col>
                            </Row>
                            <Row>
                                <p>Configure the OpenVPN settings on Android devices. In order to enable this,
                                device needs to have "OpenVPN for Android" application installed.</p>
                            </Row>
                        </div>
                        <div>
                            <Row>
                                <Col offset={0} span={8}>
                                    <Title level={4}>Always On VPN Settings</Title>
                                </Col>
                                <Col offset={12} span={2} >
                                    <Switch checkedChildren=" ON " unCheckedChildren="OFF" />
                                </Col>
                            </Row>
                            <Row>
                                <p>Configure an always-on VPN connection through a specific VPN client application.</p>
                            </Row>
                        </div>
                    </TabPane>
                    <TabPane tab="Certificate Install" key="7">
                        <Row>
                            <Col offset={0} span={8}>
                                <Title level={4}>Certificate Install Settings</Title>
                            </Col>
                            <Col offset={12} span={2} >
                                <Switch checkedChildren=" ON " unCheckedChildren="OFF" />
                            </Col>
                        </Row>
                        <Row>
                            <p>Configure the certificate install settings on Android devices.</p>
                        </Row>
                    </TabPane>
                    <TabPane tab="Work-Profile Configurations" key="8">
                        <Row>
                            <Col offset={0} span={8}>
                                <Title level={4}>Work-Profile Configurations</Title>
                            </Col>
                            <Col offset={12} span={2} >
                                <Switch checkedChildren=" ON " unCheckedChildren="OFF" />
                            </Col>
                        </Row>
                        <Row>
                            <p>The configurations below can be applied to the devices where the agent
                            is running in Android Work-Profile.</p>
                        </Row>
                    </TabPane>
                    <TabPane tab="COSU Profile Configuration" key="9">
                        <Row>
                            <Col offset={0} span={8}>
                                <Title level={4}>COSU Profile Configuration</Title>
                            </Col>
                            <Col offset={12} span={2} >
                                <Switch checkedChildren=" ON " unCheckedChildren="OFF" />
                            </Col>
                        </Row>
                        <Row>
                            <p>This policy can be used to configure the profile of COSU Devices.</p>
                        </Row>
                    </TabPane>
                    <TabPane tab="Application Restrictions" key="10">
                        <Row>
                            <Col offset={0} span={8}>
                                <Title level={4}>Application Restrictions</Title>
                            </Col>
                            <Col offset={12} span={2} >
                                <Switch checkedChildren=" ON " unCheckedChildren="OFF" />
                            </Col>
                        </Row>
                        <Row>
                            <p>This configuration can be used to create a black list or white list of applications.</p>
                        </Row>
                    </TabPane>
                    <TabPane tab="Runtime Permission Policy" key="11">
                        <Row>
                            <Col offset={0} span={8}>
                                <Title level={4}>Runtime Permission Policy</Title>
                            </Col>
                            <Col offset={12} span={2} >
                                <Switch checkedChildren=" ON " unCheckedChildren="OFF" />
                            </Col>
                        </Row>
                        <Row>
                            <p>This configuration can be used to set a runtime permission policy to an Android Device.</p>
                        </Row>
                    </TabPane>
                    <TabPane tab="System Update Policy (COSU)" key="12">
                        <Row>
                            <Col offset={0} span={8}>
                                <Title level={4}>System Update Policy (COSU)</Title>
                            </Col>
                            <Col offset={12} span={2} >
                                <Switch checkedChildren=" ON " unCheckedChildren="OFF" />
                            </Col>
                        </Row>
                        <Row>
                            <p>This configuration can be used to set a passcode policy to an Android Device.
                            Once this configuration profile is installed on a device,
                            corresponding users will not be able to modify these settings on their devices.</p>
                        </Row>
                    </TabPane>
                    <TabPane tab="Enrollment Application Install" key="13">
                        <Row>
                            <Col offset={0} span={8}>
                                <Title level={4}>Enrollment Application Install</Title>
                            </Col>
                            <Col offset={12} span={2} >
                                <Switch checkedChildren=" ON " unCheckedChildren="OFF" />
                            </Col>
                        </Row>
                        <Row>
                            <p>This configuration can be used to install applications during Android device enrollment.</p>
                        </Row>
                    </TabPane>
                </Tabs>
            </div>
        );
    }
}

export default withConfigContext(Form.create({name: 'add-policy'})(ConfigureProfile));