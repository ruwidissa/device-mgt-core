import React from "react";
import {Avatar, Row, Col, Typography, Button} from "antd";
import "../../../App.css";

const {Title, Text} = Typography;

class ReleaseView extends React.Component {
    render() {
        const release = this.props.release;
        console.log(release);
        return (
            <div className="release">
                <Row>
                    <Col xl={4} sm={6} xs={8} className="release-icon">
                        <img src={release.iconPath} alt="icon"/>
                    </Col>
                    <Col xl={10} sm={11} className="release-title">
                        <Title level={2}>App Name</Title>
                        <Text>{release.version}</Text><br/>
                        <Text type="secondary">{release.description}</Text><br/>
                    </Col>
                    <Col xl={8} md={10} sm={24} xs={24} style={{float: "right"}}>
                        <div>
                            <Button.Group style={{float: "right"}}>
                                <Button htmlType="button" icon="shop">Open in store</Button>
                                <Button htmlType="button" type="primary" icon="edit">edit</Button>
                            </Button.Group>
                        </div>

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