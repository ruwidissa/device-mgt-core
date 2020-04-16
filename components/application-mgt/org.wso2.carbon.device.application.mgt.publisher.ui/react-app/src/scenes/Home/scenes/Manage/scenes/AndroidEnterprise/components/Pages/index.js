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
  Tag,
  notification,
  Table,
  Typography,
  Divider,
  Icon,
  Popconfirm,
  Button,
} from 'antd';

import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import './styles.css';
import { Link } from 'react-router-dom';
import AddNewPage from './components/AddNewPage';
import { handleApiError } from '../../../../../../../../services/utils/errorHandler';
import { isAuthorized } from '../../../../../../../../services/utils/authorizationHandler';

const { Text, Title } = Typography;

class Pages extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      data: [],
      pagination: {},
      loading: false,
      selectedRows: [],
      homePageId: null,
    };
    this.hasPermissionToManage = isAuthorized(
      this.props.config.user,
      '/device-mgt/enterprise/user/view',
    );
  }

  rowSelection = {
    onChange: (selectedRowKeys, selectedRows) => {
      this.setState({
        selectedRows: selectedRows,
      });
    },
  };

  componentDidMount() {
    this.setHomePage();
    this.fetch();
  }

  // fetch data from api
  fetch = (params = {}) => {
    const config = this.props.context;
    this.setState({ loading: true });

    // send request to the invoker
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          '/device-mgt/android/v1.0/enterprise/store-layout/page',
      )
      .then(res => {
        if (res.status === 200) {
          const pagination = { ...this.state.pagination };
          this.setState({
            loading: false,
            data: res.data.data.page,
            pagination,
          });
        }
      })
      .catch(error => {
        handleApiError(error, 'Error occurred while trying to load pages.');
        this.setState({ loading: false });
      });
  };

  setHomePage = () => {
    const config = this.props.context;
    // send request to the invoker
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          '/device-mgt/android/v1.0/enterprise/store-layout/home-page',
      )
      .then(res => {
        if (res.status === 200) {
          this.setState({
            homePageId: res.data.data.homepageId,
          });
        }
      })
      .catch(error => {
        handleApiError(error, 'Error occurred while trying to get home page.');
        this.setState({ loading: false });
      });
  };

  updateHomePage = pageId => {
    const config = this.props.context;
    this.setState({
      loading: true,
    });
    // send request to the invoker
    axios
      .put(
        window.location.origin +
          config.serverConfig.invoker.uri +
          '/device-mgt/android/v1.0/enterprise/store-layout/home-page/' +
          pageId,
        {},
      )
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Done!',
            description: 'Home page was updated successfully!',
          });

          this.setState({
            homePageId: res.data.data.homepageId,
            loading: false,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to update the home page.',
        );
        this.setState({ loading: false });
      });
  };

  deletePage = pageId => {
    const { data } = this.state;
    const config = this.props.context;
    this.setState({
      loading: true,
    });
    // send request to the invoker
    axios
      .delete(
        window.location.origin +
          config.serverConfig.invoker.uri +
          '/device-mgt/android/v1.0/enterprise/store-layout/page/' +
          pageId,
      )
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Done!',
            description: 'Home page was updated successfully!',
          });

          for (let i = 0; i < data.length; i++) {
            if (data[i].id === pageId) {
              data.splice(i, 1);
            }
          }

          this.setState({
            loading: false,
            data: data,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to delete the page.',
        );
        this.setState({ loading: false });
      });
  };

  handleTableChange = (pagination, filters, sorter) => {
    const pager = { ...this.state.pagination };
    pager.current = pagination.current;
    this.setState({
      pagination: pager,
    });
  };

  columns = [
    {
      title: 'Page',
      dataIndex: 'name',
      key: 'name',
      width: 300,
      render: (name, page) => {
        const pageName = name[0].text;
        return (
          <div>
            <Link
              to={`/publisher/manage/android-enterprise/pages/${pageName}/${page.id}`}
            >
              {' '}
              {pageName + ' '}
            </Link>
            {page.id === this.state.homePageId && (
              <Tag color="#badc58">Home Page</Tag>
            )}
          </div>
        );
      },
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (name, page) => (
        <div>
          {this.hasPermissionToManage && (
            <>
              <span className="action">
                <Button
                  disabled={page.id === this.state.homePageId}
                  className="btn-warning"
                  icon="home"
                  type="link"
                  onClick={() => {
                    this.updateHomePage(page.id);
                  }}
                >
                  set as homepage
                </Button>
              </span>
              <Divider type="vertical" />
              <Popconfirm
                title="Are you sure？"
                okText="Yes"
                cancelText="No"
                onConfirm={() => {
                  this.deletePage(page.id);
                }}
              >
                <span className="action">
                  <Text type="danger">
                    <Icon type="delete" /> delete
                  </Text>
                </span>
              </Popconfirm>
            </>
          )}
        </div>
      ),
    },
  ];

  render() {
    const { data, pagination, loading } = this.state;
    return (
      <div className="layout-pages">
        <Title level={4}>Pages</Title>
        <AddNewPage />
        <div style={{ backgroundColor: '#ffffff', borderRadius: 5 }}>
          <Table
            columns={this.columns}
            rowKey={record => record.id}
            dataSource={data}
            pagination={{
              ...pagination,
              size: 'small',
              // position: "top",
              showTotal: (total, range) =>
                `showing ${range[0]}-${range[1]} of ${total} pages`,
              showQuickJumper: true,
            }}
            loading={loading}
            onChange={this.handleTableChange}
            // rowSelection={this.rowSelection}
            scroll={{ x: 1000 }}
          />
        </div>
      </div>
    );
  }
}

export default withConfigContext(Pages);
