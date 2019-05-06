import React from "react";
import * as SRD from "storm-react-diagrams";
import "storm-react-diagrams/dist/style.min.css";
import "./LifeCycle.css";

class LifeCycle extends React.Component {
    render() {
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

        model.addAll(node1, node2, node3, link1, link2);

        engine.setDiagramModel(model);

        return (
            <div >
                <SRD.DiagramWidget diagramEngine={engine} />
            </div>
        );
    }
}

export default LifeCycle;