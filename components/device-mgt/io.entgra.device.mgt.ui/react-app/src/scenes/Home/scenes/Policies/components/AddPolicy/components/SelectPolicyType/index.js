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
  Col,
  Form,
  Icon,
  message,
  notification,
  Radio,
  Select,
  Tooltip,
} from 'antd';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import axios from 'axios';
const { Option } = Select;

class SelectPolicyType extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      correctivePoliciesList: [],
    };
  }

  componentDidMount() {
    this.fetchPolicies();
  }

  fetchPolicies = () => {
    let apiUrl =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      '/policies';

    // send request to the invokerss
    axios
      .get(apiUrl)
      .then(res => {
        if (res.status === 200) {
          let policies = res.data.data.policies;
          let correctivePolicies = [];
          for (let i = 0; i < policies.length; i++) {
            if (policies[i].policyType === 'CORRECTIVE') {
              correctivePolicies.push(
                <Option key={policies[i].profileId}>
                  {policies[i].policyName}
                </Option>,
              );
            }
          }
          this.setState({
            correctivePoliciesList: correctivePolicies,
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
            description: 'Error occurred while trying to load policies.',
          });
        }

        this.setState({ loading: false });
      });
  };

  handlePolicyTypes = event => {
    if (event.target.value === 'GENERAL') {
      document.getElementById('generalPolicySubPanel').style.display = 'block';
    } else {
      document.getElementById('generalPolicySubPanel').style.display = 'none';
    }
  };

  render() {
    const { getFieldDecorator } = this.props.form;
    return (
      <div>
        <Form.Item style={{ display: 'block' }}>
          {getFieldDecorator('policyType', {
            initialValue: 'GENERAL',
          })(
            <Radio.Group onChange={this.handlePolicyTypes}>
              <Radio value="GENERAL">General Policy</Radio>
              <Radio value="CORRECTIVE">Corrective Policy</Radio>
            </Radio.Group>,
          )}
        </Form.Item>
        <div id="generalPolicySubPanel" style={{ display: 'block' }}>
          <Form.Item
            label={
              <span>
                Select Corrective Policy&nbsp;
                <Tooltip
                  title={
                    'Select the corrective policy to be applied when this general policy is violated'
                  }
                  placement="right"
                >
                  <Icon type="question-circle-o" />
                </Tooltip>
              </span>
            }
          >
            {getFieldDecorator('correctiveActions', {
              initialValue: 'NONE',
            })(
              <Select style={{ width: '100%' }}>
                <Option value="NONE">None</Option>
                {this.state.correctivePoliciesList}
              </Select>,
            )}
          </Form.Item>
        </div>
        <Col span={16} offset={20}>
          <div style={{ marginTop: 24 }}>
            <Button style={{ marginRight: 8 }} onClick={this.props.getPrevStep}>
              Back
            </Button>
            <Button type="primary" onClick={this.props.getNextStep}>
              Continue
            </Button>
          </div>
        </Col>
      </div>
    );
  }
}

export default withConfigContext(Form.create()(SelectPolicyType));
