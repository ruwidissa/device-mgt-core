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
import { List, Spin, Button, Alert } from 'antd';
import './styles.css';

import InfiniteScroll from 'react-infinite-scroller';
import SingleReview from './components/Review';
import axios from 'axios';
import { withConfigContext } from '../../../../../../../../../../../../../../components/context/ConfigContext';
import { handleApiError } from '../../../../../../../../../../../../../../services/utils/errorHandler';

const limit = 5;

class Reviews extends React.Component {
  state = {
    data: [],
    loading: false,
    hasMore: false,
    loadMore: false,
    forbiddenErrors: {
      reviews: false,
    },
  };

  componentDidMount() {
    this.fetchData(0, limit, res => {
      this.setState({
        data: res,
      });
    });
  }

  fetchData = (offset, limit, callback) => {
    const { uuid, type } = this.props;
    const config = this.props.context;

    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.store +
          '/reviews/' +
          type +
          '/' +
          uuid,
        {
          headers: { 'X-Platform': config.serverConfig.platform },
        },
      )
      .then(res => {
        if (res.status === 200) {
          let reviews = res.data.data.data;
          callback(reviews);
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to load reviews.',
          true,
        );
        if (error.hasOwnProperty('response') && error.response.status === 403) {
          const { forbiddenErrors } = this.state;
          forbiddenErrors.reviews = true;
          this.setState({
            forbiddenErrors,
            loading: false,
          });
        } else {
          this.setState({
            loading: false,
          });
        }
      });
  };

  handleInfiniteOnLoad = count => {
    const offset = count * limit;
    let data = this.state.data;
    this.setState({
      loading: true,
    });
    if (data.length > 149) {
      this.setState({
        hasMore: false,
        loading: false,
      });
      return;
    }
    this.fetchData(offset, limit, res => {
      if (res.length > 0) {
        data = data.concat(res);
        this.setState({
          data,
          loading: false,
        });
      } else {
        this.setState({
          hasMore: false,
          loading: false,
        });
      }
    });
  };

  enableLoading = () => {
    this.setState({
      hasMore: true,
      loadMore: true,
    });
  };

  deleteCallback = () => {
    this.fetchData(0, limit, res => {
      this.setState({
        data: res,
      });
    });
    this.props.deleteCallback();
  };

  render() {
    const { loading, hasMore, data, loadMore } = this.state;
    const { uuid } = this.props;
    return (
      <div>
        {this.state.forbiddenErrors.reviews && (
          <Alert
            message="You don't have permission to view reviews."
            type="warning"
            banner
            closable
          />
        )}
        <div className="infinite-container">
          <InfiniteScroll
            initialLoad={false}
            pageStart={0}
            loadMore={this.handleInfiniteOnLoad}
            hasMore={!loading && hasMore}
            useWindow={true}
          >
            <List
              dataSource={data}
              renderItem={item => (
                <List.Item key={item.id}>
                  <SingleReview
                    uuid={uuid}
                    review={item}
                    isDeletable={true}
                    isEditable={false}
                    deleteCallback={this.deleteCallback}
                  />
                </List.Item>
              )}
            >
              {loading && hasMore && (
                <div className="loading-container">
                  <Spin />
                </div>
              )}
            </List>
          </InfiniteScroll>
          {!loadMore && data.length >= limit && (
            <div style={{ textAlign: 'center' }}>
              <Button
                type="dashed"
                htmlType="button"
                onClick={this.enableLoading}
              >
                Read All Reviews
              </Button>
            </div>
          )}
        </div>
      </div>
    );
  }
}

export default withConfigContext(Reviews);
