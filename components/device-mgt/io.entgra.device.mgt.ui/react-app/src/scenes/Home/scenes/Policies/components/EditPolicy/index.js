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
import { Form, Row, Col, Card, Steps, message, notification } from 'antd';
import { withConfigContext } from '../../../../../../components/ConfigContext';
import ConfigureProfile from './components/ConfigureProfile';
import SelectPolicyType from './components/SelectPolicyType';
import AssignGroups from './components/AssignGroups';
import PublishDevices from './components/PublishDevices';
import axios from 'axios';
const { Step } = Steps;

class EditPolicy extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      currentStepIndex: 0,
      policyUIConfigurationsList: null,
      newPolicyPayload: { compliance: 'enforce' },
      policyProfile: {},
      payloadData: {},
      policyFeatureList: [],
      policyData: {},
      deviceType: null,
    };
  }

  componentDidMount() {
    this.getSelectedPolicy(this.props.policyId);
  }

  getPolicyPayloadData = (dataName, dataValue) => {
    Object.defineProperty(this.state.payloadData, dataName, {
      value: dataValue,
      writable: true,
    });
    if (dataName === 'publishDevicesData') {
      this.createPayload();
    }
  };

  createPayload = () => {
    const {
      publishDevicesData,
      configureProfileData,
      policyTypeData,
      groupData,
    } = this.state.payloadData;
    const profile = {
      profileName: publishDevicesData.policyName,
      deviceType: this.state.deviceType,
      profileFeaturesList: configureProfileData,
    };

    const payload = {
      ...publishDevicesData,
      compliance: 'enforce',
      ownershipType: null,
      ...policyTypeData,
      profile: profile,
      ...groupData,
    };
    this.onEditPolicy(JSON.stringify(payload));
  };

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
            isLoading: true,
            policyData: res.data.data,
            deviceType: res.data.data.profile.deviceType,
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
      });
  };

  onEditPolicy = value => {
    axios
      .put(
        window.location.origin +
          this.config.serverConfig.invoker.uri +
          this.config.serverConfig.invoker.deviceMgt +
          '/policies/' +
          this.props.policyId,
        value,
        { headers: { 'Content-Type': 'application-json' } },
      )
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Done',
            duration: 4,
            description: 'Successfully Updated the Policy.',
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
            description: 'Error occurred while trying to Updated the Policy.',
          });
        }
      });
  };

  getNextStep = () => {
    const currentStepIndex = this.state.currentStepIndex + 1;
    this.setState({ currentStepIndex });
  };

  getPrevStep = () => {
    const currentStepIndex = this.state.currentStepIndex - 1;
    this.setState({ currentStepIndex });
  };

  render() {
    const {
      currentStepIndex,
      policyUIConfigurationsList,
      policyFeatureList,
      policyData,
      deviceType,
    } = this.state;
    return (
      <div>
        {policyUIConfigurationsList != null && (
          <Row>
            <Col span={20} offset={2}>
              <Steps style={{ minHeight: 32 }} current={currentStepIndex}>
                <Step key="ProfileConfigure" title="Configure profile" />
                <Step key="PolicyType" title="Select policy type" />
                <Step key="AssignGroups" title="Assign to groups" />
                <Step key="Publish" title="Publish to devices" />
              </Steps>
            </Col>
            <Col span={16} offset={4}>
              <Card style={{ marginTop: 24 }}>
                <div
                  style={{ display: currentStepIndex === 0 ? 'unset' : 'none' }}
                >
                  <ConfigureProfile
                    policyUIConfigurationsList={policyUIConfigurationsList}
                    getPolicyPayloadData={this.getPolicyPayloadData}
                    getNextStep={this.getNextStep}
                    policyFeatureList={policyFeatureList}
                    deviceType={deviceType}
                  />
                </div>
                <div
                  style={{ display: currentStepIndex === 1 ? 'unset' : 'none' }}
                >
                  <SelectPolicyType
                    getPolicyPayloadData={this.getPolicyPayloadData}
                    policyData={policyData}
                    getPrevStep={this.getPrevStep}
                    getNextStep={this.getNextStep}
                  />
                </div>
                <div
                  style={{ display: currentStepIndex === 2 ? 'unset' : 'none' }}
                >
                  <AssignGroups
                    getPolicyPayloadData={this.getPolicyPayloadData}
                    policyData={policyData}
                    getPrevStep={this.getPrevStep}
                    getNextStep={this.getNextStep}
                  />
                </div>
                <div
                  style={{ display: currentStepIndex === 3 ? 'unset' : 'none' }}
                >
                  <PublishDevices
                    policyData={policyData}
                    getPolicyPayloadData={this.getPolicyPayloadData}
                    getPrevStep={this.getPrevStep}
                  />
                </div>
              </Card>
            </Col>
          </Row>
        )}
      </div>
    );
  }
}

export default withConfigContext(
  Form.create({ name: 'edit-policy' })(EditPolicy),
);
