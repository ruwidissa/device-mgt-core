import keyMirror from 'keymirror';

const ActionTypes = keyMirror({
    LOGIN: null,
    GET_APPS: null,
    OPEN_RELEASES_MODAL: null,
    CLOSE_RELEASES_MODAL: null,
    GET_RELEASE: null,
    GET_LIFECYCLE: null

});

export default ActionTypes;