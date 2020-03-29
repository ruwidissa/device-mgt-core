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
import './index.css';

const routes = [
  {
    path: '/store/login',
    exact: true,
    component: Login,
  },
  {
    path: '/store',
    exact: false,
    component: Dashboard,
    routes: [
      {
        path: '/store/:deviceType',
        component: Apps,
        exact: true,
      },
      {
        path: '/store/:deviceType/apps/:uuid',
        exact: true,
        component: Release,
      },
    ],
  },
];

ReactDOM.render(<App routes={routes} />, document.getElementById('root'));

// If you want your app e and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
