/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.policy.mgt.common;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.DynamicTaskContext;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.PolicyPaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Policy;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Profile;

import java.util.List;

/**
 * This interface defines the policy management which should be implemented by the plugins
 */

public interface PolicyAdministratorPoint {

    /**
     * This method adds a policy to the platform
     *
     */

    Policy addPolicy(Policy policy) throws PolicyManagementException;


    Policy updatePolicy(Policy policy) throws PolicyManagementException;

    boolean updatePolicyPriorities(List<Policy> policies) throws PolicyManagementException;

    void activatePolicy(int policyId) throws PolicyManagementException;

    void inactivatePolicy(int policyId) throws PolicyManagementException;

    boolean deletePolicy(Policy policy) throws PolicyManagementException;
    boolean deletePolicy(int policyId) throws PolicyManagementException;

    void publishChanges() throws PolicyManagementException;

    /**
     * This method adds a policy per device which should be implemented by the related plugins.
     */
    Policy addPolicyToDevice(List<DeviceIdentifier> deviceIdentifierList, Policy policy) throws PolicyManagementException;

    /**
     * This method adds the policy to specific role.
     *
     * @param roleNames
     * @param policy
     * @return primary key (generated key)
     */
    Policy addPolicyToRole(List<String> roleNames, Policy policy) throws  PolicyManagementException;

    /**
     * This method returns the policy of whole platform
     *
     * @return
     */

    List<Policy> getPolicies() throws PolicyManagementException;

    Policy getPolicy(int policyId) throws PolicyManagementException;

    /**
     * This method gives the device specific policy.
     *
     * @param deviceIdentifier
     * @return Policy
     */

    List<Policy> getPoliciesOfDevice(DeviceIdentifier deviceIdentifier) throws  PolicyManagementException;

    /**
     * This method returns the device type specific policy.
     *
     * @param deviceType
     * @return Policy
     */

    List<Policy> getPoliciesOfDeviceType(String deviceType) throws  PolicyManagementException;

    /**
     * This method returns the role specific policy.
     *
     * @param roleName
     * @return
     */

    List<Policy> getPoliciesOfRole(String roleName) throws  PolicyManagementException;


    List<Policy> getPoliciesOfUser(String username) throws  PolicyManagementException;


    /**
     * This method checks weather a policy is available for a device.
     *
     * @param deviceIdentifier
     * @return
     * @throws PolicyManagementException
     */
    boolean isPolicyAvailableForDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException;


    /**
     * This method checks weather a policy is used by a particular device.
     *
     * @param deviceIdentifier
     * @return
     * @throws PolicyManagementException
     */
    boolean isPolicyApplied(DeviceIdentifier deviceIdentifier) throws PolicyManagementException;


    /**
     * @param deviceIdentifier
     * @param policy
     * @throws PolicyManagementException
     */
    void setPolicyUsed(DeviceIdentifier deviceIdentifier, Policy policy) throws PolicyManagementException;

    /**
     * This method will remove the policy applied to the device.
     * @param deviceIdentifier
      * @throws PolicyManagementException
     */
    void removePolicyUsed(DeviceIdentifier deviceIdentifier) throws PolicyManagementException;

    /**
     * This method will add the profile to database,
     * @param profile
     * @throws PolicyManagementException
     */
    Profile addProfile(Profile profile) throws PolicyManagementException;

    boolean deleteProfile(Profile profile) throws PolicyManagementException;

    Profile updateProfile(Profile profile) throws PolicyManagementException;

    Profile getProfile(int profileId) throws PolicyManagementException;

    List<Profile> getProfiles() throws PolicyManagementException;

    int getPolicyCount() throws PolicyManagementException;

    /**
     * @param policyType type of the policy
     * @return policy list of the specific type
     * @throws PolicyManagementException
     */
    List<Policy> getPolicies(String policyType) throws PolicyManagementException;

    /**
     * Returns a list of policies filtered by offset and limit
     * @param request {@link PolicyPaginationRequest} contains offset and limit and filters
     * @return {@link List<Policy>} - list of policies for current tenant
     * @throws PolicyManagementException when there is an error while retrieving the policies from database or
     * while retrieving device groups
     */
    List<Policy> getPolicyList(PolicyPaginationRequest request) throws PolicyManagementException;
}
