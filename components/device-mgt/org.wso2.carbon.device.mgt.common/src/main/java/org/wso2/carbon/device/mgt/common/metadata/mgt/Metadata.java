/*
 *  Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.common.metadata.mgt;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DTO of Metadata object which is used to communicate Operation metadata to MDM core.
 */
@ApiModel(value = "Metadata", description = "This is used to communicate Operation metadata to MDM.")
public class Metadata {

    /**
     * Data types available in metadata repository.
     */
    public enum DataType {
        INT, STRING
    }

    @JsonProperty(value = "dataType")
    @ApiModelProperty(name = "dataType", value = "Defines the data type related to the metadata", required = true)
    private DataType dataType = DataType.STRING;

    @JsonProperty(value = "metaKey", required = true)
    @ApiModelProperty(name = "metaKey", value = "Defines the device Name related to the metadata.")
    private String metaKey;

    @JsonProperty(value = "metaValue", required = true)
    @ApiModelProperty(name = "metaValue", value = "Provides the message you want to send to the user.", required = true)
    private String metaValue;

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = DataType.valueOf(dataType);
    }

    public String getMetaKey() {
        return metaKey;
    }

    public void setMetaKey(String metaKey) {
        this.metaKey = metaKey;
    }

    public String getMetaValue() {
        return metaValue;
    }

    public void setMetaValue(String metaValue) {
        this.metaValue = metaValue;
    }

    @Override
    public String toString() {
        return "metadata {" +
                ", type=" + dataType + '\'' +
                ", metaKey='" + metaKey + '\'' +
                ", metaValue='" + metaValue + '\'' +
                '}';
    }

}
