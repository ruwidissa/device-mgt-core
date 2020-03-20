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
import {
  Icon,
  message,
  Modal,
  notification,
  Select,
  Table,
  Tag,
  Tooltip,
} from 'antd';
import TimeAgo from 'javascript-time-ago';
// Load locale-specific relative date/time formatting rules.
import en from 'javascript-time-ago/locale/en';
import { withConfigContext } from '../../../../../../components/ConfigContext';
import BulkActionBar from './components/BulkActionBar';
import { Link } from 'react-router-dom';

let config = null;

const columns = [
  {
    title: 'Device',
    dataIndex: 'name',
    width: 100,
  },
  {
    title: 'Type',
    dataIndex: 'type',
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
    // todo add filtering options
  },
  {
    title: 'Owner',
    dataIndex: 'enrolmentInfo',
    key: 'owner',
    render: enrolmentInfo => enrolmentInfo.owner,
    // todo add filtering options
  },
  {
    title: 'Ownership',
    dataIndex: 'enrolmentInfo',
    key: 'ownership',
    render: enrolmentInfo => enrolmentInfo.ownership,
    // todo add filtering options
  },
  {
    title: 'Status',
    dataIndex: 'enrolmentInfo',
    key: 'status',
    // eslint-disable-next-line react/display-name
    render: enrolmentInfo => {
      const status = enrolmentInfo.status.toLowerCase();
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
    // todo add filtering options
  },
  {
    title: 'Last Updated',
    dataIndex: 'enrolmentInfo',
    key: 'dateOfLastUpdate',
    // eslint-disable-next-line react/display-name
    render: data => {
      const { dateOfLastUpdate } = data;
      const timeAgoString = getTimeAgo(dateOfLastUpdate);
      return (
        <Tooltip title={new Date(dateOfLastUpdate).toString()}>
          {timeAgoString}
        </Tooltip>
      );
    },
    // todo add filtering options
  },
  {
    title: '',
    dataIndex: 'deviceIdentifier',
    key: 'actions',
    // eslint-disable-next-line react/display-name
    render: (data, row) => {
      const { type, deviceIdentifier } = row;
      return (
        <Link to={`/entgra/geo/history/${type}/${deviceIdentifier}`}>
          <Icon type="environment" /> Location History
        </Link>
      );
    },
  },
];

const getTimeAgo = time => {
  const timeAgo = new TimeAgo('en-US');
  return timeAgo.format(time);
};

class DeviceTable extends React.Component {
  constructor(props) {
    super(props);
    config = this.props.context;
    TimeAgo.addLocale(en);
    this.state = {
      data: [],
      pagination: {},
      loading: false,
      selectedRows: [],
      deviceGroups: [],
      groupModalVisible: false,
      selectedGroupId: [],
      selectedRowKeys: [],
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
          config.serverConfig.invoker.deviceMgt +
          '/devices?' +
          encodedExtraParams,
      )
      .then(res => {
        if (res.status === 200) {
          const pagination = { ...this.state.pagination };
          this.setState({
            loading: false,
            data: res.data.data.devices,
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
            description: 'Error occurred while trying to load devices.',
          });
        }

        this.setState({ loading: false });
      });
  };

  deleteDevice = () => {
    const config = this.props.context;
    this.setState({ loading: true });

    const deviceData = this.state.selectedRows.map(obj => obj.deviceIdentifier);

    // send request to the invoker
    axios
      .put(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/admin/devices/permanent-delete',
        deviceData,
        { headers: { 'Content-Type': 'application/json' } },
      )
      .then(res => {
        if (res.status === 200) {
          this.fetch();
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
            description: 'Error occurred while trying to delete devices.',
          });
        }

        this.setState({ loading: false });
      });
  };

  disenrollDevice = () => {
    const config = this.props.context;
    this.setState({ loading: true });

    const deviceData = this.state.selectedRows[0];

    // send request to the invoker
    axios
      .delete(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/devices/type/' +
          deviceData.type +
          '/id/' +
          deviceData.deviceIdentifier,
        { headers: { 'Content-Type': 'application/json' } },
      )
      .then(res => {
        if (res.status === 200) {
          this.fetch();
          this.setState({
            selectedRowKeys: [],
          });
          notification.success({
            message: 'Done',
            duration: 4,
            description: 'Successfully dis-enrolled the device.',
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
            description: 'Error occurred while trying to dis-enroll devices.',
          });
        }

        this.setState({ loading: false });
      });
  };

  addDevicesToGroup = groupId => {
    const config = this.props.context;
    this.setState({ loading: true });

    let apiUrl;
    let deviceData;
    if (this.state.selectedRows.length === 1) {
      apiUrl =
        window.location.origin +
        config.serverConfig.invoker.uri +
        config.serverConfig.invoker.deviceMgt +
        '/groups/device/assign';
      deviceData = {
        deviceIdentifier: {
          id: this.state.selectedRows[0].deviceIdentifier,
          type: this.state.selectedRows[0].type,
        },
        deviceGroupIds: groupId,
      };
    } else if (!groupId[0]) {
      apiUrl =
        window.location.origin +
        config.serverConfig.invoker.uri +
        config.serverConfig.invoker.deviceMgt +
        '/groups/id/' +
        groupId +
        '/devices/add';
      deviceData = this.state.selectedRows.map(obj => ({
        id: obj.deviceIdentifier,
        type: obj.type,
      }));
    } else {
      apiUrl =
        window.location.origin +
        config.serverConfig.invoker.uri +
        config.serverConfig.invoker.deviceMgt +
        '/groups/id/' +
        groupId[0] +
        '/devices/add';
      deviceData = this.state.selectedRows.map(obj => ({
        id: obj.deviceIdentifier,
        type: obj.type,
      }));
    }

    // send request to the invoker
    axios
      .post(apiUrl, deviceData, {
        headers: { 'Content-Type': 'application/json' },
      })
      .then(res => {
        if (res.status === 200) {
          this.setState({
            loading: false,
          });
          notification.success({
            message: 'Done',
            duration: 4,
            description: 'Successfully added to the device group.',
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
            description: 'Error occurred while adding to the device group.',
          });
        }

        this.setState({ loading: false });
      });
  };

  getGroups = () => {
    this.setState({
      groupModalVisible: true,
    });
    // send request to the invoker
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/groups',
      )
      .then(res => {
        this.setState({ deviceGroups: res.data.data.deviceGroups });
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
            description: 'Error occurred while retrieving device groups.',
          });
        }

        this.setState({ loading: false });
      });
  };

  handleOk = e => {
    if (this.state.selectedGroupId) {
      this.addDevicesToGroup(this.state.selectedGroupId);
      this.setState({
        groupModalVisible: false,
      });
    } else {
      notification.error({
        message: 'There was a problem',
        duration: 0,
        description: 'Please select a group.',
      });
    }
  };

  handleCancel = e => {
    this.setState({
      groupModalVisible: false,
    });
  };

  onGroupSelectChange = value => {
    this.setState({ selectedGroupId: value });
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

  onSelectChange = (selectedRowKeys, selectedRows) => {
    this.setState({
      selectedRowKeys,
      selectedRows: selectedRows,
    });
  };

  render() {
    const {
      data,
      pagination,
      loading,
      selectedRows,
      selectedRowKeys,
    } = this.state;
    const isSelectedSingle = this.state.selectedRows.length == 1;

    let selectedText;
    if (isSelectedSingle) {
      selectedText = 'You have selected 1 device';
    } else {
      selectedText =
        'You have selected ' + this.state.selectedRows.length + ' devices';
    }

    const rowSelection = {
      selectedRowKeys,
      selectedRows,
      onChange: this.onSelectChange,
    };

    let item = this.state.deviceGroups.map(data => (
      <Select.Option value={data.id} key={data.id}>
        {data.name}
      </Select.Option>
    ));
    return (
      <div>
        <BulkActionBar
          deleteDevice={this.deleteDevice}
          getGroups={this.getGroups}
          disenrollDevice={this.disenrollDevice}
          selectedRows={this.state.selectedRows}
        />
        <div>
          <Table
            columns={columns}
            rowKey={record =>
              record.deviceIdentifier +
              record.enrolmentInfo.owner +
              record.enrolmentInfo.ownership
            }
            dataSource={data}
            pagination={{
              ...pagination,
              size: 'small',
              // position: "top",
              showTotal: (total, range) =>
                `showing ${range[0]}-${range[1]} of ${total} devices`,
              // showQuickJumper: true
            }}
            loading={loading}
            onChange={this.handleTableChange}
            rowSelection={rowSelection}
          />
        </div>

        <Modal
          title="Grouping Devices"
          width="350px"
          visible={this.state.groupModalVisible}
          onOk={this.handleOk}
          onCancel={this.handleCancel}
        >
          <p>{selectedText}</p>
          <Select
            mode={isSelectedSingle ? 'multiple' : 'default'}
            showSearch
            style={{ display: 'block' }}
            placeholder="Select Group"
            optionFilterProp="children"
            onChange={this.onGroupSelectChange}
          >
            {item}
          </Select>
        </Modal>
      </div>
    );
  }
}

export default withConfigContext(DeviceTable);
