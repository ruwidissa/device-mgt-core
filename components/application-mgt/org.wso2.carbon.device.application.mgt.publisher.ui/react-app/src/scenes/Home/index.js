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
import { Layout, Menu, Icon, Drawer, Button } from 'antd';
import { Switch, Link } from 'react-router-dom';
import RouteWithSubRoutes from '../../components/RouteWithSubRoutes';
import { Redirect } from 'react-router';
import './styles.css';
import { withConfigContext } from '../../components/ConfigContext';
import Logout from './components/Logout';
import { isAuthorized } from '../../services/utils/authorizationHandler';

const { Header, Content, Footer } = Layout;
const { SubMenu } = Menu;

class Dashboard extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      routes: props.routes,
      visible: false,
      collapsed: false,
    };
    this.config = this.props.context;
    this.Logo = this.config.theme.logo;
    this.footerText = this.config.theme.footerText;
  }

  showMobileNavigationBar = () => {
    this.setState({
      visible: true,
      collapsed: !this.state.collapsed,
    });
  };

  onCloseMobileNavigationBar = () => {
    this.setState({
      visible: false,
    });
  };

  render() {
    return (
      <div>
        <Layout>
          <Header
            style={{
              paddingLeft: 0,
              paddingRight: 0,
              backgroundColor: 'white',
            }}
          >
            <div className="logo-image">
              <Link to="/publisher/apps">
                <img alt="logo" src={this.Logo} />
              </Link>
            </div>

            <div className="web-layout">
              <Menu
                theme="light"
                mode="horizontal"
                defaultSelectedKeys={['1']}
                style={{ lineHeight: '64px' }}
              >
                <Menu.Item key="1">
                  <Link to="/publisher/apps">
                    <Icon type="appstore" />
                    Apps
                  </Link>
                </Menu.Item>
                {isAuthorized(
                  this.props.context.user,
                  '/permission/admin/app-mgt/publisher/application/update',
                ) && (
                  <SubMenu
                    title={
                      <span className="submenu-title-wrapper">
                        <Icon type="plus" />
                        Add New App
                      </span>
                    }
                  >
                    <Menu.Item key="add-new-public-app">
                      <Link to="/publisher/add-new-app/public">Public App</Link>
                    </Menu.Item>
                    <Menu.Item key="add-new-enterprise-app">
                      <Link to="/publisher/add-new-app/enterprise">
                        Enterprise App
                      </Link>
                    </Menu.Item>
                    <Menu.Item key="add-new-web-clip">
                      <Link to="/publisher/add-new-app/web-clip">Web Clip</Link>
                    </Menu.Item>
                    <Menu.Item key="add-new-custom-app">
                      <Link to="/publisher/add-new-app/custom-app">
                        Custom App
                      </Link>
                    </Menu.Item>
                  </SubMenu>
                )}
                <SubMenu
                  title={
                    <span className="submenu-title-wrapper">
                      <Icon type="control" />
                      Manage
                    </span>
                  }
                >
                  <Menu.Item key="manage">
                    <Link to="/publisher/manage">
                      <Icon type="setting" /> General
                    </Link>
                  </Menu.Item>
                  {this.config.androidEnterpriseToken != null && (
                    <Menu.Item key="manage-android-enterprise">
                      <Link to="/publisher/manage/android-enterprise">
                        <Icon type="android" theme="filled" /> Android
                        Enterprise
                      </Link>
                    </Menu.Item>
                  )}
                </SubMenu>

                <SubMenu
                  className="profile"
                  title={
                    <span className="submenu-title-wrapper">
                      <Icon type="user" />
                      {this.config.username}
                    </span>
                  }
                >
                  <Logout />
                </SubMenu>
              </Menu>
            </div>
          </Header>
        </Layout>

        <Layout className="mobile-layout">
          <div className="mobile-menu-button">
            <Button type="link" onClick={this.showMobileNavigationBar}>
              <Icon
                type={this.state.collapsed ? 'menu-fold' : 'menu-unfold'}
                className="bar-icon"
              />
            </Button>
          </div>
        </Layout>
        <Drawer
          title={
            <Link
              to="/publisher/apps"
              onClick={this.onCloseMobileNavigationBar}
            >
              <img
                alt="logo"
                src={this.Logo}
                style={{ marginLeft: 30 }}
                width={'60%'}
              />
            </Link>
          }
          placement="left"
          closable={false}
          onClose={this.onCloseMobileNavigationBar}
          visible={this.state.visible}
          getContainer={false}
          style={{ position: 'absolute' }}
        >
          <Menu
            theme="light"
            mode="inline"
            defaultSelectedKeys={['1']}
            style={{ lineHeight: '64px', width: 231 }}
            onClick={this.onCloseMobileNavigationBar}
          >
            <Menu.Item key="1">
              <Link to="/publisher/apps">
                <Icon type="appstore" />
                Apps
              </Link>
            </Menu.Item>
            <SubMenu
              title={
                <span className="submenu-title-wrapper">
                  <Icon type="plus" />
                  Add New App
                </span>
              }
            >
              <Menu.Item key="setting:1">
                <Link to="/publisher/add-new-app/public">Public APP</Link>
              </Menu.Item>
              <Menu.Item key="setting:2">
                <Link to="/publisher/add-new-app/enterprise">
                  Enterprise APP
                </Link>
              </Menu.Item>
              <Menu.Item key="setting:3">
                <Link to="/publisher/add-new-app/web-clip">Web Clip</Link>
              </Menu.Item>
              <Menu.Item key="setting:4">
                <Link to="/publisher/add-new-app/custom-app">Custom App</Link>
              </Menu.Item>
            </SubMenu>
            <Menu.Item key="2">
              <Link to="/publisher/manage">
                <Icon type="control" />
                Manage
              </Link>
            </Menu.Item>
          </Menu>
        </Drawer>
        <Layout className="mobile-layout">
          <Menu
            mode="horizontal"
            defaultSelectedKeys={['1']}
            style={{ lineHeight: '63px', position: 'fixed', marginLeft: '80%' }}
          >
            <SubMenu
              title={
                <span className="submenu-title-wrapper">
                  <Icon type="user" />
                </span>
              }
            >
              <Logout />
            </SubMenu>
          </Menu>
        </Layout>

        <Layout className="dashboard-body">
          <Content style={{ marginTop: 2 }}>
            <Switch>
              <Redirect exact from="/publisher" to="/publisher/apps" />
              {this.state.routes.map(route => (
                <RouteWithSubRoutes key={route.path} {...route} />
              ))}
            </Switch>
          </Content>
          <Footer style={{ textAlign: 'center' }}>{this.footerText}</Footer>
        </Layout>
      </div>
    );
  }
}

export default withConfigContext(Dashboard);
