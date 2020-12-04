/*
 * Copyright (C) 2020. Entgra (Pvt) Ltd, https://entgra.io
 * All Rights Reserved.
 *
 * Unauthorized copying/redistribution of this file, via any medium
 * is strictly prohibited.
 * Proprietary and confidential.
 *
 *  Licensed under the Entgra Commercial License,
 *  Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *  You may obtain a copy of the License at
 *  https://entgra.io/licenses/entgra-commercial/1.0
 */

import axios from 'axios';
import { notification } from 'antd';

export const getUiConfig = config => {
  return axios
    .get(window.location.origin + config.serverConfig.appUiConfigUri)
    .then(res => {
      return res.data;
    })
    .catch(error => {
      notification.error({
        message: 'There was a problem',
        duration: 0,
        description: 'Error occurred while trying to load UI configurations.',
      });
    });
};
