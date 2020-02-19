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
import axios from 'axios';
import { Card, Col, Icon, message, notification, Row } from 'antd';
import TimeAgo from 'javascript-time-ago';
// Load locale-specific relative date/time formatting rules.
import en from 'javascript-time-ago/locale/en';
import { withConfigContext } from '../../../../../../components/ConfigContext';

let apiUrl;

class Index extends React.Component {
  constructor(props) {
    super(props);
    TimeAgo.addLocale(en);
    this.state = {
      data: [],
      pagination: {},
      loading: false,
      selectedRows: [],
    };
  }

  componentDidMount() {
    this.fetchUsers();
  }

  // fetch data from api
  fetchUsers = (params = {}) => {
    const config = this.props.context;
    this.setState({ loading: true });

    apiUrl =
      window.location.origin +
      config.serverConfig.invoker.uri +
      config.serverConfig.invoker.deviceMgt +
      '/device-types';

    // send request to the invokerss
    axios
      .get(apiUrl)
      .then(res => {
        if (res.status === 200) {
          const pagination = { ...this.state.pagination };
          this.setState({
            loading: false,
            data: JSON.parse(res.data.data),
            pagination,
          });
        }
      })
      .catch(error => {
        if (error.hasOwnProperty('response') && error.response.status === 401) {
          // todo display a popop with error
          message.error('You are not logged in');
          window.location.href = window.location.origin + '/entgra/login';
        } else {
          notification.error({
            message: 'There was a problem',
            duration: 0,
            description: 'Error occurred while trying to load device types.',
          });
        }

        this.setState({ loading: false });
      });
  };

  handleTableChange = (pagination, filters, sorter) => {
    const pager = { ...this.state.pagination };
    pager.current = pagination.current;
    this.setState({
      pagination: pager,
    });
    this.fetch({
      results: pagination.pageSize,
      page: pagination.current,
      sortField: sorter.field,
      sortOrder: sorter.order,
      ...filters,
    });
  };

  render() {
    const { data } = this.state;
    const { Meta } = Card;
    const itemCard = data.map(data => (
      <Col span={5} key={data.id}>
        <Card
          size="default"
          style={{ width: 200 }}
          bordered={true}
          actions={[
            <Icon type="setting" key="setting" />,
            <Icon type="edit" key="edit" />,
          ]}
        >
          <Meta
            avatar={<Icon type="desktop" key="device-types" />}
            title={data.name}
          />
        </Card>
      </Col>
    ));
    return (
      <div style={{ background: '#ECECEC', padding: '20px' }}>
        <Row gutter={16}>{itemCard}</Row>
      </div>
    );
  }
}

export default withConfigContext(Index);
