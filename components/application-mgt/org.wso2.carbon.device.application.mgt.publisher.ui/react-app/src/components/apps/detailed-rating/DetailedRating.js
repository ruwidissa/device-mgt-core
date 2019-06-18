import React from "react";
import {Row, Typography, Icon} from "antd";
import StarRatings from "react-star-ratings";
import "./DetailedRating.css";
import config from "../../../../public/conf/config.json";
import axios from "axios";

const { Text } = Typography;


class DetailedRating extends React.Component{

    constructor(props){
        super(props);
        this.state={
            detailedRating: null
        }
    }

    componentDidMount() {
        this.getData(this.props.uuid);
    }

    componentDidUpdate(prevProps, prevState) {
        if (prevProps.uuid !== this.props.uuid) {
            this.getData(this.props.uuid);
        }
    }

    getData = (uuid)=>{
        const request = "method=get&content-type=application/json&payload={}&api-endpoint=/application-mgt-store/v1.0/reviews/"+uuid+"/rating";

        return axios.get(
            config.serverConfig.protocol + "://"+config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri+"/reviews/"+uuid+"/rating",
            {
                headers: { 'X-Platform': config.serverConfig.platform }
            }).then(res => {
            if (res.status === 200) {
                let detailedRating = res.data.data;
                this.setState({
                    detailedRating
                })
            }

        }).catch(function (error) {
            if (error.response.status === 401) {
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort+'/publisher/login';
            }
        });
    };

    render() {
        const detailedRating = this.state.detailedRating;

        console.log(detailedRating);

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

        const ratingBarPercentages = [0,0,0,0,0];

        if(maximumRating>0){
            for(let i = 0; i<5; i++){
                ratingBarPercentages[i] = (ratingVariety[(i+1).toString()])/maximumRating*100;
            }
        }

        console.log(ratingBarPercentages);

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
                        <span className="bar rate-5" style={{width: ratingBarPercentages[4]+"%"}}> </span>
                    </div>
                    <div className="bar-container">
                        <span className="number">4</span>
                        <span className="bar rate-4" style={{width: ratingBarPercentages[3]+"%"}}> </span>
                    </div>
                    <div className="bar-container">
                        <span className="number">3</span>
                        <span className="bar rate-3" style={{width: ratingBarPercentages[2]+"%"}}> </span>
                    </div>
                    <div className="bar-container">
                        <span className="number">2</span>
                        <span className="bar rate-2" style={{width: ratingBarPercentages[1]+"%"}}> </span>
                    </div>
                    <div className="bar-container">
                        <span className="number">1</span>
                        <span className="bar rate-1" style={{width: ratingBarPercentages[0]+"%"}}> </span>
                    </div>
                </div>
            </Row>
        );
    }
}


export default DetailedRating;