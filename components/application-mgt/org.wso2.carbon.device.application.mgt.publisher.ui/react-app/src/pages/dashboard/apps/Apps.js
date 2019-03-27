import React from "react";
import "antd/dist/antd.css";
import {Table, Divider, Tag, Card, PageHeader, Typography, Avatar} from "antd";

const Paragraph = Typography;

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
        breadcrumbName: 'Apps',
    },
];

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
                  color = 'red'
                  break;
              case 'default':
                  color = 'blue'
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


const data = [{
    key: '1',
    icon: 'https://gw.alipayobjects.com/zos/rmsportal/zOsKZmFRdUtvpqCImOVY.png',
    name: 'John Brown',
    platform: 'android',
    type: 'Enterprise',
    status: 'published',
    version: '13.0.0.1',
    updated_at: '27-03-2019 08:27'
},{
    key: '2',
    icon: 'http://aztechbeat.com/wp-content/uploads/2014/04/confide-app-icon.png',
    name: 'Lorem Ipsum',
    platform: 'ios',
    type: 'Enterprise',
    status: 'published',
    version: '2.3.1.2',
    updated_at: '27-03-2019 09:45'
},{
    key: '3',
    icon: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRx2Xx1-hnH16EGZHUlT06nOcfGODPoboA2TXKaBVtODto4lJtK',
    name: 'Lorem Ipsum',
    platform: 'ios',
    type: 'Enterprise',
    status: 'removed',
    version: '4.1.1.0',
    updated_at: '27-03-2019 09:46'
}];

class Apps extends React.Component {
    routes;

    constructor(props) {
        super(props);
        this.routes = props.routes;
    }

    render() {
        return (
            <div>
                <PageHeader
                    breadcrumb={{routes}}
                />
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 780}}>
                    <Card>
                        <Table columns={columns} dataSource={data}/>
                    </Card>
                </div>

            </div>

        );
    }
}

export default Apps;
