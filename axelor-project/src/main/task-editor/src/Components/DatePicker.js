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
