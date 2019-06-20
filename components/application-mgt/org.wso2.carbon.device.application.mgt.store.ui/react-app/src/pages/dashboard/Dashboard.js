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
            routes: props.routes,
            selectedKeys : []
        }
    }

    changeSelectedMenuItem = (key) =>{
        this.setState({
            selectedKeys: [key]
        })
    };

    render() {
        const {selectedKeys} = this.state;
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
                            selectedKeys={selectedKeys}
                            style={{lineHeight: '64px'}}
                        >
                            <Menu.Item key="android"><Link to="/store/android"><Icon type="android" theme="filled"/>Android</Link></Menu.Item>
                            <Menu.Item key="ios"><Link to="/store/ios"><Icon type="apple" theme="filled"/>iOS</Link></Menu.Item>
                            <Menu.Item key="web-clip"><Link to="/store/web-clip"><Icon type="upload"/>Web Clips</Link></Menu.Item>
                        </Menu>
                    </Header>
                </Layout>
                <Layout>
                    <Content style={{padding: '0 0'}}>
                        <Switch>
                            <Redirect exact from="/store" to="/store/android"/>
                            {this.state.routes.map((route) => (
                                <RouteWithSubRoutes changeSelectedMenuItem={this.changeSelectedMenuItem} key={route.path} {...route} />
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
