import React from "react";
import {
    Card,
    Button,
    Steps,
    Row,
    Col,
    Form,
    Result,
    notification,
    Spin
} from "antd";
import axios from "axios";
import {withRouter} from 'react-router-dom'
import config from "../../../public/conf/config.json";
import NewAppDetailsForm from "./subForms/NewAppDetailsForm";
import NewAppUploadForm from "./subForms/NewAppUploadForm";

const {Step} = Steps;


class AddNewAppFormComponent extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            current: 0,
            categories: [],
            tags: [],
            icons: [],
            screenshots: [],
            loading: false,
            binaryFiles: [],
            application: null,
            release: null,
            isError: false
        };
    }

    onSuccessApplicationData = (application) => {
        this.setState({
            application,
            current: 1
        });
    };

    onSuccessReleaseData = (releaseData) => {
        this.setState({
            loading: true,
            isError: false
        });
        const {application} = this.state;
        const {data, release} = releaseData;
        const {formConfig} = this.props;

        //add release wrapper
        application[formConfig.releaseWrapperName] = [release];

        const json = JSON.stringify(application);
        const blob = new Blob([json], {
            type: 'application/json'
        });
        data.append(formConfig.jsonPayloadName, blob);

        const url = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications" + formConfig.endpoint;

        axios.post(
            url,
            data,
            {
                headers: {
                    'X-Platform': config.serverConfig.platform
                },
            }
        ).then(res => {
            if (res.status === 201) {
                this.setState({
                    loading: false,
                    current: 2
                });
            } else {
                this.setState({
                    loading: false,
                    isError: true,
                    current: 2
                });
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
            } else {
                notification["error"]({
                    message: "Something went wrong!",
                    description:
                        "Sorry, we were unable to complete your request.",
                });

            }
            this.setState({
                loading: false,
                isError: true,
                current: 2
            });
        });

    };

    onClickBackButton = () => {
        const current = this.state.current - 1;
        this.setState({current});
    };

    render() {
        const { loading, current, isError} = this.state;
        const {formConfig} = this.props;

        return (
            <div>
                <Spin tip="Uploading..." spinning={loading}>
                    <Row>
                        <Col span={16} offset={4}>
                            <Steps style={{minHeight: 32}} current={current}>
                                <Step key="Application" title="Application"/>
                                <Step key="Release" title="Release"/>
                                <Step key="Result" title="Result"/>
                            </Steps>
                            <Card style={{marginTop: 24}}>
                                <div style={{display: (current === 0 ? 'unset' : 'none')}}>
                                    <NewAppDetailsForm
                                        formConfig={formConfig}
                                        onSuccessApplicationData={this.onSuccessApplicationData}/>
                                </div>
                                <div style={{display: (current === 1 ? 'unset' : 'none')}}>
                                    <NewAppUploadForm
                                        formConfig={formConfig}
                                        onSuccessReleaseData={this.onSuccessReleaseData}
                                        onClickBackButton={this.onClickBackButton}/>
                                </div>

                                <div style={{display: (current === 2 ? 'unset' : 'none')}}>

                                    {!isError && (<Result
                                        status="success"
                                        title="Application created successfully!"
                                        extra={[
                                            <Button type="primary" key="console"
                                                    onClick={() => this.props.history.push('/publisher/apps')}>
                                                Go to applications
                                            </Button>
                                        ]}
                                    />)}

                                    {isError && (<Result
                                        status="500"
                                        title="Error occurred while creating the application."
                                        subTitle="Go back to edit the details and submit again."
                                        extra={<Button onClick={this.onClickBackButton}>Back</Button>}
                                    />)}
                                </div>
                            </Card>
                        </Col>
                    </Row>
                </Spin>
            </div>

        );
    }
}

const AddNewAppForm = withRouter(Form.create({name: 'add-new-app'})(AddNewAppFormComponent));
export default AddNewAppForm;
