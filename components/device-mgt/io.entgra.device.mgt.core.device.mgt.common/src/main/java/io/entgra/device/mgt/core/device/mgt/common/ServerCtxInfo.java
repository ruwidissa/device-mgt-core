/*
 *   Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *   Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package io.entgra.device.mgt.core.device.mgt.common;

public class ServerCtxInfo {
    private int activeServerCount;
    private int localServerHashIdx;

    public ServerCtxInfo(int activeServerCount, int localServerHashIdx){
        this.activeServerCount = activeServerCount;
        this.localServerHashIdx = localServerHashIdx;
    }

    public int getActiveServerCount() {
        return activeServerCount;
    }

    public void setActiveServerCount(int activeServerCount) {
        this.activeServerCount = activeServerCount;
    }

    public int getLocalServerHashIdx() {
        return localServerHashIdx;
    }

    public void setLocalServerHashIdx(int localServerHashIdx) {
        this.localServerHashIdx = localServerHashIdx;
    }
}