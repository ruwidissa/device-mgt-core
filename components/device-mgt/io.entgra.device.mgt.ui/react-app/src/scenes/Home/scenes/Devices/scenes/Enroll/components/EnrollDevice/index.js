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
import {
  Divider,
  message,
  notification,
  Select,
  Card,
  Spin,
  Button,
  Row,
  Col,
} from 'antd';
import TimeAgo from 'javascript-time-ago/modules/JavascriptTimeAgo';
import en from 'javascript-time-ago/locale/en';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import axios from 'axios';
import QRCode from 'qrcode.react';
import QRPlaceholder from '../../../../../../../../../public/images/qr-code.png';
import installAgent from '../../../../../../../../../public/images/install_agent.png';
import register from '../../../../../../../../../public/images/register.png';
import registration from '../../../../../../../../../public/images/registration.png';
import setProfile from '../../../../../../../../../public/images/set_profile.png';

const { Option } = Select;

class EnrollDevice extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    TimeAgo.addLocale(en);
    this.state = {
      isSelected: false,
      loading: false,
      payload: {},
    };
  }

  // fetch data from api
  generateQRCode = ownershipType => {
    const { deviceType } = this.props;
    this.setState({ loading: true });

    let apiUrl =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      '/device-mgt/' +
      deviceType +
      '/v1.0/configuration/enrollment-qr-config/' +
      ownershipType;

    // send request to the invokerss
    axios
      .get(apiUrl)
      .then(res => {
        if (res.status === 200) {
          this.setState({
            loading: false,
            payload: res.data.data,
            isSelected: true,
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
            description: 'Error occurred while trying to get QR code payload.',
          });
        }

        this.setState({ loading: false });
      });
  };

  onChange = value => {
    this.generateQRCode(value);
  };

  onClick = () => {
    this.props.goBack();
  };

  render() {
    const { payload, isSelected, loading } = this.state;
    const { enrollmentType } = this.props;
    return (
      <div style={{ textAlign: 'center', fontSize: 12 }}>
        <div
          style={{
            display: enrollmentType === 'manual' ? 'inline-block' : 'none',
          }}
        >
          <Row gutter={[16, 16]}>
            <Col span={6}>
              <h1>Step 1</h1>
              <p>
                {/* eslint-disable-next-line react/no-unescaped-entities */}
                Let's start by installing the Android agent on your device. Open
                the downloaded file, and tap INSTALL.
              </p>
              <img src={installAgent} />
            </Col>
            <Col span={6}>
              <h1>Step 2</h1>
              <p>Tap Skip to proceed with the default enrollment process.</p>
              <img src={setProfile} />
            </Col>
            <Col span={6}>
              <h1>Step 3</h1>
              <p>
                Enter the server address based on your environment, in the text
                box provided.
              </p>
              <img src={registration} />
            </Col>
            <Col span={6}>
              <h1>Step 4</h1>
              <p>Enter your:</p>
              <div>
                <p>Organization: carbon.super</p>
                <p>Username: admin</p>
                <p>Password: Your password</p>
              </div>
              <img src={register} />
            </Col>
          </Row>
        </div>

        <div
          style={{
            display: enrollmentType === 'qr' ? 'inline-block' : 'none',
          }}
        >
          <Divider>Generate QR code to QR based Provisioning.</Divider>
          <div style={{ textAlign: 'center' }}>
            <div style={{ marginBottom: 10 }}>
              <Select
                showSearch
                style={{ width: 200, textAlign: 'center' }}
                placeholder="Select device ownership"
                optionFilterProp="children"
                onChange={this.onChange}
                filterOption={(input, option) =>
                  option.props.children
                    .toLowerCase()
                    .indexOf(input.toLowerCase()) >= 0
                }
              >
                <Option key={'byod'} value={'BYOD'}>
                  {'BYOD'}
                </Option>
                <Option key={'cope'} value={'COPE'}>
                  {'COPE'}
                </Option>
                <Option key={'COSU'} value={'COSU'}>
                  {'COSU (KIOSK)'}
                </Option>
                <Option key={'WORK PROFILE'} value={'WORK_PROFILE'}>
                  {'WORK PROFILE'}
                </Option>
                <Option key={'GOOGLE_ENTERPRISE'} value={'GOOGLE_ENTERPRISE'}>
                  {'GOOGLE_WORK_PROFILE'}
                </Option>
              </Select>
            </div>
            <Spin spinning={loading}>
              <div
                style={{
                  display: isSelected ? 'inline-block' : 'none',
                }}
              >
                <Card hoverable>
                  <QRCode size={300} value={JSON.stringify(payload)} />
                </Card>
              </div>
              <div style={{ display: !isSelected ? 'inline-block' : 'none' }}>
                <Card hoverable>
                  <img src={QRPlaceholder} />
                </Card>
              </div>
            </Spin>
          </div>
        </div>
        <div style={{ textAlign: 'right', marginTop: 10 }}>
          <Button type="default" size={'default'} onClick={this.onClick}>
            Back
          </Button>
        </div>
      </div>
    );
  }
}

export default withConfigContext(EnrollDevice);
