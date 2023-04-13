import React from 'react';
import { FormGroup, FormControlLabel, Checkbox } from '@material-ui/core';

function DefaultBoolean({ title, value, name, onChange, readOnly, ...other }) {
  const isIndeterminate = value === null;
  const checked = Boolean(value);
  return (
    <FormGroup>
      <FormControlLabel
        control={
          <Checkbox
            checked={checked}
            onChange={() =>
              onChange({
                target: {
                  value: value === false ? null : value === null ? true : false,
                },
              })
            }
            value={name}
            indeterminate={isIndeterminate}
            disabled={readOnly}
            {...other}
          />
        }
        label={title}
      />
    </FormGroup>
  );
}

export default DefaultBoolean;
