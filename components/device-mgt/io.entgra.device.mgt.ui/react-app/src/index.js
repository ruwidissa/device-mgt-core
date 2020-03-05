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
import Reports from './scenes/Home/scenes/Reports';
import EnrollmentsVsUnenrollmentsReport from './scenes/Home/scenes/Reports/scenes/EnrolmentVsUnenrollments';
import EnrollmentTypeReport from './scenes/Home/scenes/Reports/scenes/EnrollmentType';
import PolicyReport from './scenes/Home/scenes/Reports/scenes/PolicyCompliance';
import DeviceStatusReport from './scenes/Home/scenes/Reports/scenes/DeviceStatus';
import AppNotInstalledDevicesReport from './scenes/Home/scenes/Reports/scenes/AppNotInstalledDevices';
import Geo from './scenes/Home/scenes/Geo';
import EncryptionStatus from './scenes/Home/scenes/Reports/scenes/EncryptionStatus';
import OutdatedOSversionReport from './scenes/Home/scenes/Reports/scenes/OutdatedOSVersion';

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
      // {
      //   path: '/entgra/devices',
      //   component: Devices,
      //   exact: true,
      // },
      // {
      //   path: '/entgra/devices/enroll',
      //   component: DeviceEnroll,
      //   exact: true,
      // },
      // {
      //   path: '/entgra/devices',
      //   component: Devices,
      //   exact: true,
      // },
      // {
      //   path: '/entgra/devices/enroll',
      //   component: DeviceEnroll,
      //   exact: true,
      // },
      {
        path: '/entgra/geo',
        component: Geo,
        exact: true,
      },
      {
        path: '/entgra/reports',
        component: Reports,
        exact: true,
      },
      // {
      //   path: '/entgra/groups',
      //   component: Groups,
      //   exact: true,
      // },
      // {
      //   path: '/entgra/users',
      //   component: Users,
      //   exact: true,
      // },
      // {
      //   path: '/entgra/policies',
      //   component: Policies,
      //   exact: true,
      // },
      // {
      //   path: '/entgra/policy/add',
      //   component: AddNewPolicy,
      //   exact: true,
      // },
      // {
      //   path: '/entgra/roles',
      //   component: Roles,
      //   exact: true,
      // },
      // {
      //   path: '/entgra/devicetypes',
      //   component: DeviceTypes,
      //   exact: true,
      // },
      // {
      //   path: '/entgra/certificates',
      //   component: Certificates,
      //   exact: true,
      // },
      {
        path: '/entgra/reports/enrollments',
        component: EnrollmentsVsUnenrollmentsReport,
        exact: true,
      },
      {
        path: '/entgra/reports/expired-devices',
        component: OutdatedOSversionReport,
        exact: true,
      },
      {
        path: '/entgra/reports/enrollment-type',
        component: EnrollmentTypeReport,
        exact: true,
      },
      {
        path: '/entgra/reports/policy/compliance',
        component: PolicyReport,
        exact: true,
      },
      {
        path: '/entgra/reports/device-status',
        component: DeviceStatusReport,
        exact: true,
      },
      {
        path: '/entgra/reports/app-not-installed',
        component: AppNotInstalledDevicesReport,
        exact: true,
      },
      {
        path: '/entgra/reports/encryption-status',
        component: EncryptionStatus,
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
