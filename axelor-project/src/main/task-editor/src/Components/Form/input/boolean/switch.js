import React from 'react';
import { makeStyles } from '@material-ui/styles';
import { Switch as Boolean, FormControl, FormLabel } from '@material-ui/core';

const useStyles = makeStyles(theme => ({
  label: {
    fontSize: '0.8rem',
  },
}));

export default function Switch({ title, value, onChange, readOnly, ...other }) {
  const classes = useStyles();
  return (
    <FormControl>
      <FormLabel className={classes.label}>{title}</FormLabel>
      <Boolean checked={value || false} onChange={onChange} color="primary" value="" disabled={readOnly} {...other} />
    </FormControl>
  );
}
