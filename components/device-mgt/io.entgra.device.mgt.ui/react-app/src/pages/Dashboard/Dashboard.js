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

import React from "react";
import {Layout, Menu, Icon} from 'antd';
import {Switch, Link} from "react-router-dom";
import RouteWithSubRoutes from "../../components/RouteWithSubRoutes"
import {Redirect} from 'react-router'
import "./Dashboard.css";
import {withConfigContext} from "../../context/ConfigContext";
import Logout from "./Logout/Logout";

const {Header, Content, Footer, Sider} = Layout;
const {SubMenu} = Menu;

class Dashboard extends React.Component {
    constructor(props) {
        super(props);

        const mobileWidth = (window.innerWidth<=768 ? '0' : '80');

        this.state = {
            routes: props.routes,
            selectedKeys: [],
            deviceTypes: [],
            isNavBarCollapsed: false,
            mobileWidth
        };
        this.logo = this.props.context.theme.logo;
    }

    toggle = () => {
        this.setState({
            isNavBarCollapsed: !this.state.isNavBarCollapsed,
        });
    };

    render() {
        return (
            <div>
                <Layout className="layout" >

                    <Sider
                        trigger={null}
                        collapsible
                        collapsed={this.state.isNavBarCollapsed}
                        collapsedWidth={this.state.mobileWidth}
                    >

                        <div className="logo-image">
                            <Link to="/entgra/devices"><img alt="logo" src={this.logo}/>
                            <span className="brand">Entgra</span></Link>
                        </div>
                        <Menu theme="dark" mode="inline" defaultSelectedKeys={['devices']}>
                           <Menu.Item key="devices">
                                <Link to="/entgra/devices">
                                    <Icon type="appstore"/>
                                    <span>Devices</span>
                                </Link>
                            </Menu.Item>
                            <Menu.Item key="geo">
                                <Link to="/entgra/geo">
                                    <Icon type="environment"/>
                                    <span>Geo</span>
                                </Link>
                            </Menu.Item>
                            <Menu.Item key="reports">
                                <Link to="/entgra/reports">
                                    <Icon type="bar-chart"/>
                                    <span>Reports</span>
                                </Link>
                            </Menu.Item>
                        </Menu>

                    </Sider>

                    <Layout>
                        <Header style={{background: '#fff', padding: 0}}>
                            <div className="trigger">
                            <Icon
                                type={this.state.isNavBarCollapsed ? 'menu-unfold' : 'menu-fold'}
                                onClick={this.toggle}
                            />
                            </div>

                            <Menu
                                theme="light"
                                mode="horizontal"
                                style={{lineHeight: '64px'}}
                            >
                                <Menu.Item key="trigger">
                                </Menu.Item>
                                <SubMenu className="profile"
                                         title={
                                             <span className="submenu-title-wrapper">
                                     <Icon type="user"/>
                                     </span>
                                         }
                                >
                                    <Logout/>
                                </SubMenu>

                            </Menu>
                        </Header>

                        <Content style={{marginTop: 2}}>
                            <Switch>
                                <Redirect exact from="/entgra" to="/entgra/devices"/>
                                {this.state.routes.map((route) => (
                                    <RouteWithSubRoutes key={route.path} {...route} />
                                ))}
                            </Switch>
                        </Content>

                        <Footer style={{textAlign: 'center'}}>
                            ©2019 entgra.io
                        </Footer>

                    </Layout>
                </Layout>
            </div>
        );
    }
}

export default withConfigContext(Dashboard);
