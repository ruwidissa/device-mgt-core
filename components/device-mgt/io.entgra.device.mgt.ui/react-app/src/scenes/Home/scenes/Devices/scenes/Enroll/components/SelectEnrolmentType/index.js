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
import { Button, Divider } from 'antd';
import TimeAgo from 'javascript-time-ago/modules/JavascriptTimeAgo';
import en from 'javascript-time-ago/locale/en';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';

class SelectEnrollmentType extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    TimeAgo.addLocale(en);
    this.state = {
      pagination: {},
      loading: false,
      selectedRows: [],
    };
  }

  onEnrollWithQR = () => {
    this.props.getEnrollmentData('qr');
  };

  onEnrollManually = () => {
    this.props.getEnrollmentData('manual');
  };

  onClick = () => {
    this.props.goBack();
  };

  render() {
    return (
      <div>
        <Divider orientation="left">
          Step 02 - Enroll the Android Agent.
        </Divider>

        <div>
          <p>
            {' '}
            Your device can be enrolled with Entgra IoTS automatically via QR
            code. To enroll first download agent as mentioned in Step 1 then
            proceed with the ENROLL WITH QR option from the device setup
            activity. Thereafter select the ownership configuration and scan the
            generated QR to complete the process.
          </p>
        </div>
        <div style={{ margin: '30px' }}>
          <Button
            type="primary"
            size={'default'}
            onClick={this.onEnrollWithQR}
            style={{ marginRight: 10 }}
          >
            Enroll Using QR
          </Button>

          <Button
            type="primary"
            size={'default'}
            onClick={this.onEnrollManually}
          >
            Enroll Manually
          </Button>
        </div>
        <div style={{ textAlign: 'right' }}>
          <Button type="default" size={'default'} onClick={this.onClick}>
            Back
          </Button>
        </div>
      </div>
    );
  }
}

export default withConfigContext(SelectEnrollmentType);
