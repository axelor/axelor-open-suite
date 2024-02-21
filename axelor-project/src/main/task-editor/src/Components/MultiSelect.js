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
import React, { useState, useEffect, useCallback } from 'react';
import Autocomplete from '@material-ui/lab/Autocomplete';
import DoneIcon from '@material-ui/icons/Done';
import { TextField, Chip } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';

import { COLORS } from '../constants';
import Service from '../Services';

function useDebounceEffect(handler, interval) {
  const isMounted = React.useRef(false);
  React.useEffect(() => {
    if (isMounted.current) {
      const timer = setTimeout(() => handler(), interval);
      return () => clearTimeout(timer);
    }
    isMounted.current = true;
  }, [handler, interval]);
}

const chipColor = {};

const getChipColor = id => {
  if (chipColor[id]) return chipColor[id];
  const no = Math.round(Math.random() * 10) % COLORS.length;
  return (chipColor[id] = COLORS[no]);
};

const useStyles = makeStyles({
  input: {
    fontSize: 14,
    color: 'black',
  },
  label: {
    fontSize: 14,
  },
  iconSelected: {
    fontSize: 20,
  },
  chip: {
    marginLeft: 5,
  },
  deleteIcon: {
    color: 'white',
  },
});

export default function MultiSelect({
  model,
  optionLabel = 'name',
  index,
  value = [],
  update,
  criteriaIds,
  operator = 'and',
  classes: classesProp = {},
  className: classNameProp,
  disableClearable,
  TextFieldProps = {},
  options: propOptions,
  criteria: propCriteria,
}) {
  const [open, setOpen] = useState(false);
  const [options, setOptions] = useState([]);
  const [searchText, setsearchText] = useState(null);
  const classes = useStyles();

  const fetchOptions = useCallback(
    (searchText = '') => {
      const criteria = [];
      if (searchText) {
        criteria.push({
          fieldName: optionLabel,
          operator: 'like',
          value: searchText,
        });
      }

      if (propCriteria) {
        propCriteria && propCriteria.map(c => criteria.push(c));
      }

      const data = {
        criteria,
        operator: operator,
      };

      return Service.search(model, {
        fields: ['name', 'fullName'],
        data,
        limit: 10,
        offset: 0,
      }).then(({ data }) => {
        if (data && data.length) {
          setOptions([...data]);
        }
      });
    },
    [model, operator, optionLabel, propCriteria],
  );

  const optionDebounceHandler = React.useCallback(() => {
    if (searchText) {
      fetchOptions(searchText, criteriaIds);
    }
  }, [fetchOptions, searchText, criteriaIds]);

  useDebounceEffect(optionDebounceHandler, 500);

  useEffect(() => {
    if (!open) {
      setOptions([]);
    }
  }, [open]);

  useEffect(() => {
    if (open) {
      if (propOptions && propOptions.length > 0) {
        setOptions([...propOptions]);
      } else {
        fetchOptions(null, criteriaIds);
      }
    }
  }, [fetchOptions, open, criteriaIds, propOptions]);

  return (
    <Autocomplete
      classes={{
        inputFocused: classes.input,
        clearIndicator: classes.input,
        popupIndicator: classes.input,
        disabled: classes.disabled,
        ...classesProp,
      }}
      className={classNameProp}
      value={value}
      size="small"
      key={index}
      open={open}
      onOpen={e => {
        e && e.stopPropagation();
        setOpen(true);
      }}
      onClose={e => {
        e && e.stopPropagation();
        setOpen(false);
      }}
      multiple
      options={options}
      disableCloseOnSelect
      disableClearable={disableClearable}
      clearOnEscape
      autoComplete
      onChange={(e, option) => {
        update(option.filter((v, i, a) => a.findIndex(t => t.id === v.id) === i));
        setOpen(false);
      }}
      renderTags={(value, getTagProps) =>
        value.map((option, index) => (
          <Chip
            variant="outlined"
            label={option[optionLabel]}
            size="small"
            classes={{
              deleteIcon: classes.deleteIcon,
            }}
            style={{
              backgroundColor: getChipColor(option.id),
              border: `1px solid ${getChipColor(option.id)}`,
              color: 'white',
            }}
            {...getTagProps({ index })}
          />
        ))
      }
      getOptionLabel={option => option[optionLabel] || ''}
      renderOption={(option, { selected }) => (
        <React.Fragment>
          <DoneIcon
            className={classes.iconSelected}
            style={{
              visibility: (value && value.find(value => value.id === option.id)) || selected ? 'visible' : 'hidden',
            }}
          />
          <Chip
            variant="outlined"
            label={option[optionLabel]}
            size="small"
            className={classes.chip}
            style={{
              backgroundColor: getChipColor(option.id),
              border: `1px solid ${getChipColor(option.id)}`,
              color: 'white',
            }}
          />
        </React.Fragment>
      )}
      onInputChange={(e, val) => setsearchText(val)}
      renderInput={params => (
        <TextField
          {...params}
          variant="outlined"
          fullWidth
          {...TextFieldProps}
          {...params}
          InputProps={{
            ...(TextFieldProps.InputProps || {}),
            ...(params.InputProps || {}),
            onClick: e => e && e.stopPropagation(),
          }}
          inputProps={{
            ...(params.inputProps || {}),
            onClick: e => {
              e && e.stopPropagation();
              params.inputProps && params.inputProps.onClick && params.inputProps.onClick(e);
            },
          }}
          InputLabelProps={{
            className: classes && classes.label,
          }}
          className={classNameProp}
        />
      )}
    />
  );
}
