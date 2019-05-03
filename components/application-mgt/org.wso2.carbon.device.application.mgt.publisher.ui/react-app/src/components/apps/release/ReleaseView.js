import React from "react";
import {Avatar, Row, Col, Typography} from "antd";

const {Title, Text} = Typography;

class ReleaseView extends React.Component {
    render() {
        const release = this.props.release;
        return (
            <div>
                <Row>
                    <Col span={4}>
                        <Avatar size={128} shape="square"
                                src={release.iconPath}/>
                    </Col>
                    <Col span={18}>
                        <Title level={2}>App Name</Title>
                        <Text>{release.version}</Text><br/>
                        <Text type="secondary">{release.description}</Text>
                    </Col>
                </Row>
                <br/>
                <Row>
                    <Col span={6}>
                        <img style={{width:"100%"}} src={release.screenshotPath1}/>
                    </Col>
                </Row>
            </div>
        );
    }
}

export default ReleaseView;