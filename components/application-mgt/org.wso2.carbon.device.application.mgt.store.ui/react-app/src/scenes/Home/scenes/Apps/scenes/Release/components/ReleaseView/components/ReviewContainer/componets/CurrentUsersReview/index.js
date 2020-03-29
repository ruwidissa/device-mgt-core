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
import { List, Typography, Empty, Alert } from 'antd';
import SingleReview from '../Reviews/components/Review';
import AddReview from './components/AddReview';
import { withConfigContext } from '../../../../../../../../../../../../components/context/ConfigContext';

const { Text } = Typography;

class CurrentUsersReview extends React.Component {
  render() {
    const { uuid, currentUserReviews } = this.props;
    return (
      <div>
        <Text>MY REVIEW</Text>
        {this.props.forbidden && (
          <Alert
            message="You don't have permission to add reviews."
            type="warning"
            banner
            closable
          />
        )}
        {!this.props.forbidden && (
          <div
            style={{
              overflow: 'auto',
              paddingTop: 8,
              paddingLeft: 24,
            }}
          >
            {currentUserReviews.length > 0 && (
              <div>
                <List
                  dataSource={currentUserReviews}
                  renderItem={item => (
                    <List.Item key={item.id}>
                      <SingleReview
                        uuid={uuid}
                        review={item}
                        isDeletable={true}
                        isEditable={true}
                        deleteCallback={this.props.deleteCallback}
                        onUpdateReview={this.props.onUpdateReview}
                        isPersonalReview={true}
                      />
                    </List.Item>
                  )}
                />
              </div>
            )}

            {currentUserReviews.length === 0 && (
              <div>
                <Empty
                  image={Empty.PRESENTED_IMAGE_DEFAULT}
                  imagestyle={{
                    height: 60,
                  }}
                  description={
                    <span>
                      Share your experience with your community by adding a
                      review.
                    </span>
                  }
                >
                  {/* <Button type="primary">Add review</Button>*/}
                  <AddReview
                    uuid={uuid}
                    onUpdateReview={this.props.onUpdateReview}
                  />
                </Empty>
              </div>
            )}
          </div>
        )}
      </div>
    );
  }
}

export default withConfigContext(CurrentUsersReview);
