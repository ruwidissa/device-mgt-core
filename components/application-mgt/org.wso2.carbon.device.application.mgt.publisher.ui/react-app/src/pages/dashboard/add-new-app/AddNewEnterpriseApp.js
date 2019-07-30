import React from "react";
import {
    PageHeader,
    Typography,
    Breadcrumb,
    Icon
} from "antd";
import AddNewAppForm from "../../../components/new-app/AddNewAppForm";
import {Link} from "react-router-dom";

const {Paragraph} = Typography;

const formConfig = {
    installationType: "ENTERPRISE",
    endpoint: "/ent-app",
    jsonPayloadName: "application",
    releaseWrapperName: "entAppReleaseWrappers",
    specificElements: {
        binaryFile: {
            required: true
        }
    }
};

class AddNewEnterpriseApp extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            current: 0,
            categories: []
        };
    }

    render() {
        return (
            <div>
                <PageHeader style={{paddingTop:0}}>
                    <Breadcrumb style={{paddingBottom:16}}>
                        <Breadcrumb.Item>
                            <Link to="/publisher/apps"><Icon type="home"/> Home</Link>
                        </Breadcrumb.Item>
                        <Breadcrumb.Item>Add New Enterprise App</Breadcrumb.Item>
                    </Breadcrumb>
                    <div className="wrap">
                        <h3>Add New Enterprise App</h3>
                        <Paragraph>Submit and share your own application to the corporate app store.</Paragraph>
                    </div>
                </PageHeader>
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 720}}>
                    <AddNewAppForm formConfig={formConfig}/>
                </div>

            </div>

        );
    }
}

export default AddNewEnterpriseApp;
