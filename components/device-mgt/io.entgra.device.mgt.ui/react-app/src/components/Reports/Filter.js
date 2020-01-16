/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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
import { Select } from 'antd';

class Filter extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      selectedItem: null,
    };
  }

  // Send updated filter value to Reports.js
  onChange = value => {
    this.setState({ selectedItem: value }, () => {
      if (this.props.dropDownName == 'Device Status') {
        this.props.updateFiltersValue(this.state.selectedItem, 'Device Status');
      } else {
        this.props.updateFiltersValue(
          this.state.selectedItem,
          'Device Ownership',
        );
      }
    });
  };

  render() {
    // Dynamically generate dropdown items from dropDownItems array
    let item = this.props.dropDownItems.map(data => (
      <Select.Option value={data} key={data}>
        {data}
      </Select.Option>
    ));
    return (
      <Select
        showSearch
        style={{ width: 200 }}
        placeholder={this.props.dropDownName}
        optionFilterProp="children"
        onChange={this.onChange}
        filterOption={(input, option) =>
          option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
        }
      >
        {item}
      </Select>
    );
  }
}

export default Filter;
