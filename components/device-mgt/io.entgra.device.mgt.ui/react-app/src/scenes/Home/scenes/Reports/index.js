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
import { PageHeader, Breadcrumb, Icon, Row, Col, Card } from 'antd';
import { Link } from 'react-router-dom';

class Reports extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
    this.state = {
      paramsObject: {},
    };
  }
  // Get modified value from datepicker and set it to paramsObject
  updateDurationValue = (modifiedFromDate, modifiedToDate) => {
    let tempParamObj = this.state.paramsObject;
    tempParamObj.from = modifiedFromDate;
    tempParamObj.to = modifiedToDate;
    this.setState({ paramsObject: tempParamObj });
  };

  // Get modified value from filters and set it to paramsObject
  updateFiltersValue = (modifiedValue, filterType) => {
    let tempParamObj = this.state.paramsObject;
    if (filterType == 'Device Status') {
      tempParamObj.status = modifiedValue;
      if (modifiedValue == 'ALL' && tempParamObj.status) {
        delete tempParamObj.status;
      }
    } else {
      tempParamObj.ownership = modifiedValue;
      if (modifiedValue == 'ALL' && tempParamObj.ownership) {
        delete tempParamObj.ownership;
      }
    }
    this.setState({ paramsObject: tempParamObj });
  };

  render() {
    return (
      <div>
        <PageHeader style={{ paddingTop: 0 }}>
          <Breadcrumb style={{ paddingBottom: 16 }}>
            <Breadcrumb.Item>
              <Link to="/entgra">
                <Icon type="home" /> Home
              </Link>
            </Breadcrumb.Item>
            <Breadcrumb.Item>Reports</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap">
            <h3>Reports</h3>
            <div style={{ borderRadius: 5 }}>
              <Row gutter={16}>
                <Col span={8}>
                  <Link
                    to={{
                      // Path to respective report page
                      pathname: '/entgra/reports/policy/compliance',
                      data: {
                        name: 'all_policy_compliance_report',
                      },
                    }}
                  >
                    <Card
                      bordered={true}
                      hoverable={true}
                      style={{ borderRadius: 10, marginBottom: 16 }}
                    >
                      <div align="center">
                        <Icon
                          type="desktop"
                          style={{ fontSize: '25px', color: '#08c' }}
                        />
                        <h2>
                          <b>Policy Compliance Report</b>
                        </h2>
                        <p>Policy compliance details of all enrolled devices</p>
                      </div>
                    </Card>
                  </Link>
                </Col>
                <Col span={8}>
                  <Link
                    to={{
                      // Path to respective report page
                      pathname: '/entgra/reports/enrollments',
                      data: {
                        name: 'enrollments_vs_unenrollments_report',
                      },
                    }}
                  >
                    <Card
                      bordered={true}
                      hoverable={true}
                      style={{ borderRadius: 10, marginBottom: 16 }}
                    >
                      <div align="center">
                        <Icon
                          type="desktop"
                          style={{ fontSize: '25px', color: '#08c' }}
                        />
                        <h2>
                          <b>Enrollments vs Unenrollments</b>
                        </h2>
                        <p>Details on device enrollments vs unenrollments</p>
                      </div>
                    </Card>
                  </Link>
                </Col>

                <Col span={8}>
                  <Link
                    to={{
                      // Path to respective report page
                      pathname: '/entgra/reports/device-status',
                      data: {
                        name: 'enrollment_status_report',
                      },
                    }}
                  >
                    <Card
                      bordered={true}
                      hoverable={true}
                      style={{ borderRadius: 10, marginBottom: 16 }}
                    >
                      <div align="center">
                        <Icon
                          type="desktop"
                          style={{ fontSize: '25px', color: '#08c' }}
                        />
                        <h2>
                          <b>Device Status Report</b>
                        </h2>
                        <p>Report based on device status</p>
                      </div>
                    </Card>
                  </Link>
                </Col>

                <Col span={8}>
                  <Link
                    to={{
                      // Path to respective report page
                      pathname: '/entgra/reports/expired-devices',
                      data: {
                        name: 'expired_devices_report',
                      },
                    }}
                  >
                    <Card
                      bordered={true}
                      hoverable={true}
                      style={{ borderRadius: 10, marginBottom: 16 }}
                    >
                      <div align="center">
                        <Icon
                          type="desktop"
                          style={{ fontSize: '25px', color: '#08c' }}
                        />
                        <h2>
                          <b>Outdated OS Version Report</b>
                        </h2>
                        <p>Report based on device OS version</p>
                      </div>
                    </Card>
                  </Link>
                </Col>

                <Col span={8}>
                  <Link
                    to={{
                      // Path to respective report page
                      pathname: '/entgra/reports/enrollment-type',
                      data: {
                        name: 'enrollemt_type_report',
                      },
                    }}
                  >
                    <Card
                      bordered={true}
                      hoverable={true}
                      style={{ borderRadius: 10, marginBottom: 16 }}
                    >
                      <div align="center">
                        <Icon
                          type="desktop"
                          style={{ fontSize: '25px', color: '#08c' }}
                        />
                        <h2>
                          <b>Device Type Report</b>
                        </h2>
                        <p>Report for all device types</p>
                      </div>
                    </Card>
                  </Link>
                </Col>

                <Col span={8}>
                  <Link
                    to={{
                      // Path to respective report page
                      pathname: '/entgra/reports/app-not-installed',
                      data: {
                        name: 'app_not_installed_devices_report',
                      },
                    }}
                  >
                    <Card
                      bordered={true}
                      hoverable={true}
                      style={{ borderRadius: 10, marginBottom: 16 }}
                    >
                      <div align="center">
                        <Icon
                          type="desktop"
                          style={{ fontSize: '25px', color: '#08c' }}
                        />
                        <h2>
                          <b>App NOT Installed Devices Report</b>
                        </h2>
                        <p>Report for all device types</p>
                      </div>
                    </Card>
                  </Link>
                </Col>
              </Row>
            </div>
          </div>
        </PageHeader>
        <div
          style={{ background: '#f0f2f5', padding: 24, minHeight: 720 }}
        ></div>
      </div>
    );
  }
}

export default Reports;
