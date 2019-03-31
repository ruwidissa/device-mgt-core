import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import * as serviceWorker from './serviceWorker';
import { renderRoutes } from "react-router-config";
import Dashboard from "./pages/dashboard/Dashboard"
import Login from "./pages/Login";
import {BrowserRouter} from "react-router-dom";


const routes = [
    {
        component: App,
        routes: [
            {
                path: "/publisher",
                exact: true,
                component: Dashboard,
                routes: [
                    {
                        path: "/publisher/a",
                        component: Login
                    }
                ]
            },
            {
                path: "/publisher/login",
                component: Login
            }
        ]
    }
];

ReactDOM.render( <BrowserRouter>
    {/* kick it all off with the root route */}
    {renderRoutes(routes)}
</BrowserRouter>, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
