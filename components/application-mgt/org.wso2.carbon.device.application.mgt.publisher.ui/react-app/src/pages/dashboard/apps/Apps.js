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
        breadcrumbName: 'Apps',
    },
];


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
                    </Card>
                </div>

            </div>

        );
    }
}

export default Apps;
