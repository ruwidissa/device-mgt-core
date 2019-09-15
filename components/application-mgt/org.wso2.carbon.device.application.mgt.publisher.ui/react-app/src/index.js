/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
import AddNewCustomApp from "./pages/dashboard/add-new-app/AddNewCustomApp";


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
                path: '/publisher/apps/:deviceType/:appId/add-release',
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
                path: '/publisher/add-new-app/custom-app',
                component: AddNewCustomApp,
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
