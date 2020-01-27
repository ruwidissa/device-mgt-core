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
import { Select, message, notification } from 'antd';

import { withConfigContext } from '../../../context/ConfigContext';
import axios from 'axios';

const { Option } = Select;

class SelectPolicyDropDown extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
    this.state = {
      isOpen: false,
      currentPage: 1,
      data: [],
      pagination: {},
      loading: false,
    };
  }

  componentDidMount() {
    this.fetchPolicies();
  }

  // fetch data from api
  fetchPolicies = (params = {}) => {
    const config = this.props.context;
    this.setState({ loading: true });

    // send request to the invokerss
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/policies',
      )
      .then(res => {
        if (res.status === 200) {
          this.setState({
            loading: false,
            data: JSON.parse(res.data.data),
          });
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
            description: 'Error occurred while trying to load policies.',
          });
        }

        this.setState({ loading: false });
      });
  };

  handleChange = value => {
    this.props.getPolicyId(value);
  };

  render() {
    let item;
    if (this.state.data) {
      item = this.state.data.map(data => (
        <Select.Option value={data.id} key={data.id}>
          {data.profile.profileName}
        </Select.Option>
      ));
    }

    return (
      <Select
        defaultValue="all"
        style={{ width: 200 }}
        onChange={this.handleChange}
      >
        <Option value="all">All</Option>
        {item}
      </Select>
    );
  }
}

export default withConfigContext(SelectPolicyDropDown);
