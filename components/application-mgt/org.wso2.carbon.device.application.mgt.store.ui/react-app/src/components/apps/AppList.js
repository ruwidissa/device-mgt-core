/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from "react";
import AppCard from "./AppCard";
import {Col, message, notification, Row, Result, Skeleton} from "antd";
import axios from "axios";
import {withConfigContext} from "../../context/ConfigContext";
import {handleApiError} from "../../js/Utils";

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
            window.location.origin+ config.serverConfig.invoker.uri + config.serverConfig.invoker.store + "/applications/",
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
            handleApiError(error,"Error occurred while trying to load apps.");
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