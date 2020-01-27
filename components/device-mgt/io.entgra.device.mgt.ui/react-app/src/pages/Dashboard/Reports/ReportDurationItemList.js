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
import { Icon, Col, Row, Card, PageHeader, Breadcrumb } from 'antd';

import { Link } from 'react-router-dom';
import moment from 'moment';

class ReportDurationItemList extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      reportParams: ['ACTIVE', 'INACTIVE', 'REMOVED'],
      enrollmentsVsUnenrollmentsParams: ['Enrollments', 'Unenrollments'],
      enrollmentTypeParams: ['BYOD', 'COPE'],
    };
  }

  // Array for pre defined date durations
  durationItemArray = [
    {
      name: 'Daily Report',
      description: 'Report of today',
      duration: [
        moment().format('YYYY-MM-DD'),
        moment()
          .add(1, 'days')
          .format('YYYY-MM-DD'),
      ],
    },
    {
      name: 'Weekly Report',
      description: 'Report of last 7 days',
      duration: [
        moment()
          .subtract(6, 'days')
          .format('YYYY-MM-DD'),
        moment()
          .add(1, 'days')
          .format('YYYY-MM-DD'),
      ],
    },
    {
      name: 'Monthly Report',
      description: 'Report of last month',
      duration: [
        moment()
          .subtract(29, 'days')
          .format('YYYY-MM-DD'),
        moment()
          .add(1, 'days')
          .format('YYYY-MM-DD'),
      ],
    },
  ];

  // Map durationItemArray and additional parameters to antd cards
  mapDurationCards = data => {
    return this.durationItemArray.map(item => (
      <Col key={item.name} span={6}>
        <Link
          to={{
            // Path to respective report page
            pathname: '/entgra/reports/policy',
            reportData: {
              duration: item.duration,
              data: data,
            },
          }}
        >
          <Card
            key={item.name}
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
                <b>{item.name}</b>
              </h2>
              <p>{item.description}</p>
            </div>
          </Card>
        </Link>
      </Col>
    ));
  };

  itemAllPolicyCompliance = this.durationItemArray.map(data => (
    <Col key={data.name} span={6}>
      <Link
        to={{
          // Path to respective report page
          pathname: '/entgra/policyreport',
          reportData: {
            duration: data.duration,
          },
        }}
      >
        <Card
          key={data.name}
          bordered={true}
          hoverable={true}
          style={{ borderRadius: 10, marginBottom: 16 }}
        >
          <div align="center">
            <Icon type="desktop" style={{ fontSize: '25px', color: '#08c' }} />
            <h2>
              <b>{data.name}</b>
            </h2>
            <p>{data.description}</p>
          </div>
        </Card>
      </Link>
    </Col>
  ));

  itemPerPolicyCompliance = this.durationItemArray.map(data => (
    <Col key={data.name} span={6}>
      <Link
        to={{
          // Path to respective report page
          pathname: '/entgra/policyreport',
          reportData: {
            duration: data.duration,
            policyId: 6,
          },
        }}
      >
        <Card
          key={data.name}
          bordered={true}
          hoverable={true}
          style={{ borderRadius: 10, marginBottom: 16 }}
        >
          <div align="center">
            <Icon type="desktop" style={{ fontSize: '25px', color: '#08c' }} />
            <h2>
              <b>{data.name}</b>
            </h2>
            <p>{data.description}</p>
          </div>
        </Card>
      </Link>
    </Col>
  ));

  render() {
    const { data } = this.props.location;

    let itemStatus = this.durationItemArray.map(data => (
      <Col key={data.name} span={6}>
        <Link
          to={{
            // Path to respective report page
            pathname: '/entgra/devicestatus',
            reportData: {
              duration: data.duration,
              reportType: data.reportType,
              params: this.state.reportParams,
              paramsType: data.paramsType,
            },
          }}
        >
          <Card
            key={data.name}
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
                <b>{data.name}</b>
              </h2>
              <p>{data.description}</p>
              {/* <p>{data.duration}</p>*/}
            </div>
          </Card>
        </Link>
      </Col>
    ));

    let itemEnrollmentsVsUnenrollments = this.durationItemArray.map(data => (
      <Col key={data.name} span={6}>
        <Link
          to={{
            // Path to respective report page
            pathname: '/entgra/enrollmentsvsunenrollments',
            reportData: {
              duration: data.duration,
              reportType: data.reportType,
              params: this.state.enrollmentsVsUnenrollmentsParams,
              paramsType: data.paramsType,
            },
          }}
        >
          <Card
            key={data.name}
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
                <b>{data.name}</b>
              </h2>
              <p>{data.description}</p>
            </div>
          </Card>
        </Link>
      </Col>
    ));

    let itemEnrollmentType = this.durationItemArray.map(data => (
      <Col key={data.name} span={6}>
        <Link
          to={{
            // Path to respective report page
            pathname: '/entgra/enrollmenttype',
            reportData: {
              duration: data.duration,
              reportType: data.reportType,
              params: this.state.enrollmentTypeParams,
              paramsType: data.paramsType,
            },
          }}
        >
          <Card
            key={data.name}
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
                <b>{data.name}</b>
              </h2>
              <p>{data.description}</p>
            </div>
          </Card>
        </Link>
      </Col>
    ));

    let cardItem = this.itemAllPolicyCompliance;

    switch (data.name) {
      case 'all_policy_compliance_report':
        cardItem = this.itemAllPolicyCompliance;
        // cardItem = this.mapDurationCards({});
        break;
      case 'per_policy_compliance_report':
        cardItem = this.itemPerPolicyCompliance;
        // cardItem = this.mapDurationCards({
        //   policyId: 6,
        // });
        break;
      case 'enrollments_vs_unenrollments_report':
        cardItem = itemEnrollmentsVsUnenrollments;
        break;
      case 'enrollment_status_report':
        cardItem = itemStatus;
        break;
      case 'enrollemt_type_report':
        cardItem = itemEnrollmentType;
        break;
    }

    return (
      <div>
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
                <Row gutter={16}>{cardItem}</Row>
              </div>
            </div>
          </PageHeader>
          <div
            style={{ background: '#f0f2f5', padding: 24, minHeight: 720 }}
          ></div>
        </div>
      </div>
    );
  }
}

export default ReportDurationItemList;
