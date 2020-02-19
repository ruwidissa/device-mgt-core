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
import { Card, Col, Icon, Row } from 'antd';
import TimeAgo from 'javascript-time-ago';
// Load locale-specific relative date/time formatting rules.
import en from 'javascript-time-ago/locale/en';
import { withConfigContext } from '../../../../../../../../../../components/ConfigContext';

class DeviceType extends React.Component {
  constructor(props) {
    super(props);
    TimeAgo.addLocale(en);
    this.config = this.props.context;
    this.state = {
      data: this.config.deviceTypes,
      pagination: {},
      loading: false,
      selectedRows: [],
    };
  }

  onClickCard = (e, deviceType) => {
    this.props.getDeviceType(deviceType);
  };

  render() {
    const { data } = this.state;
    const { Meta } = Card;
    const itemCard = data.map(data => (
      <Col span={5} key={data.id}>
        <Card
          size="default"
          style={{ width: 150 }}
          bordered={true}
          onClick={e => this.onClickCard(e, data.name)}
          cover={
            <Icon
              type="android"
              key="device-types"
              style={{
                color: '#ffffff',
                backgroundColor: '#4b92db',
                fontSize: '100px',
                padding: '20px',
              }}
            />
          }
        >
          <Meta title={data.name} />
        </Card>
      </Col>
    ));
    return (
      <div>
        <Row gutter={16}>{itemCard}</Row>
      </div>
    );
  }
}

export default withConfigContext(DeviceType);
