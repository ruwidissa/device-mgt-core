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

import React from 'react';
import { Avatar } from 'antd';
import { List, Typography } from 'antd';
import StarRatings from 'react-star-ratings';

const { Text, Paragraph } = Typography;
const colorList = [
  '#f0932b',
  '#badc58',
  '#6ab04c',
  '#eb4d4b',
  '#0abde3',
  '#9b59b6',
  '#3498db',
  '#22a6b3',
];

class SingleReview extends React.Component {
  render() {
    const review = this.props.review;
    const randomColor = colorList[Math.floor(Math.random() * colorList.length)];
    const avatarLetter = review.username.charAt(0).toUpperCase();
    const content = (
      <div style={{ marginTop: -5 }}>
        <StarRatings
          rating={review.rating}
          starRatedColor="#777"
          starDimension="12px"
          starSpacing="2px"
          numberOfStars={5}
          name="rating"
        />
        <Text style={{ fontSize: 12, color: '#aaa' }} type="secondary">
          {' '}
          {review.createdAt}
        </Text>
        <br />
        <Paragraph
          ellipsis={{ rows: 3, expandable: true }}
          style={{ color: '#777' }}
        >
          {review.content}
        </Paragraph>
      </div>
    );

    return (
      <div>
        <List.Item.Meta
          avatar={
            <Avatar
              style={{ backgroundColor: randomColor, verticalAlign: 'middle' }}
              size="large"
            >
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
