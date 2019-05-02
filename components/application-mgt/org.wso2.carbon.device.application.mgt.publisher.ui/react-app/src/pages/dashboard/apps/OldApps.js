import React from "react";
import "antd/dist/antd.css";
import {Table, Divider, Tag, Card, PageHeader, Typography, Avatar,Input, Button, Icon, Row, Col} from "antd";
import Highlighter from 'react-highlight-words';
import axios from "axios";

const Paragraph = Typography;
const Search = Input.Search;

const routes = [
    {
        path: 'index',
        breadcrumbName: 'Publisher',
    },
    {
        path: 'first',
        breadcrumbName: 'Dashboard',
    },
    {
        path: 'second',
        breadcrumbName: 'OldApps',
    },
];


class OldApps extends React.Component {
    routes;

    state = {
        searchText: '',
    };


    constructor(props) {
        super(props);
        this.routes = props.routes;
        this.state = {
            data: []
        };

        this.loadData = this.loadData.bind(this);
    }

    loadData(){
        const thisComponent = this;
        const request = "method=post&content-type=application/json&payload={}&api-endpoint=/application-mgt-publisher/v1.0/applications";
        axios.post('https://localhost:9443/api/application-mgt-handler/v1.0/invoke', request
        ).then(res => {
            if(res.status === 200){
                console.log(res.status);
                let apps = [];
                res.data['data']['applications'].forEach(function (app) {
                    apps.push({
                        key: app.id,
                        icon: app["applicationReleases"][0]["iconPath"],
                        name: app.name,
                        platform: 'undefined',
                        type: app.type,
                        status: 'undefined',
                        version: 'undefined',
                        updated_at: 'undefined'
                    });
                });
                thisComponent.setState({
                    data : apps
                })
            }

        }).catch(function (error) {
            if(error.response.status === 401){
                window.location.href = 'https://localhost:9443/publisher/login';
            }
        });
    }

    componentDidMount() {
        this.loadData();

    }

    getColumnSearchProps = (dataIndex) => ({
        filterDropdown: ({
                             setSelectedKeys, selectedKeys, confirm, clearFilters,
                         }) => (
            <div style={{ padding: 8 }}>
                <Input
                    ref={node => { this.searchInput = node; }}
                    placeholder={`Search ${dataIndex}`}
                    value={selectedKeys[0]}
                    onChange={e => setSelectedKeys(e.target.value ? [e.target.value] : [])}
                    onPressEnter={() => this.handleSearch(selectedKeys, confirm)}
                    style={{ width: 188, marginBottom: 8, display: 'block' }}
                />
                <Button
                    type="primary"
                    onClick={() => this.handleSearch(selectedKeys, confirm)}
                    icon="search"
                    size="small"
                    style={{ width: 90, marginRight: 8 }}
                >
                    Search
                </Button>
                <Button
                    onClick={() => this.handleReset(clearFilters)}
                    size="small"
                    style={{ width: 90 }}
                >
                    Reset
                </Button>
            </div>
        ),
        filterIcon: filtered => <Icon type="search" style={{ color: filtered ? '#1890ff' : undefined }} />,
        onFilter: (value, record) => record[dataIndex].toString().toLowerCase().includes(value.toLowerCase()),
        onFilterDropdownVisibleChange: (visible) => {
            if (visible) {
                setTimeout(() => this.searchInput.select());
            }
        },
        render: (text) => (
            <Highlighter
                highlightStyle={{ backgroundColor: '#ffc069', padding: 0 }}
                searchWords={[this.state.searchText]}
                autoEscape
                textToHighlight={text.toString()}
            />
        ),
    });

    handleSearch = (selectedKeys, confirm) => {
        confirm();
        this.setState({ searchText: selectedKeys[0] });
    };

    handleReset = (clearFilters) => {
        clearFilters();
        this.setState({ searchText: '' });
    };


    render() {
        const columns = [{
            title: '',
            dataIndex: 'icon',
            key: 'icon',
            render: text => <Avatar size="large" src={text}/>,
        }, {
            title: 'Name',
            dataIndex: 'name',
            key: 'name',
            render: text => <a href="javascript:;">{text}</a>,
            ...this.getColumnSearchProps('name'),
        }, {
            title: 'Platform',
            dataIndex: 'platform',
            key: 'platform',
        }, {
            title: 'Type',
            dataIndex: 'type',
            key: 'type',
        }, {
            title: 'Status',
            key: 'status',
            dataIndex: 'status',
            render: tag => {
                let color;
                switch (tag) {
                    case 'published':
                        color = 'green';
                        break;
                    case 'removed':
                        color = 'red';
                        break;
                    case 'default':
                        color = 'blue';
                }
                return <Tag color={color} key={tag}>{tag.toUpperCase()}</Tag>;
            },
        },  {
            title: 'Published Version',
            dataIndex: 'version',
            key: 'version',
        },  {
            title: 'Last Updated',
            dataIndex: 'updated_at',
            key: 'updated_at',
        },{
            title: 'Action',
            key: 'action',
            render: () => (
                <span>
      <a href="javascript:;">Edit</a>
      <Divider type="vertical" />
      <a href="javascript:;">Manage</a>
    </span>
            ),
        }];

        return (
            <div>
                <PageHeader
                    breadcrumb={{routes}}
                />
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 780}}>

                    <Card>
                        <Row style={{padding:10}}>
                            <Col span={6} offset={18}>
                                <Search
                                    placeholder="search"
                                    onSearch={value => console.log(value)}
                                    style={{ width: 200}}
                                />
                                <Button style={{margin:5}}>Advanced Search</Button>
                            </Col>
                        </Row>
                        <Table columns={columns} dataSource={this.state.data}/>
                    </Card>
                </div>

            </div>

        );
    }
}

export default OldApps;
