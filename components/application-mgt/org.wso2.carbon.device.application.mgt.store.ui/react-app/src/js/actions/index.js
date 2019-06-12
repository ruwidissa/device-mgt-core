import axios from "axios";
import ActionTypes from "../constants/ActionTypes";
import config from "../../../public/conf/config.json";

export const getApps = () => dispatch => {

    const request = "method=post&content-type=application/json&payload={}&api-endpoint=/application-mgt-store/v1.0/applications";

    return axios.post(config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
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
            window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort+'/store/login';
        }
    });

};

export const getRelease = (uuid) => dispatch => {

    const request = "method=get&content-type=application/json&payload={}&api-endpoint=/application-mgt-store/v1.0/applications/" + uuid;

    return axios.post(config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
    ).then(res => {
        if (res.status === 200) {
            let release = res.data.data.applicationReleases[0];
            dispatch({
                type: ActionTypes.GET_RELEASE,
                payload: {
                    release: release,
                    releaseLoading: false
                }
            });
        }
    }).catch(function (error) {
        if (error.response.status === 401) {
            window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort+'/store/login';
        }else if(error.response.status===404){
            dispatch({
                type: ActionTypes.GET_RELEASE,
                payload: {
                    release: null,
                    releaseLoading: false
                }});
        }
    });


};

export const openReleasesModal = (app) => dispatch => {
    dispatch({
        type: ActionTypes.OPEN_RELEASES_MODAL,
        payload: {
            app: app
        }
    });
};


export const openLifecycleModal = (nextState) => dispatch => {
    dispatch({
        type: ActionTypes.OPEN_LIFECYCLE_MODAL,
        payload: {
            nextState: nextState
        }
    });
};

export const closeLifecycleModal = () => dispatch => {
    dispatch({
        type: ActionTypes.CLOSE_LIFECYCLE_MODAL
    });
};

export const setLoading = (stateToLoad) => dispatch => {
    dispatch({
        type: ActionTypes.SET_LOADING_STATE,
        payload: {
            stateToLoad: stateToLoad
        }
    });
};

export const getLifecycle = () => dispatch => {
    const request = "method=get&content-type=application/json&payload={}&api-endpoint=/application-mgt-store/v1.0/applications/lifecycle-config";

    return axios.post(config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
    ).then(res => {
        if (res.status === 200) {
            let lifecycle = res.data.data;
            dispatch({type: ActionTypes.GET_LIFECYCLE, payload: lifecycle});
        }

    }).catch(function (error) {
        if (error.response.status === 401) {
            window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort+'/store/login';
        }
    });
};

export const updateLifecycleState = (uuid, nextState, reason) => dispatch => {

    const payload = {
        action: nextState,
        reason: reason
    };

    const request = "method=post&content-type=application/json&payload=" + JSON.stringify(payload) + "&api-endpoint=/application-mgt-store/v1.0/applications/life-cycle/" + uuid;


    return axios.post(config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
    ).then(res => {
        if (res.status === 201) {
            let release = res.data.data;
            dispatch({type: ActionTypes.UPDATE_LIFECYCLE_STATE, payload: release});
        }else {
            alert("error");
            dispatch({
                type: ActionTypes.CLOSE_LIFECYCLE_MODAL
            });
        }

    }).catch(function (error) {
        if (error.response.status === 401) {
            window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort+'/store/login';
        } else if (error.response.status === 500) {
            alert("error");
            dispatch({
                type: ActionTypes.CLOSE_LIFECYCLE_MODAL
            });
        }
    });


};

export const getDetailedRating = (uuid) => dispatch => {
    const request = "method=get&content-type=application/json&payload={}&api-endpoint=/application-mgt-store/v1.0/reviews/"+uuid+"/rating";

    return axios.post(config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
    ).then(res => {
        if (res.status === 200) {
            let detailedRating = res.data.data;
            dispatch({type: ActionTypes.GET_DETAILED_RATING, payload: detailedRating});
        }

    }).catch(function (error) {
        if (error.response.status === 401) {
            window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort+'/store/login';
        } else{
            dispatch({
                type: ActionTypes.GET_DETAILED_RATING, payload: null
            });
        }
    });


};
