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
  Typography,
  Tag,
  Divider,
  Button,
  Modal,
  notification,
  Steps,
  Icon,
  Alert,
  Tabs,
} from 'antd';
import axios from 'axios';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import './styles.css';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import { handleApiError } from '../../../../../../../../services/utils/errorHandler';
import LifeCycleHistory from './components/LifeCycleHistory';

const { Text, Title, Paragraph } = Typography;
const { TabPane } = Tabs;

const modules = {
  toolbar: [
    [{ header: [1, 2, false] }],
    ['bold', 'italic', 'underline', 'strike', 'blockquote', 'code-block'],
    [{ list: 'ordered' }, { list: 'bullet' }],
    ['link', 'image'],
  ],
};

const formats = [
  'header',
  'bold',
  'italic',
  'underline',
  'strike',
  'blockquote',
  'code-block',
  'list',
  'bullet',
  'link',
  'image',
];

const { Step } = Steps;

class LifeCycle extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      currentStatus: props.currentStatus,
      selectedStatus: null,
      reasonText: '',
      isReasonModalVisible: false,
      isConfirmButtonLoading: false,
      current: 0,
      lifecycleSteps: [],
      lifeCycleStates: [],
    };
  }

  componentDidMount() {
    const config = this.props.context;
    const lifeCycleConfig = config.lifecycle;
    const lifecycleSteps = Object.keys(lifeCycleConfig).map(config => {
      return lifeCycleConfig[config];
    });
    this.setState({
      current: lifeCycleConfig[this.props.currentStatus].step,
      lifecycleSteps,
    });
    this.getLifeCycleHistory();
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (
      prevProps.currentStatus !== this.props.currentStatus ||
      prevProps.uuid !== this.props.uuid
    ) {
      this.setState({
        currentStatus: this.props.currentStatus,
      });
    }
  }

  handleChange = value => {
    this.setState({ reasonText: value });
  };

  showReasonModal = lifecycleState => {
    this.setState({
      selectedStatus: lifecycleState,
      isReasonModalVisible: true,
    });
  };

  closeReasonModal = () => {
    this.setState({
      isReasonModalVisible: false,
    });
  };

  addLifeCycle = () => {
    const config = this.props.context;
    const lifeCycleConfig = config.lifecycle;
    const { selectedStatus, reasonText } = this.state;
    const { uuid } = this.props;
    const data = {
      action: selectedStatus,
      reason: reasonText,
    };

    this.setState({
      isConfirmButtonLoading: true,
    });

    axios
      .post(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.publisher +
          '/applications/life-cycle/' +
          uuid,
        data,
      )
      .then(res => {
        if (res.status === 201) {
          this.setState({
            current: lifeCycleConfig[selectedStatus].step,
            isReasonModalVisible: false,
            isConfirmButtonLoading: false,
            currentStatus: selectedStatus,
            selectedStatus: null,
            reasonText: '',
          });
          this.props.changeCurrentLifecycleStatus(selectedStatus);
          notification.success({
            message: 'Done!',
            description: 'Lifecycle state updated successfully!',
          });
          this.getLifeCycleHistory();
        }
      })
      .catch(error => {
        handleApiError(error, 'Error occurred while trying to add lifecycle');
        this.setState({
          isConfirmButtonLoading: false,
        });
      });
  };

  getLifeCycleHistory = () => {
    const config = this.props.context;
    const { uuid } = this.props;

    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.publisher +
          '/applications/life-cycle/state-changes/' +
          uuid,
      )
      .then(res => {
        if (res.status === 200) {
          this.setState({ lifeCycleStates: JSON.parse(res.data.data) });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to get lifecycle history',
        );
      });
  };

  onChange = current => {
    this.setState({ current });
  };

  render() {
    const {
      currentStatus,
      selectedStatus,
      current,
      lifecycleSteps,
      lifeCycleStates,
    } = this.state;
    const { lifecycle } = this.props;
    let proceedingStates = [];
    if (
      lifecycle !== null &&
      lifecycle.hasOwnProperty(currentStatus) &&
      lifecycle[currentStatus].hasOwnProperty('proceedingStates')
    ) {
      proceedingStates = lifecycle[currentStatus].proceedingStates;
    }

    return (
      <div>
        <Title level={4}>Manage Lifecycle</Title>
        <Divider />
        <Paragraph>
          Ensure that your security policies are not violated by the
          application. Have a thorough review and approval process before
          directly publishing it to your app store. You can easily transition
          from one state to another. <br />
        </Paragraph>
        <Tabs defaultActiveKey="1" type="card">
          <TabPane tab="Change Lifecycle" key="1">
            <div>
              <Steps
                direction={'vertical'}
                current={current}
                onChange={this.onChange}
                size="small"
              >
                {lifecycleSteps.map((step, index) => (
                  <Step
                    key={index}
                    icon={<Icon type={step.icon} />}
                    title={step.title}
                    disabled={current !== step.step}
                    description={
                      current === step.step && (
                        <div style={{ width: 400 }}>
                          <p>{step.text}</p>
                          {proceedingStates.map(lifecycleState => {
                            return (
                              <Button
                                size={'small'}
                                style={{ marginRight: 3 }}
                                onClick={() =>
                                  this.showReasonModal(lifecycleState)
                                }
                                key={lifecycleState}
                                type={'primary'}
                              >
                                {lifecycleState}
                              </Button>
                            );
                          })}
                        </div>
                      )
                    }
                  />
                ))}
              </Steps>
            </div>
          </TabPane>
          <TabPane tab="Lifecycle History" key="2">
            <LifeCycleHistory lifeCycleStates={lifeCycleStates} />
          </TabPane>
        </Tabs>
        <Divider />
        <Modal
          title="Confirm changing lifecycle state"
          visible={this.state.isReasonModalVisible}
          onOk={this.addLifeCycle}
          onCancel={this.closeReasonModal}
          okText="Confirm"
        >
          <Text>
            You are going to change the lifecycle state from,
            <br />
            <Tag color="blue">{currentStatus}</Tag>to{' '}
            <Tag color="blue">{selectedStatus}</Tag>
          </Text>
          <br />
          {lifecycle && selectedStatus && lifecycle[selectedStatus].isEndState && (
            <Alert
              message="In this state application becomes completely obsolete. Be careful,
              this process cannot be undone."
              banner
              style={{ marginTop: 5 }}
            />
          )}
          <Divider orientation="left">Reason</Divider>
          <ReactQuill
            theme="snow"
            value={this.state.reasonText}
            onChange={this.handleChange}
            modules={modules}
            formats={formats}
            placeholder="Leave a comment (optional)"
          />
        </Modal>
      </div>
    );
  }
}

export default withConfigContext(LifeCycle);
