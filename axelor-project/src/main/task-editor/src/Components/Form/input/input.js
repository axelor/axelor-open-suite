import React from 'react';
import PropTypes from 'prop-types';
import Input from '@material-ui/core/Input';
import { TextField } from '@material-ui/core';
import StringWidget from '../string';

function InputField({
  name,
  title,
  autoTitle,
  value: $value = '',
  onChange,
  onBlur,
  readOnly,
  inline,
  InputProps,
  style,
  isControlled = true,
  ...other
}) {
  const [value, setValue] = React.useState($value);

  function _onBlur(e) {
    onChange(value);
    onBlur && onBlur(e);
  }

  function handleChange(e) {
    const { value } = e.target;
    if (isControlled) {
      setValue(value);
    } else {
      onChange(value);
    }
  }

  React.useEffect(() => {
    setValue($value);
  }, [$value]);

  if (inline) {
    return readOnly ? (
      <StringWidget value={value} />
    ) : (
      <Input
        style={{ width: '100%', ...style }}
        placeholder={title}
        inputProps={{ 'aria-label': title }}
        name={name}
        onChange={handleChange}
        onBlur={_onBlur}
        autoComplete="off"
        readOnly={readOnly}
        disabled={readOnly}
        value={value || ''}
        {...other}
      />
    );
  }
  return readOnly ? (
    <StringWidget title={title} value={value} />
  ) : (
    <TextField
      id={`filled-${name}`}
      label={title || autoTitle}
      name={name}
      style={{ width: '100%', ...style }}
      onChange={handleChange}
      onBlur={_onBlur}
      autoComplete="off"
      fullWidth
      InputProps={{ readOnly, ...InputProps }}
      value={value || ''}
      className={other.className}
      {...other}
    />
  );
}

InputField.propTypes = {
  name: PropTypes.string,
  title: PropTypes.string,
  value: PropTypes.string,
  onChange: PropTypes.func,
  readOnly: PropTypes.bool,
  multiline: PropTypes.bool,
  rows: PropTypes.number,
};

InputField.defaultProps = {
  rows: 3,
  readOnly: false,
};
export default InputField;
