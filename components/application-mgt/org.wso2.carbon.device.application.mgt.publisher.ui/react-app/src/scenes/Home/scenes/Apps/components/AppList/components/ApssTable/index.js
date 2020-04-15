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
import { Avatar, Table, Tag, Icon, Badge, Alert, Tooltip } from 'antd';
import axios from 'axios';
import pSBC from 'shade-blend-color';
import './styles.css';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import AppDetailsDrawer from './AppDetailsDrawer';
import { handleApiError } from '../../../../../../../../services/utils/errorHandler';

let config = null;

const columns = [
  {
    title: '',
    dataIndex: 'name',
    // eslint-disable-next-line react/display-name
    render: (name, row) => {
      let avatar = null;
      if (row.applicationReleases.length === 0) {
        const avatarLetter = name.charAt(0).toUpperCase();
        avatar = (
          <Avatar
            shape="square"
            size="large"
            style={{
              marginRight: 20,
              borderRadius: '28%',
              border: '1px solid #ddd',
              backgroundColor: pSBC(0.5, config.theme.primaryColor),
            }}
          >
            {avatarLetter}
          </Avatar>
        );
      } else {
        const { applicationReleases } = row;
        let hasPublishedRelease = false;
        for (let i = 0; i < applicationReleases.length; i++) {
          if (applicationReleases[i].currentStatus === 'PUBLISHED') {
            hasPublishedRelease = true;
            break;
          }
        }
        avatar = hasPublishedRelease ? (
          <Badge
            title="Published"
            style={{
              backgroundColor: '#52c41a',
              borderRadius: '50%',
              color: 'white',
            }}
            count={
              <Tooltip title="Published">
                <Icon
                  style={{
                    backgroundColor: '#52c41a',
                    borderRadius: '50%',
                    color: 'white',
                  }}
                  type="check-circle"
                />
              </Tooltip>
            }
          >
            <Avatar
              shape="square"
              size="large"
              style={{
                borderRadius: '28%',
                border: '1px solid #ddd',
              }}
              src={row.applicationReleases[0].iconPath}
            />
          </Badge>
        ) : (
          <Avatar
            shape="square"
            size="large"
            style={{
              borderRadius: '28%',
              border: '1px solid #ddd',
            }}
            src={row.applicationReleases[0].iconPath}
          />
        );
      }

      return (
        <div>
          {avatar}
          <span style={{ marginLeft: 20 }}>{name}</span>
        </div>
      );
    },
  },
  {
    title: 'Categories',
    dataIndex: 'categories',
    // eslint-disable-next-line react/display-name
    render: categories => (
      <span>
        {categories.map(category => {
          return (
            <Tag
              style={{ marginBottom: 8 }}
              color={pSBC(0.3, config.theme.primaryColor)}
              key={category}
            >
              {category}
            </Tag>
          );
        })}
      </span>
    ),
  },
  {
    title: 'Platform',
    dataIndex: 'deviceType',
    // eslint-disable-next-line react/display-name
    render: platform => {
      const defaultPlatformIcons = config.defaultPlatformIcons;
      let icon = defaultPlatformIcons.default.icon;
      let color = defaultPlatformIcons.default.color;
      let theme = defaultPlatformIcons.default.theme;
      if (defaultPlatformIcons.hasOwnProperty(platform)) {
        icon = defaultPlatformIcons[platform].icon;
        color = defaultPlatformIcons[platform].color;
        theme = defaultPlatformIcons[platform].theme;
      }
      return (
        <span style={{ fontSize: 20, color: color, textAlign: 'center' }}>
          <Icon type={icon} theme={theme} />
        </span>
      );
    },
  },
  {
    title: 'Type',
    dataIndex: 'type',
  },
  {
    title: 'Subscription',
    dataIndex: 'subMethod',
  },
];

class AppsTable extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      pagination: {},
      apps: [],
      filters: {},
      isDrawerVisible: false,
      selectedApp: null,
      selectedAppIndex: -1,
      loading: false,
      isForbiddenErrorVisible: false,
    };
    config = this.props.context;
  }

  componentDidMount() {
    const { filters } = this.props;
    this.setState({
      filters,
    });
    this.fetch(filters);
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    const { filters } = this.props;
    if (prevProps.filters !== this.props.filters) {
      this.setState({
        filters,
      });
      this.fetch(filters);
    }
  }

  // handler to show app drawer
  showDrawer = (app, appIndex) => {
    this.setState({
      isDrawerVisible: true,
      selectedApp: app,
      selectedAppIndex: appIndex,
    });
  };

  // handler to close the app drawer
  closeDrawer = () => {
    this.setState({
      isDrawerVisible: false,
    });
  };

  handleTableChange = (pagination, filters, sorter) => {
    const pager = { ...this.state.pagination };
    pager.current = pagination.current;

    this.setState({
      pagination: pager,
    });
    this.fetch(this.state.filters, {
      results: pagination.pageSize,
      page: pagination.current,
      sortField: sorter.field,
      sortOrder: sorter.order,
      ...filters,
    });
  };

  fetch = (filters, params = {}) => {
    this.setState({ loading: true });
    const config = this.props.context;

    if (!params.hasOwnProperty('page')) {
      params.page = 1;
    }

    const data = {
      offset: 10 * (params.page - 1),
      limit: 10,
      ...filters,
    };

    axios
      .post(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.publisher +
          '/applications',
        data,
      )
      .then(res => {
        if (res.status === 200) {
          const data = res.data.data;
          let apps = [];

          if (res.data.data.hasOwnProperty('applications')) {
            apps = data.applications;
          }
          const pagination = { ...this.state.pagination };
          // Read total count from server
          // pagination.total = data.totalCount;
          pagination.total = data.pagination.count;
          this.setState({
            loading: false,
            apps: apps,
            pagination,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to load apps.',
          true,
        );
        if (error.hasOwnProperty('response') && error.response.status === 403) {
          this.setState({
            isForbiddenErrorVisible: true,
          });
        }
        this.setState({ loading: false });
      });
  };

  onUpdateApp = (key, value) => {
    const apps = [...this.state.apps];
    apps[this.state.selectedAppIndex][key] = value;
    this.setState({
      apps,
    });
  };

  render() {
    const { isDrawerVisible, loading } = this.state;
    return (
      <div>
        {this.state.isForbiddenErrorVisible && (
          <Alert
            message="You don't have permission to view apps."
            type="warning"
            banner
            closable
          />
        )}
        <div className="apps-table">
          <Table
            rowKey={record => record.id}
            dataSource={this.state.apps}
            columns={columns}
            pagination={this.state.pagination}
            onChange={this.handleTableChange}
            rowClassName="app-row"
            loading={loading}
            onRow={(record, rowIndex) => {
              return {
                onClick: event => {
                  this.showDrawer(record, rowIndex);
                },
              };
            }}
          />
          <AppDetailsDrawer
            visible={isDrawerVisible}
            onClose={this.closeDrawer}
            app={this.state.selectedApp}
            onUpdateApp={this.onUpdateApp}
          />
        </div>
      </div>
    );
  }
}

export default withConfigContext(AppsTable);
