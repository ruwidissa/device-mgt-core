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
  Card,
  Tag,
  message,
  Icon,
  Input,
  notification,
  Divider,
  Button,
  Spin,
  Tooltip,
  Popconfirm,
  Modal,
  Row,
  Col,
  Typography,
  Alert,
} from 'antd';
import axios from 'axios';
import { TweenOneGroup } from 'rc-tween-one';
import { withConfigContext } from '../../../../../../components/ConfigContext';
import { handleApiError } from '../../../../../../services/utils/errorHandler';

const { Title } = Typography;

class ManageTags extends React.Component {
  state = {
    loading: false,
    searchText: '',
    tags: [],
    tempElements: [],
    inputVisible: false,
    inputValue: '',
    isAddNewVisible: false,
    isEditModalVisible: false,
    currentlyEditingId: null,
    editingValue: null,
    forbiddenErrors: {
      tags: false,
    },
  };

  componentDidMount() {
    const config = this.props.context;
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.publisher +
          '/applications/tags',
      )
      .then(res => {
        if (res.status === 200) {
          let tags = JSON.parse(res.data.data);
          this.setState({
            tags: tags,
            loading: false,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to load tags.',
          true,
        );
        if (error.hasOwnProperty('response') && error.response.status === 403) {
          const { forbiddenErrors } = this.state;
          forbiddenErrors.tags = true;
          this.setState({
            forbiddenErrors,
            loading: false,
          });
        } else {
          this.setState({
            loading: false,
          });
        }
      });
  }

  handleCloseButton = () => {
    this.setState({
      tempElements: [],
      isAddNewVisible: false,
    });
  };

  deleteTag = id => {
    const config = this.props.context;

    this.setState({
      loading: true,
    });

    axios
      .delete(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.publisher +
          '/admin/applications/tags/' +
          id,
      )
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Done!',
            description: 'Tag Removed Successfully!',
          });

          const { tags } = this.state;
          const remainingElements = tags.filter(function(value) {
            return value.tagName !== id;
          });

          this.setState({
            loading: false,
            tags: remainingElements,
          });
        }
      })
      .catch(error => {
        handleApiError(error, 'Error occurred while trying to delete the tag.');
        this.setState({
          loading: false,
        });
      });
  };

  renderElement = tag => {
    const tagName = tag.tagName;
    const tagElem = (
      <Tag color="#34495e" style={{ marginTop: 8 }}>
        {tagName}
        <Divider type="vertical" />
        <Tooltip title="edit">
          <Icon
            onClick={() => {
              this.openEditModal(tagName);
            }}
            type="edit"
          />
        </Tooltip>
        <Divider type="vertical" />
        <Tooltip title="delete">
          <Popconfirm
            title="Are you sure delete this tag?"
            onConfirm={() => {
              if (tag.isTagDeletable) {
                this.deleteTag(tagName);
              } else {
                notification.error({
                  message: 'Cannot delete "' + tagName + '"',
                  description:
                    'This tag is currently used. Please unassign the tag from apps.',
                });
              }
            }}
            okText="Yes"
            cancelText="No"
          >
            <Icon type="delete" />
          </Popconfirm>
        </Tooltip>
      </Tag>
    );
    return (
      <span key={tag.tagName} style={{ display: 'inline-block' }}>
        {tagElem}
      </span>
    );
  };

  renderTempElement = tag => {
    const { tempElements } = this.state;
    const tagElem = (
      <Tag
        style={{ marginTop: 8 }}
        closable
        onClose={e => {
          e.preventDefault();
          const remainingElements = tempElements.filter(function(value) {
            return value.tagName !== tag.tagName;
          });
          this.setState({
            tempElements: remainingElements,
          });
        }}
      >
        {tag.tagName}
      </Tag>
    );
    return (
      <span key={tag.tagName} style={{ display: 'inline-block' }}>
        {tagElem}
      </span>
    );
  };

  showInput = () => {
    this.setState({ inputVisible: true }, () => this.input.focus());
  };

  handleInputChange = e => {
    this.setState({ inputValue: e.target.value });
  };

  handleInputConfirm = () => {
    const { inputValue, tags } = this.state;
    let { tempElements } = this.state;
    if (inputValue) {
      if (
        tags.findIndex(i => i.tagName === inputValue) === -1 &&
        tempElements.findIndex(i => i.tagName === inputValue) === -1
      ) {
        tempElements = [
          ...tempElements,
          { tagName: inputValue, isTagDeletable: true },
        ];
      } else {
        message.warning('Tag already exists');
      }
    }

    this.setState({
      tempElements,
      inputVisible: false,
      inputValue: '',
    });
  };

  handleSave = () => {
    const config = this.props.context;
    const { tempElements, tags } = this.state;
    this.setState({
      loading: true,
    });

    const data = tempElements.map(tag => tag.tagName);

    axios
      .post(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.publisher +
          '/applications/tags',
        data,
      )
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Done!',
            description: 'New tags were added successfully',
          });

          this.setState({
            tags: [...tags, ...tempElements],
            tempElements: [],
            inputVisible: false,
            inputValue: '',
            loading: false,
            isAddNewVisible: false,
          });
        }
      })
      .catch(error => {
        handleApiError(error, 'Error occurred while trying to delete tag.');
        this.setState({
          loading: false,
        });
      });
  };

  saveInputRef = input => (this.input = input);

  closeEditModal = e => {
    this.setState({
      isEditModalVisible: false,
      currentlyEditingId: null,
    });
  };

  openEditModal = id => {
    this.setState({
      isEditModalVisible: true,
      currentlyEditingId: id,
      editingValue: id,
    });
  };

  editItem = () => {
    const config = this.props.context;

    const { editingValue, currentlyEditingId, tags } = this.state;

    this.setState({
      loading: true,
      isEditModalVisible: false,
    });

    axios
      .put(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.publisher +
          '/applications/tags/rename?from=' +
          currentlyEditingId +
          '&to=' +
          editingValue,
        {},
      )
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Done!',
            description: 'Tag was edited successfully',
          });

          tags[
            tags.findIndex(i => i.tagName === currentlyEditingId)
          ].tagName = editingValue;

          this.setState({
            tags: tags,
            loading: false,
            editingValue: null,
          });
        }
      })
      .catch(error => {
        handleApiError(error, 'Error occurred while trying to edit tag.');
        this.setState({
          loading: false,
          editingValue: null,
        });
      });
  };

  handleEditInputChange = e => {
    this.setState({
      editingValue: e.target.value,
    });
  };

  render() {
    const {
      tags,
      inputVisible,
      inputValue,
      tempElements,
      isAddNewVisible,
      forbiddenErrors,
    } = this.state;
    const tagsElements = tags.map(this.renderElement);
    const temporaryElements = tempElements.map(this.renderTempElement);
    return (
      <div style={{ marginBottom: 16 }}>
        {forbiddenErrors.tags && (
          <Alert
            message="You don't have permission to view tags."
            type="warning"
            banner
            closable
          />
        )}
        <Card>
          <Spin tip="Working on it..." spinning={this.state.loading}>
            <Row>
              <Col span={16}>
                <Title level={4}>Tags</Title>
              </Col>
              <Col span={8}>
                {!isAddNewVisible && (
                  <div style={{ float: 'right' }}>
                    <Button
                      icon="plus"
                      // type="primary"
                      size="small"
                      onClick={() => {
                        this.setState(
                          {
                            isAddNewVisible: true,
                            inputVisible: true,
                          },
                          () => this.input.focus(),
                        );
                      }}
                      htmlType="button"
                    >
                      Add
                    </Button>
                  </div>
                )}
              </Col>
            </Row>
            {isAddNewVisible && (
              <div>
                <Divider />
                <div style={{ marginBottom: 16 }}>
                  <TweenOneGroup
                    enter={{
                      scale: 0.8,
                      opacity: 0,
                      type: 'from',
                      duration: 100,
                      onComplete: e => {
                        e.target.style = '';
                      },
                    }}
                    leave={{ opacity: 0, width: 0, scale: 0, duration: 200 }}
                    appear={false}
                  >
                    {temporaryElements}

                    {inputVisible && (
                      <Input
                        ref={this.saveInputRef}
                        type="text"
                        size="small"
                        style={{ width: 120 }}
                        value={inputValue}
                        onChange={this.handleInputChange}
                        onBlur={this.handleInputConfirm}
                        onPressEnter={this.handleInputConfirm}
                      />
                    )}
                    {!inputVisible && (
                      <Tag
                        onClick={this.showInput}
                        style={{ background: '#fff', borderStyle: 'dashed' }}
                      >
                        <Icon type="plus" /> New Tag
                      </Tag>
                    )}
                  </TweenOneGroup>
                </div>
                <div>
                  {tempElements.length > 0 && (
                    <span>
                      <Button
                        onClick={this.handleSave}
                        htmlType="button"
                        type="primary"
                        size="small"
                        disabled={tempElements.length === 0}
                      >
                        Save
                      </Button>
                      <Divider type="vertical" />
                    </span>
                  )}
                  <Button onClick={this.handleCloseButton} size="small">
                    Cancel
                  </Button>
                </div>
              </div>
            )}
            <Divider dashed="true" />
            <div style={{ marginTop: 8 }}>
              <TweenOneGroup
                enter={{
                  scale: 0.8,
                  opacity: 0,
                  type: 'from',
                  duration: 100,
                  onComplete: e => {
                    e.target.style = '';
                  },
                }}
                leave={{ opacity: 0, width: 0, scale: 0, duration: 200 }}
                appear={false}
              >
                {tagsElements}
              </TweenOneGroup>
            </div>
          </Spin>
        </Card>
        <Modal
          title="Edit"
          visible={this.state.isEditModalVisible}
          onCancel={this.closeEditModal}
          onOk={this.editItem}
        >
          <Input
            value={this.state.editingValue}
            ref={input => (this.editingInput = input)}
            onChange={this.handleEditInputChange}
          />
        </Modal>
      </div>
    );
  }
}

export default withConfigContext(ManageTags);
