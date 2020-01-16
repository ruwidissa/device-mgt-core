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
import { PageHeader, Breadcrumb, Icon, Card } from 'antd';

import { Link } from 'react-router-dom';
import ReportDeviceTable from '../../../components/Devices/ReportDevicesTable';
import PieChart from '../../../components/Reports/Widgets/PieChart';
import { withConfigContext } from '../../../context/ConfigContext';

class EnrollmentTypeReport extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
    const { reportData } = this.props.location;
    this.state = {
      paramsObject: {
        from: reportData.duration[0],
        to: reportData.duration[1],
      },
    };

    console.log(reportData.duration);
  }

  onClickPieChart = value => {
    const chartValue = value.data.point.item;
    let tempParamObj = this.state.paramsObject;

    console.log(chartValue);

    tempParamObj.ownership = chartValue;

    this.setState({ paramsObject: tempParamObj });
  };

  render() {
    const { reportData } = this.props.location;

    const params = { ...this.state.paramsObject };
    return (
      <div>
        <PageHeader style={{ paddingTop: 0 }}>
          <Breadcrumb style={{ paddingBottom: 16 }}>
            <Breadcrumb.Item>
              <Link to="/entgra">
                <Icon type="home" /> Home
              </Link>
            </Breadcrumb.Item>
            <Breadcrumb.Item>Report</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap" style={{ marginBottom: '10px' }}>
            <h3>Summary of enrollments</h3>
          </div>

          <div>
            <Card
              bordered={true}
              hoverable={true}
              style={{
                borderRadius: 5,
                marginBottom: 10,
                height: window.innerHeight * 0.5,
              }}
            >
              <PieChart
                onClickPieChart={this.onClickPieChart}
                reportData={reportData}
              />
            </Card>
          </div>

          <div style={{ backgroundColor: '#ffffff', borderRadius: 5 }}>
            <ReportDeviceTable paramsObject={params} />
          </div>
        </PageHeader>
        <div
          style={{ background: '#f0f2f5', padding: 24, minHeight: 720 }}
        ></div>
      </div>
    );
  }
}

export default withConfigContext(EnrollmentTypeReport);
