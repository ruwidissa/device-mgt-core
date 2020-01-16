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
import { Button, message, Modal, notification, Table, List } from 'antd';
import TimeAgo from 'javascript-time-ago';
// Load locale-specific relative date/time formatting rules.
import en from 'javascript-time-ago/locale/en';
import { withConfigContext } from '../../context/ConfigContext';
import AddRole from './AddRole';
import RoleAction from './RoleAction';
import Filter from '../Utils/Filter/Filter';

const searchFields = [
  {
    name: 'filter',
    placeholder: 'Name',
  },
];

class RolesTable extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    TimeAgo.addLocale(en);
    this.state = {
      data: [],
      pagination: {},
      loading: false,
      selectedRows: [],
      userData: [],
      users: [],
      isEditRoleModalVisible: false,
      isUserListModalVisible: false,
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

  openUserListModal = event => {
    let apiUrl =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      '/roles/' +
      event;

    // send request to the invokerss
    axios
      .get(apiUrl)
      .then(res => {
        if (res.status === 200) {
          this.setState({
            userData: res.data.data.users,
            isUserListModalVisible: true,
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
      });
  };

  handleOk = e => {
    this.setState({
      isUserListModalVisible: false,
    });
  };

  handleCancel = e => {
    this.setState({
      isUserListModalVisible: false,
    });
  };

  // fetch data from api
  fetchUsers = (params = {}, filters = {}) => {
    // const config = this.props.context;
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

    let apiUrl =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      '/roles?' +
      encodedExtraParams;

    // send request to the invokerss
    axios
      .get(apiUrl)
      .then(res => {
        if (res.status === 200) {
          const pagination = { ...this.state.pagination };
          this.setState({
            loading: false,
            data: res.data.data.roles,
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
    const columns = [
      {
        title: 'Role Name',
        dataIndex: '',
        key: 'role',
        width: '60%',
      },
      {
        title: 'View',
        dataIndex: '',
        key: 'users',
        render: (id, row) => (
          <Button
            type="primary"
            size={'small'}
            icon="book"
            onClick={() => this.openUserListModal(row)}
          >
            Users
          </Button>
        ),
      },
      {
        title: 'Action',
        dataIndex: 'id',
        key: 'action',
        render: (id, row) => (
          <span>
            <RoleAction data={row} fetchUsers={this.fetchUsers} />
          </span>
        ),
      },
    ];
    return (
      <div>
        <div style={{ background: '#f0f2f5' }}>
          <AddRole fetchUsers={this.fetchUsers} />
        </div>
        <div style={{ textAlign: 'right' }}>
          <Filter fields={searchFields} callback={this.fetchUsers} />
        </div>
        <div style={{ backgroundColor: '#ffffff', borderRadius: 5 }}>
          <Table
            columns={columns}
            rowKey={record => record}
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
            title="USERS"
            width="900px"
            visible={this.state.isUserListModalVisible}
            onOk={this.handleOk}
            onCancel={this.handleCancel}
          >
            <div>
              <List
                size="small"
                bordered
                dataSource={this.state.userData}
                renderItem={item => <List.Item>{item}</List.Item>}
              />
            </div>
          </Modal>
        </div>
      </div>
    );
  }
}

export default withConfigContext(RolesTable);
