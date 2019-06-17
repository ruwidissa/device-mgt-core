import React from 'react';
import ReactDOM from 'react-dom';
import * as serviceWorker from './serviceWorker';
import App from "./App";
import Login from "./pages/Login";
import Dashboard from "./pages/dashboard/Dashboard";
import Apps from "./pages/dashboard/apps/Apps";
import Release from "./pages/dashboard/apps/release/Release";
import './index.css';

const routes = [
    {
        path: '/store/login',
        exact: true,
        component: Login
    },
    {
        path: '/store',
        exact: false,
        component: Dashboard,
        routes: [
            {
                path: '/store/:deviceType',
                component: Apps,
                exact: true
            },
            {
                path: '/store/:deviceType/apps/:uuid',
                exact: true,
                component: Release
            }
        ]
    }
];


ReactDOM.render(
    <App routes={routes}/>,
    document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
