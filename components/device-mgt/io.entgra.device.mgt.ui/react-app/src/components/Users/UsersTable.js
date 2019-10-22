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
import axios from "axios";
import {message, notification, Table, Typography, Panel, Collapse, Button, List, Modal, Icon} from "antd";
import TimeAgo from 'javascript-time-ago'

// Load locale-specific relative date/time formatting rules.
import en from 'javascript-time-ago/locale/en'
import {withConfigContext} from "../../context/ConfigContext";
import DeviceTable from "../Devices/DevicesTable";
import UsersDevices from "./UsersDevices";

const {Text} = Typography;

let config = null;
let apiUrl;

class UsersTable extends React.Component {
    constructor(props) {
        super(props);
        config =  this.props.context;
        TimeAgo.addLocale(en);
        this.state = {
            data: [],
            pagination: {},
            loading: false,
            selectedRows: [],
            rolesModalVisible: false,
            rolesData: [],
            user:''
        };
    }

    rowSelection = {
        onChange: (selectedRowKeys, selectedRows) => {
            this.setState({
                selectedRows: selectedRows
            })
        }
    };

    componentDidMount() {
        this.fetchUsers();
    }

    //fetch data from api
    fetchUsers = (params = {}) => {
        const config = this.props.context;
        this.setState({loading: true});

        // get current page
        const currentPage = (params.hasOwnProperty("page")) ? params.page : 1;

        const extraParams = {
            offset: 10 * (currentPage - 1), //calculate the offset
            limit: 10,
        };

        const encodedExtraParams = Object.keys(extraParams)
            .map(key => key + '=' + extraParams[key]).join('&');

        apiUrl = window.location.origin + config.serverConfig.invoker.uri +
            config.serverConfig.invoker.deviceMgt +
            "/users?" + encodedExtraParams;

        //send request to the invokerss
        axios.get(apiUrl).then(res => {
            if (res.status === 200) {
                const pagination = {...this.state.pagination};
                this.setState({
                    loading: false,
                    data: res.data.data.users,
                    pagination,
                });
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                //todo display a popop with error
                message.error('You are not logged in');
                window.location.href = window.location.origin + '/entgra/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:"Error occurred while trying to load users.",
                });
            }

            this.setState({loading: false});
        });
    };

    //fetch data from api
    fetchRoles = (username) => {
        const config = this.props.context;

        this.setState({
            rolesModalVisible: true,
            user: username
        });

        apiUrl = window.location.origin + config.serverConfig.invoker.uri +
            config.serverConfig.invoker.deviceMgt +
            "/users/" + username + "/roles";

        //send request to the invokerss
        axios.get(apiUrl).then(res => {
            if (res.status === 200) {
                this.setState({
                    rolesData: res.data.data.roles,
                });
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                //todo display a popop with error
                message.error('You are not logged in');
                window.location.href = window.location.origin + '/entgra/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:"Error occurred while trying to load roles.",
                });
            }

            this.setState({loading: false});
        });
    };

    handleTableChange = (pagination, filters, sorter) => {
        const pager = {...this.state.pagination};
        pager.current = pagination.current;
        this.setState({
            pagination: pager,
        });
        this.fetch({
            results: pagination.pageSize,
            page: pagination.current,
            sortField: sorter.field,
            sortOrder: sorter.order,
            ...filters,
        });
    };

    handleOk = e => {
        this.setState({
            rolesModalVisible: false,
        });
    };

    handleCancel = e => {
        this.setState({
            rolesModalVisible: false,
        });
    };

    render() {

        const {data, pagination, loading, selectedRows} = this.state;
        const { Panel } = Collapse;
        const columns = [
            {
                title: 'User Name',
                dataIndex: 'username',
                width: 150,
                key: "username",
            },
            {
                title: 'First Name',
                width: 150,
                dataIndex: 'firstname',
                key: 'firstname',
            },
            {
                title: 'Last Name',
                width: 150,
                dataIndex: 'lastname',
                key: 'lastname',
            },
            {
                title: 'Email',
                width: 100,
                dataIndex: 'emailAddress',
                key: 'emailAddress',
            },
            {
                title: '',
                dataIndex: 'username',
                key: 'roles',
                render: (username) =>
                    <Button
                        type="link"
                        size={"default"}
                        icon="info-circle"
                        onClick={() => this.fetchRoles(username)}>Info</Button>
            }
        ];
        return (
            <div>
                <div>
                    <Table
                        columns={columns}
                        rowKey={record => (record.username)}
                        dataSource={data}
                        pagination={{
                            ...pagination,
                            size: "small",
                            // position: "top",
                            showTotal: (total, range) => `showing ${range[0]}-${range[1]} of ${total} groups`
                            // showQuickJumper: true
                        }}
                        loading={loading}
                        onChange={this.handleTableChange}
                        rowSelection={this.rowSelection}
                        scroll={{x: 1000}}
                    />
                </div>
                <div>
                    <Modal
                        width="900px"
                        title="Info"
                        visible={this.state.rolesModalVisible}
                        onOk={this.handleOk}
                        onCancel={this.handleCancel}
                    >
                        <Collapse>
                            <Panel header="User Roles" key="1">
                                <List
                                    size="small"
                                    bordered
                                    dataSource={this.state.rolesData}
                                    renderItem={item => <List.Item>{item}</List.Item>}
                                />
                            </Panel>
                            <Panel header="Enrolled Devices" key="2">
                                <UsersDevices user={this.state.user}/>
                            </Panel>
                        </Collapse>
                    </Modal>
                </div>
            </div>
        );
    }
}

export default withConfigContext(UsersTable);