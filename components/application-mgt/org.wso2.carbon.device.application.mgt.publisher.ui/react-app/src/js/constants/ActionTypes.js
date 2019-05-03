import keyMirror from 'keymirror';

// export const LOGIN = "LOGIN";
// export const GET_APPS = "GET_APPS";
// export const OPEN_RELEASES_MODAL = "OPEN_RELEASES_MODAL";
// export const CLOSE_RELEASES_MODAL = "CLOSE_RELEASES_MODAL";

const ActionTypes = keyMirror({
    LOGIN: null,
    GET_APPS: null,
    OPEN_RELEASES_MODAL: null,
    CLOSE_RELEASES_MODAL: null,
    GET_RELEASE: null

});

export default ActionTypes;