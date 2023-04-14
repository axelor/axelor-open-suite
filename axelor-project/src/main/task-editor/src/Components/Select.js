import React, { useState, useEffect, useCallback } from 'react';
import clsx from 'clsx';
import AutoComplete from '@material-ui/lab/Autocomplete';
import { TextField } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
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

const useStyles = makeStyles({
  autoComplete: {
    '& > div > label': {
      fontSize: 14,
    },
  },
  input: {
    fontSize: 14,
    color: 'black',
  },
  label: {
    fontSize: 14,
  },
});

export default function SelectComponent({
  name,
  model,
  optionLabel = 'name',
  multiple = false,
  required,
  index,
  value,
  update,
  disabled,
  criteriaIds,
  type,
  operator = 'and',
  classes: classesProp = {},
  className: classNameProp,
  size,
  disableClearable,
  TextFieldProps = {},
  options: propOptions,
  criteria: propCriteria,
  label,
}) {
  const [open, setOpen] = React.useState(false);
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
      }).then(({ data } = {}) => {
        if (data && data.length) {
          setOptions(data);
        }
      });
    },
    [model, operator, optionLabel, propCriteria],
  );

  const optionDebounceHandler = React.useCallback(() => {
    if (searchText) {
      fetchOptions(searchText);
    }
  }, [fetchOptions, searchText]);

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
    <AutoComplete
      classes={{
        inputFocused: classes.input,
        clearIndicator: classes.input,
        popupIndicator: classes.input,
        disabled: classes.disabled,
        ...classesProp,
      }}
      size={size}
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
      onClick={e => e && e.stopPropagation()}
      disableClearable={disableClearable}
      clearOnEscape
      autoComplete
      className={clsx(classes.autoComplete, classNameProp)}
      options={options}
      multiple={multiple}
      required={required}
      value={value || {}}
      getOptionSelected={(option, val) => {
        return option.id === val.id;
      }}
      onChange={(e, value) => {
        let values = value;
        if (type === 'multiple') {
          values = value && value.filter((val, i, self) => i === self.findIndex(t => t.id === val.id));
        }
        update(
          {
            [name]: values,
          },
          e,
        );
      }}
      name={name}
      disabled={disabled}
      onInputChange={(e, val) => setsearchText(val)}
      renderInput={params => (
        <TextField
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
          label={label}
        />
      )}
      getOptionLabel={option => option[optionLabel] || ''}
    />
  );
}
