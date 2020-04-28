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
import {
  EyeInvisibleOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons';
import { Button, Modal, notification, Tooltip } from 'antd';
import axios from 'axios';
import { handleApiError } from '../../../../../../../../../../../services/utils/errorHandler';
import { withConfigContext } from '../../../../../../../../../../../components/ConfigContext';
import { withRouter } from 'react-router-dom';
import '../../styles.css';

const { confirm } = Modal;

class RetireApp extends React.Component {
  showModal = () => {
    confirm({
      title: 'Are you sure you want to retire this app?',
      icon: <ExclamationCircleOutlined style={{ color: 'red' }} />,
      content:
        'You are trying to retire the entire application, by performing this operation, ' +
        'you will not see the app data or app release data on either publisher or store. ' +
        'Further, please note, this process cannot be undone.',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk: this.hideApp,
    });
  };

  hideApp = () => {
    const config = this.props.context;
    const apiUrl =
      window.location.origin +
      config.serverConfig.invoker.uri +
      config.serverConfig.invoker.publisher +
      '/admin/applications/retire/' +
      this.props.id;
    axios
      .put(apiUrl)
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Successfully hided the app',
          });
          this.props.history.push('/publisher');
        }
      })
      .catch(error => {
        console.log(error);
        handleApiError(
          error,
          'Something Went wrong when trying to reitre the app, Please contact  the administrator',
        );
        this.setState({
          loading: false,
        });
      });
  };

  render() {
    return (
      <div>
        {this.props.isHideableApp && (
          <Button
            type="link"
            onClick={this.showModal}
            className="btn-view-more"
          >
            <EyeInvisibleOutlined /> Retire
          </Button>
        )}
        {!this.props.isHideableApp && (
          <Tooltip
            placement="leftTop"
            title="All releases should be in retired state"
          >
            <Button type="link" disabled={true} className="btn-view-more">
              <EyeInvisibleOutlined /> Retire
            </Button>
          </Tooltip>
        )}
      </div>
    );
  }
}

export default withConfigContext(withRouter(RetireApp));
