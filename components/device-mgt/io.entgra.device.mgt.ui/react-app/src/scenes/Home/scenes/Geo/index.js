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
import { PageHeader, Typography, Breadcrumb, Icon } from 'antd';
import { Link } from 'react-router-dom';
import GeoDashboard from './components/GeoDashboard';

const { Paragraph } = Typography;

class Geo extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
  }

  render() {
    const { deviceIdentifier, deviceType } = this.props.match.params;
    return (
      <div>
        <PageHeader style={{ paddingTop: 0 }}>
          <Breadcrumb style={{ paddingBottom: 16 }}>
            <Breadcrumb.Item>
              <Link to="/entgra">
                <Icon type="home" /> Home
              </Link>
            </Breadcrumb.Item>
            <Breadcrumb.Item>
              <Link to="/entgra">Devices</Link>
            </Breadcrumb.Item>
            <Breadcrumb.Item>{`Location History - ${deviceType} / ${deviceIdentifier}`}</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap">
            <h3>Location History </h3>
            <Paragraph>{`${deviceType} / ${deviceIdentifier}`}</Paragraph>
          </div>
        </PageHeader>
        <div
          style={{
            background: '#f0f2f5',
            padding: 24,
            minHeight: 720,
            alignItems: 'center',
          }}
        >
          <GeoDashboard
            deviceIdentifier={deviceIdentifier}
            deviceType={deviceType}
          />
        </div>
      </div>
    );
  }
}

export default Geo;
