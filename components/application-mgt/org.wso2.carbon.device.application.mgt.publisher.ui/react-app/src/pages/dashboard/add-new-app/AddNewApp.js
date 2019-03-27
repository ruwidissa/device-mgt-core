import React from "react";
import "antd/dist/antd.css";
import {PageHeader, Breadcrumb, Typography} from "antd";

const Paragraph = Typography;

const routes = [
    {
        path: 'index',
        breadcrumbName: 'publisher',
    },
    {
        path: 'first',
        breadcrumbName: 'dashboard',
    },
    {
        path: 'second',
        breadcrumbName: 'add new app',
    },
];

class AddNewApp extends React.Component {
    routes;

    constructor(props) {
        super(props);
        this.routes = props.routes;
    }



    render() {
        return (
            <div>
                <PageHeader
                    title="Add New App"
                    breadcrumb={{ routes }}
                >
                    <div className="wrap">
                        <div className="content">
                            <Paragraph>
                                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempo.
                            </Paragraph>
                        </div>
                    </div>
                </PageHeader>
                <div style={{ background: '#f0f2f5', padding: 24, minHeight: 280 }}>

                </div>

            </div>

        );
    }
}

export default AddNewApp;
