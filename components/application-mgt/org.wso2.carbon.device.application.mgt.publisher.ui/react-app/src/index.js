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
import * as serviceWorker from './services/serviceWorkers/serviceWorker';
import App from './App';
import Login from './scenes/Login';
import Dashboard from './scenes/Home';
import Apps from './scenes/Home/scenes/Apps';
import Release from './scenes/Home/scenes/Apps/scenes/Release';
import AddNewEnterpriseApp from './scenes/Home/scenes/AddNewApp/scenes/Enterprise';
import Mange from './scenes/Home/scenes/Manage';
import './index.css';
import AddNewPublicApp from './scenes/Home/scenes/AddNewApp/scenes/Public';
import AddNewWebClip from './scenes/Home/scenes/AddNewApp/scenes/WebClip';
import AddNewRelease from './scenes/Home/scenes/AddNewRelease';
import AddNewCustomApp from './scenes/Home/scenes/AddNewApp/scenes/Custom';
import ManageAndroidEnterprise from './scenes/Home/scenes/Manage/scenes/AndroidEnterprise';
import Page from './scenes/Home/scenes/Manage/scenes/AndroidEnterprise/scenes/Page';

const routes = [
  {
    path: '/publisher/login',
    exact: true,
    component: Login,
  },
  {
    path: '/publisher/',
    exact: false,
    component: Dashboard,
    routes: [
      {
        path: '/publisher/apps',
        component: Apps,
        exact: true,
      },
      {
        path: '/publisher/apps/releases/:uuid',
        exact: true,
        component: Release,
      },
      {
        path: '/publisher/apps/:deviceType/:appId/add-release',
        component: AddNewRelease,
        exact: true,
      },
      {
        path: '/publisher/add-new-app/enterprise',
        component: AddNewEnterpriseApp,
        exact: true,
      },
      {
        path: '/publisher/add-new-app/public',
        component: AddNewPublicApp,
        exact: true,
      },
      {
        path: '/publisher/add-new-app/web-clip',
        component: AddNewWebClip,
        exact: true,
      },
      {
        path: '/publisher/add-new-app/custom-app',
        component: AddNewCustomApp,
        exact: true,
      },
      {
        path: '/publisher/manage',
        component: Mange,
        exact: true,
      },
      {
        path: '/publisher/manage/android-enterprise',
        component: ManageAndroidEnterprise,
        exact: true,
      },
      {
        path: '/publisher/manage/android-enterprise/pages/:pageName/:pageId',
        component: Page,
        exact: true,
      },
    ],
  },
];

ReactDOM.render(<App routes={routes} />, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
