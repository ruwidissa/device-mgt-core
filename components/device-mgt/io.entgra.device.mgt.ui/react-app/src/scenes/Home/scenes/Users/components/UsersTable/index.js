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
import { Button, List, message, Modal, notification, Table } from 'antd';
import TimeAgo from 'javascript-time-ago';
// Load locale-specific relative date/time formatting rules.
import en from 'javascript-time-ago/locale/en';
import { withConfigContext } from '../../../../../../components/ConfigContext';
import UsersDevices from './components/UserDevices';
import AddUser from './components/AddUser';
import UserActions from './components/UserActions';
import Filter from '../../../../components/Filter';
import ExternalDevicesModal from './components/ExternalDevicesModal';
const ButtonGroup = Button.Group;

let apiUrl;

const searchFields = [
  {
    name: 'username',
    placeholder: 'Username',
  },
  {
    name: 'firstName',
    placeholder: 'First Name',
  },
  {
    name: 'lastName',
    placeholder: 'Last Name',
  },
  {
    name: 'emailAddress',
    placeholder: 'Email Address',
  },
];

class UsersTable extends React.Component {
  constructor(props) {
    super(props);
    TimeAgo.addLocale(en);
    this.state = {
      data: [],
      pagination: {},
      loading: false,
      selectedRows: [],
      rolesModalVisible: false,
      devicesModalVisible: false,
      rolesData: [],
      user: '',
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
    this.fetchUsers();
  }

  // fetch data from api
  fetchUsers = (params = {}, filters = {}) => {
    const config = this.props.context;
    this.setState({ loading: true });

    // get current page
    const currentPage = params.hasOwnProperty('page') ? params.page : 1;

    const extraParams = {
      offset: 10 * (currentPage - 1), // calculate the offset
      limit: 10,
      ...filters,
    };

    const encodedExtraParams = Object.keys(extraParams)
      .map(key => key + '=' + extraParams[key])
      .join('&');

    apiUrl =
      window.location.origin +
      config.serverConfig.invoker.uri +
      config.serverConfig.invoker.deviceMgt +
      '/users/search?' +
      encodedExtraParams;

    // send request to the invokerss
    axios
      .get(apiUrl)
      .then(res => {
        if (res.status === 200) {
          const pagination = { ...this.state.pagination };
          this.setState({
            loading: false,
            data: res.data.data.users,
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
            description: 'Error occurred while trying to load users.',
          });
        }

        this.setState({ loading: false });
      });
  };

  fetchRoles = username => {
    const config = this.props.context;

    this.setState({
      rolesModalVisible: true,
      user: username,
    });

    apiUrl =
      window.location.origin +
      config.serverConfig.invoker.uri +
      config.serverConfig.invoker.deviceMgt +
      '/users/' +
      username +
      '/roles';

    axios
      .get(apiUrl)
      .then(res => {
        if (res.status === 200) {
          this.setState({
            rolesData: res.data.data.roles,
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
            description: 'Error occurred while trying to load roles.',
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

  handleOk = e => {
    this.setState({
      rolesModalVisible: false,
      devicesModalVisible: false,
    });
  };

  handleCancel = e => {
    this.setState({
      rolesModalVisible: false,
      devicesModalVisible: false,
    });
  };

  openDeviceListModal = e => {
    this.setState({
      devicesModalVisible: true,
    });
  };

  render() {
    const { data, pagination, loading } = this.state;
    const columns = [
      {
        title: 'User Name',
        dataIndex: 'username',
        key: 'username',
      },
      {
        title: 'First Name',
        dataIndex: 'firstname',
        key: 'firstname',
        width: 200,
      },
      {
        title: 'Last Name',
        dataIndex: 'lastname',
        key: 'lastname',
      },
      {
        title: 'Email',
        dataIndex: 'emailAddress',
        key: 'emailAddress',
      },
      {
        title: 'View',
        dataIndex: 'username',
        key: 'roles',
        render: username => (
          <ButtonGroup>
            <Button
              type="primary"
              size={'small'}
              icon="book"
              onClick={() => this.fetchRoles(username)}
            >
              Roles
            </Button>
            <Button
              type="primary"
              size={'small'}
              icon="desktop"
              onClick={this.openDeviceListModal}
            >
              Devices
            </Button>
          </ButtonGroup>
        ),
      },
      {
        title: 'External Device Claims',
        dataIndex: 'claims',
        key: 'claims',
        render: (id, row) => <ExternalDevicesModal user={row.username} />,
      },
      {
        title: 'Action',
        dataIndex: 'id',
        key: 'action',
        render: (id, row) => (
          <span>
            <UserActions data={row} fetchUsers={this.fetchUsers} />
          </span>
        ),
      },
    ];
    return (
      <div>
        <div style={{ background: '#f0f2f5' }}>
          <AddUser fetchUsers={this.fetchUsers} />
        </div>
        <div style={{ textAlign: 'right' }}>
          <Filter fields={searchFields} callback={this.fetchUsers} />
        </div>
        <div style={{ backgroundColor: '#ffffff', borderRadius: 5 }}>
          <Table
            columns={columns}
            rowKey={record => record.username}
            dataSource={data}
            pagination={{
              ...pagination,
              size: 'small',
              // position: "top",
              showTotal: (total, range) =>
                `showing ${range[0]}-${range[1]} of ${total} groups`,
              // showQuickJumper: true
            }}
            loading={loading}
            onChange={this.handleTableChange}
            rowSelection={this.rowSelection}
          />
        </div>
        <div>
          <Modal
            title="ROLES"
            width="900px"
            visible={this.state.rolesModalVisible}
            onOk={this.handleOk}
            onCancel={this.handleCancel}
          >
            <div>
              <List
                size="small"
                bordered
                dataSource={this.state.rolesData}
                renderItem={item => <List.Item>{item}</List.Item>}
              />
            </div>
          </Modal>
        </div>

        <div>
          <Modal
            title="DEVICES"
            width="900px"
            visible={this.state.devicesModalVisible}
            onOk={this.handleOk}
            onCancel={this.handleCancel}
          >
            <div>
              <UsersDevices user={this.state.user} />
            </div>
          </Modal>
        </div>
      </div>
    );
  }
}

export default withConfigContext(UsersTable);
