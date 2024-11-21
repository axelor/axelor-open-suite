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
