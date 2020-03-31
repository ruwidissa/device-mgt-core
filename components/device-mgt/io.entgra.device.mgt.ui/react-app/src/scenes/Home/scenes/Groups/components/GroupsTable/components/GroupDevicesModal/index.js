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

import { Button, Modal } from 'antd';
import TimeAgo from 'javascript-time-ago';
// Load locale-specific relative date/time formatting rules.
import en from 'javascript-time-ago/locale/en';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import DevicesTable from '../../../../../../components/DevicesTable';

let apiUrl;

class GroupDevicesModal extends React.Component {
  constructor(props) {
    super(props);
    TimeAgo.addLocale(en);
    this.state = {
      data: [],
      pagination: {},
      loading: false,
      selectedRows: [],
      visible: false,
      apiUrl: null,
    };
  }

  openDrawer = () => {
    this.setState({ visible: true });
    const groupData = {
      groupId: this.props.groupData.id,
      groupName: this.props.groupData.name,
    };
    this.fetchGroupDevices(groupData);
  };

  handleModalCancel = () => {
    this.setState({
      visible: false,
    });
  };

  // fetch data from api
  fetchGroupDevices = (params = {}, filters = {}) => {
    const config = this.props.context;

    // get current page
    const currentPage = params.hasOwnProperty('page') ? params.page : 1;

    const extraParams = {
      offset: 10 * (currentPage - 1), // calculate the offset
      limit: 10,
      ...params,
    };

    const encodedExtraParams = Object.keys(extraParams)
      .map(key => key + '=' + extraParams[key])
      .join('&');

    apiUrl =
      window.location.origin +
      config.serverConfig.invoker.uri +
      config.serverConfig.invoker.deviceMgt +
      '/devices?' +
      encodedExtraParams;

    this.setState({ apiUrl: apiUrl });
  };

  handleTableChange = (pagination, filters, sorter) => {
    const pager = { ...this.state.pagination };
    pager.current = pagination.current;
    this.setState({
      pagination: pager,
    });
    this.fetchGroupDevices({
      results: pagination.pageSize,
      page: pagination.current,
      sortField: sorter.field,
      sortOrder: sorter.order,
      ...filters,
    });
  };

  render() {
    const { apiUrl, visible } = this.state;
    return (
      <div>
        <Button
          type="primary"
          size={'small'}
          icon="desktop"
          onClick={this.openDrawer}
        >
          Devices
        </Button>
        <Modal
          title="DEVICES"
          width="80%"
          visible={visible}
          onCancel={this.handleModalCancel}
          footer={[
            <Button key="cancel" onClick={this.handleModalCancel}>
              Cancel
            </Button>,
            <Button key="ok" type="primary" onClick={this.handleModalCancel}>
              OK
            </Button>,
          ]}
        >
          <div style={{ alignItems: 'center' }}>
            <DevicesTable apiUrl={apiUrl} />
          </div>
        </Modal>
      </div>
    );
  }
}

export default withConfigContext(GroupDevicesModal);
