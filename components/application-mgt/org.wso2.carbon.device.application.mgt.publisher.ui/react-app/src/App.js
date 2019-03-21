import React from "react";
import "antd/dist/antd.css";
import RouteWithSubRoutes from "./components/RouteWithSubRoutes";
import {
    BrowserRouter as Router,
    Link,
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
                <ul>
                    <li><Link to="/publisher/tacos">Tacos</Link></li>
                    <li><Link to="/publisher/sandwiches">Sandwiches</Link></li>
                </ul>

                {this.routes.map((route) => (
                    <RouteWithSubRoutes key={route.path} {...route} />
                ))}
            </div>
        </Router>

    );
  }
}

export default App;
