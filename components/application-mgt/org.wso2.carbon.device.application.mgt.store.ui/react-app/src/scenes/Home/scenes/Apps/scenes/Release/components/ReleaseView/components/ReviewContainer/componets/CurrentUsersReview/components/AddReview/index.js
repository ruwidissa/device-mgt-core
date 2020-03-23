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
import {
  Drawer,
  Button,
  Icon,
  Row,
  Col,
  Typography,
  Divider,
  Input,
  Spin,
  notification,
} from 'antd';
import StarRatings from 'react-star-ratings';
import axios from 'axios';
import { withConfigContext } from '../../../../../../../../../../../../../../components/context/ConfigContext';
import { handleApiError } from '../../../../../../../../../../../../../../services/utils/errorHandler';

const { Title } = Typography;
const { TextArea } = Input;

class AddReview extends React.Component {
  state = {
    visible: false,
    content: '',
    rating: 0,
    loading: false,
  };

  showDrawer = () => {
    this.setState({
      visible: true,
      content: '',
      rating: 0,
      loading: false,
    });
  };

  onClose = () => {
    this.setState({
      visible: false,
    });
  };
  changeRating = (newRating, name) => {
    this.setState({
      rating: newRating,
    });
  };

  onChange = e => {
    this.setState({ content: e.target.value });
  };

  onSubmit = () => {
    const config = this.props.context;
    const { content, rating } = this.state;
    const { uuid } = this.props;
    this.setState({
      loading: true,
    });

    const payload = {
      content: content,
      rating: rating,
    };

    axios
      .post(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.store +
          '/reviews/' +
          uuid,
        payload,
      )
      .then(res => {
        if (res.status === 201) {
          this.setState({
            loading: false,
            visible: false,
          });
          notification.success({
            message: 'Done!',
            description: 'Your review has been posted successfully.',
          });
          this.props.onUpdateReview();
        } else {
          this.setState({
            loading: false,
            visible: false,
          });
          notification.error({
            message: 'There was a problem',
            duration: 0,
            description: 'We are unable to add your review right now.',
          });
        }
      })
      .catch(error => {
        handleApiError(error, 'We are unable to add your review right now.');
        this.setState({
          loading: false,
          visible: false,
        });
      });
  };

  render() {
    return (
      <div>
        <Button type="primary" onClick={this.showDrawer}>
          <Icon type="star" /> Add a review
        </Button>

        <Drawer
          // title="Basic Drawer"
          placement="bottom"
          closable={false}
          onClose={this.onClose}
          visible={this.state.visible}
          height={400}
        >
          <Spin spinning={this.state.loading} tip="Posting your review...">
            <Row>
              <Col lg={8} />
              <Col lg={8}>
                <Title level={4}>Add review</Title>
                <Divider />
                <TextArea
                  placeholder="Tell others what you think about this app. Would you recommend it, and why?"
                  onChange={this.onChange}
                  rows={4}
                  value={this.state.content || ''}
                  style={{ marginBottom: 20 }}
                />
                <StarRatings
                  rating={this.state.rating}
                  changeRating={this.changeRating}
                  starRatedColor="#777"
                  starHoverColor="#444"
                  starDimension="20px"
                  starSpacing="2px"
                  numberOfStars={5}
                  name="rating"
                />
                <br />
                <br />
                <Button onClick={this.onClose} style={{ marginRight: 8 }}>
                  Cancel
                </Button>
                <Button
                  disabled={this.state.rating === 0}
                  onClick={this.onSubmit}
                  type="primary"
                >
                  Submit
                </Button>
              </Col>
            </Row>
          </Spin>
        </Drawer>
      </div>
    );
  }
}

export default withConfigContext(AddReview);
