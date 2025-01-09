/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import React, { useEffect, useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';

import { PRIORITY } from '../../constants';
import { StaticSelect } from '../../Components';

const useStyles = makeStyles(theme => ({
  progress: {
    paddingBlock: '9.5px',
  },
}));

export function TaskPrioritySelect({ options: _options, ...props }) {
  const options = React.useMemo(() => {
    const colorOptions = PRIORITY;
    const colorField = 'technicalTypeSelect';
    return _options.map(option => ({
      ...option,
      $background: colorOptions[option[colorField]],
    }));
  }, [_options]);

  return <StaticSelect name="priority" options={options} {...props} />;
}

const MIN_PROGRESS_VALUE = 0;
const MAX_PROGRESS_VALUE = 100;

export function TaskProgress({ ...props }) {
  const classes = useStyles();

  const [value, setValue] = useState(parseFloat(props.value).toFixed(2));

  useEffect(() => setValue(parseFloat(props.value).toFixed(2)), [props.value]);

  const formatProgressValue = event => {
    let newValue = parseFloat(event.target.value);

    if (newValue < MIN_PROGRESS_VALUE) {
      newValue = MIN_PROGRESS_VALUE;
    }
    if (newValue > MAX_PROGRESS_VALUE) {
      newValue = MAX_PROGRESS_VALUE;
    }

    setValue(newValue.toFixed(2));
    props.onChange(newValue, event);
  };

  return (
    <TextField
      variant="outlined"
      type="number"
      classes={{ select: classes.progress }}
      inputProps={{
        min: MIN_PROGRESS_VALUE,
        max: MAX_PROGRESS_VALUE,
        step: 1,
        name: 'progress',
        id: 'outlined-progress',
      }}
      {...props}
      value={value}
      onChange={event => setValue(event.target.value)}
      onBlur={formatProgressValue}
      onClick={formatProgressValue}
    />
  );
}
