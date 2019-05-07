import React from "react";
import * as SRD from "storm-react-diagrams";
import "storm-react-diagrams/dist/style.min.css";
import "./LifeCycle.css";
import {distributeElements} from "../../../js/utils/dagre-utils.ts";


class LifeCycleGraph extends React.Component {
    render() {

        const lifecycle = this.props.lifecycle;
        const nodes = [];
        const links = [];

        const engine = new SRD.DiagramEngine();
        engine.installDefaultFactories();

        const model = new SRD.DiagramModel();


        Object.keys(lifecycle).forEach((stateName) => {
            const node = createNode(stateName);
            nodes.push(node);
            lifecycle[stateName].node = node;
        });

        Object.keys(lifecycle).forEach((stateName) => {
            console.log(stateName);
            const state = lifecycle[stateName];

            //todo: remove checking property
            if(state.hasOwnProperty("proceedingStates")) {
                // console.log(state,state.proceedingStates);

                state.proceedingStates.forEach((proceedingState) => {
                    // console.log(proceedingState);
                    // console.log(lifecycle[proceedingState]);
                    // links.push(connectNodes(state.node, lifecycle[proceedingState].node));
                });
            }
        });
        links.push(connectNodes(nodes[0], nodes[1]));

        nodes.forEach((node) => {
            model.addNode(node);
        });

        console.log(links);
        links.forEach((link) => {
            model.addLink(link);
        });


        let distributedModel = getDistributedModel(engine, model);
        engine.setDiagramModel(distributedModel);

        return (
            <div>
                <SRD.DiagramWidget diagramEngine={engine}/>
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

function createNode(name) {
    return new SRD.DefaultNodeModel(name, "rgb(0,192,255)");
}

let count = 0;

function connectNodes(nodeFrom, nodeTo) {
    //just to get id-like structure
    count++;
    const portOut = nodeFrom.addPort(new SRD.DefaultPortModel(true, `${nodeFrom.name}-out-${count}`, " "));
    const portTo = nodeTo.addPort(new SRD.DefaultPortModel(false, `${nodeFrom.name}-to-${count}`, " "));
    return portOut.link(portTo);
}

export default LifeCycleGraph;