import ActionTypes from "../constants/ActionTypes";

const initialState = {
    apps: []
};

function rootReducer(state = initialState, action) {
    if (action.type === ActionTypes.GET_APPS) {
        console.log(11);
        return Object.assign({}, state, {
            apps: action.payload
        });
    }
    return state;
}

export default rootReducer;