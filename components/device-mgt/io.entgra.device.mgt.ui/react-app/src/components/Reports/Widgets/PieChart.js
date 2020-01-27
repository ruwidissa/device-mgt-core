import React from 'react';
import {
  Chart,
  Geom,
  Axis,
  Tooltip,
  Coord,
  Label,
  Legend,
  Guide,
} from 'bizcharts';
import DataSet from '@antv/data-set';
import axios from 'axios';
import { message, notification } from 'antd';
import { withConfigContext } from '../../../context/ConfigContext';

let config = null;

class PieChart extends React.Component {
  constructor(props) {
    super(props);
    config = this.props.context;
    this.state = {
      loading: true,
      statArray: [],
    };
  }

  componentDidMount() {
    const { reportData } = this.props;
    let params = {
      status: reportData.params[0],
      from: reportData.duration[0],
      to: reportData.duration[1],
    };

    const urlSet = {
      paramsList: reportData.params,
      duration: reportData.duration,
    };

    if (reportData.params[0] === 'Enrollments') {
      this.getEnrollmentsVsUnenrollmentsCount(params, urlSet);
    } else if (reportData.params[0] === 'BYOD') {
      this.getEnrollmentTypeCount(params, urlSet);
    } else {
      this.getCount(params, urlSet);
    }
  }

  onChartChange = data => {
    this.props.onClickPieChart(data);
  };

  statArray = [];

  // Call count APIs and get count for given parameters, then create data object to build pie chart
  getCount = (params, urlSet) => {
    this.setState({ loading: true });

    let { statArray } = this.state;

    const urlArray = [];

    urlSet.paramsList.map(data => {
      const paramsObj = {
        status: data,
        from: urlSet.duration[0],
        to: urlSet.duration[1],
      };
      const encodedExtraParams = Object.keys(paramsObj)
        .map(key => key + '=' + paramsObj[key])
        .join('&');
      const apiUrl =
        window.location.origin +
        config.serverConfig.invoker.uri +
        config.serverConfig.invoker.deviceMgt +
        '/reports/devices/count?' +
        encodedExtraParams;

      urlArray.push(axios.get(apiUrl, data));
    });

    axios
      .all(urlArray)
      .then(res => {
        res.map(response => {
          if (response.status === 200) {
            let countData = {
              item: response.config[0],
              // eslint-disable-next-line radix
              count: parseInt(response.data.data),
            };
            statArray.push(countData);
          }
        });
        this.setState({ statArray });
      })
      .catch(error => {
        if (error.hasOwnProperty('response') && error.response.status === 401) {
          // todo display a popup with error
          message.error('You are not logged in');
          window.location.href = window.location.origin + '/entgra/login';
        } else {
          notification.error({
            message: 'There was a problem',
            duration: 0,
            description: 'Error occurred while trying to get device count.',
          });
        }
      });
  };

  // Call count APIs and get count for given parameters, then create data object to build pie chart
  getEnrollmentsVsUnenrollmentsCount = (params, urlSet) => {
    this.setState({ loading: true });

    let { statArray } = this.state;

    const urlArray = [];

    urlSet.paramsList.map(data => {
      const paramsObj = {
        from: urlSet.duration[0],
        to: urlSet.duration[1],
      };
      const encodedExtraParams = Object.keys(paramsObj)
        .map(key => key + '=' + paramsObj[key])
        .join('&');

      let apiUrl;
      if (data === 'Enrollments') {
        apiUrl =
          window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/reports/devices/count?status=ACTIVE&status=INACTIVE&' +
          encodedExtraParams;
      } else {
        apiUrl =
          window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/reports/devices/count?status=REMOVED&' +
          encodedExtraParams;
      }

      urlArray.push(axios.get(apiUrl, data));
    });

    axios
      .all(urlArray)
      .then(res => {
        res.map(response => {
          if (response.status === 200) {
            let countData = {
              item: response.config[0],
              // eslint-disable-next-line radix
              count: parseInt(response.data.data),
            };
            statArray.push(countData);
          }
        });
        this.setState({ statArray });
      })
      .catch(error => {
        if (error.hasOwnProperty('response') && error.response.status === 401) {
          // todo display a popup with error
          message.error('You are not logged in');
          window.location.href = window.location.origin + '/entgra/login';
        } else {
          notification.error({
            message: 'There was a problem',
            duration: 0,
            description: 'Error occurred while trying to get device count.',
          });
        }
      });
  };

  // Call count APIs and get count for given parameters, then create data object to build pie chart
  getEnrollmentTypeCount = (params, urlSet) => {
    this.setState({ loading: true });

    let { statArray } = this.state;

    const urlArray = [];

    urlSet.paramsList.map(data => {
      const paramsObj = {
        ownership: data,
        from: urlSet.duration[0],
        to: urlSet.duration[1],
      };
      const encodedExtraParams = Object.keys(paramsObj)
        .map(key => key + '=' + paramsObj[key])
        .join('&');
      const apiUrl =
        window.location.origin +
        config.serverConfig.invoker.uri +
        config.serverConfig.invoker.deviceMgt +
        '/reports/devices/count?' +
        encodedExtraParams;

      urlArray.push(axios.get(apiUrl, data));
    });

    axios
      .all(urlArray)
      .then(res => {
        res.map(response => {
          if (response.status === 200) {
            let countData = {
              item: response.config[0],
              // eslint-disable-next-line radix
              count: parseInt(response.data.data),
            };
            statArray.push(countData);
          }
        });
        this.setState({ statArray });
      })
      .catch(error => {
        if (error.hasOwnProperty('response') && error.response.status === 401) {
          // todo display a popup with error
          message.error('You are not logged in');
          window.location.href = window.location.origin + '/entgra/login';
        } else {
          notification.error({
            message: 'There was a problem',
            duration: 0,
            description: 'Error occurred while trying to get device count.',
          });
        }
      });
  };

  render() {
    const { DataView } = DataSet;
    const { Html } = Guide;
    const { statArray } = this.state;

    const dv = new DataView();
    dv.source(statArray).transform({
      type: 'percent',
      field: 'count',
      dimension: 'item',
      as: 'percent',
    });
    const cols = {
      percent: {
        formatter: val => {
          val = val * 100 + '%';
          return val;
        },
      },
    };

    return (
      <div>
        <Chart
          height={window.innerHeight / 2}
          data={dv}
          scale={cols}
          padding={[20, 25, 20, 20]}
          forceFit
          onPlotClick={this.onChartChange}
          animate={true}
        >
          <Coord type={'theta'} radius={0.75} innerRadius={0.6} />
          <Axis name="percent" />
          <Legend
            position="right"
            offsetY={-window.innerHeight / 2 + 120}
            offsetX={-100}
          />
          <Tooltip
            showTitle={false}
            itemTpl='<li><span style="background-color:{color};" class="g2-tooltip-marker"></span>{name}: {value}</li>'
          />
          <Guide>
            <Html
              position={['50%', '50%']}
              html='<div style="color:#8c8c8c;font-size:1.16em;text-align: center;width: 10em;">Total<br><span style="color:#262626;font-size:2.5em">200</span>台</div>'
              alignX="middle"
              alignY="middle"
            />
          </Guide>
          <div onClick={this.clicked}>
            <Geom
              type="intervalStack"
              position="percent"
              color="item"
              tooltip={[
                'item*percent',
                (item, percent) => {
                  percent = percent * 100 + '%';
                  return {
                    name: item,
                    value: percent,
                  };
                },
              ]}
              style={{
                lineWidth: 1,
                stroke: '#fff',
              }}
            >
              <Label
                content="percent"
                formatter={(val, item) => {
                  return item.point.item + ': ' + val;
                }}
              />
            </Geom>
          </div>
        </Chart>
      </div>
    );
  }
}

export default withConfigContext(PieChart);
