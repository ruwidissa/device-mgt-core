import React from "react";
import {PageHeader, Typography, Breadcrumb, Row, Col, Icon} from "antd";
import ManageCategories from "../../../components/manage/categories/ManageCategories";
import ManageTags from "../../../components/manage/categories/ManageTags";
import {Link} from "react-router-dom";

const {Paragraph} = Typography;

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
        breadcrumbName: 'Manage',
    },
];


class Manage extends React.Component {
    routes;

    constructor(props) {
        super(props);
        this.routes = props.routes;

    }

    render() {
        return (
            <div>
                <PageHeader style={{paddingTop: 0}}>
                    <Breadcrumb style={{paddingBottom: 16}}>
                        <Breadcrumb.Item>
                            <Link to="/publisher/apps"><Icon type="home"/> Home</Link>
                        </Breadcrumb.Item>
                        <Breadcrumb.Item>Manage</Breadcrumb.Item>
                    </Breadcrumb>
                    <div className="wrap">
                        <h3>Manage</h3>
                        <Paragraph>Maintain and manage categories and tags here..</Paragraph>
                    </div>
                </PageHeader>
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 780}}>
                    <Row gutter={16}>
                        <Col sm={24} md={12}>
                            <ManageCategories/>
                        </Col>
                        <Col sm={24} md={12}>
                            <ManageTags/>
                        </Col>
                    </Row>
                </div>

            </div>

        );
    }
}

export default Manage;
