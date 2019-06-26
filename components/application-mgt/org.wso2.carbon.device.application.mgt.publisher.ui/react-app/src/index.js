import React from 'react';
import ReactDOM from 'react-dom';
import * as serviceWorker from './serviceWorker';
import App from "./App";
import Login from "./pages/Login";
import Dashboard from "./pages/dashboard/Dashboard";
import Apps from "./pages/dashboard/apps/Apps";
import Release from "./pages/dashboard/apps/release/Release";
import AddNewEnterpriseApp from "./pages/dashboard/add-new-app/AddNewEnterpriseApp";
import Mange from "./pages/dashboard/manage/Manage";
import './index.css';
import AddNewPublicApp from "./pages/dashboard/add-new-app/AddNewPublicApp";
import AddNewWebClip from "./pages/dashboard/add-new-app/AddNewWebClip";
import AddNewRelease from "./pages/dashboard/add-new-release/AddNewRelease";


const routes = [
    {
        path: '/publisher/login',
        exact: true,
        component: Login
    },
    {
        path: '/publisher/',
        exact: false,
        component: Dashboard,
        routes: [
            {
                path: '/publisher/apps',
                component: Apps,
                exact: true
            },
            {
                path: '/publisher/apps/releases/:uuid',
                exact: true,
                component: Release
            },
            {
                path: '/publisher/apps/:appId/add-release',
                component: AddNewRelease,
                exact: true
            },
            {
                path: '/publisher/add-new-app/enterprise',
                component: AddNewEnterpriseApp,
                exact: true
            },
            {
                path: '/publisher/add-new-app/public',
                component: AddNewPublicApp,
                exact: true
            },
            {
                path: '/publisher/add-new-app/web-clip',
                component: AddNewWebClip,
                exact: true
            },
            {
                path: '/publisher/manage',
                component: Mange,
                exact: true
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
