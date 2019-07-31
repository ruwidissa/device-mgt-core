import React from "react";
import {
    Icon,
    PageHeader,
    Typography,
    Breadcrumb
} from "antd";
import AddNewReleaseForm from "../../../components/new-release/AddReleaseForm";
import {Link} from "react-router-dom";

const Paragraph = Typography;

class AddNewRelease extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            current: 0,
            categories: []
        };
    }

    render() {
        const {appId} = this.props.match.params;
        return (
            <div>
                <PageHeader style={{paddingTop: 0}}>
                    <Breadcrumb style={{paddingBottom: 16}}>
                        <Breadcrumb.Item>
                            <Link to="/publisher/apps"><Icon type="home"/> Home</Link>
                        </Breadcrumb.Item>
                        <Breadcrumb.Item>Add New Release</Breadcrumb.Item>
                    </Breadcrumb>
                    <div className="wrap">
                        <h3>Add New Release</h3>
                        <Paragraph>Maintain and manage categories and tags here..</Paragraph>
                    </div>
                </PageHeader>
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 720}}>
                    <AddNewReleaseForm appId={appId}/>
                </div>

            </div>

        );
    }
}

export default AddNewRelease;
