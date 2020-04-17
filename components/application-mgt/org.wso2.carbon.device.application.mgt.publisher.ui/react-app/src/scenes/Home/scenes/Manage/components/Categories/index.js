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
import pSBC from 'shade-blend-color';
import { withConfigContext } from '../../../../../../components/ConfigContext';
import { handleApiError } from '../../../../../../services/utils/errorHandler';
import { isAuthorized } from '../../../../../../services/utils/authorizationHandler';

const { Title } = Typography;

class ManageCategories extends React.Component {
  state = {
    loading: false,
    searchText: '',
    categories: [],
    tempElements: [],
    inputVisible: false,
    inputValue: '',
    isAddNewVisible: false,
    isEditModalVisible: false,
    currentlyEditingId: null,
    editingValue: null,
    forbiddenErrors: {
      categories: false,
    },
  };

  componentDidMount() {
    const config = this.props.context;
    this.hasPermissionToManage = isAuthorized(
      config.user,
      '/permission/admin/app-mgt/publisher/admin/application/update',
    );
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.publisher +
          '/applications/categories',
      )
      .then(res => {
        if (res.status === 200) {
          let categories = JSON.parse(res.data.data);
          this.setState({
            categories: categories,
            loading: false,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occured while trying to load categories',
          true,
        );
        this.setState({
          loading: false,
        });
      });
  }

  handleCloseButton = () => {
    this.setState({
      tempElements: [],
      isAddNewVisible: false,
    });
  };

  deleteCategory = id => {
    const config = this.props.context;
    this.setState({
      loading: true,
    });
    axios
      .delete(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.publisher +
          '/admin/applications/categories/' +
          id,
      )
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Done!',
            description: 'Category Removed Successfully!',
          });

          const { categories } = this.state;
          const remainingElements = categories.filter(function(value) {
            return value.categoryName !== id;
          });

          this.setState({
            loading: false,
            categories: remainingElements,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to load categories.',
        );
        this.setState({
          loading: false,
        });
      });
  };

  renderElement = category => {
    const config = this.props.context;
    const categoryName = category.categoryName;
    const tagElem = (
      <Tag
        color={pSBC(0.3, config.theme.primaryColor)}
        style={{ marginTop: 8 }}
      >
        {categoryName}
        {this.hasPermissionToManage && (
          <>
            <Divider type="vertical" />
            <Tooltip title="edit">
              <Icon
                onClick={() => {
                  this.openEditModal(categoryName);
                }}
                type="edit"
              />
            </Tooltip>
            <Divider type="vertical" />
            <Tooltip title="delete">
              <Popconfirm
                title="Are you sure delete this category?"
                onConfirm={() => {
                  if (category.isCategoryDeletable) {
                    this.deleteCategory(categoryName);
                  } else {
                    notification.error({
                      message: 'Cannot delete "' + categoryName + '"',
                      description:
                        'This category is currently used. Please unassign the category from apps.',
                    });
                  }
                }}
                okText="Yes"
                cancelText="No"
              >
                <Icon type="delete" />
              </Popconfirm>
            </Tooltip>
          </>
        )}
      </Tag>
    );
    return (
      <span key={category.categoryName} style={{ display: 'inline-block' }}>
        {tagElem}
      </span>
    );
  };

  renderTempElement = category => {
    const tagElem = (
      <Tag
        style={{ marginTop: 8 }}
        closable
        onClose={e => {
          e.preventDefault();
          const { tempElements } = this.state;
          const remainingElements = tempElements.filter(function(value) {
            return value.categoryName !== category.categoryName;
          });
          this.setState({
            tempElements: remainingElements,
          });
        }}
      >
        {category.categoryName}
      </Tag>
    );
    return (
      <span key={category.categoryName} style={{ display: 'inline-block' }}>
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
    const { inputValue, categories } = this.state;
    let { tempElements } = this.state;
    if (inputValue) {
      if (
        categories.findIndex(i => i.categoryName === inputValue) === -1 &&
        tempElements.findIndex(i => i.categoryName === inputValue) === -1
      ) {
        tempElements = [
          ...tempElements,
          { categoryName: inputValue, isCategoryDeletable: true },
        ];
      } else {
        message.warning('Category already exists');
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
    const { tempElements, categories } = this.state;
    this.setState({
      loading: true,
    });

    const data = tempElements.map(category => category.categoryName);

    axios
      .post(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.publisher +
          '/admin/applications/categories',
        data,
      )
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Done!',
            description: 'New Categories were added successfully',
          });

          this.setState({
            categories: [...categories, ...tempElements],
            tempElements: [],
            inputVisible: false,
            inputValue: '',
            loading: false,
            isAddNewVisible: false,
          });
        }
      })
      .catch(error => {
        handleApiError(error, 'Error occurred while trying to add categories.');
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

    const { editingValue, currentlyEditingId, categories } = this.state;

    this.setState({
      loading: true,
      isEditModalVisible: false,
    });

    axios
      .put(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.publisher +
          '/admin/applications/categories/rename?from=' +
          currentlyEditingId +
          '&to=' +
          editingValue,
        {},
      )
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Done!',
            description: 'Category was edited successfully',
          });

          categories[
            categories.findIndex(i => i.categoryName === currentlyEditingId)
          ].categoryName = editingValue;

          this.setState({
            categories: categories,
            loading: false,
            editingValue: null,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to delete the category.',
        );
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
      categories,
      inputVisible,
      inputValue,
      tempElements,
      isAddNewVisible,
    } = this.state;
    const categoriesElements = categories.map(this.renderElement);
    const temporaryElements = tempElements.map(this.renderTempElement);
    return (
      <div style={{ marginBottom: 16 }}>
        {!this.hasPermissionToManage && (
          <Alert
            message="You don't have permission to add / edit / delete categories."
            type="warning"
            banner
          />
        )}
        <Card>
          <Spin tip="Working on it..." spinning={this.state.loading}>
            <Row>
              <Col span={16}>
                <Title level={4}>Categories</Title>
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
                      disabled={!this.hasPermissionToManage}
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
                        <Icon type="plus" /> New Category
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
                {categoriesElements}
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

export default withConfigContext(ManageCategories);
