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

package io.entgra.device.mgt.core.policy.mgt.core.util;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.ConfigurationEntry;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.PlatformConfiguration;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.PlatformConfigurationManagementService;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroup;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.CorrectiveAction;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.policy.PolicyConfiguration;
import io.entgra.device.mgt.core.device.mgt.core.config.tenant.PlatformConfigurationManagementServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.PolicyOperation;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.ProfileOperation;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Policy;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyAdministratorPoint;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyManagementException;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.ProfileFeature;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyTransformException;
import io.entgra.device.mgt.core.policy.mgt.core.config.datasource.DataSourceConfig;
import io.entgra.device.mgt.core.policy.mgt.core.config.datasource.JNDILookupDefinition;
import io.entgra.device.mgt.core.policy.mgt.core.dao.util.PolicyManagementDAOUtil;
import io.entgra.device.mgt.core.policy.mgt.core.impl.PolicyAdministratorPointImpl;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.sql.DataSource;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.util.*;

public class PolicyManagerUtil {
    private static final Gson gson = new Gson();
    public static final String GENERAL_CONFIG_RESOURCE_PATH = "general";
    public static final String MONITORING_FREQUENCY = "notifierFrequency";
    private static final Log log = LogFactory.getLog(PolicyManagerUtil.class);

    public static Document convertToDocument(File file) throws PolicyManagementException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new PolicyManagementException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document : " + e.getMessage(), e);
        }
    }

    /**
     * Resolve data source from the data source definition
     *
     * @param config data source configuration
     * @return data source resolved from the data source definition
     */
    public static DataSource resolveDataSource(DataSourceConfig config) {
        DataSource dataSource = null;
        if (config == null) {
            throw new RuntimeException("Device Management Repository data source configuration " +
                    "is null and thus, is not initialized");
        }
        JNDILookupDefinition jndiConfig = config.getJndiLookupDefinition();
        if (jndiConfig != null) {
            if (log.isDebugEnabled()) {
                log.debug("Initializing Device Management Repository data source using the JNDI " +
                        "Lookup Definition");
            }
            List<JNDILookupDefinition.JNDIProperty> jndiPropertyList =
                    jndiConfig.getJndiProperties();
            if (jndiPropertyList != null) {
                Hashtable<Object, Object> jndiProperties = new Hashtable<Object, Object>();
                for (JNDILookupDefinition.JNDIProperty prop : jndiPropertyList) {
                    jndiProperties.put(prop.getName(), prop.getValue());
                }
                dataSource =
                        PolicyManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), jndiProperties);
            } else {
                dataSource = PolicyManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), null);
            }
        }
        return dataSource;
    }

    public static String makeString(List<Integer> values) {

        StringBuilder buff = new StringBuilder();
        for (int value : values) {
            buff.append(value).append(",");
        }
        buff.deleteCharAt(buff.length() - 1);
        return buff.toString();
    }

    /**
     * Transform policy into a Operation
     * @param policy policy to be transformed
     * @return policy operation object
     * @throws PolicyTransformException for errors occurred while transforming a policy
     */
    public static Operation transformPolicy(Policy policy) throws PolicyTransformException {
        List<ProfileFeature> effectiveFeatures = policy.getProfile().getProfileFeaturesList();
        PolicyOperation policyOperation = new PolicyOperation();
        policyOperation.setEnabled(true);
        policyOperation.setType(Operation.Type.POLICY);
        policyOperation.setCode(PolicyOperation.POLICY_OPERATION_CODE);
        policyOperation.setProfileOperations(createProfileOperations(effectiveFeatures));
        if (policy.getPolicyType() != null &&
                PolicyManagementConstants.GENERAL_POLICY_TYPE.equals(policy.getPolicyType())) {
            String policyPayloadVersion = policy.getPolicyPayloadVersion();
            float payloadVersion = 0f;
            if (!StringUtils.isEmpty(policyPayloadVersion)) {
                payloadVersion = Float.parseFloat(policyPayloadVersion);
            }
            if (payloadVersion >= 2.0f) {
                setMultipleCorrectiveActions(effectiveFeatures, policyOperation, policy);
            } else {
                setSingleCorrectiveAction(policy, effectiveFeatures);
            }
        }
        policyOperation.setPayLoad(policyOperation.getProfileOperations());
        return policyOperation;
    }

    /**
     * This method is used for generate single corrective action set for a single policy which is
     * bind to the policy payload
     * @param policy regarding policy object
     * @param effectiveFeatures effective feature list
     * @throws PolicyTransformException when transforming of the policy have issues
     */
    private static void setSingleCorrectiveAction(Policy policy, List<ProfileFeature>
            effectiveFeatures) throws PolicyTransformException {
        if (policy.getCorrectiveActions() != null) {
            for (CorrectiveAction correctiveAction : policy.getCorrectiveActions()) {
                if (PolicyManagementConstants.POLICY_CORRECTIVE_ACTION_TYPE
                        .equalsIgnoreCase(correctiveAction.getActionType())) {
                    PolicyAdministratorPoint pap = new PolicyAdministratorPointImpl();
                    try {
                        Policy correctivePolicy = pap.getPolicy(correctiveAction.getPolicyId());
                        if (correctivePolicy == null ||
                                !(PolicyManagementConstants.CORRECTIVE_POLICY_TYPE
                                        .equalsIgnoreCase(correctivePolicy.getPolicyType()))) {
                            String msg = "No corrective policy was found for the policy "
                                    + policy.getPolicyName() + " and policy ID " + policy.getId();
                            log.error(msg);
                            throw new PolicyTransformException(msg);
                        } else {
                            List<ProfileOperation> correctiveProfileOperations =
                                    createProfileOperations(correctivePolicy.getProfile()
                                            .getProfileFeaturesList());
                            ProfileFeature correctivePolicyFeature = new ProfileFeature();
                            correctivePolicyFeature.setProfileId(correctivePolicy.getProfileId());
                            correctivePolicyFeature.setContent(new Gson()
                                    .toJson(correctiveProfileOperations));
                            correctivePolicyFeature.setDeviceType(correctivePolicy
                                    .getProfile().getDeviceType());
                            correctivePolicyFeature.setFeatureCode(
                                    PolicyManagementConstants.CORRECTIVE_POLICY_FEATURE_CODE);
                            correctivePolicyFeature.setId(correctivePolicy.getId());
                            effectiveFeatures.add(correctivePolicyFeature);
                        }
                    } catch (PolicyManagementException e) {
                        String msg = "Error occurred while retrieving corrective " +
                                "policy for policy " + policy.getPolicyName() + " and policy ID " +
                                policy.getId();
                        log.error(msg, e);
                        throw new PolicyTransformException(msg, e);
                    }
                    break;
                }
            }
        }
    }

    /**
     * This method is use for generate multiple corrective actions per policy which is attached
     * to the feature of the policy
     * @param features regarding feature list of the policy
     * @param policyOperation operation list which to be sent to the device
     * @param policy regarding policy
     * @throws PolicyTransformException
     */
    private static void setMultipleCorrectiveActions(List<ProfileFeature> features,
                                             PolicyOperation policyOperation, Policy policy)
            throws PolicyTransformException {
        ProfileOperation correctiveProfileOperation = new ProfileOperation();
        correctiveProfileOperation.setCode(PolicyManagementConstants.POLICY_ACTIONS);
        Set<Integer> correctivePolicyIdSet = new HashSet<>();
        try {
            List<CorrectiveAction> correctiveActions;
            for (ProfileFeature feature : features) {
                correctiveActions = feature.getCorrectiveActions();
                for (CorrectiveAction correctiveAction : correctiveActions) {
                    if (PolicyManagementConstants.POLICY_CORRECTIVE_ACTION_TYPE
                            .equals(correctiveAction.getActionType())) {
                        correctivePolicyIdSet.add(correctiveAction.getPolicyId());
                    } else if (PolicyManagementConstants.EMAIL_CORRECTIVE_ACTION_TYPE
                            .equals(correctiveAction.getActionType())) {
                        createEmailCorrectiveActions(correctiveProfileOperation);
                    }
                    //Add check for another action type in future implementation
                }
            }
            PolicyAdministratorPoint pap = new PolicyAdministratorPointImpl();
            List<Policy> allCorrectivePolicies = pap
                    .getPolicies(PolicyManagementConstants.CORRECTIVE_POLICY_TYPE);
            for (Integer policyId : correctivePolicyIdSet) {
                for (Policy correctivePolicy : allCorrectivePolicies) {
                    if (policyId == correctivePolicy.getId()) {
                        createCorrectiveProfileOperations(correctivePolicy, correctiveProfileOperation);
                        break;
                    }
                }
            }
            policyOperation.getProfileOperations().add(correctiveProfileOperation);
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving corrective policy for policy " +
                    policy.getPolicyName() + " and policy ID " + policy.getId();
            log.error(msg, e);
            throw new PolicyTransformException(msg, e);
        }
    }

    /**
     * Transform email type corrective actions
     * @param correctiveProfileOperation Email type corrective operation
     */
    private static void createEmailCorrectiveActions(ProfileOperation correctiveProfileOperation) {
        ProfileOperation profileOperation = new ProfileOperation();
        profileOperation.setId(PolicyManagementConstants.EMAIL_ACTION_ID);
        profileOperation.setCode(PolicyManagementConstants.EMAIL_FEATURE_CODE);
        profileOperation.setEnabled(true);
        profileOperation.setStatus(Operation.Status.PENDING);
        profileOperation.setType(Operation.Type.PROFILE);
        List<ProfileOperation> profileOperations = new ArrayList<>();
        profileOperation.setPayLoad(profileOperations);
        List<ProfileOperation> payLoad = new ArrayList<>();
        payLoad.add(profileOperation);
        correctiveProfileOperation.setPayLoad(payLoad);
    }

    /**
     * This method is using for generate profile operations list which to be sent to the device.
     * this method is only using multiple corrective actions
     * @param correctivePolicy regarding corrective policy
     * @param correctiveOperationList regarding operations list of the corrective policy
     */
    private static void createCorrectiveProfileOperations(Policy correctivePolicy,
                                                          ProfileOperation correctiveOperationList) {
        ProfileOperation profileOperation = new ProfileOperation();
        profileOperation.setId(correctivePolicy.getId());
        profileOperation.setCode(PolicyManagementConstants.POLICY_FEATURE_CODE);
        profileOperation.setEnabled(true);
        profileOperation.setStatus(Operation.Status.PENDING);
        profileOperation.setType(Operation.Type.PROFILE);
        List<ProfileOperation> profileOperations = createProfileOperations(correctivePolicy
                .getProfile().getProfileFeaturesList());
        profileOperation.setPayLoad(profileOperations);
        List<ProfileOperation> payLoad;
        if (correctiveOperationList.getPayLoad() != null) {
            payLoad = (List<ProfileOperation>) correctiveOperationList.getPayLoad();
        } else {
            payLoad = new ArrayList<>();
        }
        payLoad.add(profileOperation);
        correctiveOperationList.setPayLoad(payLoad);
    }

    /**
     * Create list of profile operations
     * @param effectiveFeatures effective features of the policy
     * @return List of ProfileOperation
     */
    public static List<ProfileOperation> createProfileOperations(List<ProfileFeature> effectiveFeatures) {
        List<ProfileOperation> profileOperations = new ArrayList<>();
        for (ProfileFeature feature : effectiveFeatures) {
            ProfileOperation profileOperation = new ProfileOperation();
            profileOperation.setCode(feature.getFeatureCode());
            profileOperation.setEnabled(true);
            profileOperation.setId(feature.getId());
            profileOperation.setStatus(Operation.Status.PENDING);
            profileOperation.setType(Operation.Type.PROFILE);
            profileOperation.setPayLoad(feature.getContent());
            if (feature.getCorrectiveActions() != null) {
                for (CorrectiveAction correctiveAction : feature.getCorrectiveActions()) {
                    if (correctiveAction.isReactive()) {
                        if (profileOperation.getReactiveActionIds() == null) {
                            profileOperation.setReactiveActionIds(new ArrayList<>());
                        }
                        if (correctiveAction.getActionType().equals(PolicyManagementConstants.EMAIL_CORRECTIVE_ACTION_TYPE)) {
                            profileOperation.getReactiveActionIds().add(PolicyManagementConstants.EMAIL_ACTION_ID);
                        } else if (correctiveAction.getActionType().equals(PolicyManagementConstants.POLICY_CORRECTIVE_ACTION_TYPE)){
                            profileOperation.getReactiveActionIds().add(correctiveAction.getPolicyId());
                        }
                    } else {
                        if (profileOperation.getCorrectiveActionIds() == null) {
                            profileOperation.setCorrectiveActionIds(new ArrayList<>());
                        }
                        if (correctiveAction.getActionType().equals(PolicyManagementConstants.EMAIL_CORRECTIVE_ACTION_TYPE)) {
                            profileOperation.getCorrectiveActionIds().add(PolicyManagementConstants.EMAIL_ACTION_ID);
                        } else if (correctiveAction.getActionType().equals(PolicyManagementConstants.POLICY_CORRECTIVE_ACTION_TYPE)){
                            profileOperation.getCorrectiveActionIds().add(correctiveAction.getPolicyId());
                        }
                    }
                }
            }
            profileOperations.add(profileOperation);
        }
        return  profileOperations;
    }


    public static byte[] getBytes(Object obj) throws java.io.IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        oos.close();
        bos.close();
        byte[] data = bos.toByteArray();
        return data;
    }

    /**
     * Using for converting policy objects into Json strings
     * @param obj
     * @return
     */
    public static String convertToJson(Object obj) {
        return gson.toJson(obj);
    }

    public static boolean convertIntToBoolean(int x) {

        return x == 1;
    }


//    public static Cache getCacheManagerImpl() {
//        return Caching.getCacheManagerFactory()
//                .getCacheManager(PolicyManagementConstants.DM_CACHE_MANAGER).getCache(PolicyManagementConstants
//                        .DM_CACHE);
//    }


    public static Cache<Integer, Policy> getPolicyCache(String name) {
        CacheManager manager = getCacheManager();
        return (manager != null) ? manager.<Integer, Policy>getCache(name) :
                Caching.getCacheManager().<Integer, Policy>getCache(name);
    }

    public static Cache<Integer, List<Policy>> getPolicyListCache(String name) {
        CacheManager manager = getCacheManager();
        return (manager != null) ? manager.<Integer, List<Policy>>getCache(name) :
                Caching.getCacheManager().<Integer, List<Policy>>getCache(name);
    }

    private static CacheManager getCacheManager() {
        return Caching.getCacheManagerFactory().getCacheManager(
                PolicyManagementConstants.DM_CACHE_MANAGER);
    }


    public static HashMap<Integer, Device> covertDeviceListToMap(List<Device> devices) {

        HashMap<Integer, Device> deviceHashMap = new HashMap<>();
        for (Device device : devices) {
            deviceHashMap.put(device.getId(), device);
        }
        return deviceHashMap;
    }


    public static int getMonitoringFrequency() throws PolicyManagementException {

        PlatformConfigurationManagementService configMgtService = new PlatformConfigurationManagementServiceImpl();
        PlatformConfiguration tenantConfiguration;
        int monitoringFrequency = 0;
        try {
            tenantConfiguration = configMgtService.getConfiguration(GENERAL_CONFIG_RESOURCE_PATH);
            List<ConfigurationEntry> configuration = tenantConfiguration.getConfiguration();

            if (configuration != null && !configuration.isEmpty()) {
                for (ConfigurationEntry cEntry : configuration) {
                    if (MONITORING_FREQUENCY.equalsIgnoreCase(cEntry.getName())) {
                        if (cEntry.getValue() == null) {
                            throw new PolicyManagementException("Invalid value, i.e. '" + cEntry.getValue() +
                                    "', is configured as the monitoring frequency");
                        }
                        monitoringFrequency = (int) (Double.parseDouble(cEntry.getValue().toString()) + 0.5d);
                    }
                }
            }

        } catch (ConfigurationManagementException e) {
            log.error("Error while getting the configurations from registry.", e);
        }

        if (monitoringFrequency == 0) {
            PolicyConfiguration policyConfiguration = DeviceConfigurationManager.getInstance().
                    getDeviceManagementConfig().getPolicyConfiguration();
            monitoringFrequency = policyConfiguration.getMonitoringFrequency();
        }

        return monitoringFrequency;
    }


    public static Map<Integer, DeviceGroup> convertDeviceGroupMap(List<DeviceGroup> deviceGroups) {
        Map<Integer, DeviceGroup> groupMap = new HashMap<>();
        for (DeviceGroup dg: deviceGroups){
            groupMap.put(dg.getGroupId(), dg);
        }
        return groupMap;
    }
}
