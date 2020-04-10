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
import { Button, Divider, Input, Modal, notification, Spin } from 'antd';
import axios from 'axios';
import { withConfigContext } from '../../../../../../../../../../components/ConfigContext';
import { withRouter } from 'react-router';
import { handleApiError } from '../../../../../../../../../../services/utils/errorHandler';

class AddNewPage extends React.Component {
  state = {
    visible: false,
    pageName: '',
  };

  showModal = () => {
    this.setState({
      visible: true,
      loading: false,
    });
  };

  handleCancel = e => {
    this.setState({
      visible: false,
    });
  };

  handlePageName = e => {
    this.setState({
      pageName: e.target.value,
    });
  };

  createNewPage = () => {
    const config = this.props.context;
    this.setState({ loading: true });

    axios
      .post(
        window.location.origin +
          config.serverConfig.invoker.uri +
          '/device-mgt/android/v1.0/enterprise/store-layout/page',
        {
          locale: 'en',
          pageName: this.state.pageName,
        },
      )
      .then(res => {
        if (res.status === 200) {
          const { pageId, pageName } = res.data.data;

          notification.success({
            message: 'Saved!',
            description: 'Page created successfully!',
          });

          this.setState({ loading: false });

          this.props.history.push(
            `/publisher/manage/android-enterprise/pages/${pageName}/${pageId}`,
          );
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to update the cluster.',
        );
        this.setState({ loading: false });
      });
  };

  render() {
    return (
      <div style={{ marginTop: 24, marginBottom: 24 }}>
        <Button type="dashed" onClick={this.showModal}>
          Add new page
        </Button>
        <Modal
          title="Add new page"
          visible={this.state.visible}
          onOk={this.createNewPage}
          onCancel={this.handleCancel}
          okText="Create Page"
          footer={null}
        >
          <Spin spinning={this.state.loading}>
            <p>Choose a name for the page</p>
            <Input onChange={this.handlePageName} />
            <Divider />
            <div>
              <Button onClick={this.handleCancel}>Cancel</Button>
              <Divider type="vertical" />
              <Button
                onClick={this.createNewPage}
                htmlType="button"
                type="primary"
                disabled={this.state.pageName.length === 0}
              >
                Create Page
              </Button>
            </div>
          </Spin>
        </Modal>
      </div>
    );
  }
}

export default withConfigContext(withRouter(AddNewPage));
