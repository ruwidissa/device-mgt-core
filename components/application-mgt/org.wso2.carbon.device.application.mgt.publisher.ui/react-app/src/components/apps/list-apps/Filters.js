import React from "react";
import {Avatar, Card, Col, Row, Table, Typography, Input, Divider, Checkbox, Select, Button} from "antd";

const {Option} = Select;
const {Title, Text} = Typography;

class Filters extends React.Component {
    constructor(props) {
        super(props);
    }


    render() {
        return (

            <Card>
                <Row>
                    <Col span={6}>
                        <Title level={4}>Filter</Title>
                    </Col>
                </Row>
                <Divider/>

                <Text strong={true}>Category</Text>
                <br/><br/>
                <Select
                    mode="multiple"
                    style={{width: '100%'}}
                    placeholder="All Categories"
                >
                    <Option key={1}>IoT</Option>
                    <Option key={2}>EMM</Option>
                </Select>
                <Divider/>

                <Text strong={true}>Platform</Text>
                <br/><br/>
                <Checkbox>Android</Checkbox><br/>
                <Checkbox>iOS</Checkbox><br/>
                <Checkbox>Windows</Checkbox><br/>
                <Checkbox>Default</Checkbox><br/>
                <Divider/>

                <Text strong={true}>Tags</Text>
                <br/><br/>
                <Select
                    mode="multiple"
                    style={{width: '100%'}}
                    placeholder="All Tags"
                >
                    <Option key={1}>test tag</Option>
                </Select>

                <Divider/>
                <Text strong={true}>Type</Text>
                <br/><br/>
                <Checkbox>Enterprise</Checkbox><br/>
                <Checkbox>Public</Checkbox><br/>
                <Checkbox>Web APP</Checkbox><br/>
                <Checkbox>Web Clip</Checkbox><br/>
                <Divider/>

                <Text strong={true}>Subscription</Text>
                <br/><br/>
                <Checkbox>Free</Checkbox><br/>
                <Checkbox>Paid</Checkbox><br/>
                <Divider/>
            </Card>
        );
    }
}


export default Filters;