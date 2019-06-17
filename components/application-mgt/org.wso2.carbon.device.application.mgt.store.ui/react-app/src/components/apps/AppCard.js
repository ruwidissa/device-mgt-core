import {Card, Typography, Col, Row} from 'antd';
import React from "react";
import {Link} from "react-router-dom";
import "../../App.css";
import StarRatings from 'react-star-ratings';

const {Meta} = Card;
const {Text} = Typography;

class AppCard extends React.Component {

    constructor(props) {
        super(props);
        this.handleReleasesClick = this.handleReleasesClick.bind(this);
    }

    handleReleasesClick() {
        this.props.openReleasesModal(this.props.app);
    }

    render() {
        const app = this.props.app;
        const release = this.props.app.applicationReleases[0];

        const description = (
            <div className="appCard">
                <Link to={"/store/"+app.deviceType+"/apps/" + release.uuid}>
                    <Row className="release">
                        <Col span={24} className="release-icon">
                            <img src={release.iconPath} alt="icon"/>
                            {/*<Avatar shape="square" size={128} src={release.iconPath} />*/}
                        </Col>
                        <Col span={24}>
                            <Text strong level={4}>{app.name}</Text><br/>
                            <Text type="secondary" level={4}>{app.deviceType}</Text><br/>
                            <StarRatings
                                rating={app.rating}
                                starRatedColor="#777"
                                starDimension = "12px"
                                starSpacing = "0"
                                numberOfStars={5}
                                name='rating'
                            />
                        </Col>
                    </Row>
                </Link>
            </div>
        );

        return (
            <Card style={{marginTop: 16}}>
                <Meta
                    description={description}
                />
            </Card>
        );
    }
}

export default AppCard;