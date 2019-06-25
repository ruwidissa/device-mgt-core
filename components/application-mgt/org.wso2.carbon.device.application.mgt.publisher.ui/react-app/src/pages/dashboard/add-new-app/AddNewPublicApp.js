import React from "react";
import "antd/dist/antd.css";
import {
    PageHeader,
    Typography
} from "antd";
import AddNewAppForm from "../../../components/new-app/AddNewAppForm";

const Paragraph = Typography;

const formConfig = {
    installationType: "PUBLIC",
    endpoint: "/public-app",
    jsonPayloadName:"public-app",
    releaseWrapperName: "publicAppReleaseWrappers",
    specificElements: {
        packageName : {
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

    componentDidMount() {
        // this.getCategories();
    }


    render() {
        return (
            <div>
                <PageHeader
                    title="Add New Public App"
                >
                    <div className="wrap">
                        <div className="content">
                            <Paragraph>
                                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempo.
                            </Paragraph>
                        </div>
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
