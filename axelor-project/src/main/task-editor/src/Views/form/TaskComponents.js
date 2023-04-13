import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Select from '@material-ui/core/Select';

import { PRIORITY } from '../../constants';
import { StaticSelect } from '../../Components';

const useStyles = makeStyles(theme => ({
  progressSelect: {
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

const ProgressOptions = [0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100];

export function TaskProgressSelect({ t, ...props }) {
  const classes = useStyles();
  return (
    <Select
      native
      classes={{
        select: classes.progressSelect,
      }}
      inputProps={{
        name: 'progressSelect',
        id: 'outlined-progressSelect',
      }}
      {...props}
    >
      <option aria-label={t('TaskEditor.none')} value="" />
      {ProgressOptions.map(progress => (
        <option key={progress} value={progress}>
          {progress} %
        </option>
      ))}
    </Select>
  );
}
