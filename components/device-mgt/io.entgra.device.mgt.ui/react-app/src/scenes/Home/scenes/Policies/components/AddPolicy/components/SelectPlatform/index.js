/*
 * Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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
import axios from 'axios';
import { Card, Col, Icon, message, notification, Row } from 'antd';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';

class SelectPlatform extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      data: [],
      loading: false,
    };
  }

  componentDidMount() {
    this.getDeviceTypes();
  }

  onClickCard = (e, type, formname) => {
    this.props.getPolicyConfigJson(type);
    let deviceType = {
      deviceType: type,
    };
    this.props.getPolicyPayloadData(formname, deviceType);
  };

  // fetch data from api
  getDeviceTypes() {
    this.setState({ loading: true });

    let apiUrl =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      '/device-types';

    // send request to the invokers
    axios
      .get(apiUrl)
      .then(res => {
        if (res.status === 200) {
          this.setState({
            loading: false,
            data: JSON.parse(res.data.data),
          });
        }
      })
      .catch(error => {
        if (error.hasOwnProperty('response') && error.response.status === 401) {
          // todo display a popop with error
          message.error('You are not logged in');
          window.location.href = window.location.origin + '/entgra/login';
        } else {
          notification.error({
            message: 'There was a problem',
            duration: 0,
            description: 'Error occurred while trying to load device types.',
          });
        }

        this.setState({ loading: false });
      });
  }

  render() {
    const { data } = this.state;
    const { Meta } = Card;
    const itemCard = data.map(data => (
      <Col span={5} key={data.id}>
        <a>
          <Card
            size="default"
            style={{ width: 150 }}
            bordered={true}
            onClick={e =>
              this.onClickCard(e, data.name, 'selectedPlatformData')
            }
            cover={
              <Icon
                type={data.name === 'ios' ? 'apple' : data.name}
                key="device-types"
                style={{
                  color: '#ffffff',
                  backgroundColor: '#4b92db',
                  fontSize: '100px',
                  padding: '20px',
                }}
              />
            }
          >
            <Meta title={data.name} />
          </Card>
        </a>
      </Col>
    ));
    return (
      <div>
        <Row gutter={16}>{itemCard}</Row>
      </div>
    );
  }
}

export default withConfigContext(SelectPlatform);
