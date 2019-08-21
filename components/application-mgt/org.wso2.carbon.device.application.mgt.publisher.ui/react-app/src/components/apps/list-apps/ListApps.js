/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from "react";
import {Card, Col, Row, Typography, Input, Divider} from "antd";
import AppsTable from "./appsTable/AppsTable";
import Filters from "./Filters";
import AppDetailsDrawer from "./AppDetailsDrawer/AppDetailsDrawer";

const {Title} = Typography;
const Search = Input.Search;


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

    setSearchText = (appName) => {
        const filters = {...this.state.filters};
        if (appName === '' && filters.hasOwnProperty("appName")) {
            delete filters["appName"];
        } else {
            filters.appName = appName;
        }
        this.setState({
            filters
        });
        console.log(filters);
    };

    render() {
        const {isDrawerVisible, filters} = this.state;
        return (

            <Card>
                <Row gutter={28}>
                    <Col md={6}>
                        <Filters setFilters={this.setFilters}/>
                    </Col>
                    <Col md={18}>
                        <Row>
                            <Col span={6}>
                                <Title level={4}>Apps</Title>
                            </Col>
                            <Col span={18} style={{textAlign: "right"}}>
                                <Search
                                    placeholder="input search text"
                                    onSearch={this.setSearchText}
                                    style={{width: 200}}
                                />
                            </Col>
                        </Row>
                        <Divider dashed={true}/>
                        <AppsTable filters={filters} showDrawer={this.showDrawer}/>
                        <AppDetailsDrawer visible={isDrawerVisible} onClose={this.closeDrawer}
                                          app={this.state.selectedApp}/>
                    </Col>
                </Row>
            </Card>
        );
    }
}

export default ListApps;