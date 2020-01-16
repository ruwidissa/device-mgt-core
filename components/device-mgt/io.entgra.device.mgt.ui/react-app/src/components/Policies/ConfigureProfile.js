import React from 'react';
import {
  Tabs,
  Row,
  Col,
  Switch,
  Input,
  Typography,
  Form,
  Checkbox,
  Select,
  Tooltip,
  Icon,
  Alert,
  Upload,
  Button,
  Radio,
} from 'antd';
import { withConfigContext } from '../../context/ConfigContext';
import '../../pages/Dashboard/Policies/policies.css';
import jsonResponse from './configuration';
const { Title, Paragraph } = Typography;
const { TabPane } = Tabs;
const { Option } = Select;
const { TextArea } = Input;

const policyConfigurationsList = jsonResponse.PolicyConfigurations;

class ConfigureProfile extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.policies = policyConfigurationsList.androidPolicy.Policy;
    this.state = {
      isDisplayMain: 'none',
      activeKeys: [],
    };
  }

  componentDidMount() {}

  onChange = e => {
    console.log(`checked = ${e.target.id}`);
  };

  onChecked = (e, i) => {
    if (e) {
      this.setState({
        isDisplayMain: 'block',
      });
    } else {
      this.setState({
        isDisplayMain: 'none',
      });
    }
  };

  onClickSwitch = e => {};

  getPanelItems = panel => {
    const { getFieldDecorator } = this.props.form;
    return panel.map((item, k) => {
      switch (item._type) {
        case 'select':
          return (
            <Form.Item
              key={k}
              label={
                <span>
                  {item.Label}&nbsp;
                  <Tooltip title={item.tooltip} placement="right">
                    <Icon type="question-circle-o" />
                  </Tooltip>
                </span>
              }
              style={{ display: 'block' }}
            >
              {getFieldDecorator(`${item._id}`, {
                initialValue: `${item.Optional.Option[0]}`,
              })(
                <Select>
                  {item.Optional.Option.map(option => {
                    return <Option key={option}>{option}</Option>;
                  })}
                </Select>,
              )}
            </Form.Item>
          );
        case 'input':
          return (
            <Form.Item
              key={k}
              label={
                <span>
                  {item.Label}&nbsp;
                  <Tooltip title={item.tooltip} placement="right">
                    <Icon type="question-circle-o" />
                  </Tooltip>
                </span>
              }
              style={{ display: 'block' }}
            >
              {getFieldDecorator(`${item._id}`, {
                rules: [
                  {
                    pattern: new RegExp(`${item.Optional.rules.regex}`),
                    message: `${item.Optional.rules.validationMsg}`,
                  },
                ],
              })(<Input placeholder={item.Optional.Placeholder} />)}
            </Form.Item>
          );
        case 'checkbox':
          return (
            <Form.Item key={k}>
              {getFieldDecorator(`${item._id}`, {
                valuePropName: 'checked',
                initialValue: `${item.Optional.checked}`,
              })(
                <Checkbox
                  // checked={item.Optional.checked}
                  onChange={this.onChange}
                >
                  <span>
                    {item.Label}&nbsp;
                    <Tooltip title={item.tooltip} placement="right">
                      <Icon type="question-circle-o" />
                    </Tooltip>
                  </span>
                </Checkbox>,
              )}
            </Form.Item>
          );
        case 'textArea':
          return (
            <Form.Item
              key={k}
              label={
                <span>
                  {item.Label}&nbsp;
                  <Tooltip title={item.tooltip} placement="right">
                    <Icon type="question-circle-o" />
                  </Tooltip>
                </span>
              }
              style={{ display: 'block' }}
            >
              {getFieldDecorator(`${item._id}`, {})(
                <TextArea
                  placeholder={item.Optional.Placeholder}
                  rows={item.Optional.Row}
                />,
              )}
            </Form.Item>
          );
        case 'radioGroup':
          return (
            <Form.Item
              key={k}
              label={
                <span>
                  {item.Label}&nbsp;
                  <Tooltip title={item.tooltip} placement="right">
                    <Icon type="question-circle-o" />
                  </Tooltip>
                </span>
              }
              style={{ display: 'block' }}
            >
              {getFieldDecorator(`${item._id}`, {})(
                <Radio.Group>
                  {item.Optional.Radio.map(option => {
                    return (
                      <Radio key={option} value={option}>
                        {option}
                      </Radio>
                    );
                  })}
                </Radio.Group>,
              )}
            </Form.Item>
          );
        case 'title':
          return (
            <Title key={k} level={4}>
              {item.Label}{' '}
            </Title>
          );
        case 'paragraph':
          return (
            <Paragraph key={k} style={{ marginTop: 20 }}>
              {item.Label}{' '}
            </Paragraph>
          );
        case 'alert':
          return (
            <Alert key={k} description={item.Label} type="warning" showIcon />
          );
        case 'upload':
          return (
            <Form.Item
              key={k}
              label={
                <span>
                  {item.Label}&nbsp;
                  <Tooltip title={item.tooltip} placement="right">
                    <Icon type="question-circle-o" />
                  </Tooltip>
                </span>
              }
            >
              {getFieldDecorator('upload', {})(
                <Upload>
                  <Button>
                    <Icon type="upload" /> Click to upload
                  </Button>
                </Upload>,
              )}
            </Form.Item>
          );
        default:
          return null;
      }
    });
  };

  render() {
    return (
      <div>
        <Tabs tabPosition={'left'} size={'large'}>
          {this.policies.map((element, i) => {
            return (
              <TabPane tab={element.Name} key={i}>
                {/* <div style={{ height: 800, overflowY: "scroll"}}>*/}
                {Object.values(element.Panel).map((panel, j) => {
                  return (
                    <div key={j}>
                      <div>
                        <Row>
                          <Col offset={0} span={14}>
                            <Title level={4}>{panel.title} </Title>
                          </Col>
                          <Col offset={8} span={1}>
                            <Switch
                              checkedChildren="ON"
                              unCheckedChildren="OFF"
                              id={i}
                              onClick={this.onClickSwitch}
                              onChange={this.onChecked}
                            />
                          </Col>
                        </Row>
                        <Row>{panel.description}</Row>
                      </div>
                      <div style={{ display: `${this.state.isDisplayMain}` }}>
                        <Form>{this.getPanelItems(panel.PanelItem)}</Form>
                      </div>
                    </div>
                  );
                })}
              </TabPane>
            );
          })}
        </Tabs>
      </div>
    );
  }
}

export default withConfigContext(Form.create()(ConfigureProfile));
