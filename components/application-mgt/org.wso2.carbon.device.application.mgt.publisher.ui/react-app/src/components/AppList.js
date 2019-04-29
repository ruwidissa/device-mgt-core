import React from "react";
import AppCard from "./AppCard";
import {Col, Row} from "antd";

class AppList extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            apps: [
                {
                    id: 1,
                    title: 'Hi',
                    platform: 'android',
                    description: 'lorem',
                    subType: 'FREE',
                    type: 'ENTERPRISE'
                }
            ]
        }
    }

    render() {
        return (
            <Row gutter={16}>
                {this.state.apps.map(app => (
                    <Col xs={24} sm={12} md={6} lg={6}>
                        <AppCard title={app.title}
                                 platform={app.platform}
                                 type={app.type}
                                 subType={app.subType}
                                 description={app.description}/>
                    </Col>
                ))}
            </Row>
        );
    }
}

export default AppList;