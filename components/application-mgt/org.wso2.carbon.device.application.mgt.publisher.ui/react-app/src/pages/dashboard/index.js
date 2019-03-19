import React from "react";
import { version, Button } from 'antd';
import {Link} from 'react-router-dom';

class Dashboard extends React.Component {
    render() {
        return (
                <div className="App">
                    <p>Currentdddddd antd version: {version}</p>
                    <p>Please fork this codesandbox to reproduce your issue.</p>
                    <p>请 fork 这个链接来重现你碰到的问题。</p>
                    <Link to="/publisher/login">login</Link>
                    <Button type="primary">Hello</Button>
                </div>
        );
    }
}

export default Dashboard;
