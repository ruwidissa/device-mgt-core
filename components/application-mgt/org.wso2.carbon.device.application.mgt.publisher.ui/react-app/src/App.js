import React from "react";
import "antd/dist/antd.css";
import { renderRoutes } from "react-router-config";

class App extends React.Component {
    constructor(props) {
        super(props);
       this.state = {
           route : props.route
        }
    }
  render() {
    return (
        <div>
            {renderRoutes(this.state.route.routes)}
        </div>

    );
  }
}

export default App;
