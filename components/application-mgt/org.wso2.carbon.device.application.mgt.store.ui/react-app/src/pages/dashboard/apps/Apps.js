import React from "react";
import "antd/dist/antd.css";
import {PageHeader, Typography,Input, Button, Row, Col} from "antd";
import AppList from "../../../components/apps/AppList";
import ReleaseModal from "../../../components/apps/ReleaseModal";

const Search = Input.Search;

const routes = [
    {
        path: 'index',
        breadcrumbName: 'store',
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
                    <ReleaseModal/>
                    <AppList/>
                </div>

            </div>

        );
    }
}

export default Apps;
