/*
 * Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto;

public class RootChildrenRequest extends PaginationRequest{

    int maxDepth;
    boolean includeDevice;

    public RootChildrenRequest(int start, int limit) {
        super(start, limit);
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth cannot be negative");
        }
        this.maxDepth = maxDepth;
    }

    public boolean isIncludeDevice() {
        return includeDevice;
    }

    public void setIncludeDevice(boolean includeDevice) {
        this.includeDevice = includeDevice;
    }
}
