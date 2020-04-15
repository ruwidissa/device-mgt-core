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
  Select,
  Button,
  Modal,
  notification,
} from 'antd';
import axios from 'axios';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import './styles.css';
import LifeCycleDetailsModal from './components/lifeCycleDetailsModal';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import { handleApiError } from '../../../../../../../../services/utils/errorHandler';

const { Text, Title, Paragraph } = Typography;
const { Option } = Select;

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

class LifeCycle extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      currentStatus: props.currentStatus,
      selectedStatus: null,
      reasonText: '',
      isReasonModalVisible: false,
      isConfirmButtonLoading: false,
    };
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

  handleSelectChange = value => {
    this.setState({ selectedStatus: value });
  };

  showReasonModal = () => {
    this.setState({
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
        }
      })
      .catch(error => {
        handleApiError(error, 'Error occurred while trying to add lifecycle');
        this.setState({
          isConfirmButtonLoading: false,
        });
      });
  };

  render() {
    const {
      currentStatus,
      selectedStatus,
      isConfirmButtonLoading,
    } = this.state;
    const { lifecycle } = this.props;
    const selectedValue = selectedStatus == null ? [] : selectedStatus;
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
          Note: ‘Change State To’ displays only the next states allowed from the
          current state
        </Paragraph>
        {lifecycle !== null && <LifeCycleDetailsModal lifecycle={lifecycle} />}
        <Divider dashed={true} />
        <Text strong={true}>Current State: </Text>{' '}
        <Tag color="blue">{currentStatus}</Tag>
        <br />
        <br />
        <Text>Change State to: </Text>
        <Select
          placeholder="Select state"
          style={{ width: 120 }}
          size="small"
          onChange={this.handleSelectChange}
          value={selectedValue}
          showSearch={true}
        >
          {proceedingStates.map(lifecycleState => {
            return (
              <Option key={lifecycleState} value={lifecycleState}>
                {lifecycleState}
              </Option>
            );
          })}
        </Select>
        <Button
          style={{ marginLeft: 10 }}
          size="small"
          type="primary"
          htmlType="button"
          onClick={this.showReasonModal}
          loading={isConfirmButtonLoading}
          disabled={selectedStatus == null}
        >
          Change
        </Button>
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
          <br />
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
