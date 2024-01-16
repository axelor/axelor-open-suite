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
import React, { useState } from 'react';
import clsx from 'clsx';
import PropTypes from 'prop-types';
import { FormControl, MenuItem, Typography, TextField, Chip } from '@material-ui/core';
import { emphasize } from '@material-ui/core/styles/colorManipulator';
import { makeStyles } from '@material-ui/styles';
import CancelIcon from '@material-ui/icons/Cancel';
import FileIcon from '@material-ui/icons/Description';
import Select from 'react-select';
import classNames from 'classnames';
import { useDebounce } from './utils';
import StringWidget from './string';

const useStyles = makeStyles(theme => ({
  formControl: {
    width: '100%',
    position: 'relative',
    '&.inline': {
      marginTop: 3,
    },
    '.inline &': {
      paddingBottom: 3,
    },
  },
  inputRoot: {
    '& > div': {
      marginTop: '12px',
    },
  },
  root: {
    flexGrow: 1,
  },
  inputWrapper: {
    '.inline & > *': {
      paddingBottom: 3,
    },
  },
  input: {
    display: 'flex',
    padding: 0,
    textOverflow: 'ellipsis',
    position: 'relative',
    whiteSpace: 'nowrap',
    height: 36,
    '.single': {
      height: 36,
    },
    '.inline &': {
      height: 26,
    },
  },
  valueContainer: {
    position: 'relative',
    display: 'flex',
    flexWrap: 'wrap',
    flex: 1,
    alignItems: 'center',
    overflow: 'hidden',
    '.single > &': {
      flexWrap: 'nowrap',
    },
    '.inline &': {
      flexWrap: 'nowrap',
    },
  },
  chip: {
    margin: theme.spacing(0.5, 0.25),
  },
  chipFocused: {
    backgroundColor: emphasize(
      theme.palette.type === 'light' ? theme.palette.grey[300] : theme.palette.grey[700],
      0.08,
    ),
  },
  noOptionsMessage: {
    padding: theme.spacing(1, 2),
  },
  singleValue: {
    fontSize: 16,
    textOverflow: 'ellipsis',
    overflow: 'hidden',
  },
  placeholder: {
    position: 'absolute',
    left: 2,
    bottom: 6,
    fontSize: 16,
    '.inline &': {
      bottom: 1,
    },
  },
  divider: {
    height: theme.spacing(2),
  },
  viewIcon: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    position: 'absolute',
    right: 70,
    bottom: 8,
    zIndex: 1,
    cursor: 'pointer',
    '& svg': {
      fontSize: 20,
      color: '#ddd',
      '&:hover': {
        color: '#555',
      },
    },
  },
}));

export function NoOptionsMessage(props) {
  return (
    <Typography color="textSecondary" className={props.selectProps.classes.noOptionsMessage} {...props.innerProps}>
      {props.children}
    </Typography>
  );
}

NoOptionsMessage.propTypes = {
  children: PropTypes.node,
  innerProps: PropTypes.object,
  selectProps: PropTypes.object.isRequired,
};

function inputComponent({ inputRef, ...props }) {
  return <div ref={inputRef} {...props} />;
}

inputComponent.propTypes = {
  inputRef: PropTypes.oneOfType([PropTypes.func, PropTypes.object]),
};

function Control(props) {
  const {
    children,
    innerProps,
    innerRef,
    selectProps: { classes, isMulti, TextFieldProps, InputProps },
  } = props;
  return (
    <TextField
      className={classes.inputWrapper}
      fullWidth
      classes={{
        ...(TextFieldProps && TextFieldProps.label && { root: classes.inputRoot }),
      }}
      InputProps={{
        ...InputProps,
        inputComponent,
        inputProps: {
          className: classNames(classes.input, {
            single: !isMulti,
          }),
          ref: innerRef,
          children,
          ...innerProps,
        },
      }}
      {...TextFieldProps}
    />
  );
}

Control.propTypes = {
  children: PropTypes.node,
  innerProps: PropTypes.object,
  innerRef: PropTypes.oneOfType([PropTypes.func, PropTypes.object]),
  selectProps: PropTypes.object.isRequired,
};

export function Option(props) {
  return (
    <MenuItem
      ref={props.innerRef}
      selected={props.isFocused}
      component="div"
      style={{
        fontWeight: props.isSelected ? 500 : 400,
      }}
      {...props.innerProps}
      className="menu-item"
    >
      {props.children}
    </MenuItem>
  );
}

Option.propTypes = {
  children: PropTypes.node,
  innerProps: PropTypes.object,
  innerRef: PropTypes.oneOfType([PropTypes.func, PropTypes.object]),
  isFocused: PropTypes.bool,
  isSelected: PropTypes.bool,
};

function Placeholder(props) {
  return (
    <Typography color="textSecondary" className={props.selectProps.classes.placeholder} {...props.innerProps}>
      {props.children}
    </Typography>
  );
}

Placeholder.propTypes = {
  children: PropTypes.node,
  innerProps: PropTypes.object,
  selectProps: PropTypes.object.isRequired,
};

export function SingleValue(props) {
  return (
    <Typography className={props.selectProps.classes.singleValue} {...props.innerProps}>
      {props.children}
    </Typography>
  );
}

SingleValue.propTypes = {
  children: PropTypes.node,
  innerProps: PropTypes.object,
  selectProps: PropTypes.object.isRequired,
};

function ValueContainer(props) {
  return (
    <div className={classNames(props.selectProps.classes.valueContainer, props.selectProps.valueContainerClassName)}>
      {props.children}
    </div>
  );
}

ValueContainer.propTypes = {
  children: PropTypes.node,
  selectProps: PropTypes.object.isRequired,
};

export function MultiValue(props) {
  const chipProps = {};
  if (!props.isDisabled) {
    chipProps['onDelete'] = props.removeProps.onClick;
  }
  return (
    <Chip
      tabIndex={-1}
      label={props.children}
      className={clsx(props.selectProps.classes.chip, {
        [props.selectProps.classes.chipFocused]: props.isFocused,
      })}
      deleteIcon={<CancelIcon {...props.removeProps} />}
      {...chipProps}
    />
  );
}

MultiValue.propTypes = {
  children: PropTypes.node,
  isFocused: PropTypes.bool,
  removeProps: PropTypes.object.isRequired,
  selectProps: PropTypes.object.isRequired,
};

const components = {
  Control,
  MultiValue,
  NoOptionsMessage,
  Option,
  Placeholder,
  SingleValue,
  ValueContainer,
};

function Selection(props) {
  const classes = useStyles();
  const [options, setOptions] = useState(props.options || []);
  const [loading, setLoading] = useState(false);
  const [selectedValue, setSelectedValue] = React.useState(null);
  const {
    name,
    title = '',
    onChange,
    value,
    error,
    readOnly = false,
    isMulti = false,
    isSearchable = false,
    optionValueKey = 'id',
    optionLabelKey = 'title',
    fetchAPI,
    beforeFetchAPI,
    isPortal,
    inline,
    autoFocus,
    onFocus: _onFocus,
    InputProps,
    formControlStyle,
    clearIndicatorStyle,
    onView,
    ...other
  } = props;

  const findOption = React.useCallback(
    value => {
      return props.options?.find(i => i[optionValueKey] === value);
    },
    [props.options, optionValueKey],
  );

  React.useEffect(() => {
    if (typeof value !== 'object') {
      const selectedItem = value.split(',');
      setSelectedValue(
        selectedItem.length === 1 ? findOption(selectedItem[0].trim()) : selectedItem.map(i => findOption(i.trim())),
      );
    } else {
      setSelectedValue(value);
    }
  }, [value, findOption]);

  async function fetch(value = '') {
    setLoading(true);
    if (beforeFetchAPI) {
      await beforeFetchAPI();
    }
    if (fetchAPI) {
      const data = await fetchAPI({ search: value });
      setOptions(data);
    } else {
      setOptions(props.options);
    }
    setLoading(false);
  }

  function onFocus() {
    _onFocus && _onFocus();
    fetch();
  }

  const delayFetch = useDebounce(fetch, 400);

  function onFilter(value, { action }) {
    // fetch only for input change case
    if (action !== 'input-change') return;
    setLoading(true);
    delayFetch(value);
  }

  function handleChange(item) {
    if (typeof value !== 'object') {
      isMulti ? onChange(item.map(i => i[optionValueKey]).join(',')) : onChange(item && item[optionValueKey]);
    } else {
      onChange(item);
    }
  }

  function onViewClick(e) {
    e.stopPropagation();
    onView(value);
  }

  function getValueTitle(value) {
    const option = options.find(x => `${x.value}` === `${value}`);
    return (option && option[optionLabelKey]) || value;
  }
  return readOnly ? (
    <StringWidget title={title} value={getValueTitle(value)} />
  ) : (
    <FormControl className={classNames(classes.formControl, { inline })} style={formControlStyle}>
      <Select
        autoFocus={autoFocus}
        name={name}
        InputProps={InputProps}
        TextFieldProps={{
          ...(inline ? {} : { label: title, InputLabelProps: { shrink: true } }),
          error,
        }}
        isLoading={loading}
        onFocus={onFocus}
        isClearable
        options={loading ? [] : options}
        onInputChange={onFilter}
        onChange={handleChange}
        value={selectedValue}
        placeholder={isSearchable ? `Search ${title}` : `Select ${title}`}
        classes={classes}
        components={components}
        isDisabled={readOnly}
        isMulti={isMulti}
        isSearchable={isSearchable}
        getOptionLabel={e => e[optionLabelKey]}
        getOptionValue={e => e[optionValueKey]}
        blurInputOnSelect
        menuShouldBlockScroll
        menuPlacement="auto"
        styles={{
          menuPortal: styles => ({ ...styles, zIndex: 1500 }),
          container: styles => ({ width: '100%' }),
          clearIndicator: styles => ({ ...styles, ...clearIndicatorStyle }),
          menu: styles => ({ ...styles, marginTop: 0, borderRadius: 0 }),
        }}
        {...(other.options ? {} : { filterOption: () => true })}
        {...(isPortal ? { menuPortalTarget: document.body } : {})}
        {...other}
      />
      {value && onView && (
        <div onClick={onViewClick} className={classes.viewIcon}>
          <FileIcon />
        </div>
      )}
    </FormControl>
  );
}
Selection.propTypes = {
  name: PropTypes.string,
  title: PropTypes.string,
  onChange: PropTypes.func,
  onView: PropTypes.func,
  value: PropTypes.any,
  readOnly: PropTypes.bool,
  isMulti: PropTypes.bool,
  isSearchable: PropTypes.bool,
  isPortal: PropTypes.bool,
  beforeFetchAPI: PropTypes.func,
};

Selection.defaultProps = {
  isPortal: true,
};

export default Selection;
