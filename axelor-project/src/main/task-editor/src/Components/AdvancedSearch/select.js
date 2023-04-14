import React from 'react';
import TextField from '@material-ui/core/TextField';
import Autocomplete from '@material-ui/lab/Autocomplete';

export default function Selection({ name, value = '', onChange, options, ...rest }) {
  const defaultProps = {
    options: options,
    getOptionLabel: option => option.title || '',
  };

  return (
    <Autocomplete
      {...defaultProps}
      value={options.find(o => o.name === value)}
      onChange={(e, value) => {
        if (value) {
          onChange(value.name);
        }
      }}
      name={name}
      style={{
        marginRight: 8,
        width: 150,
      }}
      {...rest}
      renderInput={params => <TextField {...params} />}
    ></Autocomplete>
  );
}
