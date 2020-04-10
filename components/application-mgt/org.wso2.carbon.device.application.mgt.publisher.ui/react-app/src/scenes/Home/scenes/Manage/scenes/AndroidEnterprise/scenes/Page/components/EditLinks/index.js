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
import { Button, Modal, notification, Select, Spin } from 'antd';
import axios from 'axios';
import { withConfigContext } from '../../../../../../../../../../components/ConfigContext';
import { handleApiError } from '../../../../../../../../../../services/utils/errorHandler';

const { Option } = Select;

class EditLinks extends React.Component {
  constructor(props) {
    super(props);
    this.selectedLinks = [];
    this.state = {
      visible: false,
    };
  }

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

  updateLinks = () => {
    const config = this.props.context;
    this.setState({ loading: true });

    axios
      .put(
        window.location.origin +
          config.serverConfig.invoker.uri +
          '/device-mgt/android/v1.0/enterprise/store-layout/page-link',
        {
          pageId: this.props.pageId,
          links: this.selectedLinks,
        },
      )
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Saved!',
            description: 'Links updated successfully!',
          });

          this.props.updateLinks(this.selectedLinks);

          this.setState({
            loading: false,
            visible: false,
          });
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

  handleChange = selectedLinks => {
    this.selectedLinks = selectedLinks;
  };

  render() {
    return (
      <div>
        <Button onClick={this.showModal} type="link">
          [add / remove links]
        </Button>
        <Modal
          title="Add / Remove Links"
          visible={this.state.visible}
          onOk={this.updateLinks}
          onCancel={this.handleCancel}
          okText="Update"
        >
          <Spin spinning={this.state.loading}>
            <Select
              mode="multiple"
              style={{ width: '100%' }}
              placeholder="Please select links"
              defaultValue={this.props.selectedLinks}
              onChange={this.handleChange}
            >
              {this.props.pages.map(page => (
                <Option disabled={page.id === this.props.pageId} key={page.id}>
                  {page.name[0].text}
                </Option>
              ))}
            </Select>
          </Spin>
        </Modal>
      </div>
    );
  }
}

export default withConfigContext(EditLinks);
