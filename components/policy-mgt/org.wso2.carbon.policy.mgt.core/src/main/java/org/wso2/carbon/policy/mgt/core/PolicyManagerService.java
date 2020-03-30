/*
*  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/


package org.wso2.carbon.policy.mgt.core;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.core.task.TaskScheduleService;

import java.util.List;

public interface PolicyManagerService {

/*
    Feature addFeature(Feature feature) throws FeatureManagementException;

    Feature updateFeature(Feature feature) throws FeatureManagementException;
*/

    Profile addProfile(Profile profile) throws PolicyManagementException;

    Profile updateProfile(Profile profile) throws PolicyManagementException;

    Policy addPolicy(Policy policy) throws PolicyManagementException;

    Policy updatePolicy(Policy policy) throws PolicyManagementException;

    boolean deletePolicy(Policy policy) throws PolicyManagementException;

    boolean deletePolicy(int policyId) throws PolicyManagementException;

    Policy getEffectivePolicy(DeviceIdentifier deviceIdentifier) throws PolicyManagementException;

    List<ProfileFeature> getEffectiveFeatures(DeviceIdentifier deviceIdentifier) throws FeatureManagementException;

    List<Policy> getPolicies(String deviceType) throws PolicyManagementException;

    List<Feature> getFeatures() throws FeatureManagementException;

    PolicyAdministratorPoint getPAP() throws PolicyManagementException;

    PolicyInformationPoint getPIP() throws PolicyManagementException;

    PolicyEvaluationPoint getPEP() throws PolicyManagementException;

    TaskScheduleService getTaskScheduleService() throws PolicyMonitoringTaskException;

    int getPolicyCount() throws PolicyManagementException;

    @Deprecated
    Policy getAppliedPolicyToDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException;

    Policy getAppliedPolicyToDevice(Device device) throws PolicyManagementException;

    @Deprecated
    List<ComplianceFeature> checkPolicyCompliance(DeviceIdentifier deviceIdentifier, Object
            deviceResponse) throws PolicyComplianceException;

    List<ComplianceFeature> checkPolicyCompliance(Device device, Object
            deviceResponse) throws PolicyComplianceException;

    @Deprecated
    boolean checkCompliance(DeviceIdentifier deviceIdentifier, Object response) throws PolicyComplianceException;

    boolean checkCompliance(Device device, Object response) throws PolicyComplianceException;

    @Deprecated
    NonComplianceData getDeviceCompliance(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException;

    NonComplianceData getDeviceCompliance(Device device) throws PolicyComplianceException;

    boolean isCompliant(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException;

    PaginationResult getPolicyCompliance(
            PaginationRequest paginationRequest, String policyId, boolean complianceStatus, boolean isPending, String fromDate, String toDate)
            throws PolicyComplianceException;

    List<ComplianceFeature> getNoneComplianceFeatures(int complianceStatusId) throws PolicyComplianceException;
}
