import React from "react";
import {Layout, Menu, Icon} from 'antd';

const {Header, Content, Footer} = Layout;

import styles from './Dashboard.less';
import Logo from "../../../public/images/logo.svg";
import {Link, NavLink} from "react-router-dom";
import RouteWithSubRoutes from "../../components/RouteWithSubRoutes"
import { Switch, Redirect } from 'react-router'


class Dashboard extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            routes: props.routes
        }
    }

    render() {
        return (
            <Layout className="layout">
                <Header>
                    <div className={styles.logo}>
                        <img src={Logo}/>
                    </div>
                    <Menu
                        theme="light"
                        mode="horizontal"
                        defaultSelectedKeys={['2']}
                        style={{lineHeight: '64px'}}
                    >
                        <Menu.Item key="1"><Link to="/publisher/apps"><Icon type="appstore"/>Apps</Link></Menu.Item>
                        <Menu.Item key="2"><Link to="/publisher/apps"><Icon type="line-chart"/>Apps</Link></Menu.Item>
                        <Menu.Item key="3"><Link to="/publisher/apps/new-app"><Icon type="upload"/>Add New App</Link></Menu.Item>
                    </Menu>
                </Header>
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
        );
    }
}

export default Dashboard;
