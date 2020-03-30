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
      isPolicyActive: true,
    };
  }

  // This method checks whether active devices are selected
  onCheckPolicyStatus = () => {
    let tempIsPolicyActive;
    for (let i = 0; i < this.props.selectedRows.length; i++) {
      if (this.props.selectedRows[i].active) {
        tempIsPolicyActive = true;
        break;
      }
      tempIsPolicyActive = false;
    }
    this.setState({ isPolicyActive: tempIsPolicyActive });
  };

  onConfirmRemove = () => {
    if (!this.state.isPolicyActive) {
      this.props.removePolicy();
    }
  };

  onConfirmPublish = () => {
    if (!this.state.isPolicyActive) {
      this.props.publishPolicy();
    }
  };

  onConfirmUnpublish = () => {
    if (this.state.isPolicyActive) {
      this.props.unpublishPolicy();
    }
  };

  render() {
    const isSelected = this.props.selectedRows.length > 0;

    return (
      <div>
        <div style={{ padding: '5px' }}>
          <Tooltip placement="bottom" title={'Apply Changes to Device'}>
            <Popconfirm
              placement="topLeft"
              title={'Do you really want to apply changes to all policies?'}
              onConfirm={this.props.applyChanges}
              okText="Yes"
              cancelText="No"
            >
              <Button
                type="link"
                shape="circle"
                icon="check-circle"
                size={'default'}
                style={{ margin: '2px' }}
              >
                APPLY CHANGES TO DEVICES
              </Button>
            </Popconfirm>
          </Tooltip>
        </div>
        <div
          style={{ display: isSelected ? 'inline' : 'none', padding: '8px' }}
        >
          <Tooltip
            placement="bottom"
            title={'Remove'}
            autoAdjustOverflow={true}
          >
            <Popconfirm
              placement="topLeft"
              title={
                !this.state.isPolicyActive
                  ? 'Do you really want to remove the selected policy(s)?'
                  : 'You cannot select already active policies. Please deselect active policies and try again.'
              }
              onConfirm={this.onConfirmRemove}
              okText="Yes"
              cancelText="No"
            >
              <Button
                type="link"
                shape="circle"
                icon="delete"
                size={'default'}
                onClick={this.onCheckPolicyStatus}
                style={{ margin: '2px' }}
              >
                Remove
              </Button>
            </Popconfirm>
          </Tooltip>
          <Divider type="vertical" />
          <Tooltip placement="bottom" title={'Publish'}>
            <Popconfirm
              placement="topLeft"
              title={
                !this.state.isPolicyActive
                  ? 'Do you really want to publish the selected policy(s)??'
                  : 'You cannot select already active policies. Please deselect active policies and try again.'
              }
              okText="Yes"
              onConfirm={this.onConfirmPublish}
              cancelText="No"
            >
              <Button
                type="link"
                shape="circle"
                icon="import"
                onClick={this.onCheckPolicyStatus}
                size={'default'}
                style={{
                  margin: '2px',
                }}
              >
                Publish
              </Button>
            </Popconfirm>
          </Tooltip>
          <Divider type="vertical" />
          <Tooltip placement="bottom" title={'Unpublish'}>
            <Popconfirm
              placement="topLeft"
              title={
                this.state.isPolicyActive
                  ? 'Do you really want to unpublish the selected policy(s)?'
                  : 'You cannot select already inactive policies to be unpublished. Please deselect inactive policies and try again.'
              }
              okText="Yes"
              onConfirm={this.onConfirmUnpublish}
              cancelText="No"
            >
              <Button
                type="link"
                shape="circle"
                icon="export"
                onClick={this.onCheckPolicyStatus}
                size={'default'}
                style={{ margin: '2px' }}
              >
                Unpublish
              </Button>
            </Popconfirm>
          </Tooltip>
        </div>
      </div>
    );
  }
}

export default BulkActionBar;
