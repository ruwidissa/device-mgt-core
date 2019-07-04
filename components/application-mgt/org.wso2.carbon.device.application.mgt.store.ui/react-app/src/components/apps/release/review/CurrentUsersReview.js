import React from "react";
import {List, message, Typography, Empty, Button, Row, Col} from "antd";
import SingleReview from "./singleReview/SingleReview";
import axios from "axios";
import config from "../../../../../public/conf/config.json";
import AddReview from "./AddReview";

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

        axios.get(
            config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.store + "/reviews/app/user/" + uuid,
        ).then(res => {
            if (res.status === 200) {
                const data = res.data.data.data;
                this.setState({data});
            }

        }).catch((error) => {
            if (error.response.hasOwnProperty(status) && error.response.status === 401) {
                message.error('You are not logged in');
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/store/login';
            } else {
                message.error('Something went wrong when trying to get your review... :(');
            }
        });
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
                                        <SingleReview uuid={uuid} review={item} isDeletable={true} isEditable={true}/>
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

export default CurrentUsersReview;