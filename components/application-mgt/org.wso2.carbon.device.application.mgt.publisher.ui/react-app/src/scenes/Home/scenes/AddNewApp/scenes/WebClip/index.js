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
import { Icon, PageHeader, Typography, Breadcrumb, Result } from 'antd';
import AddNewAppForm from '../../components/AddNewAppForm';
import { Link } from 'react-router-dom';
import Authorized from '../../../../../../components/Authorized/Authorized';

const { Paragraph } = Typography;

const formConfig = {
  installationType: 'WEB_CLIP',
  endpoint: '/web-app',
  jsonPayloadName: 'webapp',
  releaseWrapperName: 'webAppReleaseWrappers',
  specificElements: {
    url: {
      required: true,
    },
    version: {
      required: true,
    },
  },
};

class AddNewEnterpriseApp extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      current: 0,
      categories: [],
    };
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
            <Breadcrumb.Item>Add New Web Clip</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap">
            <h3>Add New Web Clip</h3>
            <Paragraph>Share a Web Clip to your corporate app store.</Paragraph>
          </div>
        </PageHeader>
        <div style={{ background: '#f0f2f5', padding: 24, minHeight: 720 }}>
          <Authorized
            permission="/permission/admin/app-mgt/publisher/application/update"
            yes={<AddNewAppForm formConfig={formConfig} />}
            no={
              <Result
                status="403"
                title="You don't have permission to add new apps."
                subTitle="Please contact system administrator"
              />
            }
          />
        </div>
      </div>
    );
  }
}

export default AddNewEnterpriseApp;
