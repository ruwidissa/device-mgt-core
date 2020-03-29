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
import AppCard from './components/AppCard';
import { Col, Row, Result } from 'antd';
import axios from 'axios';
import { withConfigContext } from '../../../../../../components/context/ConfigContext';
import { handleApiError } from '../../../../../../services/utils/errorHandler';
import InfiniteScroll from 'react-infinite-scroller';

const limit = 30;

class AppList extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      apps: [],
      loading: true,
      hasMore: true,
      loadMore: true,
      forbiddenErrors: {
        apps: false,
      },
      totalAppCount: 0,
    };
  }

  componentDidMount() {
    const { deviceType } = this.props;
    this.props.changeSelectedMenuItem(deviceType);
    this.fetchData(0, 30, res => {
      this.setState({
        apps: res,
        loading: false,
      });
    });
  }

  componentDidUpdate(prevProps, prevState) {
    if (prevProps.deviceType !== this.props.deviceType) {
      const { deviceType } = this.props;
      this.props.changeSelectedMenuItem(deviceType);
      this.fetchData(0, 30, res => {
        this.setState({
          apps: res,
          loading: false,
          hasMore: true,
        });
      });
    }
  }

  fetchData = (offset, limit, callbackFunction) => {
    const { deviceType } = this.props;
    const config = this.props.context;
    const payload = {
      offset,
      limit,
    };
    if (deviceType === 'web-clip') {
      payload.appType = 'WEB_CLIP';
    } else {
      payload.deviceType = deviceType;
    }

    this.setState({
      loading: true,
    });
    // send request to the invoker
    axios
      .post(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.store +
          '/applications/',
        payload,
      )
      .then(res => {
        if (res.status === 200) {
          // todo remove this property check after backend improvement
          let apps = res.data.data.hasOwnProperty('applications')
            ? res.data.data.applications
            : [];
          callbackFunction(apps);
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to load apps.',
          true,
        );
        if (error.hasOwnProperty('response') && error.response.status === 403) {
          const { forbiddenErrors } = this.state;
          forbiddenErrors.apps = true;
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
    let apps = this.state.apps;
    this.setState({
      loading: true,
    });

    this.fetchData(offset, limit, res => {
      if (res.length > 0) {
        apps = apps.concat(res);
        this.setState({
          apps,
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

  render() {
    const { apps, loading, forbiddenErrors, hasMore } = this.state;

    return (
      <div>
        <InfiniteScroll
          key={this.props.deviceType}
          initialLoad={false}
          pageStart={0}
          loadMore={this.handleInfiniteOnLoad}
          hasMore={!loading && hasMore}
          useWindow={true}
        >
          <Row gutter={16}>
            {forbiddenErrors.apps && (
              <Result
                status="403"
                title="403"
                subTitle="You don't have permission to view apps."
              />
            )}
            {!forbiddenErrors.apps && apps.length === 0 && (
              <Result
                status="404"
                title="No apps, yet."
                subTitle="No apps available, yet! When the administration uploads, apps will show up here."
              />
            )}
            {apps.map(app => (
              <Col key={app.id} xs={12} sm={6} md={6} lg={4} xl={3}>
                <AppCard key={app.id} app={app} />
              </Col>
            ))}
          </Row>
        </InfiniteScroll>
      </div>
    );
  }
}

export default withConfigContext(AppList);
