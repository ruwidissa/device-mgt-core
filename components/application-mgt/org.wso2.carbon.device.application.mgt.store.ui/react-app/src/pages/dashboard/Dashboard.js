import React from "react";
import {Layout, Menu, Icon} from 'antd';
const {Header, Content, Footer} = Layout;
import {Link} from "react-router-dom";
import RouteWithSubRoutes from "../../components/RouteWithSubRoutes"
import {Switch} from 'react-router'
import "../../App.css";
import {withConfigContext} from "../../context/ConfigContext";

class Dashboard extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            routes: props.routes,
            selectedKeys: []
        };
        this.logo = this.props.context.theme.logo;
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
                    <Header style={{paddingLeft: 0, paddingRight: 0}}>
                        <div className="logo-image">
                            <img alt="logo" src={this.logo}/>
                        </div>
                        <Menu
                            theme="light"
                            mode="horizontal"
                            defaultSelectedKeys={selectedKeys}
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

export default withConfigContext(Dashboard);
