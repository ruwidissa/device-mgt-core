import React from "react";
import {Layout, Menu, Icon} from 'antd';

const {Header, Content, Footer} = Layout;

import Logo from "../../../public/images/logo.svg";
import {Link, NavLink} from "react-router-dom";
import RouteWithSubRoutes from "../../components/RouteWithSubRoutes"
import {Switch, Redirect} from 'react-router'
import "../../App.css";

class Dashboard extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            routes: props.routes
        }
    }

    render() {
        return (
            <div>
                <Layout className="layout">
                    <Header>
                        <div className="logo">
                            <img src={Logo}/>
                        </div>
                        <Menu
                            theme="light"
                            mode="horizontal"
                            defaultSelectedKeys={['1']}
                            style={{lineHeight: '64px'}}
                        >
                            <Menu.Item key="1"><Link to="/store/android"><Icon type="android" theme="filled"/>Android</Link></Menu.Item>
                            <Menu.Item key="2"><Link to="/store/apps"><Icon type="apple" theme="filled"/>iOS</Link></Menu.Item>
                            <Menu.Item key="3"><Link to="/store/apps/new-app"><Icon type="upload"/>Web Clips</Link></Menu.Item>
                        </Menu>
                    </Header>
                </Layout>
                <Layout>
                    <Content style={{padding: '0 0'}}>
                        <Switch>
                            <Redirect exact from="/store" to="/store/android"/>
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
