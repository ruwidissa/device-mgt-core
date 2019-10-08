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
import {Button, Icon, notification} from "antd";

class BulkActionBar extends React.Component {

    constructor(props){
        super(props);
        this.state = {
            selectedMultiple:false,
            selectedSingle:false
        }
    }

    //This method is used to trigger delete request on selected devices
    deleteDevice = () => {
        const deviceStatusArray = this.props.selectedRows.map(obj => obj.enrolmentInfo.status);
        if(deviceStatusArray.includes("ACTIVE") || deviceStatusArray.includes("INACTIVE")){
            notification["error"]({
                message: "There was a problem",
                duration: 0,
                description:
                "Cannot delete ACTIVE/INACTIVE devices.",
            });
        }else{
            this.props.deleteDevice();
        }
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if(prevProps.selectedRows !== this.props.selectedRows){
            if(this.props.selectedRows.length > 1){
                this.setState({selectedMultiple:true,selectedSingle:false})
            }else if(this.props.selectedRows.length == 1){
                this.setState({selectedSingle:true,selectedMultiple:true})
            }else{
                this.setState({selectedSingle:false,selectedMultiple:false})
            }
        }
    }

    render() {
        return(
                <div style={{padding:'5px'}}>
                                <Button
                                        type="normal"
                                        icon="delete"
                                        size={'default'}
                                        onClick={this.deleteDevice}
                                        style={
                                            {display:this.state.selectedMultiple ? "inline" : "none"}
                                        }>Delete
                                </Button>

                                <Button
                                        type="normal"
                                        icon="delete"
                                        size={'default'}
                                        style={
                                            {display:this.state.selectedSingle ? "inline" : "none"}
                                        }>Disenroll
                                </Button>
                </div>
        )
    }
}

export default BulkActionBar;