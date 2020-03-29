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
import { Divider, Icon, Tooltip } from 'antd';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import { Link } from 'react-router-dom';

class PolicyAction extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
  }

  render() {
    return (
      <div>
        <div>
          <Tooltip placement="top" title={'Edit Policy'}>
            <Link
              to={`/entgra/policy/edit/${this.props.selectedPolicyData.id}`}
            >
              <Icon type="edit" />
            </Link>
          </Tooltip>
          <Divider type="vertical" />
          <Tooltip placement="top" title={''}>
            <Link
              to={`/entgra/policy/view/${this.props.selectedPolicyData.id}`}
            >
              <Icon type="eye" />
            </Link>
          </Tooltip>
        </div>
      </div>
    );
  }
}

export default withConfigContext(PolicyAction);
