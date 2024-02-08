/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.common.bean;

import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.DefaultAccessPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.EmailOutlookAccessPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.POPIMAPAccessPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.WebOutlookAccessPolicy;

import java.util.Set;

public class AccessPolicy {
    private DefaultAccessPolicy defaultAccessPolicy;
    private Set<EmailOutlookAccessPolicy> emailOutlookAccessPolicy;
    private POPIMAPAccessPolicy POPIMAPAccessPolicy;
    private WebOutlookAccessPolicy webOutlookAccessPolicy;

    public DefaultAccessPolicy getDefaultAccessPolicy() {
        return defaultAccessPolicy;
    }

    public void setDefaultAccessPolicy(DefaultAccessPolicy defaultAccessPolicy) {
        this.defaultAccessPolicy = defaultAccessPolicy;
    }

    public Set<EmailOutlookAccessPolicy> getEmailOutlookAccessPolicy() {
        return emailOutlookAccessPolicy;
    }

    public void setEmailOutlookAccessPolicy(Set<EmailOutlookAccessPolicy> emailOutlookAccessPolicy) {
        this.emailOutlookAccessPolicy = emailOutlookAccessPolicy;
    }

    public POPIMAPAccessPolicy getPOPIMAPAccessPolicy() {
        return POPIMAPAccessPolicy;
    }

    public void setPOPIMAPAccessPolicy(POPIMAPAccessPolicy POPIMAPAccessPolicy) {
        this.POPIMAPAccessPolicy = POPIMAPAccessPolicy;
    }

    public WebOutlookAccessPolicy getWebOutlookAccessPolicy() {
        return webOutlookAccessPolicy;
    }

    public void setWebOutlookAccessPolicy(WebOutlookAccessPolicy webOutlookAccessPolicy) {
        this.webOutlookAccessPolicy = webOutlookAccessPolicy;
    }
}
