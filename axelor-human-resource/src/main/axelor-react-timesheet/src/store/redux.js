export const CHANGE_MODE = 'CHANGE_MODE';
export const CHANGE_KEY = 'CHANGE_KEY';

export function changeMode(mode) {
  return {
    type: CHANGE_MODE,
    text: mode,
  }
}

export function changeKeyPress(navigationKey) {
  return {
    type: CHANGE_KEY,
    text: navigationKey,
  }
}


const storageMode = localStorage.getItem('timesheet-mode');

const initialState = {
    mode: storageMode || 'week',
    navigationKey: 'start',
};

function timesheetApp(state = initialState, action) {
    switch(action.type) {
        case CHANGE_MODE:
            localStorage.setItem('timesheet-mode', action.text);  
            return Object.assign({}, state, {
                mode: action.text,
            });
        case CHANGE_KEY:
            return Object.assign({}, state, {
                navigationKey: action.text,
            });
        default: 
            return state;
    }
}

export default timesheetApp;