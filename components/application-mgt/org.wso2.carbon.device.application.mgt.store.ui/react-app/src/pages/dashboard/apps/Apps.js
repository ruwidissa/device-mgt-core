import React from "react";
import "antd/dist/antd.css";
import AppList from "../../../components/apps/AppList";


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
        const {deviceType} = this.props.match.params;
        return (
            <div>
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 780}}>
                    {deviceType!==null && <AppList deviceType={deviceType}/>}
                </div>

            </div>

        );
    }
}

export default Apps;
