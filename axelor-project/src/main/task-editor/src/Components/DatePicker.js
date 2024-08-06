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
import React, { useEffect } from 'react';
import moment from 'moment';
import { KeyboardDatePicker } from '@material-ui/pickers';
import { makeStyles } from '@material-ui/core/styles';
import classnames from 'classnames';

const useStyles = makeStyles(theme => ({
  root: {
    minWidth: 200,
    border: '1px solid lightgray',
    height: 'fit-content',
    padding: '2px 10px',
    borderRadius: 4,
  },
}));
function DatePicker({ value, onChange, className, inputClassName }) {
  const [date, setDate] = React.useState(value);
  const classes = useStyles();

  const onSelectUpdate = date => {
    onChange(date);
    setDate(date);
  };

  useEffect(() => {
    setDate(value);
  }, [value]);

  return (
    <KeyboardDatePicker
      className={classnames(classes.root, className)}
      disableToolbar
      autoOk
      variant="inline"
      format="DD/MM/YYYY"
      classes={{
        root: inputClassName,
      }}
      InputProps={{
        disableUnderline: true,
      }}
      value={date ? moment(date) : null}
      onChange={date => onSelectUpdate(date)}
    />
  );
}

DatePicker.defaultProps = {
  value: null,
  onChange: () => {},
};

export default DatePicker;
