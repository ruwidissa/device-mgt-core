/*
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://entgra.io) All Rights Reserved.
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

package org.wso2.carbon.policy.mgt.core.mgt.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DynamicTaskContext;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.CorrectiveAction;
import org.wso2.carbon.device.mgt.common.policy.mgt.DeviceGroupWrapper;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.PolicyCriterion;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.policy.PolicyConfiguration;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMgtConstants;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.policy.mgt.common.Criterion;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.common.ProfileManagementException;
import org.wso2.carbon.policy.mgt.core.cache.impl.PolicyCacheManagerImpl;
import org.wso2.carbon.policy.mgt.core.dao.FeatureDAO;
import org.wso2.carbon.policy.mgt.core.dao.FeatureManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.PolicyDAO;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.ProfileDAO;
import org.wso2.carbon.policy.mgt.core.dao.ProfileManagerDAOException;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.policy.mgt.core.mgt.ProfileManager;
import org.wso2.carbon.policy.mgt.core.mgt.bean.UpdatedPolicyDeviceListBean;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagementConstants;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolicyManagerImpl implements PolicyManager {

    private final PolicyDAO policyDAO;
    private final ProfileDAO profileDAO;
    private final FeatureDAO featureDAO;
    private final ProfileManager profileManager;
    private final PolicyConfiguration policyConfiguration;
    private static final Log log = LogFactory.getLog(PolicyManagerImpl.class);

    public PolicyManagerImpl() {
        this.policyDAO = PolicyManagementDAOFactory.getPolicyDAO();
        this.profileDAO = PolicyManagementDAOFactory.getProfileDAO();
        this.featureDAO = PolicyManagementDAOFactory.getFeatureDAO();
        this.policyConfiguration = DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getPolicyConfiguration();
        this.profileManager = new ProfileManagerImpl();
    }

    @Override
    public Policy addPolicy(Policy policy) throws PolicyManagementException {
        try {
            PolicyManagementDAOFactory.beginTransaction();
            if (policy.getProfile() != null && policy.getProfile().getProfileId() == 0) {
                Profile profile = policy.getProfile();

                Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                profile.setCreatedDate(currentTimestamp);
                profile.setUpdatedDate(currentTimestamp);

                profileDAO.addProfile(profile);
                featureDAO.addProfileFeatures(profile.getProfileFeaturesList(), profile.getProfileId());
            }
            policy.setPolicyPayloadVersion("2.0");
            policy = policyDAO.addPolicy(policy);
            if (policy.getProfile() != null) {
                Profile profile = policy.getProfile();
                List<ProfileFeature> profileFeaturesList = profile.getProfileFeaturesList();
                for (ProfileFeature profileFeature : profileFeaturesList) {
                    if (profileFeature.getCorrectiveActions() != null &&
                            !profileFeature.getCorrectiveActions().isEmpty()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Adding corrective actions for policy " + policy.getPolicyName() +
                                    " having policy id " + policy.getId());
                        }
                        policyDAO.addCorrectiveActionsOfPolicy(profileFeature.getCorrectiveActions(),
                                policy.getId(), profileFeature.getId());
                    }
                }
            }

            if (policy.getUsers() != null) {
                policyDAO.addPolicyToUser(policy.getUsers(), policy);
            }
            if (policy.getRoles() != null) {
                policyDAO.addPolicyToRole(policy.getRoles(), policy);
            }
            if (policy.getDevices() != null) {
                policyDAO.addPolicyToDevice(policy.getDevices(), policy);
            }
            if (policy.getDeviceGroups() != null && !policy.getDeviceGroups().isEmpty()) {
                policyDAO.addDeviceGroupsToPolicy(policy);
            }
            if (policy.getPolicyCriterias() != null) {
                List<PolicyCriterion> criteria = policy.getPolicyCriterias();
                for (PolicyCriterion criterion : criteria) {

                    Criterion cr = policyDAO.getCriterion(criterion.getName());

                    if (cr.getId() == 0) {
                        Criterion criteriaObj = new Criterion();
                        criteriaObj.setName(criterion.getName());
                        policyDAO.addCriterion(criteriaObj);
                        criterion.setCriteriaId(criteriaObj.getId());
                    } else {
                        criterion.setCriteriaId(cr.getId());
                    }
                }

                policyDAO.addPolicyCriteria(policy);
                policyDAO.addPolicyCriteriaProperties(policy.getPolicyCriterias());
            }

            if (policy.isActive()) {
                policyDAO.activatePolicy(policy.getId());
            }
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the policy (" +
                    policy.getId() + " - " + policy.getPolicyName() + ")", e);
        } catch (ProfileManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the profile related to policy (" +
                    policy.getId() + " - " + policy.getPolicyName() + ")", e);
        } catch (FeatureManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the features of profile related to " +
                    "policy (" + policy.getId() + " - " + policy.getPolicyName() + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policy;
    }

    @Override
    public Policy updatePolicy(Policy policy) throws PolicyManagementException {
        try {
            // Previous policy needs to be obtained before beginning the transaction
            Policy previousPolicy = this.getPolicy(policy.getId());

            PolicyManagementDAOFactory.beginTransaction();
            // This will keep track of the policies updated.
            policyDAO.recordUpdatedPolicy(policy);

            List<ProfileFeature> existingFeaturesList = new ArrayList<>();
            List<ProfileFeature> newFeaturesList = new ArrayList<>();
            List<ProfileFeature> featuresToDelete = new ArrayList<>();
            List<String> temp = new ArrayList<>();
            List<String> updateDFes = new ArrayList<>();
            Map<Integer, List<CorrectiveAction>> updatedCorrectiveActionsMap = new HashMap<>();
            Map<Integer, List<CorrectiveAction>> existingCorrectiveActionsMap = new HashMap<>();

            List<ProfileFeature> updatedFeatureList = policy.getProfile().getProfileFeaturesList();
            List<ProfileFeature> existingProfileFeaturesList = previousPolicy.getProfile().getProfileFeaturesList();

            // Checks for the existing features
            for (ProfileFeature feature : updatedFeatureList) {
                for (ProfileFeature fe : existingProfileFeaturesList) {
                    if (feature.getFeatureCode().equalsIgnoreCase(fe.getFeatureCode())) {
                        existingFeaturesList.add(feature);
                        temp.add(feature.getFeatureCode());
                    }
                }
                updateDFes.add(feature.getFeatureCode());
            }

            // Check for the features to delete
            for (ProfileFeature feature : existingProfileFeaturesList) {
                if (!updateDFes.contains(feature.getFeatureCode())) {
                    featuresToDelete.add(feature);
                }
            }

            // Checks for the new features
            for (ProfileFeature feature : updatedFeatureList) {
                if (!temp.contains(feature.getFeatureCode())) {
                    newFeaturesList.add(feature);
                }
            }

            int profileId = previousPolicy.getProfile().getProfileId();
            policy.getProfile().setProfileId(profileId);
            policy.setProfileId(profileId);
            Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            policy.getProfile().setUpdatedDate(currentTimestamp);
            policy.setPriorityId(previousPolicy.getPriorityId());
            policy.setPolicyPayloadVersion(previousPolicy.getPolicyPayloadVersion());

            policyDAO.updatePolicy(policy);
            profileDAO.updateProfile(policy.getProfile());
            featureDAO.updateProfileFeatures(existingFeaturesList, profileId);
            if (!newFeaturesList.isEmpty()) {
                featureDAO.addProfileFeatures(newFeaturesList, profileId);
            }
            if (!featuresToDelete.isEmpty()) {
                for (ProfileFeature pf : featuresToDelete)
                    featureDAO.deleteProfileFeatures(pf.getId());
            }
            policyDAO.deleteCriteriaAndDeviceRelatedConfigs(policy.getId());
            if (policy.getUsers() != null) {
                policyDAO.updateUserOfPolicy(policy.getUsers(), previousPolicy);
            }
            if (policy.getRoles() != null) {
                policyDAO.updateRolesOfPolicy(policy.getRoles(), previousPolicy);
            }
            if (policy.getDevices() != null) {
                policyDAO.addPolicyToDevice(policy.getDevices(), previousPolicy);
            }
            if (policy.getDeviceGroups() != null && !policy.getDeviceGroups().isEmpty()) {
                policyDAO.addDeviceGroupsToPolicy(policy);
            }

            if (policy.getPolicyCriterias() != null && !policy.getPolicyCriterias().isEmpty()) {
                List<PolicyCriterion> criteria = policy.getPolicyCriterias();
                for (PolicyCriterion criterion : criteria) {
                    if (!policyDAO.checkCriterionExists(criterion.getName())) {
                        Criterion criteriaObj = new Criterion();
                        criteriaObj.setName(criterion.getName());
                        policyDAO.addCriterion(criteriaObj);
                        criterion.setCriteriaId(criteriaObj.getId());
                    }
                }

                policyDAO.addPolicyCriteria(policy);
                policyDAO.addPolicyCriteriaProperties(policy.getPolicyCriterias());
            }
            String policyPayloadVersion = previousPolicy.getPolicyPayloadVersion();
            float payloadVersion = 0f;
            if (policyPayloadVersion != null && !StringUtils.isEmpty(policyPayloadVersion)) {
                payloadVersion = Float.parseFloat(policyPayloadVersion);
            }

            List<ProfileFeature> updatedFeatures = policy.getProfile().getProfileFeaturesList();
            List<ProfileFeature> features = featureDAO.getFeaturesForProfile(profileId);
            for (ProfileFeature updatedFeature : updatedFeatures) {
                for (ProfileFeature feature : features) {
                    if (updatedFeature.getFeatureCode().equals(feature.getFeatureCode())) {
                        updatedFeature.setId(feature.getId());
                        break;
                    }
                }

                if (updatedFeature.getCorrectiveActions() != null) {
                    updatedCorrectiveActionsMap.put(updatedFeature.getId(),
                            updatedFeature.getCorrectiveActions());
                }
            }

            for (ProfileFeature fe : existingProfileFeaturesList) {
                if (fe.getCorrectiveActions() != null && !fe.getCorrectiveActions().isEmpty()) {
                    existingCorrectiveActionsMap.put(fe.getId(), fe.getCorrectiveActions());
                }
            }

            if (payloadVersion >= 2.0f) {
                updateMultipleCorrectiveActions(updatedCorrectiveActionsMap,
                        existingCorrectiveActionsMap, policy, previousPolicy);
            } else {
                updateSingleCorrectiveActionList(policy, previousPolicy);
            }
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while updating the policy ("
                    + policy.getId() + " - " + policy.getPolicyName() + ")", e);
        } catch (ProfileManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while updating the profile (" +
                    policy.getProfile().getProfileName() + ")", e);
        } catch (FeatureManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while updating the profile features (" +
                    policy.getProfile().getProfileName() + ")", e);

        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policy;
    }

    /**
     * Using for update old type of corrective policies which has single corrective policy
     * per single general policy
     * @param policy updating new corrective policy
     * @param previousPolicy previous corrective policy
     * @throws PolicyManagerDAOException for errors occur while updating corrective actions
     */
    private void updateSingleCorrectiveActionList(Policy policy, Policy previousPolicy)
            throws PolicyManagerDAOException {
        List<CorrectiveAction> updatedCorrectiveActions = policy.getCorrectiveActions();
        List<CorrectiveAction> existingCorrectiveActions = previousPolicy.getCorrectiveActions();
        List<CorrectiveAction> correctiveActionsToUpdate = new ArrayList<>();
        List<CorrectiveAction> correctiveActionsToDelete = new ArrayList<>();
        List<CorrectiveAction> correctiveActionsToAdd = new ArrayList<>();
        List<String> correctiveActionTypesToUpdate = new ArrayList<>();
        List<String> existingCorrectiveActionTypes = new ArrayList<>();

        if (updatedCorrectiveActions != null) {
            for (CorrectiveAction updatedCorrectiveAction : updatedCorrectiveActions) {
                for (CorrectiveAction existingCorrectiveAction : existingCorrectiveActions) {
                    if (updatedCorrectiveAction.getActionType()
                            .equals(existingCorrectiveAction.getActionType())) {
                        correctiveActionsToUpdate.add(updatedCorrectiveAction);
                        existingCorrectiveActionTypes.add(updatedCorrectiveAction.getActionType());
                    }
                }
                correctiveActionTypesToUpdate.add(updatedCorrectiveAction.getActionType());
            }

            for (CorrectiveAction updatedCorrectiveAction : updatedCorrectiveActions) {
                if (!existingCorrectiveActionTypes.contains(updatedCorrectiveAction
                        .getActionType())) {
                    correctiveActionsToAdd.add(updatedCorrectiveAction);
                }
            }
        }

        for (CorrectiveAction existingCorrectiveAction : existingCorrectiveActions) {
            if (!correctiveActionTypesToUpdate.contains(existingCorrectiveAction.getActionType())) {
                correctiveActionsToDelete.add(existingCorrectiveAction);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Updating corrective actions for policy " + policy.getPolicyName() +
                    " having policy id " + policy.getId());
        }

        if (!correctiveActionsToUpdate.isEmpty()) {
            policyDAO.updateCorrectiveActionsOfPolicy(correctiveActionsToUpdate,
                    previousPolicy.getId(), -1);
        }

        if (!correctiveActionsToAdd.isEmpty()) {
            policyDAO.addCorrectiveActionsOfPolicy(correctiveActionsToAdd,
                    previousPolicy.getId(), -1);
        }

        if (!correctiveActionsToDelete.isEmpty()) {
            policyDAO.deleteCorrectiveActionsOfPolicy(correctiveActionsToDelete,
                    previousPolicy.getId(), -1);
        }
    }

    /**
     * Using for update new type of corrective policies which has multiple corrective policies
     * per single general policy
     * @param updatedCorrectiveActionsMap updated corrective actions <FeatureId, CorrectiveActionList>
     * @param existingCorrectiveActionsMap existing corrective actions <FeatureId, CorrectiveActionList>
     * @param policy updating policy
     * @param previousPolicy for errors occur while updating corrective actions
     * @throws PolicyManagerDAOException
     */
    private void updateMultipleCorrectiveActions(
            Map<Integer, List<CorrectiveAction>> updatedCorrectiveActionsMap,
            Map<Integer, List<CorrectiveAction>> existingCorrectiveActionsMap,
            Policy policy, Policy previousPolicy) throws PolicyManagerDAOException {

        Map<Integer, List<CorrectiveAction>> correctiveActionsToUpdate = new HashMap<>();
        Map<Integer, List<String>> existingCorrectiveActionTypes = new HashMap<>();
        Map<Integer, List<String>> correctiveActionTypesToUpdate = new HashMap<>();
        Map<Integer, List<CorrectiveAction>> correctiveActionsToAdd = new HashMap<>();
        Map<Integer, List<CorrectiveAction>> correctiveActionsToDelete = new HashMap<>();

        for (Integer featureId : updatedCorrectiveActionsMap.keySet()) {
            List<CorrectiveAction> correctiveActionListToUpdate = new ArrayList<>();
            List<CorrectiveAction> updatedCorrectiveActions = updatedCorrectiveActionsMap
                    .get(featureId);
            for (CorrectiveAction updatedCorrectiveAction : updatedCorrectiveActions) {
                List<CorrectiveAction> existingCorrectiveActions = existingCorrectiveActionsMap
                        .get(featureId);
                if (existingCorrectiveActions != null) {
                    for (CorrectiveAction existingCorrectiveAction : existingCorrectiveActions) {
                        if (existingCorrectiveAction.getActionType().equals(updatedCorrectiveAction
                                .getActionType())) {
                            correctiveActionListToUpdate.add(updatedCorrectiveAction);
                            List<String> existingTypes = existingCorrectiveActionTypes
                                    .get(featureId);
                            if (existingTypes == null) {
                                existingTypes = new ArrayList<>();
                            }
                            existingTypes.add(updatedCorrectiveAction.getActionType());
                            existingCorrectiveActionTypes.put(featureId, existingTypes);
                        }
                    }
                }

                List<String> toUpdateTypes = correctiveActionTypesToUpdate.get(featureId);
                if (toUpdateTypes == null) {
                    toUpdateTypes = new ArrayList<>();
                }
                toUpdateTypes.add(updatedCorrectiveAction.getActionType());
                correctiveActionTypesToUpdate.put(featureId, toUpdateTypes);
            }
            if (!correctiveActionListToUpdate.isEmpty()) {
                correctiveActionsToUpdate.put(featureId, correctiveActionListToUpdate);
            }

            List<String> existingTypes = existingCorrectiveActionTypes.get(featureId);
            for (CorrectiveAction updatedCorrectiveAction : updatedCorrectiveActions) {
                if (existingTypes == null || !existingTypes.contains(updatedCorrectiveAction
                        .getActionType())) {
                    List<CorrectiveAction> correctiveActions = correctiveActionsToAdd
                            .get(featureId);
                    if (correctiveActions == null) {
                        correctiveActions = new ArrayList<>();
                    }
                    correctiveActions.add(updatedCorrectiveAction);
                    correctiveActionsToAdd.put(featureId, correctiveActions);
                }
            }
        }

        for (Integer featureId : existingCorrectiveActionsMap.keySet()) {
            List<CorrectiveAction> existingCorrectiveActions = existingCorrectiveActionsMap
                    .get(featureId);
            List<String> actionTypesToUpdate = correctiveActionTypesToUpdate.get(featureId);
            for (CorrectiveAction existingCorrectiveAction : existingCorrectiveActions) {
                if (actionTypesToUpdate == null ||
                        !actionTypesToUpdate.contains(existingCorrectiveAction.getActionType())) {
                    List<CorrectiveAction> correctiveActionListToDelete = correctiveActionsToDelete
                            .get(featureId);
                    if (correctiveActionListToDelete == null) {
                        correctiveActionListToDelete = new ArrayList<>();
                    }
                    correctiveActionListToDelete.add(existingCorrectiveAction);
                    correctiveActionsToDelete.put(featureId, correctiveActionListToDelete);
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Updating corrective actions for policy " + policy.getPolicyName() +
                    " having policy id " + policy.getId());
        }

        if (!correctiveActionsToUpdate.isEmpty()) {
            for (Integer featureId : correctiveActionsToUpdate.keySet()) {
                List<CorrectiveAction> correctiveActions = correctiveActionsToUpdate
                        .get(featureId);
                policyDAO.updateCorrectiveActionsOfPolicy(correctiveActions,
                        previousPolicy.getId(), featureId);
            }
        }

        if (!correctiveActionsToAdd.isEmpty()) {
            for (Integer featureId : correctiveActionsToAdd.keySet()) {
                List<CorrectiveAction> correctiveActions = correctiveActionsToAdd
                        .get(featureId);
                policyDAO.addCorrectiveActionsOfPolicy(correctiveActions,
                        previousPolicy.getId(), featureId);
            }
        }

        if (!correctiveActionsToDelete.isEmpty()) {
            for (Integer featureId : correctiveActionsToDelete.keySet()) {
                List<CorrectiveAction> correctiveActions = correctiveActionsToDelete
                        .get(featureId);
                policyDAO.deleteCorrectiveActionsOfPolicy(correctiveActions,
                        previousPolicy.getId(), featureId);
            }
        }
    }

    @Override
    public boolean updatePolicyPriorities(List<Policy> policies) throws PolicyManagementException {
        boolean bool;
        try {
            List<Policy> existingPolicies;
            if (policyConfiguration.getCacheEnable()) {
                existingPolicies = PolicyCacheManagerImpl.getInstance().getAllPolicies();
            } else {
                existingPolicies = this.getPolicies();
            }
            PolicyManagementDAOFactory.beginTransaction();
            bool = policyDAO.updatePolicyPriorities(policies);

            // This logic is added because ui sends only policy id and priority to update priorities.

            for (Policy policy : policies) {
                for (Policy exPolicy : existingPolicies) {
                    if (policy.getId() == exPolicy.getId()) {
                        policy.setProfile(exPolicy.getProfile());
                    }
                }
            }
            policyDAO.recordUpdatedPolicies(policies);
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while updating the policy priorities", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return bool;
    }

    @Override
    public boolean deletePolicy(Policy policy) throws PolicyManagementException {
        try {
            PolicyManagementDAOFactory.beginTransaction();
            policyDAO.deleteAllPolicyRelatedConfigs(policy.getId());
            policyDAO.deletePolicy(policy.getId());
            featureDAO.deleteFeaturesOfProfile(policy.getProfileId());
            profileDAO.deleteProfile(policy.getProfileId());
            PolicyManagementDAOFactory.commitTransaction();
            return true;
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while deleting the policy ("
                    + policy.getId() + " - " + policy.getPolicyName() + ")", e);
        } catch (ProfileManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while deleting the profile for policy ("
                    + policy.getId() + ")", e);
        } catch (FeatureManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while deleting the profile features for policy ("
                    + policy.getId() + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean deletePolicy(int policyId) throws PolicyManagementException {
        boolean bool;
        List<Policy> policies = this.getPolicies();
        Policy pol = null;
        for (Policy p : policies) {
            if (policyId == p.getId()) {
                pol = p;
            }
        }
        String deviceType = pol.getProfile().getDeviceType();
        List<Policy> deviceTypePolicyList = this.getPoliciesOfDeviceType(deviceType);
        if (deviceTypePolicyList.size() == 1) {
            List<Device> devices = this.getPolicyAppliedDevicesIds(policyId);
            List<DeviceIdentifier> deviceIdentifiers = this.convertDevices(devices);
            this.addPolicyRevokeOperation(deviceIdentifiers);
        }

        try {
            PolicyManagementDAOFactory.beginTransaction();

            Policy policy = policyDAO.getPolicy(policyId);
            policyDAO.deleteAllPolicyRelatedConfigs(policyId);
            bool = policyDAO.deletePolicy(policyId);

            if (log.isDebugEnabled()) {
                log.debug("Profile ID: " + policy.getProfileId());
            }

            featureDAO.deleteFeaturesOfProfile(policy.getProfileId());
            profileDAO.deleteProfile(policy.getProfileId());
            PolicyManagementDAOFactory.commitTransaction();
            return bool;
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while deleting the policy (" + policyId + ")", e);
        } catch (ProfileManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while deleting the profile for policy ("
                    + policyId + ")", e);
        } catch (FeatureManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while deleting the profile features for policy ("
                    + policyId + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void activatePolicy(int policyId) throws PolicyManagementException {
        try {
            Policy policy = this.getPolicy(policyId);
            PolicyManagementDAOFactory.beginTransaction();
            policyDAO.activatePolicy(policyId);
            policyDAO.recordUpdatedPolicy(policy);
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while activating the policy. (Id : " + policyId + ")" +
                    "", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void inactivatePolicy(int policyId) throws PolicyManagementException {
        Policy policy = this.getPolicy(policyId);
        try {
            PolicyManagementDAOFactory.beginTransaction();
            policyDAO.inactivatePolicy(policyId);
            policyDAO.recordUpdatedPolicy(policy);
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while inactivating the policy. (Id : " + policyId +
                    ")" +
                    "", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public Policy addPolicyToDevice(List<DeviceIdentifier> deviceIdentifierList,
                                    Policy policy) throws PolicyManagementException {
        List<Device> deviceList = new ArrayList<>();
        DeviceManagementProviderService deviceManagementService = PolicyManagementDataHolder
                .getInstance().getDeviceManagementService();
        for (DeviceIdentifier deviceIdentifier : deviceIdentifierList) {
            try {
                Device device = deviceManagementService.getDevice(deviceIdentifier, false);
                deviceList.add(device);
            } catch (DeviceManagementException e) {
                throw new PolicyManagementException("Error occurred while retrieving device information", e);
            }
        }
        try {
            PolicyManagementDAOFactory.beginTransaction();
            if (policy.getId() == 0) {
                policyDAO.addPolicy(policy);
            }

            policy = policyDAO.addPolicyToDevice(deviceList, policy);
            PolicyManagementDAOFactory.commitTransaction();

            if (policy.getDevices() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device list of policy is not null.");
                }
                policy.getDevices().addAll(deviceList);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Device list of policy is null. So added the first device to the list.");
                }
                policy.setDevices(deviceList);
            }
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the policy ("
                    + policy.getId() + " - " + policy.getPolicyName() + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policy;
    }

    @Override
    public Policy addPolicyToRole(List<String> roleNames, Policy policy) throws PolicyManagementException {
        try {
            PolicyManagementDAOFactory.beginTransaction();
            if (policy.getId() == 0) {
                policyDAO.addPolicy(policy);
            }
            policy = policyDAO.addPolicyToRole(roleNames, policy);
            PolicyManagementDAOFactory.commitTransaction();

            if (policy.getRoles() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("New roles list is added to the policy ");
                }
                policy.getRoles().addAll(roleNames);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Roles list was null, new roles are added.");
                }
                policy.setRoles(roleNames);
            }
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the policy ("
                    + policy.getId() + " - " + policy.getPolicyName() + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policy;
    }

    @Override
    public Policy addPolicyToUser(List<String> usernameList, Policy policy) throws PolicyManagementException {
        try {
            PolicyManagementDAOFactory.beginTransaction();
            if (policy.getId() == 0) {
                policyDAO.addPolicy(policy);
            }
            policy = policyDAO.addPolicyToUser(usernameList, policy);
            PolicyManagementDAOFactory.commitTransaction();

            if (policy.getRoles() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("New users list is added to the policy ");
                }
                policy.getRoles().addAll(usernameList);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Users list was null, new users list is added.");
                }
                policy.setRoles(usernameList);
            }
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the policy ("
                    + policy.getId() + " - " + policy.getPolicyName() + ") to user list.", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policy;
    }

    @Override
    public Policy getPolicyByProfileID(int profileId) throws PolicyManagementException {
        Policy policy;
        Profile profile;
        List<Device> deviceList;
        List<String> roleNames;
        try {
            PolicyManagementDAOFactory.openConnection();
            policy = policyDAO.getPolicyByProfileID(profileId);
            roleNames = policyDAO.getPolicyAppliedRoles(policy.getId());
            profile = profileDAO.getProfile(profileId);
            policy.setProfile(profile);
            policy.setRoles(roleNames);
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the policy related to profile ID (" +
                    profileId + ")", e);
        } catch (ProfileManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the profile related to profile ID (" +
                    profileId + ")", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }

        // This is due to connection close in following method too.
        deviceList = getPolicyAppliedDevicesIds(policy.getId());
        policy.setDevices(deviceList);
        return policy;
    }

    @Override
    public Policy getPolicy(int policyId) throws PolicyManagementException {
        Policy policy;
        List<Device> deviceList;
        List<String> roleNames;
        List<String> userNames;
        try {
            PolicyManagementDAOFactory.openConnection();
            policy = policyDAO.getPolicy(policyId);
            roleNames = policyDAO.getPolicyAppliedRoles(policyId);
            userNames = policyDAO.getPolicyAppliedUsers(policyId);
            policy.setRoles(roleNames);
            policy.setUsers(userNames);

            if (log.isDebugEnabled()) {
                log.debug("Retrieving corrective actions of policy " + policy.getPolicyName() +
                        " having policy id " + policy.getId());
            }

        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the policy related to policy ID (" +
                    policyId + ")", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }

        // This is done because connection close in below method too.
        deviceList = this.getPolicyAppliedDevicesIds(policyId);
        policy.setDevices(deviceList);

        try {
            Profile profile = profileManager.getProfile(policy.getProfileId());
            policy.setProfile(profile);
        } catch (ProfileManagementException e) {
            throw new PolicyManagementException("Error occurred while getting the profile related to policy ID (" +
                    policyId + ")", e);
        }

        try {
            PolicyManagementDAOFactory.openConnection();
            List<CorrectiveAction> correctiveActionsOfPolicy = policyDAO
                    .getCorrectiveActionsOfPolicy(policyId);
            String policyPayloadVersion = policy.getPolicyPayloadVersion();
            float payloadVersion = 0f;
            if (policyPayloadVersion != null && !StringUtils.isEmpty(policyPayloadVersion)) {
                payloadVersion = Float.parseFloat(policyPayloadVersion);
            }
            if (payloadVersion >= 2.0f) {
                setMultipleCorrectiveActions(correctiveActionsOfPolicy, policy.getProfile());
            } else {
                policy.setCorrectiveActions(getSingleCorrectiveAction
                        (correctiveActionsOfPolicy, policyId));
            }
        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while getting the corrective actions related to policy " +
                    "ID (" + policyId + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening DB connection";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policy;
    }

    @Override
    public List<Policy> getPolicies() throws PolicyManagementException {

        List<Policy> policyList;
        List<Profile> profileList;
        try {
            profileList = profileManager.getAllProfiles();
        } catch (ProfileManagementException e) {
            throw new PolicyManagementException("Error occurred while getting all the profiles.", e);
        }
        try {
            PolicyManagementDAOFactory.openConnection();
            policyList = policyDAO.getAllPolicies();
            this.buildPolicyList(policyList, profileList);
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting all the policies.", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } catch (GroupManagementException e) {
            throw new PolicyManagementException("Error occurred while getting device groups.", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }

        // Following is done because connection close has been implemented in every method.
        for (Policy policy : policyList) {
            policy.setDevices(this.getPolicyAppliedDevicesIds(policy.getId()));
        }
        return policyList;
    }

    @Override
    public List<Policy> getPoliciesOfDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        List<Integer> policyIdList;
        List<Policy> policies = new ArrayList<>();
        DeviceManagementProviderService deviceManagementService = PolicyManagementDataHolder
                .getInstance().getDeviceManagementService();
        Device device;
        try {
            device = deviceManagementService.getDevice(deviceIdentifier, false);
        } catch (DeviceManagementException e) {
            throw new PolicyManagementException("Error occurred while getting device related to device identifier (" +
                    deviceIdentifier.getId() + " - " + deviceIdentifier.getType() + ")", e);
        }

        try {
            PolicyManagementDAOFactory.openConnection();
            policyIdList = policyDAO.getPolicyIdsOfDevice(device);
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the policies for device identifier (" +
                    deviceIdentifier.getId() + " - " + deviceIdentifier.getType() + ")", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while open a data source connection", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }

        List<Policy> tempPolicyList;
        if (policyConfiguration.getCacheEnable()) {
            tempPolicyList = PolicyCacheManagerImpl.getInstance().getAllPolicies();
        } else {
            tempPolicyList = this.getPolicies();
        }

        for (Policy policy : tempPolicyList) {
            for (Integer i : policyIdList) {
                if (policy.getId() == i) {
                    policies.add(policy);
                }
            }
        }

        Collections.sort(policies);
        return policies;
    }

    @Override
    public List<Policy> getPoliciesOfDeviceType(String deviceTypeName) throws PolicyManagementException {
        List<Policy> policies = new ArrayList<>();
        List<Policy> allPolicies;
        if (policyConfiguration.getCacheEnable()) {
            allPolicies = PolicyCacheManagerImpl.getInstance().getAllPolicies();
        } else {
            allPolicies = this.getPolicies();
        }

        for (Policy policy : allPolicies) {
            if (policy.getProfile().getDeviceType().equalsIgnoreCase(deviceTypeName)) {
                policies.add(policy);
            }
        }
        Collections.sort(policies);
        return policies;
    }

    @Override
    public List<Policy> getPoliciesOfRole(String roleName) throws PolicyManagementException {
        List<Policy> policies = new ArrayList<>();
        List<Integer> policyIdList;
        try {
            PolicyManagementDAOFactory.openConnection();
            policyIdList = policyDAO.getPolicyOfRole(roleName);
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the policies.", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while open a data source connection", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }

        List<Policy> tempPolicyList;
        if (policyConfiguration.getCacheEnable()) {
            tempPolicyList = PolicyCacheManagerImpl.getInstance().getAllPolicies();
        } else {
            tempPolicyList = this.getPolicies();
        }

        for (Policy policy : tempPolicyList) {
            for (Integer i : policyIdList) {
                if (policy.getId() == i) {
                    policies.add(policy);
                }
            }
        }
        Collections.sort(policies);
        return policies;
    }

    @Override
    public List<Policy> getPoliciesOfUser(String username) throws PolicyManagementException {
        List<Policy> policies = new ArrayList<>();
        List<Integer> policyIdList;
        try {
            PolicyManagementDAOFactory.openConnection();
            policyIdList = policyDAO.getPolicyOfUser(username);
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the policies.", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while open a data source connection", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        List<Policy> tempPolicyList;
        if (policyConfiguration.getCacheEnable()) {
            tempPolicyList = PolicyCacheManagerImpl.getInstance().getAllPolicies();
        } else {
            tempPolicyList = this.getPolicies();
        }

        for (Policy policy : tempPolicyList) {
            for (Integer i : policyIdList) {
                if (policy.getId() == i) {
                    policies.add(policy);
                }
            }
        }
        Collections.sort(policies);
        return policies;
    }

    @Override
    public List<Device> getPolicyAppliedDevicesIds(int policyId) throws PolicyManagementException {
        List<Device> deviceList = new ArrayList<>();
        List<Integer> deviceIds;
        DeviceManagementProviderService deviceManagementService = PolicyManagementDataHolder
                .getInstance().getDeviceManagementService();
        List<Device> allDevices;
        try {
            allDevices = deviceManagementService.getAllDevices();
        } catch (DeviceManagementException e) {
            throw new PolicyManagementException("Error occurred while getting the devices related to policy id (" +
                    policyId + ")", e);
        }
        try {
            PolicyManagementDAOFactory.openConnection();
            deviceIds = policyDAO.getPolicyAppliedDevicesIds(policyId);
            HashMap<Integer, Device> allDeviceMap = new HashMap<>();
            if (!allDevices.isEmpty()) {
                allDeviceMap = PolicyManagerUtil.covertDeviceListToMap(allDevices);
            }
            for (int deviceId : deviceIds) {
                if (allDeviceMap.containsKey(deviceId)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Policy Applied device ids .............: " + deviceId + " - Policy Id " + policyId);
                    }
                    deviceList.add(allDeviceMap.get(deviceId));
                }
                //TODO FIX ME -- This is wrong, Device id is not  device identifier, so converting is wrong.
                //deviceList.add(deviceDAO.getDevice(new DeviceIdentifier(Integer.toString(deviceId), ""), tenantId));
            }
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting the device ids related to policy id (" +
                    policyId + ")", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return deviceList;
    }

    @Override
    public void addAppliedPolicyFeaturesToDevice(DeviceIdentifier deviceIdentifier,
                                                 Policy policy) throws PolicyManagementException {
        DeviceManagementProviderService deviceManagementService = PolicyManagementDataHolder
                .getInstance().getDeviceManagementService();
        Device device;
        try {
            device = deviceManagementService.getDevice(deviceIdentifier, false);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting the device details (" + deviceIdentifier.getId() + ")";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        int deviceId = device.getId();
        try {
            PolicyManagementDAOFactory.beginTransaction();
            boolean exist = policyDAO.checkPolicyAvailable(deviceId, device.getEnrolmentInfo().getId());
            if (exist) {
                policyDAO.updateEffectivePolicyToDevice(deviceId, device.getEnrolmentInfo().getId(), policy);
            } else {
                policyDAO.addEffectivePolicyToDevice(deviceId, device.getEnrolmentInfo().getId(), policy);
            }
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the evaluated policy to device (" +
                    deviceId + " - " + policy.getId() + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public UpdatedPolicyDeviceListBean applyChangesMadeToPolicies() throws PolicyManagementException {
        List<String> changedDeviceTypes = new ArrayList<>();
        List<Policy> updatedPolicies = new ArrayList<>();
        List<Integer> updatedPolicyIds = new ArrayList<>();
        boolean transactionDone = false;
        try {
            List<Policy> allPolicies;
            if (policyConfiguration.getCacheEnable()) {
                allPolicies = PolicyCacheManagerImpl.getInstance().getAllPolicies();
            } else {
                allPolicies = this.getPolicies();
            }
            for (Policy policy : allPolicies) {
                if (policy.isUpdated()) {
                    updatedPolicies.add(policy);
                    updatedPolicyIds.add(policy.getId());
                    if (!changedDeviceTypes.contains(policy.getProfile().getDeviceType())) {
                        changedDeviceTypes.add(policy.getProfile().getDeviceType());
                    }
                }
            }
            PolicyManagementDAOFactory.beginTransaction();
            transactionDone = true;
            policyDAO.markPoliciesAsUpdated(updatedPolicyIds);
            policyDAO.removeRecordsAboutUpdatedPolicies();
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while applying the changes to policy operations.", e);
        } finally {
            if (transactionDone) {
                PolicyManagementDAOFactory.closeConnection();
            }
        }
        return new UpdatedPolicyDeviceListBean(updatedPolicies, updatedPolicyIds, changedDeviceTypes);
    }


    @Override
    public void addAppliedPolicyToDevice(DeviceIdentifier deviceIdentifier, Policy policy)
            throws PolicyManagementException {
        DeviceManagementProviderService deviceManagementService = PolicyManagementDataHolder
                .getInstance().getDeviceManagementService();
        Device device;
        try {
            device = deviceManagementService.getDevice(deviceIdentifier, false);
        } catch (DeviceManagementException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while getting the device details (" +
                    deviceIdentifier.getId() + ")", e);
        }
        int deviceId = device.getId();
        try {
            PolicyManagementDAOFactory.beginTransaction();

            Policy policySaved = policyDAO.getAppliedPolicy(deviceId, device.getEnrolmentInfo().getId());
            if (policySaved != null && policySaved.getId() != 0) {
                policyDAO.updateEffectivePolicyToDevice(deviceId, device.getEnrolmentInfo().getId(), policy);
            } else {
                policyDAO.addEffectivePolicyToDevice(deviceId, device.getEnrolmentInfo().getId(), policy);
            }
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while adding the evaluated policy to device (" +
                    deviceId + " - " + policy.getId() + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void removeAppliedPolicyToDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        DeviceManagementProviderService deviceManagementService = PolicyManagementDataHolder
                .getInstance().getDeviceManagementService();
        Device device;
        try {
            device = deviceManagementService.getDevice(deviceIdentifier, false);
        } catch (DeviceManagementException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while getting the device details (" +
                    deviceIdentifier.getId() + ")", e);
        }
        int deviceId = device.getId();
        try {
            PolicyManagementDAOFactory.beginTransaction();

            Policy policySaved = policyDAO.getAppliedPolicy(deviceId, device.getEnrolmentInfo().getId());
            if (policySaved != null) {
                policyDAO.deleteEffectivePolicyToDevice(deviceId, device.getEnrolmentInfo().getId());
            }
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new PolicyManagementException("Error occurred while removing the applied policy to device (" +
                    deviceId + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean checkPolicyAvailable(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        boolean exist;
        DeviceManagementProviderService deviceManagementService = PolicyManagementDataHolder
                .getInstance().getDeviceManagementService();
        Device device;
        try {
            device = deviceManagementService.getDevice(deviceIdentifier, false);
        } catch (DeviceManagementException e) {
            throw new PolicyManagementException("Error occurred while getting the device details (" +
                    deviceIdentifier.getId() + ")", e);
        }
        try {
            PolicyManagementDAOFactory.openConnection();
            exist = policyDAO.checkPolicyAvailable(device.getId(), device.getEnrolmentInfo().getId());
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while checking whether device has a policy " +
                    "to apply.", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return exist;
    }

    @Override
    public boolean setPolicyApplied(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        DeviceManagementProviderService deviceManagementService = PolicyManagementDataHolder
                .getInstance().getDeviceManagementService();
        Device device;
        try {
            device = deviceManagementService.getDevice(deviceIdentifier, false);
        } catch (DeviceManagementException e) {
            throw new PolicyManagementException("Error occurred while getting the device details (" +
                    deviceIdentifier.getId() + ")", e);
        }
        try {
            PolicyManagementDAOFactory.openConnection();
            policyDAO.setPolicyApplied(device.getId(), device.getEnrolmentInfo().getId());
            return true;
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while setting the policy has applied to device (" +
                    deviceIdentifier.getId() + ")", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getPolicyCount() throws PolicyManagementException {
        try {
            PolicyManagementDAOFactory.openConnection();
            return policyDAO.getPolicyCount();
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting policy count", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    @Deprecated
    public Policy getAppliedPolicyToDevice(DeviceIdentifier deviceId) throws PolicyManagementException {
        DeviceManagementProviderService deviceManagementService = PolicyManagementDataHolder
                .getInstance().getDeviceManagementService();
        Device device;
        try {
            device = deviceManagementService.getDevice(deviceId, false);
            if (device == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No device is found upon the device identifier '" + deviceId.getId() +
                            "' and type '" + deviceId.getType() + "'. Therefore returning null");
                }
                return null;
            }
        } catch (DeviceManagementException e) {
            throw new PolicyManagementException("Error occurred while getting device id.", e);
        }
        return getAppliedPolicyToDevice(device);
    }

    @Override
    public Policy getAppliedPolicyToDevice(Device device) throws PolicyManagementException {
        Policy policy;
        try {
            PolicyManagementDAOFactory.openConnection();
            policy = policyDAO.getAppliedPolicy(device.getId(), device.getEnrolmentInfo().getId());
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting policy id or policy.", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policy;
    }

    @Override
    public HashMap<Integer, Integer> getAppliedPolicyIdsDeviceIds() throws PolicyManagementException {
        try {
            PolicyManagementDAOFactory.openConnection();
            return policyDAO.getAppliedPolicyIdsDeviceIds();
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while reading the policy applied database.", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while reading the policy applied database.", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    private List<DeviceGroupWrapper> getDeviceGroupNames(List<DeviceGroupWrapper> groupWrappers)
            throws GroupManagementException {
        GroupManagementProviderService groupManagementService = PolicyManagementDataHolder
                .getInstance().getGroupManagementService();
        for (DeviceGroupWrapper wrapper : groupWrappers) {
            DeviceGroup deviceGroup = groupManagementService.getGroup(wrapper.getId(), false);
            wrapper.setName(deviceGroup.getName());
            wrapper.setOwner(deviceGroup.getOwner());
        }
        return groupWrappers;
    }

    private List<DeviceIdentifier> convertDevices(List<Device> devices) {
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        for (Device device : devices) {
            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(device.getDeviceIdentifier());
            identifier.setType(device.getType());
            deviceIdentifiers.add(identifier);
        }
        return deviceIdentifiers;
    }

    private void addPolicyRevokeOperation(List<DeviceIdentifier> deviceIdentifiers) throws PolicyManagementException {
        try {
            String type;
            if (!deviceIdentifiers.isEmpty()) {
                type = deviceIdentifiers.get(0).getType();
                PolicyManagementDataHolder.getInstance().getDeviceManagementService().addOperation(type,
                        this.getPolicyRevokeOperation(), deviceIdentifiers);
            }
        } catch (InvalidDeviceException e) {
            String msg = "Invalid DeviceIdentifiers found.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (OperationManagementException e) {
            String msg = "Error occurred while adding the operation to device.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    private Operation getPolicyRevokeOperation() {
        CommandOperation policyRevokeOperation = new CommandOperation();
        policyRevokeOperation.setEnabled(true);
        policyRevokeOperation.setCode(OperationMgtConstants.OperationCodes.POLICY_REVOKE);
        policyRevokeOperation.setType(Operation.Type.COMMAND);
        return policyRevokeOperation;
    }

    @Override
    public List<Policy> getPolicies(String type) throws PolicyManagementException {
        List<Policy> policyList;
        List<Profile> profileList;
        try {
            profileList = profileManager.getAllProfiles();
        } catch (ProfileManagementException e) {
            throw new PolicyManagementException("Error occurred while getting all the profiles.", e);
        }
        try {
            PolicyManagementDAOFactory.openConnection();
            policyList = policyDAO.getAllPolicies(type);
            this.buildPolicyList(policyList, profileList);
        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while getting all the policies. ";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source ";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (GroupManagementException e) {
            String msg = "Error occurred while getting device groups. ";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        for (Policy policy : policyList) {
            policy.setDevices(this.getPolicyAppliedDevicesIds(policy.getId()));
        }
        return policyList;
    }

    /**
     * Build the list of policies which are included new and old types of corrective actions
     * @param policyList queried policy list
     * @param profileList queried profile list
     * @throws PolicyManagerDAOException when failed to read policies from DB
     * @throws GroupManagementException when failed to read policy groups from DB
     */
    private void buildPolicyList(List<Policy> policyList, List<Profile> profileList)
            throws PolicyManagerDAOException, GroupManagementException {
        List<CorrectiveAction> allCorrectiveActions = policyDAO.getAllCorrectiveActions();
        for (Policy policy : policyList) {
            String policyPayloadVersion = policy.getPolicyPayloadVersion();
            float payloadVersion = 0f;
            if (policyPayloadVersion != null &&
                    !StringUtils.isEmpty(policyPayloadVersion)) {
                payloadVersion = Float.parseFloat(policyPayloadVersion);
            }
            for (Profile profile : profileList) {
                if (policy.getProfileId() == profile.getProfileId()) {
                    policy.setProfile(profile);
                    if (payloadVersion >= 2.0f && PolicyManagementConstants.GENERAL_POLICY_TYPE
                            .equals(policy.getPolicyType())) {
                        setMultipleCorrectiveActions(allCorrectiveActions, profile);
                    }
                }
            }
            policy.setRoles(policyDAO.getPolicyAppliedRoles(policy.getId()));
            policy.setUsers(policyDAO.getPolicyAppliedUsers(policy.getId()));
            policy.setPolicyCriterias(policyDAO.getPolicyCriteria(policy.getId()));
            if (payloadVersion < 2.0f && PolicyManagementConstants.GENERAL_POLICY_TYPE
                    .equals(policy.getPolicyType())) {
                policy.setCorrectiveActions
                        (getSingleCorrectiveAction(allCorrectiveActions, policy.getId()));
            }

            List<DeviceGroupWrapper> deviceGroupWrappers = policyDAO.getDeviceGroupsOfPolicy(policy.getId());
            if (!deviceGroupWrappers.isEmpty()) {
                deviceGroupWrappers = this.getDeviceGroupNames(deviceGroupWrappers);
            }
            policy.setDeviceGroups(deviceGroupWrappers);
            if (log.isDebugEnabled()) {
                log.debug("Retrieving corrective actions for policy " + policy.getPolicyName() +
                        " having policy id " + policy.getId());
            }
        }
        Collections.sort(policyList);
    }

    /**
     * Get the corrective action list of a specific policy
     * @param allCorrectiveActions stored corrective actions of all policies
     * @param policyId Id of the policy to get corrective action
     * @return
     */
    private List<CorrectiveAction> getSingleCorrectiveAction
            (List<CorrectiveAction> allCorrectiveActions, int policyId) {
        List<CorrectiveAction> correctiveActionsOfPolicy = new ArrayList<>();
        for (CorrectiveAction correctiveAction : allCorrectiveActions) {
            if (correctiveAction.getAssociatedGeneralPolicyId() != null &&
                    correctiveAction.getAssociatedGeneralPolicyId() == policyId) {
                clearMetaDataValues(correctiveAction);
                correctiveActionsOfPolicy.add(correctiveAction);
            }
        }
        return correctiveActionsOfPolicy;
    }

    /**
     * Set the corrective actions of a specific policy against with the policy profile.
     * This method is using with the new implementation of corrective policies which is able to apply multiple corrective
     * policies based on a feature code of a policy
     * @param allCorrectiveActions corrective action list retrieved from the DB
     * @param profile profile of the selected policy
     */
    private void setMultipleCorrectiveActions(List<CorrectiveAction> allCorrectiveActions,
                                              Profile profile) {
        for (ProfileFeature profileFeature : profile.getProfileFeaturesList()) {
            List<CorrectiveAction> correctiveActionList = new ArrayList<>();
            for (CorrectiveAction correctiveAction : allCorrectiveActions) {
                if (correctiveAction.getFeatureId() != null &&
                        profileFeature.getId() == correctiveAction.getFeatureId()) {
                    clearMetaDataValues(correctiveAction);
                    correctiveActionList.add(correctiveAction);
                }
            }
            profileFeature.setCorrectiveActions(correctiveActionList);
        }
    }

    /**
     * Clear corrective action metadata values to avoid sending in payload
     * @param correctiveAction list of corrective actions
     */
    private void clearMetaDataValues(CorrectiveAction correctiveAction) {
        correctiveAction.setAssociatedGeneralPolicyId(null); //avoiding send in payload
        correctiveAction.setFeatureId(null); //avoiding send in payload
    }

    @Override
    public List<Policy> getPolicyList() throws PolicyManagementException {

        List<Policy> policyList;
        try {
            PolicyManagementDAOFactory.openConnection();
            policyList = policyDAO.getAllPolicies();
            for (Policy policy : policyList) {
                policy.setRoles(policyDAO.getPolicyAppliedRoles(policy.getId()));
                policy.setUsers(policyDAO.getPolicyAppliedUsers(policy.getId()));
                List<DeviceGroupWrapper> deviceGroupWrappers = policyDAO.getDeviceGroupsOfPolicy(policy.getId());
                if (!deviceGroupWrappers.isEmpty()) {
                    deviceGroupWrappers = this.getDeviceGroupNames(deviceGroupWrappers);
                }
                policy.setDeviceGroups(deviceGroupWrappers);
            }
            Collections.sort(policyList);
        } catch (PolicyManagerDAOException e) {
            throw new PolicyManagementException("Error occurred while getting all the policies.", e);
        } catch (SQLException e) {
            throw new PolicyManagementException("Error occurred while opening a connection to the data source", e);
        } catch (GroupManagementException e) {
            throw new PolicyManagementException("Error occurred while getting device groups.", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return policyList;
    }
}
