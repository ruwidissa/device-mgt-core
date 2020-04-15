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
  Icon,
  Card,
  Badge,
  Tooltip,
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

const { Meta } = Card;
const { Text, Title } = Typography;
const { Option } = Select;

const IconText = ({ type, text }) => (
  <span>
    <Icon type={type} style={{ marginRight: 8 }} />
    {text}
  </span>
);

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
    const drawerWidth = window.innerWidth <= 770 ? '80%' : '40%';

    this.state = {
      loading: false,
      name: '',
      description: null,
      globalCategories: [],
      globalTags: [],
      categories: [],
      tags: [],
      temporaryDescription: null,
      temporaryCategories: [],
      temporaryTags: [],
      isDescriptionEditEnabled: false,
      isCategoriesEditEnabled: false,
      isTagsEditEnabled: false,
      drawer: null,
      drawerWidth,
    };
  }

  componentDidMount() {
    this.getCategories();
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (prevProps.app !== this.props.app) {
      const { name, description, tags, categories } = this.props.app;
      this.setState({
        name,
        description,
        tags,
        categories,
        isDescriptionEditEnabled: false,
        isCategoriesEditEnabled: false,
        isTagsEditEnabled: false,
      });
    }
  }

  getCategories = () => {
    const config = this.props.context;
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.publisher +
          '/applications/categories',
      )
      .then(res => {
        if (res.status === 200) {
          const categories = JSON.parse(res.data.data);
          this.getTags();
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

  // change the app name
  handleNameSave = name => {
    const config = this.props.context;
    const { id } = this.props.app;
    if (name !== this.state.name && name !== '') {
      const data = { name: name };
      axios
        .put(
          window.location.origin +
            config.serverConfig.invoker.uri +
            config.serverConfig.invoker.publisher +
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
    const config = this.props.context;
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
            config.serverConfig.invoker.uri +
            config.serverConfig.invoker.publisher +
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
    const config = this.props.context;
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
            config.serverConfig.invoker.uri +
            config.serverConfig.invoker.publisher +
            '/applications/' +
            id,
          data,
        )
        .then(res => {
          if (res.status === 200) {
            const app = res.data.data;
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

  // handle description save
  handleDescriptionSave = () => {
    const config = this.props.context;
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
            config.serverConfig.invoker.uri +
            config.serverConfig.invoker.publisher +
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
    const config = this.props.context;
    const { app, visible, onClose } = this.props;
    const {
      name,
      loading,
      description,
      isDescriptionEditEnabled,
      isCategoriesEditEnabled,
      isTagsEditEnabled,
      temporaryDescription,
      temporaryCategories,
      temporaryTags,
      globalCategories,
      globalTags,
      categories,
      tags,
    } = this.state;
    if (app == null) {
      return null;
    }

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
            backgroundColor: pSBC(0.5, config.theme.primaryColor),
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
            <div style={{ textAlign: 'center' }}>
              {avatar}
              <Title editable={{ onChange: this.handleNameSave }} level={2}>
                {name}
              </Title>
            </div>
            <Divider />
            {/* display manage config button only if the app is public android app*/}
            {app.isAndroidEnterpriseApp &&
              config.androidEnterpriseToken !== null && (
                <div>
                  <div>
                    <Text strong={true}>Set up managed configurations</Text>
                  </div>
                  <div style={{ paddingTop: 16 }}>
                    <Text>
                      If you are developing apps for the enterprise market, you
                      may need to satisfy particular requirements set by a
                      organization&apos;s policies. Managed configurations,
                      previously known as application restrictions, allow the
                      organization&apos;s IT admin to remotely specify settings
                      for apps. This capability is particularly useful for
                      organization-approved apps deployed to a work profile.
                    </Text>
                  </div>
                  <br />
                  <ManagedConfigurationsIframe
                    style={{ paddingTop: 16 }}
                    packageName={app.packageName}
                  />
                  <Divider dashed={true} />
                </div>
              )}
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
                        <Link to={'apps/releases/' + release.uuid}>
                          <Card className="release-card">
                            <Meta
                              avatar={
                                <div>
                                  {release.currentStatus === 'PUBLISHED' ? (
                                    <Badge
                                      title="Published"
                                      count={
                                        <Tooltip title="Published">
                                          <Icon
                                            style={{
                                              backgroundColor: '#52c41a',
                                              borderRadius: '50%',
                                              color: 'white',
                                            }}
                                            type="check-circle"
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
                                    fontSize: '0.7em',
                                  }}
                                  className="description-view"
                                >
                                  <IconText
                                    type="check"
                                    text={release.currentStatus}
                                  />
                                  <Divider type="vertical" />
                                  <IconText
                                    type="upload"
                                    text={release.releaseType}
                                  />
                                  <Divider type="vertical" />
                                  <IconText
                                    type="star-o"
                                    text={release.rating.toFixed(1)}
                                  />
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
              <div>
                <Divider dashed={true} />
                <div style={{ paddingBottom: 16 }}>
                  <Text>Add new release for the application</Text>
                </div>
                <Link
                  to={`/publisher/apps/${app.deviceType}/${app.id}/add-release`}
                >
                  <Button htmlType="button" type="primary" size="small">
                    Add
                  </Button>
                </Link>
              </div>
            )}
            <Divider dashed={true} />

            <Text strong={true}>Description </Text>
            {!isDescriptionEditEnabled && (
              <Text
                style={{
                  color: config.theme.primaryColor,
                  cursor: 'pointer',
                }}
                onClick={this.enableDescriptionEdit}
              >
                <Icon type="edit" />
              </Text>
            )}

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
            {!isCategoriesEditEnabled && (
              <Text
                style={{ color: config.theme.primaryColor, cursor: 'pointer' }}
                onClick={this.enableCategoriesEdit}
              >
                <Icon type="edit" />
              </Text>
            )}
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
                      color={pSBC(0.3, config.theme.primaryColor)}
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
            {!isTagsEditEnabled && (
              <Text
                style={{ color: config.theme.primaryColor, cursor: 'pointer' }}
                onClick={this.enableTagsEdit}
              >
                <Icon type="edit" />
              </Text>
            )}
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

            <div className="app-rate">
              {app.applicationReleases.length > 0 && (
                <DetailedRating
                  type="app"
                  uuid={app.applicationReleases[0].uuid}
                />
              )}
            </div>
          </Spin>
        </Drawer>
      </div>
    );
  }
}

export default withConfigContext(AppDetailsDrawer);
