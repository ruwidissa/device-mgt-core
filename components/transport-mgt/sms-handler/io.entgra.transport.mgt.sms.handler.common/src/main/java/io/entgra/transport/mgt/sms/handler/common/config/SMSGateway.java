/*
 * Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.transport.mgt.sms.handler.common.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "Gateway")
public class SMSGateway {

    private String name;
    private String extensionClass;
    private boolean isDefault;
    private List<Property> properties;

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name = "extensionClass")
    public String getExtensionClass() {
        return extensionClass;
    }

    public void setExtensionClass(String extensionClass) {
        this.extensionClass = extensionClass;
    }

    @XmlAttribute(name = "isDefault")
    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @XmlElementWrapper(name = "Properties")
    @XmlElement(name = "Property")
    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    /**
     * Retrives the Property based on the provided property name
     * @param propertyName has the name of the Property to be retrieved
     * @return retrieved {@link Property}
     */
    public Property getPropertyByName(String propertyName) {
        for (Property property : properties) {
            if (propertyName.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }
}
