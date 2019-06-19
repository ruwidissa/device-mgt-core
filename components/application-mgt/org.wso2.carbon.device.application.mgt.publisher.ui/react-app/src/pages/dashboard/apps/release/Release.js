import React from "react";
import '../../../../App.css';
import {PageHeader, Typography, Row, Col, message, Card} from "antd";
import axios from 'axios';
import config from "../../../../../public/conf/config.json";
import ReleaseView from "../../../../components/apps/release/ReleaseView";
import LifeCycle from "../../../../components/apps/release/LifeCycle";

const {Title} = Typography;

const routes = [
    {
        path: 'index',
        breadcrumbName: 'Publisher',
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


class Release extends React.Component {
    routes;

    constructor(props) {
        super(props);
        this.routes = props.routes;
        this.state = {
            loading: true,
            app: null,
            uuid: null
        }
    }

    componentDidMount() {
        const {uuid} = this.props.match.params;
        this.fetchData(uuid);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (prevState.uuid !== this.state.uuid) {
            const {uuid} = this.props.match.params;
            this.fetchData(uuid);
        }
    }

    fetchData = (uuid) => {

        //send request to the invoker
        axios.get(
            config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/release/"+ uuid,
            {
                headers: {'X-Platform': config.serverConfig.platform}
            }
        ).then(res => {
            if (res.status === 200) {
                let app = res.data.data;
                this.setState({
                    app: app,
                    loading: false,
                    uuid: uuid
                })
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                //todo display a popop with error
                message.error('You are not logged in');
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/store/login';
            } else {
                message.error('Something went wrong... :(');
            }

            this.setState({loading: false});
        });
    };

    render() {
        const {app} = this.state;

        if (app == null) {
            return (
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 780}}>
                    <Title level={3}>No Apps Found</Title>
                </div>
            );
        }

        //todo remove uppercase
        return (
            <div>
                <div className="main-container">
                    <Row style={{padding: 10}}>
                        <Col lg={16} md={24} style={{padding: 3}}>
                            <Card>
                                <ReleaseView app={app}/>
                            </Card>
                        </Col>
                        <Col lg={8} md={24} style={{padding: 3}}>
                            <Card lg={8} md={24}>
                                {/*<LifeCycle currentStatus={release.currentStatus.toUpperCase()}/>*/}
                            </Card>
                        </Col>
                    </Row>
                </div>
            </div>

        );
    }
}

export default Release;
