import React from "react";
import {Drawer, Button, Icon, Row, Col, Typography, Divider, Input, Spin, notification} from 'antd';
import StarRatings from "react-star-ratings";
import axios from "axios";
import "./EditReview.css";
import {withConfigContext} from "../../../../../../context/ConfigContext";

const {Title} = Typography;
const {TextArea} = Input;

class EditReview extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            visible: false,
            content: '',
            rating: 0,
            loading: false
        };
    }

    componentDidMount() {
        const {content,rating,id} = this.props.review;
        console.log(this.props.review);
        this.setState({
            content,
            rating
        });
    }

    showDrawer = () => {
        this.setState({
            visible: true,
            loading: false
        });
    };

    onClose = () => {
        this.setState({
            visible: false,

        });
    };

    changeRating = (newRating, name) => {
        this.setState({
            rating: newRating
        });
    };

    onChange = (e) => {
        this.setState({content: e.target.value})
    };

    onSubmit = () => {
        const config = this.props.context;
        const {content, rating} = this.state;
        const {id} = this.props.review;
        const {uuid} = this.props;
        this.setState({
            loading: true
        });

        const payload = {
            content: content,
            rating: rating
        };

        axios.put(
            config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.store + "/reviews/" + uuid+"/"+id,
            payload,
        ).then(res => {
            if (res.status === 200) {
                this.setState({
                    loading: false,
                    visible: false
                });
                notification["success"]({
                    message: 'Done!',
                    description:
                        'Your review has been update successfully.',
                });

                this.props.updateCallback(res.data.data);
            } else {
                this.setState({
                    loading: false,
                    visible: false
                });
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "We are unable to update your review right now.",
                });
            }

        }).catch((error) => {
            console.log(error);
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/store/login';
            } else {
                this.setState({
                    loading: false,
                    visible: false
                });
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "We are unable to add your review right now.",
                });
            }
        });


    };

    render() {
        return (
            <span>
                <span className="edit-button" onClick={this.showDrawer}>edit</span>
                <Drawer
                    // title="Basic Drawer"
                    placement="bottom"
                    closable={false}
                    onClose={this.onClose}
                    visible={this.state.visible}
                    height={400}
                >
                    <Spin spinning={this.state.loading} tip="Posting your review...">
                        <Row>
                            <Col lg={8}/>
                            <Col lg={8}>
                                <Title level={4}>Edit review</Title>
                                <Divider/>
                                <TextArea
                                    placeholder="Tell others what you think about this app. Would you recommend it, and why?"
                                    onChange={this.onChange}
                                    rows={6}
                                    value={this.state.content || ''}
                                    style={{marginBottom: 20}}
                                />
                                <StarRatings
                                    rating={this.state.rating}
                                    changeRating={this.changeRating}
                                    starRatedColor="#777"
                                    starHoverColor="#444"
                                    starDimension="20px"
                                    starSpacing="2px"
                                    numberOfStars={5}
                                    name='rating'
                                />
                                <br/><br/>
                                <Button onClick={this.onClose} style={{marginRight: 8}}>
                                    Cancel
                                </Button>
                                <Button disabled={this.state.rating === 0} onClick={this.onSubmit} type="primary">
                                    Submit
                                </Button>
                            </Col>
                        </Row>
                    </Spin>
                </Drawer>


            </span>
        );
    }
}

export default withConfigContext(EditReview);