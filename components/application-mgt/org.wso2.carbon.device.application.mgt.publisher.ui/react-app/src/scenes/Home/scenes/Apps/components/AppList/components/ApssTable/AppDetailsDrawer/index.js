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
  Drawer,
  Select,
  Avatar,
  Typography,
  Divider,
  Tag,
  notification,
  List,
  Button,
  Spin,
  message,
  Card,
  Badge,
  Tooltip,
  Dropdown,
  Menu,
} from 'antd';
import DetailedRating from '../../../../DetailedRating';
import { Link } from 'react-router-dom';
import axios from 'axios';
import ReactQuill from 'react-quill';
import ReactHtmlParser from 'react-html-parser';
import './styles.css';
import pSBC from 'shade-blend-color';
import { withConfigContext } from '../../../../../../../../../components/ConfigContext';
import ManagedConfigurationsIframe from './components/ManagedConfigurationsIframe';
import { handleApiError } from '../../../../../../../../../services/utils/errorHandler';
import Authorized from '../../../../../../../../../components/Authorized/Authorized';
import { isAuthorized } from '../../../../../../../../../services/utils/authorizationHandler';
import {
  CheckCircleOutlined,
  EditOutlined,
  MoreOutlined,
  StarOutlined,
  UploadOutlined,
  CheckOutlined,
} from '@ant-design/icons';
import DeleteApp from './components/DeleteApp';
import RetireApp from './components/RetireApp';

const { Meta } = Card;
const { Text, Title } = Typography;
const { Option } = Select;

const modules = {
  toolbar: [
    ['bold', 'italic', 'underline', 'strike', 'blockquote'],
    [{ list: 'ordered' }, { list: 'bullet' }],
    ['link'],
  ],
};

const formats = [
  'bold',
  'italic',
  'underline',
  'strike',
  'blockquote',
  'list',
  'bullet',
  'link',
];

class AppDetailsDrawer extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    const drawerWidth = window.innerWidth <= 770 ? '80%' : '40%';

    this.state = {
      loading: false,
      name: '',
      description: null,
      globalCategories: [],
      globalTags: [],
      globalUnrestrictedRoles: [],
      categories: [],
      tags: [],
      unrestrictedRoles: [],
      temporaryDescription: null,
      temporaryCategories: [],
      temporaryTags: [],
      temporaryUnrestrictedRoles: [],
      isDescriptionEditEnabled: false,
      isCategoriesEditEnabled: false,
      isTagsEditEnabled: false,
      isUnrestrictedRolesEditEnabled: false,
      drawer: null,
      drawerWidth,
    };
  }

  componentDidMount() {
    if (
      isAuthorized(
        this.props.context.user,
        '/permission/admin/app-mgt/publisher/application/update',
      )
    ) {
      this.getCategories();
      this.getTags();
      this.getUnrestrictedRoles();
    }
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (prevProps.app !== this.props.app) {
      const {
        name,
        description,
        tags,
        categories,
        unrestrictedRoles,
      } = this.props.app;
      this.setState({
        name,
        description,
        tags,
        categories,
        unrestrictedRoles,
        isDescriptionEditEnabled: false,
        isCategoriesEditEnabled: false,
        isTagsEditEnabled: false,
        isUnrestrictedRolesEditEnabled: false,
      });
    }
  }

  getCategories = () => {
    axios
      .get(
        window.location.origin +
          this.config.serverConfig.invoker.uri +
          this.config.serverConfig.invoker.publisher +
          '/applications/categories',
      )
      .then(res => {
        if (res.status === 200) {
          const categories = JSON.parse(res.data.data);
          const globalCategories = categories.map(category => {
            return (
              <Option key={category.categoryName}>
                {category.categoryName}
              </Option>
            );
          });

          this.setState({
            globalCategories,
            loading: false,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to load categories.',
          true,
        );
        this.setState({
          loading: false,
        });
      });
  };

  getTags = () => {
    axios
      .get(
        window.location.origin +
          this.config.serverConfig.invoker.uri +
          this.config.serverConfig.invoker.publisher +
          '/applications/tags',
      )
      .then(res => {
        if (res.status === 200) {
          const tags = JSON.parse(res.data.data);

          const globalTags = tags.map(tag => {
            return <Option key={tag.tagName}>{tag.tagName}</Option>;
          });

          this.setState({
            globalTags,
            loading: false,
          });
        }
      })
      .catch(error => {
        handleApiError(error, 'Error occurred while trying to load tags.');
        this.setState({
          loading: false,
        });
      });
  };

  getUnrestrictedRoles = () => {
    axios
      .get(
        window.location.origin +
          this.config.serverConfig.invoker.uri +
          this.config.serverConfig.invoker.deviceMgt +
          '/roles',
      )
      .then(res => {
        if (res.status === 200) {
          const globalUnrestrictedRoles = res.data.data.roles;

          this.setState({
            globalUnrestrictedRoles,
            loading: false,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to load roles.',
          true,
        );
        this.setState({
          loading: false,
        });
      });
  };

  // change the app name
  handleNameSave = name => {
    const { id } = this.props.app;
    if (name !== this.state.name && name !== '') {
      const data = { name: name };
      axios
        .put(
          window.location.origin +
            this.config.serverConfig.invoker.uri +
            this.config.serverConfig.invoker.publisher +
            '/applications/' +
            id,
          data,
        )
        .then(res => {
          if (res.status === 200) {
            const app = res.data.data;
            this.props.onUpdateApp('name', app.name);
            notification.success({
              message: 'Saved!',
              description: 'App name updated successfully!',
            });
            this.setState({
              loading: false,
              name: app.name,
            });
          }
        })
        .catch(error => {
          if (
            error.hasOwnProperty('response') &&
            error.response.status === 401
          ) {
            message.error('You are not logged in');
            window.location.href = window.location.origin + '/publisher/login';
          } else {
            notification.error({
              message: 'There was a problem',
              duration: 0,
              description: 'Error occurred while trying to save the app name.',
            });
          }

          this.setState({ loading: false });
        });
    }
  };

  // handle description change
  handleDescriptionChange = temporaryDescription => {
    this.setState({ temporaryDescription });
  };

  enableDescriptionEdit = () => {
    this.setState({
      isDescriptionEditEnabled: true,
      temporaryDescription: this.state.description,
    });
  };

  disableDescriptionEdit = () => {
    this.setState({
      isDescriptionEditEnabled: false,
    });
  };

  enableCategoriesEdit = () => {
    this.setState({
      isCategoriesEditEnabled: true,
      temporaryCategories: this.state.categories,
    });
  };

  disableCategoriesEdit = () => {
    this.setState({
      isCategoriesEditEnabled: false,
    });
  };

  // handle description change
  handleCategoryChange = temporaryCategories => {
    this.setState({ temporaryCategories });
  };

  // change app categories
  handleCategorySave = () => {
    const { id } = this.props.app;
    const { temporaryCategories, categories } = this.state;

    const difference = temporaryCategories
      .filter(x => !categories.includes(x))
      .concat(categories.filter(x => !temporaryCategories.includes(x)));

    if (difference.length !== 0 && temporaryCategories.length !== 0) {
      const data = { categories: temporaryCategories };
      axios
        .put(
          window.location.origin +
            this.config.serverConfig.invoker.uri +
            this.config.serverConfig.invoker.publisher +
            '/applications/' +
            id,
          data,
        )
        .then(res => {
          if (res.status === 200) {
            const app = res.data.data;
            this.props.onUpdateApp('categories', temporaryCategories);
            notification.success({
              message: 'Saved!',
              description: 'App categories updated successfully!',
            });
            this.setState({
              loading: false,
              categories: app.categories,
              isCategoriesEditEnabled: false,
            });
          }
        })
        .catch(error => {
          if (
            error.hasOwnProperty('response') &&
            error.response.status === 401
          ) {
            message.error('You are not logged in');
            window.location.href = window.location.origin + '/publisher/login';
          } else {
            notification.error({
              message: 'There was a problem',
              duration: 0,
              description:
                'Error occurred while trying to updating categories.',
            });
          }

          this.setState({ loading: false });
        });
    }
  };

  enableTagsEdit = () => {
    this.setState({
      isTagsEditEnabled: true,
      temporaryTags: this.state.tags,
    });
  };

  disableTagsEdit = () => {
    this.setState({
      isTagsEditEnabled: false,
    });
  };

  // handle description change
  handleTagsChange = temporaryTags => {
    this.setState({ temporaryTags });
  };

  // change app tags
  handleTagsSave = () => {
    const { id } = this.props.app;
    const { temporaryTags, tags } = this.state;

    const difference = temporaryTags
      .filter(x => !tags.includes(x))
      .concat(tags.filter(x => !temporaryTags.includes(x)));

    if (difference.length !== 0 && temporaryTags.length !== 0) {
      const data = { tags: temporaryTags };
      axios
        .put(
          window.location.origin +
            this.config.serverConfig.invoker.uri +
            this.config.serverConfig.invoker.publisher +
            '/applications/' +
            id,
          data,
        )
        .then(res => {
          if (res.status === 200) {
            const app = res.data.data;
            this.props.onUpdateApp('tags', temporaryTags);
            notification.success({
              message: 'Saved!',
              description: 'App tags updated successfully!',
            });
            this.setState({
              loading: false,
              tags: app.tags,
              isTagsEditEnabled: false,
            });
          }
        })
        .catch(error => {
          if (
            error.hasOwnProperty('response') &&
            error.response.status === 401
          ) {
            message.error('You are not logged in');
            window.location.href = window.location.origin + '/publisher/login';
          } else {
            notification.error({
              message: 'There was a problem',
              duration: 0,
              description: 'Error occurred while trying to update tags',
            });
          }

          this.setState({ loading: false });
        });
    }
  };

  enableUnrestrictedRolesEdit = () => {
    this.setState({
      isUnrestrictedRolesEditEnabled: true,
      temporaryUnrestrictedRoles: this.state.unrestrictedRoles,
    });
  };

  disableUnrestrictedRolesEdit = () => {
    this.setState({
      isUnrestrictedRolesEditEnabled: false,
    });
  };

  handleUnrestrictedRolesChange = temporaryUnrestrictedRoles => {
    this.setState({ temporaryUnrestrictedRoles });
  };

  handleUnrestrictedRolesSave = () => {
    const { id } = this.props.app;
    const { temporaryUnrestrictedRoles, unrestrictedRoles } = this.state;

    temporaryUnrestrictedRoles
      .filter(x => !unrestrictedRoles.includes(x))
      .concat(
        unrestrictedRoles.filter(x => !temporaryUnrestrictedRoles.includes(x)),
      );

    const data = { unrestrictedRoles: temporaryUnrestrictedRoles };
    axios
      .put(
        window.location.origin +
          this.config.serverConfig.invoker.uri +
          this.config.serverConfig.invoker.publisher +
          '/applications/' +
          id,
        data,
      )
      .then(res => {
        if (res.status === 200) {
          const app = res.data.data;
          this.props.onUpdateApp(
            'unrestrictedRoles',
            temporaryUnrestrictedRoles,
          );
          notification.success({
            message: 'Saved!',
            description: 'App unrestricted roles updated successfully!',
          });
          this.setState({
            loading: false,
            unrestrictedRoles: app.unrestrictedRoles,
            isUnrestrictedRolesEditEnabled: false,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to update unrestricted roles.',
          true,
        );
        this.setState({
          loading: false,
        });
      });
  };

  // handle description save
  handleDescriptionSave = () => {
    const { id } = this.props.app;
    const { description, temporaryDescription } = this.state;

    if (
      temporaryDescription !== description &&
      temporaryDescription !== '<p><br></p>'
    ) {
      const data = { description: temporaryDescription };
      axios
        .put(
          window.location.origin +
            this.config.serverConfig.invoker.uri +
            this.config.serverConfig.invoker.publisher +
            '/applications/' +
            id,
          data,
        )
        .then(res => {
          if (res.status === 200) {
            const app = res.data.data;
            notification.success({
              message: 'Saved!',
              description: 'App description updated successfully!',
            });
            this.setState({
              loading: false,
              description: app.description,
              isDescriptionEditEnabled: false,
            });
          }
        })
        .catch(error => {
          if (
            error.hasOwnProperty('response') &&
            error.response.status === 401
          ) {
            message.error('You are not logged in');
            window.location.href = window.location.origin + '/publisher/login';
          } else {
            message.error('Something went wrong... :(');
          }

          this.setState({ loading: false });
        });
    } else {
      this.setState({ isDescriptionEditEnabled: false });
    }
  };

  render() {
    const { app, visible, onClose } = this.props;
    const {
      name,
      loading,
      description,
      isDescriptionEditEnabled,
      isCategoriesEditEnabled,
      isTagsEditEnabled,
      isUnrestrictedRolesEditEnabled,
      temporaryDescription,
      temporaryCategories,
      temporaryTags,
      temporaryUnrestrictedRoles,
      globalCategories,
      globalTags,
      globalUnrestrictedRoles,
      categories,
      tags,
      unrestrictedRoles,
    } = this.state;
    if (app == null) {
      return null;
    }
    const { id } = this.props.app;

    let avatar = null;

    if (app.applicationReleases.length === 0) {
      const avatarLetter = name.charAt(0).toUpperCase();
      avatar = (
        <Avatar
          shape="square"
          size={100}
          style={{
            marginBottom: 10,
            borderRadius: '28%',
            backgroundColor: pSBC(0.5, this.config.theme.primaryColor),
          }}
        >
          {avatarLetter}
        </Avatar>
      );
    } else {
      avatar = (
        <img
          style={{
            marginBottom: 10,
            width: 100,
            borderRadius: '28%',
            border: '1px solid #ddd',
          }}
          src={app.applicationReleases[0].iconPath}
        />
      );
    }

    return (
      <div>
        <Drawer
          placement="right"
          width={this.state.drawerWidth}
          closable={false}
          onClose={onClose}
          visible={visible}
        >
          <Spin spinning={loading} delay={500}>
            <div className="app-details-drawer">
              <Dropdown
                trigger={['click']}
                overlay={
                  <Menu>
                    <Menu.Item key="0">
                      <DeleteApp id={id} isDeletableApp={app.isDeletableApp} />
                    </Menu.Item>
                    <Menu.Item key="1">
                      <RetireApp id={id} isHideableApp={app.isHideableApp} />
                    </Menu.Item>
                    {this.config.androidEnterpriseToken !== null &&
                      isAuthorized(
                        this.config.user,
                        '/permission/admin/device-mgt/enterprise/user/modify',
                      ) && (
                        <Menu.Item key="2">
                          <ManagedConfigurationsIframe
                            isEnabled={app.isAndroidEnterpriseApp}
                            style={{ paddingTop: 16 }}
                            packageName={app.packageName}
                          />
                        </Menu.Item>
                      )}
                  </Menu>
                }
              >
                <MoreOutlined style={{ fontSize: 34 }} />
              </Dropdown>
            </div>
            <div style={{ textAlign: 'center' }}>
              {avatar}
              <Authorized
                permission="/permission/admin/app-mgt/publisher/application/update"
                yes={
                  <Title editable={{ onChange: this.handleNameSave }} level={2}>
                    {name}
                  </Title>
                }
                no={<Title level={2}>{name}</Title>}
              />
            </div>
            <Divider />
            <Text strong={true}>Releases </Text>
            <div className="releases-details">
              <List
                style={{ paddingTop: 16 }}
                grid={{ gutter: 16, column: 2 }}
                pagination={{
                  pageSize: 4, // number of releases per page
                  size: 'small',
                }}
                dataSource={app.applicationReleases}
                renderItem={release => (
                  <div className="app-release-cards">
                    <List.Item>
                      <Tooltip
                        title="Click to view full details"
                        placement="topRight"
                      >
                        <Link
                          to={{
                            pathname: `apps/releases/${release.uuid}`,
                            state: {
                              fullAppDetails: app.applicationReleases,
                            },
                          }}
                        >
                          <Card className="release-card">
                            <Meta
                              avatar={
                                <div>
                                  {release.currentStatus === 'PUBLISHED' ? (
                                    <Badge
                                      title="Published"
                                      count={
                                        <Tooltip title="Published">
                                          <CheckCircleOutlined
                                            style={{
                                              backgroundColor: '#52c41a',
                                              borderRadius: '50%',
                                              color: 'white',
                                            }}
                                          />
                                        </Tooltip>
                                      }
                                    >
                                      <Avatar
                                        size="large"
                                        shape="square"
                                        src={release.iconPath}
                                      />
                                    </Badge>
                                  ) : (
                                    <Avatar
                                      size="large"
                                      shape="square"
                                      src={release.iconPath}
                                    />
                                  )}
                                </div>
                              }
                              title={release.version}
                              description={
                                <div
                                  style={{
                                    fontSize: '0.8em',
                                  }}
                                  className="description-view"
                                >
                                  <CheckOutlined /> {release.currentStatus}
                                  <Divider type="vertical" />
                                  <UploadOutlined /> {release.releaseType}
                                  <Divider type="vertical" />
                                  <StarOutlined /> {release.rating.toFixed(1)}
                                </div>
                              }
                            />
                          </Card>
                        </Link>
                      </Tooltip>
                    </List.Item>
                  </div>
                )}
              />
            </div>

            {/* display add new release only if app type is enterprise*/}
            {app.type === 'ENTERPRISE' && (
              <Authorized
                permission="/permission/admin/app-mgt/publisher/application/update"
                yes={
                  <div>
                    <Divider dashed={true} />
                    <div style={{ paddingBottom: 16 }}>
                      <Text>Add new release for the application</Text>
                    </div>
                    <Link
                      to={{
                        pathname: `/publisher/apps/${app.deviceType}/${app.id}/add-release`,
                        state: {
                          appDetails: app.applicationReleases[0],
                          fullAppDetails: app.applicationReleases,
                        },
                      }}
                    >
                      <Button htmlType="button" type="primary" size="small">
                        Add
                      </Button>
                    </Link>
                  </div>
                }
              />
            )}
            <Divider dashed={true} />

            <Text strong={true}>Description </Text>
            <Authorized
              permission="/permission/admin/app-mgt/publisher/application/update"
              yes={
                !isDescriptionEditEnabled && (
                  <Text
                    style={{
                      color: this.config.theme.primaryColor,
                      cursor: 'pointer',
                    }}
                    onClick={this.enableDescriptionEdit}
                  >
                    <EditOutlined />
                  </Text>
                )
              }
            />

            {!isDescriptionEditEnabled && (
              <div>{ReactHtmlParser(description)}</div>
            )}

            {isDescriptionEditEnabled && (
              <div>
                <ReactQuill
                  theme="snow"
                  value={temporaryDescription}
                  onChange={this.handleDescriptionChange}
                  modules={modules}
                  formats={formats}
                  placeholder="Add description"
                  style={{
                    marginBottom: 10,
                    marginTop: 10,
                  }}
                />
                <Button
                  style={{ marginRight: 10 }}
                  size="small"
                  htmlType="button"
                  onClick={this.disableDescriptionEdit}
                >
                  Cancel
                </Button>
                <Button
                  size="small"
                  type="primary"
                  htmlType="button"
                  onClick={this.handleDescriptionSave}
                >
                  Save
                </Button>
              </div>
            )}

            <Divider dashed={true} />
            <Text strong={true}>Categories </Text>
            <Authorized
              permission="/permission/admin/app-mgt/publisher/application/update"
              yes={
                !isCategoriesEditEnabled && (
                  <Text
                    style={{
                      color: this.config.theme.primaryColor,
                      cursor: 'pointer',
                    }}
                    onClick={this.enableCategoriesEdit}
                  >
                    <EditOutlined />
                  </Text>
                )
              }
            />
            <br />
            <br />
            {isCategoriesEditEnabled && (
              <div>
                <Select
                  mode="multiple"
                  style={{ width: '100%' }}
                  placeholder="Please select categories"
                  onChange={this.handleCategoryChange}
                  value={temporaryCategories}
                >
                  {globalCategories}
                </Select>
                <div style={{ marginTop: 10 }}>
                  <Button
                    style={{ marginRight: 10 }}
                    size="small"
                    htmlType="button"
                    onClick={this.disableCategoriesEdit}
                  >
                    Cancel
                  </Button>
                  <Button
                    size="small"
                    type="primary"
                    htmlType="button"
                    onClick={this.handleCategorySave}
                  >
                    Save
                  </Button>
                </div>
              </div>
            )}
            {!isCategoriesEditEnabled && (
              <span>
                {categories.map(category => {
                  return (
                    <Tag
                      color={pSBC(0.3, this.config.theme.primaryColor)}
                      key={category}
                      style={{ marginBottom: 5 }}
                    >
                      {category}
                    </Tag>
                  );
                })}
              </span>
            )}

            <Divider dashed={true} />
            <Text strong={true}>Tags </Text>
            <Authorized
              permission="/permission/admin/app-mgt/publisher/application/update"
              yes={
                !isTagsEditEnabled && (
                  <Text
                    style={{
                      color: this.config.theme.primaryColor,
                      cursor: 'pointer',
                    }}
                    onClick={this.enableTagsEdit}
                  >
                    <EditOutlined />
                  </Text>
                )
              }
            />
            <br />
            <br />
            {isTagsEditEnabled && (
              <div>
                <Select
                  mode="tags"
                  style={{ width: '100%' }}
                  placeholder="Please select categories"
                  onChange={this.handleTagsChange}
                  value={temporaryTags}
                >
                  {globalTags}
                </Select>
                <div style={{ marginTop: 10 }}>
                  <Button
                    style={{ marginRight: 10 }}
                    size="small"
                    htmlType="button"
                    onClick={this.disableTagsEdit}
                  >
                    Cancel
                  </Button>
                  <Button
                    size="small"
                    type="primary"
                    htmlType="button"
                    onClick={this.handleTagsSave}
                  >
                    Save
                  </Button>
                </div>
              </div>
            )}
            {!isTagsEditEnabled && (
              <span>
                {tags.map(tag => {
                  return (
                    <Tag color="#34495e" key={tag} style={{ marginBottom: 5 }}>
                      {tag}
                    </Tag>
                  );
                })}
              </span>
            )}

            <Divider dashed={true} />
            <Text strong={true}>Unrestricted Roles</Text>
            <Authorized
              permission="/permission/admin/app-mgt/publisher/application/update"
              yes={
                !isUnrestrictedRolesEditEnabled && (
                  <Text
                    style={{
                      color: this.config.theme.primaryColor,
                      cursor: 'pointer',
                    }}
                    onClick={this.enableUnrestrictedRolesEdit}
                  >
                    <EditOutlined />
                  </Text>
                )
              }
            />
            <br />
            <br />
            {!unrestrictedRoles.length && (
              <Alert
                message="Application is not restricted to any roles."
                type="info"
                showIcon
              />
            )}
            {isUnrestrictedRolesEditEnabled && (
              <div>
                <Select
                  mode="multiple"
                  style={{ width: '100%' }}
                  placeholder="Please select unrestricted roles"
                  onChange={this.handleUnrestrictedRolesChange}
                  value={temporaryUnrestrictedRoles}
                >
                  {globalUnrestrictedRoles.map(unrestrictedRole => {
                    return (
                      <Option key={unrestrictedRole}>{unrestrictedRole}</Option>
                    );
                  })}
                </Select>
                <div style={{ marginTop: 10 }}>
                  <Button
                    style={{ marginRight: 10 }}
                    size="small"
                    htmlType="button"
                    onClick={this.disableUnrestrictedRolesEdit}
                  >
                    Cancel
                  </Button>
                  <Button
                    size="small"
                    type="primary"
                    htmlType="button"
                    onClick={this.handleUnrestrictedRolesSave}
                  >
                    Save
                  </Button>
                </div>
              </div>
            )}
            {!isUnrestrictedRolesEditEnabled && (
              <span>
                {unrestrictedRoles.map(unrestrictedRole => {
                  return (
                    <Tag
                      color={this.config.theme.primaryColor}
                      key={unrestrictedRole}
                      style={{ marginBottom: 5 }}
                    >
                      {unrestrictedRole}
                    </Tag>
                  );
                })}
              </span>
            )}

            <Authorized
              permission="/permission/admin/app-mgt/publisher/review/view"
              yes={
                <div>
                  <Divider dashed={true} />
                  <div className="app-rate">
                    {app.applicationReleases.length > 0 && (
                      <DetailedRating
                        type="app"
                        uuid={app.applicationReleases[0].uuid}
                      />
                    )}
                  </div>
                </div>
              }
            />
          </Spin>
        </Drawer>
      </div>
    );
  }
}

export default withConfigContext(AppDetailsDrawer);
