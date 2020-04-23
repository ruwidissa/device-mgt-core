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
import { Modal, Button, Icon, notification } from 'antd';
import axios from 'axios';
import { handleApiError } from '../../../../../../../../../../services/utils/errorHandler';
import { withConfigContext } from '../../../../../../../../../../components/ConfigContext';
import { withRouter } from 'react-router-dom';
const { confirm } = Modal;

class DeleteRelease extends React.Component {
  showModal = () => {
    confirm({
      title: 'Are you sure you want to delete the application release?',
      content:
        'If you have multiple application releases, only the selected app release will be ' +
        'deleted. Otherwise, the whole application will be deleted. Further note, this will ' +
        'delete application artifacts permanently.\n' +
        'Be careful, this process cannot be undone.',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk: this.deleteRelease,
    });
  };

  deleteRelease = () => {
    const config = this.props.context;
    const apiUrl =
      window.location.origin +
      config.serverConfig.invoker.uri +
      config.serverConfig.invoker.publisher +
      '/admin/applications/release/' +
      this.props.uuid;
    axios
      .delete(apiUrl)
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Successfully deleted the release',
          });
          this.props.history.push('/publisher');
        }
      })
      .catch(error => {
        handleApiError(
          'Something Went wrong when trying to delete the release, Please contact  the administrator',
        );
        this.setState({
          loading: false,
        });
      });
  };

  render() {
    return (
      <>
        <Button
          disabled={!this.props.isDeletableState}
          size="small"
          type="danger"
          onClick={this.showModal}
        >
          <Icon type="delete" /> Delete
        </Button>
      </>
    );
  }
}

export default withConfigContext(withRouter(DeleteRelease));
