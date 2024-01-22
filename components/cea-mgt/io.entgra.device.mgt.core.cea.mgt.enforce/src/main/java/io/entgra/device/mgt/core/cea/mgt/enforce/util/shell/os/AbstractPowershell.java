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

package io.entgra.device.mgt.core.cea.mgt.enforce.util.shell.os;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.entgra.device.mgt.core.cea.mgt.enforce.bean.PowershellRequest;
import io.entgra.device.mgt.core.cea.mgt.enforce.bean.PowershellResponse;
import io.entgra.device.mgt.core.cea.mgt.enforce.exception.PowershellExecutionException;
import io.entgra.device.mgt.core.cea.mgt.enforce.util.shell.Powershell;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

public class AbstractPowershell implements Powershell {
    private static final Log log = LogFactory.getLog(AbstractPowershell.class);
    protected static final String SYMBOL_SPLITTER = "&";
    private static final String PARAMETER_COMMAND = "-Command";
    private static final String COMMAND_REDIRECT_WARNINGS = "$WarningPreference = 'SilentlyContinue';";
    private final String BINARY;

    AbstractPowershell(String BINARY) {
        this.BINARY = BINARY;
    }

    @Override
    public PowershellResponse execute(PowershellRequest powershellRequest) throws PowershellExecutionException {

        String commandString = String.join(SYMBOL_SPLITTER, Arrays.asList(BINARY, PARAMETER_COMMAND, COMMAND_REDIRECT_WARNINGS,
                powershellRequest.getCommand().getCommandString()));
        ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(commandString.split(SYMBOL_SPLITTER)));
        StringWriter standardOutputStringWriter = new StringWriter();
        PrintWriter standardOutputPrintWriter = new PrintWriter(standardOutputStringWriter);
        StringWriter errorStringWriter = new StringWriter();
        PrintWriter errorPrintWriter = new PrintWriter(errorStringWriter);
        try {
            Process process = processBuilder.start();
            List<Thread> streamConsumerThreads = Arrays.asList(
                    new Thread(new ThreadedStreamConsumer(process.getInputStream(), standardOutputPrintWriter)),
                    new Thread(new ThreadedStreamConsumer(process.getErrorStream(), errorPrintWriter))
            );

            for (Thread streamConsumerThread : streamConsumerThreads) {
                streamConsumerThread.start();
            }
            int exitCode = process.waitFor();

            for (Thread streamConsumerThread : streamConsumerThreads) {
                streamConsumerThread.join();
            }
            return constructResponse(exitCode, getStringContent(standardOutputStringWriter),
                    getStringContent(errorStringWriter));
        } catch (IOException e) {
            String msg = "IOException occurred while executing powershell command : "
                    + powershellRequest.getCommand();
            log.error(msg, e);
            throw new PowershellExecutionException(msg, e);
        } catch (InterruptedException e) {
            String msg = "Thread got interrupted while executing powershell command : "
                    + powershellRequest.getCommand();
            log.error(msg, e);
            throw new PowershellExecutionException(msg, e);
        }
    }

    private PowershellResponse constructResponse(int exitCode, String standardOutput, String errorOutput) {
        JsonElement standardOutputJson = new Gson().fromJson(standardOutput, JsonElement.class);
        return new PowershellResponse(standardOutputJson,
                errorOutput, exitCode, exitCode == 0);
    }

    private String getStringContent(StringWriter stringWriter) {
        return stringWriter.getBuffer().toString().trim();
    }

    private static class ThreadedStreamConsumer implements Runnable {
        private final InputStream inputStream;
        private final PrintWriter printWriter;

        public ThreadedStreamConsumer(InputStream inputStream, PrintWriter printWriter) {
            this.inputStream = inputStream;
            this.printWriter = printWriter;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).
                    lines().forEach(printWriter::println);
        }
    }
}
