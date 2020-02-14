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
import { withConfigContext } from '../../../context/ConfigContext';

const { Option } = Select;

class AppListDropDown extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      selectItem: [],
    };
  }

  componentDidMount() {
    this.fetchFullAppList();
  }

  fetchFullAppList = () => {
    const config = this.props.context;
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/devices/android/applications?offset=0&limit=-1',
      )
      .then(res => {
        if (res.status === 200) {
          let selectItem;
          selectItem = res.data.data.applicationList.map(data => (
            <Option value={data.id} key={data.applicationIdentifier}>
              {data.name.replace('%', ' ')}
            </Option>
          ));
          this.setState({ selectItem });
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

  onChange = (value, data) => {
    this.props.getAppList(data.key);
  };

  render() {
    const { selectItem } = this.state;
    return (
      <div>
        <Select
          showSearch
          style={{ width: 200 }}
          placeholder="Select an app"
          optionFilterProp="children"
          onChange={this.onChange}
          filterOption={(input, option) =>
            option.props.children.toLowerCase().indexOf(input.toLowerCase()) >=
            0
          }
        >
          {selectItem}
        </Select>
      </div>
    );
  }
}

export default withConfigContext(AppListDropDown);
