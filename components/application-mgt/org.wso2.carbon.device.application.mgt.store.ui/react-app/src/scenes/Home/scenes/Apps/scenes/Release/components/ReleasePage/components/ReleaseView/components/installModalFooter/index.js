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
import { Button, DatePicker, Checkbox } from 'antd';

class InstallModalFooter extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      scheduledTime: null,
      isScheduledInstallVisible: false,
    };
  }

  onDateTimeChange = (value, dateString) => {
    this.setState({
      scheduledTime: dateString,
    });
  };

  toggleScheduledInstall = () => {
    this.setState({
      isScheduledInstallVisible: !this.state.isScheduledInstallVisible,
    });
  };

  triggerInstallOperation = () => {
    const { scheduledTime, isScheduledInstallVisible } = this.state;
    if (isScheduledInstallVisible && scheduledTime != null) {
      this.props.operation(scheduledTime);
    } else {
      this.props.operation();
    }
  };

  render() {
    const { scheduledTime, isScheduledInstallVisible } = this.state;
    const { disabled, type } = this.props;
    return (
      <div>
        <div
          style={{
            textAlign: 'right',
          }}
        >
          <div style={{ margin: 8 }}>
            <Checkbox
              checked={this.state.isScheduledInstallVisible}
              onChange={this.toggleScheduledInstall}
            >
              Schedule {type}
            </Checkbox>
          </div>
          <span
            style={{
              display: isScheduledInstallVisible ? 'inline' : 'none',
            }}
          >
            <DatePicker
              showTime
              placeholder="Select Time"
              format="YYYY-MM-DDTHH:mm"
              onChange={this.onDateTimeChange}
            />
          </span>
          <Button
            style={{ margin: 5 }}
            disabled={
              disabled || (isScheduledInstallVisible && scheduledTime == null)
            }
            htmlType="button"
            type="primary"
            onClick={this.triggerInstallOperation}
          >
            {type}
          </Button>
        </div>
      </div>
    );
  }
}

export default InstallModalFooter;
