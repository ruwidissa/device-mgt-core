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

package io.entgra.device.mgt.core.cea.mgt.enforce.util.shell.parser;

import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.DefaultAccessPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.EmailOutlookAccessPolicy;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parser {
    public static final String TRUE = "$true";
    public static final String FALSE = "$false";
    public static final String ALLOW = "ALLOW";
    public static final String BLOCK = "BLOCK";

    public static class COMMAND_SetActiveSyncOrganizationSettings {
        public static final String COMMAND = "Set-ActiveSyncOrganizationSettings";
        public static final String PARAMETER_DefaultAccessLevel = "-DefaultAccessLevel";
        public static final Map<String, String> POLICY_TO_VALUE = Stream.of(new String[][]{
                {DefaultAccessPolicy.ALLOW.toString(), "Allow"},
                {DefaultAccessPolicy.BLOCK.toString(), "Block"},
                {DefaultAccessPolicy.QUARANTINE.toString(), "Quarantine"}
        }).collect(Collectors.collectingAndThen(
                Collectors.toMap(entry -> entry[0], entry -> entry[1]),
                Collections::<String, String>unmodifiableMap
        ));
    }

    public static class COMMAND_SetCASMailbox {
        public static final String COMMAND = "Set-CASMailbox";
        public static final String PARAMETER_Identity = "-Identity";
        public static final String PARAMETER_ActiveSyncAllowedDeviceIDs = "-ActiveSyncAllowedDeviceIDs";
        public static final String PARAMETER_ActiveSyncBlockedDeviceIDs = "-ActiveSyncBlockedDeviceIDs";
        public static final String PARAMETER_EwsAllowMacOutlook = "-EwsAllowMacOutlook";
        public static final String PARAMETER_MacOutlookEnabled = "-MacOutlookEnabled";
        public static final String PARAMETER_OneWinNativeOutlookEnabled = "-OneWinNativeOutlookEnabled";
        public static final String PARAMETER_OutlookMobileEnabled = "-OutlookMobileEnabled";
        public static final String PARAMETER_OWAEnabled = "-OWAEnabled";
        public static final String PARAMETER_ImapEnabled = "-ImapEnabled";
        public static final String PARAMETER_PopEnabled = "-PopEnabled";
        public static final Map<String, String> POLICY_TO_VALUE = Stream.of(new String[][]{
                {EmailOutlookAccessPolicy.MOBILE_OUTLOOK_BLOCK.toString(), FALSE},
                {EmailOutlookAccessPolicy.MAC_OUTLOOK_BLOCK.toString(), FALSE},
                {EmailOutlookAccessPolicy.WINDOWS_OUTLOOK_BLOCK.toString(), FALSE},
                {EmailOutlookAccessPolicy.MAC_OLD_OUTLOOK_BLOCK.toString(), FALSE},
                {ALLOW, TRUE},
                {BLOCK, FALSE}
        }).collect(Collectors.collectingAndThen(
                Collectors.toMap(entry -> entry[0], entry -> entry[1]),
                Collections::<String, String>unmodifiableMap
        ));
    }

    public static class COMMAND_GetEXOMailbox {
        public static final String COMMAND = "Get-EXOMailbox";
        public static final String PARAMETER_ResultSize = "-ResultSize";
    }

    public static class COMMAND_ForEach {
        public static final String COMMAND = "ForEach";
        public static final String PARAMETER_Begin = "{";
        public static final String PARAMETER_End = "}";
    }

    public static class COMMAND_WhereObject {
        public static final String COMMAND = "Where-Object";
        public static final String PARAMETER_Begin = "{";
        public static final String PARAMETER_End = "}";
    }

    public static class COMMAND_GetEXOMobileDeviceStatistics {
        public static final String COMMAND = "Get-EXOMobileDeviceStatistics";
        public static final String PARAMETER_ActiveSync = "-ActiveSync";
        public static final String PARAMETER_Mailbox = "-Mailbox";
    }

    public static class COMMAND_ConvertToJson {
        public static final String COMMAND = "ConvertTo-Json";
        public static final String PARAMETER_AsArray = "-AsArray";
    }

    public static class COMMAND_SelectObject {
        public static final String COMMAND = "Select-Object";
    }

    public static class COMMAND_SetVariable {
        public static final String COMMAND = "Set-Variable";
        public static final String PARAMETER_Name = "-Name";
        public static final String PARAMETER_Value = "-Value";
        public static final String PARAMETER_PassThrough = "-PassThru";
    }

}
