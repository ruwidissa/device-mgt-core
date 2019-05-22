import React from "react";
import {Avatar, Row, Col, Typography} from "antd";
import "../../../App.css";

const {Title, Text} = Typography;

class ReleaseView extends React.Component {
    render() {
        const release = this.props.release;
        console.log(release);
        return (
            <div className="release">
                <Row>
                    <Col md={4} sm={6} xs={8} className="release-icon">
                        <img src={release.iconPath}/>
                    </Col>
                    <Col md={18} sm={12}>
                        <Title level={2}>App Name</Title>
                        <Text>{release.version}</Text><br/>
                        <Text type="secondary">{release.description}</Text>
                    </Col>
                </Row>
                <br/>
                <Row>
                    <Col lg={6} md={8} xs={8} className="release-screenshot">
                        <img src={release.screenshotPath1}/>
                    </Col>
                    <Col lg={6} md={8} xs={8} className="release-screenshot">
                        <img src={release.screenshotPath2}/>
                    </Col>
                    <Col lg={6} md={8} xs={8} className="release-screenshot">
                        <img src={release.screenshotPath3}/>
                    </Col>
                </Row>
            </div>
        );
    }
}

export default ReleaseView;