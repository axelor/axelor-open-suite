import React from 'react';
import PropTypes from 'prop-types';
import Selection from './selection';
import StringWidget from './string';

function ManyToOne({ readOnly, viewRecord, ...others }) {
  return readOnly ? <StringWidget viewRecord={viewRecord} {...others} /> : <Selection {...others} />;
}

ManyToOne.propTypes = {
  name: PropTypes.string,
  title: PropTypes.string,
  onChange: PropTypes.func,
  fetchAPI: PropTypes.any,
  value: PropTypes.object,
  readOnly: PropTypes.bool,
  isSearchable: PropTypes.bool,
  optionLabelKey: PropTypes.string,
  optionValueKey: PropTypes.string,
};

export default ManyToOne;
