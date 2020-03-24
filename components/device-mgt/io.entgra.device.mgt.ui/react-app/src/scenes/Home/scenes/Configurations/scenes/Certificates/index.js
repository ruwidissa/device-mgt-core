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
import CertificateTable from './components/CertificateTable';

const { Paragraph } = Typography;

class Certificates extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
  }

  render() {
    return (
      <div>
        <PageHeader style={{ paddingTop: 0 }}>
          <Breadcrumb style={{ paddingBottom: 16 }}>
            <Breadcrumb.Item>
              <Link to="/entgra/devices">
                <Icon type="home" /> Home
              </Link>
            </Breadcrumb.Item>
            <Breadcrumb.Item>Configurations</Breadcrumb.Item>
            <Breadcrumb.Item>Certificates</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap">
            <h3>Certificates</h3>
            <Paragraph>Certificate configurations</Paragraph>
          </div>
          <div style={{ backgroundColor: '#ffffff', borderRadius: 5 }}>
            <CertificateTable />
          </div>
        </PageHeader>
        <div
          style={{ background: '#f0f2f5', padding: 24, minHeight: 720 }}
        ></div>
      </div>
    );
  }
}

export default Certificates;
