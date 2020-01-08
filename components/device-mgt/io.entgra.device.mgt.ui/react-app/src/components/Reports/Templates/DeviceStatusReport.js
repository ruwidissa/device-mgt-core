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
import {
    PageHeader,
    Typography,
    Breadcrumb,
    Icon,
    Tag,
    Radio, Select, Button, Card,
    Row, Col, message, notification
} from "antd";

import {Link} from "react-router-dom";
import PoliciesTable from "../../../components/Policies/PoliciesTable";
import DevicesTable from "../../../components/Devices/DevicesTable";
import DateRangePicker from "../../../components/Reports/DateRangePicker";
import ReportDeviceTable from "../../../components/Devices/ReportDevicesTable";
import PieChart from "../../../components/Reports/Widgets/PieChart";
import axios from "axios";
import CountWidget from "../../../components/Reports/Widgets/CountWidget";
import {withConfigContext} from "../../../context/ConfigContext";
const {Paragraph} = Typography;
const { CheckableTag } = Tag;

const { Option } = Select;
let config = null;


class DeviceStatusReport extends React.Component {
    routes;

    constructor(props) {
        super(props);
        this.routes = props.routes;
        config =  this.props.context;
        const { reportData } = this.props.location;
        this.state = {
            selectedTags: ['Enrolled'],
            paramsObject:{
                from:reportData.duration[0],
                to:reportData.duration[1]
            },
            statsObject:{},
            statArray:[{item:"ACTIVE",count:0},{item:"INACTIVE",count:0},{item:"REMOVED",count:0}]
        };
    }

    onClickPieChart = (value) => {
        console.log(value.data.point.item);
        const chartValue = value.data.point.item;
        let tempParamObj = this.state.paramsObject;

        tempParamObj.status = chartValue;


        this.setState({paramsObject:tempParamObj});
        console.log(this.state.paramsObject)
    };

    render() {
        const { statArray } = this.state;
        const { reportData } = this.props.location;

        const params = {...this.state.paramsObject};
        return (
            <div>
                <PageHeader style={{paddingTop: 0}}>
                    <Breadcrumb style={{paddingBottom: 16}}>
                        <Breadcrumb.Item>
                            <Link to="/entgra"><Icon type="home"/> Home</Link>
                        </Breadcrumb.Item>
                        <Breadcrumb.Item>Report</Breadcrumb.Item>
                    </Breadcrumb>
                    <div className="wrap" style={{marginBottom: '10px'}}>
                        <h3>Summary of enrollments</h3>
                        <div style={{marginBottom: '10px'}}>
                            <Select defaultValue="android" style={{ width: 120 , marginRight:10}}>
                                <Option value="android">Android</Option>
                                <Option value="ios">IOS</Option>
                                <Option value="windows">Windows</Option>
                            </Select>
                            <Button onClick={this.onSubmitReport} style={{marginLeft:10}} type="primary">Generate Report</Button>
                        </div>
                    </div>

                    <div>
                        <Card
                            bordered={true}
                            hoverable={true}
                            style={{borderRadius: 5, marginBottom: 10, height:window.innerHeight*0.5}}>

                            <PieChart onClickPieChart={this.onClickPieChart} reportData={reportData}/>
                        </Card>
                    </div>

                    <div style={{backgroundColor:"#ffffff", borderRadius: 5}}>
                        <ReportDeviceTable paramsObject={params}/>
                    </div>
                </PageHeader>
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 720}}>

                </div>
            </div>
        );
    }
}

export default withConfigContext(DeviceStatusReport);
