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
import { message, notification, Select } from 'antd';
import axios from 'axios';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';

const { Option } = Select;

class AppVersionDropDown extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      selectItem: [],
      loading: false,
    };
  }

  componentDidMount() {
    if (this.props.packageName) {
      this.fetchVersionList();
    }
  }

  // Rerender component when parameters change
  componentDidUpdate(prevProps, prevState, snapshot) {
    if (prevProps.packageName !== this.props.packageName) {
      this.fetchVersionList();
    }
  }

  fetchVersionList = () => {
    const config = this.props.context;
    this.setState({ loading: true });
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/devices/application/' +
          this.props.packageName +
          '/versions',
      )
      .then(res => {
        if (res.status === 200) {
          let selectItem;
          selectItem = JSON.parse(res.data.data).map(data => (
            <Option value={data} key={data}>
              {data}
            </Option>
          ));
          this.setState({ selectItem, loading: false });
        }
      })
      .catch(error => {
        if (error.hasOwnProperty('response') && error.response.status === 401) {
          // todo display a popop with error
          message.error('You are not logged in');
          window.location.href = window.location.origin + '/entgra/login';
        } else {
          notification.error({
            message: 'There was a problem',
            duration: 0,
            description:
              'Error occurred while trying to load application list.',
          });
        }
      });
  };

  onChange = value => {
    this.props.getVersion(value);
  };

  render() {
    const { selectItem, loading } = this.state;
    return (
      <div>
        <Select
          defaultValue={'all'}
          showSearch
          style={{ width: 200 }}
          placeholder="Select app version"
          optionFilterProp="children"
          onChange={this.onChange}
          loading={loading}
          filterOption={(input, option) =>
            option.props.children.toLowerCase().indexOf(input.toLowerCase()) >=
            0
          }
        >
          <Option value={'all'} key={'all'}>
            All
          </Option>
          {selectItem}
        </Select>
      </div>
    );
  }
}

export default withConfigContext(AppVersionDropDown);
