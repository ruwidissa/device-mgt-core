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
  Button,
  Col,
  Divider,
  Icon,
  message,
  notification,
  Popconfirm,
  Row,
  Spin,
  Tooltip,
  Typography,
} from 'antd';

import './styles.css';
import axios from 'axios';
import { withConfigContext } from '../../../../../../../../../../components/ConfigContext';
import AddAppsToClusterModal from './components/AddAppsToClusterModal';
import { handleApiError } from '../../../../../../../../../../services/utils/errorHandler';

const { Title } = Typography;

class Cluster extends React.Component {
  constructor(props) {
    super(props);
    const { cluster, pageId } = this.props;
    this.originalCluster = Object.assign({}, cluster);
    const { name, products, clusterId } = cluster;
    this.clusterId = clusterId;
    this.pageId = pageId;
    this.state = {
      name,
      products,
      isSaveable: false,
      loading: false,
    };
  }

  handleNameChange = name => {
    this.setState({
      name,
    });
    if (name !== this.originalCluster.name) {
      this.setState({
        isSaveable: true,
      });
    }
  };

  isProductsChanged = currentProducts => {
    let isChanged = false;
    const originalProducts = this.originalCluster.products;
    if (currentProducts.length === originalProducts.length) {
      for (let i = 0; i < currentProducts.length; i++) {
        if (currentProducts[i].packageId !== originalProducts[i].packageId) {
          isChanged = true;
          break;
        }
      }
    } else {
      isChanged = true;
    }
    return isChanged;
  };

  swapProduct = (index, swapIndex) => {
    const products = [...this.state.products];
    if (swapIndex !== -1 && index < products.length) {
      // swap elements
      [products[index], products[swapIndex]] = [
        products[swapIndex],
        products[index],
      ];

      this.setState({
        products,
      });

      this.setState({
        isSaveable: this.isProductsChanged(products),
      });
    }
  };

  removeProduct = index => {
    const products = [...this.state.products];
    products.splice(index, 1);
    this.setState({
      products,
      isSaveable: true,
    });
  };

  getCurrentCluster = () => {
    const { products, name } = this.state;
    return {
      pageId: this.pageId,
      clusterId: this.clusterId,
      name: name,
      products: products,
      orderInPage: this.props.orderInPage,
    };
  };

  resetChanges = () => {
    const cluster = this.originalCluster;
    const { name, products } = cluster;

    this.setState({
      loading: false,
      name,
      products,
      isSaveable: false,
    });
  };

  updateCluster = () => {
    const config = this.props.context;

    const cluster = this.getCurrentCluster();
    this.setState({ loading: true });

    axios
      .put(
        window.location.origin +
          config.serverConfig.invoker.uri +
          '/device-mgt/android/v1.0/enterprise/store-layout/cluster',
        cluster,
      )
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Saved!',
            description: 'Cluster updated successfully!',
          });
          const cluster = res.data.data;

          this.originalCluster = Object.assign({}, cluster);

          this.resetChanges();
          if (this.props.toggleAddNewClusterVisibility !== undefined) {
            this.props.toggleAddNewClusterVisibility(false);
          }
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to update the cluster.',
        );
        this.setState({ loading: false });
      });
  };

  deleteCluster = () => {
    const config = this.props.context;
    this.setState({ loading: true });

    axios
      .delete(
        window.location.origin +
          config.serverConfig.invoker.uri +
          `/device-mgt/android/v1.0/enterprise/store-layout/cluster/${this.clusterId}/page/` +
          this.pageId,
      )
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Done!',
            description: 'Cluster deleted successfully!',
          });

          this.setState({
            loading: false,
          });

          this.props.removeLoadedCluster(this.clusterId);
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to update the cluster.',
        );
        this.setState({ loading: false });
      });
  };

  getUnselectedProducts = () => {
    const { applications } = this.props;
    const selectedProducts = this.state.products;

    // get a copy from all products
    const unSelectedProducts = [...applications];

    // remove selected products from unselected products
    selectedProducts.forEach(selectedProduct => {
      for (let i = 0; i < unSelectedProducts.length; i++) {
        if (selectedProduct.packageId === unSelectedProducts[i].packageId) {
          // remove item from array
          unSelectedProducts.splice(i, 1);
        }
      }
    });

    return unSelectedProducts;
  };

  addSelectedProducts = products => {
    this.setState({
      products: [...this.state.products, ...products],
      isSaveable: products.length > 0,
    });
  };

  cancelAddingNewCluster = () => {
    this.resetChanges();
    this.props.toggleAddNewClusterVisibility(false);
  };

  saveNewCluster = () => {
    const config = this.props.context;

    const cluster = this.getCurrentCluster();
    this.setState({ loading: true });

    axios
      .post(
        window.location.origin +
          config.serverConfig.invoker.uri +
          '/device-mgt/android/v1.0/enterprise/store-layout/cluster',
        cluster,
      )
      .then(res => {
        if (res.status === 200) {
          notification.success({
            message: 'Saved!',
            description: 'Cluster updated successfully!',
          });

          const cluster = res.data.data;

          this.resetChanges();
          this.props.addSavedClusterToThePage(cluster);
        }
      })
      .catch(error => {
        if (error.hasOwnProperty('response') && error.response.status === 401) {
          message.error('You are not logged in');
          window.location.href = window.location.origin + '/publisher/login';
        } else {
          notification.error({
            message: 'There was a problem',
            duration: 0,
            description: 'Error occurred while trying to update the cluster.',
          });
        }

        this.setState({ loading: false });
      });
  };

  render() {
    const { name, products, loading } = this.state;
    const unselectedProducts = this.getUnselectedProducts();
    const { isTemporary, index } = this.props;
    const Product = ({ product, index }) => {
      const { packageId } = product;
      let imageSrc = '';
      const iconUrl = product.iconUrl;
      // check if the icon url is an url or google image id
      if (iconUrl.startsWith('http')) {
        imageSrc = iconUrl;
      } else {
        imageSrc = `https://lh3.googleusercontent.com/${iconUrl}=s240-rw`;
      }
      return (
        <div className="product">
          <div className="arrow">
            {this.props.hasPermissionToManage && (
              <button
                disabled={index === 0}
                className="btn"
                onClick={() => {
                  this.swapProduct(index, index - 1);
                }}
              >
                <Icon type="caret-left" theme="filled" />
              </button>
            )}
          </div>
          <div className="product-icon">
            <img src={imageSrc} />
            <Tooltip title={packageId}>
              <div className="title">{packageId}</div>
            </Tooltip>
          </div>
          {this.props.hasPermissionToManage && (
            <>
              <div className="arrow">
                <button
                  disabled={index === products.length - 1}
                  onClick={() => {
                    this.swapProduct(index, index + 1);
                  }}
                  className="btn btn-right"
                >
                  <Icon type="caret-right" theme="filled" />
                </button>
              </div>
              <div className="delete-btn">
                <button
                  className="btn"
                  onClick={() => {
                    this.removeProduct(index);
                  }}
                >
                  <Icon type="close-circle" theme="filled" />
                </button>
              </div>
            </>
          )}
        </div>
      );
    };

    return (
      <div className="cluster" id={this.props.orderInPage}>
        <Spin spinning={loading}>
          <Row>
            <Col span={16}>
              {this.props.hasPermissionToManage && (
                <Title editable={{ onChange: this.handleNameChange }} level={4}>
                  {name}
                </Title>
              )}
              {!this.props.hasPermissionToManage && (
                <Title level={4}>{name}</Title>
              )}
            </Col>
            <Col span={8}>
              {!isTemporary && this.props.hasPermissionToManage && (
                <div style={{ float: 'right' }}>
                  <Tooltip title="Move Up">
                    <Button
                      type="link"
                      icon="caret-up"
                      size="large"
                      onClick={() => {
                        this.props.swapClusters(index, index - 1);
                      }}
                      htmlType="button"
                    />
                  </Tooltip>
                  <Tooltip title="Move Down">
                    <Button
                      type="link"
                      icon="caret-down"
                      size="large"
                      onClick={() => {
                        this.props.swapClusters(index, index + 1);
                      }}
                      htmlType="button"
                    />
                  </Tooltip>
                  <Tooltip title="Delete Cluster">
                    <Popconfirm
                      title="Are you sure?"
                      okText="Yes"
                      cancelText="No"
                      onConfirm={this.deleteCluster}
                    >
                      <Button
                        type="danger"
                        icon="delete"
                        shape="circle"
                        htmlType="button"
                      />
                    </Popconfirm>
                  </Tooltip>
                </div>
              )}
            </Col>
          </Row>
          <div className="products-row">
            {this.props.hasPermissionToManage && (
              <AddAppsToClusterModal
                addSelectedProducts={this.addSelectedProducts}
                unselectedProducts={unselectedProducts}
              />
            )}
            {products.map((product, index) => {
              return (
                <Product
                  key={product.packageId}
                  product={product}
                  index={index}
                />
              );
            })}
          </div>
          {this.props.hasPermissionToManage && (
            <Row>
              <Col>
                {isTemporary && (
                  <div>
                    <Button onClick={this.cancelAddingNewCluster}>
                      Cancel
                    </Button>
                    <Divider type="vertical" />
                    <Tooltip
                      title={
                        products.length === 0
                          ? 'You must add applications to the cluster before saving'
                          : ''
                      }
                    >
                      <Button
                        disabled={products.length === 0}
                        onClick={this.saveNewCluster}
                        htmlType="button"
                        type="primary"
                      >
                        Save
                      </Button>
                    </Tooltip>
                  </div>
                )}
                {!isTemporary && (
                  <div>
                    <Button
                      onClick={this.resetChanges}
                      disabled={!this.state.isSaveable}
                    >
                      Cancel
                    </Button>
                    <Divider type="vertical" />
                    <Button
                      onClick={this.updateCluster}
                      htmlType="button"
                      type="primary"
                      disabled={!this.state.isSaveable}
                    >
                      Save
                    </Button>
                  </div>
                )}
              </Col>
            </Row>
          )}
        </Spin>
      </div>
    );
  }
}

export default withConfigContext(Cluster);
