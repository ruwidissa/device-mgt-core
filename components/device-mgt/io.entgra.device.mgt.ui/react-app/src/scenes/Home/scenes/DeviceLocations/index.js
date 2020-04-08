import React from 'react';
import { Breadcrumb, PageHeader } from 'antd';
import DeviceLocationMap from './Component/DeviceLocationMap';

class DeviceLocations extends React.Component {
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
            <Breadcrumb.Item>Devices Location</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap">
            <h3>Devices Location </h3>
          </div>
        </PageHeader>
        <div
          style={{
            background: '#f0f2f5',
            padding: 24,
            minHeight: 720,
            alignItems: 'center',
          }}
        >
          <DeviceLocationMap />
        </div>
      </div>
    );
  }
}

export default DeviceLocations;
