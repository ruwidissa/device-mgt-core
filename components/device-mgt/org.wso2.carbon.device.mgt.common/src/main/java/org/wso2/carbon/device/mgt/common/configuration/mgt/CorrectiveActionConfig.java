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

package org.wso2.carbon.device.mgt.common.configuration.mgt;

import java.util.List;

public class CorrectiveActionConfig {

    private List<String> mailReceivers;
    private String mailSubject;
    private String mailBody;
    private List<String> actionTypes;

    public List<String> getMailReceivers() {
        return mailReceivers;
    }

    public void setMailReceivers(List<String> mailReceivers) {
        this.mailReceivers = mailReceivers;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public String getMailBody() {
        return mailBody;
    }

    public void setMailBody(String mailBody) {
        this.mailBody = mailBody;
    }

    public List<String> getActionTypes() {
        return actionTypes;
    }

    public void setActionTypes(List<String> actionTypes) {
        this.actionTypes = actionTypes;
    }
}
