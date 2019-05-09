import keyMirror from 'keymirror';

const ActionTypes = keyMirror({
    LOGIN: null,
    GET_APPS: null,
    OPEN_RELEASES_MODAL: null,
    CLOSE_RELEASES_MODAL: null,
    GET_RELEASE: null,
    GET_LIFECYCLE: null,
    OPEN_LIFECYCLE_MODAL: null
});

export default ActionTypes;