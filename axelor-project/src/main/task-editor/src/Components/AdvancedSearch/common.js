import React from 'react';
import { Link, Radio, RadioGroup, FormControlLabel, Checkbox, Button } from '@material-ui/core';
import classnames from 'classnames';

export function ButtonLink({ title, className, onClick, ...rest }) {
  return (
    title && (
      <Link component="button" variant="body2" onClick={onClick} className={className} {...rest}>
        {title}
      </Link>
    )
  );
}

export function BooleanRadio({ name, className, onChange, value, classes, data }) {
  return (
    <RadioGroup aria-label={name} name={name} className={className} value={value} onChange={e => onChange(e)}>
      {data.map(({ value, label }, index) => (
        <FormControlLabel
          key={index}
          value={value}
          control={<Radio />}
          label={label}
          classes={{ label: classes && classes.label }}
        />
      ))}
    </RadioGroup>
  );
}

export function BooleanCheckBox({ name, value, onChange, title, inline = false, isDisabled, classes, ...rest }) {
  if (inline) {
    return (
      <Checkbox
        checked={Boolean(value)}
        onChange={({ target: { checked } }) => onChange(checked)}
        value={name}
        name={name}
        disabled={isDisabled}
        {...rest}
      />
    );
  }
  return (
    <FormControlLabel
      control={
        <Checkbox
          checked={Boolean(value)}
          onChange={({ target: { checked } }) => onChange({ name, checked })}
          value={name}
        />
      }
      label={title}
      classes={{ label: classes && classes.label }}
    />
  );
}

export function SimpleButton({ classes, onClick, title, hide, className, ...rest }) {
  return (
    <Button
      variant="contained"
      color="primary"
      className={classnames(classes.button, className)}
      onClick={onClick}
      {...rest}
      style={hide ? { display: 'none' } : {}}
    >
      {title}
    </Button>
  );
}
