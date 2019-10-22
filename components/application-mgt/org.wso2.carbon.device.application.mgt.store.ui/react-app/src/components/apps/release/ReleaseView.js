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
import {Divider, Row, Col, Typography, Button, Dropdown, notification, Menu, Icon, Spin, Tabs} from "antd";
import "../../../App.css";
import ImgViewer from "../../apps/release/images/ImgViewer";
import StarRatings from "react-star-ratings";
import DetailedRating from "./DetailedRating";
import Reviews from "./review/Reviews";
import axios from "axios";
import AppInstallModal from "./install/AppInstallModal";
import AppUninstallModal from "./install/AppUninstallModal";
import CurrentUsersReview from "./review/CurrentUsersReview";
import {withConfigContext} from "../../../context/ConfigContext";
import {handleApiError} from "../../../js/Utils";
import ReviewContainer from "./review/ReviewContainer";
import InstalledDevicesTable from "./InstalledDevicesTable";

const {Title, Text, Paragraph} = Typography;
const {TabPane} = Tabs;

class ReleaseView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            loading: false,
            appInstallModalVisible: false,
            appUninstallModalVisible: false
        }
    }

    appOperation = (type, payload, operation, timestamp = null) => {
        const config = this.props.context;
        const release = this.props.app.applicationReleases[0];
        const {uuid} = release;

        this.setState({
            loading: true,
        });
        let url = window.location.origin + config.serverConfig.invoker.uri +
            config.serverConfig.invoker.store + "/subscription/" + uuid + "/" + type + "/" + operation;
        if (timestamp != null) {
            url += `?timestamp=${timestamp}`;
        }
        axios.post(
            url,
            payload,
            {
                headers: {'X-Platform': config.serverConfig.platform}
            }
        ).then(res => {
            if (res.status === 200 || res.status === 201) {
                this.setState({
                    loading: false,
                    appInstallModalVisible: false,
                    appUninstallModalVisible: false,
                });
                notification["success"]({
                    message: 'Done!',
                    description:
                        'Operation triggered.',
                });
            } else {
                this.setState({
                    loading: false
                });
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "Error occurred while " + operation + "ing app",
                });
            }
        }).catch((error) => {
            handleApiError(error, "Error occurred while " + operation + "ing the app.");
        });
    };

    closeAppOperationModal = () => {
        this.setState({
            appInstallModalVisible: false,
            appUninstallModalVisible: false
        });
    };

    handleSubscribeClick = (e) => {
        if (e.key === "install") {
            this.setState({
                appInstallModalVisible: true // display app install modal
            })
        } else if (e.key === "uninstall") {
            this.setState({
                appUninstallModalVisible: true // display app uninstall modal
            })
        }
    };

    render() {
        const {app, deviceType} = this.props;
        const release = app.applicationReleases[0];

        let metaData = [];
        try {
            metaData = JSON.parse(release.metaData);
        } catch (e) {

        }
        if (app.hasOwnProperty("packageName")) {
            metaData.push({
                key: "Package Name",
                value: app.packageName
            });
        }
        const menu = (
            <Menu onClick={this.handleSubscribeClick}>
                <Menu.Item key="install">Install</Menu.Item>
                <Menu.Item key="uninstall">Uninstall</Menu.Item>
            </Menu>
        );

        return (
            <div>
                <AppInstallModal
                    uuid={release.uuid}
                    loading={this.state.loading}
                    visible={this.state.appInstallModalVisible}
                    deviceType={deviceType}
                    onClose={this.closeAppOperationModal}
                    onInstall={this.appOperation}/>
                <AppUninstallModal
                    uuid={release.uuid}
                    loading={this.state.loading}
                    visible={this.state.appUninstallModalVisible}
                    deviceType={deviceType}
                    onClose={this.closeAppOperationModal}
                    onUninstall={this.appOperation}/>
                <div className="release">
                    <Row>
                        <Col xl={4} sm={6} xs={8} className="release-icon">
                            <img src={release.iconPath} alt="icon"/>
                        </Col>
                        <Col xl={10} sm={11} className="release-title">
                            <Title level={2}>{app.name}</Title>
                            <Text>Version : {release.version}</Text><br/><br/>
                            <StarRatings
                                rating={app.rating}
                                starRatedColor="#777"
                                starDimension="20px"
                                starSpacing="2px"
                                numberOfStars={5}
                                name='rating'
                            />
                        </Col>
                        <Col xl={8} md={10} sm={24} xs={24} style={{float: "right"}}>
                            <div style={{
                                textAlign: "right"
                            }}>
                                <Dropdown overlay={menu}>
                                    <Button type="primary">
                                        Subscribe <Icon type="down"/>
                                    </Button>
                                </Dropdown>
                            </div>
                        </Col>
                    </Row>
                    <Divider dashed={true}/>
                    <Tabs>
                        <TabPane tab="App" key="1">
                            <Row>
                                <ImgViewer images={release.screenshots}/>
                            </Row>
                            <Divider/>
                            <Paragraph type="secondary" ellipsis={{rows: 3, expandable: true}}>
                                {release.description}
                            </Paragraph>
                            <Divider/>
                            <Text>META DATA</Text>
                            <Row>
                                {
                                    metaData.map((data, index) => {
                                        return (
                                            <Col key={index} lg={8} md={6} xs={24} style={{marginTop: 15}}>
                                                <Text>{data.key}</Text><br/>
                                                <Text type="secondary">{data.value}</Text>
                                            </Col>
                                        )
                                    })
                                }
                                {(metaData.length === 0) && (<Text type="secondary">No meta data available.</Text>)}
                            </Row>
                            <Divider/>
                            <ReviewContainer uuid={release.uuid}/>
                        </TabPane>
                        <TabPane tab="Installed devices" key="2">
                            <InstalledDevicesTable uuid={release.uuid}/>
                        </TabPane>
                    </Tabs>
                </div>
            </div>
        );
    }
}

export default withConfigContext(ReleaseView);
