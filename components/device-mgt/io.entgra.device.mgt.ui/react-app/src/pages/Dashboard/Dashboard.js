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
import "../../App.css";
import {withConfigContext} from "../../context/ConfigContext";
import Logout from "./Logout/Logout";

const {Header, Content, Footer} = Layout;
const {SubMenu} = Menu;


class Dashboard extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            routes: props.routes,
            selectedKeys: [],
            deviceTypes: []
        };
        this.logo = this.props.context.theme.logo;
    }

    render() {
        return (
            <div>
                <Layout className="layout">
                    <Header style={{paddingLeft: 0, paddingRight: 0}}>
                        <div className="logo-image">
                            <img alt="logo" src={this.logo}/>
                        </div>
                        <Menu
                            theme="light"
                            mode="horizontal"
                            defaultSelectedKeys={['1']}
                            style={{lineHeight: '64px'}}
                        >
                            <Menu.Item key="devices"><Link to="/entgra/devices"><Icon type="appstore"/>Devices</Link></Menu.Item>
                            <Menu.Item key="geo"><Link to="/entgra/geo"><Icon type="environment"/>Geo</Link></Menu.Item>
                            <Menu.Item key="reports"><Link to="/entgra/reports"><Icon type="bar-chart"/>Reports</Link></Menu.Item>

                            <SubMenu className="profile"
                                     title={
                                         <span className="submenu-title-wrapper">
                                     <Icon type="user"/>
                                         Profile
                                     </span>
                                     }
                            >
                                <Logout/>
                            </SubMenu>

                        </Menu>

                    </Header>
                </Layout>
                <Layout>
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
            </div>
        );
    }
}

export default withConfigContext(Dashboard);
