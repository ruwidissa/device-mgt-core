import ActionTypes from "../constants/ActionTypes";

const initialState = {
    apps: [],
    releaseView: {
        visible: false,
        app: null
    },
    release: null,
    lifecycle: null
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
        return Object.assign({}, state, {
            release: action.payload
        });
    } else if (action.type === ActionTypes.GET_LIFECYCLE) {
        return Object.assign({}, state, {
            lifecycle: action.payload
        });
    }
    return state;
}


export default rootReducer;