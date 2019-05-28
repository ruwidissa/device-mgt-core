import React from "react";
import {Row, Typography, Icon} from "antd";
import StarRatings from "react-star-ratings";
import "./DetailedRating.css";
import {connect} from "react-redux";
import {getDetailedRating} from "../../../js/actions";

const { Text } = Typography;

// connecting state. with the component
const mapStateToProps= state => {
    return {detailedRating : state.detailedRating}
};

const mapDispatchToProps = dispatch => ({
    getDetailedRating: (uuid) => dispatch(getDetailedRating(uuid))
});



class ConnectedDetailedRating extends React.Component{

    componentDidMount() {
        this.props.getDetailedRating(this.props.uuid);
    }

    render() {
        const detailedRating = this.props.detailedRating;

        if(detailedRating ==null){
            return null;
        }

        const totalCount = detailedRating.noOfUsers;
        const ratingVariety = detailedRating.ratingVariety;

        const ratingArray = [];

        for (let [key, value] of Object.entries(ratingVariety)) {
            ratingArray.push(value);
        }

        const maximumRating = Math.max(...ratingArray);

        console.log(maximumRating);

        return (
            <Row className="d-rating">
                <div className="numeric-data">
                    <div className="rate">{detailedRating.ratingValue.toFixed(1)}</div>
                    <StarRatings
                        rating={detailedRating.ratingValue}
                        starRatedColor="#777"
                        starDimension = "16px"
                        starSpacing = "2px"
                        numberOfStars={5}
                        name='rating'
                    />
                    <br/>
                    <Text type="secondary" className="people-count"><Icon type="team" /> {totalCount} total</Text>
                </div>
                <div className="bar-containers">
                    <div className="bar-container">
                        <span className="number">5</span>
                        <span className="bar rate-5" style={{width: (ratingVariety["5"]/maximumRating*100)+"%"}}> </span>
                    </div>
                    <div className="bar-container">
                        <span className="number">4</span>
                        <span className="bar rate-4" style={{width: (ratingVariety["4"]/maximumRating*100)+"%"}}> </span>
                    </div>
                    <div className="bar-container">
                        <span className="number">3</span>
                        <span className="bar rate-3" style={{width: (ratingVariety["3"]/maximumRating*100)+"%"}}> </span>
                    </div>
                    <div className="bar-container">
                        <span className="number">2</span>
                        <span className="bar rate-2" style={{width: (ratingVariety["2"]/maximumRating*100)+"%"}}> </span>
                    </div>
                    <div className="bar-container">
                        <span className="number">1</span>
                        <span className="bar rate-1" style={{width: (ratingVariety["1"]/maximumRating*100)+"%"}}> </span>
                    </div>
                </div>
            </Row>
        );
    }
}

const DetailedRating = connect(mapStateToProps,mapDispatchToProps)(ConnectedDetailedRating);

export default DetailedRating;