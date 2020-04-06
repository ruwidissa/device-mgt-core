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
import Authorized from '../../../../../../components/Authorized';
import ReleasePage from './components/ReleasePage';
import { Result } from 'antd';

class Release extends React.Component {
  render() {
    const { uuid, deviceType } = this.props.match.params;
    return (
      <Authorized
        permission="/permission/admin/app-mgt/store/application/view"
        yes={
          <ReleasePage
            uuid={uuid}
            deviceType={deviceType}
            changeSelectedMenuItem={this.props.changeSelectedMenuItem}
          />
        }
        no={
          <Result
            status="403"
            title="403"
            subTitle="You don't have permission to view apps."
          />
        }
      />
    );
  }
}

export default Release;
