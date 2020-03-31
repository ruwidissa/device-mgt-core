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
import {
  Alert,
  Button,
  Checkbox,
  Col,
  Collapse,
  Form,
  Icon,
  Input,
  Popconfirm,
  Radio,
  Row,
  Select,
  Table,
  Tabs,
  Tooltip,
  Typography,
  Upload,
} from 'antd';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import moment from 'moment';

const { Text, Title, Paragraph } = Typography;
const { TabPane } = Tabs;
const { Option } = Select;
const { TextArea } = Input;

const subPanelpayloadAttributes = {};
const fieldKeys = [];
let subFormContainer = {};

class PolicyInfo extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      data: {},
      policyFeatureList: [],
      activePanelKeys: [],
      activeSubPanelKeys: [],
      profilePreviewKey: '',
      customInputDataArray: [],
      inputTableDataSources: {},
      isInfoPreview: false,
    };
  }

  setProfileInfo = e => {
    let activePolicies = [];
    let activePolicyFields = {};
    let activeSubPanels = [];
    const allFields = this.props.form.getFieldsValue();
    this.props.policyFeatureList.map(element => {
      activePolicies.push(element.featureCode);
      let featureData = JSON.parse(element.content);
      Object.keys(featureData).map(key => {
        if (element.featureCode in subPanelpayloadAttributes) {
          Object.entries(subPanelpayloadAttributes[element.featureCode]).map(
            ([panelKey, payloadAttr]) => {
              if (key === payloadAttr) {
                activeSubPanels.push(`${element.featureCode}-${panelKey}`);
              }
            },
          );

          let regex = new RegExp(`${element.featureCode}.+${key}`, 'g');
          Object.keys(allFields).map(fieldName => {
            if (fieldName.match(regex) != null) {
              activePolicyFields[fieldName] = featureData[key];
            }
          });
        } else if (element.featureCode in subFormContainer) {
          let regex = new RegExp(`.+${element.featureCode}-${key}`, 'g');
          Object.keys(allFields).map(fieldName => {
            if (fieldName.match(regex) != null) {
              activePolicyFields[fieldName] = featureData[key];
              if (
                !activePolicies.includes(
                  fieldName.replace(`-${element.featureCode}-${key}`, ''),
                )
              ) {
                activePolicies.push(
                  fieldName.replace(`-${element.featureCode}-${key}`, ''),
                );
              }
            }
          });
        } else {
          let regex = new RegExp(`${element.featureCode}.+${key}`, 'g');
          Object.keys(allFields).map(fieldName => {
            if (fieldName.match(regex) != null) {
              activePolicyFields[fieldName] = featureData[key];
            }
          });
        }
      });
    });
    this.props.form.setFieldsValue(activePolicyFields);
    this.setState({
      activePanelKeys: activePolicies,
      activeSubPanelKeys: activeSubPanels,
    });
  };

  // convert time from 24h format to 12h format
  timeConverter = time => {
    time = time
      .toString()
      .match(/^([01]\d|2[0-3])(:)([0-5]\d)(:[0-5]\d)?$/) || [time];
    if (time.length > 1) {
      time = time.slice(1);
      time[5] = +time[0] < 12 ? ' AM' : ' PM';
      time[0] = +time[0] % 12 || 12;
    }
    return time.join('');
  };

  // get Option value from start Time, end Time and time difference between 2 values
  getOptionForTimeSelectors = (startTimeValue, endTimeValue, timeIncrement) => {
    let timeOptions = [];
    let time = new Date(
      moment()
        .startOf('day')
        .format('YYYY/MM/DD'),
    );
    let tempValue = startTimeValue;
    time.setMinutes(time.getMinutes() + tempValue);
    let startOption = (
      <Option value={String(tempValue)}>
        {this.timeConverter(
          `${String(time)
            .split(' ')[4]
            .substring(0, 5)}`,
        )}
      </Option>
    );
    timeOptions.push(startOption);

    while (tempValue !== endTimeValue) {
      time = new Date(
        moment()
          .startOf('day')
          .format('YYYY/MM/DD'),
      );
      tempValue += timeIncrement;
      if (tempValue > 1440) {
        tempValue = 0;
        continue;
      }
      time.setMinutes(time.getMinutes() + tempValue);
      let option = (
        <Option value={String(tempValue)}>
          {this.timeConverter(
            `${String(time)
              .split(' ')[4]
              .substring(0, 5)}`,
          )}
        </Option>
      );
      timeOptions.push(option);
    }
    return timeOptions;
  };

  // handle items which handle from radio buttons
  handleRadioPanel = (e, subPanel) => {
    {
      subPanel.map((panel, i) => {
        if (panel.value === e.target.value) {
          document.getElementById(panel.value).style.display = 'block';
        } else {
          document.getElementById(panel.value).style.display = 'none';
        }
      });
    }
  };

  // handle items which handle from select options
  handleSelectedPanel = (e, subPanel) => {
    {
      subPanel.map((panel, i) => {
        if (panel.id === e) {
          document.getElementById(panel.id).style.display = 'block';
        } else {
          document.getElementById(panel.id).style.display = 'none';
        }
      });
    }
  };

  getColumns = ({ getFieldDecorator }, arr) => {
    const columnArray = [];
    const actionColumn = [
      {
        title: '',
        dataIndex: 'operation',
        render: (name, row) => (
          <Form.Item>
            <Popconfirm title="Sure to delete?">
              <a>
                <Text type="danger">
                  <Icon type="delete" />
                </Text>
              </a>
            </Popconfirm>
          </Form.Item>
        ),
      },
    ];
    Object.values(arr).map((columnData, c) => {
      if (columnData.type === 'input') {
        const column = {
          title: `${columnData.name}`,
          dataIndex: `${columnData.key}`,
          key: `${columnData.key}`,
          render: (name, row, i) => (
            <Form.Item>
              {getFieldDecorator(`${columnData.key}${i}`, {})(
                <Input
                  type={columnData.others.inputType}
                  placeholder={columnData.others.placeholder}
                />,
              )}
            </Form.Item>
          ),
        };
        columnArray.push(column);
      } else if (columnData.type === 'upload') {
        const column = {
          title: `${columnData.name}`,
          dataIndex: `${columnData.key}`,
          key: `${columnData.key}`,
          render: (name, row, i) => (
            <Form.Item>
              {getFieldDecorator(`${columnData.key}${i}`, {})(
                <Upload>
                  <Button>
                    <Icon type="upload" /> Choose file
                  </Button>
                </Upload>,
              )}
            </Form.Item>
          ),
        };
        columnArray.push(column);
      } else if (columnData.type === 'select') {
        const column = {
          title: `${columnData.name}`,
          dataIndex: `${columnData.key}`,
          key: `${columnData.key}`,
          render: (name, row, i) => (
            <Form.Item>
              {getFieldDecorator(`${columnData.key}${i}`, {
                initialValue: columnData.others.initialDataIndex,
              })(
                <Select disabled>
                  {columnData.others.option.map((option, i) => {
                    return (
                      <Option key={i} value={option.key}>
                        {option.value}
                      </Option>
                    );
                  })}
                </Select>,
              )}
            </Form.Item>
          ),
        };
        columnArray.push(column);
      }
    });
    const columns = columnArray.concat(actionColumn);
    return columns;
  };

  // generate form items
  getPanelItems = (panel, panelId) => {
    const { getFieldDecorator } = this.props.form;
    const subPanelList = {};
    return panel.map((item, k) => {
      fieldKeys.push(item.id);
      switch (item.type) {
        case 'select':
          if (item.optional.hasOwnProperty('subPanel')) {
            return (
              <div>
                <Form.Item
                  key={k}
                  label={
                    <span>
                      {item.label}&nbsp;
                      <Tooltip title={item.tooltip} placement="right">
                        <Icon type="question-circle-o" />
                      </Tooltip>
                    </span>
                  }
                  style={{ display: 'block' }}
                >
                  {getFieldDecorator(`${item.id}`, {
                    initialValue: `${item.optional.option[0].name}`,
                  })(
                    <Select
                      onChange={e =>
                        this.handleSelectedPanel(e, item.optional.subPanel)
                      }
                      disabled
                    >
                      {item.optional.option.map((option, i) => {
                        return (
                          <Option key={i} value={option.value}>
                            {option.name}
                          </Option>
                        );
                      })}
                    </Select>,
                  )}
                </Form.Item>
                <div className={'sub-panel-container'}>
                  {item.optional.subPanel.map((panel, i) => {
                    return (
                      <div
                        id={panel.id}
                        key={i}
                        style={
                          panel.id === item.optional.initialDataIndex
                            ? { display: 'block' }
                            : { display: 'none' }
                        }
                      >
                        {this.getPanelItems(panel.panelItem)}
                      </div>
                    );
                  })}
                </div>
              </div>
            );
          }
          return (
            <Form.Item
              key={k}
              label={
                <span>
                  {item.label}&nbsp;
                  <Tooltip title={item.tooltip} placement="right">
                    <Icon type="question-circle-o" />
                  </Tooltip>
                </span>
              }
              style={{ display: 'block' }}
            >
              {getFieldDecorator(`${item.id}`, {
                initialValue: `${item.optional.option[0].name}`,
              })(
                <Select disabled>
                  {item.optional.option.map((option, i) => {
                    return (
                      <Option key={i} value={option.value}>
                        {option.name}
                      </Option>
                    );
                  })}
                </Select>,
              )}
            </Form.Item>
          );
        case 'timeSelector':
          return (
            <Form.Item
              key={k}
              label={
                <span>
                  {item.label}&nbsp;
                  <Tooltip title={item.tooltip} placement="right">
                    <Icon type="question-circle-o" />
                  </Tooltip>
                </span>
              }
              style={{ display: 'block' }}
            >
              {getFieldDecorator(`${item.id}`, {
                initialValue: item.optional.initialDataIndex,
              })(
                <Select>
                  {this.getOptionForTimeSelectors(
                    item.optional.firstOptionValue,
                    item.optional.lastOptionValue,
                    item.optional.valueDifference,
                  )}
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
                  {item.label}&nbsp;
                  <Tooltip title={item.tooltip} placement="right">
                    <Icon type="question-circle-o" />
                  </Tooltip>
                </span>
              }
              style={{ display: 'block' }}
            >
              {getFieldDecorator(`${item.id}`, {
                rules: [
                  {
                    pattern: new RegExp(`${item.optional.rules.regex}`),
                    message: `${item.optional.rules.validationMsg}`,
                  },
                ],
              })(<Input placeholder={item.optional.placeholder} disabled />)}
            </Form.Item>
          );
        case 'checkbox':
          if (item.optional.hasOwnProperty('subPanel')) {
            return (
              <div key={k}>
                <Collapse
                  bordered={false}
                  activeKey={this.state.activeSubPanelKeys}
                >
                  <Collapse.Panel
                    key={item.id}
                    showArrow={false}
                    style={{ border: 0 }}
                    header={
                      <Form.Item key={k}>
                        {getFieldDecorator(`${item.id}`, {
                          valuePropName: 'checked',
                          initialValue: item.optional.ischecked,
                        })(
                          <Checkbox disabled>
                            <span>
                              {item.label}&nbsp;
                              <Tooltip title={item.tooltip} placement="right">
                                <Icon type="question-circle-o" />
                              </Tooltip>
                            </span>
                          </Checkbox>,
                        )}
                      </Form.Item>
                    }
                  >
                    <div>
                      <div>
                        {item.optional.subPanel.map((panel, i) => {
                          subPanelList[panel.others.itemSwitch] =
                            panel.others.itemPayload;
                          if (
                            subPanelpayloadAttributes.hasOwnProperty(panelId)
                          ) {
                            Object.assign(
                              subPanelpayloadAttributes[panelId],
                              subPanelList,
                            );
                          } else {
                            subPanelpayloadAttributes[panelId] = subPanelList;
                          }
                          return (
                            <div key={i}>
                              {this.getPanelItems(panel.panelItem, panelId)}
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  </Collapse.Panel>
                </Collapse>
              </div>
            );
          }
          return (
            <Form.Item key={k}>
              {getFieldDecorator(`${item.id}`, {
                valuePropName: 'checked',
                initialValue: item.optional.ischecked,
              })(
                <Checkbox disabled>
                  <span>
                    {item.label}&nbsp;
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
                  {item.label}&nbsp;
                  <Tooltip title={item.tooltip} placement="right">
                    <Icon type="question-circle-o" />
                  </Tooltip>
                </span>
              }
              style={{ display: 'block' }}
            >
              {getFieldDecorator(`${item.id}`, {
                initialValue: null,
              })(
                <TextArea
                  placeholder={item.optional.placeholder}
                  rows={item.optional.row}
                  disabled
                />,
              )}
            </Form.Item>
          );
        case 'radioGroup':
          return (
            <div>
              <Form.Item
                key={k}
                label={
                  <span>
                    {item.label}&nbsp;
                    <Tooltip title={item.tooltip} placement="right">
                      <Icon type="question-circle-o" />
                    </Tooltip>
                  </span>
                }
                style={{ display: 'block' }}
              >
                {getFieldDecorator(`${item.id}`, {
                  initialValue: `${item.optional.initialValue}`,
                })(
                  <Radio.Group
                    onChange={e =>
                      this.handleRadioPanel(e, item.optional.radio)
                    }
                  >
                    {item.optional.radio.map((option, i) => {
                      return (
                        <Radio key={i} value={option.value}>
                          {option.name}
                        </Radio>
                      );
                    })}
                  </Radio.Group>,
                )}
              </Form.Item>
              <div className={'sub-panel-container'}>
                {item.optional.subPanel.map((panel, i) => {
                  return (
                    <div
                      key={i}
                      id={panel.id}
                      style={
                        panel.id === item.optional.initialValue
                          ? { display: 'block' }
                          : { display: 'none' }
                      }
                    >
                      {this.getPanelItems(panel.panelItem)}
                    </div>
                  );
                })}
              </div>
            </div>
          );
        case 'title':
          return (
            <Title key={k} level={4}>
              {item.label}{' '}
            </Title>
          );
        case 'paragraph':
          return (
            <Paragraph key={k} style={{ marginTop: 20 }}>
              {item.label}{' '}
            </Paragraph>
          );
        case 'alert':
          return (
            <Alert key={k} description={item.label} type="warning" showIcon />
          );
        case 'upload':
          return (
            <Form.Item
              key={k}
              label={
                <span>
                  {item.label}&nbsp;
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
        case 'inputTable':
          if (
            !(`${item.optional.dataSource}` in this.state.inputTableDataSources)
          ) {
            Object.defineProperty(
              this.state.inputTableDataSources,
              `${item.optional.dataSource}`,
              { value: [], writable: true },
            );
          }
          return (
            <div key={k}>
              <Table
                id={item.id}
                dataSource={
                  this.state.inputTableDataSources[item.optional.dataSource]
                }
                columns={this.getColumns(
                  { getFieldDecorator },
                  item.optional.columns,
                )}
              />
            </div>
          );
        case 'customInputTable':
          return (
            <div key={k}>
              <Table
                id={item.id}
                dataSource={this.state.customInputDataArray}
                columns={this.getColumns(
                  { getFieldDecorator },
                  item.optional.columns,
                )}
              />
            </div>
          );
        default:
          return null;
      }
    });
  };

  onPreview = e => {
    this.setProfileInfo();
    this.setState({
      profilePreviewKey: 'profileInfo',
      isInfoPreview: true,
    });
  };

  onCancelPreview = e => {
    this.setState({
      profilePreviewKey: 'profileInfo',
      isInfoPreview: false,
    });
  };

  render() {
    const { policyUIConfigurationsList } = this.props;
    const { getFieldDecorator } = this.props.form;
    return (
      <div style={{ marginTop: 20 }}>
        <Row>
          <Col span={4}>
            <Title level={4}>Profile Information</Title>
          </Col>
          <Col span={16}>
            <Button
              type="link"
              icon="eye"
              onClick={this.onPreview}
              style={{ display: this.state.isInfoPreview ? 'none' : 'inline' }}
            >
              <Text
                style={{
                  fontSize: 'small',
                }}
              >
                (Click to view policy information)
              </Text>
            </Button>
            <Button
              type="link"
              icon="eye-invisible"
              onClick={this.onCancelPreview}
              style={{ display: this.state.isInfoPreview ? 'inline' : 'none' }}
            />
          </Col>
        </Row>
        <Collapse
          bordered={false}
          activeKey={this.state.profilePreviewKey}
          style={{ display: this.state.isInfoPreview ? 'block' : 'none' }}
        >
          <Collapse.Panel
            key={'profileInfo'}
            showArrow={false}
            style={{
              border: 0,
            }}
          >
            <div className="tab-container">
              <Tabs tabPosition={'left'} size={'large'}>
                {policyUIConfigurationsList.map((element, i) => {
                  return (
                    <TabPane tab={<span>{element.name}</span>} key={i}>
                      {Object.values(element.panels).map((panel, j) => {
                        panel = panel.panel;
                        return (
                          <div key={j}>
                            <Collapse
                              bordered={false}
                              activeKey={this.state.activePanelKeys}
                            >
                              <Collapse.Panel
                                key={panel.panelId}
                                showArrow={false}
                                style={{ border: 0 }}
                                header={
                                  <div>
                                    <Row>
                                      <Col offset={0} span={14}>
                                        <Title level={4}> {panel.title} </Title>
                                      </Col>
                                    </Row>
                                    <Row>{panel.description}</Row>
                                  </div>
                                }
                              >
                                {panel.hasOwnProperty('panelItem') && (
                                  <div>
                                    <Form name={panel.panelId}>
                                      <Form.Item style={{ display: 'none' }}>
                                        {getFieldDecorator(`${panel.panelId}`, {
                                          initialValue: ' ',
                                        })(<Input />)}
                                      </Form.Item>
                                      {this.getPanelItems(
                                        panel.panelItem,
                                        panel.panelId,
                                      )}
                                    </Form>
                                  </div>
                                )}
                                {panel.hasOwnProperty('subFormLists') && (
                                  <div>
                                    {Object.values(panel.subFormLists).map(
                                      (form, i) => {
                                        subFormContainer[`${form.id}`] =
                                          panel.panelId;
                                        return (
                                          <Form name={form.id} key={i}>
                                            <Form.Item
                                              style={{ display: 'none' }}
                                            >
                                              {getFieldDecorator(`${form.id}`, {
                                                initialValue: ' ',
                                              })(<Input />)}
                                            </Form.Item>
                                            {this.getPanelItems(form.panelItem)}
                                          </Form>
                                        );
                                      },
                                    )}
                                  </div>
                                )}
                              </Collapse.Panel>
                            </Collapse>
                          </div>
                        );
                      })}
                    </TabPane>
                  );
                })}
              </Tabs>
            </div>
          </Collapse.Panel>
        </Collapse>
      </div>
    );
  }
}

export default withConfigContext(Form.create()(PolicyInfo));
