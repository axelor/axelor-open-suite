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
import { makeStyles } from '@material-ui/core/styles';
import { Select, FormControl, Chip, MenuItem } from '@material-ui/core';
import DoneIcon from '@material-ui/icons/Done';
import classnames from 'classnames';

import { translate } from '../utils';

const useStyles = makeStyles(theme => ({
  select: {
    marginBottom: 0,
    minWidth: 200,
    textAlign: 'left',
    [theme.breakpoints.only('xs')]: {
      minWidth: 150,
    },
  },
  selectPadding: {
    padding: 10,
  },
  formControl: {
    minWidth: 200,
    [theme.breakpoints.only('xs')]: {
      minWidth: 150,
    },
  },
  valueChip: {
    height: 20,
    color: 'white',
  },
  itemChip: {
    height: 25,
    color: 'white',
    marginLeft: 10,
  },
  noneMenuItem: {
    paddingLeft: 50,
  },
  menuItemIcon: {
    fontSize: 20,
    visibility: 'hidden',
  },
  menuItemIconActive: {
    visibility: 'visible',
  },
}));

function MenuItemContent({ active, label, backgroundColor }) {
  const classes = useStyles();
  return (
    <>
      <DoneIcon
        className={classnames(classes.menuItemIcon, {
          [classes.menuItemIconActive]: active,
        })}
      />
      {label && (
        <Chip
          label={label}
          className={classes.itemChip}
          style={{
            backgroundColor,
          }}
        />
      )}
    </>
  );
}

function StaticSelect({ name, className, value, onChange, options, getAvatarColor, hasNoneOption = false }) {
  const { id } = value || {};
  const classes = useStyles();

  function getItemLabel(option) {
    return option && translate(`value:${option.name}`) !== `value:${option.name}`
      ? translate(`value:${option.name}`)
      : option.name;
  }

  function getItemBackground(option) {
    return (option && option.$background) || (option && getAvatarColor && getAvatarColor(option.name)) || 'lightgray';
  }

  function findOption(id) {
    return options.find(option => option.id === id);
  }

  function handleChange(e) {
    onChange(findOption(e.target.value) || null, e);
  }

  const renderValue = value => {
    const option = findOption(value);
    return (
      option && (
        <Chip
          className={classes.valueChip}
          label={getItemLabel(option)}
          style={{
            background: getItemBackground(option),
          }}
        />
      )
    );
  };

  return (
    <FormControl variant="outlined" className={classnames(classes.formControl, className)}>
      <Select
        classes={{
          select: classes.selectPadding,
        }}
        className={classes.select}
        inputProps={{
          name: `${name}`,
          id: `outlined-${name}-native-simple`,
        }}
        value={id || ''}
        onChange={handleChange}
        renderValue={renderValue}
      >
        {hasNoneOption && (
          <MenuItem className={classes.noneMenuItem} value={''}>
            <em>{translate('None')}</em>
          </MenuItem>
        )}
        {options &&
          options.map(option => (
            <MenuItem value={option.id} key={option.id}>
              <MenuItemContent
                active={id === option.id}
                label={getItemLabel(option)}
                backgroundColor={getItemBackground(option)}
              />
            </MenuItem>
          ))}
      </Select>
    </FormControl>
  );
}

StaticSelect.defaultProps = {
  options: [],
  onChange: () => {},
};

export default StaticSelect;
