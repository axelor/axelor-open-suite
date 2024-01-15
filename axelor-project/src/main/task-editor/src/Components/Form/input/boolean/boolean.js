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
