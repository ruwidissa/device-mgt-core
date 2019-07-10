import React from "react";
import {Layout, Menu, Icon} from 'antd';
import Logo from "../../../public/images/logo.svg";
import {Switch, Link} from "react-router-dom";
import RouteWithSubRoutes from "../../components/RouteWithSubRoutes"
import {Redirect} from 'react-router'
import "../../App.css";

const {Header, Content, Footer} = Layout;
const {SubMenu} = Menu;

class Dashboard extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            routes: props.routes
        };
    }

    render() {
        return (
            <div>
                <Layout className="layout">
                    <Header style={{paddingLeft: 0, paddingRight: 0}}>
                        <div style={{width: 120}} className="logo-image">
                            <img alt="logo" src={Logo}/>
                        </div>
                        <Menu
                            theme="light"
                            mode="horizontal"
                            defaultSelectedKeys={['1']}
                            style={{lineHeight: '64px'}}
                        >
                            <Menu.Item key="1"><Link to="/publisher/apps"><Icon type="appstore"/>Apps</Link></Menu.Item>
                            <SubMenu
                                title={
                                    <span className="submenu-title-wrapper">
                                     <Icon type="plus"/>
                                         Add New App
                                     </span>
                                }
                            >
                                <Menu.Item key="setting:1"><Link to="/publisher/add-new-app/public">Public
                                    APP</Link></Menu.Item>
                                <Menu.Item key="setting:2"><Link to="/publisher/add-new-app/enterprise">Enterprise
                                    APP</Link></Menu.Item>
                                <Menu.Item key="setting:3"><Link to="/publisher/add-new-app/web-clip">Web
                                    Clip</Link></Menu.Item>
                            </SubMenu>
                            <Menu.Item key="2"><Link to="/publisher/manage"><Icon
                                type="control"/>Manage</Link></Menu.Item>
                        </Menu>
                    </Header>
                </Layout>
                <Layout>
                    <Content style={{padding: '0 0'}}>
                        <Switch>
                            <Redirect exact from="/publisher" to="/publisher/apps"/>
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

export default Dashboard;
