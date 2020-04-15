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
import { Modal, Button } from 'antd';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';

class GooglePlayIframe extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;

    this.state = {
      visible: false,
    };
  }

  showModal = () => {
    this.setState({
      visible: true,
    });
  };

  handleOk = e => {
    this.setState({
      visible: false,
    });
  };

  handleCancel = e => {
    this.setState({
      visible: false,
    });
  };

  render() {
    return (
      <div style={{ display: 'inline-block', padding: 4 }}>
        <Button type="primary" onClick={this.showModal}>
          Approve Applications
        </Button>
        <Modal
          title={null}
          visible={this.state.visible}
          onOk={this.handleOk}
          onCancel={this.handleCancel}
          width={740}
          footer={null}
        >
          <iframe
            style={{
              height: 720,
              border: 0,
              width: '100%',
            }}
            src={
              'https://play.google.com/work/embedded/search?token=' +
              this.config.androidEnterpriseToken +
              '&mode=APPROVE&showsearchbox=TRUE'
            }
          />
        </Modal>
      </div>
    );
  }
}

export default withConfigContext(GooglePlayIframe);
