import React from "react";
import AppCard from "./AppCard";
import {Col, Row} from "antd";
import {connect} from "react-redux";

// connecting state.articles with the component
const mapStateToProps= state => {
    return {apps : state.apps}
};


class ConnectedAppList extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <Row gutter={16}>
                {this.props.apps.map(app => (
                    <Col key={app.id} xs={24} sm={12} md={6} lg={6}>
                        <AppCard key={app.id} title={app.title}
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

const AppList = connect(mapStateToProps)(ConnectedAppList);

export default AppList;