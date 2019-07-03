import React from "react";
import {Avatar, Card, Col, Row, Table, Typography, Tag, Icon, message} from "antd";
import axios from "axios";
import config from "../../../../public/conf/config.json";

const columns = [
    {
        title: '',
        dataIndex: 'name',
        render: (name, row) => {
            return (
                <div>
                    <Avatar shape="square" size="large"
                            style={{
                                marginRight: 20,
                                borderRadius: "28%",
                                border: "1px solid #ddd"
                            }}
                            src={row.applicationReleases[0].iconPath}
                    />
                    {name}
                </div>);
        }
    },
    {
        title: 'Categories',
        dataIndex: 'categories',
        render: categories => (
            <span>
                {categories.map(category => {
                    return (
                        <Tag color="blue" key={category}>
                            {category}
                        </Tag>
                    );
                })}
            </span>
        )
    },
    {
        title: 'Platform',
        dataIndex: 'deviceType',
        render: platform => {
            const defaultPlatformIcons = config.defaultPlatformIcons;
            let icon = defaultPlatformIcons.default.icon;
            let color = defaultPlatformIcons.default.color;
            if (defaultPlatformIcons.hasOwnProperty(platform)) {
                icon = defaultPlatformIcons[platform].icon;
                color = defaultPlatformIcons[platform].color;
            }
            return (<span style={{fontSize: 20, color: color, textAlign: "center"}}><Icon type={icon}
                                                                                          theme="filled"/></span>)
        }
    },
    {
        title: 'Type',
        dataIndex: 'type'
    },
    {
        title: 'Subscription',
        dataIndex: 'subMethod'
    },
];

class AppsTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            pagination: {},
            apps: [],
            filters: {}
        };
    }

    componentDidMount() {
        const {filters} =this.props;
        this.setState({
            filters
        });
        this.fetch(filters);

    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        const {filters} =this.props;
        if (prevProps.filters !== this.props.filters) {
            console.log("d",this.props.filters);
            this.setState({
                filters
            });
            this.fetch(filters);
        }
    }

    handleTableChange = (pagination, filters, sorter) => {
        const pager = {...this.state.pagination};
        pager.current = pagination.current;

        this.setState({
            pagination: pager,
        });
        this.fetch(this.state.filters,{
            results: pagination.pageSize,
            page: pagination.current,
            sortField: sorter.field,
            sortOrder: sorter.order,
            ...filters,
        });
    };

    fetch = (filters,params = {}) => {
        this.setState({loading: true});

        if(!params.hasOwnProperty("page")){
           params.page = 1;
        }

        const data = {
            offset: 10 * (params.page - 1),
            limit: 10,
            ...filters
        };
        console.log("f", data);

       axios.post(
            config.serverConfig.protocol + "://"+config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri +config.serverConfig.invoker.publisher+"/applications",
            data,

        ).then(res => {
            if (res.status === 200) {
                const data = res.data.data;
                let apps = [];

                if (res.data.data.hasOwnProperty("applications")) {
                    apps = data.applications;
                }
                const pagination = {...this.state.pagination};
                // Read total count from server
                // pagination.total = data.totalCount;
                pagination.total = data.pagination.count;
                this.setState({
                    loading: false,
                    apps: apps,
                    pagination,
                });

            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                message.error('You are not logged in');
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort+'/publisher/login';
            } else {
                message.error('Something went wrong... :(');
            }

            this.setState({loading: false});
        });
    };

    render() {
        return (

            <Table
                rowKey={record => record.id}
                dataSource={this.state.apps}
                columns={columns}
                pagination={this.state.pagination}
                onChange={this.handleTableChange}
                onRow={(record, rowIndex) => {
                    return {
                        onClick: event => {
                            this.props.showDrawer(record);
                        },
                    };
                }}
            />

        );
    }
}

export default AppsTable;