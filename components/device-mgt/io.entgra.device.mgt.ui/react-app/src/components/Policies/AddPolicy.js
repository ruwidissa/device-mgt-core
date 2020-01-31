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
  Button,
  Form,
  Row,
  Col,
  Card,
  Steps,
  message,
  notification,
} from 'antd';
import { withConfigContext } from '../../context/ConfigContext';
import SelectPlatform from './SelectPlatform';
import ConfigureProfile from './ConfigureProfile';
import axios from 'axios';
const { Step } = Steps;

class AddPolicy extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      currentStepIndex: 0,
      isLoading: false,
      policyUIConfigurationsList: [],
    };
  }

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
            currentStepIndex: 1,
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

  onHandleNext = () => {
    const currentStepIndex = this.state.currentStepIndex + 1;
    this.setState({ currentStepIndex });
  };

  onHandlePrev = () => {
    const currentStepIndex = this.state.currentStepIndex - 1;
    this.setState({ currentStepIndex });
  };

  render() {
    const { currentStepIndex, policyUIConfigurationsList } = this.state;
    return (
      <div>
        <Row>
          <Col span={20} offset={2}>
            <Steps style={{ minHeight: 32 }} current={currentStepIndex}>
              <Step key="Platform" title="Select a Platform" />
              <Step key="ProfileConfigure" title="Configure profile" />
              <Step key="PolicyType" title="Select policy type" />
              <Step key="AssignGroups" title="Assign to groups" />
              <Step key="Publish" title="Publish to devices" />
              <Step key="Result" title="Result" />
            </Steps>
          </Col>
          <Col span={16} offset={4}>
            <Card style={{ marginTop: 24 }}>
              <div
                style={{ display: currentStepIndex === 0 ? 'unset' : 'none' }}
              >
                <SelectPlatform
                  getPolicyConfigJson={this.getPolicyConfigJson}
                />
              </div>
              <div
                style={{ display: currentStepIndex === 1 ? 'unset' : 'none' }}
              >
                <ConfigureProfile
                  policyUIConfigurationsList={policyUIConfigurationsList}
                />
              </div>
              <div
                style={{ display: currentStepIndex === 2 ? 'unset' : 'none' }}
              ></div>
              <div
                style={{ display: currentStepIndex === 3 ? 'unset' : 'none' }}
              ></div>
              <div
                style={{ display: currentStepIndex === 4 ? 'unset' : 'none' }}
              ></div>
              <div
                style={{ display: currentStepIndex === 5 ? 'unset' : 'none' }}
              ></div>
            </Card>
          </Col>
          <Col span={16} offset={4}>
            <div style={{ marginTop: 24 }}>
              {currentStepIndex > 0 && (
                <Button
                  style={{ marginRight: 8 }}
                  onClick={() => this.onHandlePrev()}
                >
                  Previous
                </Button>
              )}
              {currentStepIndex > 0 && currentStepIndex < 5 && (
                <Button type="primary" onClick={() => this.onHandleNext()}>
                  Next
                </Button>
              )}
              {currentStepIndex === 5 && <Button type="primary">Done</Button>}
            </div>
          </Col>
        </Row>
      </div>
    );
  }
}

export default withConfigContext(
  Form.create({ name: 'add-policy' })(AddPolicy),
);
