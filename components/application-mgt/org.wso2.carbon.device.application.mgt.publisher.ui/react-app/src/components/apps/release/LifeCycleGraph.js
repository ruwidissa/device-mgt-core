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
            }else if(nextStates.includes(stateName)){
                color = "rgb(0,192,255)";
            }
            const node = createNode(stateName, color);
            // node.addPort()
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
        });
        links.forEach((link) => {
            model.addLink(link);
        });


        let distributedModel = getDistributedModel(engine, model);
        engine.setDiagramModel(distributedModel);

        return (
            <div style={{height: 900}}>
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
    //just to get id-like structure
    // count++;
    // const portOut = nodeFrom.addPort(new SRD.DefaultPortModel(true, `${nodeFrom.name}-out-${count}`, " "));
    // const portTo = nodeTo.addPort(new SRD.DefaultPortModel(false, `${nodeFrom.name}-to-${count}`, " "));
    return nodeFrom.getPort(outPortName).link(nodeTo.getPort(inPortName));
}

export default LifeCycleGraph;