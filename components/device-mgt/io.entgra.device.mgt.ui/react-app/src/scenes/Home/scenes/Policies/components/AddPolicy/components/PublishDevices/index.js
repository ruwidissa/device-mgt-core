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
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import { Button, Col, Form, Input } from 'antd';
const { TextArea } = Input;

class PublishDevices extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
  }

  onClickSavePolicy = (event, isPublish, formName) => {
    this.props.form.validateFields((err, values) => {
      if (!err) {
        if (isPublish) {
          Object.assign(values, { active: isPublish });
        }
        this.props.getPolicyPayloadData(formName, values);
      }
    });
  };

  render() {
    const { getFieldDecorator } = this.props.form;
    return (
      <div>
        <Form.Item
          label={'Set a name to your policy *'}
          style={{ display: 'block' }}
        >
          {getFieldDecorator('policyName', {
            rules: [
              {
                pattern: new RegExp('^.{1,30}$'),
                message: 'Should be 1-to-30 characters long',
              },
            ],
          })(<Input placeholder={'Should be 1 to 30 characters long'} />)}
        </Form.Item>
        <Form.Item label={'Add a Description'} style={{ display: 'block' }}>
          {getFieldDecorator('description', {})(<TextArea rows={8} />)}
        </Form.Item>
        <Col span={16} offset={18}>
          <div style={{ marginTop: 24 }}>
            <Button style={{ marginRight: 8 }} onClick={this.props.getPrevStep}>
              Back
            </Button>
            <Button
              type="primary"
              style={{ marginRight: 8 }}
              onClick={e =>
                this.onClickSavePolicy(e, true, 'publishDevicesData')
              }
            >
              Save & Publish
            </Button>
            <Button
              type="primary"
              onClick={e =>
                this.onClickSavePolicy(e, false, 'publishDevicesData')
              }
            >
              Save
            </Button>
          </div>
        </Col>
      </div>
    );
  }
}

export default withConfigContext(Form.create()(PublishDevices));
