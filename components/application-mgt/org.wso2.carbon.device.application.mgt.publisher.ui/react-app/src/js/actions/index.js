import axios from "axios";
import ActionTypes from "../constants/ActionTypes";
import config from "../../../public/conf/config.json";

export const getApps = () => dispatch => {

    const request = "method=post&content-type=application/json&payload={}&api-endpoint=/application-mgt-publisher/v1.0/applications";

    return axios.post('https://' + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
    ).then(res => {
        if (res.status === 200) {
            let apps = [];

            if (res.data.data.hasOwnProperty("applications")) {
                apps = res.data.data.applications;
            }
            dispatch({type: ActionTypes.GET_APPS, payload: apps});
        }

    }).catch(function (error) {
        if (error.response.status === 401) {
            window.location.href = 'https://localhost:9443/publisher/login';
        }
    });


};

export const getRelease = (uuid) => dispatch => {

    const request = "method=get&content-type=application/json&payload={}&api-endpoint=/application-mgt-publisher/v1.0/applications/release/"+uuid;

    return axios.post('https://' + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
    ).then(res => {
        if (res.status === 200) {
            let release = res.data.data;
            dispatch({type: ActionTypes.GET_RELEASE, payload: release});
        }

    }).catch(function (error) {
        if (error.response.status === 401) {
            window.location.href = 'https://localhost:9443/publisher/login';
        }
    });


};

export const openReleasesModal = (app) => dispatch => {
    dispatch({
        type: ActionTypes.OPEN_RELEASES_MODAL,
        payload: {
            app:app
        }
    });
};

export const getLifecycle = ()=> dispatch =>{
    const request = "method=get&content-type=application/json&payload={}&api-endpoint=/application-mgt-publisher/v1.0/applications/lifecycle-config";

    return axios.post('https://' + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
    ).then(res => {
        if (res.status === 200) {
            let lifecycle = res.data.data;
            dispatch({type: ActionTypes.GET_LIFECYCLE, payload: lifecycle});
        }

    }).catch(function (error) {
        if (error.response.status === 401) {
            window.location.href = 'https://localhost:9443/publisher/login';
        }
    });
};
