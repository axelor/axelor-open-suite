import React, { useState, useCallback } from 'react';
import PropTypes from 'prop-types';
import { default as NumberFormat } from 'react-number-format';
import Input from './input';
import StringWidget from '../string';

function NumberFormatCustom(props) {
  const { inputRef, onChange, thousandSeparator = true, ...other } = props;
  return <NumberFormat getInputRef={inputRef} thousandSeparator={thousandSeparator} {...other} />;
}

function NumberField({
  type = 'integer',
  title,
  onChange,
  value = 0,
  readOnly,
  scale = 2,
  customeFormat,
  onBlur: blur,
  ...other
}) {
  let [val, setVal] = useState(value);

  const formatValue = useCallback(value => Number(Number(value).toFixed(type === 'integer' ? 0 : scale)), [
    type,
    scale,
  ]);

  React.useEffect(() => {
    setVal(formatValue(value));
  }, [value, setVal, formatValue]);

  function onBlur(e) {
    onChange(formatValue(e.target.value));
    setVal(formatValue(e.target.value));
    blur && blur(e);
  }
  return readOnly ? (
    <StringWidget
      title={title}
      value={<NumberFormatCustom {...customeFormat} value={value} displayType={'text'} />}
      customRender={true}
    />
  ) : (
    <Input onChange={setVal} title={title} type={'number'} value={`${val}`} onBlur={onBlur} {...other} />
  );
}
NumberField.propTypes = {
  name: PropTypes.string,
  title: PropTypes.string,
  type: PropTypes.string,
  onChange: PropTypes.func,
  readOnly: PropTypes.bool,
  scale: PropTypes.number,
  customeFormat: PropTypes.object,
};

NumberField.defaultProps = {
  readOnly: false,
};
export default NumberField;
