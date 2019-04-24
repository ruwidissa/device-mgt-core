import React from "react";
import { Layout, Menu, Breadcrumb } from 'antd';

const { Header, Content, Footer } = Layout;

import styles from './Dashboard.less';
import Logo from "../../../public/images/logo.svg";
import Login from "../Login";
import {renderRoutes} from "react-router-config";
import {NavLink} from "react-router-dom";


class Dashboard extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            route : props.route
        }
        console.log(props);
    }

    render() {
        return (
            <Layout className="layout">
                <Header>
                    <div style={{backgroundImage: "url(" + { Logo} + ")"}} className={styles.logo}/>
                    <Menu
                        theme="light"
                        mode="horizontal"
                        defaultSelectedKeys={['2']}
                        style={{ lineHeight: '64px' }}
                    >
                        <Menu.Item key="1">nav 1</Menu.Item>
                        <Menu.Item key="2">nav 2</Menu.Item>
                        <Menu.Item key="3">nav 3</Menu.Item>
                    </Menu>
                </Header>
                <Content style={{ padding: '0 50px' }}>
                    <Breadcrumb style={{ margin: '16px 0' }}>
                        <Breadcrumb.Item>Home</Breadcrumb.Item>
                        <Breadcrumb.Item>List</Breadcrumb.Item>
                        <Breadcrumb.Item>App</Breadcrumb.Item>
                    </Breadcrumb>
                    <NavLink exact to="/publisher/a" className="nav-link" >
                        Items
                    </NavLink>

                    {/* child routes won't render without this */}
                    {renderRoutes(this.state.route.routes, { someProp: "these extra props are optional" })}
                    <div style={{ background: '#fff', padding: 24, minHeight: 280 }}>Content</div>
                </Content>
                <Footer style={{ textAlign: 'center' }}>
                    ©2019 entgra.io
                </Footer>
            </Layout>
        );
    }
}

export default Dashboard;
