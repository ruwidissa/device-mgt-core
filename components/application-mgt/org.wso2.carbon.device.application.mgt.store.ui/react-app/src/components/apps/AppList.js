import React from "react";
import AppCard from "./AppCard";
import {Col, message, notification, Row, Result, Skeleton} from "antd";
import axios from "axios";
import {withConfigContext} from "../../context/ConfigContext";

class AppList extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            apps: [],
            loading: true
        }
    }

    componentDidMount() {
        const {deviceType} = this.props;
        this.props.changeSelectedMenuItem(deviceType);
        this.fetchData(deviceType);
    }


    componentDidUpdate(prevProps, prevState) {
        if (prevProps.deviceType !== this.props.deviceType) {
            const {deviceType} = this.props;
            this.props.changeSelectedMenuItem(deviceType);
            this.fetchData(deviceType);
        }
    }

    fetchData = (deviceType) => {
        const config = this.props.context;
        const payload = {};
        if (deviceType === "web-clip") {
            payload.appType = "WEB_CLIP";
        } else {
            payload.deviceType = deviceType;
        }
        this.setState({
            loading: true
        });
        //send request to the invoker
        axios.post(
            config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.store + "/applications/",
            payload,
        ).then(res => {
            if (res.status === 200) {
                //todo remove this property check after backend improvement
                let apps = (res.data.data.hasOwnProperty("applications")) ? res.data.data.applications : [];
                this.setState({
                    apps: apps,
                    loading: false
                })
            }

        }).catch((error) => {
            console.log(error.response);
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                //todo display a popup with error
                message.error('You are not logged in');
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/store/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "Error occurred while trying to load apps.",
                });
            }

            this.setState({loading: false});
        });
    };

    render() {
        const {apps,loading} = this.state;

        return (
            <Skeleton loading={loading} active>
                <Row gutter={16}>
                    {apps.length === 0 && (
                        <Result
                            status="404"
                            title="No apps, yet."
                            subTitle="No apps available, yet! When the administration uploads, apps will show up here."
                            // extra={<Button type="primary">Back Home</Button>}
                        />
                    )}
                    {apps.map(app => (
                        <Col key={app.id} xs={12} sm={6} md={6} lg={4} xl={3}>
                            <AppCard key={app.id}
                                     app={app}
                            />
                        </Col>
                    ))}
                </Row>
            </Skeleton>
        );
    }
}

export default withConfigContext(AppList);