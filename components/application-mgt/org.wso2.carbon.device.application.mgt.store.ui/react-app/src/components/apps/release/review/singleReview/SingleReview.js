import React from "react";
import {Avatar} from "antd";
import {List, Typography} from "antd";
import StarRatings from "react-star-ratings";
import Twemoji from "react-twemoji";
import "./SingleReview.css";
import EditReview from "./editReview/EditReview";

const {Text, Paragraph} = Typography;
const colorList = ['#f0932b', '#badc58', '#6ab04c', '#eb4d4b', '#0abde3', '#9b59b6', '#3498db', '#22a6b3','#e84393','#f9ca24'];

class SingleReview extends React.Component {

    render() {
        const {review, isEditable, isDeletable, uuid} = this.props;
        const {username} = review;
        const randomColor = colorList[username.length%10];
        const avatarLetter = username.charAt(0).toUpperCase();
        const content = (
            <div style={{marginTop: -5}}>
                <StarRatings
                    rating={review.rating}
                    starRatedColor="#777"
                    starDimension="12px"
                    starSpacing="2px"
                    numberOfStars={5}
                    name='rating'
                />
                <Text style={{fontSize: 12, color: "#aaa"}} type="secondary"> {review.createdAt}</Text><br/>
                    <Paragraph style={{color: "#777"}}>
                        <Twemoji options={{className: 'twemoji'}}>
                            {review.content}
                        </Twemoji>
                    </Paragraph>
            </div>
        );

        const title = (
            <div>
            {review.username}
                {isEditable && (<EditReview uuid={uuid} review={review}/>)}
                {isDeletable && (<span className="delete-button">delete</span>)}
            </div>
        );

        return (
            <div>
                <List.Item.Meta
                    avatar={
                        <Avatar style={{backgroundColor: randomColor, verticalAlign: 'middle'}} size="large">
                            {avatarLetter}
                        </Avatar>
                    }
                    title={title}
                    description={content}
                />
            </div>
        );
    }
}

export default SingleReview;