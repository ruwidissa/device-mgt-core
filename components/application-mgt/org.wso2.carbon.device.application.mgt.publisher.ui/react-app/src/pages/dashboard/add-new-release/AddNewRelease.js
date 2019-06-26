import React from "react";
import "antd/dist/antd.css";
import {
    PageHeader,
    Typography
} from "antd";
import AddNewAppForm from "../../../components/new-release/AddReleaseForm";

const Paragraph = Typography;

class AddNewRelease extends React.Component {

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
        const {appId} = this.props.match.params;
        return (
            <div>
                <PageHeader
                    title="Add New Release"
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
                    <AddNewAppForm appId={appId}/>
                </div>

            </div>

        );
    }
}

export default AddNewRelease;
