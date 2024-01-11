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
