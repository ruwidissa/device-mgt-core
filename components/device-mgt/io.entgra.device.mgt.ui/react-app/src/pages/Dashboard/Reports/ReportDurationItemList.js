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
import { Icon, Col, Row, Card } from 'antd';

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

  durationItemArray = [
    {
      name: 'Daily Report',
      description: 'Enrollments of today',
      duration: [
        moment().format('YYYY-MM-DD'),
        moment()
          .add(1, 'days')
          .format('YYYY-MM-DD'),
      ],
    },
    {
      name: 'Weekly Report',
      description: 'Enrollments of last 7 days',
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
      description: 'Enrollments of last month',
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

  render() {
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
    return (
      <div>
        <div style={{ borderRadius: 5 }}>
          <Row gutter={16}>{itemStatus}</Row>
        </div>

        <div style={{ borderRadius: 5 }}>
          <Row gutter={16}>{itemEnrollmentsVsUnenrollments}</Row>
        </div>

        <div style={{ borderRadius: 5 }}>
          <Row gutter={16}>{itemEnrollmentType}</Row>
        </div>
      </div>
    );
  }
}

export default ReportDurationItemList;
