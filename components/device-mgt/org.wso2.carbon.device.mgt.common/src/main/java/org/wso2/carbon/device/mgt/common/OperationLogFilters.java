/*
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.common;

import java.util.List;

/**
 * This class carries information related to operation log filtering values which will be used in the UI to filter operations.
 */
public class OperationLogFilters {
    private List<String> operationCode;
    private Long createdDayFrom;
    private Long createdDayTo ;
    private Long updatedDayFrom;
    private Long updatedDayTo ;
    private List<String> status;
    public OperationLogFilters() {
    }
    public OperationLogFilters(List<String> operationCode , Long createdDayFrom, Long createdDayTo,
                               Long updatedDayFrom, Long updatedDayTo, List<String> status) {
        this.operationCode = operationCode;
        this.createdDayFrom = createdDayFrom;
        this.createdDayTo = createdDayTo;
        this.updatedDayFrom = updatedDayFrom;
        this.updatedDayTo = updatedDayTo;
        this.status = status;
    }
    public List<String> getOperationCode() {
        return operationCode;
    }
    public void setOperationCode(List<String> operationCode) {
        this.operationCode = operationCode;
    }
    public List<String> getStatus() {
        return status;
    }
    public void setStatus(List<String> status) {
        this.status = status;
    }
    public Long getUpdatedDayFrom() {
        return updatedDayFrom;
    }
    public void setUpdatedDayFrom(Long updatedDayFrom) {
        this.updatedDayFrom = updatedDayFrom;
    }
    public Long getUpdatedDayTo() {
        return updatedDayTo;
    }
    public void setUpdatedDayTo(Long updatedDayTo) {
        this.updatedDayTo = updatedDayTo;
    }
    public Long getCreatedDayFrom() { return createdDayFrom; }
    public void setCreatedDayFrom(Long createdDayFrom) { this.createdDayFrom = createdDayFrom; }
    public Long getCreatedDayTo() { return createdDayTo; }
    public void setCreatedDayTo(Long createdDayTo) { this.createdDayTo = createdDayTo; }
}
