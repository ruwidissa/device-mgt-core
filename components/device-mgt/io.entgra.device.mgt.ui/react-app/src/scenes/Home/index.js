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
import { Layout, Menu, Icon } from 'antd';
import { Switch, Link } from 'react-router-dom';
import RouteWithSubRoutes from '../../components/RouteWithSubRoutes';
import { Redirect } from 'react-router';
import './styles.css';
import { withConfigContext } from '../../components/ConfigContext';
import Logout from './components/Logout';

const { Header, Content, Footer } = Layout;
const { SubMenu } = Menu;

class Home extends React.Component {
  constructor(props) {
    super(props);

    const mobileWidth = window.innerWidth <= 768 ? '0' : '80';

    this.state = {
      routes: props.routes,
      selectedKeys: [],
      deviceTypes: [],
      isNavBarCollapsed: false,
      mobileWidth,
    };
    this.logo = this.props.context.theme.logo;
    this.config = this.props.context;
  }

  toggle = () => {
    console.log(this.config);
    this.setState({
      isNavBarCollapsed: !this.state.isNavBarCollapsed,
    });
  };

  render() {
    return (
      <div>
        <Layout className="layout">
          <Layout>
            <Header style={{ background: '#fff', padding: 0 }}>
              <div className="logo-image">
                <Link to="/entgra/reports">
                  <img alt="logo" src={this.logo} />
                </Link>
              </div>

              <Menu
                theme="light"
                mode="horizontal"
                style={{
                  lineHeight: '64px',
                  marginRight: 110,
                }}
              >
                <SubMenu
                  key="devices"
                  title={
                    <span>
                      <Icon type="appstore" />
                      <span>Devices</span>
                    </span>
                  }
                >
                  <Menu.Item key="devices">
                    <Link to="/entgra/devices">
                      <span>View</span>
                    </Link>
                  </Menu.Item>
                  <Menu.Item key="deviceEnroll">
                    <Link to="/entgra/devices/enroll">
                      <span>Enroll</span>
                    </Link>
                  </Menu.Item>
                </SubMenu>
                <SubMenu
                  key="geo"
                  title={
                    <span>
                      <Icon type="environment" />
                      <span>Geo</span>
                    </span>
                  }
                >
                  <Menu.Item key="singleDevice">
                    <Link to="/entgra/geo">
                      <span>Single Device View</span>
                    </Link>
                  </Menu.Item>
                  <Menu.Item key="deviceGroup">
                    <Link to="#">
                      <span>Device Group View</span>
                    </Link>
                  </Menu.Item>
                </SubMenu>
                <Menu.Item key="reports">
                  <Link to="/entgra/reports">
                    <Icon type="bar-chart" />
                    <span>Reports</span>
                  </Link>
                </Menu.Item>
                <Menu.Item key="groups">
                  <Link to="/entgra/groups">
                    <Icon type="deployment-unit" />
                    <span>Groups</span>
                  </Link>
                </Menu.Item>
                <Menu.Item key="users">
                  <Link to="/entgra/users">
                    <Icon type="user" />
                    <span>Users</span>
                  </Link>
                </Menu.Item>
                <SubMenu
                  key="policies"
                  title={
                    <span>
                      <Icon type="audit" />
                      <span>Policies</span>
                    </span>
                  }
                >
                  <Menu.Item key="policiesList">
                    <Link to="/entgra/policies">
                      <span>View</span>
                    </Link>
                  </Menu.Item>
                  <Menu.Item key="addPolicy">
                    <Link to="/entgra/policy/add">
                      <span>Add New Policy</span>
                    </Link>
                  </Menu.Item>
                </SubMenu>
                <Menu.Item key="roles">
                  <Link to="/entgra/roles">
                    <Icon type="book" />
                    <span>Roles</span>
                  </Link>
                </Menu.Item>
                <Menu.Item key="devicetypes">
                  <Link to="/entgra/devicetypes">
                    <Icon type="desktop" />
                    <span>Device Types</span>
                  </Link>
                </Menu.Item>
                <SubMenu
                  key="configurations"
                  title={
                    <span>
                      <Icon type="setting" />
                      <span>Configurations</span>
                    </span>
                  }
                >
                  <Menu.Item key="certificates">
                    <Link to="/entgra/certificates">
                      <span>Certificates</span>
                    </Link>
                  </Menu.Item>
                </SubMenu>
                <Menu.Item className="profile" key="Notifications">
                  <Link to="/entgra/notifications">
                    <span>Notifications</span>
                  </Link>
                </Menu.Item>
                <SubMenu
                  className="profile"
                  title={
                    <span className="submenu-title-wrapper">
                      <Icon type="user" />
                      {this.config.user}
                    </span>
                  }
                >
                  <Logout />
                </SubMenu>
              </Menu>
            </Header>

            <Content>
              <Switch>
                <Redirect exact from="/entgra/devices" to="/entgra/reports" />
                {this.state.routes.map(route => (
                  <RouteWithSubRoutes key={route.path} {...route} />
                ))}
              </Switch>
            </Content>

            <Footer style={{ textAlign: 'center' }}>©2019 entgra.io</Footer>
          </Layout>
        </Layout>
      </div>
    );
  }
}

export default withConfigContext(Home);
