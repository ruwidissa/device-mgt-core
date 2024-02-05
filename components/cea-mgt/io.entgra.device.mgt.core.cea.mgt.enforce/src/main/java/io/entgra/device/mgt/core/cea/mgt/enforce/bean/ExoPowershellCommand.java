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

package io.entgra.device.mgt.core.cea.mgt.enforce.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExoPowershellCommand extends PowershellCommand {
    private static final String COMMAND_IMPORT_MODULE_EXO = "Import-Module ExchangeOnlineManagement" + SYMBOL_END_LINE;
    private static final String COMMAND_CONVERT_TO_JSON = "ConvertTo-Json";
    private final String accessToken;
    private final String organization;

    protected ExoPowershellCommand(String command, String accessToken, String organization) {
        super(command);
        this.accessToken = accessToken;
        this.organization = organization;
    }

    public String getCommandString() {
        List<String> partsOfCommand = new ArrayList<>(Arrays.asList(
                COMMAND_IMPORT_MODULE_EXO,
                constructConnectionCommand(),
                constructFullCommand(),
                SYMBOL_PIPE));

        if (isOutputNull) {
            partsOfCommand.add(COMMAND_OUTPUT_NULL);
        } else if (convertToJson) {
            partsOfCommand.add(COMMAND_CONVERT_TO_JSON);
        } else {
            partsOfCommand.remove(partsOfCommand.size() - 1);
        }

        return String.join(SYMBOL_SPLITTER, partsOfCommand);
    }

    private String constructConnectionCommand() {
        return "Connect-ExchangeOnline" +
                " -Organization " + organization +
                " -AccessToken " + accessToken + " -ShowBanner:$false" + SYMBOL_END_LINE;
    }

    public static class ExoPowershellCommandBuilder {
        private final String command;
        private String accessToken;
        private String organization;

        public ExoPowershellCommandBuilder(String command) {
            this.command = command;
        }

        public ExoPowershellCommandBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public ExoPowershellCommandBuilder organization(String organization) {
            this.organization = organization;
            return this;
        }

        public ExoPowershellCommand build() {
            return new ExoPowershellCommand(command, accessToken, organization);
        }
    }
}
