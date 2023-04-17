import React from 'react';
import PropTypes from 'prop-types';
import Selection from './selection';
import StringWidget from './string';

function ManyToMany({ readOnly, viewRecord, ...others }) {
  return readOnly ? <StringWidget {...others} viewRecord={viewRecord} /> : <Selection {...others} isMulti />;
}

ManyToMany.propTypes = {
  name: PropTypes.string,
  title: PropTypes.string,
  onChange: PropTypes.func,
  fetchAPI: PropTypes.any,
  value: PropTypes.array,
  readOnly: PropTypes.bool,
  isSearchable: PropTypes.bool,
  optionLabelKey: PropTypes.string,
  optionValueKey: PropTypes.string,
};

export default ManyToMany;
