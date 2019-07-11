import React from "react";
import {PageHeader, Typography,Input, Button, Row, Col} from "antd";
import ListApps from "../../../components/apps/list-apps/ListApps";

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
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 780}}>
                    <ListApps/>
                </div>

            </div>

        );
    }
}

export default Apps;
