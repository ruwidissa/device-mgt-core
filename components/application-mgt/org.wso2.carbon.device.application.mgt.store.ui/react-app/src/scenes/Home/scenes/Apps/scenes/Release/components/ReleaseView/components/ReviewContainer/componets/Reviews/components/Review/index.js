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
import { Avatar, notification } from 'antd';
import { List, Typography, Popconfirm } from 'antd';
import StarRatings from 'react-star-ratings';
import Twemoji from 'react-twemoji';
import './styles.css';
import EditReview from './components/Edit';
import axios from 'axios';
import { withConfigContext } from '../../../../../../../../../../../../../../components/context/ConfigContext';
import { handleApiError } from '../../../../../../../../../../../../../../services/utils/errorHandler';

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
  '#e84393',
  '#f9ca24',
];

class Review extends React.Component {
  static defaultProps = {
    isPersonalReview: false,
  };

  constructor(props) {
    super(props);
    const { username } = this.props.review;
    const color = colorList[username.length % 10];
    this.state = {
      content: '',
      rating: 0,
      color: color,
      review: props.review,
    };
  }

  updateCallback = review => {
    this.setState({
      review,
    });
    this.props.onUpdateReview();
  };

  deleteReview = () => {
    const { uuid } = this.props;
    const { id } = this.state.review;
    const config = this.props.context;

    let url =
      window.location.origin +
      config.serverConfig.invoker.uri +
      config.serverConfig.invoker.store;

    // call as an admin api if the review is not a personal review
    if (!this.props.isPersonalReview) {
      url += '/admin';
    }

    url += '/reviews/' + uuid + '/' + id;

    axios
      .delete(url)
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Done!',
            description: 'The review has been deleted successfully.',
          });

          this.props.deleteCallback(id);
        }
      })
      .catch(error => {
        handleApiError(error, 'We were unable to delete the review..');
      });
  };

  render() {
    const { isEditable, isDeletable, uuid } = this.props;
    const { color, review } = this.state;
    const { content, rating, username } = review;
    const avatarLetter = username.charAt(0).toUpperCase();
    const body = (
      <div style={{ marginTop: -5 }}>
        <StarRatings
          rating={rating}
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
        <Paragraph style={{ color: '#777' }}>
          <Twemoji options={{ className: 'twemoji' }}>{content}</Twemoji>
        </Paragraph>
      </div>
    );

    const title = (
      <div>
        {review.username}
        {isEditable && (
          <EditReview
            uuid={uuid}
            review={review}
            updateCallback={this.updateCallback}
          />
        )}
        {isDeletable && (
          <Popconfirm
            title="Are you sure delete this review?"
            onConfirm={this.deleteReview}
            okText="Yes"
            cancelText="No"
          >
            <span className="delete-button">delete</span>
          </Popconfirm>
        )}
      </div>
    );

    return (
      <div>
        <List.Item.Meta
          avatar={
            <Avatar
              style={{ backgroundColor: color, verticalAlign: 'middle' }}
              size="large"
            >
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

export default withConfigContext(Review);
