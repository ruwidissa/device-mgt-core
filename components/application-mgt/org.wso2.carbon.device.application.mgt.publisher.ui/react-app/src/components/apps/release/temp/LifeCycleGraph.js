/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from "react";
import * as SRD from "storm-react-diagrams";
import "storm-react-diagrams/dist/style.min.css";
import "./LifeCycle.css";
import {distributeElements} from "../../../js/utils/dagre-utils.ts";

const inPortName = "IN";
const outPortName = "OUT";

class LifeCycleGraph extends React.Component {
    render() {

        const lifecycle = this.props.lifecycle;
        const nodes = [];
        const links = [];

        const engine = new SRD.DiagramEngine();
        engine.installDefaultFactories();

        const model = new SRD.DiagramModel();
        const nextStates = lifecycle[this.props.currentStatus].proceedingStates;


        Object.keys(lifecycle).forEach((stateName) => {
            let color = "rgb(83, 92, 104)";
            if (stateName === this.props.currentStatus) {
                color = "rgb(192,255,0)";
            } else if (nextStates.includes(stateName)) {
                color = "rgb(0,192,255)";
            }
            const node = createNode(stateName, color);
            nodes.push(node);
            lifecycle[stateName].node = node;
        });

        Object.keys(lifecycle).forEach((stateName) => {
            const state = lifecycle[stateName];
            //todo: remove checking property
            if (state.hasOwnProperty("proceedingStates")) {

                state.proceedingStates.forEach((proceedingState) => {
                    links.push(connectNodes(state.node, lifecycle[proceedingState].node));
                });
            }
        });

        nodes.forEach((node) => {
            model.addNode(node);
            // node.addListener({
            //     selectionChanged: (node, isSelected) => {
            //         console.log(isSelected);
            //     }
            // });
        });
        links.forEach((link) => {
            model.addLink(link);
        });


        let distributedModel = getDistributedModel(engine, model);
        engine.setDiagramModel(distributedModel);

        return (
            <div style={{height: 500}}>
                <SRD.DiagramWidget diagramEngine={engine} maxNumberPointsPerLink={10} smartRouting={true}/>
            </div>
        );
    }
}

function getDistributedModel(engine, model) {
    const serialized = model.serializeDiagram();
    const distributedSerializedDiagram = distributeElements(serialized);

    //deserialize the model
    let deSerializedModel = new SRD.DiagramModel();
    deSerializedModel.deSerializeDiagram(distributedSerializedDiagram, engine);
    return deSerializedModel;
}

function createNode(name, color) {
    const node = new SRD.DefaultNodeModel(name, color);
    node.addPort(new SRD.DefaultPortModel(true, inPortName, " "));
    node.addPort(new SRD.DefaultPortModel(false, outPortName, " "));
    return node;
}

let count = 0;

function connectNodes(nodeFrom, nodeTo) {
    return nodeFrom.getPort(outPortName).link(nodeTo.getPort(inPortName));
}

function f() {
    // console.log(1);
}

export default LifeCycleGraph;