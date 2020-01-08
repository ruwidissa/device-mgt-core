import React from "react";

import {Card, Col, Icon} from "antd";
import {Link} from "react-router-dom";

class CountWidget extends React.Component {

    constructor(props) {
        super(props);
        this.routes = props.routes;
        this.state = {
            statArray:[]
        }
    }

    componentDidMount() {
        this.setState({statArray:this.props.statArray})
        console.log("$$$$")
        console.log(this.props.statArray)
    }


    render() {
        const countObj = [
            {item:"All",count:100},
            {item:"Enrolled",count:80},
            {item:"Unenrolled",count:20}];

        const { statArray } = this.state;

        let card = statArray.map((data) =>
            // <Card
            //     bordered={true}
            //     hoverable={true}
            //     key={data.item}
            //     style={{borderRadius: 5, marginBottom: 5, width:"100%"}}>
            //
            //     <h3>{data.item} Devices: {data.count}</h3>
            //
            // </Card>
            <Col key={data.item} span={6}>
                    <Card key={data.item} bordered={true} hoverable={true} style={{borderRadius: 10, marginBottom: 16}}>

                        <div align='center'>
                            <h2><b>{data.item}</b></h2>
                            <h1>{data.count}</h1>
                            {/*<p>{data.duration}</p>*/}
                            {/*<ReportFilterModal/>*/}
                        </div>
                    </Card>
            </Col>
        )

        return(
            <div>
                {card}
            </div>

        )
    }
}

export default CountWidget;