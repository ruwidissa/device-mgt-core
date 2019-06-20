import React from "react";
import {Avatar} from "antd";
import {List,Typography} from "antd";
import StarRatings from "react-star-ratings";

const {Text, Paragraph} = Typography;
const colorList = ['#f0932b','#badc58','#6ab04c','#eb4d4b','#0abde3', '#9b59b6','#3498db','#22a6b3'];

class SingleReview extends React.Component {

    render() {
        const review = this.props.review;
        const randomColor = colorList[Math.floor(Math.random() * (colorList.length))];
        const avatarLetter = review.username.charAt(0).toUpperCase();
        const content = (
            <div style={{marginTop: -5}}>
                <StarRatings
                    rating={review.rating}
                    starRatedColor="#777"
                    starDimension = "12px"
                    starSpacing = "2px"
                    numberOfStars={5}
                    name='rating'
                />
                <Text style={{fontSize: 12, color: "#aaa"}} type="secondary"> {review.createdAt}</Text><br/>
                <Paragraph ellipsis={{ rows: 3, expandable: true }} style={{color: "#777"}}>{review.content}</Paragraph>
            </div>
        );

        return (
            <div>
                <List.Item.Meta
                    avatar={
                        <Avatar style={{ backgroundColor: randomColor, verticalAlign: 'middle' }} size="large">
                            {avatarLetter}
                        </Avatar>
                    }
                    title={review.username}
                    description={content}
                />
            </div>
        );
    }
}

export default SingleReview;