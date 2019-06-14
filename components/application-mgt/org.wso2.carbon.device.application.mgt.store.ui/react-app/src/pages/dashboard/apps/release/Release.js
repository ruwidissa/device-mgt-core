import React from "react";
import '../../../../App.css';
import {Skeleton, Typography, Row, Col, Card, message} from "antd";
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
            app: null
        }

    }

    componentDidMount() {
        const {uuid} = this.props.match.params;
        this.fetchData(uuid);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (prevProps !== this.props) {
            this.fetchData(uuid);
        }
    }

    fetchData = (uuid)=>{
        const parameters = {
            method: "get",
            'content-type': "application/json",
            payload: "{}",
            'api-endpoint': "/application-mgt-store/v1.0/applications/" + uuid
        };

        //url-encode parameters
        const request = Object.keys(parameters).map(key => key + '=' + parameters[key]).join('&');

        //send request to the invoker
        axios.post(config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
        ).then(res => {
            if (res.status === 200) {
                let app = res.data.data;

                console.log(app);

                this.setState({
                    app: app,
                    loading: false
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
        const {app, loading} = this.state;
        let content = <Title level={3}>No Releases Found</Title>;

        if (app != null && app.applicationReleases.length!==0) {
            content = <ReleaseView app={app}/>;
        }


        return (
            <div style={{background: '#f0f2f5', padding: 24, minHeight: 780}}>
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
