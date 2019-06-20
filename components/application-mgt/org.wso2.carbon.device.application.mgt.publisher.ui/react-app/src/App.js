import React from "react";
import "antd/dist/antd.css";
import RouteWithSubRoutes from "./components/RouteWithSubRoutes";
import {
    BrowserRouter as Router,
    Link, Redirect, Switch,
} from 'react-router-dom';

class App extends React.Component {
    routes;

    constructor(props) {
        super(props);
        this.routes = props.routes;
    }

    render() {
        return (
            <Router>
                <div>
                    <Switch>
                        <Redirect exact from="/publisher" to="/publisher/apps"/>
                        {this.routes.map((route) => (
                            <RouteWithSubRoutes key={route.path} {...route} />
                        ))}
                    </Switch>
                </div>
            </Router>

        );
    }
}

export default App;
