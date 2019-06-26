import React from "react";
import {Typography, Select, Spin, message, notification, Button} from "antd";
import debounce from 'lodash.debounce';
import axios from "axios";
import config from "../../../../../public/conf/config.json";

const {Text} = Typography;
const {Option} = Select;


class UserInstall extends React.Component {

    constructor(props) {
        super(props);
        this.lastFetchId = 0;
        this.fetchUser = debounce(this.fetchUser, 800);
    }

    state = {
        data: [],
        value: [],
        fetching: false,
    };

    fetchUser = value => {
        this.lastFetchId += 1;
        const fetchId = this.lastFetchId;
        this.setState({data: [], fetching: true});


        //send request to the invoker
        axios.get(
            config.serverConfig.protocol + "://"+config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt+"/users/search?username=" + value,
            {
                headers: { 'X-Platform': config.serverConfig.platform }
            }
        ).then(res => {
            if (res.status === 200) {
                if (fetchId !== this.lastFetchId) {
                    // for fetch callback order
                    return;
                }

                const data = res.data.data.users.map(user => ({
                    text: user.username,
                    value: user.username,
                }));

                this.setState({data, fetching: false});
            }

        }).catch((error) => {
            if (error.response.hasOwnProperty(status) && error.response.status === 401) {
                message.error('You are not logged in');
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/store/login';
            } else {
                message.error('Something went wrong... :(');
            }

            this.setState({fetching: false});
        });
    };

    handleChange = value => {
        this.setState({
            value,
            data: [],
            fetching: false,
        });
    };

    install = () => {
        const {value} = this.state;
        const data = [];
        value.map(val => {
            data.push(val.key);
        });
        this.props.onInstall("user", data);
    };

    render() {
        const {fetching, data, value} = this.state;

        return (
            <div>
                <Text>Start installing the application for one or more users by entering the corresponding user name. Select install to automatically start downloading the application for the respective user/users. </Text>
                <p>Select users</p>
                <Select
                    mode="multiple"
                    labelInValue
                    value={value}
                    placeholder="Enter the username"
                    notFoundContent={fetching ? <Spin size="small"/> : null}
                    filterOption={false}
                    onSearch={this.fetchUser}
                    onChange={this.handleChange}
                    style={{width: '100%'}}
                >
                    {data.map(d => (
                        <Option key={d.value}>{d.text}</Option>
                    ))}
                </Select>
                <div style={{paddingTop: 10, textAlign: "right"}}>
                    <Button disabled={value.length===0} htmlType="button" type="primary" onClick={this.install}>Install</Button>
                </div>
            </div>
        );
    }
}

export default UserInstall;