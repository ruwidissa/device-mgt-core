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
import { Col, message, notification } from 'antd';
import { withConfigContext } from '../../../../../../components/ConfigContext';
import PolicyInfo from './component/PolicyInfo';
import ProfileOverview from './component/ProfileOverview';
import axios from 'axios';

class PolicyProfile extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      policyId: this.props.policyId,
      policyData: null,
      policyUIConfigurationsList: [],
      policyFeatureList: [],
    };
  }

  componentDidMount() {
    this.getSelectedPolicy(this.props.policyId);
  }

  getSelectedPolicy = policyId => {
    let apiUrl =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      '/policies/' +
      policyId;

    // send request to the invokers
    axios
      .get(apiUrl)
      .then(res => {
        if (res.status === 200) {
          this.setState({
            policyData: res.data.data,
            policyFeatureList: res.data.data.profile.profileFeaturesList,
          });
          this.getPolicyConfigJson(res.data.data.profile.deviceType);
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
            description: 'Error occurred while trying to load selected policy.',
          });
        }
      });
  };

  getPolicyConfigJson = type => {
    this.setState({ isLoading: true });

    let apiUrl =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      '/device-types/' +
      type +
      '/ui-policy-configurations';
    // send request to the invokers
    axios
      .get(apiUrl)
      .then(res => {
        if (res.status === 200) {
          this.setState({
            isLoading: false,
            policyUIConfigurationsList: JSON.parse(res.data.data),
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
            description: 'Error occurred while trying to load Policy details.',
          });
        }
        this.setState({ isLoading: false });
      });
  };

  render() {
    const {
      policyData,
      policyUIConfigurationsList,
      policyFeatureList,
    } = this.state;
    return (
      <div>
        <Col span={16} offset={4}>
          {/* <Card style={{ marginTop: 24 }}>*/}
          <div>
            {policyData != null && (
              <ProfileOverview
                policyId={this.props.policyId}
                policyData={policyData}
              />
            )}
          </div>
          <div>
            {policyData != null && (
              <PolicyInfo
                policyId={this.state.policyId}
                policyFeatureList={policyFeatureList}
                policyUIConfigurationsList={policyUIConfigurationsList}
              />
            )}
          </div>
        </Col>
      </div>
    );
  }
}

export default withConfigContext(PolicyProfile);
