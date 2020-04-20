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

import react from 'react';
import { withConfigContext } from '../context/ConfigContext';

class Authorized extends react.Component {
  constructor(props) {
    super(props);
  }

  isAuthorized = (user, permission) => {
    if (!user || !permission) {
      return false;
    }
    return user.permissions.includes(permission);
  };

  render() {
    return this.isAuthorized(this.props.context.user, this.props.permission)
      ? this.props.yes
      : this.props.no;
  }
}

Authorized.defaultProps = {
  yes: () => null,
  no: () => null,
};
export default withConfigContext(Authorized);
