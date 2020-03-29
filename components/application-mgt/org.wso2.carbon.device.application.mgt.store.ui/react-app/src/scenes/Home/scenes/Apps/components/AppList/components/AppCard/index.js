/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { Card, Typography, Col, Row } from 'antd';
import React from 'react';
import { Link } from 'react-router-dom';
import './styles.css';
import StarRatings from 'react-star-ratings';

const { Meta } = Card;
const { Text } = Typography;

class AppCard extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    const app = this.props.app;
    const release = this.props.app.applicationReleases[0];

    const description = (
      <div className="appCard">
        <Link to={'/store/' + app.deviceType + '/apps/' + release.uuid}>
          <Row className="release">
            <Col span={24} className="release-icon">
              <div className="box">
                <div className="content">
                  <img className="app-icon" src={release.iconPath} alt="icon" />
                </div>
              </div>
              {/* <img src={release.iconPath} alt="icon"/>*/}
              {/* <Avatar shape="square" size={128} src={release.iconPath} />*/}
            </Col>
            <Col span={24} style={{ paddingTop: 10 }}>
              <Text className="app-name" strong level={4}>
                {app.name}
              </Text>
              <br />
              <Text type="secondary" level={4}>
                {app.type.toLowerCase()}
              </Text>
              <br />
              <StarRatings
                rating={app.rating}
                starRatedColor="#777"
                starDimension="12px"
                starSpacing="0"
                numberOfStars={5}
                name="rating"
              />
            </Col>
          </Row>
        </Link>
      </div>
    );

    return (
      <Card style={{ marginTop: 16 }}>
        <Meta description={description} />
      </Card>
    );
  }
}

export default AppCard;
