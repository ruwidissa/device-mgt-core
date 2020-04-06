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

import React from 'react';
import axios from 'axios';
import { Tag, Table, Typography, Button, Alert } from 'antd';
import TimeAgo from 'javascript-time-ago';

// Load locale-specific relative date/time formatting rules.
import en from 'javascript-time-ago/locale/en';
import { withConfigContext } from '../../../../../../../../../../../../components/context/ConfigContext';
import { handleApiError } from '../../../../../../../../../../../../services/utils/errorHandler';
import Authorized from '../../../../../../../../../../../../components/Authorized';

const { Text } = Typography;

const columns = [
  {
    title: 'Device',
    dataIndex: 'device',
    width: 100,
    render: device => device.name,
  },
  {
    title: 'Owner',
    dataIndex: 'device',
    key: 'owner',
    render: device => device.enrolmentInfo.owner,
  },
  {
    title: 'Action Type',
    dataIndex: 'actionType',
    key: 'actionType',
    render: actionType => actionType.toLowerCase(),
  },
  {
    title: 'Action',
    dataIndex: 'action',
    key: 'action',
    // eslint-disable-next-line react/display-name
    render: action => {
      action = action.toLowerCase();
      let color = 'fff';
      if (action === 'subscribed') {
        color = '#6ab04c';
      } else if (action === 'unsubscribed') {
        color = '#f0932b';
      }
      return <span style={{ color: color }}>{action}</span>;
    },
  },
  {
    title: 'Triggered By',
    dataIndex: 'actionTriggeredBy',
    key: 'actionTriggeredBy',
  },
  {
    title: 'Action Triggered At',
    dataIndex: 'actionTriggeredTimestamp',
    key: 'actionTriggeredTimestamp',
  },
  {
    title: 'Action Status',
    dataIndex: 'status',
    key: 'actionStatus',
    // eslint-disable-next-line react/display-name
    render: status => {
      let color = '#f9ca24';
      switch (status) {
        case 'COMPLETED':
          color = '#badc58';
          break;
        case 'REPEATED':
          color = '#6ab04c';
          break;
        case 'ERROR':
        case 'INVALID':
        case 'UNAUTHORIZED':
          color = '#ff7979';
          break;
        case 'IN_PROGRESS':
          color = '#f9ca24';
          break;
        case 'PENDING':
          color = '#636e72';
          break;
      }
      return <Tag color={color}>{status.toLowerCase()}</Tag>;
    },
  },
  {
    title: 'Device Status',
    dataIndex: 'device',
    key: 'deviceStatus',
    // eslint-disable-next-line react/display-name
    render: device => {
      const status = device.enrolmentInfo.status.toLowerCase();
      let color = '#f9ca24';
      switch (status) {
        case 'active':
          color = '#badc58';
          break;
        case 'created':
          color = '#6ab04c';
          break;
        case 'removed':
          color = '#ff7979';
          break;
        case 'inactive':
          color = '#f9ca24';
          break;
        case 'blocked':
          color = '#636e72';
          break;
      }
      return <Tag color={color}>{status}</Tag>;
    },
  },
];

class SubscriptionDetails extends React.Component {
  constructor(props) {
    super(props);
    TimeAgo.addLocale(en);
    this.state = {
      data: [],
      pagination: {},
      loading: false,
      selectedRows: [],
      deviceGroups: [],
      groupModalVisible: false,
      selectedGroupId: [],
      isForbidden: false,
    };
  }

  componentDidMount() {
    this.fetch();
  }

  // fetch data from api
  fetch = (params = {}) => {
    const config = this.props.context;
    this.setState({ loading: true });
    // get current page
    const currentPage = params.hasOwnProperty('page') ? params.page : 1;

    const extraParams = {
      offset: 10 * (currentPage - 1), // calculate the offset
      limit: 10,
      requireDeviceInfo: true,
    };

    const encodedExtraParams = Object.keys(extraParams)
      .map(key => key + '=' + extraParams[key])
      .join('&');

    // send request to the invoker
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.store +
          `/admin/subscription/${this.props.uuid}?` +
          encodedExtraParams,
      )
      .then(res => {
        if (res.status === 200) {
          this.setState({
            loading: false,
            data: res.data.data,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Something went wrong when trying to load subscription data.',
          true,
        );
        if (error.hasOwnProperty('response') && error.response.status === 403) {
          this.setState({
            isForbidden: true,
            loading: false,
          });
        } else {
          this.setState({
            loading: false,
          });
        }
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
    const { data, pagination, loading } = this.state;
    return (
      <Authorized
        permission="/permission/admin/app-mgt/store/admin/subscription/view"
        yes={
          <div>
            <div style={{ paddingBottom: 24 }}>
              <Text>
                The following are the subscription details of the application in
                each respective device.
              </Text>
            </div>
            <div style={{ textAlign: 'right', paddingBottom: 6 }}>
              <Button icon="sync" onClick={this.fetch}>
                Refresh
              </Button>
            </div>
            <Table
              columns={columns}
              rowKey={record =>
                record.device.deviceIdentifier +
                record.device.enrolmentInfo.owner +
                record.device.enrolmentInfo.ownership
              }
              dataSource={data.data}
              pagination={{
                ...pagination,
                size: 'small',
                total: data.recordsTotal,
                showTotal: (total, range) =>
                  `showing ${range[0]}-${range[1]} of ${total} devices`,
              }}
              onChange={this.handleTableChange}
              loading={loading}
              scroll={{ x: 1000 }}
            />
          </div>
        }
        no={
          <Alert
            type="warning"
            message="You don't have permission to view subscription details"
          />
        }
      />
    );
  }
}

export default withConfigContext(SubscriptionDetails);
