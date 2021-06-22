/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.extensions.device.type.template.feature;

import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.Operation;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.OperationMetadata;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.Params;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.UIParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This implementation retreives the features that are configured through the deployer.
 */
public class ConfigurationBasedFeatureManager implements FeatureManager {
    private List<Feature> features = new ArrayList<>();
    private static final String METHOD = "method";
    private static final String URI = "uri";
    private static final String OPERATION_META = "operationMeta";
    private static final String CONTENT_TYPE = "contentType";
    private static final String PERMISSION = "permission";
    private static final String SCOPE = "scope";
    private static final String ICON = "icon";
    private static final String FILTERS = "filters";
    private static final String PATH_PARAMS = "pathParams";
    private static final String QUERY_PARAMS = "queryParams";
    private static final String FORM_PARAMS = "formParams";
    private static final String UI_PARAMS = "uiParams";
    private static final Pattern PATH_PARAM_REGEX = Pattern.compile("\\{(.*?)\\}");

    public ConfigurationBasedFeatureManager(
            List<org.wso2.carbon.device.mgt.extensions.device.type.template.config.Feature> features) {
        for (org.wso2.carbon.device.mgt.extensions.device.type.template.config.Feature feature : features) {
            Feature deviceFeature = new Feature();
            deviceFeature.setCode(feature.getCode());
            deviceFeature.setName(feature.getName());
            deviceFeature.setDescription(feature.getDescription());
            deviceFeature.setType(feature.getType());
            Operation operation = feature.getOperation();
            List<Feature.MetadataEntry> metadataEntries = null;
            if (feature.getMetaData() != null) {
                metadataEntries = new ArrayList<>();
                int id = 0;
                for (String metaData : feature.getMetaData()) {
                    Feature.MetadataEntry metadataEntry = new Feature.MetadataEntry();
                    metadataEntry.setId(id);
                    metadataEntry.setValue(metaData);
                    metadataEntries.add(metadataEntry);
                    id++;
                }
            }
            if (operation != null) {
                deviceFeature.setHidden(operation.isHidden());
                Map<String, Object> operationMeta = new HashMap<>();
                OperationMetadata metadata = operation.getMetadata();

                List<String> pathParams = new ArrayList<>();

                if (metadata != null) {
                    operationMeta.put(METHOD, metadata.getMethod().toUpperCase());
                    operationMeta.put(URI, metadata.getUri());
                    if (StringUtils.isNotEmpty(metadata.getContentType())) {
                        operationMeta.put(CONTENT_TYPE, metadata.getContentType());
                    }
                    if (StringUtils.isNotEmpty(metadata.getPermission())) {
                        operationMeta.put(PERMISSION, metadata.getPermission());
                    }
                    if (StringUtils.isNotEmpty(metadata.getScope())) {
                        operationMeta.put(SCOPE, metadata.getScope());
                    }
                    if (metadata.getFilterList() != null && metadata.getFilterList().size() > 0) {
                        operationMeta.put(FILTERS, metadata.getFilterList());
                    }
                    operationMeta.put(ICON, operation.getIcon());
                    setPathParams(metadata.getUri(), pathParams);
                }
                operationMeta.put(PATH_PARAMS, pathParams);

                Params params = operation.getParams();
                if (params != null) {
                    List<String> queryParams = params.getQueryParameters() != null ?
                            params.getQueryParameters().getParameter() : new ArrayList<>();
                    List<String> formParams = params.getFormParameters() != null ?
                            params.getFormParameters().getParameter() : new ArrayList<>();
                    List<UIParameter> uiParams = params.getUiParameters() != null ?
                            params.getUiParameters().getUiParameterList() : new ArrayList<>();
                    operationMeta.put(QUERY_PARAMS, queryParams);
                    operationMeta.put(UI_PARAMS, uiParams);
                    operationMeta.put(FORM_PARAMS, formParams);
                }

                if (metadataEntries == null) {
                    metadataEntries = new ArrayList<>();
                }
                Feature.MetadataEntry metadataEntry = new Feature.MetadataEntry();
                metadataEntry.setId(0);
                metadataEntry.setName(OPERATION_META);
                metadataEntry.setValue(operationMeta);
                metadataEntries.add(metadataEntry);
                deviceFeature.setMetadataEntries(metadataEntries);
            } else {
                deviceFeature.setHidden(true);
            }
            this.features.add(deviceFeature);
        }
    }

    @Override
    public boolean addFeature(Feature feature) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean addFeatures(List<Feature> features) throws DeviceManagementException {
        return false;
    }

    @Override
    public Feature getFeature(String name) throws DeviceManagementException {
        Feature extractedFeature = null;
        for (Feature feature : features) {
            if (feature.getName().equalsIgnoreCase(name)) {
                extractedFeature = feature;
            }
        }
        return extractedFeature;
    }

    @Override
    public List<Feature> getFeatures() throws DeviceManagementException {
        return features;
    }

    @Override
    public List<Feature> getFeatures(String type) throws DeviceManagementException {
        if (StringUtils.isEmpty(type)) {
            return this.getFeatures();
        }

        if (features == null) {
            return null;
        } else {
            List<Feature> filteredFeatures = new ArrayList<>();
            for (Feature feature : this.getFeatures()) {
                if (type.equals(feature.getType())) {
                    filteredFeatures.add(feature);
                }
            }
            return filteredFeatures;
        }
    }

    @Override
    public List<Feature> getFeatures(String type, boolean isHidden) throws DeviceManagementException {
        if (features == null) {
            return null;
        } else {
            List<Feature> filteredFeatures = new ArrayList<>();
            if (StringUtils.isEmpty(type)) {
                for (Feature feature : this.getFeatures()) {
                    if (isHidden == feature.isHidden()) {
                        filteredFeatures.add(feature);
                    }
                }
                return filteredFeatures;
            } else {
                for (Feature feature : this.getFeatures()) {
                    if (isHidden == feature.isHidden() && type.equals(feature.getType())) {
                        filteredFeatures.add(feature);
                    }
                }
                return filteredFeatures;
            }
        }
    }

    @Override
    public boolean removeFeature(String name) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean addSupportedFeaturesToDB() throws DeviceManagementException {
        return false;
    }

    private void setPathParams(String context, List<String> pathParams) {
        Matcher regexMatcher = PATH_PARAM_REGEX.matcher(context);
        while (regexMatcher.find()) {
            pathParams.add(regexMatcher.group(1));
        }
    }
}
