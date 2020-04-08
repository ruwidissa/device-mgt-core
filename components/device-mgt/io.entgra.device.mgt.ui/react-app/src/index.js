/*
 * Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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
import Home from './scenes/Home';
import './index.css';
import Geo from './scenes/Home/scenes/Geo';
import Notifications from './scenes/Home/scenes/Notifications';
import DeviceEnroll from './scenes/Home/scenes/Devices/scenes/Enroll';
import Groups from './scenes/Home/scenes/Groups';
import Users from './scenes/Home/scenes/Users';
import Policies from './scenes/Home/scenes/Policies';
import AddNewPolicy from './scenes/Home/scenes/Policies/scenes/AddNewPolicy';
import Roles from './scenes/Home/scenes/Roles';
import DeviceTypes from './scenes/Home/scenes/DeviceTypes';
import Certificates from './scenes/Home/scenes/Configurations/scenes/Certificates';
import Devices from './scenes/Home/scenes/Devices';
import ViewPolicy from './scenes/Home/scenes/Policies/scenes/ViewPolicy';
import EditSelectedPolicy from './scenes/Home/scenes/Policies/scenes/EditSelectedPolicy';
import DeviceLocations from './scenes/Home/scenes/DeviceLocations';

const routes = [
  {
    path: '/entgra/login',
    exact: true,
    component: Login,
  },
  {
    path: '/entgra',
    exact: false,
    component: Home,
    routes: [
      {
        path: '/entgra/devices',
        component: Devices,
        exact: true,
      },
      {
        path: '/entgra/devices/enroll',
        component: DeviceEnroll,
        exact: true,
      },
      {
        path: '/entgra/geo/history/:deviceType/:deviceIdentifier',
        component: Geo,
        exact: true,
      },
      {
        path: '/entgra/groups',
        component: Groups,
        exact: true,
      },
      {
        path: '/entgra/users',
        component: Users,
        exact: true,
      },
      {
        path: '/entgra/policies',
        component: Policies,
        exact: true,
      },
      {
        path: '/entgra/policy/add',
        component: AddNewPolicy,
        exact: true,
      },
      {
        path: '/entgra/policy/view/:policyId',
        component: ViewPolicy,
        exact: true,
      },
      {
        path: '/entgra/policy/edit/:policyId',
        component: EditSelectedPolicy,
        exact: true,
      },
      {
        path: '/entgra/roles',
        component: Roles,
        exact: true,
      },
      {
        path: '/entgra/devicetypes',
        component: DeviceTypes,
        exact: true,
      },
      {
        path: '/entgra/configurations/certificates',
        component: Certificates,
        exact: true,
      },
      {
        path: '/entgra/notifications',
        component: Notifications,
        exact: true,
      },
      {
        path: '/entgra/device-locations',
        component: DeviceLocations,
        exact: true,
      },
    ],
  },
];

ReactDOM.render(<App routes={routes} />, document.getElementById('root'));

// If you want your app e and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
