import axios from "axios";
import {GET_APPS} from "../constants/action-types";

export function getApps() {
    axios.post('https://localhost:9443/api/application-mgt-handler/v1.0/invoke', request
    ).then(res => {
        if(res.status === 200){
            return {type: GET_APPS, payload : res.data}
        }

    }).catch(function (error) {
        if(error.response.status === 401){
            window.location.href = 'https://localhost:9443/publisher/login';
        }
    });
}