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
import { Button, Modal, notification, Popover, Spin, Tooltip } from 'antd';
import axios from 'axios';
import { withConfigContext } from '../../../../../../../../../../../components/ConfigContext';
import { handleApiError } from '../../../../../../../../../../../services/utils/errorHandler';
import { SettingOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import '../../styles.css';

class ManagedConfigurationsIframe extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      visible: false,
      loading: false,
      isHintVisible: false,
    };
  }

  showModal = () => {
    this.getMcm();
    this.setState({
      visible: true,
    });
  };

  handleOk = e => {
    this.setState({
      visible: false,
    });
  };

  handleCancel = e => {
    this.setState({
      visible: false,
    });
  };

  getMcm = () => {
    const { packageName } = this.props;
    this.setState({ loading: true });

    // send request to the invoker
    axios
      .get(
        window.location.origin +
          this.config.serverConfig.invoker.uri +
          '/device-mgt/android/v1.0/enterprise/managed-configs/package/' +
          packageName,
      )
      .then(res => {
        if (res.status === 200) {
          let mcmId = null;
          if (res.data.hasOwnProperty('data')) {
            mcmId = res.data.data.mcmId;
          }
          this.loadIframe(mcmId);
          this.setState({ loading: false });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to load configurations.',
        );
        this.setState({ loading: false, visible: false });
      });
  };

  loadIframe = mcmId => {
    const { packageName } = this.props;
    let method = 'post';
    // eslint-disable-next-line no-undef
    gapi.load('gapi.iframes', () => {
      const parameters = {
        token: this.config.androidEnterpriseToken,
        packageName: packageName,
      };
      if (mcmId != null) {
        parameters.mcmId = mcmId;
        parameters.canDelete = true;
        method = 'put';
      }

      const queryString = Object.keys(parameters)
        .map(key => key + '=' + parameters[key])
        .join('&');

      var options = {
        url: 'https://play.google.com/managed/mcm?' + queryString,
        where: document.getElementById('manage-config-iframe-container'),
        attributes: { style: 'height:720px', scrolling: 'yes' },
      };

      // eslint-disable-next-line no-undef
      var iframe = gapi.iframes.getContext().openChild(options);
      iframe.register(
        'onconfigupdated',
        event => {
          this.updateConfig(method, event);
        },
        // eslint-disable-next-line no-undef
        gapi.iframes.CROSS_ORIGIN_IFRAMES_FILTER,
      );

      iframe.register(
        'onconfigdeleted',
        event => {
          this.deleteConfig(event);
        },
        // eslint-disable-next-line no-undef
        gapi.iframes.CROSS_ORIGIN_IFRAMES_FILTER,
      );
    });
  };

  updateConfig = (method, event) => {
    const { packageName } = this.props;
    this.setState({ loading: true });

    const data = {
      mcmId: event.mcmId,
      profileName: event.name,
      packageName,
    };

    // send request to the invoker
    axios({
      method,
      url:
        window.location.origin +
        this.config.serverConfig.invoker.uri +
        '/device-mgt/android/v1.0/enterprise/managed-configs',
      data,
    })
      .then(res => {
        if (res.status === 200 || res.status === 201) {
          notification.success({
            message: 'Saved!',
            description: 'Configuration Profile updated Successfully',
          });
          this.setState({
            loading: false,
            visible: false,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to update configurations.',
        );
        this.setState({ loading: false });
      });
  };

  deleteConfig = event => {
    this.setState({ loading: true });

    // send request to the invoker
    axios
      .delete(
        window.location.origin +
          this.config.serverConfig.invoker.uri +
          '/device-mgt/android/v1.0/enterprise/managed-configs/mcm/' +
          event.mcmId,
      )
      .then(res => {
        if (res.status === 200 || res.status === 201) {
          notification.success({
            message: 'Saved!',
            description: 'Configuration Profile removed Successfully',
          });
          this.setState({
            loading: false,
            visible: false,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to remove configurations.',
        );
        this.setState({ loading: false });
      });
  };

  handleHintVisibleChange = visible => {
    this.setState({ isHintVisible: visible });
  };

  render() {
    return (
      <div>
        {this.props.isEnabled && (
          <Button
            type="link"
            className="btn-view-more"
            onClick={this.showModal}
          >
            <SettingOutlined /> Managed Configurations
          </Button>
        )}
        {!this.props.isEnabled && (
          <Tooltip
            placement="leftTop"
            title="Managed configurations are available only with android enterprise apps."
          >
            <Button type="link" className="btn-view-more" disabled={true}>
              <SettingOutlined /> Managed Configurations
            </Button>
          </Tooltip>
        )}
        <Modal
          visible={this.state.visible}
          onOk={this.handleOk}
          onCancel={this.handleCancel}
          footer={null}
        >
          <div>
            <Popover
              title={null}
              trigger="click"
              visible={this.state.isHintVisible}
              content={
                <p>
                  If you are developing apps for the enterprise market, you may
                  need to satisfy particular requirements set by a
                  organization&quot;s policies. Managed configurations,
                  previously previously known as application restrictions, allow
                  the organization&quot;s IT admin to remotely specify settings
                  for apps. This capability is particularly useful for
                  organization-approved apps deployed to a work profile.
                </p>
              }
              onVisibleChange={this.handleHintVisibleChange}
              overlayStyle={{ width: 300 }}
            >
              <Button
                size="large"
                type="link"
                style={{ marginTop: -56, fontSize: '1.2em' }}
              >
                <QuestionCircleOutlined />
              </Button>
            </Popover>
            <Spin spinning={this.state.loading}>
              <div id="manage-config-iframe-container"></div>
            </Spin>
          </div>
        </Modal>
      </div>
    );
  }
}

export default withConfigContext(ManagedConfigurationsIframe);
