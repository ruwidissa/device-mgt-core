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
import { Form, Row, Col, Card, Steps } from 'antd';
import { withConfigContext } from '../../context/ConfigContext';
import DeviceType from './DeviceType';
import EnrollAgent from './EnrollAgent';
const { Step } = Steps;

class AddDevice extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      isAddDeviceModalVisible: false,
      current: 0,
    };
  }

  onClickType = () => {
    this.setState({
      current: 1,
    });
  };

  render() {
    const { current } = this.state;
    return (
      <div>
        <Row>
          <Col span={16} offset={4}>
            <Steps style={{ minHeight: 32 }} current={current}>
              <Step key="DeviceType" title="Device Type" />
              <Step key="EnrollAgent" title="Enroll Agent" />
              <Step key="Result" title="Result" />
            </Steps>
          </Col>
          <Col span={16} offset={4}>
            <Card style={{ marginTop: 24 }}>
              <div style={{ display: current === 0 ? 'unset' : 'none' }}>
                <DeviceType onClickType={this.onClickType} />
              </div>
              <div style={{ display: current === 1 ? 'unset' : 'none' }}>
                <EnrollAgent />
              </div>

              <div style={{ display: current === 2 ? 'unset' : 'none' }}></div>
            </Card>
          </Col>
        </Row>
      </div>
    );
  }
}

export default withConfigContext(
  Form.create({ name: 'add-device' })(AddDevice),
);
