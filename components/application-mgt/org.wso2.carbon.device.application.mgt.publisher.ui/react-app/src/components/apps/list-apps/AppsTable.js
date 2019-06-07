import React from "react";
import {Avatar, Card, Col, Row, Table, Typography, Tag, Icon, message} from "antd";
import {connect} from "react-redux";
import {getApps} from "../../../js/actions";
import axios from "axios";
import config from "../../../../public/conf/config.json";

const {Title} = Typography;

// connecting state.apps with the component
const mapStateToProps = state => {
    return {apps: state.apps}
};

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
        dataIndex: 'appCategories',
        render: appCategories => (
            <span>
                {appCategories.map(category => {
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
        dataIndex: 'subType'
    },
];

class ConnectedAppsTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            pagination: {
                total: 100
            },
            apps: []
        };
    }

    componentDidMount() {
        this.fetch();
    }

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

    fetch = (params = {}) => {
        this.setState({loading: true});

        const extraParams = {
            offset: 10 * (params.page - 1),
            limit: 10
        };
        // note: encode with '%26' not '&'
        const encodedExtraParams = Object.keys(extraParams).map(key => key + '=' + extraParams[key]).join('%26');
        const parameters = {
            method: "post",
            'content-type': "application/json",
            payload: JSON.stringify({}),
            'api-endpoint': "/application-mgt-publisher/v1.0/applications?" + encodedExtraParams
        };

        const request = Object.keys(parameters).map(key => key + '=' + parameters[key]).join('&');
        console.log(request);
        axios.post('https://' + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
        ).then(res => {
            if (res.status === 200) {
                let apps = [];

                if (res.data.data.hasOwnProperty("applications")) {
                    apps = res.data.data.applications;
                }
                const pagination = {...this.state.pagination};
                // Read total count from server
                // pagination.total = data.totalCount;
                pagination.total = 200;
                this.setState({
                    loading: false,
                    apps: apps,
                    pagination,
                });

            }

        }).catch((error) => {
            if (error.response.status === 401) {
                message.error('You are not logged in');
                window.location.href = 'https://localhost:9443/publisher/login';
            } else {
                message.error('Something went wrong... :(');
            }

            this.setState({loading: false});
        });
    };

    render() {
        console.log("rendered");
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

const AppsTable = connect(mapStateToProps, {getApps})(ConnectedAppsTable);

export default AppsTable;