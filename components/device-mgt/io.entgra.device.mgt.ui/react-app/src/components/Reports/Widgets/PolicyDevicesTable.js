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
import { Button, Icon, Table, Tooltip } from 'antd';
import TimeAgo from 'javascript-time-ago';
import moment from 'moment';
// Load locale-specific relative date/time formatting rules.
import en from 'javascript-time-ago/locale/en';
import { withConfigContext } from '../../../context/ConfigContext';
import FeatureListModal from './FeatureListModal';
import { handleApiError } from '../../../js/Utils';

let config = null;

// Table columns for non compliant devices
const columnsNonCompliant = [
  {
    title: 'Device',
    dataIndex: 'deviceName',
    width: 100,
    sorter: (a, b) => a.deviceName.localeCompare(b.deviceName),
  },
  {
    title: 'Type',
    dataIndex: 'deviceType',
    key: 'type',
    // eslint-disable-next-line react/display-name
    render: type => {
      const defaultPlatformIcons = config.defaultPlatformIcons;
      let icon = defaultPlatformIcons.default.icon;
      let color = defaultPlatformIcons.default.color;
      let theme = defaultPlatformIcons.default.theme;

      if (defaultPlatformIcons.hasOwnProperty(type)) {
        icon = defaultPlatformIcons[type].icon;
        color = defaultPlatformIcons[type].color;
        theme = defaultPlatformIcons[type].theme;
      }

      return (
        <span style={{ fontSize: 20, color: color, textAlign: 'center' }}>
          <Icon type={icon} theme={theme} />
        </span>
      );
    },
  },
  {
    title: 'Owner',
    dataIndex: 'owner',
    key: 'owner',
  },
  {
    title: 'Policy',
    dataIndex: 'policyName',
    key: 'policy',
    sorter: (a, b) => a.policyName.localeCompare(b.policyName),
  },
  {
    title: 'Last Failed Time',
    dataIndex: 'lastFailedTime',
    key: 'lastFailedTime',
    render: data => {
      if (data) {
        return (
          <Tooltip title={new Date(data).toString()}>
            {moment(data).fromNow()}
          </Tooltip>
        );
      }
      return 'Not available';
    },
  },
  {
    title: 'Last Success Time',
    dataIndex: 'lastSucceededTime',
    key: 'lastSucceededTime',
    render: data => {
      if (data) {
        return (
          <Tooltip title={new Date(data).toString()}>
            {moment(data).fromNow()}
          </Tooltip>
        );
      }
      return 'Not available';
    },
  },
  {
    title: 'Attempts',
    dataIndex: 'attempts',
    key: 'attempts',
  },
  {
    title: 'Violated Features',
    dataIndex: 'id',
    key: 'violated_features',
    // eslint-disable-next-line react/display-name
    render: id => <FeatureListModal id={id} />,
  },
  {
    title: 'Device Details',
    dataIndex: 'id',
    key: 'device_details',
    // eslint-disable-next-line react/display-name
    render: id => (
      <Button type="primary" size={'small'} icon="book">
        Device Details
      </Button>
    ),
  },
];

// Table columns for compliant devices
const columnsCompliant = [
  {
    title: 'Device',
    dataIndex: 'deviceName',
    width: 100,
    sorter: (a, b) => a.deviceName.localeCompare(b.deviceName),
  },
  {
    title: 'Type',
    dataIndex: 'deviceType',
    key: 'type',
    // eslint-disable-next-line react/display-name
    render: type => {
      const defaultPlatformIcons = config.defaultPlatformIcons;
      let icon = defaultPlatformIcons.default.icon;
      let color = defaultPlatformIcons.default.color;
      let theme = defaultPlatformIcons.default.theme;

      if (defaultPlatformIcons.hasOwnProperty(type)) {
        icon = defaultPlatformIcons[type].icon;
        color = defaultPlatformIcons[type].color;
        theme = defaultPlatformIcons[type].theme;
      }

      return (
        <span style={{ fontSize: 20, color: color, textAlign: 'center' }}>
          <Icon type={icon} theme={theme} />
        </span>
      );
    },
  },
  {
    title: 'Owner',
    dataIndex: 'owner',
    key: 'owner',
  },
  {
    title: 'Policy',
    dataIndex: 'policyName',
    key: 'policy',
    sorter: (a, b) => a.policyName.localeCompare(b.policyName),
  },
  {
    title: 'Last Success Time',
    dataIndex: 'lastSucceededTime',
    key: 'lastSucceededTime',
    render: data => {
      if (data) {
        return (
          <Tooltip title={new Date(data).toString()}>
            {moment(data).fromNow()}
          </Tooltip>
        );
      }
      return 'Not available';
    },
  },
  {
    title: 'Attempts',
    dataIndex: 'attempts',
    key: 'attempts',
  },
  {
    title: 'Device Details',
    dataIndex: 'id',
    key: 'device_details',
    // eslint-disable-next-line react/display-name
    render: id => (
      <Button type="primary" size={'small'} icon="book">
        Device Details
      </Button>
    ),
  },
];

class PolicyDevicesTable extends React.Component {
  constructor(props) {
    super(props);
    config = this.props.context;
    TimeAgo.addLocale(en);
    this.state = {
      data: [],
      pagination: {},
      loading: false,
      selectedRows: [],
      paramsObj: {},
    };
  }

  rowSelection = {
    onChange: (selectedRowKeys, selectedRows) => {
      this.setState({
        selectedRows: selectedRows,
      });
    },
  };

  componentDidMount() {
    this.fetchData();
  }

  // Rerender component when parameters change
  componentDidUpdate(prevProps, prevState, snapshot) {
    if (
      prevProps.isCompliant !== this.props.isCompliant ||
      prevProps.policyReportData !== this.props.policyReportData
    ) {
      this.fetchData();
    }
  }

  // fetch data from api
  fetchData = (params = {}) => {
    // const policyReportData = this.props;
    this.setState({ loading: true });
    // get current page
    const currentPage = params.hasOwnProperty('page') ? params.page : 1;

    let extraParams;

    if (this.props.policyReportData.policy) {
      extraParams = {
        policy: this.props.policyReportData.policy,
        from: this.props.policyReportData.from,
        to: this.props.policyReportData.to,
        offset: 10 * (currentPage - 1), // calculate the offset
        limit: 10,
      };
    } else {
      extraParams = {
        from: this.props.policyReportData.from,
        to: this.props.policyReportData.to,
        offset: 10 * (currentPage - 1), // calculate the offset
        limit: 10,
      };
    }

    const encodedExtraParams = Object.keys(extraParams)
      .map(key => key + '=' + extraParams[key])
      .join('&');

    let apiUrl;

    if (this.props.isCompliant) {
      apiUrl =
        window.location.origin +
        config.serverConfig.invoker.uri +
        config.serverConfig.invoker.deviceMgt +
        '/devices/compliance/true?' +
        encodedExtraParams;
    } else {
      apiUrl =
        window.location.origin +
        config.serverConfig.invoker.uri +
        config.serverConfig.invoker.deviceMgt +
        '/devices/compliance/false?' +
        encodedExtraParams;
    }

    // send request to the invoker
    axios
      .get(apiUrl)
      .then(res => {
        if (res.status === 200) {
          const pagination = { ...this.state.pagination };
          this.setState({
            loading: false,
            data: res.data.data,
            pagination,
          });
        }
      })
      .catch(error => {
        handleApiError(error, 'Error occurred while trying to load devices.');
        this.setState({ loading: false });
      });
  };

  handleTableChange = (pagination, filters, sorter) => {
    const pager = { ...this.state.pagination };
    pager.current = pagination.current;
    this.setState({
      pagination: pager,
    });
    this.fetchData({
      results: pagination.pageSize,
      page: pagination.current,
      sortField: sorter.field,
      sortOrder: sorter.order,
      ...filters,
    });
  };

  render() {
    let { data, pagination, loading } = this.state;
    const { isCompliant } = this.props;

    return (
      <div>
        <Table
          columns={isCompliant ? columnsCompliant : columnsNonCompliant}
          rowKey={record => record.id}
          dataSource={data.complianceData}
          pagination={{
            ...pagination,
            size: 'small',
            // position: "top",
            total: data.count,
            showTotal: (total, range) =>
              `showing ${range[0]}-${range[1]} of ${total} devices`,
            // showQuickJumper: true
          }}
          loading={loading}
          onChange={this.handleTableChange}
          rowSelection={this.rowSelection}
        />
      </div>
    );
  }
}

export default withConfigContext(PolicyDevicesTable);
