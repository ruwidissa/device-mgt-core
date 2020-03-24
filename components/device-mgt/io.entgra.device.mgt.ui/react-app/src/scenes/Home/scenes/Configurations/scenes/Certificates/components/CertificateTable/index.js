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
  notification,
  Popconfirm,
  Table,
  Tooltip,
  Typography,
} from 'antd';
import TimeAgo from 'javascript-time-ago';
// Load locale-specific relative date/time formatting rules.
import en from 'javascript-time-ago/locale/en';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import Moment from 'react-moment';

const { Paragraph, Text } = Typography;

let config = null;

class CertificateTable extends React.Component {
  constructor(props) {
    super(props);
    config = this.props.context;
    TimeAgo.addLocale(en);
    this.state = {
      data: [],
      pagination: {},
      loading: false,
    };
  }

  componentDidMount() {
    this.fetch();
  }

  // fetch data from api
  fetch = (params = {}) => {
    this.setState({ loading: true });
    // get current page
    const currentPage = params.hasOwnProperty('page') ? params.page : 1;

    const extraParams = {
      offset: 10 * (currentPage - 1), // calculate the offset
      limit: 10,
    };

    const encodedExtraParams = Object.keys(extraParams)
      .map(key => key + '=' + extraParams[key])
      .join('&');

    // send request to the invoker
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          '/certificate-mgt/v1.0/admin/certificates' +
          encodedExtraParams,
      )
      .then(res => {
        if (res.status === 200) {
          const pagination = { ...this.state.pagination };
          this.setState({
            loading: false,
            data: res.data.data.certificates,
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

  deleteCertificate = serialNumber => {
    axios
      .delete(
        window.location.origin +
          config.serverConfig.invoker.uri +
          '/certificate-mgt/v1.0/admin/certificates/' +
          serialNumber,
        { headers: { 'Content-Type': 'application/json' } },
      )
      .then(res => {
        if (res.status === 200) {
          this.fetch();
          notification.success({
            message: 'Done',
            duration: 4,
            description: 'Successfully deleted the certificate.',
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
            description:
              'Error occurred while trying to delete the certificate.',
          });
        }
      });
  };

  columns = [
    {
      title: 'Serial Number',
      dataIndex: 'serialNumber',
    },
    {
      title: 'Username',
      dataIndex: 'username',
    },
    {
      title: 'Certificate Version',
      dataIndex: 'certificateVersion',
    },
    {
      title: 'Certificate Serial',
      dataIndex: 'certificateserial',
    },
    {
      title: 'Not Before',
      dataIndex: 'notBefore',
      render: notBefore => (
        <Moment format="YYYY/MM/DD HH:mm" date={notBefore} />
      ),
    },
    {
      title: 'Not After',
      dataIndex: 'notAfter',
      render: notAfter => <Moment format="YYYY/MM/DD HH:mm" date={notAfter} />,
    },
    {
      title: 'Subject',
      dataIndex: 'subject',
      render: subject => (
        <Paragraph
          style={{ marginBottom: 0 }}
          ellipsis={{ rows: 1, expandable: true }}
        >
          {subject}
        </Paragraph>
      ),
    },
    {
      title: 'Issuer',
      dataIndex: 'issuer',
      render: issuer => (
        <Paragraph
          style={{ marginBottom: 0 }}
          ellipsis={{ rows: 1, expandable: true }}
        >
          {issuer}
        </Paragraph>
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      dataIndex: 'serialNumber',
      render: serialNumber => (
        <Tooltip placement="bottom" title={'Remove User'}>
          <Popconfirm
            placement="top"
            title={'Are you sure?'}
            onConfirm={() => {
              this.deleteCertificate(serialNumber);
            }}
            okText="Ok"
            cancelText="Cancel"
          >
            <a>
              <Text type="danger">
                <Icon type="delete" />
              </Text>
            </a>
          </Popconfirm>
        </Tooltip>
      ),
    },
  ];

  render() {
    const { data, pagination, loading } = this.state;

    return (
      <div>
        <div>
          <Table
            columns={this.columns}
            rowKey={record => record.serialNumber}
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
          />
        </div>
      </div>
    );
  }
}

export default withConfigContext(CertificateTable);
