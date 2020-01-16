import React from 'react';

import { Card, Col } from 'antd';

class CountWidget extends React.Component {
  constructor(props) {
    super(props);
    this.routes = props.routes;
    this.state = {
      statArray: [],
    };
  }

  componentDidMount() {
    this.setState({ statArray: this.props.statArray });
    console.log('$$$$');
    console.log(this.props.statArray);
  }

  render() {
    const { statArray } = this.state;

    let card = statArray.map(data => (
      <Col key={data.item} span={6}>
        <Card
          key={data.item}
          bordered={true}
          hoverable={true}
          style={{ borderRadius: 10, marginBottom: 16 }}
        >
          <div align="center">
            <h2>
              <b>{data.item}</b>
            </h2>
            <h1>{data.count}</h1>
            {/* <p>{data.duration}</p>*/}
            {/* <ReportFilterModal/>*/}
          </div>
        </Card>
      </Col>
    ));

    return <div>{card}</div>;
  }
}

export default CountWidget;
