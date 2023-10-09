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

package io.entgra.device.mgt.core.apimgt.extension.rest.api;

import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.*;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import org.json.JSONObject;

import java.util.List;

public interface PublisherRESTAPIServices {

    Scope[] getScopes(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean isSharedScopeNameExists(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String key)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean addNewSharedScope(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, Scope scope)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean updateSharedScope(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, Scope scope)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    APIInfo getApi(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String apiUuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    APIInfo[] getApis(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    APIInfo addAPI(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, APIInfo api)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean updateApi(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, APIInfo api)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean saveAsyncApiDefinition(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String uuid,
                                   String asyncApiDefinition)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    MediationPolicy[] getAllApiSpecificMediationPolicies(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                                         String apiUuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean addApiSpecificMediationPolicy(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                          String uuid, Mediation mediation)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean deleteApiSpecificMediationPolicy(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                             String uuid, Mediation mediation)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean changeLifeCycleStatus(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                  String uuid, String action)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    APIRevision[] getAPIRevisions(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String uuid,
                                  Boolean deploymentStatus)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    APIRevision addAPIRevision(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                              APIRevision apiRevision)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean deployAPIRevision(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String uuid,
                              String apiRevisionId, List<APIRevisionDeployment> apiRevisionDeploymentList)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean undeployAPIRevisionDeployment(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                          APIRevision apiRevisionDeployment, String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean deleteAPIRevision(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                              APIRevision apiRevision, String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Documentation[] getDocumentations(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                 String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean deleteDocumentations(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                 String uuid, String documentID)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Documentation addDocumentation(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                   String uuid, Documentation documentation)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean addDocumentationContent(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                    String apiUuid, String docId, String docContent)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;
}
