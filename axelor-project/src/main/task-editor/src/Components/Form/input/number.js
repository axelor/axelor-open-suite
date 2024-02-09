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
