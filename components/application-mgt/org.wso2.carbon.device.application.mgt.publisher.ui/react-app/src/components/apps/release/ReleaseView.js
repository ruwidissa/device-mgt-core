import React from "react";
import {Divider, Row, Col, Typography, Button, Drawer} from "antd";
import StarRatings from "react-star-ratings";
import Reviews from "./review/Reviews";
import "../../../App.css";
import config from "../../../../public/conf/config.json";
import DetailedRating from "../detailed-rating/DetailedRating";

const {Title, Text, Paragraph} = Typography;

class ReleaseView extends React.Component {
    render() {
        const app = this.props.app;
        const release = (app !== null) ? app.applicationReleases[0] : null;
        if(release == null){
            return null;
        }
        return (
            <div>
                <div className="release">
                    <Row>
                        <Col xl={4} sm={6} xs={8} className="release-icon">
                            <img src={release.iconPath} alt="icon"/>
                        </Col>
                        <Col xl={10} sm={11} className="release-title">
                            <Title level={2}>{app.name}</Title>
                            <Text>Version : {release.version}</Text><br/><br/>
                            <StarRatings
                                rating={release.rating}
                                starRatedColor="#777"
                                starDimension="20px"
                                starSpacing="2px"
                                numberOfStars={5}
                                name='rating'
                            />
                        </Col>
                        <Col xl={8} md={10} sm={24} xs={24} style={{float: "right"}}>
                            <div>
                                <Button.Group style={{float: "right"}}>
                                    <Button htmlType="button" icon="edit">edit</Button>
                                    <Button htmlType="button"
                                            type="primary"
                                            icon="shop"
                                            disabled={release.currentStatus !== "PUBLISHED"}
                                            onClick={() => {
                                                window.open("https://"+ config.serverConfig.hostname + ':' + config.serverConfig.httpsPort+"/store/"+app.deviceType+"/apps/"+release.uuid)
                                            }}>
                                        Open in store
                                    </Button>
                                </Button.Group>
                            </div>
                        </Col>
                    </Row>
                    <Divider/>
                    <Row>
                        {release.screenshots.map((screenshotUrl) => {
                            return (
                                <Col key={"col-" + screenshotUrl} lg={6} md={8} xs={8} className="release-screenshot">
                                    <img key={screenshotUrl} src={screenshotUrl}/>
                                </Col>
                            )
                        })}
                    </Row>
                    <Divider/>
                    <Paragraph type="secondary" ellipsis={{rows: 3, expandable: true}}>
                        {release.description}
                    </Paragraph>
                    <Divider/>
                    <Text>REVIEWS</Text>
                    <Row>
                        <Col lg={18}>
                            <DetailedRating type="release" uuid={release.uuid}/>
                        </Col>
                    </Row>
                    <Reviews type="release" uuid={release.uuid}/>
                </div>
            </div>
        );
    }
}

export default ReleaseView;