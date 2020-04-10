/*
 * Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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

import React from 'react';
import { Card, Col, Row, Typography, Input, Divider } from 'antd';
import AppsTable from './components/ApssTable';
import Filters from './components/Filters';

const { Title } = Typography;
const Search = Input.Search;

class Apps extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      filters: {},
    };
    this.appName = '';
  }

  setFilters = filters => {
    if (this.appName === '' && filters.hasOwnProperty('appName')) {
      delete filters.appName;
    } else {
      filters.appName = this.appName;
    }
    this.setState({
      filters,
    });
  };

  setSearchText = appName => {
    const filters = { ...this.state.filters };
    this.appName = appName;
    if (appName === '' && filters.hasOwnProperty('appName')) {
      delete filters.appName;
    } else {
      filters.appName = appName;
    }
    this.setState({
      filters,
    });
  };

  onChangeSearchText = e => {
    const filters = { ...this.state.filters };
    const appName = e.target.value;
    if (appName === '' && filters.hasOwnProperty('appName')) {
      delete filters.appName;
      this.setState({
        filters,
      });
    }
  };

  render() {
    const { filters } = this.state;
    return (
      <Card>
        <Row gutter={28}>
          <Col md={6}>
            <Filters setFilters={this.setFilters} />
          </Col>
          <Col md={18}>
            <Row>
              <Col span={6}>
                <Title level={4}>Apps</Title>
              </Col>
              <Col span={18} style={{ textAlign: 'right' }}>
                <Search
                  placeholder="Search by app name"
                  onSearch={this.setSearchText}
                  onChange={this.onChangeSearchText}
                  style={{ width: 240, zIndex: 0 }}
                />
              </Col>
            </Row>
            <Divider dashed={true} />
            <AppsTable filters={filters} />
          </Col>
        </Row>
      </Card>
    );
  }
}

export default Apps;
