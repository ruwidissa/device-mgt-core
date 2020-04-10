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
import { Button, notification } from 'antd';
import axios from 'axios';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import { handleApiError } from '../../../../../../../../services/utils/errorHandler';

class SyncAndroidApps extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      loading: false,
    };
  }

  syncApps = () => {
    const config = this.props.context;
    this.setState({
      loading: true,
    });

    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          '/device-mgt/android/v1.0/enterprise/products/sync',
      )
      .then(res => {
        notification.success({
          message: 'Done!',
          description: 'Apps synced successfully!',
        });

        this.setState({
          loading: false,
        });
      })
      .catch(error => {
        handleApiError(error, 'Error occurred while syncing the apps.');
        this.setState({
          loading: false,
        });
      });
  };

  render() {
    const { loading } = this.state;
    return (
      <div style={{ display: 'inline-block', padding: 4 }}>
        <Button
          onClick={this.syncApps}
          loading={loading}
          style={{ marginTop: 16 }}
          type="primary"
          icon="sync"
        >
          Sync{loading && 'ing...'}
        </Button>
      </div>
    );
  }
}

export default withConfigContext(SyncAndroidApps);
