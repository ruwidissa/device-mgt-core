/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.policy.mgt.core.mgt;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;

import java.util.List;

public interface MonitoringManager {

    @Deprecated
    List<ComplianceFeature> checkPolicyCompliance(DeviceIdentifier deviceIdentifier, Object deviceResponse)
            throws PolicyComplianceException;

    List<ComplianceFeature> checkPolicyCompliance(Device device, Object deviceResponse)
            throws PolicyComplianceException;

    boolean isCompliant(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException;

    @Deprecated
    NonComplianceData getDevicePolicyCompliance(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException;

    NonComplianceData getDevicePolicyCompliance(Device device) throws PolicyComplianceException;

    void addMonitoringOperation(List<Device> devices) throws PolicyComplianceException;

    List<String> getDeviceTypes() throws PolicyComplianceException;

    PaginationResult getPolicyCompliance(
            PaginationRequest paginationRequest, String policyId, boolean complianceStatus, boolean isPending,
            String fromDate, String toDate) throws PolicyComplianceException;

    List<ComplianceFeature> getNoneComplianceFeatures(int complianceStatusId)
            throws PolicyComplianceException;

}
