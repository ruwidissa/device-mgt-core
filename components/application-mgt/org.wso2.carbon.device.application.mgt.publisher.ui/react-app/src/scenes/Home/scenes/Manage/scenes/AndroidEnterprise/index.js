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
import { PageHeader, Breadcrumb, Divider, Icon, Result } from 'antd';
import { Link } from 'react-router-dom';
import SyncAndroidApps from './components/SyncAndroidApps';
import { withConfigContext } from '../../../../../../components/ConfigContext';
import GooglePlayIframe from './components/GooglePlayIframe';
import Pages from './components/Pages';
import Authorized from '../../../../../../components/Authorized/Authorized';

class ManageAndroidEnterprise extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
    this.config = this.props.context;
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
            <Breadcrumb.Item>Android Enterprise</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap">
            <h3>Manage Android Enterprise</h3>
            {/* <Paragraph>Lorem ipsum</Paragraph>*/}
          </div>
        </PageHeader>
        <div style={{ background: '#f0f2f5', padding: 24, minHeight: 720 }}>
          <Authorized
            permission="/permission/admin/device-mgt/enterprise/user/view"
            yes={
              <>
                <SyncAndroidApps />
                <Authorized
                  permission="/permission/admin/device-mgt/enterprise/user/modify"
                  yes={<GooglePlayIframe />}
                />
                <Divider />
                <Pages />
              </>
            }
            no={
              <Result
                status="403"
                title="You don't have permission to view android enterprise configurations."
                subTitle="Please contact system administrator"
              />
            }
          />
        </div>
      </div>
    );
  }
}

export default withConfigContext(ManageAndroidEnterprise);
