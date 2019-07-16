import React from "react";
import axios from "axios";
import config from "../../../../../public/conf/config.json";
import {Button, message, notification, Table, Typography} from "antd";
import TimeAgo from 'javascript-time-ago'

// Load locale-specific relative date/time formatting rules.
import en from 'javascript-time-ago/locale/en'
const {Text} = Typography;
const columns = [
    {
        title: 'Device',
        dataIndex: 'name',
        fixed: 'left',
        width: 100,
    },
    {
        title: 'Modal',
        dataIndex: 'deviceInfo',
        key:'modal',
        render: deviceInfo => `${deviceInfo.vendor} ${deviceInfo.deviceModel}`
        // todo add filtering options
    },
    {
        title: 'Owner',
        dataIndex: 'enrolmentInfo',
        key: 'owner',
        render: enrolmentInfo => enrolmentInfo.owner
        // todo add filtering options
    },
    {
        title: 'Last Updated',
        dataIndex: 'enrolmentInfo',
        key: 'dateOfLastUpdate',
        render: (data) => {
            return (getTimeAgo(data.dateOfLastUpdate));
        }
        // todo add filtering options
    },
    {
        title: 'Status',
        dataIndex: 'enrolmentInfo',
        key: 'status',
        render: enrolmentInfo => enrolmentInfo.status
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
        title: 'OS Version',
        dataIndex: 'deviceInfo',
        key:'osVersion',
        render: deviceInfo => deviceInfo.osVersion
        // todo add filtering options
    },
    {
        title: 'IMEI',
        dataIndex: 'properties',
        key:'imei',
        render: properties => {
            let imei = "not-found";
            for (let i = 0; i < properties.length; i++) {
                if(properties[i].name==="IMEI"){
                    imei = properties[i].value;
                }
            }
            return imei;
        }
        // todo add filtering options
    },
];

const getTimeAgo = (time) => {
    const timeAgo = new TimeAgo('en-US');
    return timeAgo.format(time);
};


class DeviceInstall extends React.Component {
    constructor(props) {
        super(props);
        TimeAgo.addLocale(en);
        this.state = {
            data: [],
            pagination: {},
            loading: false,
            selectedRows:[]
        };
    }

    rowSelection = {
        onChange: (selectedRowKeys, selectedRows) => {
            // console.log(`selectedRowKeys: ${selectedRowKeys}`, 'selectedRows: ', selectedRows);
            this.setState({
                selectedRows: selectedRows
            })
        },
        getCheckboxProps: record => ({
            disabled: record.name === 'Disabled User', // Column configuration not to be checked
            name: record.name,
        }),
    };

    componentDidMount() {
        this.fetch();
    }

    //fetch data from api
    fetch = (params = {}) => {
        this.setState({loading: true});

        // get current page
        const currentPage = (params.hasOwnProperty("page")) ? params.page : 1;

        const extraParams = {
            offset: 10 * (currentPage - 1), //calculate the offset
            limit: 10,
            status: "ACTIVE",
            requireDeviceInfo: true
        };

        // note: encode with '%26' not '&'
        const encodedExtraParams = Object.keys(extraParams).map(key => key + '=' + extraParams[key]).join('%26');

        const parameters = {
            method: "get",
            'content-type': "application/json",
            payload: "{}",
            'api-endpoint': "/device-mgt/v1.0/devices?" + encodedExtraParams
        };

        //url-encode parameters
        const request = Object.keys(parameters).map(key => key + '=' + parameters[key]).join('&');

        //send request to the invoker
        axios.get(
            config.serverConfig.protocol + "://"+config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt+"devices?" + encodedExtraParams,

        ).then(res => {
            if (res.status === 200) {
                const pagination = {...this.state.pagination};
                this.setState({
                    loading: false,
                    data: res.data.data.devices,
                    pagination,
                });

            }

        }).catch((error) => { console.log(error);
            if (error.hasOwnProperty("status") && error.response.status === 401) {
                //todo display a popop with error
                message.error('You are not logged in');
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/store/login';
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

    install = () => {
        const {selectedRows} = this.state;
        const payload = [];
        selectedRows.map(device => {
            payload.push({
                deviceIdentifier: device.deviceIdentifier,
                type: device.type
            });
        });
        this.props.onInstall("device", payload);
    };


    render() {
        const {data,pagination,loading,selectedRows} = this.state;
        return (
            <div>
                <Text>Start installing the application for one or more users by entering the corresponding user name. Select install to automatically start downloading the application for the respective user/users. </Text>
                <Table
                    style={{paddingTop:20}}
                    columns={columns}
                    rowKey={record => record.deviceIdentifier}
                    dataSource={data}
                    pagination={{
                        ...pagination,
                        size: "small",
                        // position: "top",
                        showTotal: (total, range) => `showing ${range[0]}-${range[1]} of ${total} devices`
                        // showQuickJumper: true
                    }}
                    loading={loading}
                    onChange={this.handleTableChange}
                    rowSelection={this.rowSelection}
                    scroll={{x: 1000}}
                />
                <div style={{paddingTop: 10, textAlign: "right"}}>
                    <Button disabled={selectedRows.length===0} htmlType="button" type="primary" onClick={this.install}>Install</Button>
                </div>
            </div>
        );
    }
}

export default DeviceInstall;