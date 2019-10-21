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
import {Tag, message, notification, Table, Typography, Tooltip, Icon, Divider, Button, Modal, Select} from "antd";
import TimeAgo from 'javascript-time-ago'

// Load locale-specific relative date/time formatting rules.
import en from 'javascript-time-ago/locale/en'
import {withConfigContext} from "../../../context/ConfigContext";

const {Text} = Typography;

let config = null;

const columns = [
    {
        title: 'Device',
        dataIndex: 'name',
        width: 100,
    },
    {
        title: 'Owner',
        dataIndex: 'enrolmentInfo',
        key: 'owner',
        render: enrolmentInfo => enrolmentInfo.owner
        // todo add filtering options
    },
    {
        title: 'Ownership',
        dataIndex: 'enrolmentInfo',
        key: 'ownership',
        render: enrolmentInfo => enrolmentInfo.ownership
        // todo add filtering options
    },
    {
        title: 'Status',
        dataIndex: 'enrolmentInfo',
        key: 'status',
        render: (enrolmentInfo) => {
            const status = enrolmentInfo.status.toLowerCase();
            let color = "#f9ca24";
            switch (status) {
                case "active":
                    color = "#badc58";
                    break;
                case "created":
                    color = "#6ab04c";
                    break;
                case "removed":
                    color = "#ff7979";
                    break;
                case "inactive":
                    color = "#f9ca24";
                    break;
                case "blocked":
                    color = "#636e72";
                    break;
            }
            return <Tag color={color}>{status}</Tag>;
        }
        // todo add filtering options
    },
    {
        title: 'Last Updated',
        dataIndex: 'enrolmentInfo',
        key: 'dateOfLastUpdate',
        render: (data) => {
            const {dateOfLastUpdate} = data;
            const timeAgoString = getTimeAgo(dateOfLastUpdate);
            return <Tooltip title={new Date(dateOfLastUpdate).toString()}>{timeAgoString}</Tooltip>;
        }
        // todo add filtering options
    }
];

const getTimeAgo = (time) => {
    const timeAgo = new TimeAgo('en-US');
    return timeAgo.format(time);
};


class InstalledDevicesTable extends React.Component {
    constructor(props) {
        super(props);
        config = this.props.context;
        TimeAgo.addLocale(en);
        this.state = {
            data: [],
            pagination: {},
            loading: false,
            selectedRows: [],
            deviceGroups: [],
            groupModalVisible: false,
            selectedGroupId: []
        };
    }

    componentDidMount() {
        this.fetch();
    }

    //fetch data from api
    fetch = (params = {}) => {
        const config = this.props.context;
        this.setState({loading: true});
        // get current page
        const currentPage = (params.hasOwnProperty("page")) ? params.page : 1;

        const extraParams = {
            offset: 10 * (currentPage - 1), //calculate the offset
            limit: 10,
            requireDeviceInfo: true,
        };

        const encodedExtraParams = Object.keys(extraParams)
            .map(key => key + '=' + extraParams[key]).join('&');

        //send request to the invoker
        axios.get(
            window.location.origin + config.serverConfig.invoker.uri +
            config.serverConfig.invoker.deviceMgt +
            "/devices?" + encodedExtraParams,
        ).then(res => {
            if (res.status === 200) {
                const pagination = {...this.state.pagination};
                this.setState({
                    loading: false,
                    data: res.data.data.devices,
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
                    description:
                        "Error occurred while trying to load devices.",
                });
            }

            this.setState({loading: false});
        });
    };

    render() {
        const {data, pagination, loading, selectedRows} = this.state;
        return (
            <div>
                <div style={{paddingBottom:24}}>
                    <Text>
                        Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque
                        laudantium,
                        totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae
                        dicta sunt explicabo.
                    </Text>
                </div>
                <Table
                    columns={columns}
                    rowKey={record => (record.deviceIdentifier + record.enrolmentInfo.owner + record.enrolmentInfo.ownership)}
                    dataSource={data}
                    pagination={{
                        ...pagination,
                        size: "small",
                        // position: "top",
                        showTotal: (total, range) => `showing ${range[0]}-${range[1]} of ${total} devices`
                        // showQuickJumper: true
                    }}
                    loading={loading}
                    scroll={{x: 1000}}
                />
            </div>
        );
    }
}

export default withConfigContext(InstalledDevicesTable);