import React from "react";
import '../../../../App.css';
import {Skeleton, Typography, Row, Col, Card, message, notification} from "antd";
import ReleaseView from "../../../../components/apps/release/ReleaseView";
import axios from "axios";
import config from "../../../../../public/conf/config.json";

const {Title} = Typography;

class Release extends React.Component {
    routes;

    constructor(props) {
        super(props);
        this.routes = props.routes;
        this.state={
            loading: true,
            app: null,
            uuid: null
        }

    }

    componentDidMount() {
        const {uuid, deviceType} = this.props.match.params;
        this.fetchData(uuid);
        this.props.changeSelectedMenuItem(deviceType);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (prevState.uuid !== this.state.uuid) {
            const {uuid,deviceType} = this.props.match.params;
            this.fetchData(uuid);
            this.props.changeSelectedMenuItem(deviceType);
        }
    }

    fetchData = (uuid)=>{
        //send request to the invoker
        axios.get(
            config.serverConfig.protocol + "://"+config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.store+"/applications/"+uuid,

        ).then(res => {
            if (res.status === 200) {
                let app = res.data.data;

                this.setState({
                    app: app,
                    loading: false,
                    uuid: uuid
                })
            }

        }).catch((error) => { console.log(error);
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                //todo display a popop with error
                message.error('You are not logged in');
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/store/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "Error occurred while trying to load releases.",
                });
            }

            this.setState({loading: false});
        });
    };

    render() {
        const {app, loading} = this.state;
        let content = <Title level={3}>No Releases Found</Title>;

        if (app != null && app.applicationReleases.length!==0) {
            content = <ReleaseView app={app}/>;
        }


        return (
            <div style={{background: '#f0f2f5', minHeight: 780}}>
                <Row style={{padding: 10}}>
                    <Col lg={4}>

                    </Col>
                    <Col lg={16} md={24} style={{padding: 3}}>
                        <Card>
                            <Skeleton loading={loading} avatar={{size: 'large'}} active paragraph={{rows: 8}}>
                                {content}
                            </Skeleton>
                        </Card>
                    </Col>
                </Row>

            </div>
        );
    }
}


export default Release;
