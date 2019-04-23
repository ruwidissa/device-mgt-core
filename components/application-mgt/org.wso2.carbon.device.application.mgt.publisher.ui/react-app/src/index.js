import React from 'react';
import ReactDOM from 'react-dom';
import * as serviceWorker from './serviceWorker';
import App from "./App";
import Login from "./pages/Login";
import Dashboard from "./pages/dashboard/Dashboard";
import Apps from "./pages/dashboard/apps/Apps";
import AddNewApp from "./pages/dashboard/add-new-app/AddNewApp";
import './index.css';
import store from "./js/store/index";
import {Provider} from "react-redux";


const routes = [
    {
        path: '/publisher/Login',
        component: Login
    },
    {
        path: '/publisher/application-mgt',
        component: Dashboard,
        routes: [
            {
                path: '/publisher/application-mgt/apps',
                component: Apps
            },
            {
                path: '/publisher/application-mgt/new-app',
                component: AddNewApp
            }
        ]
    }
];


ReactDOM.render(
    <Provider store={store}>
        <App routes={routes}/>
    </Provider>,
    document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
