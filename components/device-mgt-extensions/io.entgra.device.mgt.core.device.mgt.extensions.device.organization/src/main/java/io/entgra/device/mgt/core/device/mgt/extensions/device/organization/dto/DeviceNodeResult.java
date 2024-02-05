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

package io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto;

import java.util.List;
import java.util.Set;

public class DeviceNodeResult {

    private List<DeviceNode> nodes;
    private Set<DeviceOrganization> edges;

    public DeviceNodeResult(List<DeviceNode> nodes, Set<DeviceOrganization> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<DeviceNode> getNodes() {
        return nodes;
    }

    public Set<DeviceOrganization> getEdges() {
        return edges;
    }

}
