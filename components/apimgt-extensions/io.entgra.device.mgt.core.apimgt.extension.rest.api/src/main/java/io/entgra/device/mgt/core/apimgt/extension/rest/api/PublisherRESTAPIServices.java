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

import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.APIInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.APIRevision;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.APIRevisionDeployment;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Documentation;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Mediation;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.MediationPolicy;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Scope;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;

import java.util.List;

public interface PublisherRESTAPIServices {

    Scope[] getScopes()
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean isSharedScopeNameExists(String key)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean addNewSharedScope(Scope scope)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean updateSharedScope(Scope scope)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean deleteSharedScope(Scope scope)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    APIInfo getApi(String apiUuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    APIInfo[] getApis()
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    APIInfo addAPI(APIInfo api)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean updateApi(APIInfo api)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean saveAsyncApiDefinition(String uuid,
                                   String asyncApiDefinition)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    MediationPolicy[] getAllApiSpecificMediationPolicies(
            String apiUuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean addApiSpecificMediationPolicy(
            String uuid, Mediation mediation)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean deleteApiSpecificMediationPolicy(
            String uuid, Mediation mediation)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean changeLifeCycleStatus(
            String uuid, String action)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    APIRevision[] getAPIRevisions(String uuid,
                                  Boolean deploymentStatus)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    APIRevision addAPIRevision(
            APIRevision apiRevision)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean deployAPIRevision(String uuid,
                              String apiRevisionId, List<APIRevisionDeployment> apiRevisionDeploymentList)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean undeployAPIRevisionDeployment(
            APIRevision apiRevisionDeployment, String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean deleteAPIRevision(
            APIRevision apiRevision, String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Documentation[] getDocumentations(
            String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean deleteDocumentations(
            String uuid, String documentID)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    Documentation addDocumentation(
            String uuid, Documentation documentation)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;

    boolean addDocumentationContent(
            String apiUuid, String docId, String docContent)
            throws APIServicesException, BadRequestException, UnexpectedResponseException;
}
