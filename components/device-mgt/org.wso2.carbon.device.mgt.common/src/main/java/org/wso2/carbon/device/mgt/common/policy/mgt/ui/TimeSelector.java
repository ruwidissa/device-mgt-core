/* Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.common.policy.mgt.ui;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TimeSelector")
public class TimeSelector {

    private int initialDataIndex;
    private int startOptionValue;
    private int lastOptionValue;
    private int valueDifference;

    @XmlElement(name = "InitialDataIndex")
    public int getInitialDataIndex() { return initialDataIndex; }

    public void setInitialDataIndex(int initialDataIndex) { this.initialDataIndex = initialDataIndex; }

    @XmlElement(name = "StartOptionValue")
    public int getStartOptionValue() { return startOptionValue; }

    public void setStartOptionValue(int startOptionValue) { this.startOptionValue = startOptionValue; }

    @XmlElement(name = "LastOptionValue")
    public int getLastOptionValue() { return lastOptionValue; }

    public void setLastOptionValue(int lastOptionValue) { this.lastOptionValue = lastOptionValue; }

    @XmlElement(name = "ValueDifference")
    public int getValueDifference() { return valueDifference; }

    public void setValueDifference(int valueDifference) { this.valueDifference = valueDifference; }
}
