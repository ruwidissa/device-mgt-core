/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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
import { Layout, Menu, Icon, Drawer, Button, Alert } from 'antd';

const { Header, Content, Footer } = Layout;
import { Link } from 'react-router-dom';
import RouteWithSubRoutes from '../../components/RouteWithSubRoutes';
import { Switch } from 'react-router';
import axios from 'axios';
import './styles.css';
import { withConfigContext } from '../../components/context/ConfigContext';
import Logout from './components/Logout';
import { handleApiError } from '../../services/utils/errorHandler';

const { SubMenu } = Menu;

class Dashboard extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      routes: props.routes,
      selectedKeys: [],
      deviceTypes: [],
      visible: false,
      collapsed: false,
      forbiddenErrors: {
        deviceTypes: false,
      },
    };
    this.logo = this.props.context.theme.logo;
    this.footerText = this.props.context.theme.footerText;
    this.config = this.props.context;
  }

  componentDidMount() {
    this.getDeviceTypes();
  }

  getDeviceTypes = () => {
    const config = this.props.context;
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/device-types',
      )
      .then(res => {
        if (res.status === 200) {
          const deviceTypes = JSON.parse(res.data.data);
          this.setState({
            deviceTypes,
            loading: false,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to load device types.',
          true,
        );
        if (error.hasOwnProperty('response') && error.response.status === 403) {
          const { forbiddenErrors } = this.state;
          forbiddenErrors.deviceTypes = true;
          this.setState({
            forbiddenErrors,
            loading: false,
          });
        } else {
          this.setState({
            loading: false,
          });
        }
      });
  };

  changeSelectedMenuItem = key => {
    this.setState({
      selectedKeys: [key],
    });
  };

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
    const config = this.props.context;
    const { selectedKeys, deviceTypes, forbiddenErrors } = this.state;

    const DeviceTypesData = deviceTypes.map(deviceType => {
      const platform = deviceType.name;
      const defaultPlatformIcons = config.defaultPlatformIcons;
      let icon = defaultPlatformIcons.default.icon;
      let theme = defaultPlatformIcons.default.theme;
      if (defaultPlatformIcons.hasOwnProperty(platform)) {
        icon = defaultPlatformIcons[platform].icon;
        theme = defaultPlatformIcons[platform].theme;
      }
      return (
        <Menu.Item key={platform}>
          <Link to={'/store/' + platform}>
            <Icon type={icon} theme={theme} />
            {platform}
          </Link>
        </Menu.Item>
      );
    });

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
              <Link to="/store">
                <img alt="logo" src={this.logo} />
              </Link>
            </div>

            <div className="web-layout">
              <Menu
                theme="light"
                mode="horizontal"
                defaultSelectedKeys={selectedKeys}
                style={{ lineHeight: '64px' }}
              >
                {DeviceTypesData}

                <Menu.Item key="web-clip">
                  <Link to="/store/web-clip">
                    <Icon type="upload" />
                    Web Clips
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
            <Link to="/store" onClick={this.onCloseMobileNavigationBar}>
              <img
                alt="logo"
                src={this.logo}
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
            defaultSelectedKeys={selectedKeys}
            style={{ lineHeight: '64px', width: 231 }}
            onClick={this.onCloseMobileNavigationBar}
          >
            {DeviceTypesData}

            <Menu.Item key="web-clip">
              <Link to="/store/web-clip">
                <Icon type="upload" />
                Web Clips
              </Link>
            </Menu.Item>
          </Menu>
        </Drawer>
        <Layout className="mobile-layout">
          <Menu
            mode="horizontal"
            defaultSelectedKeys={selectedKeys}
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
          {forbiddenErrors.deviceTypes && (
            <Alert
              message="You don't have permission to view device types."
              type="warning"
              banner
              closable
            />
          )}
          <Content style={{ padding: '0 0' }}>
            <Switch>
              {this.state.routes.map(route => (
                <RouteWithSubRoutes
                  changeSelectedMenuItem={this.changeSelectedMenuItem}
                  key={route.path}
                  {...route}
                />
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
