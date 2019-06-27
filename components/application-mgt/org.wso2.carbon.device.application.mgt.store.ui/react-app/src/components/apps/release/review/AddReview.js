import React from "react";
import {Drawer, Button, Icon, Row, Col, Typography, Divider, Input,Spin,notification} from 'antd';
import StarRatings from "react-star-ratings";
import axios from "axios";
import config from "../../../../../public/conf/config.json";

const {Title} = Typography;
const {TextArea} = Input;

class AddReview extends React.Component {
    state = {
        visible: false,
        content: '',
        rating: 0,
        loading: false
    };

    showDrawer = () => {
        this.setState({
            visible: true,
            content: '',
            rating: 0,
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
        const {content, rating} = this.state;
        const {uuid} = this.props;
        this.setState({
            loading: true
        });

        const payload = {
            content: content,
            rating: rating
        };

        const request = "method=post&content-type=application/json&payload="+JSON.stringify(payload)+"&api-endpoint=/application-mgt-store/v1.0/reviews/"+uuid;

        axios.post(
            config.serverConfig.protocol + "://"+config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri +config.serverConfig.invoker.store+"/reviews/"+uuid,
            payload,
            ).then(res => {
            if (res.status === 201) {
                this.setState({
                    loading: false,
                    visible: false
                });
                notification["success"]({
                    message: 'Done!',
                    description:
                        'Your review has been posted successfully.',
                });

                setTimeout(()=>{
                    window.location.href= uuid;
                },2000)
            }else{
                this.setState({
                    loading: false,
                    visible: false
                });
                notification["error"]({
                    message: 'Something went wrong',
                    description:
                        "We are unable to add your review right now.",
                });
            }

        }).catch((error) =>{
            if (error.response.status === 401) {
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort+'/store/login';
            } else{
                this.setState({
                    loading: false,
                    visible: false
                });
                notification["error"]({
                    message: 'Something went wrong',
                    description:
                        "We are unable to add your review right now.",
                });
            }
        });


    };

    render() {
        return (
            <div style={{paddingTop: 20}}>
                <Button style={{float: "right"}} type="dashed" onClick={this.showDrawer}>
                    <Icon type="star"/> Add a review
                </Button>

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
                            <Title level={4}>Add review</Title>
                            <Divider/>
                            <TextArea
                                placeholder="Tell others what you think about this app. Would you recommend it, and why?"
                                onChange={this.onChange}
                                autosize={{minRows: 6, maxRows: 12}}
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
                            <Button disabled={this.state.rating===0} onClick={this.onSubmit} type="primary">
                                Submit
                            </Button>
                        </Col>
                    </Row>
                    </Spin>
                </Drawer>


            </div>
        );
    }
}

export default AddReview;