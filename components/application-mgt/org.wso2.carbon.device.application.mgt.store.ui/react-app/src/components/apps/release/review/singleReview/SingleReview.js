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
    constructor(props) {
        super(props);
        this.state = {
            content: '',
            rating: 0,
            color: '#f0932b'
        }
    }

    componentDidMount() {
        const {content, rating, username} = this.props.review;
        const color = colorList[username.length%10];
        this.setState({
            content,
            rating,
            color
        });
    }


    updateCallback = (response) =>{
        console.log(response);
        const {rating, content} = response;
        this.setState({
            rating,
            content
        });
    };

    render() {
        const {review, isEditable, isDeletable, uuid} = this.props;
        const {content, rating, color} = this.state;
        const {username} = review;
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
                {isDeletable && (<span className="delete-button">delete</span>)}
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