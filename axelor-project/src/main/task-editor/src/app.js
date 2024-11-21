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
import React from 'react';
import MomentUtils from '@date-io/moment';
import { MuiPickersUtilsProvider } from '@material-ui/pickers';

import { WithTheme } from './withRoot';
import TaskEditor from './Views/list/TaskEditor';

export const getThemeOptions = ({ language, theme } = {}) => {
  const options = {};
  if (language === 'ar' || language === 'he') options.direction = 'rtl';
  if (theme === 'dark') options.palette = { type: 'dark' };
  return options;
};

const { direction, palette } = getThemeOptions({});

function App() {
  return (
    <WithTheme direction={direction} palette={palette}>
      <MuiPickersUtilsProvider utils={MomentUtils}>
        <div className="app">
          <TaskEditor />
        </div>
      </MuiPickersUtilsProvider>
    </WithTheme>
  );
}

export default App;
