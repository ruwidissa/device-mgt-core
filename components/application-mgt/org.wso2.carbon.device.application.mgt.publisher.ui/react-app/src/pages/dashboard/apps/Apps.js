import React from "react";
import ListApps from "../../../components/apps/list-apps/ListApps";

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
