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
import React, { useState } from 'react';

import MomentUtils from '@date-io/moment';
import moment from 'moment';
import {
  MuiPickersUtilsProvider,
  TimePicker as Time,
  DateTimePicker as DateTime,
  KeyboardDatePicker,
} from '@material-ui/pickers';
import StringWidget from '../string';

const PICKERS = {
  date: KeyboardDatePicker,
  time: Time,
  datetime: DateTime,
};

const defaultFormat = {
  date: 'DD/MM/YYYY',
  time: 'LT',
  datetime: 'DD/MM/YYYY h:mm a',
};

function DateTimePicker({ inline, type = 'date', ...props }) {
  const [open, setOpen] = useState(false);
  let valueRef = React.useRef();
  const { name, title, format, readOnly = false, error, onChange, ...other } = props;

  if (readOnly) {
    const value = props.value ? moment(props.value).format(format || defaultFormat[type]) : '';
    return <StringWidget title={title} value={value} />;
  }
  const Picker = PICKERS[type];

  function onKeyDown(e) {
    if (e.keyCode === 40) setOpen(true);
  }

  function onClose() {
    onChange(valueRef.current);
    setOpen(false);
  }

  return (
    <MuiPickersUtilsProvider utils={MomentUtils} moment={moment}>
      <Picker
        autoOk={true}
        open={open}
        onChange={value => {
          valueRef.current = value;
        }}
        PopoverProps={{
          anchorOrigin: { vertical: 'bottom', horizontal: 'left' },
          transformOrigin: { vertical: 'top', horizontal: 'left' },
        }}
        disableToolbar
        variant="inline"
        {...(inline ? { invalidDateMessage: '' } : {})}
        style={{ width: '100%', ...(inline ? { margin: 0 } : {}) }}
        label={inline ? '' : title}
        format={format || defaultFormat[type]}
        {...(type !== 'time' ? { animateYearScrolling: false } : {})}
        {...other}
        onKeyDown={onKeyDown}
        onClose={onClose}
        onOpen={() => setOpen(true)}
      />
    </MuiPickersUtilsProvider>
  );
}

export default DateTimePicker;
