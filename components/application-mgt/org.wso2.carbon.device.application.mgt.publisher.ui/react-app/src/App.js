import React from "react";
import "antd/dist/antd.css";
import "./index.css";
import {BrowserRouter, Route, Switch} from 'react-router-dom';
import Dashboard from "./pages/dashboard"
import Login from "./pages/login"

class App extends React.Component {
  render() {
    return (
        <BrowserRouter>
            <Switch>
                <Route path="/publisher" component={Dashboard} exact/>
                <Route path="/publisher/login" component={Login}/>
            </Switch>
        </BrowserRouter>
    );
  }
}

export default App;
