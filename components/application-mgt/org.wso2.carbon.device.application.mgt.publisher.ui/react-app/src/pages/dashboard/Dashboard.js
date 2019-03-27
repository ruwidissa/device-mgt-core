import React from "react";
import { Layout, Menu, Icon } from 'antd';

const { Header, Content, Footer } = Layout;

import styles from './Dashboard.less';
import Logo from "../../../public/images/logo.svg";
import Login from "../Login";
import {renderRoutes} from "react-router-config";
import {Link, NavLink} from "react-router-dom";
import RouteWithSubRoutes from "../../components/RouteWithSubRoutes"


class Dashboard extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            routes : props.routes
        }
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
                        <Menu.Item key="1"><Link to="apps"><Icon type="appstore" />Apps</Link></Menu.Item>
                        <Menu.Item key="2"><Link to="apps"><Icon type="line-chart" />Apps</Link></Menu.Item>
                        <Menu.Item key="3"><Link to="new-app"><Icon type="upload" />Add New App</Link></Menu.Item>
                    </Menu>
                </Header>
                <Content style={{ padding: '0 0' }}>
                    {this.state.routes.map((route) => (
                        <RouteWithSubRoutes key={route.path} {...route} />
                    ))}

                </Content>
                <Footer style={{ textAlign: 'center' }}>
                    ©2019 entgra.io
                </Footer>
            </Layout>
        );
    }
}

export default Dashboard;
