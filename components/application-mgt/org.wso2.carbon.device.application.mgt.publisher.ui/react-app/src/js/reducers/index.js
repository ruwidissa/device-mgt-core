import {GET_APPS} from "../constants/action-types";

const initialState = {
    apps : []
};

function rootReducer(state = initialState, action) {
    if (action.type === GET_APPS) {
        return Object.assign({}, state, {
            apps: state.apps.concat(action.payload)
        });
    }
    return state;
}

export default rootReducer;