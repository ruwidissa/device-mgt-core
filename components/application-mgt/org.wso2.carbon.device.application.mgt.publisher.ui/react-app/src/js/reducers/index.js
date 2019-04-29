import {GET_APPS} from "../constants/action-types";

const initialState = {
    apps: [{
        id: 1,
        title: 'Hi',
        platform: 'android',
        description: 'lorem',
        subType: 'FREE',
        type: 'ENTERPRISE'
    },
        {
            id: 2,
            title: 'Hi',
            platform: 'android',
            description: 'lorem',
            subType: 'FREE',
            type: 'ENTERPRISE'
        }]
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