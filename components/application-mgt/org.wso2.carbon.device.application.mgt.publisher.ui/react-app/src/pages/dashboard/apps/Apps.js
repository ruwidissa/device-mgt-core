import React from "react";
import "antd/dist/antd.css";
import {PageHeader, Typography,Input, Button, Row, Col} from "antd";

import AppCard from "../../../components/AppCard";

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

                    <Row gutter={16}>
                        <Col xs={24} sm={12} md={6} lg={6}>
                            <AppCard title="Forest" platform="android" type="ENTERPRISE" subType="FREE" description="This function is a valid React component because it accepts a single “props” (which stands for properties) object"/>
                        </Col>
                        <Col xs={24} sm={12} md={6} lg={6}>
                            <AppCard title="hi" platform="ios" type="ENTERPRISE" subType="FREE" description="This function is a valid React component because it accepts a single “props” (which stands for properties) object"/>
                        </Col>
                        <Col xs={24} sm={12} md={6} lg={6}>
                            <AppCard title="Unknown App" type="ENTERPRISE" subType="FREE" description="This function is a valid React component because it accepts a single “props” (which stands for properties) object"/>
                        </Col>
                    </Row>
                </div>

            </div>

        );
    }
}

export default Apps;
