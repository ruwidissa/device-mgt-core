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
import { Modal, Button, Tag, List, Typography } from 'antd';
import pSBC from 'shade-blend-color';
import { withConfigContext } from '../../../../../../../../../../components/ConfigContext';

const { Text } = Typography;

class LifeCycleDetailsModal extends React.Component {
  constructor(props) {
    super(props);
    this.state = { visible: false };
  }

  showModal = () => {
    this.setState({
      visible: true,
    });
  };

  handleCancel = e => {
    this.setState({
      visible: false,
    });
  };

  render() {
    const config = this.props.context;
    const lifeCycleConfig = config.lifecycle;
    const { lifecycle } = this.props;
    return (
      <div>
        <Button size="small" icon="question-circle" onClick={this.showModal}>
          Learn more
        </Button>
        <Modal
          title="Lifecycle"
          visible={this.state.visible}
          footer={null}
          onCancel={this.handleCancel}
        >
          <List
            itemLayout="horizontal"
            dataSource={Object.keys(lifecycle)}
            renderItem={lifecycleState => {
              let text = '';
              let footerText = '';
              let nextProceedingStates = [];

              if (lifeCycleConfig.hasOwnProperty(lifecycleState)) {
                text = lifeCycleConfig[lifecycleState].text;
              }
              if (
                lifecycle[lifecycleState].hasOwnProperty('proceedingStates')
              ) {
                nextProceedingStates =
                  lifecycle[lifecycleState].proceedingStates;
                footerText =
                  'You can only proceed to one of the following states:';
              }

              return (
                <List.Item>
                  <List.Item.Meta title={lifecycleState} />
                  {text}
                  <br />
                  <Text type="secondary">{footerText}</Text>
                  <div>
                    {nextProceedingStates.map(lifecycleState => {
                      return (
                        <Tag
                          key={lifecycleState}
                          style={{ margin: 5 }}
                          color={pSBC(0.3, config.theme.primaryColor)}
                        >
                          {lifecycleState}
                        </Tag>
                      );
                    })}
                  </div>
                </List.Item>
              );
            }}
          />
        </Modal>
      </div>
    );
  }
}

export default withConfigContext(LifeCycleDetailsModal);
