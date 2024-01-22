/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.enforce.Impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.entgra.device.mgt.core.cea.mgt.common.bean.ActiveSyncDevice;
import io.entgra.device.mgt.core.cea.mgt.common.bean.ActiveSyncServer;
import io.entgra.device.mgt.core.cea.mgt.common.bean.CEAPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.GracePeriod;
import io.entgra.device.mgt.core.cea.mgt.common.bean.MailboxProfile;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.EmailOutlookAccessPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.GraceAllowedPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.WebOutlookAccessPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAEnforcementException;
import io.entgra.device.mgt.core.cea.mgt.common.service.CEAEnforcementService;
import io.entgra.device.mgt.core.cea.mgt.common.util.Constants;
import io.entgra.device.mgt.core.cea.mgt.common.util.EASMgtUtil;
import io.entgra.device.mgt.core.cea.mgt.enforce.Impl.gateway.ExchangeOnlineGatewayServiceImpl;
import io.entgra.device.mgt.core.cea.mgt.enforce.bean.ExoPowershellCommand;
import io.entgra.device.mgt.core.cea.mgt.enforce.bean.PowershellCommand;
import io.entgra.device.mgt.core.cea.mgt.enforce.bean.PowershellRequest;
import io.entgra.device.mgt.core.cea.mgt.enforce.bean.PowershellResponse;
import io.entgra.device.mgt.core.cea.mgt.enforce.exception.GatewayServiceException;
import io.entgra.device.mgt.core.cea.mgt.enforce.exception.PowershellExecutionException;
import io.entgra.device.mgt.core.cea.mgt.enforce.exception.UnsupportedOsException;
import io.entgra.device.mgt.core.cea.mgt.enforce.service.gateway.GatewayService;
import io.entgra.device.mgt.core.cea.mgt.enforce.util.DeviceMgtUtil;
import io.entgra.device.mgt.core.cea.mgt.enforce.util.annotation.Enforce;
import io.entgra.device.mgt.core.cea.mgt.enforce.util.shell.Powershell;
import io.entgra.device.mgt.core.cea.mgt.enforce.util.shell.parser.Parser;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.UserStoreException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ExchangeOnlineCEAEnforcementServiceImpl implements CEAEnforcementService {
    private static final Log log = LogFactory.getLog(ExchangeOnlineCEAEnforcementServiceImpl.class);
    private static volatile ExchangeOnlineCEAEnforcementServiceImpl INSTANCE;
    private final GatewayService gatewayService;
    private final Powershell powershell;

    ExchangeOnlineCEAEnforcementServiceImpl() throws UnsupportedOsException {
        gatewayService = new ExchangeOnlineGatewayServiceImpl();
        powershell = Powershell.getPowershell();
    }

    public static ExchangeOnlineCEAEnforcementServiceImpl getInstance() throws UnsupportedOsException {
        if (INSTANCE == null) {
            synchronized (ExchangeOnlineCEAEnforcementServiceImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ExchangeOnlineCEAEnforcementServiceImpl();
                }
            }
        }
        return INSTANCE;
    }

    @Enforce
    public void enforceDefaultAccessPolicy(CEAPolicy ceaPolicy) throws CEAEnforcementException {
        try {
            PowershellCommand setActiveSyncOrganizationSettings = getCommand(Parser.
                    COMMAND_SetActiveSyncOrganizationSettings.COMMAND, ceaPolicy.getActiveSyncServer());
            setActiveSyncOrganizationSettings.addOption(Parser.COMMAND_SetActiveSyncOrganizationSettings.
                            PARAMETER_DefaultAccessLevel,
                    Parser.COMMAND_SetActiveSyncOrganizationSettings.POLICY_TO_VALUE.
                            get(ceaPolicy.getAccessPolicy().getDefaultAccessPolicy().toString()));
            PowershellResponse powershellResponse = powershell.execute(getPowershellRequest(setActiveSyncOrganizationSettings));
            if (powershellResponse.isSuccess()) {
                log.info("Default access policy successfully enforced for " + ceaPolicy.getTenantId());
            } else {
                log.error("Default access policy enforcement procedure failed for " + ceaPolicy.getTenantId());
            }
        } catch (GatewayServiceException e) {
            String msg = "Active sync gateway service failed while enforcing default CEA access policy";
            log.error(msg, e);
            throw new CEAEnforcementException(msg, e);
        } catch (PowershellExecutionException e) {
            String msg = "Error occurred while executing powershell command for enforcing " +
                    "CEA access policy";
            log.error(msg, e);
            throw new CEAEnforcementException(msg, e);
        }
    }

    @Enforce
    public void enforceEmailOutlookAccessPolicy(CEAPolicy ceaPolicy) throws CEAEnforcementException {
        Set<EmailOutlookAccessPolicy> emailOutlookAccessPolicies = ceaPolicy.getAccessPolicy().getEmailOutlookAccessPolicy();
        if (emailOutlookAccessPolicies.contains(EmailOutlookAccessPolicy.NOT_CONFIGURED)) {
            if (log.isDebugEnabled()) {
                log.debug("CEA email outlook policy not configured, but the support is available in " +
                        ExchangeOnlineCEAEnforcementServiceImpl.class);
            }
            return;
        }
        ActiveSyncServer activeSyncServer = ceaPolicy.getActiveSyncServer();
        try {
            PowershellCommand setCASMailbox = getCommand(Parser.COMMAND_SetCASMailbox.COMMAND,
                    activeSyncServer);
            setCASMailbox.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_Identity, "$_.Identity");
            setCASMailbox.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_OutlookMobileEnabled, Parser.TRUE);
            setCASMailbox.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_MacOutlookEnabled, Parser.TRUE);
            setCASMailbox.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_OneWinNativeOutlookEnabled, Parser.TRUE);
            setCASMailbox.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_EwsAllowMacOutlook, Parser.TRUE);

            if (emailOutlookAccessPolicies.contains(EmailOutlookAccessPolicy.MOBILE_OUTLOOK_BLOCK)) {
                setCASMailbox.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_OutlookMobileEnabled,
                        Parser.COMMAND_SetCASMailbox.POLICY_TO_VALUE.get(EmailOutlookAccessPolicy.MOBILE_OUTLOOK_BLOCK.toString()));
            }

            if (emailOutlookAccessPolicies.contains(EmailOutlookAccessPolicy.MAC_OUTLOOK_BLOCK)) {
                setCASMailbox.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_MacOutlookEnabled,
                        Parser.COMMAND_SetCASMailbox.POLICY_TO_VALUE.get(EmailOutlookAccessPolicy.MAC_OUTLOOK_BLOCK.toString()));
            }

            if (emailOutlookAccessPolicies.contains(EmailOutlookAccessPolicy.WINDOWS_OUTLOOK_BLOCK)) {
                setCASMailbox.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_OneWinNativeOutlookEnabled,
                        Parser.COMMAND_SetCASMailbox.POLICY_TO_VALUE.get(EmailOutlookAccessPolicy.WINDOWS_OUTLOOK_BLOCK.toString()));
                setCASMailbox.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_EwsAllowMacOutlook,
                        Parser.COMMAND_SetCASMailbox.POLICY_TO_VALUE.get(EmailOutlookAccessPolicy.MAC_OLD_OUTLOOK_BLOCK.toString()));
            }

            PowershellResponse powershellResponse = powershell.execute(getPowershellRequest(
                    toAllMailboxesCommand(setCASMailbox, activeSyncServer)));
            if (powershellResponse.isSuccess()) {
                log.info("Email outlook access policy successfully enforced for " + ceaPolicy.getTenantId());
            } else {
                log.error("Email outlook access policy enforcement procedure failed for " + ceaPolicy.getTenantId());
            }
        } catch (GatewayServiceException e) {
            String msg = "Active sync auth service failed while enforcing default " +
                    "CEA email outlook access policy";
            log.error(msg, e);
            throw new CEAEnforcementException(msg, e);
        } catch (PowershellExecutionException e) {
            String msg = "Error occurred while executing powershell command for enforcing " +
                    "CEA email outlook access policy";
            log.error(msg, e);
            throw new CEAEnforcementException(msg, e);
        }
    }

    @Enforce
    public void enforcePOPIMAPAccessPolicy(CEAPolicy ceaPolicy) throws CEAEnforcementException {
        if (ceaPolicy.getAccessPolicy().getPOPIMAPAccessPolicy().
                equalsName(EmailOutlookAccessPolicy.NOT_CONFIGURED.name())) {
            if (log.isDebugEnabled()) {
                log.debug("CEA POP/IMAP policy not configured, but support is available in " +
                        ExchangeOnlineCEAEnforcementServiceImpl.class);
            }
            return;
        }
        ActiveSyncServer activeSyncServer = ceaPolicy.getActiveSyncServer();
        try {
            PowershellCommand setCASMailbox = getCommand(Parser.COMMAND_SetCASMailbox.COMMAND,
                    activeSyncServer);
            String POPIMAPPolicy = ceaPolicy.getAccessPolicy().getPOPIMAPAccessPolicy().toString();
            setCASMailbox.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_Identity, "$_.Identity");
            setCASMailbox.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_ImapEnabled,
                    Parser.COMMAND_SetCASMailbox.POLICY_TO_VALUE.get(POPIMAPPolicy));
            setCASMailbox.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_PopEnabled,
                    Parser.COMMAND_SetCASMailbox.POLICY_TO_VALUE.get(POPIMAPPolicy));
            PowershellResponse powershellResponse = powershell.execute(getPowershellRequest(
                    toAllMailboxesCommand(setCASMailbox, activeSyncServer)));
            if (powershellResponse.isSuccess()) {
                log.info("POP/IMAP access policy successfully enforced for " + ceaPolicy.getTenantId());
            } else {
                log.error("POP/IMAP access policy enforcement procedure failed for " + ceaPolicy.getTenantId());
            }
        } catch (GatewayServiceException e) {
            String msg = "Active sync auth service failed while enforcing default CEA POP/IMAP policy";
            log.error(msg, e);
            throw new CEAEnforcementException(msg, e);
        } catch (PowershellExecutionException e) {
            String msg = "Error occurred while executing powershell command for enforcing " +
                    "CEA POP/IMAP policy";
            log.error(msg, e);
            throw new CEAEnforcementException(msg, e);
        }
    }

    @Enforce
    public void enforceWebOutlookAccessPolicy(CEAPolicy ceaPolicy) throws CEAEnforcementException {
        if (ceaPolicy.getAccessPolicy().getWebOutlookAccessPolicy().
                equalsName(WebOutlookAccessPolicy.NOT_CONFIGURED.name())) {
            if (log.isDebugEnabled()) {
                log.debug("CEA Outlook web access policy not configured, but support is available in " +
                        ExchangeOnlineCEAEnforcementServiceImpl.class);
            }
            return;
        }
        ActiveSyncServer activeSyncServer = ceaPolicy.getActiveSyncServer();
        try {
            PowershellCommand setCASMailbox = getCommand(Parser.COMMAND_SetCASMailbox.COMMAND,
                    activeSyncServer);
            setCASMailbox.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_Identity, "$_.Identity");
            setCASMailbox.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_OWAEnabled,
                    Parser.COMMAND_SetCASMailbox.POLICY_TO_VALUE.get(ceaPolicy.getAccessPolicy().
                            getWebOutlookAccessPolicy().toString()));
            PowershellResponse powershellResponse = powershell.execute(getPowershellRequest(
                    toAllMailboxesCommand(setCASMailbox, activeSyncServer)));
            if (powershellResponse.isSuccess()) {
                log.info("Web outlook access policy successfully enforced for " + ceaPolicy.getTenantId());
            } else {
                log.error("Web outlook access policy enforcement procedure failed for " + ceaPolicy.getTenantId());
            }
        } catch (GatewayServiceException e) {
            String msg = "Active sync auth service failed while enforcing CEA web outlook access policy";
            log.error(msg, e);
            throw new CEAEnforcementException(msg, e);
        } catch (PowershellExecutionException e) {
            String msg = "Error occurred while executing powershell command for enforcing " +
                    "CEA web outlook access policy";
            log.error(msg, e);
            throw new CEAEnforcementException(msg, e);
        }
    }

    @Enforce
    public void enforceConditionalAccessPolicy(CEAPolicy ceaPolicy) throws CEAEnforcementException {
        GracePeriod gracePeriod = ceaPolicy.getGracePeriod();
        ActiveSyncServer activeSyncServer = ceaPolicy.getActiveSyncServer();
        boolean isSynced = ceaPolicy.isSynced();
        Date created = ceaPolicy.getCreated();
        Date lastSynced = ceaPolicy.getLastSynced();

        /*
        * Here we are filtering the devices(active sync devices) which are communicating
        * with the exchange online server into valid and not valid categories.
        * Valid category can contain devices which are currently managed by UEM or devices
        * which are syncing with the exchange online server under a grace period.
        * */

        try {
            /* Get the devices based on the last sync timestamp or cea policy created
            * time to avoid unnecessary device bulks.
            * */
            List<ActiveSyncDevice> validActiveSyncDevices = isSynced ? DeviceMgtUtil.
                    getEnrolledActiveSyncDevices(lastSynced, false) :
                    DeviceMgtUtil.getEnrolledActiveSyncDevices(new Date(), true);
            List<ActiveSyncDevice> notValidActiveSyncDevices = new ArrayList<>();

            List<ActiveSyncDevice> connectedActiveSyncDevices = isSynced ?
                    getConnectedActiveSyncDevicesAfter(lastSynced, activeSyncServer) :
                    getAllConnectedActiveSyncDevices(activeSyncServer);
            for (ActiveSyncDevice activeSyncDevice : connectedActiveSyncDevices) {
                if (!EASMgtUtil.isManageByUEM(activeSyncDevice.getDeviceId())
                        && !validActiveSyncDevices.contains(activeSyncDevice)) {
                    notValidActiveSyncDevices.add(activeSyncDevice);
                } else {
                    validActiveSyncDevices.add(activeSyncDevice);
                }
            }

            if (gracePeriod.getGraceAllowedPolicy().equalsName(GraceAllowedPolicy.NOT_ALLOWED.name())) {
                // Block grace offered new devices if exists
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, -Constants.MAX_GRACE_PERIOD_IN_DAYS);
                List<ActiveSyncDevice> graceExceededNewlyConnectedActiveSyncDevices =
                        getConnectedActiveSyncDevicesAfter(calendar.getTime(), activeSyncServer);
                List<ActiveSyncDevice> managedDevices = DeviceMgtUtil.getEnrolledActiveSyncDevices(calendar.getTime(), false);
                categorizeDevices(validActiveSyncDevices, notValidActiveSyncDevices,
                        graceExceededNewlyConnectedActiveSyncDevices, managedDevices, gracePeriod, false);

                // Block grace offered existing devices if exists
                List<ActiveSyncDevice> connectedActiveSyncDevicesBeforeTheCreationOfCEAPolicy =
                        getConnectedActiveSyncDevicesBefore(created, activeSyncServer);
                categorizeDevices(validActiveSyncDevices, notValidActiveSyncDevices,
                        connectedActiveSyncDevicesBeforeTheCreationOfCEAPolicy, validActiveSyncDevices, gracePeriod, false);
            }

            if (gracePeriod.getGraceAllowedPolicy().equalsName(GraceAllowedPolicy.NEW_AND_EXISTING.name()) ||
                    gracePeriod.getGraceAllowedPolicy().equalsName(GraceAllowedPolicy.NEW_ONLY.name())) {

                List<ActiveSyncDevice> newlyConnectedActiveSyncDevices =
                        getConnectedActiveSyncDevicesAfter(isSynced ? lastSynced : created, activeSyncServer);
                categorizeDevices(validActiveSyncDevices, notValidActiveSyncDevices,
                        newlyConnectedActiveSyncDevices, validActiveSyncDevices, gracePeriod, true);

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, -Constants.MAX_GRACE_PERIOD_IN_DAYS);
                List<ActiveSyncDevice> graceExceededNewlyConnectedActiveSyncDevices =
                        getConnectedActiveSyncDevicesAfter(calendar.getTime(), activeSyncServer);
                List<ActiveSyncDevice> managedDevices = DeviceMgtUtil.getEnrolledActiveSyncDevices(calendar.getTime(), false);
                categorizeDevices(validActiveSyncDevices, notValidActiveSyncDevices,
                        graceExceededNewlyConnectedActiveSyncDevices, managedDevices, gracePeriod, true);
            }

            if (gracePeriod.getGraceAllowedPolicy().equalsName(GraceAllowedPolicy.NEW_AND_EXISTING.name()) ||
                    gracePeriod.getGraceAllowedPolicy().equalsName(GraceAllowedPolicy.EXISTING_ONLY.name())) {
                List<ActiveSyncDevice> connectedActiveSyncDevicesBeforeTheCreationOfCEAPolicy =
                        getConnectedActiveSyncDevicesBefore(created, activeSyncServer);
                categorizeDevices(validActiveSyncDevices, notValidActiveSyncDevices,
                        connectedActiveSyncDevicesBeforeTheCreationOfCEAPolicy, validActiveSyncDevices, gracePeriod, true);
            }

            List<MailboxProfile> mailboxProfiles = generateMailboxProfiles(validActiveSyncDevices,
                    notValidActiveSyncDevices);
            for (MailboxProfile mailboxProfile : mailboxProfiles) {
                PowershellCommand powershellCommand = getCommand(Parser.COMMAND_SetCASMailbox.COMMAND, activeSyncServer);
                powershellCommand.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_Identity, mailboxProfile.getIdentity());
                powershellCommand.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_ActiveSyncAllowedDeviceIDs,
                        mailboxProfile.getAllowedEASIdentifierString());
                powershellCommand.addOption(Parser.COMMAND_SetCASMailbox.PARAMETER_ActiveSyncBlockedDeviceIDs,
                        mailboxProfile.getBlockedEASIdentifierString());
                powershell.execute(getPowershellRequest(powershellCommand));
            }
        } catch (GatewayServiceException e) {
            String msg = "Active sync auth service failed while enforcing CEA policy";
            log.error(msg, e);
            throw new CEAEnforcementException(msg, e);
        } catch (PowershellExecutionException e) {
            String msg = "Error occurred while executing powershell command for enforcing CEA policy";
            log.error(msg, e);
            throw new CEAEnforcementException(msg, e);
        } catch (DeviceManagementException | UserStoreException e) {
            String msg = "Error occurred while retrieving active sync devices";
            log.error(msg, e);
            throw new CEAEnforcementException(msg, e);
        }
    }

    /**
     * Categorize active sync devices into valid and not valid
     * @param validActiveSyncDevices Valid active sync devices
     * @param notValidActiveSyncDevices Not valid active sync devices
     * @param deviceList Device list to filter
     * @param managedList Already managing devices from UEM
     * @param gracePeriod Grace period to consider
     * @param allowGrace Whether to allow grace or not
     */
    private void categorizeDevices(List<ActiveSyncDevice> validActiveSyncDevices, List<ActiveSyncDevice> notValidActiveSyncDevices,
            List<ActiveSyncDevice> deviceList, List<ActiveSyncDevice> managedList, GracePeriod gracePeriod, boolean allowGrace) {
        for (ActiveSyncDevice activeSyncDevice : deviceList) {
            if (!EASMgtUtil.isManageByUEM(activeSyncDevice.getDeviceId())
                    && !managedList.contains(activeSyncDevice)) {
                if (allowGrace) {
                    filterDeviceBasedOnGrace(activeSyncDevice, validActiveSyncDevices, notValidActiveSyncDevices, gracePeriod);
                } else {
                    validActiveSyncDevices.remove(activeSyncDevice);
                    notValidActiveSyncDevices.add(activeSyncDevice);
                }
            } else {
                // These devices are managed by UEM, so add to the valid category
                notValidActiveSyncDevices.remove(activeSyncDevice);
                validActiveSyncDevices.add(activeSyncDevice);
            }
        }
    }

    /**
     * Filter active sync device based on grace period
     * @param activeSyncDevice Active sync device
     * @param validActiveSyncDevices Valid active sync device list
     * @param notValidActiveSyncDevices Not valid active sync device list
     * @param gracePeriod Grace period to consider
     */
    private void filterDeviceBasedOnGrace(ActiveSyncDevice activeSyncDevice, List<ActiveSyncDevice> validActiveSyncDevices,
            List<ActiveSyncDevice> notValidActiveSyncDevices, GracePeriod gracePeriod) {
        long timeDiff = Math.abs(new Date().getTime() - activeSyncDevice.getFirstSyncTime().getTime());
        // Enforce the grace period if the device not exceeds the grace limit
        if (TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS) < gracePeriod.getGracePeriod()) {
            notValidActiveSyncDevices.remove(activeSyncDevice);
            validActiveSyncDevices.add(activeSyncDevice);
        } else {
            validActiveSyncDevices.remove(activeSyncDevice);
            notValidActiveSyncDevices.add(activeSyncDevice);
        }
    }

    /**
     * Generate powershell command {@link PowershellCommand} from command string
     * @param command Powershell command string
     * @param activeSyncServer {@link ActiveSyncServer}
     * @return {@link PowershellCommand}
     * @throws GatewayServiceException Throws when error occurred while retrieving access token
     */
    private PowershellCommand getCommand(String command, ActiveSyncServer activeSyncServer)
            throws GatewayServiceException {
        String[] urlParts = activeSyncServer.getGatewayUrl().split("/");
        ExoPowershellCommand.ExoPowershellCommandBuilder commandBuilder =
                new ExoPowershellCommand.ExoPowershellCommandBuilder(command);
        commandBuilder.accessToken(gatewayService.acquireAccessToken(activeSyncServer))
                .organization(urlParts[urlParts.length - 1]);
        return commandBuilder.build();
    }

    /**
     * Wrap powershell command to effect all mailboxes in active sync server
     * @param command {@link PowershellCommand} command to wrap
     * @param activeSyncServer {@link ActiveSyncServer}
     * @return {@link PowershellCommand}
     * @throws GatewayServiceException Throws when error occurred while retrieving access token
     */
    private PowershellCommand toAllMailboxesCommand(PowershellCommand command,
                                                    ActiveSyncServer activeSyncServer) throws GatewayServiceException {
        PowershellCommand getEXOMailbox = getCommand(Parser.COMMAND_GetEXOMailbox.COMMAND, activeSyncServer);
        getEXOMailbox.addOption(Parser.COMMAND_GetEXOMailbox.PARAMETER_ResultSize, "unlimited");
        PowershellCommand forEach = getCommand(Parser.COMMAND_ForEach.COMMAND, activeSyncServer);
        forEach.addOption(Parser.COMMAND_ForEach.PARAMETER_Begin, "$upn = $_.UserPrincipalName;" + command.constructFullCommand());
        forEach.addOption(Parser.COMMAND_ForEach.PARAMETER_End, "");
        getEXOMailbox.pipe(forEach);
        getEXOMailbox.setConvertToJson(false);
        return getEXOMailbox;
    }

    /**
     * Create new powershell request to execute via powershell binaries
     * @param command {@link PowershellCommand}
     * @return {@link PowershellRequest}
     */
    private PowershellRequest getPowershellRequest(PowershellCommand command) {
        PowershellRequest powershellRequest = new PowershellRequest();
        powershellRequest.setCommand(command);
        return powershellRequest;
    }

    /**
     * Generate mailbox profiles from active sync block and allowed devices
     * @param activeSyncAllowedDevices Active sync allowed device list
     * @param activeSyncBlockedDevices Active sync blocked device list
     * @return List of {@link MailboxProfile}
     */
    private List<MailboxProfile> generateMailboxProfiles(List<ActiveSyncDevice> activeSyncAllowedDevices,
                                                         List<ActiveSyncDevice> activeSyncBlockedDevices) {
        List<MailboxProfile> mailboxProfiles = new ArrayList<>();
        MailboxProfile mailboxProfile;
        for (ActiveSyncDevice activeSyncDevice : activeSyncAllowedDevices) {
            mailboxProfile = new MailboxProfile();
            mailboxProfile.setIdentity(activeSyncDevice.getUserPrincipalName());
            if (mailboxProfiles.contains(mailboxProfile)) {
                MailboxProfile existingMailboxProfile = mailboxProfiles.get(mailboxProfiles.indexOf(mailboxProfile));
                existingMailboxProfile.addActiveSyncAllowedEASIdentifier(activeSyncDevice.getDeviceId());
            } else {
                mailboxProfile.addActiveSyncAllowedEASIdentifier(activeSyncDevice.getDeviceId());
                mailboxProfiles.add(mailboxProfile);
            }
        }

        for (ActiveSyncDevice activeSyncDevice : activeSyncBlockedDevices) {
            mailboxProfile = new MailboxProfile();
            mailboxProfile.setIdentity(activeSyncDevice.getUserPrincipalName());
            if (mailboxProfiles.contains(mailboxProfile)) {
                MailboxProfile existingMailboxProfile = mailboxProfiles.get(mailboxProfiles.indexOf(mailboxProfile));
                existingMailboxProfile.addActiveSyncBlockEASIdentifier(activeSyncDevice.getDeviceId());
            } else {
                mailboxProfile.addActiveSyncBlockEASIdentifier(activeSyncDevice.getDeviceId());
                mailboxProfiles.add(mailboxProfile);
            }
        }
        return mailboxProfiles;
    }

    /**
     * Construct active sync device list from powershell response
     * @param powershellResponse Shell response return from powershell binary
     * @return List of {@link ActiveSyncDevice}
     * @throws CEAEnforcementException Throws when error occurred while generating the device list
     */
    private List<ActiveSyncDevice> constructActiveSyncDeviceList(PowershellResponse powershellResponse)
            throws CEAEnforcementException {
        if (powershellResponse == null) {
            throw new CEAEnforcementException("Powershell response can't be null");
        }

        if (!powershellResponse.isSuccess()) {
            throw new CEAEnforcementException("Powershell request failed while getting active sync devices");
        }

        if (powershellResponse.getResponseBody() == null) {
            return Collections.emptyList();
        }

        if (!powershellResponse.getResponseBody().isJsonArray()) {
            throw new CEAEnforcementException("Unexpected result retrieve when getting active sync devices");
        }

        SimpleDateFormat powershellDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        List<ActiveSyncDevice> activeSyncDevices = new ArrayList<>();

        JsonArray elements = powershellResponse.getResponseBody().getAsJsonArray();
        JsonObject deviceJsonObject;
        ActiveSyncDevice activeSyncDevice;
        for (JsonElement element : elements) {
            try {
                deviceJsonObject = element.getAsJsonObject();
                activeSyncDevice = new ActiveSyncDevice();
                activeSyncDevice.setUserPrincipalName(deviceJsonObject.get("UserPrincipalName").getAsString());
                activeSyncDevice.setDeviceId(deviceJsonObject.get("DeviceID").getAsString());
                activeSyncDevice.setIdentity(deviceJsonObject.get("Identity").getAsString());
                activeSyncDevice.setFirstSyncTime(powershellDateFormat.parse(deviceJsonObject.get("FirstSyncTime").getAsString()));
                activeSyncDevices.add(activeSyncDevice);
            } catch (ParseException e) {
                throw new CEAEnforcementException("Error occurred while parsing active sync device json element");
            }
        }
        return activeSyncDevices;
    }

    /**
     * Get active sync devices, which are connected with active sync server after a certain timestamp
     * @param after Timestamp to retrieve connected devices
     * @param activeSyncServer {@link ActiveSyncServer}
     * @return List of {@link ActiveSyncDevice}
     * @throws GatewayServiceException Throws when error occurred while retrieving access token
     * @throws PowershellExecutionException Throws when error occurred while executing the powershell command
     * @throws CEAEnforcementException Throws when error occurred while constructing device list
     */
    private List<ActiveSyncDevice> getConnectedActiveSyncDevicesAfter(Date after, ActiveSyncServer activeSyncServer)
            throws GatewayServiceException, PowershellExecutionException, CEAEnforcementException {
        SimpleDateFormat powershellDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        PowershellCommand getEXOMobileDeviceStatistics = getCommand(Parser.COMMAND_GetEXOMobileDeviceStatistics.COMMAND,
                activeSyncServer);
        getEXOMobileDeviceStatistics.addOption(Parser.COMMAND_GetEXOMobileDeviceStatistics.PARAMETER_ActiveSync, "");
        getEXOMobileDeviceStatistics.addOption(
                Parser.COMMAND_GetEXOMobileDeviceStatistics.PARAMETER_Mailbox, "$_.Identity");

        PowershellCommand selectObject = getCommand(Parser.COMMAND_SelectObject.COMMAND, activeSyncServer);
        selectObject.addOption("@{label='UserPrincipalName' ; expression={$upn}},FirstSyncTime, DeviceID, Identity", "");

        PowershellCommand whereObject = getCommand(Parser.COMMAND_WhereObject.COMMAND, activeSyncServer);
        whereObject.addOption(Parser.COMMAND_WhereObject.PARAMETER_Begin, "$_.FirstSyncTime -gt "
                + "'" + powershellDateFormat.format(after) + "'");
        whereObject.addOption(Parser.COMMAND_WhereObject.PARAMETER_End, "");

        PowershellCommand convertToJson = getCommand(Parser.COMMAND_ConvertToJson.COMMAND, activeSyncServer);
        convertToJson.addOption(Parser.COMMAND_ConvertToJson.PARAMETER_AsArray, "");
        getEXOMobileDeviceStatistics.pipe(selectObject).pipe(whereObject).pipe(convertToJson);

        PowershellCommand toAllMailboxes = toAllMailboxesCommand(getEXOMobileDeviceStatistics, activeSyncServer);
        PowershellRequest powershellRequest = getPowershellRequest(toAllMailboxes);
        PowershellResponse powershellResponse = powershell.execute(powershellRequest);
        return constructActiveSyncDeviceList(powershellResponse);
    }

    /**
     * Get active sync devices, which are connected with active sync server before a certain timestamp
     * @param before Timestamp to retrieve connected devices
     * @param activeSyncServer {@link ActiveSyncServer}
     * @return List of {@link ActiveSyncDevice}
     * @throws GatewayServiceException Throws when error occurred while retrieving access token
     * @throws PowershellExecutionException Throws when error occurred while executing the powershell command
     * @throws CEAEnforcementException Throws when error occurred while constructing device list
     */
    private List<ActiveSyncDevice> getConnectedActiveSyncDevicesBefore(Date before, ActiveSyncServer activeSyncServer)
            throws GatewayServiceException, PowershellExecutionException, CEAEnforcementException {
        SimpleDateFormat powershellDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        PowershellCommand getEXOMobileDeviceStatistics = getCommand(Parser.COMMAND_GetEXOMobileDeviceStatistics.COMMAND,
                activeSyncServer);
        getEXOMobileDeviceStatistics.addOption(Parser.COMMAND_GetEXOMobileDeviceStatistics.PARAMETER_ActiveSync, "");
        getEXOMobileDeviceStatistics.addOption(
                Parser.COMMAND_GetEXOMobileDeviceStatistics.PARAMETER_Mailbox, "$_.Identity");

        PowershellCommand selectObject = getCommand(Parser.COMMAND_SelectObject.COMMAND, activeSyncServer);
        selectObject.addOption("@{label='UserPrincipalName' ; expression={$upn}},FirstSyncTime, DeviceID, Identity", "");

        PowershellCommand whereObject = getCommand(Parser.COMMAND_WhereObject.COMMAND, activeSyncServer);
        whereObject.addOption(Parser.COMMAND_WhereObject.PARAMETER_Begin, "$_.FirstSyncTime -lt "
                + "'" + powershellDateFormat.format(before) + "'");
        whereObject.addOption(Parser.COMMAND_WhereObject.PARAMETER_End, "");

        PowershellCommand convertToJson = getCommand(Parser.COMMAND_ConvertToJson.COMMAND, activeSyncServer);
        convertToJson.addOption(Parser.COMMAND_ConvertToJson.PARAMETER_AsArray, "");
        getEXOMobileDeviceStatistics.pipe(selectObject).pipe(whereObject).pipe(convertToJson);

        PowershellCommand toAllMailboxes = toAllMailboxesCommand(getEXOMobileDeviceStatistics, activeSyncServer);
        PowershellRequest powershellRequest = getPowershellRequest(toAllMailboxes);
        PowershellResponse powershellResponse = powershell.execute(powershellRequest);
        return constructActiveSyncDeviceList(powershellResponse);
    }

    /**
     * Get all connected active sync devices from active sync server
     * @param activeSyncServer {@link ActiveSyncServer}
     * @return List of {@link ActiveSyncDevice}
     * @throws GatewayServiceException Throws when error occurred while retrieving access token
     * @throws PowershellExecutionException Throws when error occurred while executing the powershell command
     * @throws CEAEnforcementException Throws when error occurred while constructing device list
     */
    private List<ActiveSyncDevice> getAllConnectedActiveSyncDevices(ActiveSyncServer activeSyncServer)
            throws GatewayServiceException, PowershellExecutionException, CEAEnforcementException {
        PowershellCommand getEXOMobileDeviceStatistics = getCommand(Parser.COMMAND_GetEXOMobileDeviceStatistics.COMMAND,
                activeSyncServer);
        getEXOMobileDeviceStatistics.addOption(Parser.COMMAND_GetEXOMobileDeviceStatistics.PARAMETER_ActiveSync, "");
        getEXOMobileDeviceStatistics.addOption(
                Parser.COMMAND_GetEXOMobileDeviceStatistics.PARAMETER_Mailbox, "$_.Identity");

        PowershellCommand convertToJson = getCommand(Parser.COMMAND_ConvertToJson.COMMAND, activeSyncServer);
        convertToJson.addOption(Parser.COMMAND_ConvertToJson.PARAMETER_AsArray, "");

        PowershellCommand selectObject = getCommand(Parser.COMMAND_SelectObject.COMMAND, activeSyncServer);
        selectObject.addOption("@{label='UserPrincipalName' ; expression={$upn}},FirstSyncTime, DeviceID, Identity", "");

        getEXOMobileDeviceStatistics.pipe(selectObject).pipe(convertToJson);

        PowershellCommand toAllMailboxes = toAllMailboxesCommand(getEXOMobileDeviceStatistics, activeSyncServer);
        PowershellRequest powershellRequest = getPowershellRequest(toAllMailboxes);
        PowershellResponse powershellResponse = powershell.execute(powershellRequest);
        return constructActiveSyncDeviceList(powershellResponse);
    }
}
