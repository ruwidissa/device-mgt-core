import axios from "axios";
import {GET_APPS} from "../constants/action-types";

export function getApps() {

    return (dispatch) => {
        const request = "method=post&content-type=application/json&payload={}&api-endpoint=/application-mgt-publisher/v1.0/applications";

        return axios.post('https://localhost:9443/api/application-mgt-handler/v1.0/invoke', request
        ).then(res => {
            if (res.status === 200) {
                let apps = [];

                if(res.data.data.hasOwnProperty("applications")){
                    apps = res.data.data.applications;
                }
                console.log(res.data);
                dispatch({type: GET_APPS, payload: apps});
            }

        }).catch(function (error) {
            if (error.response.status === 401) {
                window.location.href = 'https://localhost:9443/publisher/login';
            }
        });

    };


}