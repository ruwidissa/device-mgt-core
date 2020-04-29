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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "Table")
public class Table {

    List<Column> columns;
    TableValidation tableValidation;
    RowValidation rowValidation;

    @XmlElementWrapper(name = "Columns")
    @XmlElement(name = "Column")
    public List<Column> getColumns() { return columns; }

    public void setColumns(List<Column> columns) { this.columns = columns; }

    @XmlElement(name = "TableValidation")
    public TableValidation getTableValidation() { return tableValidation; }

    public void setTableValidation(TableValidation tableValidation) { this.tableValidation = tableValidation; }

    @XmlElement(name = "RowValidation")
    public RowValidation getRowValidation() { return rowValidation; }

    public void setRowValidation(RowValidation rowValidation) { this.rowValidation = rowValidation; }


}
