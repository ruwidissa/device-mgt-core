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
import { Form, notification, Spin, Card, Row, Col } from 'antd';
import axios from 'axios';
import { withRouter } from 'react-router-dom';
import { withConfigContext } from '../../../../../../components/ConfigContext';
import { handleApiError } from '../../../../../../services/utils/errorHandler';
import NewAppUploadForm from '../../../AddNewApp/components/AddNewAppForm/components/NewAppUploadForm';

const formConfig = {
  specificElements: {
    binaryFile: {
      required: true,
    },
  },
};

class AddNewReleaseFormComponent extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      loading: false,
      supportedOsVersions: [],
      application: null,
      release: null,
      deviceType: null,
      forbiddenErrors: {
        supportedOsVersions: false,
      },
    };
  }

  componentDidMount() {
    this.getSupportedOsVersions(this.props.deviceType);
  }

  getSupportedOsVersions = deviceType => {
    const config = this.props.context;
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          `/admin/device-types/${deviceType}/versions`,
      )
      .then(res => {
        if (res.status === 200) {
          let supportedOsVersions = JSON.parse(res.data.data);
          this.setState({
            supportedOsVersions,
            loading: false,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to load supported OS versions.',
          true,
        );
        if (error.hasOwnProperty('response') && error.response.status === 403) {
          const { forbiddenErrors } = this.state;
          forbiddenErrors.supportedOsVersions = true;
          this.setState({
            forbiddenErrors,
            loading: false,
          });
        } else {
          this.setState({
            loading: false,
          });
        }
      });
  };

  onSuccessReleaseData = releaseData => {
    const config = this.props.context;
    const { appId, deviceType } = this.props;
    this.setState({
      loading: true,
    });
    const { data, release } = releaseData;

    const json = JSON.stringify(release);
    const blob = new Blob([json], {
      type: 'application/json',
    });
    data.append('applicationRelease', blob);

    const url =
      window.location.origin +
      config.serverConfig.invoker.uri +
      config.serverConfig.invoker.publisher +
      '/applications/' +
      deviceType +
      '/ent-app/' +
      appId;
    axios
      .post(url, data)
      .then(res => {
        if (res.status === 201) {
          this.setState({
            loading: false,
          });

          notification.success({
            message: 'Done!',
            description: 'New release was added successfully',
          });
          const uuid = res.data.data.uuid;
          this.props.history.push('/publisher/apps/releases/' + uuid);
        } else {
          this.setState({
            loading: false,
          });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Sorry, we were unable to complete your request.',
        );
        this.setState({
          loading: false,
        });
      });
  };

  onClickBackButton = () => {
    this.props.history.push('/publisher/apps/');
  };

  render() {
    const { loading, supportedOsVersions, forbiddenErrors } = this.state;
    return (
      <div>
        <Spin tip="Uploading..." spinning={loading}>
          <Row>
            <Col span={17} offset={4}>
              <Card>
                <NewAppUploadForm
                  forbiddenErrors={forbiddenErrors}
                  formConfig={formConfig}
                  supportedOsVersions={supportedOsVersions}
                  onSuccessReleaseData={this.onSuccessReleaseData}
                  onClickBackButton={this.onClickBackButton}
                />
              </Card>
            </Col>
          </Row>
        </Spin>
      </div>
    );
  }
}

const AddReleaseForm = withRouter(
  Form.create({ name: 'add-new-release' })(AddNewReleaseFormComponent),
);
export default withConfigContext(AddReleaseForm);
