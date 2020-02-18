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
import { Button, Tooltip, Popconfirm, Divider } from 'antd';

class BulkActionBar extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      selectedMultiple: false,
      selectedSingle: false,
      canDelete: true,
    };
  }

  // This method checks whether NON-REMOVED devices are selected
  onDeleteDeviceCall = () => {
    let tempDeleteState;
    for (let i = 0; i < this.props.selectedRows.length; i++) {
      if (this.props.selectedRows[i].enrolmentInfo.status != 'REMOVED') {
        tempDeleteState = false;
        break;
      }
      tempDeleteState = true;
    }
    this.setState({ canDelete: tempDeleteState });
  };

  onConfirmDelete = () => {
    if (this.state.canDelete) {
      this.props.deleteDevice();
    }
  };

  onConfirmDisenroll = () => {
    this.props.disenrollDevice();
  };

  onDeviceGroupCall = () => {
    this.props.getGroups();
  };

  render() {
    const isSelected = this.props.selectedRows.length > 0;
    const isSelectedSingle = this.props.selectedRows.length == 1;

    return (
      <div style={{ display: isSelected ? 'inline' : 'none', padding: '11px' }}>
        <Tooltip
          placement="bottom"
          title={'Delete Device'}
          autoAdjustOverflow={true}
        >
          <Popconfirm
            placement="topLeft"
            title={
              this.state.canDelete
                ? 'Are you sure you want to delete?'
                : 'You can only delete disenrolled devices'
            }
            onConfirm={this.onConfirmDelete}
            okText="Ok"
            cancelText="Cancel"
          >
            <Button
              type="link"
              shape="circle"
              icon="delete"
              size={'default'}
              onClick={this.onDeleteDeviceCall}
              disabled={!isSelected}
              style={{ margin: '2px' }}
            />
          </Popconfirm>
        </Tooltip>
        <Divider type="vertical" />
        <Tooltip placement="bottom" title={'Disenroll Device'}>
          <Popconfirm
            placement="topLeft"
            title={'Are you sure?'}
            onConfirm={this.onConfirmDisenroll}
            okText="Ok"
            disabled={!isSelectedSingle}
            cancelText="Cancel"
          >
            <Button
              type="link"
              shape="circle"
              icon="close"
              size={'default'}
              style={{
                display: isSelectedSingle ? 'inline' : 'none',
                margin: '2px',
              }}
            />
          </Popconfirm>
        </Tooltip>
        <Divider
          type="vertical"
          style={{ display: isSelectedSingle ? 'inline-block' : 'none' }}
        />
        <Tooltip placement="bottom" title={'Add to group'}>
          <Button
            type="link"
            shape="circle"
            icon="deployment-unit"
            size={'default'}
            onClick={this.onDeviceGroupCall}
            style={{ margin: '2px' }}
          />
        </Tooltip>
      </div>
    );
  }
}

export default BulkActionBar;
