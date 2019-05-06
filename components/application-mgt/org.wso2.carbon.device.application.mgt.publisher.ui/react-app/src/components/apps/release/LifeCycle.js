import React from "react";
import * as SRD from "storm-react-diagrams";
import "storm-react-diagrams/dist/style.min.css";
import "./LifeCycle.css";
import {distributeElements} from "../../../js/utils/dagre-utils.ts";


class LifeCycle extends React.Component {
    render() {
        const nodes = [];

        const engine = new SRD.DiagramEngine();
        engine.installDefaultFactories();

        const model = new SRD.DiagramModel();

        const node1 = new SRD.DefaultNodeModel("Node 1", "rgb(0,192,255)");
        const port1 = node1.addOutPort(" ");
        // node1.setPosition(100, 100);

        const node2 = new SRD.DefaultNodeModel("Node 2", "rgb(192,255,0)");
        const port2 = node2.addInPort(" ");

        const node3 = new SRD.DefaultNodeModel("Node 3", "rgb(192,255,0)");
        const port3 = node3.addInPort(" ");
        // node2.setPosition(400, 100);

        const link1 = port1.link(port2);
        const link2 = port1.link(port3);


        nodes.push(createNode("hi"));

        nodes.forEach((node)=>{
           model.addNode(node);
        });

        model.addAll(node1, node2, node3, link1, link2);

        let distributedModel = getDistributedModel(engine, model);
        engine.setDiagramModel(distributedModel);

        return (
            <div >
                <SRD.DiagramWidget diagramEngine={engine} />
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
    const portOut = nodeFrom.addPort(new SRD.DefaultPortModel(true, `${nodeFrom.name}-out-${count}`, "Out"));
    const portTo = nodeTo.addPort(new SRD.DefaultPortModel(false, `${nodeFrom.name}-to-${count}`, "IN"));
    return portOut.link(portTo);
}

export default LifeCycle;