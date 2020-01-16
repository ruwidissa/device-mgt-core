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
import { DatePicker } from 'antd';
import moment from 'moment';

class DateRangePicker extends React.Component {
  constructor(props) {
    super(props);
  }

  // Send updated date range to Reports.js when duration change
  onChange = (dates, dateStrings) => {
    this.props.updateDurationValue(dateStrings[0], dateStrings[1]);
  };

  render() {
    const { RangePicker } = DatePicker;
    return (
      <RangePicker
        ranges={{
          Today: [moment(), moment()],
          Yesterday: [
            moment().subtract(1, 'days'),
            moment().subtract(1, 'days'),
          ],
          'Last 7 Days': [moment().subtract(6, 'days'), moment()],
          'Last 30 Days': [moment().subtract(29, 'days'), moment()],
          'This Month': [moment().startOf('month'), moment().endOf('month')],
          'Last Month': [
            moment()
              .subtract(1, 'month')
              .startOf('month'),
            moment()
              .subtract(1, 'month')
              .endOf('month'),
          ],
        }}
        format="YYYY-MM-DD"
        onChange={this.onChange}
      />
    );
  }
}

export default DateRangePicker;
