// import React from 'react';
import ReactDOM from 'react-dom';
// import './index.css';
import * as serviceWorker from './serviceWorker';
// import { renderRoutes } from "react-router-config";
// import Dashboard from "./pages/dashboard/Dashboard"
// import Login from "./pages/Login";
// import {BrowserRouter} from "react-router-dom";
//
//
// const routes = [
//     {
//         component: App,
//         routes: [
//             {
//                 path: "/publisher",
//                 exact: true,
//                 component: Dashboard,
//                 routes: [
//                     {
//                         path: "/publisher/a",
//                         component: Login
//                     }
//                 ]
//             },
//             {
//                 path: "/publisher/login",
//                 component: Login
//             }
//         ]
//     }
// ];
//
// ReactDOM.render( <BrowserRouter>
//     {/* kick it all off with the root route */}
//     {renderRoutes(routes)}
// </BrowserRouter>, document.getElementById('root'));
//
// // If you want your app to work offline and load faster, you can change
// // unregister() to register() below. Note this comes with some pitfalls.
// // Learn more about service workers: https://bit.ly/CRA-PWA
// serviceWorker.unregister();


import React from 'react';
import {
    BrowserRouter as Router,
    Route,
    Link,
} from 'react-router-dom';
import RouteWithSubRoutes from "./components/RouteWithSubRoutes"
import App from "./App"
import Login from "./pages/Login"

const Sandwiches = () => <h2>Sandwiches</h2>

const Tacos = ({ routes }) => (
    <div>
        <h2>Tacos</h2>
        <ul>
            <li><Link to="/publisher/tacos/bus">Bus</Link></li>
            <li><Link to="/publisher/tacos/cart">Cart</Link></li>
        </ul>

        {routes.map((route) => (
            <RouteWithSubRoutes key={route.path} {...route} />
        ))}
    </div>
)

const Bus = () => <h3>Bus</h3>
const Cart = () => <h3>Cart</h3>

const routes = [
    {
        path: '/publisher/Login',
        component: Login
    },
    {
        path: '/publisher/tacos',
        component: Tacos,
        routes: [
            {
                path: '/publisher/tacos/bus',
                component: Login
            },
            {
                path: '/publisher/tacos/cart',
                component: Cart
            }
        ]
    }
]

// const RouteWithSubRoutes = (route) => (
//     <Route path={route.path} render={(props) => (
//         <route.component {...props} routes={route.routes}/>
//     )}/>
// )

// class App extends React.Component {
//     render() {
//         return (
//             <Router>
//                 <div>
//                     <ul>
//                         <li><Link to="/publisher/tacos">Tacos</Link></li>
//                         <li><Link to="/publisher/sandwiches">Sandwiches</Link></li>
//                     </ul>
//
//                     {routes.map((route) => (
//                         <RouteWithSubRoutes key={route.path} {...route} />
//                     ))}
//                 </div>
//             </Router>
//         )
//     }
// }

ReactDOM.render( <App routes={routes}/>, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
