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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Column {
    @XmlElement(name = "Name", required = true)
    private String name;

    @XmlAttribute(name = "type", required = true)
    protected String type;

    @XmlAttribute(name = "key", required = true)
    protected String key;

    @XmlElement(name = "Others")
    protected OptionalData others;

    public String getColumnName() {
        return name;
    }

    public void setColumnName(String name) {
        this.name = name;
    }

    public String getColumnType() {
        return type;
    }

    public void setColumnType(String type) {
        this.type = type;
    }

    public String getColumnKey() {
        return key;
    }

    public void setColumnKey(String key) {
        this.key = key;
    }

    public OptionalData getOtherColumnData() {
        return others;
    }

    public void getOtherColumnData(OptionalData others) {
        this.others = others;
    }

}