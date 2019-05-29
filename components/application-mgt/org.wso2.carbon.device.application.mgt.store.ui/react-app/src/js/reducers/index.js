import ActionTypes from "../constants/ActionTypes";

const initialState = {
    apps: [],
    releaseView: {
        visible: false,
        app: null
    },
    release: null,
    lifecycle: null,
    lifecycleModal: {
        visible: false,
        nextState: null
    },
    loadingState: {
        release: true
    },
    detailedRating: null

};

function rootReducer(state = initialState, action) {
    if (action.type === ActionTypes.GET_APPS) {
        return Object.assign({}, state, {
            apps: action.payload
        });
    } else if (action.type === ActionTypes.OPEN_RELEASES_MODAL) {
        return Object.assign({}, state, {
            releaseView: {
                visible: true,
                app: action.payload.app
            }
        });
    } else if (action.type === ActionTypes.GET_RELEASE) {
        let loadingState = {...state.loadingState};
        loadingState.release = action.payload.releaseLoading;
        return Object.assign({}, state, {
            release: action.payload.release,
            loadingState: loadingState
        });
    } else if (action.type === ActionTypes.GET_LIFECYCLE) {
        return Object.assign({}, state, {
            lifecycle: action.payload
        });
    } else if (action.type === ActionTypes.OPEN_LIFECYCLE_MODAL) {
        return Object.assign({}, state, {
            lifecycleModal: {
                visible: true,
                nextState: action.payload.nextState
            }
        });
    } else if (action.type === ActionTypes.CLOSE_LIFECYCLE_MODAL) {
        return Object.assign({}, state, {
            lifecycleModal: {
                visible: false,
                nextState: null
            }
        });
    } else if (action.type === ActionTypes.UPDATE_LIFECYCLE_STATE) {
        return Object.assign({}, state, {
            lifecycleModal: {
                visible: false,
                nextState: null,
            },
            release: action.payload
        });
    } else if (action.type === ActionTypes.SET_LOADING_STATE) {
        let loadingState = {...state.loadingState};
        loadingState[action.payload.stateToLoad] = true;
        return Object.assign({}, state, {
            loadingState: loadingState
        });
    } else if (action.type === ActionTypes.GET_DETAILED_RATING) {
        return Object.assign({}, state, {
            detailedRating: action.payload
        });
    }

    return state;
}


export default rootReducer;