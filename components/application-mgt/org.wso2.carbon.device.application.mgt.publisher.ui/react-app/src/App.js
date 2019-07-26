import React from "react";
import "antd/dist/antd.less";
import RouteWithSubRoutes from "./components/RouteWithSubRoutes";
import {
    BrowserRouter as Router,
    Redirect, Switch,
} from 'react-router-dom';
import axios from "axios";
import {Layout, Spin, Result} from "antd";
import ConfigContext from "./context/ConfigContext";

const {Content} = Layout;
const loadingView = (
    <Layout>
        <Content style={{
            padding: '0 0',
            paddingTop: 300,
            backgroundColor: '#fff',
            textAlign: 'center'
        }}>
            <Spin tip="Loading..."/>
        </Content>
    </Layout>
);

const errorView = (
    <Result
        style={{
            paddingTop: 200
        }}
        status="500"
        title="Error occurred while loading the configuration"
        subTitle="Please refresh your browser window"
    />
);

class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            loading: true,
            error: false,
            config: {}
        }
    }

    componentDidMount() {
        axios.get(
            window.location.origin + "/publisher/public/conf/config.json",
        ).then(res => {
            console.log(res);
            this.setState({
                loading: false,
                config: res.data
            })
        }).catch((error) => {
            this.setState({
                loading: false,
                error: true
            })
        });
    }

    render() {
        const {loading, error} = this.state;

        const applicationView = (
            <Router>
                <ConfigContext.Provider value={this.state.config}>
                    <div>
                        <Switch>
                            <Redirect exact from="/publisher" to="/publisher/apps"/>
                            {this.props.routes.map((route) => (
                                <RouteWithSubRoutes key={route.path} {...route} />
                            ))}
                        </Switch>
                    </div>
                </ConfigContext.Provider>
            </Router>
        );

        return (
            <div>
                {loading && loadingView}
                {!loading && !error && applicationView}
                {error && errorView}
            </div>
        );
    }
}

export default App;
