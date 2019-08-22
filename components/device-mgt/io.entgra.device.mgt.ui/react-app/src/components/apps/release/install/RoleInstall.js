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

import React from "react";
import {Typography, Select, Spin, message, notification, Button} from "antd";
import debounce from 'lodash.debounce';
import axios from "axios";
import {withConfigContext} from "../../../../context/ConfigContext";

const {Text} = Typography;
const {Option} = Select;


class RoleInstall extends React.Component {

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
        const config = this.props.context;
        this.lastFetchId += 1;
        const fetchId = this.lastFetchId;
        this.setState({data: [], fetching: true});

        axios.get(
            window.location.origin+ config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt+"/roles?filter=" + value,

        ).then(res => {
            if (res.status === 200) {
                if (fetchId !== this.lastFetchId) {
                    // for fetch callback order
                    return;
                }

                const data = res.data.data.roles.map(role => ({
                    text: role,
                    value: role,
                }));

                this.setState({data, fetching: false});
            }

        }).catch((error) => { console.log(error);
            if (error.hasOwnProperty("status") && error.response.status === 401) {
                message.error('You are not logged in');
                window.location.href = window.location.origin+'/store/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "Error occurred while trying to load roles.",
                });
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

    install = () =>{
        const {value} = this.state;
        const data = [];
        value.map(val=>{
            data.push(val.key);
        });
        this.props.onInstall("role",data);
    };

    render() {

        const {fetching, data, value} = this.state;

        return (
            <div>
                <Text>Start installing the application for one or more roles by entering the corresponding role name. Select install to automatically start downloading the application for the respective user role/roles.</Text>
                <br/>
                <br/>
                <Select
                    mode="multiple"
                    labelInValue
                    value={value}
                    placeholder="Search roles"
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
                <div style={{paddingTop:10, textAlign:"right"}}>
                    <Button disabled={value.length===0} htmlType="button" type="primary" onClick={this.install}>Install</Button>
                </div>
            </div>
        );
    }
}

export default withConfigContext(RoleInstall);