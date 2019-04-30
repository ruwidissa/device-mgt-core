import React from "react";
import AppCard from "./AppCard";
import {Col, Row} from "antd";
import {connect} from "react-redux";
import {getApps} from "../../js/actions";

// connecting state.apps with the component
const mapStateToProps= state => {
    return {apps : state.apps}
};


class ConnectedAppList extends React.Component {
    constructor(props) {
        super(props);
    }
    componentDidMount() {
        this.props.getApps();
    }

    render() {
        return (
            <Row gutter={16}>
                {this.props.apps.map(app => (
                    <Col key={app.id} xs={24} sm={12} md={6} lg={6}>
                        <AppCard key={app.id}
                                 app = {app}
                                 name={app.name}
                                 platform={app.deviceType}
                                 type={app.type}
                                 subType={app.subType}
                                 description={app.description}/>
                    </Col>
                ))}
            </Row>
        );
    }
}

const AppList = connect(mapStateToProps,{getApps})(ConnectedAppList);

export default AppList;