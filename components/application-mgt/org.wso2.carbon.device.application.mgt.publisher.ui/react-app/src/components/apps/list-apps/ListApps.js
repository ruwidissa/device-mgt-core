import React from "react";
import {Avatar, Card, Col, Row, Table, Typography, Input, Divider, Checkbox, Select, Button} from "antd";
import {connect} from "react-redux";
import {getApps} from "../../../js/actions";
import AppsTable from "./AppsTable";
import Filters from "./Filters";
import AppDetailsDrawer from "./AppDetailsDrawer";

const {Option} = Select;
const {Title, Text} = Typography;
const Search = Input.Search;
// connecting state.apps with the component
// const mapStateToProps = state => {
//     return {apps: state.apps}
// };


class ListApps extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            isDrawerVisible: false,
            selectedApp: null,
            filters: {}
        }
    }

    //handler to show app drawer
    showDrawer = (app) => {
        this.setState({
            isDrawerVisible: true,
            selectedApp: app
        });
    };

    // handler to close the app drawer
    closeDrawer = () => {
        this.setState({
            isDrawerVisible: false
        })
    };

    setFilters = (filters) => {
        this.setState({
            filters
        });
    };

    render() {
        const {isDrawerVisible, filters} = this.state;
        return (
            <Row gutter={28}>
                <Col md={6}>
                    <Filters setFilters={this.setFilters}/>
                </Col>
                <Col md={18}>
                    <Card>
                        <Row>
                            <Col span={6}>
                                <Title level={4}>Apps</Title>
                            </Col>
                            <Col span={18} style={{textAlign: "right"}}>
                                <Search
                                    placeholder="input search text"
                                    // onSearch={value => console.log(value)}
                                    style={{width: 200}}
                                />
                            </Col>
                        </Row>
                        <Divider dashed={true}/>
                        <AppsTable filters={filters} showDrawer={this.showDrawer}/>
                        <AppDetailsDrawer visible={isDrawerVisible} onClose={this.closeDrawer}
                                          app={this.state.selectedApp}/>
                    </Card>
                </Col>
            </Row>
        );
    }
}

// const ListApps = connect(mapStateToProps, {getApps})(ConnectedListApps);

export default ListApps;