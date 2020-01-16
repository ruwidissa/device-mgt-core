import React from 'react';
import { Button, Divider, message, notification } from 'antd';
import TimeAgo from 'javascript-time-ago/modules/JavascriptTimeAgo';
import en from 'javascript-time-ago/locale/en';
import axios from 'axios';
import { withConfigContext } from '../../context/ConfigContext';

class EnrollAgent extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    TimeAgo.addLocale(en);
    this.state = {
      data: [],
      pagination: {},
      loading: false,
      selectedRows: [],
      visibleSelector: { display: 'none' },
    };
  }
  componentDidMount() {
    this.getConfigData();
  }

  onGetEnrollmentQR = () => {
    this.setState({
      visibleSelector: { display: 'block' },
    });
  };

  getConfigData = () => {
    axios
      .get(
        window.location.origin +
          this.config.serverConfig.invoker.uri +
          '/device-mgt/android/v1.0/configuration',
      )
      .catch(error => {
        if (error.hasOwnProperty('response') && error.response.status === 401) {
          // todo display a popop with error
          message.error('You are not logged in');
          window.location.href = window.location.origin + '/entgra/login';
        } else {
          notification.error({
            message: 'There was a problem',
            duration: 0,
            description: 'Error occurred while retrieving device groups.',
          });
        }
      });
  };

  render() {
    return (
      <div>
        <Divider orientation="left">Step 01 - Get your Android Agent.</Divider>
        <div>
          <p>
            The Android agent can be downloaded by using following QR. The
            generated QR code can be scanned, and the agent APK downloaded from
            the link, and transferred to the device and then installed.
          </p>
        </div>
        <div style={{ margin: '30px' }}>
          <Button type="primary" size={'default'}>
            Get Android Agent
          </Button>
        </div>
        <Divider orientation="left">
          Step 02 - Enroll the Android Agent.
        </Divider>

        <div>
          <p>
            {' '}
            Your device can be enrolled with Entgra IoTS automatically via QR
            code. To enroll first download agent as mentioned in Step 1 then
            proceed with the ENROLL WITH QR option from the device setup
            activity. Thereafter select the ownership configuration and scan the
            generated QR to complete the process.
          </p>
        </div>
        <div style={{ margin: '30px' }}>
          <Button
            type="primary"
            size={'default'}
            onClick={this.onGetEnrollmentQR}
          >
            Enroll Using QR
          </Button>
        </div>
      </div>
    );
  }
}

export default withConfigContext(EnrollAgent);
