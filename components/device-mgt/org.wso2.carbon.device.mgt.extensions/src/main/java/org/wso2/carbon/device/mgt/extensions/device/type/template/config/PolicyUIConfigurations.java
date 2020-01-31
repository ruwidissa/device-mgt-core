/*
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Java class for Policies complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * <complexType name="Policies">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="Policy" type="{}Policy"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolicyUIConfigurations", propOrder = {
        "policy"
})
public class PolicyUIConfigurations {

    @XmlElement(name = "Policy")
    protected List<Policy> policy;

    /**
     * Gets the value of the policy property.
     *
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     * @return
     */
    public List<Policy> getPolicies() {
        if (policy == null) {
            policy = new ArrayList<Policy>();
        }
        return this.policy;
    }

    public void addPolicies(List<Policy> policies) {
        this.policy = policies;
    }

}

