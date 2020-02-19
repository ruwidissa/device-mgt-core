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
import { Button, Card, Divider, message, notification } from 'antd';
import TimeAgo from 'javascript-time-ago/modules/JavascriptTimeAgo';
import en from 'javascript-time-ago/locale/en';
import axios from 'axios';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import QRCode from 'qrcode.react';

class DownloadAgent extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    TimeAgo.addLocale(en);
    this.state = {
      pagination: {},
      loading: false,
      selectedRows: [],
      buttonTitle: 'Download Agent',
      skipButtonTitle: 'Skip',
    };
  }

  onClickSkip = () => {
    this.props.goNext();
  };

  onClickGoBack = () => {
    this.props.goBack();
  };

  onClickDownloadAgent = () => {
    this.downloadAgent();
  };

  // fetch data from api
  downloadAgent = () => {
    const { deviceType } = this.props;
    this.setState({ loading: true, buttonTitle: 'Downloading..' });

    const apiUrl =
      window.location.origin +
      '/api/application-mgt/v1.0/artifact/' +
      deviceType +
      '/agent/-1234';

    // send request to the invokerss
    axios
      .get(apiUrl)
      .then(res => {
        if (res.status === 200) {
          // Download file in same window
          const url = window.URL.createObjectURL(new Blob([res.data]));
          const link = document.createElement('a');
          link.href = url;
          link.setAttribute('download', 'android-agent.apk'); // or any other extension
          document.body.appendChild(link);
          link.click();
          this.setState({
            loading: false,
            buttonTitle: 'Download Agent',
            skipButtonTitle: 'Next',
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
            description:
              'Error occurred while trying to download Entgra Android Agent',
          });
        }

        this.setState({ loading: false });
      });
  };

  render() {
    const { loading, buttonTitle, skipButtonTitle } = this.state;
    const { deviceType } = this.props;

    const apiUrl =
      window.location.origin +
      '/api/application-mgt/v1.0/artifact/' +
      deviceType +
      '/agent/-1234';
    return (
      <div>
        <Divider orientation="left">Step 01 - Get your Android Agent.</Divider>
        <div>
          <p>
            The Android agent can be downloaded by using following QR. The
            generated QR code can be scanned, and the agent APK downloaded from
            the link, and transferred to the device and then installed.
          </p>
        </div>
        <div style={{ margin: '30px', textAlign: 'center' }}>
          <Divider>Scan to get the Android Agent.</Divider>
          <div
            style={{
              marginBottm: 10,
              display: 'inline-block',
            }}
          >
            <Card hoverable>
              <QRCode size={200} value={apiUrl} />
            </Card>
          </div>
          <Divider>OR</Divider>
          <div style={{ textAlign: 'center', marginTop: 10, marginBottom: 10 }}>
            <Button
              type="primary"
              size={'default'}
              onClick={this.onClickDownloadAgent}
              loading={loading}
            >
              {buttonTitle}
            </Button>
          </div>
          <p>
            Need help? Read&nbsp;
            <a
              href={
                'https://entgra-documentation.gitlab.io/v3.8.0/docs/guide-to-work-with-the-product/' +
                'enrollment-guide/enroll-android/'
              }
            >
              Entgra IoT Server documentation.
            </a>
          </p>
        </div>
        <div style={{ textAlign: 'right' }}>
          <Button type="primary" size={'default'} onClick={this.onClickSkip}>
            {skipButtonTitle}
          </Button>
          <Button
            style={{ marginLeft: 10 }}
            type="default"
            size={'default'}
            onClick={this.onClickGoBack}
          >
            Back
          </Button>
        </div>
      </div>
    );
  }
}

export default withConfigContext(DownloadAgent);
