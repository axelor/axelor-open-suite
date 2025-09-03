/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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