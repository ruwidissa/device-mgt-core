/*
 * Copyright (C) 2018 - 2023 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.operation.template.dao.impl.config.datasource;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class for holding data source configuration at parsing with JAXB.
 */
@XmlRootElement(name = "DataSourceConfiguration")
public class DataSourceConfig {

    private JNDILookupDefinition jndiLookupDefinition;

    @XmlElement(name = "JndiLookupDefinition", required = true)
    public JNDILookupDefinition getJndiLookupDefinition() {
        return jndiLookupDefinition;
    }

    public void setJndiLookupDefinition(JNDILookupDefinition jndiLookupDefinition) {
        this.jndiLookupDefinition = jndiLookupDefinition;
    }

}
