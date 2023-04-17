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
