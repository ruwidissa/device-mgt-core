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
import moment from "moment";
import {Button, Select, message, notification, Tag, Tooltip, Empty, DatePicker} from "antd";
import axios from "axios";
import {withConfigContext} from "../../../context/ConfigContext";
import GeoCustomMap from "../geo-custom-map/GeoCustomMap";
import "./GeoDashboard.css";

class GeoDashboard extends React.Component {

    constructor(props) {
        super(props);
        let start = moment(
                new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate(), 0, 0, 0, 0)
        );
        let end = moment(start)
                .add(1, "days")
                .subtract(1, "seconds");
        this.state = {
            deviceData: [],
            selectedDevice: '',
            locationData: [],
            loading: false,
            start: start,
            end: end,
            buttonTooltip: "Fetch Locations",
        };
    }

    componentDidMount() {
        this.fetchDevices();
        // this.fetchCurrentLocation();
    }

    /**
     * Call back on apply button in the date time picker
     * @param startDate - start date
     * @param endDate - end date
     */
    applyCallback = (dates, dateStrings) => {
        console.log("Apply Callback");
        this.setState({
                          start: dateStrings[0],
                          end: dateStrings[1]
                      });
    };

    /**
     * Api call handle on fetch location date button
     */
    handleApiCall = () => {

        if (this.state.selectedDevice && this.state.start && this.state.end) {
            const toInMills = moment(this.state.end);
            const fromInMills = moment(this.state.start);
            const deviceType = this.state.selectedDevice.type;
            const deviceId = this.state.selectedDevice.deviceIdentifier;
            const config = this.props.context;
            this.setState({loading: true});

            axios.get(window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt
                      + "/devices/" + deviceType + "/" + deviceId + "/location-history?" + "from=" + fromInMills + "&to=" +
                      toInMills,).then(res => {
                if (res.status === 200) {
                    const locationData = JSON.parse(res.data.data);
                    this.setState({
                                      loading: false,
                                      locationData,
                                  });
                }
            }).catch((error) => {
                if (error.hasOwnProperty("response") && error.response.status === 401) {
                    message.error('You are not logged in');
                    window.location.href = window.location.origin + '/entgra/login';
                } else {
                    notification["error"]({
                                              message: "There was a problem",
                                              duration: 0,
                                              description:
                                                      "Error occurred while trying to fetch locations......",
                                          });
                }

                this.setState({loading: false});
                console.log(error);
            });
        } else {
            notification["error"]({
                                      message: "There was a problem",
                                      duration: 0,
                                      description:
                                              "Please provide a date range and a device.",
                                  });
        }
    };

    /**
     * Device dropdown list handler
     * @param e - selected device data
     */
    handleDeviceList = (e) => {
        let selectedDevice = this.state.deviceData[e];
        this.setState({selectedDevice})
    };

    /**
     * render fetch location button
     */
    fetchLocationButton = () => {
        let flag;
        let toolTip = "";
        if (this.state.selectedDevice === "") {
            flag = true;
            toolTip = "Please select a Device";
        }
        return (
                <Tooltip placement="rightBottom" title={toolTip}>
                    <Button disabled={flag}
                            onClick={this.handleApiCall}>
                        Fetch Locations
                    </Button>
                </Tooltip>);

    };

    /**
     * fetches device data to populate the dropdown list
     */
    fetchDevices = () => {
        const config = this.props.context;
        this.setState({loading: true});

        axios.get(
                window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt +
                "/devices?excludeStatus=REMOVED",).then(res => {
            if (res.status === 200) {
                this.setState({
                                  loading: false,
                                  deviceData: res.data.data.devices,
                              });
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                message.error('You are not logged in');
                window.location.href = window.location.origin + '/entgra/login';
            } else {
                notification["error"]({
                                          message: "There was a problem",
                                          duration: 0,
                                          description:
                                                  "Error occurred while trying to load devices.",
                                      });
            }

            this.setState({loading: false});
        });
    };

    /**
     * Geo Dashboard controller
     */
    controllerBar = () => {

        const {RangePicker} = DatePicker;
        let now = new Date();
        let start = moment(
                new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0)
        );
        let end = moment(start)
                .add(1, "days")
                .subtract(1, "seconds");
        let ranges = {
            "Today Only": [moment(start), moment(end)],
            "Yesterday Only": [
                moment(start).subtract(1, "days"),
                moment(end).subtract(1, "days")
            ],
            "3 Days": [moment(start).subtract(3, "days"), moment(end)],
            "5 Days": [moment(start).subtract(5, "days"), moment(end)],
            "1 Week": [moment(start).subtract(7, "days"), moment(end)],
            "2 Weeks": [moment(start).subtract(14, "days"), moment(end)],
            "1 Month": [moment(start).subtract(1, "months"), moment(end)],
        };

        let {deviceData} = this.state;

        return (
                <div className="controllerDiv">
                    <RangePicker
                            ranges={ranges}
                            style={{marginRight: 20}}
                            showTime
                            format="YYYY-MM-DD HH:mm:ss"
                            defaultValue={[this.state.start, this.state.end]}
                            onChange={this.applyCallback}

                    />

                    <Select
                            showSearch
                            style={{width: 220, marginRight: 20}}
                            placeholder="Select a Device"
                            optionFilterProp="children"
                            onChange={this.handleDeviceList}
                            filterOption={(input, option) =>
                                    option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
                            }
                    >
                        {deviceData.map((device, index) =>
                                                <Select.Option key={index} value={index}>
                                                    {device.name + " "}{this.statusTag(device)}
                                                </Select.Option>)}
                    </Select>

                    {this.fetchLocationButton()}
                </div>
        );
    };

    /**
     * Creates color based tags on device status
     * @param device - device object
     */
    statusTag = (device) => {

        const status = device.enrolmentInfo.status.toLowerCase();
        let color = "#f9ca24";
        switch (status) {
            case "active":
                color = "#badc58";
                break;
            case "created":
                color = "#6ab04c";
                break;
            case "inactive":
                color = "#f9ca24";
                break;
            case "blocked":
                color = "#636e72";
                break;
        }

        return <Tag color={color}>{status}</Tag>
    };

    render() {
        const locationData = [...this.state.locationData];

        return (
                <div className="container">
                    {this.controllerBar()}
                    {(locationData.length > 0) ?
                     <GeoCustomMap locationData={locationData}/>
                                               :
                     <Empty/>
                    }
                </div>
        );
    }
}

export default withConfigContext(GeoDashboard);
