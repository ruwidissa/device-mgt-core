/*
 * Copyright (c) 2023, Entgra (pvt) Ltd. (https://entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
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

package org.wso2.carbon.device.mgt.common;

/**
 * This class holds required parameters for a querying a sort by column in pagination.
 *
 */
public class SortColumn {
    String name;
    SortColumn.types type;

    public enum types {
        ASC, DESC
    }

    /**
     * ColumnName setter method
     * @param name of the column
     */
    public void setName(String name) { this.name = name; }

    /**
     * get the name of the column
     * @return name
     */
    public String getName() { return name; }

    /**
     * Column sort type
     * @param type of sort as ASC or DESC
     */
    public void setType(SortColumn.types type) { this.type = type; }

    /**
     * get column sort type
     * @return type of sort
     */
    public SortColumn.types getType() { return type; }

    @Override
    public String toString() {
        return "Column Name - " + this.name + ", Type - " + this.type ;
    }

}
