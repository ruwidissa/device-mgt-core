import React from "react";
import {
    Icon,
    PageHeader,
    Typography,
    Breadcrumb
} from "antd";
import AddNewAppForm from "../../../components/new-app/AddNewAppForm";
import {Link} from "react-router-dom";

const {Paragraph, Title}= Typography;

const formConfig = {
    installationType: "WEB_CLIP",
    endpoint: "/web-app",
    jsonPayloadName:"webapp",
    releaseWrapperName: "webAppReleaseWrappers",
    specificElements: {
        url : {
            required: true
        },
        version : {
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
                        <Breadcrumb.Item>Add New Web Clip</Breadcrumb.Item>
                    </Breadcrumb>
                    <div className="wrap">
                        <h3>Add New Web Clip</h3>
                        <Paragraph>Share a Web Clip to your corporate  app store.</Paragraph>
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
