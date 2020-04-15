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
import { Modal, Icon, Table, Avatar } from 'antd';
import '../../styles.css';
import { withConfigContext } from '../../../../../../../../../../../../components/ConfigContext';

const columns = [
  {
    title: '',
    dataIndex: 'iconUrl',
    key: 'iconUrl',
    // eslint-disable-next-line react/display-name
    render: iconUrl => <Avatar shape="square" src={iconUrl} />,
  },
  {
    title: 'Name',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: 'Page',
    dataIndex: 'packageId',
    key: 'packageId',
  },
];

class AddAppsToClusterModal extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      visible: false,
      loading: false,
      selectedProducts: [],
      homePageId: null,
    };
  }

  showModal = () => {
    this.setState({
      visible: true,
    });
  };

  handleOk = () => {
    this.props.addSelectedProducts(this.state.selectedProducts);
    this.handleCancel();
  };

  handleCancel = () => {
    this.setState({
      visible: false,
    });
  };

  rowSelection = {
    onChange: (selectedRowKeys, selectedRows) => {
      this.setState({
        selectedProducts: selectedRows,
      });
    },
  };

  render() {
    const { pagination, loading } = this.state;
    return (
      <div>
        <div className="btn-add-new-wrapper">
          <div className="btn-add-new">
            <button className="btn" onClick={this.showModal}>
              <Icon style={{ position: 'relative' }} type="plus" />
            </button>
          </div>
          <div className="title">Add app</div>
        </div>
        <Modal
          title="Select Apps"
          width={640}
          visible={this.state.visible}
          onOk={this.handleOk}
          onCancel={this.handleCancel}
        >
          <Table
            columns={columns}
            rowKey={record => record.packageId}
            dataSource={this.props.unselectedProducts}
            scroll={{ x: 300 }}
            pagination={{
              ...pagination,
              size: 'small',
              // position: "top",
              showTotal: (total, range) =>
                `showing ${range[0]}-${range[1]} of ${total} pages`,
              showQuickJumper: true,
            }}
            loading={loading}
            onChange={this.handleTableChange}
            rowSelection={this.rowSelection}
          />
        </Modal>
      </div>
    );
  }
}

export default withConfigContext(AddAppsToClusterModal);
