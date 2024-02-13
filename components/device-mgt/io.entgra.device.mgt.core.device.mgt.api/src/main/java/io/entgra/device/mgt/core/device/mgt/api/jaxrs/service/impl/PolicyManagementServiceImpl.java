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
package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;

import io.entgra.device.mgt.core.device.mgt.common.PolicyPaginationRequest;
import io.entgra.device.mgt.core.device.mgt.core.permission.mgt.PermissionManagerServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import io.entgra.device.mgt.core.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.PolicyList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.PolicyWrapper;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.PriorityUpdatedPolicyWrapper;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ProfileFeature;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.PolicyManagementService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.FilteringUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.RequestValidationUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtUtil;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Policy;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyAdministratorPoint;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyManagementException;
import io.entgra.device.mgt.core.policy.mgt.core.PolicyManagerService;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Path("/policies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PolicyManagementServiceImpl implements PolicyManagementService {

    private static final String API_BASE_PATH = "/policies";
    private static final Log log = LogFactory.getLog(PolicyManagementServiceImpl.class);

    @POST
    @Override
    public Response addPolicy(@Valid PolicyWrapper policyWrapper) {
        List<io.entgra.device.mgt.core.policy.mgt.common.ProfileFeature> features = RequestValidationUtil
                .validatePolicyDetails(policyWrapper);
        // validation failure results;
        if (!features.isEmpty()) {
            String msg = "Policy feature/s validation failed." ;
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg + " Features : " + features).build();
        }
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();

        try {
            Policy policy = this.getPolicyFromWrapper(policyWrapper);

            List<Device> devices = policy.getDevices();
            if (devices != null && devices.size() == 1) {
                DeviceAccessAuthorizationService deviceAccessAuthorizationService =
                        DeviceManagementDataHolder.getInstance().getDeviceAccessAuthorizationService();
                DeviceIdentifier deviceIdentifier = new DeviceIdentifier(devices.get(0).getDeviceIdentifier(),
                        devices.get(0).getType());
                PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                String username = threadLocalCarbonContext.getUsername();
                try {
                    String requiredPermission = PermissionManagerServiceImpl.getInstance().getRequiredPermission();
                    String[] requiredPermissions = new String[] {requiredPermission};
                    if (!deviceAccessAuthorizationService.isUserAuthorized(deviceIdentifier, username, requiredPermissions)) {
                        return Response.status(Response.Status.UNAUTHORIZED).entity(
                                new ErrorResponse.ErrorResponseBuilder().setMessage
                                        ("Current logged in user is not authorized to add policies").build()).build();
                    }
                } catch (DeviceAccessAuthorizationException e) {
                    String msg = "Error occurred while checking if the current user is authorized to add a policy";
                    log.error(msg, e);
                    return Response.serverError().entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
                }
            }

            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            Policy createdPolicy = pap.addPolicy(policy);

            return Response.created(new URI(API_BASE_PATH + "/" + createdPolicy.getId())).entity(createdPolicy).build();
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while adding policy";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving device list.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        } catch (URISyntaxException e) {
            String msg = "Error occurred while composing the location URI, which represents information of the " +
                    "newly created policy";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * GEt {@link Policy} from {@link PolicyWrapper}
     *
     * @param policyWrapper {@link PolicyWrapper}
     * @return {@link Policy}
     * @throws DeviceManagementException if error occurred while creating {@link Policy} object from
     * {@link PolicyWrapper}
     */
    private Policy getPolicyFromWrapper(@Valid PolicyWrapper policyWrapper) throws DeviceManagementException {
        Policy policy = new Policy();
        policy.setPolicyName(policyWrapper.getPolicyName());
        policy.setDescription(policyWrapper.getDescription());
        policy.setProfile(DeviceMgtUtil.convertProfile(policyWrapper.getProfile()));
        policy.setOwnershipType(policyWrapper.getOwnershipType());
        policy.setActive(policyWrapper.isActive());
        policy.setRoles(policyWrapper.getRoles());
        policy.setUsers(policyWrapper.getUsers());
        policy.setCompliance(policyWrapper.getCompliance());
        policy.setDeviceGroups(policyWrapper.getDeviceGroups());
        policy.setPolicyType(policyWrapper.getPolicyType());
        policy.setPolicyPayloadVersion(policyWrapper.getPayloadVersion());
        policy.setCorrectiveActions(policyWrapper.getCorrectiveActions());
        //TODO iterates the device identifiers to create the object. need to implement a proper DAO layer here.
        List<Device> devices = new ArrayList<>();
        List<DeviceIdentifier> deviceIdentifiers = policyWrapper.getDeviceIdentifiers();
        if (deviceIdentifiers != null) {
            for (DeviceIdentifier id : deviceIdentifiers) {
                devices.add(DeviceMgtAPIUtils.getDeviceManagementService().getDevice(id, false));
            }
        }
        policy.setDevices(devices);
        policy.setTenantId(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
        return policy;
    }

    @GET
    @Override
    public Response getPolicies(
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        List<Policy> policies;
        List<Policy> filteredPolicies;
        PolicyList targetPolicies = new PolicyList();
        try {
            PolicyAdministratorPoint policyAdministratorPoint = policyManagementService.getPAP();
            policies = policyAdministratorPoint.getPolicies();
            if(offset == 0 && limit == 0){
                targetPolicies.setCount(policies.size());
                targetPolicies.setList(policies);
            }else{
                targetPolicies.setCount(policies.size());
                filteredPolicies = FilteringUtil.getFilteredList(policies, offset, limit);
                targetPolicies.setList(filteredPolicies);
            }
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving all available policies";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(targetPolicies).build();
    }

    @GET
    @Path("/{id}")
    @Override
    public Response getPolicy(@PathParam("id") int id, @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        final Policy policy;
        try {
            PolicyAdministratorPoint policyAdministratorPoint = policyManagementService.getPAP();
            policy = policyAdministratorPoint.getPolicy(id);
            if (policy == null) {
                String msg = "Policy not found.";
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving policy corresponding to the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(policy).build();
    }

    @PUT
    @Path("/{id}")
    @Override
    public Response updatePolicy(@PathParam("id") int id, @Valid PolicyWrapper policyWrapper) {
        List<io.entgra.device.mgt.core.policy.mgt.common.ProfileFeature> features = RequestValidationUtil
                .validatePolicyDetails(policyWrapper);
        // validation failure results;
        if (!features.isEmpty()) {
            String msg = "Policy feature/s validation failed." ;
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg + " Features : " + features).build();
        }
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        try {
            Policy policy = this.getPolicyFromWrapper(policyWrapper);
            policy.setId(id);
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            Policy existingPolicy = pap.getPolicy(id);
            if (existingPolicy == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Policy not found.").build();
            }
            pap.updatePolicy(policy);
            return Response.status(Response.Status.OK).entity("Policy has successfully been updated.").build();
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while updating the policy";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the device list.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @POST
    @Path("/remove-policy")
    @Override
    public Response removePolicies(List<Integer> policyIds) {
        RequestValidationUtil.validatePolicyIds(policyIds);
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        boolean policyDeleted = true;
        String invalidPolicyIds = "";
        try {
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            for (int i : policyIds) {
                Policy policy = pap.getPolicy(i);
                if (policy == null) {
                    invalidPolicyIds += i + ",";
                    policyDeleted = false;
                }
            }
            if (policyDeleted) {
                for (int i : policyIds) {
                    Policy policy = pap.getPolicy(i);
                    pap.deletePolicy(policy);
                }
            }
        } catch (PolicyManagementException e) {
            String msg = "ErrorResponse occurred while removing policies";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        if (policyDeleted) {
            return Response.status(Response.Status.OK).entity("Policies have been successfully " +
                                                              "deleted").build();
        } else {
            //TODO:Check of this logic is correct
            String modifiedInvalidPolicyIds =
                    invalidPolicyIds.substring(0, invalidPolicyIds.length() - 1);
            String msg = "Policies does not exist.";
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    @POST
    @Path("/activate-policy")
    @Override
    public Response activatePolicies(List<Integer> policyIds) {
        RequestValidationUtil.validatePolicyIds(policyIds);
        boolean isPolicyActivated = false;
        try {
            PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            for (int i : policyIds) {
                Policy policy = pap.getPolicy(i);
                if (policy != null) {
                    pap.activatePolicy(i);
                    isPolicyActivated = true;
                }
            }
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while activating policies";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        }
        if (isPolicyActivated) {
            return Response.status(Response.Status.OK).entity("Selected policies have been successfully activated")
                    .build();
        } else {
            String msg = "Selected policies have not been activated.";
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        }
    }

    @POST
    @Path("/deactivate-policy")
    @Override
    public Response deactivatePolicies(List<Integer> policyIds) {
        RequestValidationUtil.validatePolicyIds(policyIds);
        boolean isPolicyDeActivated = false;
        try {
            PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            for (int i : policyIds) {
                Policy policy = pap.getPolicy(i);
                if (policy != null) {
                    pap.inactivatePolicy(i);
                    isPolicyDeActivated = true;
                }
            }
        } catch (PolicyManagementException e) {
            String msg = "Exception in inactivating policies.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        if (isPolicyDeActivated) {
            return Response.status(Response.Status.OK).entity("Selected policies have been successfully " +
                    "deactivated").build();
        } else {
            String msg = "Selected policies have not been activated.";
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        }
    }

    @Override
    @PUT
    @Produces("application/json")
    @Path("apply-changes")
    public Response applyChanges() {
        try {
            PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            pap.publishChanges();
        } catch (PolicyManagementException e) {
            String msg = "Exception in applying changes.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity("Changes have been successfully updated.").build();
    }

    @PUT
    @Path("/priorities")
    public Response updatePolicyPriorities(List<PriorityUpdatedPolicyWrapper> priorityUpdatedPolicies) {
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        List<Policy> policiesToUpdate = new ArrayList<>(priorityUpdatedPolicies.size());

        for (PriorityUpdatedPolicyWrapper priorityUpdatedPolicy : priorityUpdatedPolicies) {
            Policy policyObj = new Policy();
            policyObj.setId(priorityUpdatedPolicy.getId());
            policyObj.setPriorityId(priorityUpdatedPolicy.getPriority());
            policiesToUpdate.add(policyObj);
        }
        boolean policiesUpdated;
        try {
            PolicyAdministratorPoint pap = policyManagementService.getPAP();
            policiesUpdated = pap.updatePolicyPriorities(policiesToUpdate);
        } catch (PolicyManagementException e) {
            String error = "Exception in updating policy priorities.";
            log.error(error, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(error).build()).build();
        }
        if (policiesUpdated) {
            return Response.status(Response.Status.OK).entity("Policy Priorities successfully "
                    + "updated.").build();

        } else {
            String msg = "Policy priorities did not update. Bad Request.";
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        }
    }

    @GET
    @Path("/effective-policy/{deviceType}/{deviceId}")
    @Override
    public Response getEffectivePolicy(@PathParam("deviceType") String deviceType, @PathParam("deviceId") String deviceId) {
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        final Policy policy;
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(deviceType);
            Device device;
            try {
                device = DeviceMgtAPIUtils.getDeviceManagementService().getDevice(deviceIdentifier, false);
            } catch (DeviceManagementException e) {
                String msg = "Error occurred while retrieving '" + deviceType + "' device, which carries the id '"
                        + deviceId + "'";
                log.error(msg, e);
                return Response.serverError().entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            }
            policy = policyManagementService.getAppliedPolicyToDevice(device);
            if (policy == null) {
                String msg = "Policy not found for the requested device.";
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving policy corresponding to the id '" + deviceType + "'"+ deviceId;
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(policy).build();
    }

    @GET
    @Path("/type/{policyType}")
    @Override
    public Response getPolicies(
            @PathParam("policyType") String policyType,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {

        RequestValidationUtil.validatePaginationParameters(offset, limit);
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        List<Policy> policies;
        List<Policy> filteredPolicies;
        PolicyList targetPolicies = new PolicyList();
        try {
            PolicyAdministratorPoint policyAdministratorPoint = policyManagementService.getPAP();
            policies = policyAdministratorPoint.getPolicies(policyType);
            targetPolicies.setCount(policies.size());
            filteredPolicies = FilteringUtil.getFilteredList(policies, offset, limit);
            targetPolicies.setList(filteredPolicies);
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving all available policies";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }

        return Response.status(Response.Status.OK).entity(targetPolicies).build();
    }

    @POST
    @Path("/validate")
    @Override
    public Response validatePolicy(List<ProfileFeature> profileFeaturesList) {
        List<io.entgra.device.mgt.core.policy.mgt.common.ProfileFeature> features
                = RequestValidationUtil.validateProfileFeatures(profileFeaturesList);
        // validation failure results;
        if (!features.isEmpty()) {
            String msg = "Policy feature/s validation failed." ;
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg + " Features : " +features).build();
        }
        return Response.status(Response.Status.OK).entity("Valid request").build();

    }

    @GET
    @Path("/list")
    @Override
    public Response getPolicyList(
            @QueryParam("name") String name,
            @QueryParam("type") String type,
            @QueryParam("status") String status,
            @QueryParam("deviceType") String deviceType,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        List<Policy> policies;
        PolicyList targetPolicies = new PolicyList();
        PolicyPaginationRequest request = new PolicyPaginationRequest(offset, limit);
        if (name != null){
            request.setName(name);
        }
        if (type != null){
            request.setType(type);
        }
        if (status != null){
            request.setStatus(status);
        }
        if (deviceType != null) {
            request.setDeviceType(deviceType);
        }
        try {
            PolicyAdministratorPoint policyAdministratorPoint = policyManagementService.getPAP();
            policies = policyAdministratorPoint.getPolicyList(request);
            targetPolicies.setCount(policyAdministratorPoint.getPolicyCount());
            targetPolicies.setList(policies);
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving all available policies";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(targetPolicies).build();
    }

}
