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
import {
    PageHeader,
    Typography,
    Breadcrumb,
    Icon,
    Button, Select
} from "antd";
import {Link} from "react-router-dom";
import DeviceTable from "../../../components/Devices/DevicesTable";

const {Paragraph} = Typography;

class Devices extends React.Component {
    routes;

    constructor(props) {
        super(props);
        this.routes = props.routes;
        this.state = {
            deselectRequest:false,
            deleteRequest:false,
            deleteButtonDisabled:true,
            displayDeleteButton:'none',
            selected:"Actions"
        }
        this.deleteCall = this.deleteCall.bind(this);
        this.cancelDelete = this.cancelDelete.bind(this);
    }

    //This method is used to trigger delete request on selected devices
    deleteCall = () => {
        this.setState({deleteRequest:!this.state.deleteRequest});
    }

    //This method is used to cancel deletion
    cancelDelete = () => {
        this.setState({displayDeleteButton:'none' , deleteRequest:false})
    }

    //When delete action is selected, this method is called and devices which aren't in REMOVED state becomes unselectable
    onChange = value => {
        this.setState(
                {displayDeleteButton:'inline' , deselectRequest:!this.state.deselectRequest
                });
    }

    render() {
        return (
            <div>
                <PageHeader style={{paddingTop: 0}}>
                    <Breadcrumb style={{paddingBottom: 16}}>
                        <Breadcrumb.Item>
                            <Link to="/entgra/devices"><Icon type="home"/> Home</Link>
                        </Breadcrumb.Item>
                        <Breadcrumb.Item>Devices</Breadcrumb.Item>
                    </Breadcrumb>
                    <div className="wrap">
                        <h3>Devices</h3>
                        <Paragraph>Lorem ipsum dolor sit amet, est similique constituto at, quot inermis id mel, an
                            illud incorrupte nam.</Paragraph>
                        <div style={{paddingBottom:'5px'}}>
                            <table>
                                <tbody>
                                <tr>
                                    <td>
                                        <Select
                                                value={this.state.selected}
                                                showSearch
                                                style={{ width: 100 }}
                                                placeholder="Actions"
                                                optionFilterProp="children"
                                                onChange={this.onChange}
                                                filterOption={(input, option) =>
                                                        option.props.children
                                                                .toLowerCase()
                                                                .indexOf(input.toLowerCase()) >= 0
                                                }>
                                            <Select.Option value="delete">Delete</Select.Option>
                                        </Select>
                                    </td>
                                    <td>
                                        <Button type="primary" icon="delete"
                                                onClick={this.deleteCall}
                                                style={{display:this.state.displayDeleteButton}}>
                                            Delete Selected Devices
                                        </Button>.
                                    </td>
                                    <td>
                                        <Button type="danger"
                                                onClick={this.cancelDelete}
                                                style={{display:this.state.displayDeleteButton}}>
                                            Cancel
                                        </Button>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div style={{backgroundColor:"#ffffff", borderRadius: 5}}>
                        <DeviceTable
                                deleteRequest={this.state.deleteRequest}
                                deselectRequest={this.state.deselectRequest}/>
                    </div>
                </PageHeader>
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 720}}>

                </div>
            </div>
        );
    }
}

export default Devices;
