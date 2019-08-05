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
import {List, message, Typography, Empty, Button, Row, Col, notification} from "antd";
import SingleReview from "./singleReview/SingleReview";
import axios from "axios";
import AddReview from "./AddReview";
import {withConfigContext} from "../../../../context/ConfigContext";

const {Text, Paragraph} = Typography;

class CurrentUsersReview extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            data: []

        };
    }

    componentDidMount() {
        this.fetchData();
    }

    fetchData = () => {
        const {uuid} = this.props;
        const config = this.props.context;

        axios.get(
            window.location.origin+ config.serverConfig.invoker.uri + config.serverConfig.invoker.store + "/reviews/app/user/" + uuid,
        ).then(res => {
            if (res.status === 200) {
                const data = res.data.data.data;
                this.setState({data});
            }

        }).catch((error) => {
            if (error.response.hasOwnProperty(status) && error.response.status === 401) {
                message.error('You are not logged in');
                window.location.href = window.location.origin+ '/store/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "Error occurred while trying to get your review.",
                });
            }
        });
    };

    deleteCallback = () =>{
        this.setState({
            data: []
        });
    };

    addCallBack =(review) =>{

    };

    render() {
        const {data} = this.state;
        const {uuid} = this.props;
        return (
            <div>
                <Text>MY REVIEW</Text>
                <div style={{
                    overflow: "auto",
                    paddingTop: 8,
                    paddingLeft: 24
                }}>
                    {data.length > 0 && (
                        <div>
                            <List
                                dataSource={data}
                                renderItem={item => (
                                    <List.Item key={item.id}>
                                        <SingleReview uuid={uuid} review={item} isDeletable={true} isEditable={true} deleteCallback={this.deleteCallback} isPersonalReview={true}/>
                                    </List.Item>
                                )}
                            >
                            </List>
                        </div>
                    )}

                    {data.length === 0 && (
                        <div>
                            <Empty
                                image={Empty.PRESENTED_IMAGE_DEFAULT}
                                imagestyle={{
                                    height: 60,
                                }}
                                description={
                                    <span>Share your experience with your community by adding a review.</span>
                                }
                            >
                                {/*<Button type="primary">Add review</Button>*/}
                                <AddReview uuid={uuid}/>
                            </Empty>
                        </div>
                    )}

                </div>
            </div>
        );
    }

}

export default withConfigContext(CurrentUsersReview);