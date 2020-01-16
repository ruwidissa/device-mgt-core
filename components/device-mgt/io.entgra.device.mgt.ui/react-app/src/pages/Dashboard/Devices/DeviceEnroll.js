import React from 'react';
import { PageHeader, Typography, Breadcrumb, Icon } from 'antd';
import { Link } from 'react-router-dom';
import AddDevice from '../../../components/Devices/AddDevice';

const { Paragraph } = Typography;

class DeviceEnroll extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
  }

  render() {
    return (
      <div>
        <PageHeader style={{ paddingTop: 0 }}>
          <Breadcrumb style={{ paddingBottom: 16 }}>
            <Breadcrumb.Item>
              <Link to="/entgra/devices">
                <Icon type="home" /> Home
              </Link>
            </Breadcrumb.Item>
            <Breadcrumb.Item>
              <Link to="/entgra/devices">Devices</Link>
            </Breadcrumb.Item>
            <Breadcrumb.Item>Enroll Device</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap">
            <h3>Devices</h3>
            <Paragraph>All enrolled devices</Paragraph>
          </div>
          <div style={{ borderRadius: 5 }}>
            <AddDevice />
          </div>
        </PageHeader>
        <div
          style={{ background: '#f0f2f5', padding: 24, minHeight: 720 }}
        ></div>
      </div>
    );
  }
}

export default DeviceEnroll;
