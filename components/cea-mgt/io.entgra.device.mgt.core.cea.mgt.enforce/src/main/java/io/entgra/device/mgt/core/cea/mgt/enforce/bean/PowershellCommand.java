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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PowershellCommand {
    protected static final String COMMAND_OUTPUT_NULL = "Out-Null;";
    protected static final String SYMBOL_PIPE = "|";
    protected static final String SYMBOL_SPLITTER = "&";
    protected static final String SYMBOL_END_LINE = ";";
    protected final String command;
    protected final Map<String, String> parameters = new HashMap<>();
    protected boolean isOutputNull = false;
    protected boolean convertToJson = true;
    protected PowershellCommand pipedCommand;

    public PowershellCommand(String command) {
        this.command = command;
    }

    public boolean isOutputNull() {
        return isOutputNull;
    }

    public void setOutputNull(boolean outputNull) {
        isOutputNull = outputNull;
    }

    public boolean isConvertToJson() {
        return convertToJson;
    }

    public void setConvertToJson(boolean convertToJson) {
        this.convertToJson = convertToJson;
    }

    public void addOption(String option, String value) {
        parameters.put(option, value);
    }

    protected String constructParameterString() {
        List<String> optionList = new ArrayList<>();
        for (String option : parameters.keySet()) {
            optionList.add(option + " " + parameters.get(option));
        }
        return String.join(" ", optionList);
    }

    public String constructFullCommand() {
        String fullCommand = command + " " + constructParameterString();
        return pipedCommand == null ? fullCommand :
                fullCommand + SYMBOL_SPLITTER + SYMBOL_PIPE + SYMBOL_SPLITTER + pipedCommand.constructFullCommand();
    }

    public PowershellCommand pipe(PowershellCommand command) {
        pipedCommand = command;
        return command;
    }

    public abstract String getCommandString();
}
