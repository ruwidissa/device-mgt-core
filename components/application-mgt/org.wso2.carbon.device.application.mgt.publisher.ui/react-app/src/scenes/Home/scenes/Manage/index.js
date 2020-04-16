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
import { PageHeader, Typography, Breadcrumb, Row, Col, Icon } from 'antd';
import ManageCategories from './components/Categories';
import ManageTags from './components/Tags';
import { Link } from 'react-router-dom';
import Authorized from '../../../../components/Authorized/Authorized';

const { Paragraph } = Typography;

class Manage extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
  }

  render() {
    return (
      <div>
        <PageHeader style={{ paddingTop: 0, backgroundColor: '#fff' }}>
          <Breadcrumb style={{ paddingBottom: 16 }}>
            <Breadcrumb.Item>
              <Link to="/publisher/apps">
                <Icon type="home" /> Home
              </Link>
            </Breadcrumb.Item>
            <Breadcrumb.Item>Manage</Breadcrumb.Item>
            <Breadcrumb.Item>General</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap">
            <h3>Manage General Settings</h3>
            <Paragraph>
              Maintain and manage categories and tags here..
            </Paragraph>
          </div>
        </PageHeader>
        <div style={{ background: '#f0f2f5', padding: 24, minHeight: 780 }}>
          <Row gutter={16}>
            <Authorized
              permission="/permission/admin/app-mgt/publisher/application/update"
              yes={
                <>
                  <Col sm={24} md={12}>
                    <ManageCategories />
                  </Col>
                  <Col sm={24} md={12}>
                    <ManageTags />
                  </Col>
                </>
              }
            />
          </Row>
        </div>
      </div>
    );
  }
}

export default Manage;
