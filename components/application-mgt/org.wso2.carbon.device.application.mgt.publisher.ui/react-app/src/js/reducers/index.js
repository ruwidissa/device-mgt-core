import ActionTypes from "../constants/ActionTypes";

const initialState = {
    apps: [],
    releaseView: {
        visible: false,
        app: null
    }
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
    }
    return state;
}


export default rootReducer;