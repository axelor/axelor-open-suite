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
