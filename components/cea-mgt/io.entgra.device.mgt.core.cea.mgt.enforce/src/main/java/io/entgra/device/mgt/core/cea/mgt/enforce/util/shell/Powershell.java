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

package io.entgra.device.mgt.core.cea.mgt.enforce.util.shell;

import io.entgra.device.mgt.core.cea.mgt.enforce.bean.PowershellRequest;
import io.entgra.device.mgt.core.cea.mgt.enforce.bean.PowershellResponse;
import io.entgra.device.mgt.core.cea.mgt.enforce.exception.PowershellExecutionException;
import io.entgra.device.mgt.core.cea.mgt.enforce.exception.UnsupportedOsException;
import io.entgra.device.mgt.core.cea.mgt.enforce.util.shell.os.LinuxPowershell;
import io.entgra.device.mgt.core.cea.mgt.enforce.util.shell.os.MacPowershell;
import io.entgra.device.mgt.core.cea.mgt.enforce.util.shell.os.WindowsPowershell;

public interface Powershell {
    String OS = System.getProperty("os.name").toLowerCase();
    boolean IS_UNIX = (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
    boolean IS_WINDOWS = (OS.indexOf("win") >= 0);
    boolean IS_MAC = (OS.indexOf("mac") >= 0);

    static Powershell getPowershell() throws UnsupportedOsException {
        if (IS_UNIX) {
            return LinuxPowershell.getInstance();
        }
        if (IS_WINDOWS) {
            return WindowsPowershell.getInstance();
        }
        if (IS_MAC) {
            return MacPowershell.getInstance();
        }
        throw new UnsupportedOsException("OS is not supported!");
    }

    /**
     * Execute the powershell request
     *
     * @param powershellRequest {@link PowershellRequest}
     * @return {@link PowershellResponse}
     * @throws PowershellExecutionException Throws when error occurred while execution
     */
    PowershellResponse execute(PowershellRequest powershellRequest) throws PowershellExecutionException;
}

