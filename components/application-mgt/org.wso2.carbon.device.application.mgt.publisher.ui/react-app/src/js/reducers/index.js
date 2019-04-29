import {GET_APPS} from "../constants/action-types";

const initialState = {
    apps: []
};

function rootReducer(state = initialState, action) {
    if (action.type === GET_APPS) {
        console.log(11);
        return Object.assign({}, state, {
            apps: action.payload
        });
    }
    return state;
}

export default rootReducer;