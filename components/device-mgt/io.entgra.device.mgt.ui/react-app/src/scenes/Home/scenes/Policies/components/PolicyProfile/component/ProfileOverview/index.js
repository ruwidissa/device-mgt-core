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
import { Card, Col, Icon, Row, Typography } from 'antd';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
const { Title, Text } = Typography;

class ProfileOverview extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      data: {},
    };
  }

  render() {
    const { policyData } = this.props;
    return (
      <div>
        <div style={{ marginTop: 20 }}>
          <div>
            <Title level={4}>Policy Overview</Title>
          </div>
          <Card>
            <div style={{ paddingLeft: 4 }}>
              <Row style={{ marginTop: 8 }}>
                <Col span={8}>
                  <Text>Platform</Text>
                </Col>
                <Col span={8}>
                  <Text>{policyData.profile.deviceType.toUpperCase()}</Text>
                </Col>
              </Row>
              <hr style={{ backgroundColor: 'grey' }} />
              <Row style={{ marginTop: 15 }}>
                <Col span={8}>
                  <Text>Groups</Text>
                </Col>
                <Col span={8}>
                  <Text>{policyData.deviceGroups}</Text>
                </Col>
              </Row>
              <hr />
              <Row style={{ marginTop: 15 }}>
                <Col span={8}>
                  <Text>Action upon non-compliance</Text>
                </Col>
                <Col span={8}>
                  <Text>{policyData.compliance.toUpperCase()}</Text>
                </Col>
              </Row>
              <hr />
              <Row style={{ marginTop: 15 }}>
                <Col span={8}>
                  <Text>Status</Text>
                </Col>
                <Col span={8}>
                  <Text>
                    {policyData.active ? (
                      <span>
                        <Icon
                          type="exclamation-circle"
                          style={{ color: '#6ab04c' }}
                        />
                        Active
                      </span>
                    ) : (
                      <span>
                        <Icon
                          type="exclamation-circle"
                          style={{ color: '#f9ca24' }}
                        />
                        Inactive
                      </span>
                    )}
                    {policyData.updated ? <span>/Updated</span> : <span></span>}
                  </Text>
                </Col>
              </Row>
              <hr />
              <Row style={{ marginTop: 15 }}>
                <Col span={8}>
                  <Text>Assigned Roles</Text>
                </Col>
                <Col span={8}>{policyData.roles}</Col>
              </Row>
              <hr />
              <Row style={{ marginTop: 15 }}>
                <Col span={8}>
                  <Text type={8}>Policy Type</Text>
                </Col>
                <Col span={8}>
                  <Text>{policyData.policyType}</Text>
                </Col>
              </Row>
            </div>
          </Card>
        </div>
        <div style={{ marginTop: 20 }}>
          <Title level={4}>Description</Title>
          <Card>
            <Row>
              <Col span={4}>
                <Text>{policyData.description}</Text>
              </Col>
            </Row>
          </Card>
        </div>
      </div>
    );
  }
}

export default withConfigContext(ProfileOverview);
