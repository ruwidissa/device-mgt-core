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
import '../../../../../../App.css';
import { Skeleton, Typography, Row, Col, Card, Breadcrumb, Icon } from 'antd';
import ReleaseView from './components/ReleaseView';
import axios from 'axios';
import { withConfigContext } from '../../../../../../components/context/ConfigContext';
import { Link } from 'react-router-dom';
import { handleApiError } from '../../../../../../services/utils/errorHandler';

const { Title } = Typography;

class Release extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
    this.state = {
      loading: true,
      app: null,
      uuid: null,
      forbiddenErrors: {
        app: false,
      },
    };
  }

  componentDidMount() {
    const { uuid, deviceType } = this.props.match.params;
    this.fetchData(uuid);
    this.props.changeSelectedMenuItem(deviceType);
  }

  fetchData = uuid => {
    const config = this.props.context;

    // send request to the invoker
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.store +
          '/applications/' +
          uuid,
      )
      .then(res => {
        if (res.status === 200) {
          let app = res.data.data;

          this.setState({
            app: app,
            loading: false,
            uuid: uuid,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to load releases.',
          false,
        );
        if (error.hasOwnProperty('response') && error.response.status === 403) {
          const { forbiddenErrors } = this.state;
          forbiddenErrors.app = true;
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

  render() {
    const { app, loading } = this.state;
    const { deviceType } = this.props.match.params;

    let content = <Title level={3}>No Releases Found</Title>;
    let appName = 'loading...';

    if (app != null && app.applicationReleases.length !== 0) {
      content = <ReleaseView app={app} deviceType={deviceType} />;
      appName = app.name;
    }

    return (
      <div style={{ background: '#f0f2f5', minHeight: 780 }}>
        <Row style={{ padding: 10 }}>
          <Col lg={4}></Col>
          <Col lg={16} md={24} style={{ padding: 3 }}>
            <Breadcrumb style={{ paddingBottom: 16 }}>
              <Breadcrumb.Item>
                <Link to={'/store/' + deviceType}>
                  <Icon type="home" /> {deviceType + ' apps'}{' '}
                </Link>
              </Breadcrumb.Item>
              <Breadcrumb.Item>{appName}</Breadcrumb.Item>
            </Breadcrumb>
            <Card>
              <Skeleton
                loading={loading}
                avatar={{ size: 'large' }}
                active
                paragraph={{ rows: 8 }}
              >
                {content}
              </Skeleton>
            </Card>
          </Col>
        </Row>
      </div>
    );
  }
}

export default withConfigContext(Release);
