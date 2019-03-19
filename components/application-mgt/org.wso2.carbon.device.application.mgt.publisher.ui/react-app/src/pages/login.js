import React from "react";
import { version, Button } from 'antd';

class Login extends React.Component {
    render() {
        return (
                <div className="App">
                    <p>Current antd version: {version}</p>
                    <p>Please fork this codesandbox to reproduce your issue.</p>
                    <p>请 fork 这个链接来重现你碰到的问题。</p>
                    <Button type="primary">Hello</Button>
                </div>
        );
    }
}

export default Login;
