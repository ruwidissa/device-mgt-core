package org.wso2.carbon.device.application.mgt.common.config;/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LifecycleGraph {

    private Map<LifecycleStateVertex, List<LifecycleStateVertex>> adjVertices;

    public LifecycleGraph() {
        this.adjVertices = new HashMap<>() ;
    }

    public void addVertex(String label) {
        adjVertices.putIfAbsent(new LifecycleStateVertex(label), new ArrayList<>());
    }

    public void addVertex(LifecycleStateVertex vertext) {
        adjVertices.putIfAbsent(vertext, new ArrayList<>());
    }

    void removeVertex(String label) {
        LifecycleStateVertex v = new LifecycleStateVertex(label);
        adjVertices.values()
                .stream()
                .map(e -> e.remove(v))
                .collect(Collectors.toList());
        adjVertices.remove(new LifecycleStateVertex(label));
    }

    public void addEdge(String label1, String label2) {
        LifecycleStateVertex v1 = new LifecycleStateVertex(label1);
        LifecycleStateVertex v2 = new LifecycleStateVertex(label2);
        adjVertices.get(v1).add(v2);
//        adjVertices.get(v2).add(v1);
    }

    public void removeEdge(String label1, String label2) {
        LifecycleStateVertex v1 = new LifecycleStateVertex(label1);
        LifecycleStateVertex v2 = new LifecycleStateVertex(label2);
        List<LifecycleStateVertex> eV1 = adjVertices.get(v1);
        List<LifecycleStateVertex> eV2 = adjVertices.get(v2);
        if (eV1 != null)
            eV1.remove(v2);
        if (eV2 != null)
            eV2.remove(v1);
    }

    public Map<LifecycleStateVertex, List<LifecycleStateVertex>> getAdjVertices() {
        return adjVertices;
    }
}
