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
import {Card, Col, Row, Typography, Input, Divider, notification} from "antd";
import AppsTable from "./appsTable/AppsTable";
import Filters from "./Filters";
import AppDetailsDrawer from "./AppDetailsDrawer/AppDetailsDrawer";
import axios from "axios";

const {Title} = Typography;
const Search = Input.Search;

class ListApps extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            filters: {}
        }
    }

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
    };

    render() {
        const {isDrawerVisible, filters} = this.state;
        return (
            <Card>
                <Row gutter={28}>
                    <Col md={7}>
                        <Filters setFilters={this.setFilters}/>
                    </Col>
                    <Col md={17}>
                        <Row>
                            <Col span={6}>
                                <Title level={4}>Apps</Title>
                            </Col>
                            <Col span={18} style={{textAlign: "right"}}>
                                <Search
                                    placeholder="input search text"
                                    onSearch={this.setSearchText}
                                    style={{width: 170}}
                                />
                            </Col>
                        </Row>
                        <Divider dashed={true}/>
                        <AppsTable filters={filters}/>
                    </Col>
                </Row>
            </Card>
        );
    }
}

export default ListApps;