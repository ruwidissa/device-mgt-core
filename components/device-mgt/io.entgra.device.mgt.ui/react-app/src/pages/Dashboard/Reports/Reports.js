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
    Icon
} from "antd";
import {Link} from "react-router-dom";
import ReportDeviceTable from "../../../components/Devices/ReportDevicesTable";
import Filter from "../../../components/Reports/Filter";
import DateRangePicker from "../../../components/Reports/DateRangePicker";

const {Paragraph} = Typography;

class Reports extends React.Component {
    routes;

    constructor(props) {
        super(props);
        this.routes = props.routes;
        this.state = {
            paramsObject:{}
        }
    }

    //Get modified value from datepicker and set it to paramsObject
    updateDurationValue = (modifiedFromDate,modifiedToDate) => {
        let tempParamObj = this.state.paramsObject;
        tempParamObj.from = modifiedFromDate;
        tempParamObj.to = modifiedToDate;
        this.setState({paramsObject:tempParamObj});
    }

    //Get modified value from filters and set it to paramsObject
    updateFiltersValue = (modifiedValue,filterType) => {
            let tempParamObj = this.state.paramsObject;
            if(filterType=="Device Status"){
                tempParamObj.status = modifiedValue;
                if(modifiedValue=="ALL" && tempParamObj.status){
                    delete tempParamObj.status;
                }
            }else{
                tempParamObj.ownership = modifiedValue;
                if(modifiedValue=="ALL" && tempParamObj.ownership){
                    delete tempParamObj.ownership;
                }
            }
            this.setState({paramsObject:tempParamObj});
    }

    render() {
        //Arrays for filters
        const statusObj = ['ALL','ACTIVE','INACTIVE','REMOVED'];
        const ownershipObj = ['ALL','BYOD','COPE'];
        
        const params = {...this.state.paramsObject};

        return (
            <div>
                <PageHeader style={{paddingTop: 0}}>
                    <Breadcrumb style={{paddingBottom: 16}}>
                        <Breadcrumb.Item>
                            <Link to="/entgra"><Icon type="home"/> Home</Link>
                        </Breadcrumb.Item>
                        <Breadcrumb.Item>Reports</Breadcrumb.Item>
                    </Breadcrumb>
                    <div className="wrap">
                        <h3>Reports</h3>
                        <Paragraph>
                            To generate a report, select a duration and apply filters
                        </Paragraph>
                        <div style={{paddingBottom:'5px'}}>
                            <table>
                                <tbody>
                                    <tr style={{fontSize:'12px'}}>
                                        <td>Select Duration</td>
                                        <td>Device Status</td>
                                        <td>Device Ownership</td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <DateRangePicker
                                                updateDurationValue={this.updateDurationValue}/>
                                        </td>
                                        <td>
                                            <Filter
                                                updateFiltersValue={this.updateFiltersValue}
                                                dropDownItems={statusObj}
                                                dropDownName={"Device Status"}/>
                                        </td>
                                        <td>
                                            <Filter
                                                updateFiltersValue={this.updateFiltersValue}
                                                dropDownItems={ownershipObj}
                                                dropDownName={"Device Ownership"}/>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                        <div style={{backgroundColor:"#ffffff", borderRadius: 5}}>
                            <ReportDeviceTable paramsObject={params}/>
                        </div>
                    </div>
                </PageHeader>
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 720}}>

                </div>
            </div>
        );
    }
}

export default Reports;
