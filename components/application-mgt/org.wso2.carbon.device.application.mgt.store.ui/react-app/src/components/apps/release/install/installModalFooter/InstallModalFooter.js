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

import React from "react";
import {Button, DatePicker} from "antd";

class InstallModalFooter extends React.Component{
    constructor(props) {
        super(props);
        this.state={
            scheduledTime: null,
            isScheduledInstallVisible: false
        }
    }

    onDateTimeChange = (value, dateString) => {
        this.setState({
            scheduledTime: dateString
        });
    };

    showScheduledInstall = ()=>{
        this.setState({
            isScheduledInstallVisible: true
        })
    };

    hideScheduledInstall = ()=>{
        this.setState({
            isScheduledInstallVisible: false
        })
    };

    triggerInstallOperation = () =>{
        this.props.operation();
    };
    triggerScheduledInstallOperation = () =>{
        const {scheduledTime} =this.state;
        this.props.operation(scheduledTime);
    };

    render() {
        const {scheduledTime,isScheduledInstallVisible} =this.state;
        const {disabled, type} = this.props;
        return (
          <div>
              <div style={{
                  textAlign: "right",
                  display: (!isScheduledInstallVisible)?'block':'none'
              }}>
                  <Button style={{margin: 5}} disabled={disabled} htmlType="button" type="primary"
                          onClick={this.triggerInstallOperation}>
                      {type}
                  </Button>
                  <Button style={{margin: 5}} disabled={disabled} htmlType="button"
                          onClick={this.showScheduledInstall}>
                      Scheduled {type}
                  </Button>
              </div>
              <div style={{
                  textAlign: "right",
                  display: (isScheduledInstallVisible)?'block':'none'
              }}>
                  <DatePicker showTime
                              placeholder="Select Time"
                              format="YYYY-MM-DDTHH:mm"
                              onChange={this.onDateTimeChange}/>
                  <Button disabled={scheduledTime == null}
                          style={{margin: 5}}
                          htmlType="button"
                          type="primary"
                          onClick={this.triggerScheduledInstallOperation}>
                      Schedule
                  </Button>
                  <Button style={{margin: 5}} htmlType="button"
                          onClick={this.hideScheduledInstall}>
                      Cancel
                  </Button>
              </div>
          </div>
        );
    }
}

export default InstallModalFooter;