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
import { Button, message, Modal, notification, List, Typography } from 'antd';
import axios from 'axios';
import { withConfigContext } from '../../../context/ConfigContext';

class FeatureListModal extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      modalVisible: false,
      name: '',
      description: '',
      features: [],
    };
  }

  fetchViolatedFeatures = id => {
    const config = this.props.context;

    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/devices/' +
          id +
          '/features',
      )
      .then(res => {
        if (res.status === 200) {
          this.setState({
            features: JSON.parse(res.data.data),
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
              'Error occurred while trying to load non compliance feature list.',
          });
        }
      });
  };

  openModal = () => {
    this.fetchViolatedFeatures(this.props.id);
    this.setState({
      modalVisible: true,
    });
  };

  handleOk = e => {
    this.setState({
      modalVisible: false,
    });
  };

  render() {
    const { features, modalVisible } = this.state;

    let featureList = features.map(data => (
      <List.Item key={data.featureCodes}>
        <Typography.Text key={data.featureCodes} mark>
          {data.featureCode}
        </Typography.Text>
      </List.Item>
    ));

    return (
      <div>
        <div>
          <Button
            type="primary"
            size={'small'}
            icon="book"
            onClick={this.openModal}
          >
            Violated Features
          </Button>
        </div>
        <div>
          <Modal
            title="VIOLATED FEATURES"
            width="40%"
            visible={modalVisible}
            onOk={this.handleOk}
            footer={[
              <Button key="submit" type="primary" onClick={this.handleOk}>
                OK
              </Button>,
            ]}
          >
            <List size="large" bordered>
              {featureList}
            </List>
          </Modal>
        </div>
      </div>
    );
  }
}

export default withConfigContext(FeatureListModal);
