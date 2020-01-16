import React from 'react';
import { Button, Form, Row, Col, Card, Steps } from 'antd';
import { withConfigContext } from '../../context/ConfigContext';
import SelectPlatform from './SelectPlatform';
import ConfigureProfile from './ConfigureProfile';
const { Step } = Steps;

class AddPolicy extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      isAddDeviceModalVisible: false,
      current: 0,
    };
  }

  onClickType = () => {
    this.setState({
      current: 1,
    });
  };

  next() {
    const current = this.state.current + 1;
    this.setState({ current });
  }

  prev() {
    const current = this.state.current - 1;
    this.setState({ current });
  }

  render() {
    const { current } = this.state;
    return (
      <div>
        <Row>
          <Col span={20} offset={2}>
            <Steps style={{ minHeight: 32 }} current={current}>
              <Step key="Platform" title="Select a Platform" />
              <Step key="ProfileConfigure" title="Configure profile" />
              <Step key="PolicyType" title="Select policy type" />
              <Step key="AssignGroups" title="Assign to groups" />
              <Step key="Publish" title="Publish to devices" />
              <Step key="Result" title="Result" />
            </Steps>
          </Col>
          <Col span={16} offset={4}>
            <Card style={{ marginTop: 24 }}>
              <div style={{ display: current === 0 ? 'unset' : 'none' }}>
                <SelectPlatform onClickType={this.onClickType} />
              </div>
              <div style={{ display: current === 1 ? 'unset' : 'none' }}>
                <ConfigureProfile />
              </div>
              <div style={{ display: current === 2 ? 'unset' : 'none' }}></div>
              <div style={{ display: current === 3 ? 'unset' : 'none' }}></div>
              <div style={{ display: current === 4 ? 'unset' : 'none' }}></div>
              <div style={{ display: current === 5 ? 'unset' : 'none' }}></div>
            </Card>
          </Col>
          <Col span={16} offset={4}>
            <div style={{ marginTop: 24 }}>
              {current > 0 && (
                <Button style={{ marginRight: 8 }} onClick={() => this.prev()}>
                  Previous
                </Button>
              )}
              {current < 5 && current > 0 && (
                <Button type="primary" onClick={() => this.next()}>
                  Next
                </Button>
              )}
              {current === 5 && <Button type="primary">Done</Button>}
            </div>
          </Col>
        </Row>
      </div>
    );
  }
}

export default withConfigContext(
  Form.create({ name: 'add-policy' })(AddPolicy),
);
