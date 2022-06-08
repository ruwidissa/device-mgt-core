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
package org.wso2.carbon.device.mgt.jaxrs.service.impl.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.carbon.device.mgt.common.Base64File;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.OperationLogFilters;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceTypeNotFoundException;
import org.wso2.carbon.device.mgt.common.metadata.mgt.Metadata;
import org.wso2.carbon.device.mgt.common.metadata.mgt.WhiteLabelImageRequestPayload;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.core.common.util.HttpUtil;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.common.metadata.mgt.WhiteLabelThemeCreateRequest;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.beans.ApplicationWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.EventConfig;
import org.wso2.carbon.device.mgt.jaxrs.beans.GeofenceWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.OldPasswordResetWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.PolicyWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.ProfileFeature;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleInfo;
import org.wso2.carbon.device.mgt.jaxrs.beans.Scope;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.policy.mgt.common.PolicyPayloadValidator;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Arrays;

public class RequestValidationUtil {

    private static final Log log = LogFactory.getLog(RequestValidationUtil.class);
    /**
     * Checks if multiple criteria are specified in a conditional request.
     *
     * @param type      Device type upon which the selection is done
     * @param user      Device user upon whom the selection is done
     * @param roleName  Role name upon which the selection is done
     * @param ownership Ownership type upon which the selection is done
     * @param status    Enrollment status upon which the selection is done
     */
    public static void validateSelectionCriteria(final String type, final String user, final String roleName,
                                                 final String ownership, final String status) {
        List<String> inputs = new ArrayList<String>() {{
            add(type);
            add(user);
            add(roleName);
            add(ownership);
            add(status);
        }};

//        boolean hasOneSelection = false;
//        for (String i : inputs) {
//            if (i == null) {
//                continue;
//            }
//            hasOneSelection = !hasOneSelection;
//            if (!hasOneSelection) {
//                break;
//            }
//        }
        int count = 0;
        for (String i : inputs) {
            if (i == null) {
                continue;
            }
            count++;
            if (count > 1) {
                break;
            }
        }
        if (count > 1) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("The incoming request has " +
                            "more than one selection criteria defined through query parameters").build());
        }

    }


    public static void validateDeviceIdentifier(String type, String id) {
        boolean isErroneous = false;
        ErrorResponse.ErrorResponseBuilder error = new ErrorResponse.ErrorResponseBuilder();
        if (id == null) {
            isErroneous = true;
            error.addErrorItem(null, "Device identifier cannot be null");
        }
        if (type == null) {
            isErroneous = true;
            error.addErrorItem(null, "Device type cannot be null");
        }
        if (isErroneous) {
            throw new InputValidationException(error.setCode(400l).setMessage("Invalid device identifier").build());

        }
    }

    /**
     * validating the package name requested by user
     *
     * @param applications All the applications in the device
     * @param packageName  Package name sen by the user
     */
    public static void validateApplicationIdentifier(String packageName, List<Application> applications) {
        List<String> packageNames = new ArrayList<>();
        for (Application application : applications) {
            packageNames.add(application.getApplicationIdentifier());
        }
        if (!packageNames.contains(packageName)) {
            String msg = "Invalid package name";
            log.error(msg);
            throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder()
                    .setCode(HttpStatus.SC_BAD_REQUEST)
                    .setMessage(msg).build());
        }
    }

    public static void validateStatus(List<String> statusList) {
        for (String status : statusList) {
            switch (status) {
                case "ACTIVE":
                case "INACTIVE":
                case "UNCLAIMED":
                case "UNREACHABLE":
                case "SUSPENDED":
                case "DISENROLLMENT_REQUESTED":
                case "REMOVED":
                case "BLOCKED":
                case "CREATED":
                    break;
                default:
                    String msg = "Invalid enrollment status type: " + status + ". \nValid status types are " +
                                 "ACTIVE | INACTIVE | UNCLAIMED | UNREACHABLE | SUSPENDED | " +
                                 "DISENROLLMENT_REQUESTED | REMOVED | BLOCKED | CREATED";
                    log.error(msg);
                    throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder()
                                                               .setCode(HttpStatus.SC_BAD_REQUEST)
                                                               .setMessage(msg).build());
            }
        }
    }

    public static void validateOwnershipType(String ownership) {
        if (ownership == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                            "Ownership type cannot be null").build());
        }
        switch (ownership) {
            case "BYOD":
            case "COPE":
                return;
            default:
                throw new InputValidationException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                                "Invalid ownership type received. " +
                                        "Valid ownership types are BYOD | COPE").build());
        }
    }

    public static void validateNotificationStatus(String status) {
        if (status == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                            "Notification status type cannot be null").build());
        }
        switch (status) {
            case "NEW":
            case "CHECKED":
                return;
            default:
                throw new InputValidationException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Invalid notification status type " +
                                "received. Valid status types are NEW | CHECKED").build());
        }
    }

    public static void validateNotificationId(int id) {
        if (id <= 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Invalid notification id. " +
                            "Only positive integers are accepted as valid notification Ids").build());
        }
    }

    public static void validateNotification(Notification notification) {
        if (notification == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Notification content " +
                            "cannot be null").build());
        }
    }

    public static void validateTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Timestamp value " +
                            "cannot be null or empty").build());
        }
        try {
            Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                            "Invalid timestamp value").build());
        }
    }

    public static void validateActivityId(String activityId) {
        if (activityId == null || activityId.isEmpty()) {
            throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder().setCode(400l)
                    .setMessage("Activity Id cannot be null or empty. It should be in the form of " +
                            "'[ACTIVITY][_][any-positive-integer]' instead").build());
        }
        String[] splits = activityId.split("_");
        if (splits.length > 1 && splits[0] != null && !splits[0].isEmpty() && "ACTIVITY".equals(splits[0])) {
            try {
                Long.parseLong(splits[1]);
            } catch (NumberFormatException e) {
                throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder().setCode(400l)
                        .setMessage(
                                "Activity Id should be in the form of '[ACTIVITY][_][any-positive-integer]'")
                        .build());
            }
        } else {
            throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder().setCode(400l)
                    .setMessage("Activity Id should be in the form of '[ACTIVITY][_][any-positive-integer]'")
                    .build());
        }
    }

    public static void validateApplicationInstallationContext(ApplicationWrapper installationCtx) {
        int count = 0;

        if (installationCtx.getDeviceIdentifiers() != null && installationCtx.getDeviceIdentifiers().size() > 0) {
            count++;
        }
        if (installationCtx.getUserNameList() != null && installationCtx.getUserNameList().size() > 0) {
            count++;
        }
        if (installationCtx.getRoleNameList() != null && installationCtx.getRoleNameList().size() > 0) {
            count++;
        }
        if (count > 1) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("The incoming request has " +
                            "more than one application installation criteria defined").build());
        }
    }

    public static void validateApplicationUninstallationContext(ApplicationWrapper installationCtx) {
        int count = 0;

        if (installationCtx.getDeviceIdentifiers() != null && installationCtx.getDeviceIdentifiers().size() > 0) {
            count++;
        }
        if (installationCtx.getUserNameList() != null && installationCtx.getUserNameList().size() > 0) {
            count++;
        }
        if (installationCtx.getRoleNameList() != null && installationCtx.getRoleNameList().size() > 0) {
            count++;
        }
        if (count > 1) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("The incoming request has " +
                            "more than one application un-installation criteria defined").build());
        }
    }

    public static void validateUpdateConfiguration(PlatformConfiguration config) {
        if (config == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Configurations are not defined.")
                            .build());
        } else if (config.getConfiguration() == null || config.getConfiguration().size() == 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Does not contain any " +
                            "configuration entries.").build());
        }
    }

    public static void validateDeviceIdentifiers(List<DeviceIdentifier> deviceIdentifiers) {
        if (deviceIdentifiers == null || deviceIdentifiers.size() == 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Device identifier list is " +
                            "empty.").build());
        }
    }

    public static List<org.wso2.carbon.policy.mgt.common.ProfileFeature> validatePolicyDetails(
            PolicyWrapper policyWrapper) {
        if (policyWrapper == null) {
            String msg = "Found an empty policy";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg)
                            .build());
        }
        return validateProfileFeatures(policyWrapper.getProfile().getProfileFeaturesList());
    }

    public static List<org.wso2.carbon.policy.mgt.common.ProfileFeature> validateProfileFeatures
            (List<ProfileFeature> profileFeatures) {

        if (profileFeatures.isEmpty()) {
            String msg = "Found Empty Policy Feature list to validate.";
            log.error(msg);
            throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder()
                    .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        } else {
            List<org.wso2.carbon.policy.mgt.common.ProfileFeature> features = new ArrayList<>();
            String deviceType = null;
            for (ProfileFeature profileFeature : profileFeatures) {
                if (StringUtils.isBlank(profileFeature.getDeviceTypeId())) {
                    String msg = "Found an invalid policy feature with empty device type data.";
                    log.error(msg);
                    throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder()
                            .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
                }
                if (deviceType != null && !deviceType.equals(profileFeature.getDeviceTypeId())) {
                    String msg = "Found two different device types in profile feature list.";
                    log.error(msg);
                    throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder()
                            .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
                }
                deviceType = profileFeature.getDeviceTypeId();
                org.wso2.carbon.policy.mgt.common.ProfileFeature feature = new org.wso2.carbon.policy.mgt.common.ProfileFeature();
                feature.setContent(profileFeature.getContent());
                feature.setDeviceType(profileFeature.getDeviceTypeId());
                feature.setFeatureCode(profileFeature.getFeatureCode());
                feature.setPayLoad(profileFeature.getPayLoad());
                features.add(feature);
            }

            try {
                DeviceType deviceTypeObj = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceType(deviceType);
                if (deviceTypeObj == null) {
                    String msg = "Found an unsupported device type to validate profile feature.";
                    log.error(msg);
                    throw new InputValidationException(
                            new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg)
                                    .build());
                }

                Class<?> clz;
                switch (deviceTypeObj.getName()) {
                case Constants.ANDROID:
                    clz = Class.forName(Constants.ANDROID_POLICY_VALIDATOR);
                    PolicyPayloadValidator enrollmentNotifier = (PolicyPayloadValidator) clz.getDeclaredConstructor()
                            .newInstance();
                    return enrollmentNotifier.validate(features);
                case Constants.IOS:
                    //todo
                    features = new ArrayList<>();
                    break;
                case Constants.WINDOWS:
                    //todo
                    features = new ArrayList<>();
                    break;
                default:
                    log.error("No policy validator found for device type  " + deviceType);
                    break;
                }
            } catch (DeviceManagementException e) {
                String msg = "Error occurred when validating whether device type is valid one or not " + deviceType;
                log.error(msg, e);
                throw new InputValidationException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                                .setMessage(msg).build());
            } catch (InstantiationException e) {
                if (log.isDebugEnabled()) {
                    String msg = "Error when creating an instance of validator related to deviceType " + deviceType;
                    log.debug(msg, e);
                }
            } catch (IllegalAccessException e) {
                if (log.isDebugEnabled()) {
                    String msg = "Error when accessing an instance of validator related to deviceType " + deviceType;
                    log.debug(msg, e);
                }
            } catch (ClassNotFoundException e) {
                if (log.isDebugEnabled()) {
                    String msg = "Error when loading an instance of validator related to deviceType " + deviceType;
                    log.debug(msg, e);
                }
            } catch (NoSuchMethodException e) {
                if (log.isDebugEnabled()) {
                    String msg = "Error occurred while constructing validator related to deviceType " + deviceType;
                    log.debug(msg, e);
                }
            } catch (InvocationTargetException e) {
                if (log.isDebugEnabled()) {
                    String msg = "Error occurred while instantiating validator related to deviceType " + deviceType;
                    log.debug(msg, e);
                }
            }
            return features;
        }
    }


    public static void validatePolicyIds(List<Integer> policyIds) {
        if (policyIds == null || policyIds.size() == 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Policy Id list is empty.").build
                            ());
        }
    }

    public static void validateRoleName(String roleName) {
        if (roleName == null || roleName.isEmpty()) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Role name isn't valid.").build
                            ());
        }
    }

    public static void validateUsers(List<String> users) {
        if (users == null || users.size() == 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("User list isn't valid.").build
                            ());
        }
    }

    public static void validateCredentials(OldPasswordResetWrapper credentials) {
        if (credentials == null || credentials.getNewPassword() == null || credentials.getOldPassword() == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Old or New password " +
                            "fields cannot be empty").build());
        }
    }

    public static void validateRoleDetails(RoleInfo roleInfo) {
        if (roleInfo == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Request body is "
                            + "empty").build());
        } else if (roleInfo.getRoleName() == null) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Request body is "
                            + "incorrect").build());
        }
    }

    public static void validateScopes(List<Scope> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Scope details of the request body" +
                            " is incorrect or empty").build());
        }
    }

    public static void validatePaginationParameters(int offset, int limit) {
        if (offset < 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Request parameter offset is s " +
                            "negative value.").build());
        }
        if (limit < 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Request parameter limit is a " +
                            "negative value.").build());
        }
        if (limit > 100) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Request parameter limit should" +
                            " be less than or equal to 100.").build());
        }

    }

    public static void validateOwnerParameter(String owner) {
        if (owner == null || owner.isEmpty()) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Request parameter owner should" +
                            " be non empty.").build());
        }
    }

    /**
     * Checks if operation log filters are valid
     *
     * @param type          Device type upon which the selection is done
     * @param createdFrom   Since when created date and time upon to filter operation logs
     * @param createdTo     Till when created date and time upon to filter operation logs
     * @param updatedFrom   Since when received date and time upon to filter operation logs
     * @param updatedTo     Till when received date and time upon to filter operation logs
     * @param status        List of operation status codes upon to filter operation logs
     */
    public OperationLogFilters validateOperationLogFilters(List<String> operationCode,
                                                           Long createdFrom, Long createdTo, Long updatedFrom,
                                                           Long updatedTo, List<String> status, String type)
            throws DeviceTypeNotFoundException, DeviceManagementException {
        OperationLogFilters operationLogFilters = new OperationLogFilters();
        Calendar date = Calendar.getInstance();
        long timeMilli = date.getTimeInMillis();

        if (updatedFrom != null || updatedTo != null) {
            validateDates(updatedFrom, updatedTo);
            //if user only sends the fromDate toDate sets as current date
            if (updatedFrom != null && updatedTo == null) {
                timeMilli = timeMilli / 1000;
                operationLogFilters.setUpdatedDayTo(timeMilli);
                operationLogFilters.setUpdatedDayFrom(updatedFrom);
            } else {
                operationLogFilters.setUpdatedDayFrom(updatedFrom);
                operationLogFilters.setUpdatedDayTo(updatedTo);
            }
        }
        if (createdTo != null || createdFrom != null) {
            validateDates(createdFrom, createdTo);
            createdFrom = createdFrom * 1000;
            //if user only sends the fromDate toDate sets as current date
            if (createdFrom != null && createdTo == null) {
                operationLogFilters.setCreatedDayFrom(createdFrom);
                operationLogFilters.setCreatedDayTo(timeMilli);
            } else {
                createdTo = createdTo * 1000;
                operationLogFilters.setCreatedDayFrom(createdFrom);
                operationLogFilters.setCreatedDayTo(createdTo);
            }
        }
        if (status != null && !status.isEmpty()) {
            validateStatusFiltering(status);
            operationLogFilters.setStatus(status);
        }

        if (operationCode != null && !operationCode.isEmpty()) {
            validateOperationCodeFiltering(operationCode, type);
            operationLogFilters.setOperationCode(operationCode);
        }
        return operationLogFilters;
    }

    /**
     * Checks if date ranges requested by user are valid
     *
     * @param toDate   Till when created/updated dates upon to validate dates
     * @param fromDate Since when created/updated dates upon to validate dates
     */
    public static void validateDates(Long fromDate, Long toDate) {
        Calendar date = Calendar.getInstance();
        long timeMilli = date.getTimeInMillis();
        timeMilli = timeMilli / 1000;
        //if user only sends toDate
        if (fromDate == null && toDate != null) {
            String msg = "Request parameter must sent with the from date parameter";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder()
                            .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }
        //if user sends future dates
        if (fromDate != null && toDate != null) {
            if (timeMilli < fromDate || timeMilli < toDate) {
                String msg = "Bad Request cannot apply future dates";
                log.error(msg);
                throw new InputValidationException(
                        new ErrorResponse.ErrorResponseBuilder()
                                .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
            }
        }
        //if user send future dates - only when from date sends
        if (fromDate != null && toDate == null) {
            if (fromDate > timeMilli) {
                String msg = "Bad Request cannot apply future dates";
                log.error(msg);
                throw new InputValidationException(
                        new ErrorResponse.ErrorResponseBuilder()
                                .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
            }
        }
    }

    /**
     * Checks if user requested operation status codes are valid.
     *
     * @param status    status codes upon to filter operation logs using status
     */
    public static void validateStatusFiltering(List<String> status) {
        for (int i = 0; i < status.size(); i++) {
            if (Constants.OperationStatus.COMPLETED.toUpperCase().equals(status.get(i))
                    || Constants.OperationStatus.ERROR.toUpperCase().equals(status.get(i))
                    || Constants.OperationStatus.NOTNOW.toUpperCase().equals(status.get(i))
                    || Constants.OperationStatus.REPEATED.toUpperCase().equals(status.get(i))
                    || Constants.OperationStatus.PENDING.toUpperCase().equals(status.get(i))
                    || Constants.OperationStatus.IN_PROGRESS.toUpperCase().equals(status.get(i))
                    || Constants.OperationStatus.REQUIRED_CONFIRMATION.toUpperCase().equals(status.get(i))
                    || Constants.OperationStatus.CONFIRMED.toUpperCase().equals(status.get(i))) {
            } else {
                String msg = "Invalid status type: " + status + ". \nValid status types are COMPLETED | ERROR | " +
                        "IN_PROGRESS | NOTNOW | PENDING | REPEATED | REQUIRED_CONFIRMATION | CONFIRMED";
                log.error(msg);
                throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder()
                        .setCode(HttpStatus.SC_BAD_REQUEST)
                        .setMessage(msg).build());
            }
        }
    }

    /**
     * Checks if user requested operation codes are valid.
     *
     * @param operationCode    operation codes upon to filter operation logs using operation codes
     * @param type             status codes upon to filter operation logs using status
     */
    public static void validateOperationCodeFiltering(List<String> operationCode, String type)
            throws DeviceTypeNotFoundException, DeviceManagementException {
        int count = 0;
        List<Feature> features;
        DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
        FeatureManager fm = dms.getFeatureManager(type);
        features = fm.getFeatures("operation");
        for (String oc : operationCode) {
            for (Feature f : features) {
                if (f.getCode().equals(oc)) {
                    count++;
                    break;
                }
            }
        }
        if (!(count == operationCode.size())) {
            String msg = "Requested Operation code invalid";
            log.error(msg);
            throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder()
                    .setCode(HttpStatus.SC_BAD_REQUEST)
                    .setMessage(msg).build());
        }
    }

    /**
     * Check if whitelabel theme create request contains valid payload and all required payload
     *
     * @param whiteLabelThemeCreateRequest {@link WhiteLabelThemeCreateRequest}
     */
    public static void validateWhiteLabelTheme(WhiteLabelThemeCreateRequest whiteLabelThemeCreateRequest) {
        if (whiteLabelThemeCreateRequest.getFavicon() == null) {
            String msg = "Favicon is required to whitelabel";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder()
                            .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }
        if (whiteLabelThemeCreateRequest.getLogo() == null) {
            String msg = "Logo is required to whitelabel";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder()
                            .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }
        if (whiteLabelThemeCreateRequest.getFooterText() == null) {
            String msg = "Footer text is required to whitelabel";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder()
                            .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }
        if (whiteLabelThemeCreateRequest.getAppTitle() == null) {
            String msg = "App title is required to whitelabel";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder()
                            .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }
        try {
            validateWhiteLabelImage(whiteLabelThemeCreateRequest.getFavicon());
            validateWhiteLabelImage(whiteLabelThemeCreateRequest.getLogo());
        } catch (InputValidationException e) {
            String msg = "Payload contains invalid base64 files";
            log.error(msg, e);
            throw e;
        }
    }

    /**
     * Validate if {@link WhiteLabelImageRequestPayload} contains mandatory fields.
     */
    private static void validateWhiteLabelImage(WhiteLabelImageRequestPayload whiteLabelImage) {
        if (whiteLabelImage.getImageType() == null) {
            String msg = "Invalid payload found with the request. White label imageType cannot be null.";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder()
                            .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }
        if (whiteLabelImage.getImageType() == WhiteLabelImageRequestPayload.ImageType.BASE64) {
            try {
                Base64File image = new Gson().fromJson(whiteLabelImage.getImage(), Base64File.class);
                validateBase64File(image);
            } catch (JsonSyntaxException e) {
                String msg = "Invalid image payload found with the request. Image object does not represent a Base64 File. " +
                        "Hence verify the request payload object.";
                log.error(msg, e);
                throw new InputValidationException(
                        new ErrorResponse.ErrorResponseBuilder()
                                .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
            }
        }
        else if (whiteLabelImage.getImageType() == WhiteLabelImageRequestPayload.ImageType.URL) {
            try {
                String imageUrl = new Gson().fromJson(whiteLabelImage.getImage(), String.class);
                if (!HttpUtil.isHttpUrlValid(imageUrl)) {
                    String msg = "Invalid image url provided for white label image.";
                    log.error(msg);
                    throw new InputValidationException(
                            new ErrorResponse.ErrorResponseBuilder()
                                    .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
                }
            } catch (JsonSyntaxException e) {
                String msg = "Invalid payload found with the request. Hence verify the request payload object.";
                log.error(msg, e);
                throw new InputValidationException(
                        new ErrorResponse.ErrorResponseBuilder()
                                .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
            }
        } else {
            String msg = "Invalid payload found with the request. Unknown white label imageType " + whiteLabelImage.getImageType();
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder()
                            .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }
    }

    /**
     * Validate if {@link Base64File} contains mandatory fields.
     */
    private static void validateBase64File(Base64File base64File) {
        if (base64File.getBase64String() == null || base64File.getName() == null) {
            String msg = "Base64File doesn't contain required properties. name and base64String properties " +
                    "are required fields for base64file type";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder()
                            .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }
    }

    /**
     * Validate if the metaData and metaKey values are non empty & in proper format.
     *
     * @param metadata a Metadata instance, which contains user submitted values
     */
    public static void validateMetadata(Metadata metadata) {
        if (StringUtils.isEmpty(metadata.getMetaKey())) {
            String msg = "Request parameter metaKey should be non empty.";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder()
                            .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }
        String regex = "^[a-zA-Z0-9_.]*$";
        if (!metadata.getMetaKey().matches(regex)) {
            String msg = "Request parameter metaKey should only contain period, " +
                    "underscore and alphanumeric characters.";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder()
                            .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }
        if (metadata.getMetaValue() == null) {
            String msg = "Request parameter metaValue should be non empty.";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder()
                            .setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }
        if (metadata.getDataType() != null) {
            for (Metadata.DataType dataType : Metadata.DataType.values()) {
                if (dataType.name().equals(metadata.getDataType().name())) {
                    return;
                }
            }
        }
        String msg = "Request parameter dataType should  only contain one of following:" +
                Arrays.asList(Metadata.DataType.values());
        log.error(msg);
        throw new InputValidationException(
                new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
    }

    public static boolean isNonFilterRequest(String username, String firstName, String lastName, String emailAddress) {
        return StringUtils.isEmpty(username) && StringUtils.isEmpty(firstName) && StringUtils.isEmpty(lastName)
                && StringUtils.isEmpty(emailAddress);
    }

    /**
     * Check the request payload attributes are correct for create a geofence
     * @param geofenceWrapper request payload data
     */
    public static void validateGeofenceData(GeofenceWrapper geofenceWrapper) {
        boolean isGeoJsonExists = false;
        if (geofenceWrapper.getFenceName() == null || geofenceWrapper.getFenceName().trim().isEmpty()) {
            String msg = "Geofence name should not be null or empty";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }
        if (geofenceWrapper.getGeoJson() != null && !geofenceWrapper.getGeoJson().trim().isEmpty()) {
            isGeoJsonExists = true;
        }
        if ((geofenceWrapper.getLatitude() < -90 || geofenceWrapper.getLatitude() > 90) && !isGeoJsonExists) {
            String msg = "Latitude should be a value between -90 and 90";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }
        if ((geofenceWrapper.getLongitude() < -180 || geofenceWrapper.getLongitude() > 180) && !isGeoJsonExists) {
            String msg = "Longitude should be a value between -180 and 180";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }
        if (geofenceWrapper.getRadius() < 1 && !isGeoJsonExists) {
            String msg = "Minimum radius of the fence should be 1m";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }
        if (geofenceWrapper.getFenceShape().trim().isEmpty()) {
            String msg = "Fence shape should not be empty";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }
    }

    /**
     * Check the request payload attributes are correct for create an event record
     * @param eventConfig request payload data
     */
    public static void validateEventConfigurationData(List<EventConfig> eventConfig) {
        if (eventConfig == null ||eventConfig.isEmpty()) {
            String msg = "Event configuration is mandatory, since should not be null or empty";
            log.error(msg);
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
        }

        for (EventConfig config : eventConfig) {
            if (config.getActions() == null || config.getActions().isEmpty()) {
                String msg = "Event actions are mandatory, since should not be null or empty";
                log.error(msg);
                throw new InputValidationException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
            }

            if (config.getEventLogic() == null || config.getEventLogic().trim().isEmpty()) {
                String msg = "Event logic is mandatory, since should not be null or empty";
                log.error(msg);
                throw new InputValidationException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_BAD_REQUEST).setMessage(msg).build());
            }
        }
    }

    public static void validateTimeDuration(long startTimestamp, long endTimestamp) {
        if (startTimestamp > endTimestamp) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l)
                            .setMessage("Request parameter startTimestamp should not be " +
                                    "a higher value than endTimestamp").build());
        }
    }
}
