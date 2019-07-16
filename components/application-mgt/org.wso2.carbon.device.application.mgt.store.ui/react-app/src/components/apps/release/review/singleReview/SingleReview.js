import React from "react";
import {Avatar, notification} from "antd";
import {List, Typography, Popconfirm} from "antd";
import StarRatings from "react-star-ratings";
import Twemoji from "react-twemoji";
import "./SingleReview.css";
import EditReview from "./editReview/EditReview";
import axios from "axios";
import config from "../../../../../../public/conf/config.json";

const {Text, Paragraph} = Typography;
const colorList = ['#f0932b', '#badc58', '#6ab04c', '#eb4d4b', '#0abde3', '#9b59b6', '#3498db', '#22a6b3', '#e84393', '#f9ca24'];

class SingleReview extends React.Component {

    static defaultProps = {
        isPersonalReview: false
    };

    constructor(props) {
        super(props);
        const {username} = this.props.review;
        const color = colorList[username.length % 10];
        this.state = {
            content: '',
            rating: 0,
            color: color,
            review: props.review
        }
    }

    updateCallback = (review) => {
        this.setState({
            review
        });
    };

    deleteReview = () => {
        const {uuid} = this.props;
        const {id} = this.state.review;

        let url = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' +
            config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.store;

        // call as an admin api if the review is not a personal review
        if (!this.props.isPersonalReview) {
            url += "/admin";
        }

        url += "/reviews/" + uuid + "/" + id;

        axios.delete(url).then(res => {
            if (res.status === 200) {
                notification["success"]({
                    message: 'Done!',
                    description:
                        'The review has been deleted successfully.',
                });

                this.props.deleteCallback(id);
            }
        }).catch((error) => {
            console.log(error);
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/store/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "We were unable to delete the review..",
                });
            }
        });

    };

    render() {
        const {isEditable, isDeletable, uuid} = this.props;
        const {color, review} = this.state;
        const {content, rating, username} = review;
        const avatarLetter = username.charAt(0).toUpperCase();
        const body = (
            <div style={{marginTop: -5}}>
                <StarRatings
                    rating={rating}
                    starRatedColor="#777"
                    starDimension="12px"
                    starSpacing="2px"
                    numberOfStars={5}
                    name='rating'
                />
                <Text style={{fontSize: 12, color: "#aaa"}} type="secondary"> {review.createdAt}</Text><br/>
                <Paragraph style={{color: "#777"}}>
                    <Twemoji options={{className: 'twemoji'}}>
                        {content}
                    </Twemoji>
                </Paragraph>
            </div>
        );

        const title = (
            <div>
                {review.username}
                {isEditable && (<EditReview uuid={uuid} review={review} updateCallback={this.updateCallback}/>)}
                {isDeletable && (
                    <Popconfirm
                        title="Are you sure delete this review?"
                        onConfirm={this.deleteReview}
                        okText="Yes"
                        cancelText="No"
                    >
                        <span className="delete-button">delete</span>
                    </Popconfirm>)}
            </div>
        );

        return (
            <div>
                <List.Item.Meta
                    avatar={
                        <Avatar style={{backgroundColor: color, verticalAlign: 'middle'}} size="large">
                            {avatarLetter}
                        </Avatar>
                    }
                    title={title}
                    description={body}
                />
            </div>
        );
    }
}

export default SingleReview;